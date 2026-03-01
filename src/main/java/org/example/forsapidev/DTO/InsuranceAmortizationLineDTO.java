package org.example.forsapidev.DTO;

import java.math.BigDecimal;
import java.util.Date;

public class InsuranceAmortizationLineDTO {
    private Integer period;
    private Date dueDate;
    private BigDecimal payment;
    private BigDecimal interestPortion;
    private BigDecimal principalPortion;
    private BigDecimal remainingBalance;

    // Constructor
    public InsuranceAmortizationLineDTO() {}

    public InsuranceAmortizationLineDTO(Integer period, Date dueDate, BigDecimal payment,
                                        BigDecimal interestPortion, BigDecimal principalPortion,
                                        BigDecimal remainingBalance) {
        this.period = period;
        this.dueDate = dueDate;
        this.payment = payment;
        this.interestPortion = interestPortion;
        this.principalPortion = principalPortion;
        this.remainingBalance = remainingBalance;
    }

    // Getters and Setters
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public BigDecimal getPayment() { return payment; }
    public void setPayment(BigDecimal payment) { this.payment = payment; }

    public BigDecimal getInterestPortion() { return interestPortion; }
    public void setInterestPortion(BigDecimal interestPortion) { this.interestPortion = interestPortion; }

    public BigDecimal getPrincipalPortion() { return principalPortion; }
    public void setPrincipalPortion(BigDecimal principalPortion) { this.principalPortion = principalPortion; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
}