package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IScoringAggregationService;
import org.example.forsapidev.Services.Interfaces.IScoringFactorService;
import org.example.forsapidev.entities.ScoringManagement.RiskCategory;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.example.forsapidev.Repositories.ScoringManagement.ScoreResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoringAggregationService implements IScoringAggregationService {

    private final IScoringFactorService factorService;
    private final ScoreResultRepository scoreResultRepository;

    private static final double WEIGHT_FACTOR_1 = 0.30;
    private static final double WEIGHT_FACTOR_2 = 0.30;
    private static final double WEIGHT_FACTOR_3 = 0.20;
    private static final double WEIGHT_FACTOR_4 = 0.15;
    private static final double WEIGHT_FACTOR_5 = 0.05;

    @Override
    @Transactional
    public ScoreResult calculateClientScore(Long clientId) {
        log.info("Starting score calculation for client: {}", clientId);

        double f1 = factorService.calculateRevenueStabilityFactor(clientId);
        double f2 = factorService.calculatePaymentHistoryFactor(clientId);
        double f3 = factorService.calculateDebtRatioFactor(clientId);
        double f4 = factorService.calculateEmploymentTypeFactor(clientId);
        double f5 = factorService.calculateRegionFactor(clientId);

        double c1 = f1 * WEIGHT_FACTOR_1;
        double c2 = f2 * WEIGHT_FACTOR_2;
        double c3 = f3 * WEIGHT_FACTOR_3;
        double c4 = f4 * WEIGHT_FACTOR_4;
        double c5 = f5 * WEIGHT_FACTOR_5;

        double finalScore = Math.round((c1 + c2 + c3 + c4 + c5) * 100.0) / 100.0;

        RiskCategory category = RiskCategory.fromScore(finalScore);

        ScoreResult result = ScoreResult.builder()
                .clientId(clientId)
                .finalScore(finalScore)
                .riskCategory(category)
                .factor1Score(f1)
                .factor2Score(f2)
                .factor3Score(f3)
                .factor4Score(f4)
                .factor5Score(f5)
                .factor1Contribution(c1)
                .factor2Contribution(c2)
                .factor3Contribution(c3)
                .factor4Contribution(c4)
                .factor5Contribution(c5)
                .calculationDate(LocalDateTime.now())
                .calculationVersion("v1.0")
                .calculatedBy("SYSTEM")
                .build();

        ScoreResult saved = scoreResultRepository.save(result);

        log.info("Score calculated for client {}: {} ({})",
                clientId, finalScore, category);

        return saved;
    }

    @Override
    public ScoreResult getLatestScore(Long clientId) {
        return scoreResultRepository.findLatestByClientId(clientId)
                .orElseThrow(() -> new RuntimeException(
                        "No score found for client: " + clientId
                ));
    }

    @Override
    @Transactional
    public ScoreResult getOrCalculateScore(Long clientId) {
        return scoreResultRepository.findLatestByClientId(clientId)
                .orElseGet(() -> calculateClientScore(clientId));
    }
}