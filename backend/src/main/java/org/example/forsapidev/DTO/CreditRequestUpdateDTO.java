package org.example.forsapidev.DTO;

import org.example.forsapidev.entities.CreditManagement.CreditStatus;

/**
 * DTO d'entrée pour la mise à jour d'une demande de crédit.
 * Ne contient volontairement pas les relations (user, échéances, ...).
 */
public class CreditRequestUpdateDTO {

    private Double interestRate;
    private Integer durationMonths;
    private CreditStatus status;
    private Long agentId;

    public CreditRequestUpdateDTO() {
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

    public CreditStatus getStatus() {
        return status;
    }

    public void setStatus(CreditStatus status) {
        this.status = status;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }
}
