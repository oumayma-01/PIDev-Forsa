
package org.example.forsapidev.entities.WalletManagement;

import java.math.BigDecimal;

public class WalletStatistics {


    private BigDecimal balance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private int numberOfTransactions;

    public WalletStatistics(BigDecimal balance,
                            BigDecimal totalDeposits,
                            BigDecimal totalWithdrawals,
                            int numberOfTransactions) {
        this.balance = balance;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.numberOfTransactions = numberOfTransactions;
    }

    // getters


    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }
}