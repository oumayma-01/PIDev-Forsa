package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Config.ActuarialConstants;
import org.example.forsapidev.DTO.PremiumCalculationRequestDTO;
import org.example.forsapidev.DTO.PremiumCalculationResultDTO;
import org.example.forsapidev.DTO.InsuranceRiskAssessmentDTO;
import org.example.forsapidev.Services.Interfaces.IPremiumCalculationService;
import org.example.forsapidev.Services.Interfaces.IInsuranceRiskAssessmentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PremiumCalculationService implements IPremiumCalculationService {

    private final IInsuranceRiskAssessmentService riskAssessmentService;

    public PremiumCalculationService(IInsuranceRiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @Override
    public PremiumCalculationResultDTO calculatePremium(PremiumCalculationRequestDTO request) {
        // 1. Assess risk
        InsuranceRiskAssessmentDTO riskProfile = riskAssessmentService.calculateRiskScore(request.getRiskProfile());

        // 2. Calculate Pure Premium based on insurance type
        BigDecimal purePremium = calculatePurePremium(
                request.getInsuranceType(),
                request.getCoverageAmount(),
                riskProfile.getRiskCoefficient()
        );

        // 3. Calculate Inventory Premium (Prime Inventaire)
        BigDecimal inventoryPremium = calculateInventoryPremium(purePremium, request.getCoverageAmount());

        // 4. Calculate Commercial Premium (Prime Commerciale)
        BigDecimal commercialPremium = calculateCommercialPremium(inventoryPremium);

        // 5. Calculate Final Premium (Prime Finale)
        BigDecimal finalPremium = calculateFinalPremium(commercialPremium);

        // 6. Calculate periodic payment based on frequency
        int periodsPerYear = getPeriodsPerYear(request.getPaymentFrequency());
        int totalPayments = (request.getDurationMonths() / 12) * periodsPerYear;
        BigDecimal periodicPayment = finalPremium.divide(
                BigDecimal.valueOf(totalPayments), 2, RoundingMode.HALF_UP
        );

        // 7. Build result
        PremiumCalculationResultDTO result = new PremiumCalculationResultDTO();
        result.setPurePremium(purePremium);
        result.setInventoryPremium(inventoryPremium);
        result.setCommercialPremium(commercialPremium);
        result.setFinalPremium(finalPremium);
        result.setPeriodicPayment(periodicPayment);
        result.setPaymentFrequency(request.getPaymentFrequency());
        result.setNumberOfPayments(totalPayments);
        result.setCoverageAmount(request.getCoverageAmount());
        result.setRiskScore(riskProfile.getRiskScore());
        result.setRiskCategory(riskProfile.getRiskCategory());
        result.setEffectiveAnnualRate(ActuarialConstants.ANNUAL_TECHNICAL_RATE);
        result.setCalculationMethod(getCalculationMethod(request.getInsuranceType()));
        result.setAdditionalNotes(generateNotes(request, riskProfile));

        System.out.println("✅ Premium calculation completed: Final Premium = " + finalPremium);

        return result;
    }

    /**
     * Calculate Pure Premium using formula: E(S) = E(N) × E(X)
     * Where E(N) = frequency, E(X) = severity × coverage
     */
    private BigDecimal calculatePurePremium(String insuranceType, BigDecimal coverageAmount,
                                            double riskCoefficient) {
        double frequency = getClaimFrequency(insuranceType);
        double severity = getClaimSeverity(insuranceType);

        // E(S) = frequency × (severity × coverage) × risk_coefficient
        double expectedLoss = frequency * severity * coverageAmount.doubleValue() * riskCoefficient;

        return BigDecimal.valueOf(expectedLoss).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Inventory Premium: Prime Inventaire = Prime Pure + Management Fees
     */
    private BigDecimal calculateInventoryPremium(BigDecimal purePremium, BigDecimal coverageAmount) {
        BigDecimal managementFees = coverageAmount.multiply(
                BigDecimal.valueOf(ActuarialConstants.MANAGEMENT_FEE_RATE)
        );
        return purePremium.add(managementFees).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Commercial Premium: Prime Commerciale = Prime Inventaire / (1 - α)
     */
    private BigDecimal calculateCommercialPremium(BigDecimal inventoryPremium) {
        double denominator = 1 - ActuarialConstants.ACQUISITION_COST_RATE;
        return inventoryPremium.divide(
                BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP
        );
    }

    /**
     * Calculate Final Premium: Prime Finale = Prime Commerciale × (1 + profit_margin)
     */
    private BigDecimal calculateFinalPremium(BigDecimal commercialPremium) {
        return commercialPremium.multiply(
                BigDecimal.valueOf(1 + ActuarialConstants.PROFIT_MARGIN_RATE)
        ).setScale(2, RoundingMode.HALF_UP);
    }

    private double getClaimFrequency(String insuranceType) {
        switch (insuranceType.toUpperCase()) {
            case "HEALTH":
                return ActuarialConstants.HEALTH_CLAIM_FREQUENCY;
            case "LIFE":
                return ActuarialConstants.LIFE_CLAIM_FREQUENCY;
            case "PROPERTY":
                return ActuarialConstants.PROPERTY_CLAIM_FREQUENCY;
            case "ACCIDENT":
                return ActuarialConstants.ACCIDENT_CLAIM_FREQUENCY;
            case "CROP":
                return ActuarialConstants.CROP_CLAIM_FREQUENCY;
            case "LIVESTOCK":
                return ActuarialConstants.LIVESTOCK_CLAIM_FREQUENCY;
            case "BUSINESS":
                return ActuarialConstants.BUSINESS_CLAIM_FREQUENCY;
            default:
                return 0.10; // default 10%
        }
    }

    private double getClaimSeverity(String insuranceType) {
        switch (insuranceType.toUpperCase()) {
            case "HEALTH":
                return ActuarialConstants.HEALTH_SEVERITY;
            case "LIFE":
                return ActuarialConstants.LIFE_SEVERITY;
            case "PROPERTY":
                return ActuarialConstants.PROPERTY_SEVERITY;
            case "ACCIDENT":
                return ActuarialConstants.ACCIDENT_SEVERITY;
            case "CROP":
                return ActuarialConstants.CROP_SEVERITY;
            case "LIVESTOCK":
                return ActuarialConstants.LIVESTOCK_SEVERITY;
            case "BUSINESS":
                return ActuarialConstants.BUSINESS_SEVERITY;
            default:
                return 0.40; // default 40%
        }
    }

    private int getPeriodsPerYear(String paymentFrequency) {
        switch (paymentFrequency.toUpperCase()) {
            case "MONTHLY":
                return ActuarialConstants.MONTHLY_PERIODS;
            case "QUARTERLY":
                return ActuarialConstants.QUARTERLY_PERIODS;
            case "SEMI_ANNUAL":
                return ActuarialConstants.SEMI_ANNUAL_PERIODS;
            case "ANNUAL":
                return ActuarialConstants.ANNUAL_PERIODS;
            default:
                return ActuarialConstants.MONTHLY_PERIODS;
        }
    }

    private String getCalculationMethod(String insuranceType) {
        return "Frequency-Severity Method (E(S) = E(N) × E(X)) for " + insuranceType;
    }

    private String generateNotes(PremiumCalculationRequestDTO request, InsuranceRiskAssessmentDTO riskProfile) {
        return String.format(
                "Premium calculated for %s insurance with %s risk profile. " +
                        "Coverage: $%s for %d months. Payment frequency: %s.",
                request.getInsuranceType(),
                riskProfile.getRiskCategory(),
                request.getCoverageAmount(),
                request.getDurationMonths(),
                request.getPaymentFrequency()
        );
    }
}