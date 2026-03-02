package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cashback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double percentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashbackStatus status;

    @Column(nullable = false)
    private LocalDateTime earnedAt;

    private LocalDateTime usedAt;

    private Long usedInTransactionId;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
        expiresAt = earnedAt.plusMonths(6);
        status = CashbackStatus.AVAILABLE;
    }
}