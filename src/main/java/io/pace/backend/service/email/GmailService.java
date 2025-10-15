package io.pace.backend.service.email;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;


@Service
public class GmailService {
    @Autowired
    Gmail gmail;

    // Common method to send any message via Gmail API
    private void sendMessage(String to, String subject, String text) {
        try {
            Properties props = new Properties();
            Session session = Session.getInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress("me")); // Gmail API uses "me"
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject(subject);
            email.setText(text);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encodedEmail);

            gmail.users().messages().send("me", message).execute();
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Failed to send email via Gmail API: " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String to, String resetUrl) {
        String subject = "Password Reset";
        String text = "Reset your password here: " + resetUrl;
        sendMessage(to, subject, text);
    }

    public void sendTemporaryPassword(String to, String tempPassword, Long universityId) {
        String subject = "Your Temporary Account Password";
        String text = "Hello,\n\nYour temporary password is: " + tempPassword
                + "\n\nPlease log in and change your password immediately."
                + "\n\nBase University URL: http://localhost:3000/university/" + universityId;
        sendMessage(to, subject, text);
    }

    public void sendEmail(String to, String userName) {
        String subject = "[PACE] ACCOUNT VERIFIED";
        String text = "Hello " + userName + ", \n\nYour account has been verified.\n" +
                "You can now log in.";
        sendMessage(to, subject, text);
    }
}
