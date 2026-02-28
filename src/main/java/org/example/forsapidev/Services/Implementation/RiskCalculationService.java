package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IRiskCalculationService;
import org.example.forsapidev.Services.Interfaces.IScoringFactorService;
import org.example.forsapidev.entities.ScoringManagement.RiskMetrics;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.example.forsapidev.Repositories.ScoringManagement.RiskMetricsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class RiskCalculationService implements IRiskCalculationService {

    private final RiskMetricsRepository riskMetricsRepository;
    private final IScoringFactorService factorService;

    @Override
    @Transactional
    public RiskMetrics calculateRiskMetrics(ScoreResult scoreResult, Double loanAmount, Integer durationMonths) {
        log.info("Calculating risk metrics for client: {}", scoreResult.getClientId());

        double pd = calculatePD(scoreResult.getFinalScore());
        double lgd = calculateLGD(scoreResult.getClientId(), loanAmount);
        double ead = loanAmount;
        double el = pd * lgd * ead;
        double rate = calculatePersonalizedRate(pd, lgd);

        RiskMetrics metrics = RiskMetrics.builder()
                .clientId(scoreResult.getClientId())
                .scoreResultId(scoreResult.getId())
                .probabilityOfDefault(pd)
                .lossGivenDefault(lgd)
                .exposureAtDefault(ead)
                .expectedLoss(el)
                .loanAmount(loanAmount)
                .loanDurationMonths(durationMonths)
                .personalizedInterestRate(rate)
                .collateralValue(getCollateralValue(scoreResult.getClientId()))
                .seizeableIncome(getSeizeableIncome(scoreResult.getClientId()))
                .guarantorCapacity(getGuarantorCapacity(scoreResult.getClientId()))
                .recoveryCosts(150.0)
                .calculationDate(LocalDateTime.now())
                .calculationVersion("v1.0")
                .build();

        RiskMetrics saved = riskMetricsRepository.save(metrics);

        log.info("Risk metrics calculated - PD: {}%, LGD: {}%, Rate: {}%",
                pd * 100, lgd * 100, rate * 100);

        return saved;
    }

    @Override
    public RiskMetrics getLatestRiskMetrics(Long clientId) {
        return riskMetricsRepository.findLatestByClientId(clientId)
                .orElseThrow(() -> new RuntimeException(
                        "No risk metrics found for client: " + clientId
                ));
    }

    private double calculatePD(double score) {
        double pd = 0.40 - (score / 100.0) * 0.38;
        return Math.max(0.02, Math.min(0.40, pd));
    }

    private double calculateLGD(Long clientId, Double loanAmount) {
        double collateral = getCollateralValue(clientId);
        double seizable = getSeizeableIncome(clientId);
        double guarantor = getGuarantorCapacity(clientId);
        double costs = 150.0;

        double recoverable = collateral + seizable + guarantor - costs;
        double lgd = 1 - (recoverable / loanAmount);

        return Math.max(0.30, Math.min(1.0, lgd));
    }

    private double calculatePersonalizedRate(double pd, double lgd) {
        double riskPremium = pd * lgd;
        double riskFreeRate = 0.05;
        double operationalCosts = 0.02;
        double profitMargin = 0.02;

        double rate = riskPremium + riskFreeRate + operationalCosts + profitMargin;

        return Math.max(0.07, Math.min(0.20, rate));
    }

    private double getCollateralValue(Long clientId) {
        return 200.0;
    }

    private double getSeizeableIncome(Long clientId) {
        double monthlyIncome = factorService.getMonthlyIncome(clientId);
        return monthlyIncome * 0.30 * 6;
    }

    private double getGuarantorCapacity(Long clientId) {
        return 100.0;
    }
}