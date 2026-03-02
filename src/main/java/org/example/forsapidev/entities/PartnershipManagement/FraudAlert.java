package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private Long clientId;

    private Long partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudType fraudType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double riskScore;

    @Column(nullable = false)
    private Boolean isResolved;

    private LocalDateTime resolvedAt;

    private String resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isResolved = false;
    }
}