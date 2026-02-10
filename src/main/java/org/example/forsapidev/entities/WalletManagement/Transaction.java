package org.example.forsapidev.entities.WalletManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String referenceCode;

    private String description;
}