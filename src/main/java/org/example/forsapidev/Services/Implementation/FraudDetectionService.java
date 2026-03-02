package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IFraudDetectionService;
import org.example.forsapidev.entities.PartnershipManagement.*;

import org.example.forsapidev.Repositories.PartnershipManagement.FraudAlertRepository;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService implements IFraudDetectionService {

    private final FraudAlertRepository fraudAlertRepository;
    private final PartnerTransactionRepository transactionRepository;

    @Override
    @Transactional
    public Boolean checkTransaction(PartnerTransaction transaction) {
        log.info("Checking transaction for fraud: {}", transaction.getId());

        Double riskScore = calculateRiskScore(transaction);

        if (riskScore >= 70) {
            String reason = determineReason(transaction);
            createFraudAlert(transaction, reason, riskScore);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public FraudAlert createFraudAlert(PartnerTransaction transaction, String reason, Double riskScore) {
        log.warn("Creating fraud alert for transaction: {} - Reason: {}", transaction.getId(), reason);

        FraudSeverity severity;
        FraudType fraudType;

        if (riskScore >= 90) {
            severity = FraudSeverity.CRITICAL;
        } else if (riskScore >= 75) {
            severity = FraudSeverity.HIGH;
        } else if (riskScore >= 50) {
            severity = FraudSeverity.MEDIUM;
        } else {
            severity = FraudSeverity.LOW;
        }

        if (reason.contains("velocity")) {
            fraudType = FraudType.VELOCITY_ABUSE;
        } else if (reason.contains("amount")) {
            fraudType = FraudType.UNUSUAL_AMOUNT;
        } else if (reason.contains("location")) {
            fraudType = FraudType.GEOLOCATION_MISMATCH;
        } else {
            fraudType = FraudType.SUSPICIOUS_TIMING;
        }

        FraudAlert alert = FraudAlert.builder()
                .transactionId(transaction.getId())
                .clientId(transaction.getClientId())
                .partnerId(transaction.getPartnerId())
                .fraudType(fraudType)
                .severity(severity)
                .description(reason)
                .riskScore(riskScore)
                .build();

        return fraudAlertRepository.save(alert);
    }

    @Override
    public List<FraudAlert> getUnresolvedAlerts() {
        return fraudAlertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
    }

    @Override
    public List<FraudAlert> getClientAlerts(Long clientId) {
        return fraudAlertRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Override
    @Transactional
    public void resolveAlert(Long alertId, String resolvedBy, String notes) {
        log.info("Resolving fraud alert: {}", alertId);

        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setResolutionNotes(notes);

        fraudAlertRepository.save(alert);
    }

    @Override
    public Double calculateRiskScore(PartnerTransaction transaction) {
        double score = 0.0;

        if (isVelocityAbuse(transaction.getClientId())) {
            score += 40;
        }

        if (isUnusualAmount(transaction.getClientId(), transaction.getAmount())) {
            score += 30;
        }

        List<PartnerTransaction> clientHistory = transactionRepository
                .findByClientIdOrderByCreatedAtDesc(transaction.getClientId());

        if (clientHistory.size() < 3 && transaction.getAmount() > 500) {
            score += 20;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() >= 23 || now.getHour() <= 5) {
            score += 10;
        }

        return Math.min(score, 100.0);
    }

    @Override
    public Boolean isVelocityAbuse(Long clientId) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        Long recentCount = transactionRepository.countRecentTransactions(clientId, tenMinutesAgo);

        return recentCount >= 5;
    }

    @Override
    public Boolean isUnusualAmount(Long clientId, Double amount) {
        List<PartnerTransaction> history = transactionRepository
                .findRecentByClient(clientId, LocalDateTime.now().minusMonths(3));

        if (history.isEmpty()) {
            return amount > 1000;
        }

        Double avgAmount = history.stream()
                .mapToDouble(PartnerTransaction::getAmount)
                .average()
                .orElse(0.0);

        return amount > (avgAmount * 3);
    }

    @Override
    public Boolean isGeolocationSuspicious(String ipAddress, Long clientId) {
        return false;
    }

    private String determineReason(PartnerTransaction transaction) {
        StringBuilder reason = new StringBuilder();

        if (isVelocityAbuse(transaction.getClientId())) {
            reason.append("Velocity abuse detected (5+ transactions in 10 min). ");
        }

        if (isUnusualAmount(transaction.getClientId(), transaction.getAmount())) {
            reason.append("Unusual transaction amount. ");
        }

        List<PartnerTransaction> clientHistory = transactionRepository
                .findByClientIdOrderByCreatedAtDesc(transaction.getClientId());

        if (clientHistory.size() < 3 && transaction.getAmount() > 500) {
            reason.append("New customer with high amount. ");
        }

        if (reason.length() == 0) {
            reason.append("General suspicious pattern detected.");
        }

        return reason.toString().trim();
    }
}