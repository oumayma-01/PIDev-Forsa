package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import org.example.forsapidev.entities.UserManagement.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Partner extends User {

    @Column(nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType partnerType;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String businessPhone;

    private String businessEmail;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String iban;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountHolderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerStatus status;

    @Column(unique = true)
    private String qrCodeId;

    @Column(nullable = false)
    private Double maxTransactionAmount;

    @Column(nullable = false)
    private Double dailyTransactionLimit;

    @Column(nullable = false)
    private Double monthlyTransactionLimit;

    @Column(nullable = false)
    private Double commissionRate;

    private Double totalAmountProcessed;

    private Integer totalTransactionsCount;

    private LocalDateTime activatedAt;

    private LocalDateTime suspendedAt;

    private String suspensionReason;

    private String contactPersonName;

    private String contactPersonPhone;

    private String contactPersonEmail;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private PartnerBadge badge;

    private Double averageRating;

    private Integer totalReviews;



}