package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class PremiumReminderService {

    private final PremiumPaymentRepository premiumPaymentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.reminder.days-before:7}")
    private int daysBeforeReminder;

    public PremiumReminderService(PremiumPaymentRepository premiumPaymentRepository,
                                  UserRepository userRepository,
                                  EmailService emailService) {
        this.premiumPaymentRepository = premiumPaymentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9:00 AM
    public void checkAndSendReminders() {
        System.out.println("Starting premium payment reminder check...");
        sendUpcomingPaymentReminders();
        markOverduePayments();
        System.out.println("Premium payment reminder check completed!");
    }

    public void sendUpcomingPaymentReminders() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(daysBeforeReminder);

        Date reminderDateAsDate = Date.from(reminderDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayAsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<PremiumPayment> upcomingPayments = premiumPaymentRepository
                .findByStatusAndDueDateBetween(PaymentStatus.PENDING, todayAsDate, reminderDateAsDate);

        System.out.println("Found " + upcomingPayments.size() + " upcoming payments to remind");

        for (PremiumPayment payment : upcomingPayments) {
            try {
                User user = getUserFromPayment(payment);
                if (user != null && user.getEmail() != null) {
                    String policyNumber = payment.getInsurancePolicy().getPolicyNumber();
                    String dueDate = payment.getDueDate().toString();
                    Double amount = payment.getAmount().doubleValue();

                    String subject = "Reminder: Premium Payment Due Soon - Policy " + policyNumber;

                    String htmlBody = buildReminderHtml(user.getUsername(), policyNumber, dueDate, amount);

                    emailService.sendEmailWithImage(user.getEmail(), subject, htmlBody);
                }
            } catch (Exception e) {
                System.err.println("Error sending reminder for payment ID: " + payment.getId());
                e.printStackTrace();
            }
        }
    }

    public void markOverduePayments() {
        LocalDate today = LocalDate.now();
        Date todayAsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<PremiumPayment> overduePayments = premiumPaymentRepository
                .findByStatusAndDueDateBefore(PaymentStatus.PENDING, todayAsDate);

        System.out.println("⚠️ Found " + overduePayments.size() + " overdue payments");

        for (PremiumPayment payment : overduePayments) {
            try {
                // Mark as overdue
                payment.setStatus(PaymentStatus.OVERDUE);
                premiumPaymentRepository.save(payment);

                User user = getUserFromPayment(payment);
                if (user != null && user.getEmail() != null) {
                    String policyNumber = payment.getInsurancePolicy().getPolicyNumber();
                    String dueDate = payment.getDueDate().toString();
                    Double amount = payment.getAmount().doubleValue();

                    String subject = "⚠️ Urgent: Overdue Premium Payment - Policy " + policyNumber;

                    String htmlBody = buildOverdueHtml(user.getUsername(), policyNumber, dueDate, amount);

                    emailService.sendEmailWithImage(user.getEmail(), subject, htmlBody);
                }
            } catch (Exception e) {
                System.err.println("Error processing overdue payment ID: " + payment.getId());
                e.printStackTrace();
            }
        }
    }

    // Helper method to safely get user
    private User getUserFromPayment(PremiumPayment payment) {
        if (payment.getInsurancePolicy() == null || payment.getInsurancePolicy().getUser() == null) {
            return null;
        }
        Long userId = payment.getInsurancePolicy().getUser().getId();
        return userRepository.findById(userId).orElse(null);
    }

    // HTML template for upcoming reminder
    private String buildReminderHtml(String username, String policyNumber, String dueDate, Double amount) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <img src="cid:logoImage" alt="Forsa Logo" style="width: 180px; margin-bottom: 20px;"/>
                <h2>Dear %s,</h2>
                <p>This is a friendly reminder that your insurance premium payment is due soon.</p>
                <p><strong>Policy Number:</strong> %s</p>
                <p><strong>Due Date:</strong> %s</p>
                <p><strong>Amount Due:</strong> %.2f TND</p>
                <br>
                <p>Please make the payment before the due date to avoid late fees.</p>
                <p>Best regards,<br><strong>Forsa Insurance Team</strong></p>
            </body>
            </html>
            """.formatted(username, policyNumber, dueDate, amount);
    }

    // HTML template for overdue
    private String buildOverdueHtml(String username, String policyNumber, String dueDate, Double amount) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <img src="cid:logoImage" alt="Forsa Logo" style="width: 180px; margin-bottom: 20px;"/>
                <h2>Dear %s,</h2>
                <p style="color: red;"><strong>⚠️ Urgent Notice:</strong> Your premium payment is now overdue.</p>
                <p><strong>Policy Number:</strong> %s</p>
                <p><strong>Due Date:</strong> %s</p>
                <p><strong>Amount Due:</strong> %.2f TND</p>
                <br>
                <p>Please settle this payment immediately to prevent your policy from being suspended.</p>
                <p>Best regards,<br><strong>Forsa Insurance Team</strong></p>
            </body>
            </html>
            """.formatted(username, policyNumber, dueDate, amount);
    }
}