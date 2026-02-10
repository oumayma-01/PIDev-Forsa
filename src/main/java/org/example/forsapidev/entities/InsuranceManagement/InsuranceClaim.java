package org.example.forsapidev.entities.InsuranceManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "insurance_claim")
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date claimDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;

    private BigDecimal indemnificationPaid;
}
