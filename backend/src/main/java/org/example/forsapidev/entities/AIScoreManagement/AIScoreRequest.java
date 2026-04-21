package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_score_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIScoreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    private Integer scoreCalculated;

    @Column(precision = 10, scale = 2)
    private BigDecimal thresholdCalculated;

    @Column(nullable = false)
    private String status;  // PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}