package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IQRCodeService;
import org.example.forsapidev.entities.PartnershipManagement.QRCodeSession;
import org.example.forsapidev.Repositories.PartnershipManagement.QRCodeSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QRCodeService implements IQRCodeService {

    private final QRCodeSessionRepository qrCodeSessionRepository;

    private static final String ENCRYPTION_KEY = "ForSa2026SecKey!";
    private static final String ALGORITHM = "AES";

    @Override
    @Transactional
    public QRCodeSession generateQRCode(Long partnerId, Double amount, String deviceInfo, String ipAddress) {
        log.info("Generating QR code for partner: {} amount: {}", partnerId, amount);

        String sessionId = "QR-" + UUID.randomUUID().toString();

        QRCodeSession session = QRCodeSession.builder()
                .sessionId(sessionId)
                .partnerId(partnerId)
                .amount(amount)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        QRCodeSession saved = qrCodeSessionRepository.save(session);

        String encryptedData = encryptQRData(saved);
        saved.setEncryptedData(encryptedData);

        return qrCodeSessionRepository.save(saved);
    }

    @Override
    public QRCodeSession validateQRCode(String sessionId) {
        log.info("Validating QR code: {}", sessionId);

        QRCodeSession session = qrCodeSessionRepository.findValidSession(sessionId, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("QR code invalid or expired"));

        if (session.getIsUsed()) {
            throw new RuntimeException("QR code already used");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("QR code expired");
        }

        return session;
    }

    @Override
    @Transactional
    public void markQRCodeAsUsed(String sessionId, Long transactionId) {
        log.info("Marking QR code as used: {}", sessionId);

        QRCodeSession session = qrCodeSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("QR session not found"));

        session.setIsUsed(true);
        session.setTransactionId(transactionId);

        qrCodeSessionRepository.save(session);
    }

    @Override
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("Cleaning up expired QR sessions");

        Long count = qrCodeSessionRepository.countExpiredSessions(LocalDateTime.now());

        if (count > 0) {
            qrCodeSessionRepository.deleteExpiredSessions(LocalDateTime.now());
            log.info("Deleted {} expired QR sessions", count);
        }
    }

    @Override
    public List<QRCodeSession> getPartnerQRHistory(Long partnerId) {
        return qrCodeSessionRepository.findByPartnerIdOrderByGeneratedAtDesc(partnerId);
    }

    @Override
    public String encryptQRData(QRCodeSession session) {
        try {
            String data = String.format("%s|%d|%.2f|%s",
                    session.getSessionId(),
                    session.getPartnerId(),
                    session.getAmount(),
                    session.getExpiresAt()
            );

            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            log.error("Error encrypting QR data", e);
            return session.getSessionId();
        }
    }

    @Override
    public QRCodeSession decryptQRData(String encryptedData) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            String data = new String(decrypted, StandardCharsets.UTF_8);

            String[] parts = data.split("\\|");

            return qrCodeSessionRepository.findBySessionId(parts[0])
                    .orElseThrow(() -> new RuntimeException("Invalid QR data"));

        } catch (Exception e) {
            log.error("Error decrypting QR data", e);
            throw new RuntimeException("Invalid QR code");
        }
    }
}