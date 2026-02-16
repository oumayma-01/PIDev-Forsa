package org.example.forsapidev.services;

import org.example.forsapidev.repositories.InflationRateRepository;
import org.example.forsapidev.repositories.TmmRateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Interest Rate Engine applying business rules:
 * TauxFinal = TMM + AjustementDurée + (Inflation × Coefficient) + 2% + PrimeAssurance
 */
@Service
public class InterestRateEngineService {

    private final TmmRateRepository tmmRepo;
    private final InflationRateRepository inflationRepo;

    public InterestRateEngineService(TmmRateRepository tmmRepo, InflationRateRepository inflationRepo) {
        this.tmmRepo = tmmRepo;
        this.inflationRepo = inflationRepo;
    }

    // NOTE: removed static defaults for TMM and Inflation — admin must insert records via CRUD

    @Value("${engine.inflation.coefficient:0.5}")
    private BigDecimal inflationCoefficient; // 0.5

    @Value("${engine.client.adder:2.0}")
    private BigDecimal clientAdderPercent; // +2%

    @Value("${engine.insurance.premium:0.5}")
    private BigDecimal insurancePremiumPercent; // +0.5%

    /**
     * Compute final annual rate percent (e.g., 12.5 means 12.5%).
     * Throws IllegalStateException if TMM or Inflation for the request year are not configured.
     */
    public BigDecimal computeAnnualRatePercent(LocalDateTime requestDate,
                                              int durationMonths,
                                              BigDecimal tmmOverride,
                                              BigDecimal inflationOverride) {
        Integer year = requestDate != null ? requestDate.getYear() : null;

        BigDecimal tmm;
        if (tmmOverride != null) {
            tmm = tmmOverride;
        } else {
            if (year == null) throw new IllegalStateException("Request date year is required to lookup TMM and no override provided");
            tmm = tmmRepo.findByYear(year).map(r -> r.getPercent()).orElseThrow(() ->
                    new IllegalStateException("TMM for year " + year + " is not configured. Please add it via /api/admin/tmm"));
        }

        BigDecimal inflation;
        if (inflationOverride != null) {
            inflation = inflationOverride;
        } else {
            if (year == null) throw new IllegalStateException("Request date year is required to lookup Inflation and no override provided");
            inflation = inflationRepo.findByYear(year).map(r -> r.getPercent()).orElseThrow(() ->
                    new IllegalStateException("Inflation for year " + year + " is not configured. Please add it via /api/admin/inflation"));
        }

        // Ajustement selon durée
        BigDecimal durationAdj = BigDecimal.ZERO;
        if (durationMonths >= 7 && durationMonths <= 12) {
            durationAdj = new BigDecimal("1.5");
        } else if (durationMonths >= 13 && durationMonths <= 24) {
            durationAdj = new BigDecimal("3.0");
        } // 3–6 mois -> +0%

        // Inflation × coefficient
        BigDecimal inflationAdj = inflation.multiply(inflationCoefficient).setScale(4, RoundingMode.HALF_UP);

        // Taux final
        BigDecimal finalRatePercent = tmm
                .add(durationAdj)
                .add(inflationAdj)
                .add(clientAdderPercent) // tous les clients +2%
                .add(insurancePremiumPercent); // +0.5% d'assurance

        return finalRatePercent.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Convert rate percent to monthly interest amount based on principal.
     * interestMonthly = principal * (finalRatePercent/100) / 12
     */
    public BigDecimal computeMonthlyInterest(BigDecimal principal, BigDecimal annualRatePercent) {
        BigDecimal annualRate = annualRatePercent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        return principal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Fixed principal per month: principalMonthly = amountRequested / durationMonths
     */
    public BigDecimal computeMonthlyPrincipal(BigDecimal amountRequested, int durationMonths) {
        return amountRequested.divide(BigDecimal.valueOf(durationMonths), 2, RoundingMode.HALF_UP);
    }
}
