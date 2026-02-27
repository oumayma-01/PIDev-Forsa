package org.example.forsapidev.payload.response;

import org.example.forsapidev.entities.CreditManagement.AmortizationType;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pour le retour du tableau d'amortissement
 */
public class AmortizationScheduleResponse {

    private Long creditId;
    private AmortizationType calculationType;
    private BigDecimal principal;
    private BigDecimal annualRatePercent;
    private Integer durationMonths;
    private BigDecimal totalInterest;
    private BigDecimal totalAmount;
    private List<PeriodDetail> periods;

    public AmortizationScheduleResponse() {
    }

    public static class PeriodDetail {
        private int monthNumber;
        private BigDecimal principalPayment;
        private BigDecimal interestPayment;
        private BigDecimal totalPayment;
        private BigDecimal remainingBalance;

        public PeriodDetail() {
        }

        public PeriodDetail(int monthNumber, BigDecimal principalPayment, BigDecimal interestPayment,
                          BigDecimal totalPayment, BigDecimal remainingBalance) {
            this.monthNumber = monthNumber;
            this.principalPayment = principalPayment;
            this.interestPayment = interestPayment;
            this.totalPayment = totalPayment;
            this.remainingBalance = remainingBalance;
        }

        // Getters & Setters
        public int getMonthNumber() {
            return monthNumber;
        }

        public void setMonthNumber(int monthNumber) {
            this.monthNumber = monthNumber;
        }

        public BigDecimal getPrincipalPayment() {
            return principalPayment;
        }

        public void setPrincipalPayment(BigDecimal principalPayment) {
            this.principalPayment = principalPayment;
        }

        public BigDecimal getInterestPayment() {
            return interestPayment;
        }

        public void setInterestPayment(BigDecimal interestPayment) {
            this.interestPayment = interestPayment;
        }

        public BigDecimal getTotalPayment() {
            return totalPayment;
        }

        public void setTotalPayment(BigDecimal totalPayment) {
            this.totalPayment = totalPayment;
        }

        public BigDecimal getRemainingBalance() {
            return remainingBalance;
        }

        public void setRemainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
        }
    }

    // Getters & Setters
    public Long getCreditId() {
        return creditId;
    }

    public void setCreditId(Long creditId) {
        this.creditId = creditId;
    }

    public AmortizationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(AmortizationType calculationType) {
        this.calculationType = calculationType;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAnnualRatePercent() {
        return annualRatePercent;
    }

    public void setAnnualRatePercent(BigDecimal annualRatePercent) {
        this.annualRatePercent = annualRatePercent;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<PeriodDetail> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PeriodDetail> periods) {
        this.periods = periods;
    }
}

