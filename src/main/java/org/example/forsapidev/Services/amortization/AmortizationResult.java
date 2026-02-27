package org.example.forsapidev.Services.amortization;

import java.math.BigDecimal;
import java.util.List;

/**
 * Résultat du calcul d'amortissement mensuel
 */
public class AmortizationResult {

    private final List<MonthlyPeriod> periods;
    private final BigDecimal totalInterest;
    private final BigDecimal totalAmount;

    public AmortizationResult(List<MonthlyPeriod> periods, BigDecimal totalInterest, BigDecimal totalAmount) {
        this.periods = periods;
        this.totalInterest = totalInterest;
        this.totalAmount = totalAmount;
    }

    public List<MonthlyPeriod> getPeriods() {
        return periods;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Représente une période mensuelle d'amortissement
     */
    public static class MonthlyPeriod {
        private final int monthNumber;
        private final BigDecimal principalPayment;      // Amortissement
        private final BigDecimal interestPayment;       // Intérêt
        private final BigDecimal totalPayment;          // Mensualité totale
        private final BigDecimal remainingBalance;      // Capital restant

        public MonthlyPeriod(int monthNumber, BigDecimal principalPayment, BigDecimal interestPayment,
                           BigDecimal totalPayment, BigDecimal remainingBalance) {
            this.monthNumber = monthNumber;
            this.principalPayment = principalPayment;
            this.interestPayment = interestPayment;
            this.totalPayment = totalPayment;
            this.remainingBalance = remainingBalance;
        }

        public int getMonthNumber() {
            return monthNumber;
        }

        public BigDecimal getPrincipalPayment() {
            return principalPayment;
        }

        public BigDecimal getInterestPayment() {
            return interestPayment;
        }

        public BigDecimal getTotalPayment() {
            return totalPayment;
        }

        public BigDecimal getRemainingBalance() {
            return remainingBalance;
        }
    }
}

