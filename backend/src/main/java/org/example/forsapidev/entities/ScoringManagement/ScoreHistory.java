package org.example.forsapidev.entities.ScoringManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long previousScoreId;

    @Column(nullable = false)
    private Long newScoreId;

    @Column(nullable = false)
    private Double previousScore;

    @Column(nullable = false)
    private Double newScore;

    @Column(nullable = false)
    private Double scoreDelta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskCategory previousCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskCategory newCategory;

    @Column(nullable = false)
    private Boolean categoryChanged;

    // Deltas des facteurs
    private Double factor1Delta;
    private Double factor2Delta;
    private Double factor3Delta;
    private Double factor4Delta;
    private Double factor5Delta;

    @Column(name = "`trigger`", nullable = false)
    private String trigger;

    @Column(nullable = false)
    private LocalDateTime reRatingDate;

    private Double previousRate;
    private Double newRate;
    private Boolean rateAdjusted;
}