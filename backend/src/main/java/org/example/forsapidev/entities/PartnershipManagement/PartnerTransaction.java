package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double commissionAmount;

    @Column(nullable = false)
    private Double netAmountToPartner;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Double monthlyPayment;

    @Column(nullable = false)
    private Double totalRepayment;

    @Column(nullable = false)
    private String qrCodeSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime paidToPartnerAt;

    private LocalDateTime completedAt;

    private String paymentReference;

    private String paymentMethod;

    private Double cashbackAmount;

    private Boolean isFraudulent;

    private String fraudReason;

    private String clientDeviceInfo;

    private String clientIpAddress;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = TransactionStatus.PENDING;
        isFraudulent = false;
    }
}