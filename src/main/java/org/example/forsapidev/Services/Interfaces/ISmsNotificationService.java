package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;

public interface ISmsNotificationService {
    void sendTransactionConfirmation(PartnerTransaction transaction);
    void sendPaymentReminder(Long clientId, Double amount, String dueDate);
    void sendPartnerPaymentNotification(Long partnerId, Double amount, String reference);
    void sendQRCodeGenerated(Long partnerId, Double amount);
    void sendFraudAlert(Long clientId, String reason);
    Boolean sendSms(String phoneNumber, String message);
}