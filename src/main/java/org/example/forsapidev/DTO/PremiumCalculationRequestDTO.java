package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class PremiumCalculationRequestDTO {
    private String insuranceType;  // HEALTH, LIFE, PROPERTY, etc.
    private BigDecimal coverageAmount;  // Capital assuré
    private Integer durationMonths;
    private String paymentFrequency;  // MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    private InsuranceRiskAssessmentDTO riskProfile;

    // Constructor
    public PremiumCalculationRequestDTO() {}

    // Getters and Setters
    public String getInsuranceType() { return insuranceType; }
    public void setInsuranceType(String insuranceType) { this.insuranceType = insuranceType; }

    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(BigDecimal coverageAmount) { this.coverageAmount = coverageAmount; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public InsuranceRiskAssessmentDTO getRiskProfile() { return riskProfile; }
    public void setRiskProfile(InsuranceRiskAssessmentDTO riskProfile) { this.riskProfile = riskProfile; }
}