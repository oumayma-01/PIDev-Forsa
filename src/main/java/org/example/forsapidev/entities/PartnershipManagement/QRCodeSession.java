package org.example.forsapidev.entities.PartnershipManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_code_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isUsed;

    private Long transactionId;

    private String deviceInfo;

    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String encryptedData;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        expiresAt = generatedAt.plusMinutes(5);
        isUsed = false;
    }
}