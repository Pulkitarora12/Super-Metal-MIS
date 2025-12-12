package com.notes.notes.service;

import com.notes.notes.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmailToAdmin(User user, String token) {
        String verifyLink = "http://157.20.196.25:8080/admin/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("info@supermetal.co.in"); // change if needed
        message.setSubject("New Account Verification Required");
        message.setText(
                "New user registered!\n\n" +
                "👤 Username: " + user.getUserName() + "\n" +
                "📧 Email: " + user.getEmail() + "\n\n" +
                "Click the link below to verify the account:\n" +
                verifyLink + "\n\n" +
                "⚠ This link will expire in 24 hours."
        );

        mailSender.send(message);
    }
}
