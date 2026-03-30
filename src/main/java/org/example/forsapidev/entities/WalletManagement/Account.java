package org.example.forsapidev.entities.WalletManagement;

import jakarta.persistence.*;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @OneToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
}