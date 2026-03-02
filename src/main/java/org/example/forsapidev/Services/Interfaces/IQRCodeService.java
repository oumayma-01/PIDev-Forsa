package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.QRCodeSession;

import java.util.List;

public interface IQRCodeService {
    QRCodeSession generateQRCode(Long partnerId, Double amount, String deviceInfo, String ipAddress);
    QRCodeSession validateQRCode(String sessionId);
    void markQRCodeAsUsed(String sessionId, Long transactionId);
    void cleanupExpiredSessions();
    List<QRCodeSession> getPartnerQRHistory(Long partnerId);
    String encryptQRData(QRCodeSession session);
    QRCodeSession decryptQRData(String encryptedData);
}