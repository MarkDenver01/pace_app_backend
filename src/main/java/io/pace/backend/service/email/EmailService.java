package io.pace.backend.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset");
        message.setText("Reset your password here: " + resetUrl);
        mailSender.send(message);
    }

    public void sendTemporaryPassword(String to, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Temporary Account Password");
        message.setText("Hello,\n\nYour temporary password is: " + tempPassword
                + "\n\nPlease log in and change your password immediately.");
        mailSender.send(message);
    }

    public void sendEmail(String to, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[PACE] ACCOUNT VERIFIED");
        message.setText("Hello " + userName + ", \n\nYour account has been verified.\n" +
                "You can have now the rights to logged in.");
    }
}
