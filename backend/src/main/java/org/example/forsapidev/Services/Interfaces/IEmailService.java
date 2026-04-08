package org.example.forsapidev.Services.Interfaces;

public interface IEmailService {
    void sendPremiumReminderEmail(String toEmail, String userName, String policyNumber,
                                  String dueDate, Double amount);
    void sendOverduePaymentEmail(String toEmail, String userName, String policyNumber,
                                 String dueDate, Double amount);
}
