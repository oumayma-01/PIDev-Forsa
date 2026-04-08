package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IAIExplainabilityService;
import org.example.forsapidev.entities.ScoringManagement.Recommendation;
import org.example.forsapidev.entities.ScoringManagement.RecommendationPriority;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIExplainabilityService implements IAIExplainabilityService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Override
    public String generateScoreExplanation(ScoreResult scoreResult) {
        log.info("Generating AI explanation for score: {}", scoreResult.getId());

        return String.format(
                "Ton score est de %.2f/100 (%s). " +
                        "Tes points forts : stabilité des revenus (%.2f/100) et historique de paiements (%.2f/100). " +
                        "Pour améliorer ton score, concentre-toi sur la réduction de ton endettement.",
                scoreResult.getFinalScore(),
                scoreResult.getRiskCategory(),
                scoreResult.getFactor1Score(),
                scoreResult.getFactor2Score()
        );
    }

    @Override
    public List<Recommendation> generateImprovementRecommendations(ScoreResult scoreResult) {
        log.info("Generating AI recommendations for client: {}", scoreResult.getClientId());

        List<Recommendation> recommendations = new ArrayList<>();

        if (scoreResult.getFactor3Score() < 80) {
            recommendations.add(Recommendation.builder()
                    .clientId(scoreResult.getClientId())
                    .scoreResultId(scoreResult.getId())
                    .title("🎯 Optimise ton endettement")
                    .description("Ton DTI actuel impacte ton score. Rembourse 100 TND de dettes pour gagner 8 points et réduire ton taux de 1.5%.")
                    .priority(RecommendationPriority.HIGH)
                    .estimatedScoreImpact(8.0)
                    .impactExplanation(String.format("Score passerait de %.2f à %.2f",
                            scoreResult.getFinalScore(), scoreResult.getFinalScore() + 8))
                    .isFromAI(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (scoreResult.getFactor2Score() < 85) {
            recommendations.add(Recommendation.builder()
                    .clientId(scoreResult.getClientId())
                    .scoreResultId(scoreResult.getId())
                    .title("💳 Perfectionne ton historique")
                    .description("Paie toutes tes factures le jour même pendant 2 mois pour gagner 5 points supplémentaires.")
                    .priority(RecommendationPriority.MEDIUM)
                    .estimatedScoreImpact(5.0)
                    .impactExplanation(String.format("Score passerait de %.2f à %.2f",
                            scoreResult.getFinalScore(), scoreResult.getFinalScore() + 5))
                    .isFromAI(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return recommendations;
    }
}