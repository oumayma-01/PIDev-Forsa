package org.example.forsapidev.DTO;

import org.example.forsapidev.entities.CreditManagement.AmortizationType;

import java.math.BigDecimal;

/**
 * DTO pour la création d'une demande de crédit
 * L'utilisateur n'a pas besoin d'envoyer ses données - elles seront extraites du JWT
 */
public class CreditRequestCreateDTO {

    private BigDecimal amountRequested;
    private Double interestRate;
    private Integer durationMonths;
    private AmortizationType typeCalcul;

    // Constructors
    public CreditRequestCreateDTO() {}

    public CreditRequestCreateDTO(BigDecimal amountRequested, Double interestRate, Integer durationMonths, AmortizationType typeCalcul) {
        this.amountRequested = amountRequested;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.typeCalcul = typeCalcul;
    }

    // Getters and Setters
    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public AmortizationType getTypeCalcul() {
        return typeCalcul;
    }

    public void setTypeCalcul(AmortizationType typeCalcul) {
        this.typeCalcul = typeCalcul;
    }
}
