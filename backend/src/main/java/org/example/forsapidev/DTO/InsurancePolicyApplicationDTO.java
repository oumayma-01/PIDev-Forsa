package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class InsurancePolicyApplicationDTO {
    // User & Product Info
    private Long userId;
    private Long productId;

    // Coverage Details
    private BigDecimal desiredCoverage;
    private Integer durationMonths;
    private String paymentFrequency;  // MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL

    // Risk Assessment
    private InsuranceRiskAssessmentDTO riskProfile;

    // Constructor
    public InsurancePolicyApplicationDTO() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getDesiredCoverage() { return desiredCoverage; }
    public void setDesiredCoverage(BigDecimal desiredCoverage) { this.desiredCoverage = desiredCoverage; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public InsuranceRiskAssessmentDTO getRiskProfile() { return riskProfile; }
    public void setRiskProfile(InsuranceRiskAssessmentDTO riskProfile) { this.riskProfile = riskProfile; }
}