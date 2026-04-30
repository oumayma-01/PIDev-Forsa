package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.PolicyStatus;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class PremiumReminderService {

    private final PremiumPaymentRepository premiumPaymentRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.reminder.days-before:7}")
    private int daysBeforeReminder;

    public PremiumReminderService(PremiumPaymentRepository premiumPaymentRepository,
                                  InsurancePolicyRepository insurancePolicyRepository,
                                  UserRepository userRepository,
                                  EmailService emailService) {
        this.premiumPaymentRepository = premiumPaymentRepository;
        this.insurancePolicyRepository = insurancePolicyRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9:00 AM
    public void checkAndProcessPayments() {
        System.out.println("🚀 Starting consolidated premium payment processing...");
        
        // 1. First, mark new overdue payments and check for suspension
        markNewOverduePayments();
        
        // 2. Then, send consolidated reminders to users
        sendConsolidatedReminders();
        
        System.out.println("✅ Premium payment processing completed!");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("🚀 Application Ready: Triggering initial premium reminder check...");
        checkAndProcessPayments();
    }

    public void markNewOverduePayments() {
        LocalDate today = LocalDate.now();
        Date todayAsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Find all PENDING payments that are now past their due date
        List<PremiumPayment> newlyOverdue = premiumPaymentRepository
                .findByStatusAndDueDateBefore(PaymentStatus.PENDING, todayAsDate);

        if (!newlyOverdue.isEmpty()) {
            System.out.println("⚠️ Found " + newlyOverdue.size() + " newly overdue payments. Updating statuses...");
            for (PremiumPayment payment : newlyOverdue) {
                payment.setStatus(PaymentStatus.OVERDUE);
                premiumPaymentRepository.save(payment);
                
                // Check if this triggers a policy suspension
                checkForPolicySuspension(payment.getInsurancePolicy());
            }
        }
    }

    public void sendConsolidatedReminders() {
        LocalDate today = LocalDate.now();
        LocalDate reminderThreshold = today.plusDays(daysBeforeReminder);
        Date thresholdDate = Date.from(reminderThreshold.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // We want to notify users about:
        // 1. ALL Overdue payments (to keep reminding them until paid)
        // 2. PENDING payments due within the threshold (e.g. 7 days)
        
        List<User> allUsers = userRepository.findAll(); // In a huge app, we'd query only users with unpaid payments
        
        for (User user : allUsers) {
            if (user.getEmail() == null) continue;

            List<PremiumPayment> unpaidPayments = premiumPaymentRepository.findAll().stream()
                    .filter(p -> p.getInsurancePolicy() != null && 
                                p.getInsurancePolicy().getUser() != null && 
                                p.getInsurancePolicy().getUser().getId().equals(user.getId()))
                    .filter(p -> {
                        if (p.getStatus() == PaymentStatus.OVERDUE) return true;
                        if (p.getStatus() == PaymentStatus.PENDING) {
                            return p.getDueDate() != null && p.getDueDate().before(thresholdDate);
                        }
                        return false;
                    })
                    .toList();

            if (!unpaidPayments.isEmpty()) {
                sendUserConsolidatedEmail(user, unpaidPayments);
            }
        }
    }

    private void sendUserConsolidatedEmail(User user, List<PremiumPayment> payments) {
        try {
            String subject = "🔔 Forsa Insurance: Your Premium Payment Statement";
            String htmlBody = buildConsolidatedHtml(user.getUsername(), payments);
            emailService.sendEmailWithImage(user.getEmail(), subject, htmlBody);
            System.out.println("Sent consolidated reminder to: " + user.getEmail() + " (" + payments.size() + " payments)");
        } catch (Exception e) {
            System.err.println("Error sending consolidated email to user: " + user.getId());
            e.printStackTrace();
        }
    }

    private void checkForPolicySuspension(InsurancePolicy policy) {
        if (policy.getStatus() == PolicyStatus.SUSPENDED) {
            return; // Already suspended
        }

        // Get all payments for this policy, ordered by due date
        List<PremiumPayment> allPayments = premiumPaymentRepository.findByInsurancePolicy(policy);
        
        // Sort by due date ascending to check consecutive ones
        allPayments.sort((p1, p2) -> p1.getDueDate().compareTo(p2.getDueDate()));

        int consecutiveOverdue = 0;
        for (PremiumPayment p : allPayments) {
            if (p.getStatus() == PaymentStatus.OVERDUE) {
                consecutiveOverdue++;
                if (consecutiveOverdue >= 3) {
                    suspendPolicy(policy);
                    break;
                }
            } else if (p.getStatus() == PaymentStatus.PAID) {
                // Reset counter if a payment was made
                consecutiveOverdue = 0;
            }
            // PENDING or CANCELLED don't count towards consecutive overdue, 
            // but PENDING shouldn't be before OVERDUE in a normal schedule anyway.
        }
    }

    private void suspendPolicy(InsurancePolicy policy) {
        System.out.println("🚨 Suspending policy " + policy.getPolicyNumber() + " due to 3 consecutive missed payments");
        
        policy.setStatus(PolicyStatus.SUSPENDED);
        insurancePolicyRepository.save(policy);

        // Notify Client
        notifyClientOfSuspension(policy);
        
        // Notify Admin
        notifyAdminsOfSuspension(policy);
    }

    private void notifyClientOfSuspension(InsurancePolicy policy) {
        try {
            User user = policy.getUser();
            if (user != null && user.getEmail() != null) {
                String subject = "🚨 IMPORTANT: Your Insurance Policy " + policy.getPolicyNumber() + " has been SUSPENDED";
                String htmlBody = buildSuspensionClientHtml(user.getUsername(), policy.getPolicyNumber());
                emailService.sendEmailWithImage(user.getEmail(), subject, htmlBody);
            }
        } catch (Exception e) {
            System.err.println("Error notifying client of suspension: " + policy.getPolicyNumber());
            e.printStackTrace();
        }
    }

    private void notifyAdminsOfSuspension(InsurancePolicy policy) {
        try {
            // Find all admins or use a specific admin email
            // For now, let's find users with ADMIN role
            List<User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().getName() == ERole.ADMIN)
                    .toList();

            String subject = "📢 Admin Alert: Policy Suspended - " + policy.getPolicyNumber();
            String htmlBody = buildSuspensionAdminHtml(policy);

            for (User admin : admins) {
                if (admin.getEmail() != null) {
                    emailService.sendEmailWithImage(admin.getEmail(), subject, htmlBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error notifying admins of suspension: " + policy.getPolicyNumber());
            e.printStackTrace();
        }
    }

    private String buildConsolidatedHtml(String username, List<PremiumPayment> payments) {
        StringBuilder paymentRows = new StringBuilder();
        double totalDue = 0;

        for (PremiumPayment p : payments) {
            String statusColor = p.getStatus() == PaymentStatus.OVERDUE ? "#d9534f" : "#f0ad4e";
            String policyNum = p.getInsurancePolicy() != null ? p.getInsurancePolicy().getPolicyNumber() : "N/A";
            double amount = p.getAmount() != null ? p.getAmount().doubleValue() : 0;
            totalDue += amount;

            paymentRows.append("""
                <tr>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">%s</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">%s</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; font-weight: bold;">%.2f TND</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; color: %s; font-weight: bold;">%s</td>
                </tr>
                """.formatted(policyNum, p.getDueDate().toString(), amount, statusColor, p.getStatus().toString()));
        }

        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <img src="cid:logoImage" alt="Forsa Logo" style="width: 180px; margin-bottom: 20px;"/>
                <h2>Premium Payment Statement</h2>
                <p>Dear %s,</p>
                <p>This is a summary of your insurance premium payments that are currently due or overdue. Please settle these amounts to maintain your coverage.</p>
                
                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <thead>
                        <tr style="background-color: #f8f9fa;">
                            <th style="padding: 10px; text-align: left; border-bottom: 2px solid #dee2e6;">Policy Ref</th>
                            <th style="padding: 10px; text-align: left; border-bottom: 2px solid #dee2e6;">Due Date</th>
                            <th style="padding: 10px; text-align: left; border-bottom: 2px solid #dee2e6;">Amount</th>
                            <th style="padding: 10px; text-align: left; border-bottom: 2px solid #dee2e6;">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>
                
                <p style="font-size: 1.1em;"><strong>Total Outstanding: <span style="color: #1e3a8a;">%.2f TND</span></strong></p>
                
                <br>
                <p>You can pay your premiums online through your Forsa dashboard.</p>
                <p>If you have any questions, please contact our support team.</p>
                <p>Best regards,<br><strong>Forsa Insurance Team</strong></p>
            </body>
            </html>
            """.formatted(username, paymentRows.toString(), totalDue);
    }

    private String buildSuspensionClientHtml(String username, String policyNumber) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <img src="cid:logoImage" alt="Forsa Logo" style="width: 180px; margin-bottom: 20px;"/>
                <h2 style="color: #d9534f;">Policy Suspension Notice</h2>
                <p>Dear %s,</p>
                <p>We regret to inform you that your insurance policy <strong>%s</strong> has been <strong>SUSPENDED</strong> due to three consecutive missed premium payments.</p>
                <p>While your policy is suspended, you are not covered for any claims. To reactivate your policy, please settle all outstanding payments immediately.</p>
                <br>
                <p>If you have already made the payments, please contact our support team.</p>
                <p>Best regards,<br><strong>Forsa Insurance Team</strong></p>
            </body>
            </html>
            """.formatted(username, policyNumber);
    }

    private String buildSuspensionAdminHtml(InsurancePolicy policy) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <img src="cid:logoImage" alt="Forsa Logo" style="width: 180px; margin-bottom: 20px;"/>
                <h2 style="color: #d9534f;">Automated Policy Suspension Alert</h2>
                <p>The following policy has been automatically suspended by the system:</p>
                <ul>
                    <li><strong>Policy Number:</strong> %s</li>
                    <li><strong>Client:</strong> %s (%s)</li>
                    <li><strong>Product:</strong> %s</li>
                    <li><strong>Reason:</strong> 3 consecutive missed payments</li>
                    <li><strong>Suspension Date:</strong> %s</li>
                </ul>
                <p>Please review this policy and take necessary actions if needed.</p>
                <p>Best regards,<br><strong>Forsa System Monitor</strong></p>
            </body>
            </html>
            """.formatted(
                policy.getPolicyNumber(),
                policy.getUser().getUsername(),
                policy.getUser().getEmail(),
                policy.getInsuranceProduct().getProductName(),
                new Date().toString()
            );
    }
}