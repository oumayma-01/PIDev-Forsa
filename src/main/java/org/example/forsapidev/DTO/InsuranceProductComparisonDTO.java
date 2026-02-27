package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class InsuranceProductComparisonDTO {
    private Long id;
    private String productName;
    private String policyType;
    private BigDecimal premiumAmount;
    private BigDecimal coverageLimit;
    private Integer durationMonths;
    private String description;
    private Boolean isActive;

    // Calculated fields
    private Double valueScore;  // Coverage / Premium ratio
    private String valueRating;  // "Best Value", "Good Value", "Standard"
    private BigDecimal costPerMonth;
    private BigDecimal coveragePerDollar;

    // Constructor
    public InsuranceProductComparisonDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; }

    public BigDecimal getCoverageLimit() { return coverageLimit; }
    public void setCoverageLimit(BigDecimal coverageLimit) { this.coverageLimit = coverageLimit; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Double getValueScore() { return valueScore; }
    public void setValueScore(Double valueScore) { this.valueScore = valueScore; }

    public String getValueRating() { return valueRating; }
    public void setValueRating(String valueRating) { this.valueRating = valueRating; }

    public BigDecimal getCostPerMonth() { return costPerMonth; }
    public void setCostPerMonth(BigDecimal costPerMonth) { this.costPerMonth = costPerMonth; }

    public BigDecimal getCoveragePerDollar() { return coveragePerDollar; }
    public void setCoveragePerDollar(BigDecimal coveragePerDollar) { this.coveragePerDollar = coveragePerDollar; }
}