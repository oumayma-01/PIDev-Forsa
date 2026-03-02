package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IQRCodeService;
import org.example.forsapidev.entities.PartnershipManagement.QRCodeSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr-code")
@RequiredArgsConstructor
public class QRCodeController {

    private final IQRCodeService qrCodeService;

    @PostMapping("/generate")
    public ResponseEntity<QRCodeSession> generateQRCode(
            @RequestParam Long partnerId,
            @RequestParam Double amount,
            @RequestParam(required = false) String deviceInfo,
            @RequestParam(required = false) String ipAddress) {
        QRCodeSession session = qrCodeService.generateQRCode(partnerId, amount, deviceInfo, ipAddress);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/validate/{sessionId}")
    public ResponseEntity<QRCodeSession> validateQRCode(@PathVariable String sessionId) {
        QRCodeSession session = qrCodeService.validateQRCode(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/partner/{partnerId}/history")
    public ResponseEntity<List<QRCodeSession>> getPartnerQRHistory(@PathVariable Long partnerId) {
        List<QRCodeSession> history = qrCodeService.getPartnerQRHistory(partnerId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredSessions() {
        qrCodeService.cleanupExpiredSessions();
        return ResponseEntity.ok().build();
    }
}