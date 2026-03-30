package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class AdaptiveInterestResultDTO {

    private Long       accountId;
    private BigDecimal previousBalance;
    private BigDecimal interestApplied;
    private BigDecimal newBalance;
    private double     rateUsed;
    private String     justification;

    public AdaptiveInterestResultDTO() {}

    public AdaptiveInterestResultDTO(Long accountId,
                                     BigDecimal previousBalance,
                                     BigDecimal interestApplied,
                                     BigDecimal newBalance,
                                     double rateUsed,
                                     String justification) {
        this.accountId       = accountId;
        this.previousBalance = previousBalance;
        this.interestApplied = interestApplied;
        this.newBalance      = newBalance;
        this.rateUsed        = rateUsed;
        this.justification   = justification;
    }

    public Long getAccountId()                      { return accountId; }
    public void setAccountId(Long v)                { this.accountId = v; }
    public BigDecimal getPreviousBalance()          { return previousBalance; }
    public void setPreviousBalance(BigDecimal v)    { this.previousBalance = v; }
    public BigDecimal getInterestApplied()          { return interestApplied; }
    public void setInterestApplied(BigDecimal v)    { this.interestApplied = v; }
    public BigDecimal getNewBalance()               { return newBalance; }
    public void setNewBalance(BigDecimal v)         { this.newBalance = v; }
    public double getRateUsed()                     { return rateUsed; }
    public void setRateUsed(double v)               { this.rateUsed = v; }
    public String getJustification()                { return justification; }
    public void setJustification(String v)          { this.justification = v; }
}