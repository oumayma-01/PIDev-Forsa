package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_verified_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientVerifiedInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long clientId;

    @Column(length = 8)
    private String cinNumber;

    @Column(length = 100)
    private String fullName;

    @Column(precision = 10, scale = 2)
    private BigDecimal verifiedSalary;

    @Column(length = 100)
    private String employer;

    @Column(nullable = false)
    private LocalDateTime lastVerifiedAt;
}