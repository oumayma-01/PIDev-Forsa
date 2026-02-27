package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.Services.Interfaces.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
//@AllArgsConstructor
public class PremiumReminderService {

    private final PremiumPaymentRepository premiumPaymentRepository;
    private final UserRepository userRepository;
    private final IEmailService emailService;

    @Value("${app.reminder.days-before:7}")
    private int daysBeforeReminder;

    // Constructor without @AllArgsConstructor
    public PremiumReminderService(PremiumPaymentRepository premiumPaymentRepository,
                                  UserRepository userRepository,
                                  IEmailService emailService) {
        this.premiumPaymentRepository = premiumPaymentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Scheduled task that runs every day at 9:00 AM
     * Checks for upcoming and overdue payments
     */
    @Scheduled(cron = "0 0 9 * * ?")  // Runs at 9:00 AM daily
    public void checkAndSendReminders() {
        System.out.println("Starting premium payment reminder check...");

        sendUpcomingPaymentReminders();
        markOverduePayments();

        System.out.println("Premium payment reminder check completed!");
    }

    /**
     * Send reminders for payments due within next 7 days
     */
    public void sendUpcomingPaymentReminders() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(daysBeforeReminder);

        // Convert to Date for JPA query
        Date reminderDateAsDate = Date.from(reminderDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayAsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Find all pending payments due within next 7 days
        List<PremiumPayment> upcomingPayments = premiumPaymentRepository
                .findByStatusAndDueDateBetween(PaymentStatus.PENDING, todayAsDate, reminderDateAsDate);

        System.out.println("Found " + upcomingPayments.size() + " upcoming payments to remind");

        for (PremiumPayment payment : upcomingPayments) {
            try {
                // Get user details from policy
                Long userId = payment.getInsurancePolicy().getUser().getId();
                User user = userRepository.findById(userId).orElse(null);

                if (user != null && user.getEmail() != null) {
                    String policyNumber = payment.getInsurancePolicy().getPolicyNumber();
                    String dueDate = payment.getDueDate().toString();
                    Double amount = payment.getAmount().doubleValue();

                    emailService.sendPremiumReminderEmail(
                            user.getEmail(),
                            user.getUsername(),
                            policyNumber,
                            dueDate,
                            amount
                    );
                }
            } catch (Exception e) {
                System.err.println("Error sending reminder for payment ID: " + payment.getId());
                e.printStackTrace();
            }
        }
    }

    /**
     * Mark payments as OVERDUE if due date has passed
     * Send urgent email notification
     */
    public void markOverduePayments() {
        LocalDate today = LocalDate.now();
        Date todayAsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Find all pending payments where due date has passed
        List<PremiumPayment> overduePayments = premiumPaymentRepository
                .findByStatusAndDueDateBefore(PaymentStatus.PENDING, todayAsDate);

        System.out.println("⚠️ Found " + overduePayments.size() + " overdue payments");

        for (PremiumPayment payment : overduePayments) {
            try {
                // Update status to OVERDUE
                payment.setStatus(PaymentStatus.OVERDUE);
                premiumPaymentRepository.save(payment);

                // Send urgent email
                Long userId = payment.getInsurancePolicy().getUser().getId();
                User user = userRepository.findById(userId).orElse(null);

                if (user != null && user.getEmail() != null) {
                    String policyNumber = payment.getInsurancePolicy().getPolicyNumber();
                    String dueDate = payment.getDueDate().toString();
                    Double amount = payment.getAmount().doubleValue();

                    emailService.sendOverduePaymentEmail(
                            user.getEmail(),
                            user.getUsername(),
                            policyNumber,
                            dueDate,
                            amount
                    );
                }
            } catch (Exception e) {
                System.err.println("Error processing overdue payment ID: " + payment.getId());
                e.printStackTrace();
            }
        }
    }
}
