package org.example.forsapidev.entities.ScoringManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long scoreResultId;

    // Montant empruntable
    @Column
    private Double recommendedMinAmount;

    @Column
    private Double recommendedMaxAmount;

    // Recommandation d'amélioration
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationPriority priority;

    @Column
    private Double estimatedScoreImpact;

    @Column
    private String impactExplanation;

    @Column(nullable = false)
    private Boolean isFromAI;

    @Column(nullable = false)
    private Boolean isActive;

    @Column
    private LocalDateTime completedAt;

    @Column
    private Boolean wasEffective;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;
}