package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "credit_request")
public class CreditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amountRequested;

    private Double interestRate;

    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    private CreditStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    private Long agentId;
}
