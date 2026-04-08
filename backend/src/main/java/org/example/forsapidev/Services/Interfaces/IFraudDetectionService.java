package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.FraudAlert;
import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;

import java.util.List;

public interface IFraudDetectionService {
    Boolean checkTransaction(PartnerTransaction transaction);
    FraudAlert createFraudAlert(PartnerTransaction transaction, String reason, Double riskScore);
    List<FraudAlert> getUnresolvedAlerts();
    List<FraudAlert> getClientAlerts(Long clientId);
    void resolveAlert(Long alertId, String resolvedBy, String notes);
    Double calculateRiskScore(PartnerTransaction transaction);
    Boolean isVelocityAbuse(Long clientId);
    Boolean isUnusualAmount(Long clientId, Double amount);
    Boolean isGeolocationSuspicious(String ipAddress, Long clientId);
}