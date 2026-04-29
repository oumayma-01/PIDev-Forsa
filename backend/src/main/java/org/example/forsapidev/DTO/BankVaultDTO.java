package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class BankVaultDTO {

    private BigDecimal totalFunds;

    public BankVaultDTO() {}

    public BankVaultDTO(BigDecimal totalFunds) {
        this.totalFunds = totalFunds;
    }

    public BigDecimal getTotalFunds() { return totalFunds; }
    public void setTotalFunds(BigDecimal totalFunds) { this.totalFunds = totalFunds; }
}