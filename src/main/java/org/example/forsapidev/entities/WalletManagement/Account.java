package org.example.forsapidev.entities.WalletManagement;

import org.example.forsapidev.entities.WalletManagement.AccountStatus;
import org.example.forsapidev.entities.WalletManagement.AccountType;
import org.example.forsapidev.entities.WalletManagement.Wallet;
import jakarta.persistence.*;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountType type; // INVESTMENT / EPARGNE

    @Enumerated(EnumType.STRING)
    private AccountStatus status; // ACTIF / BLOQUE

    @OneToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}




}
