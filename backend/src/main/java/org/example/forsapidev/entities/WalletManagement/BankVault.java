package org.example.forsapidev.entities.WalletManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class BankVault {

    @Id
    private Long id = 1L; // singleton — only one row ever in this table

    private BigDecimal totalFunds;

    public BankVault() {}

    public BankVault(BigDecimal totalFunds) {
        this.id = 1L;
        this.totalFunds = totalFunds;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getTotalFunds() { return totalFunds; }
    public void setTotalFunds(BigDecimal totalFunds) { this.totalFunds = totalFunds; }
}