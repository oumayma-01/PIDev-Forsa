package org.example.forsapidev.entities.WalletManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    // ADD THIS — was missing entirely
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Wallet wallet;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
}