package org.example.forsapidev.Services.aiScoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Repositories.AIScoreManagement.AIScoreRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.scoring.ScoreFeatureService;
import org.example.forsapidev.entities.AIScoreManagement.AIScore;
import org.example.forsapidev.entities.AIScoreManagement.AIScoreLevel;
import org.example.forsapidev.entities.ScoringManagement.ScoreFeatures;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.response.AIScoreSummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoScoringService {

    private final AIAgentClient aiAgentClient;
    private final AIScoreRepository aiScoreRepository;
    private final ScoreFeatureService scoreFeatureService;
    private final UserRepository userRepository;

    @Transactional
    public AIScore calculateAndSave(Long clientId) {
        ScoreFeatures features = scoreFeatureService.extractFeatures(clientId);

        AIScore existing = aiScoreRepository.findByClientId(clientId).orElse(null);

        boolean stegActive = isActive(existing != null ? existing.getStegBoosterExpiry() : null);
        boolean sonedeActive = isActive(existing != null ? existing.getSonedeBoosterExpiry() : null);

        // Priority: verifiedSalary (from OCR) > wallet avg income > default
        BigDecimal verifiedSal = existing != null ? existing.getVerifiedSalary() : null;
        Double avgIncomeRaw = features.getAvgMonthlyIncome();
        double effectiveSalary;
        if (verifiedSal != null && verifiedSal.doubleValue() > 0) {
            effectiveSalary = verifiedSal.doubleValue();
        } else if (avgIncomeRaw != null && avgIncomeRaw > 0) {
            effectiveSalary = avgIncomeRaw;
        } else {
            effectiveSalary = 500.0;
        }

        int score;
        String explanation = "";

        try {
            Map<String, Object> python = aiAgentClient.calculateScore(
                    clientId, effectiveSalary, stegActive, sonedeActive, false);
            score = ((Number) python.getOrDefault("score", 0)).intValue();
            Object expl = python.get("explanation");
            if (expl != null) explanation = expl.toString();
        } catch (Exception e) {
            log.warn("Python AI unreachable for client {}: {}", clientId, e.getMessage());
            if (existing != null) {
                score = existing.getCurrentScore();
                explanation = existing.getAiExplanation() != null ? existing.getAiExplanation() : "";
            } else {
                score = offlineFallbackScore(effectiveSalary, stegActive, sonedeActive);
                explanation = "Score estimated locally (AI service unavailable).";
            }
        }

        // Extend Python score with Java behavioral adjustments
        score = applyBoosterAdjustments(score, stegActive, sonedeActive);
        score = applyCreditBehavior(score, features);
        score = applyWalletHealth(score, features);
        score = applyActivity(score, features);
        score = clamp(score, 0, 1000);

        boolean hasActiveCredit = Boolean.TRUE.equals(features.getHasActiveCredit());
        double threshold = computeThreshold(score, effectiveSalary);
        if (hasActiveCredit) threshold = 0.0;

        AIScore aiScore = existing != null ? existing : AIScore.builder()
                .clientId(clientId)
                .totalCreditsTaken(0)
                .totalCreditsCompleted(0)
                .totalPaymentsOnTime(0)
                .totalPaymentsLate(0)
                .build();

        aiScore.setCurrentScore(score);
        aiScore.setScoreLevel(AIScoreLevel.fromScore(score));
        aiScore.setCreditThreshold(BigDecimal.valueOf(threshold));
        aiScore.setAvailableThreshold(hasActiveCredit ? BigDecimal.ZERO : BigDecimal.valueOf(threshold));
        aiScore.setHasActiveCredit(hasActiveCredit);
        aiScore.setLastCalculatedAt(LocalDateTime.now());
        aiScore.setScoreExpiresAt(LocalDateTime.now().plusMonths(6));
        if (!explanation.isEmpty()) aiScore.setAiExplanation(explanation);

        AIScore saved = aiScoreRepository.save(aiScore);
        log.info("Score client {}: {}/1000 {} seuil={}TND steg={} sonede={}",
                clientId, score, saved.getScoreLevel(), threshold, stegActive, sonedeActive);
        return saved;
    }

    /** Initialize first score for a new client using OCR-extracted data. */
    @Transactional
    public AIScore initializeFirstScore(Long clientId, Double ocrSalary, Boolean stegPaid, Boolean sonedePaid) {
        AIScore aiScore = aiScoreRepository.findByClientId(clientId).orElseGet(() ->
                AIScore.builder()
                        .clientId(clientId)
                        .currentScore(0)
                        .scoreLevel(AIScoreLevel.VERY_LOW)
                        .hasActiveCredit(false)
                        .totalCreditsTaken(0)
                        .totalCreditsCompleted(0)
                        .totalPaymentsOnTime(0)
                        .totalPaymentsLate(0)
                        .lastCalculatedAt(LocalDateTime.now())
                        .scoreExpiresAt(LocalDateTime.now().plusMonths(6))
                        .creditThreshold(BigDecimal.ZERO)
                        .availableThreshold(BigDecimal.ZERO)
                        .build());

        if (ocrSalary != null && ocrSalary > 0) {
            aiScore.setVerifiedSalary(BigDecimal.valueOf(ocrSalary));
            log.info("Setting verifiedSalary={} for client {}", ocrSalary, clientId);
        }

        LocalDateTime boosterExpiry = LocalDateTime.now().plusMonths(3);
        if (Boolean.TRUE.equals(stegPaid)) {
            aiScore.setStegBoosterExpiry(boosterExpiry);
            log.info("STEG booster activated for new client {}", clientId);
        }
        if (Boolean.TRUE.equals(sonedePaid)) {
            aiScore.setSonedeBoosterExpiry(boosterExpiry);
            log.info("SONEDE booster activated for new client {}", clientId);
        }

        aiScoreRepository.save(aiScore);
        return calculateAndSave(clientId);
    }

    @Transactional
    public AIScore activateBooster(Long clientId, String type) {
        AIScore aiScore = aiScoreRepository.findByClientId(clientId).orElseGet(() ->
                AIScore.builder()
                        .clientId(clientId)
                        .currentScore(0)
                        .scoreLevel(AIScoreLevel.VERY_LOW)
                        .hasActiveCredit(false)
                        .totalCreditsTaken(0)
                        .totalCreditsCompleted(0)
                        .totalPaymentsOnTime(0)
                        .totalPaymentsLate(0)
                        .lastCalculatedAt(LocalDateTime.now())
                        .scoreExpiresAt(LocalDateTime.now().plusMonths(6))
                        .creditThreshold(BigDecimal.ZERO)
                        .availableThreshold(BigDecimal.ZERO)
                        .build());

        LocalDateTime expiry = LocalDateTime.now().plusMonths(3);
        if ("STEG".equalsIgnoreCase(type)) {
            aiScore.setStegBoosterExpiry(expiry);
            log.info("STEG booster activated for client {} until {}", clientId, expiry);
        } else if ("SONEDE".equalsIgnoreCase(type)) {
            aiScore.setSonedeBoosterExpiry(expiry);
            log.info("SONEDE booster activated for client {} until {}", clientId, expiry);
        }
        // BUG FIX: save the booster state first, then recalculate so the new booster is reflected
        aiScoreRepository.save(aiScore);
        return calculateAndSave(clientId);
    }

    public List<AIScoreSummaryDto> getAllScoreSummaries() {
        return aiScoreRepository.findAll().stream()
                .map(s -> {
                    User u = userRepository.findById(s.getClientId()).orElse(null);
                    return AIScoreSummaryDto.builder()
                            .clientId(s.getClientId())
                            .clientName(u != null ? u.getUsername() : "Client #" + s.getClientId())
                            .clientEmail(u != null ? u.getEmail() : "")
                            .score(s.getCurrentScore())
                            .scoreLevel(s.getScoreLevel().name())
                            .creditThreshold(s.getCreditThreshold() != null
                                    ? s.getCreditThreshold().doubleValue() : null)
                            .hasActiveCredit(Boolean.TRUE.equals(s.getHasActiveCredit()))
                            .lastCalculatedAt(s.getLastCalculatedAt() != null
                                    ? s.getLastCalculatedAt().toString() : null)
                            .stegBoosterActive(isActive(s.getStegBoosterExpiry()))
                            .stegBoosterExpiry(s.getStegBoosterExpiry() != null
                                    ? s.getStegBoosterExpiry().toString() : null)
                            .sonedeBoosterActive(isActive(s.getSonedeBoosterExpiry()))
                            .sonedeBoosterExpiry(s.getSonedeBoosterExpiry() != null
                                    ? s.getSonedeBoosterExpiry().toString() : null)
                            .build();
                })
                .sorted(Comparator.comparingInt(AIScoreSummaryDto::getScore).reversed())
                .collect(Collectors.toList());
    }

    // ── Score extension methods ───────────────────────────────────────────────

    private int applyBoosterAdjustments(int score, boolean stegActive, boolean sonedeActive) {
        int bonus = 0;
        if (stegActive)   bonus += 15;
        if (sonedeActive) bonus += 15;
        return score + bonus;
    }

    private int applyCreditBehavior(int score, ScoreFeatures features) {
        double repayment = features.getRepaymentHistory() != null ? features.getRepaymentHistory() : 0.5;
        int delta;
        if      (repayment >= 0.9)  delta =  40;
        else if (repayment >= 0.75) delta =  25;
        else if (repayment >= 0.6)  delta =  10;
        else if (repayment <= 0.2)  delta = -50;
        else if (repayment <= 0.35) delta = -25;
        else                        delta =   0;
        return score + delta;
    }

    private int applyWalletHealth(int score, ScoreFeatures features) {
        double balance     = features.getCurrentBalance() != null ? features.getCurrentBalance() : 0.0;
        double savingsRate = features.getSavingsRate()    != null ? features.getSavingsRate()    : 0.0;
        int bonus = 0;
        if      (balance >= 5000) bonus += 20;
        else if (balance >= 2000) bonus += 12;
        else if (balance >= 500)  bonus += 5;
        if      (savingsRate >= 0.3)  bonus += 20;
        else if (savingsRate >= 0.15) bonus += 10;
        else if (savingsRate >= 0.05) bonus += 5;
        return score + bonus;
    }

    private int applyActivity(int score, ScoreFeatures features) {
        double activity  = features.getAccountActivity() != null ? features.getAccountActivity() : 0.0;
        double stability = features.getIncomeStability() != null ? features.getIncomeStability() : 1.0;
        int bonus = 0;
        if      (activity >= 1.0)  bonus += 15;
        else if (activity >= 0.3)  bonus += 8;
        else if (activity < 0.05)  bonus -= 10;
        if      (stability < 0.15) bonus += 15;
        else if (stability < 0.35) bonus += 8;
        else if (stability > 0.8)  bonus -= 10;
        return score + bonus;
    }

    private int clamp(int score, int min, int max) {
        return Math.max(min, Math.min(max, score));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean isActive(LocalDateTime expiry) {
        return expiry != null && expiry.isAfter(LocalDateTime.now());
    }

    private double computeThreshold(int score, double monthlySalary) {
        if (monthlySalary <= 0) return 0;
        double multiplier;
        if      (score >= 800) multiplier = 4.5;
        else if (score >= 600) multiplier = 2.5;
        else if (score >= 400) multiplier = 1.5;
        else                   multiplier = 0.0;
        return monthlySalary * multiplier;
    }

    private int offlineFallbackScore(double salary, boolean steg, boolean sonede) {
        int pts = (int) Math.min(400, Math.round(Math.min(salary, 10_000) / 10_000.0 * 400));
        if (steg)   pts += 100;
        if (sonede) pts += 100;
        pts += 120;
        return Math.min(1000, Math.max(200, pts));
    }
}
