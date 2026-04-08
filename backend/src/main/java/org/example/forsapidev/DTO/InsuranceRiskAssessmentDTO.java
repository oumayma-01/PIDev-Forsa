package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class InsuranceRiskAssessmentDTO {
    private Integer age;
    private String gender;
    private BigDecimal monthlyIncome;
    private String occupationType;
    private String healthStatus;
    private Boolean hasChronicIllness;
    private Boolean isSmoker;
    private String locationRiskLevel;  // LOW, MEDIUM, HIGH
    private Integer dependents;
    private BigDecimal existingDebts;

    // Calculated fields
    private Double riskScore;
    private String riskCategory;  // LOW_RISK, MEDIUM_RISK, HIGH_RISK
    private Double riskCoefficient;

    // Constructor
    public InsuranceRiskAssessmentDTO() {}

    // Getters and Setters
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getOccupationType() { return occupationType; }
    public void setOccupationType(String occupationType) { this.occupationType = occupationType; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public Boolean getHasChronicIllness() { return hasChronicIllness; }
    public void setHasChronicIllness(Boolean hasChronicIllness) { this.hasChronicIllness = hasChronicIllness; }

    public Boolean getIsSmoker() { return isSmoker; }
    public void setIsSmoker(Boolean isSmoker) { this.isSmoker = isSmoker; }

    public String getLocationRiskLevel() { return locationRiskLevel; }
    public void setLocationRiskLevel(String locationRiskLevel) { this.locationRiskLevel = locationRiskLevel; }

    public Integer getDependents() { return dependents; }
    public void setDependents(Integer dependents) { this.dependents = dependents; }

    public BigDecimal getExistingDebts() { return existingDebts; }
    public void setExistingDebts(BigDecimal existingDebts) { this.existingDebts = existingDebts; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }

    public Double getRiskCoefficient() { return riskCoefficient; }
    public void setRiskCoefficient(Double riskCoefficient) { this.riskCoefficient = riskCoefficient; }
}