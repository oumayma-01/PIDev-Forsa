package org.example.forsapidev.entities.ScoringManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column
    private Long loanId;

    @Column(nullable = false)
    private Long scoreResultId;

    // Métriques Bâle
    @Column(nullable = false)
    private Double probabilityOfDefault; // PD

    @Column(nullable = false)
    private Double lossGivenDefault; // LGD

    @Column(nullable = false)
    private Double exposureAtDefault; // EAD

    @Column(nullable = false)
    private Double expectedLoss; // EL = PD × LGD × EAD

    // Détails crédit
    @Column(nullable = false)
    private Double loanAmount;

    @Column(nullable = false)
    private Integer loanDurationMonths;

    @Column(nullable = false)
    private Double personalizedInterestRate;

    // Détails LGD
    @Column(nullable = false)
    private Double collateralValue;

    @Column(nullable = false)
    private Double seizeableIncome;

    @Column(nullable = false)
    private Double guarantorCapacity;

    @Column(nullable = false)
    private Double recoveryCosts;

    @Column(nullable = false)
    private LocalDateTime calculationDate;

    @Column(nullable = false)
    private String calculationVersion;
}