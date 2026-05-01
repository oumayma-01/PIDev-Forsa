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

        int score;
        String explanation = "";

        try {
            double avgIncome = features.getAvgMonthlyIncome() != null ? features.getAvgMonthlyIncome() : 0.0;
            Map<String, Object> python = aiAgentClient.calculateScore(
                    clientId, avgIncome, stegActive, sonedeActive, false);
            score = ((Number) python.getOrDefault("score", 0)).intValue();
            Object expl = python.get("explanation");
            if (expl != null) explanation = expl.toString();
        } catch (Exception e) {
            log.warn("Python AI unreachable for client {}: {}", clientId, e.getMessage());
            score = existing != null ? existing.getCurrentScore() : 0;
            explanation = existing != null && existing.getAiExplanation() != null
                    ? existing.getAiExplanation() : "";
        }

        boolean hasActiveCredit = Boolean.TRUE.equals(features.getHasActiveCredit());
        double threshold = computeThreshold(score, features.getAvgMonthlyIncome());
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
        return aiScoreRepository.save(aiScore);
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean isActive(LocalDateTime expiry) {
        return expiry != null && expiry.isAfter(LocalDateTime.now());
    }

    private double computeThreshold(int score, Double avgIncome) {
        if (avgIncome == null || avgIncome <= 0) return 0;
        double multiplier;
        if (score >= 800)      multiplier = 4.5;
        else if (score >= 600) multiplier = 2.5;
        else if (score >= 400) multiplier = 1.5;
        else                   multiplier = 0.0;
        return avgIncome * multiplier;
    }
}
