package org.example.forsapidev.Services.Implementation;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
public class EmailSenderService {

    @Autowired
    private final JavaMailSender javaMailSender;


    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailWithImage(String toEmail, String subject, String body, String imagePath) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            // Ajouter l'image comme une ressource
            ClassPathResource image = new ClassPathResource("logoforsa.png");

            helper.addInline("image", image);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Gérer les erreurs de messagerie
            e.printStackTrace();
        }
    }
}
