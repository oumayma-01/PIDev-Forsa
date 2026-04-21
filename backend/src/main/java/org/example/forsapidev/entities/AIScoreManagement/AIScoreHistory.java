package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    private Integer oldScore;
    private Integer newScore;
    private Integer scoreChange;  // +5, -20, +100

    @Column(nullable = false)
    private String changeReason;
    // INITIAL, PAYMENT_ON_TIME, PAYMENT_LATE,
    // CREDIT_COMPLETED, CREDIT_DEFAULT

    private Long creditApplicationId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}