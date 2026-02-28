package org.example.forsapidev.entities.ScoringManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column
    private Long scoreResultId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false)
    private Boolean resolved;

    @Column
    private LocalDateTime resolvedAt;

    @Column
    private String resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false)
    private Boolean notificationSent;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}