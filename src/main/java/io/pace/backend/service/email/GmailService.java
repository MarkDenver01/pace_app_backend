package io.pace.backend.service.email;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;


@Service
public class GmailService {
    @Autowired
    @Lazy
    Gmail gmailClient;

    @Value("${base.url.react}")
    private String baseUrl;

    private static final SecureRandom random = new SecureRandom();

    // Common method to send any message via Gmail API
    private void sendMessage(String to, String subject, String text) {
        requireGmailLinked();

        try {
            Properties props = new Properties();
            Session session = Session.getInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress("me", "WrapAndCarry")); // Gmail API uses "me"
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject(subject);
            email.setText(text);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encodedEmail);

            gmailClient.users().messages().send("me", message).execute();
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
                + "\n\nBase University URL: " + baseUrl +"/university/" + universityId;
        sendMessage(to, subject, text);
    }

    public void sendEmail(String to, String userName) {
        String subject = "[PACE] ACCOUNT VERIFIED";
        String text = "Hello " + userName + ", \n\nYour account has been verified.\n" +
                "You can now log in.";
        sendMessage(to, subject, text);
    }

    public void sendVerificationCode(String to, String verificationCode) {
        String subject = "ACCOUNT VERIFICATION";
        String text = "Thanks for registering the account, your registration is successful." +
                "\nTo complete the activation process, just enter the verification code below on the mobile app." +
                "\nVERIFICATION CODE: " + verificationCode;
        sendMessage(to, subject, text);
    }

    public String generateVerificationCode() {
        // Generate a number between 0 and 9999
        int code = random.nextInt(10000);

        // Format it to always be 4 digits (e.g., 0057, 0423)
        return String.format("%04d", code);
    }

    private void requireGmailLinked() {
        if (gmailClient == null) {
            throw new RuntimeException("Gmail is not linked yet. Visit /user/auth to link Gmail.");
        }
    }

}
