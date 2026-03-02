package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "partner_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer transactionsCount;

    @Column(nullable = false)
    private Double totalVolume;

    @Column(nullable = false)
    private Double commissionEarned;

    @Column(nullable = false)
    private Double averageTransactionAmount;

    private Integer uniqueCustomers;

    private Double conversionRate;

    private Integer peakHour;
}