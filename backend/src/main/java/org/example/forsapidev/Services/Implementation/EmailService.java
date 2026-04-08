package org.example.forsapidev.Services.Implementation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Sends HTML email with logo to the user who owns the policy
     */
    public void sendEmailWithImage(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);           // From: your application email
            helper.setTo(toEmail);               // To: the actual user's email
            helper.setSubject(subject);
            helper.setText(htmlBody, true);      // true = HTML

            // Add logo as inline image
            ClassPathResource logo = new ClassPathResource("logoforsa.png");
            helper.addInline("logoImage", logo);

            javaMailSender.send(mimeMessage);
            System.out.println("✅ Email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to: " + toEmail);
            e.printStackTrace();
        }
    }
}