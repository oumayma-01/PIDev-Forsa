package org.example.forsapidev.entities.ScoringManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "score_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    // Score final
    @Column(nullable = false)
    private Double finalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskCategory riskCategory;

    // Les 5 facteurs (notes brutes)
    @Column(nullable = false)
    private Double factor1Score; // Stabilité revenus

    @Column(nullable = false)
    private Double factor2Score; // Historique paiements

    @Column(nullable = false)
    private Double factor3Score; // Ratio endettement

    @Column(nullable = false)
    private Double factor4Score; // Type emploi

    @Column(nullable = false)
    private Double factor5Score; // Région

    // Contributions pondérées
    @Column(nullable = false)
    private Double factor1Contribution;

    @Column(nullable = false)
    private Double factor2Contribution;

    @Column(nullable = false)
    private Double factor3Contribution;

    @Column(nullable = false)
    private Double factor4Contribution;

    @Column(nullable = false)
    private Double factor5Contribution;

    // Métadonnées
    @Column(nullable = false)
    private LocalDateTime calculationDate;

    @Column(nullable = false)
    private String calculationVersion;

    @Column(nullable = false)
    private String calculatedBy;

    // Explication IA
    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    @Column
    private LocalDateTime aiExplanationGeneratedAt;
}