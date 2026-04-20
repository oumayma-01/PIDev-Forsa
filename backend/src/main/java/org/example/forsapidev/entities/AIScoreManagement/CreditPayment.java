package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creditApplicationId;

    @Column(nullable = false)
    private Integer installmentNumber;  // 1, 2, 3, ...

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer daysLate = 0;

    @Column(nullable = false)
    private Integer scoreImpact = 0;  // +5, -20, -50

    @Column(nullable = false)
    private LocalDateTime createdAt;
}