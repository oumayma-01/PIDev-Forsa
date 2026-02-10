package org.example.forsapidev.entities.WalletManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WalletType type;

    private BigDecimal balance;

    private String currency;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
}