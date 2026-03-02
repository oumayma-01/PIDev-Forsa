package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Boolean isVerified;

    private Boolean isReported;

    private String reportReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isVerified = false;
        isReported = false;
    }
}