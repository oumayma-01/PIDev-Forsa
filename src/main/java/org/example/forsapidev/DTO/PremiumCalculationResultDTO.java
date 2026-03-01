package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class PremiumCalculationResultDTO {
    private BigDecimal purePremium;  // Prime Pure
    private BigDecimal inventoryPremium;  // Prime Inventaire
    private BigDecimal commercialPremium;  // Prime Commerciale
    private BigDecimal finalPremium;  // Prime Finale

    private BigDecimal periodicPayment;  // Based on payment frequency
    private String paymentFrequency;
    private Integer numberOfPayments;

    private BigDecimal coverageAmount;
    private Double riskScore;
    private String riskCategory;
    private Double effectiveAnnualRate;

    private String calculationMethod;
    private String additionalNotes;

    // Constructor
    public PremiumCalculationResultDTO() {}

    // Getters and Setters (all fields)
    public BigDecimal getPurePremium() { return purePremium; }
    public void setPurePremium(BigDecimal purePremium) { this.purePremium = purePremium; }

    public BigDecimal getInventoryPremium() { return inventoryPremium; }
    public void setInventoryPremium(BigDecimal inventoryPremium) { this.inventoryPremium = inventoryPremium; }

    public BigDecimal getCommercialPremium() { return commercialPremium; }
    public void setCommercialPremium(BigDecimal commercialPremium) { this.commercialPremium = commercialPremium; }

    public BigDecimal getFinalPremium() { return finalPremium; }
    public void setFinalPremium(BigDecimal finalPremium) { this.finalPremium = finalPremium; }

    public BigDecimal getPeriodicPayment() { return periodicPayment; }
    public void setPeriodicPayment(BigDecimal periodicPayment) { this.periodicPayment = periodicPayment; }

    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public Integer getNumberOfPayments() { return numberOfPayments; }
    public void setNumberOfPayments(Integer numberOfPayments) { this.numberOfPayments = numberOfPayments; }

    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(BigDecimal coverageAmount) { this.coverageAmount = coverageAmount; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }

    public Double getEffectiveAnnualRate() { return effectiveAnnualRate; }
    public void setEffectiveAnnualRate(Double effectiveAnnualRate) { this.effectiveAnnualRate = effectiveAnnualRate; }

    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
}