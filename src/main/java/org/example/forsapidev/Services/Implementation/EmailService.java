package org.example.forsapidev.Services.Implementation;
import org.example.forsapidev.Services.Interfaces.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.reminder.email.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPremiumReminderEmail(String toEmail, String userName, String policyNumber,
                                         String dueDate, Double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🔔 Premium Payment Reminder - Policy " + policyNumber);

        String body = String.format(
                "Dear %s,\n\n" +
                        "This is a friendly reminder that your insurance premium payment is due soon.\n\n" +
                        "Policy Number: %s\n" +
                        "Due Date: %s\n" +
                        "Amount: $%.2f\n\n" +
                        "Please ensure you have sufficient balance in your wallet to avoid service interruption.\n\n" +
                        "Thank you for choosing Forsa Insurance!\n\n" +
                        "Best regards,\n" +
                        "Forsa Team",
                userName, policyNumber, dueDate, amount
        );

        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Reminder email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + toEmail);
            e.printStackTrace();
        }
    }

    @Override
    public void sendOverduePaymentEmail(String toEmail, String userName, String policyNumber,
                                        String dueDate, Double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("⚠️ URGENT: Overdue Premium Payment - Policy " + policyNumber);

        String body = String.format(
                "Dear %s,\n\n" +
                        "URGENT: Your insurance premium payment is now OVERDUE.\n\n" +
                        "Policy Number: %s\n" +
                        "Due Date: %s (OVERDUE)\n" +
                        "Amount: $%.2f\n\n" +
                        "Please make the payment immediately to avoid policy suspension.\n" +
                        "Your policy may be suspended if payment is not received within 3 days.\n\n" +
                        "Contact us if you need assistance.\n\n" +
                        "Best regards,\n" +
                        "Forsa Team",
                userName, policyNumber, dueDate, amount
        );

        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Overdue email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + toEmail);
            e.printStackTrace();
        }
    }
}