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

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmailToAdmin(User user, String token) {
        String verifyLink = "http://157.20.196.25:8080/admin/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(fromEmail); // change if needed
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

    public void sendNewPasswordEmail(String toEmail, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset - Your New Password");
        message.setText("Hello,\n\n" +
                "Your password has been reset successfully.\n\n" +
                "Your new password is: " + newPassword + "\n\n" +
                "Please login with this password and change it immediately for security purposes.\n\n" +
                "If you did not request this password reset, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "Support Team");

        mailSender.send(message);
    }
}
