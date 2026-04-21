package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long aiScoreId;

    private Integer scoreAtRequest;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amountRequested;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountApproved;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(precision = 10, scale = 2)
    private BigDecimal monthlyPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}