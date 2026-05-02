package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long clientId;

    @Column(nullable = false)
    private Integer currentScore;  // Score 0-1000

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AIScoreLevel scoreLevel;

    @Column(precision = 10, scale = 2)
    private BigDecimal creditThreshold;  // Seuil max (ex: 3750 TND)

    @Column(precision = 10, scale = 2)
    private BigDecimal availableThreshold;  // Seuil disponible (0 si crédit actif)

    @Column(nullable = false)
    private Boolean hasActiveCredit = false;

    private Long activeCreditId;

    @Column(precision = 10, scale = 2)
    private BigDecimal verifiedSalary;  // Salaire vérifié par OCR

    @Column(nullable = false)
    private Integer totalCreditsTaken = 0;

    @Column(nullable = false)
    private Integer totalCreditsCompleted = 0;

    @Column(nullable = false)
    private Integer totalPaymentsOnTime = 0;

    @Column(nullable = false)
    private Integer totalPaymentsLate = 0;

    @Column(nullable = false)
    private LocalDateTime lastCalculatedAt;

    @Column(nullable = false)
    private LocalDateTime scoreExpiresAt;  // Validité 6 mois

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    private LocalDateTime stegBoosterExpiry;

    private LocalDateTime sonedeBoosterExpiry;
}