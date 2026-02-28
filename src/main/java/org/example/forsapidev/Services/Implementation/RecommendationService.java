package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IAIExplainabilityService;
import org.example.forsapidev.Services.Interfaces.IRecommendationService;
import org.example.forsapidev.Services.Interfaces.IScoringFactorService;
import org.example.forsapidev.entities.ScoringManagement.*;
import org.example.forsapidev.Repositories.ScoringManagement.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService implements IRecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final IScoringFactorService factorService;
    private final IAIExplainabilityService aiService;

    @Override
    @Transactional
    public List<Recommendation> generateRecommendations(ScoreResult scoreResult) {
        log.info("Generating recommendations for client: {}", scoreResult.getClientId());

        List<Recommendation> recommendations = new ArrayList<>();

        Recommendation amountRec = generateAmountRecommendation(scoreResult);
        recommendations.add(amountRec);

        try {
            List<Recommendation> aiRecs = aiService.generateImprovementRecommendations(scoreResult);
            recommendations.addAll(aiRecs);
        } catch (Exception e) {
            log.error("Failed to generate AI recommendations", e);
            recommendations.addAll(generateAlgorithmicRecommendations(scoreResult));
        }

        return recommendationRepository.saveAll(recommendations);
    }

    @Override
    public List<Recommendation> getActiveRecommendations(Long clientId) {
        return recommendationRepository
                .findByClientIdAndIsActiveTrueOrderByPriorityDesc(clientId);
    }

    @Override
    @Transactional
    public Recommendation markAsCompleted(Long recommendationId) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recommendation not found: " + recommendationId));

        rec.setCompletedAt(LocalDateTime.now());
        rec.setIsActive(false);

        return recommendationRepository.save(rec);
    }

    private Recommendation generateAmountRecommendation(ScoreResult scoreResult) {
        double monthlyIncome = factorService.getMonthlyIncome(scoreResult.getClientId());
        double currentDTI = factorService.getCurrentDTI(scoreResult.getClientId());

        double multiplier = getMultiplierByCategory(scoreResult.getRiskCategory());
        double scoreBasedMax = monthlyIncome * multiplier;

        double remainingDTI = 0.40 - currentDTI;
        double maxMonthlyPayment = monthlyIncome * remainingDTI;
        double dtiBasedMax = maxMonthlyPayment * 6 / 1.1;

        double maxAmount = Math.min(scoreBasedMax, dtiBasedMax);
        double minAmount = 200.0;

        String description = generateAmountDescription(
                scoreResult.getFinalScore(),
                scoreResult.getRiskCategory(),
                minAmount,
                maxAmount
        );

        return Recommendation.builder()
                .clientId(scoreResult.getClientId())
                .scoreResultId(scoreResult.getId())
                .title("💰 Montant disponible")
                .description(description)
                .recommendedMinAmount(minAmount)
                .recommendedMaxAmount(maxAmount)
                .priority(RecommendationPriority.HIGH)
                .isFromAI(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    private List<Recommendation> generateAlgorithmicRecommendations(ScoreResult scoreResult) {
        List<Recommendation> recs = new ArrayList<>();

        if (scoreResult.getFactor3Score() < 70) {
            recs.add(Recommendation.builder()
                    .clientId(scoreResult.getClientId())
                    .scoreResultId(scoreResult.getId())
                    .title("💡 Réduis ton endettement")
                    .description("Ton ratio d'endettement est élevé. Rembourse une partie de tes dettes actuelles pour améliorer ton score.")
                    .priority(RecommendationPriority.HIGH)
                    .estimatedScoreImpact(8.0)
                    .impactExplanation("Baisse de DTI de 10% = +8 points")
                    .isFromAI(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (scoreResult.getFactor2Score() < 70) {
            recs.add(Recommendation.builder()
                    .clientId(scoreResult.getClientId())
                    .scoreResultId(scoreResult.getId())
                    .title("⏰ Paie tes factures à temps")
                    .description("Tu as des retards de paiement. Paie toutes tes factures à temps pendant 3 mois pour améliorer ton historique.")
                    .priority(RecommendationPriority.HIGH)
                    .estimatedScoreImpact(5.0)
                    .impactExplanation("3 mois sans retard = +5 points")
                    .isFromAI(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (scoreResult.getFactor1Score() < 70) {
            recs.add(Recommendation.builder()
                    .clientId(scoreResult.getClientId())
                    .scoreResultId(scoreResult.getId())
                    .title("📊 Stabilise tes revenus")
                    .description("Tes revenus varient beaucoup d'un mois à l'autre. Essaie de trouver un emploi plus stable pour améliorer ton score.")
                    .priority(RecommendationPriority.MEDIUM)
                    .estimatedScoreImpact(10.0)
                    .impactExplanation("Revenus stables = +10 points")
                    .isFromAI(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return recs;
    }

    private double getMultiplierByCategory(RiskCategory category) {
        return switch (category) {
            case EXCELLENT -> 6.0;
            case GOOD -> 4.0;
            case MODERATE -> 2.5;
            case RISKY -> 1.5;
            case VERY_RISKY -> 0.0;
        };
    }

    private String generateAmountDescription(double score, RiskCategory category,
                                             double min, double max) {
        return switch (category) {
            case EXCELLENT -> String.format(
                    "Excellente nouvelle ! Avec ton score de %.2f/100, tu peux emprunter entre %.0f et %.0f TND. " +
                            "Ton profil est excellent, aucune garantie requise.",
                    score, min, max);
            case GOOD -> String.format(
                    "Bien joué ! Avec ton score de %.2f/100, tu peux emprunter entre %.0f et %.0f TND. " +
                            "Ton profil est bon.",
                    score, min, max);
            case MODERATE -> String.format(
                    "Avec ton score de %.2f/100, tu peux emprunter entre %.0f et %.0f TND. " +
                            "Une garantie pourrait être requise.",
                    score, min, max);
            case RISKY -> String.format(
                    "Ton score actuel de %.2f/100 limite ton accès au crédit. " +
                            "Tu peux emprunter entre %.0f et %.0f TND uniquement. " +
                            "Des garanties seront requises.",
                    score, min, max);
            default -> "Crédit non disponible pour le moment.";
        };
    }
}