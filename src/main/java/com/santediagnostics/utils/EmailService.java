package com.santediagnostics.utils;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    // Using the exact credentials that we know work on your network
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String EMAIL_FROM = "dasilvasegun10@gmail.com"; 
    private static final String EMAIL_PASSWORD = "iknmrgnhaicxpagz"; 

    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String subject = "Verify Your Sante Diagnostics Account";

        String htmlContent = "<html>\n" +
            "<body style=\"font-family: Arial, sans-serif;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
            "        <h2 style=\"color: #2c3e50;\">Welcome to Sante Diagnostics!</h2>\n" +
            "        <p>Dear " + fullName + ",</p>\n" +
            "        <p>Thank you for registering. To activate your account, please copy your secure verification token below:</p>\n" +
            "        <div style=\"background-color: #edf2f7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;\">\n" +
            "            <h3 style=\"color: #2b6cb0; margin: 0; letter-spacing: 1px;\">" + token + "</h3>\n" +
            "        </div>\n" +
            "        <p>Return to the Sante LIMS desktop application and paste this token into the verification screen to complete your registration.</p>\n" +
            "        <hr>\n" +
            "        <p style=\"color: #7f8c8d; font-size: 12px;\">If you did not create an account, please ignore this email.</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendResultReadyEmail(String toEmail, String fullName, String testName, String requestId) {
        String subject = "Your Test Results Are Ready - Sante Diagnostics";

        String htmlContent = "<html>\n" +
            "<body style=\"font-family: Arial, sans-serif;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
            "        <h2 style=\"color: #2c3e50;\">Your Results Are Ready</h2>\n" +
            "        <p>Dear  " + fullName + ",</p>\n" +
            "        <p>We are pleased to inform you that your <strong>" + testName + "</strong> test results are now available.</p>\n" +
            "        <p>Please log in to your Sante Diagnostics account to view and download your results.</p>\n" +
            "        <p><a href=\"#\" style=\"background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">\n" +
            "            View Your Results\n" +
            "        </a></p>\n" +
            "        <hr>\n" +
            "        <p style=\"color: #7f8c8d; font-size: 12px;\">Thank you for choosing Sante Diagnostics.</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

        sendEmail(toEmail, subject, htmlContent);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        // THE MISSING LINK: Explicitly trust the Gmail server to prevent timeouts
        props.put("mail.smtp.ssl.enable", "true"); 
        
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            new Thread(() -> {
                try {
                    Transport.send(message);
                    System.out.println("Email sent successfully to: " + toEmail);
                } catch (MessagingException e) {
                    System.err.println("Background Email Failed: " + e.getMessage());
                }
            }).start();

        } catch (MessagingException e) {
            System.err.println("Failed to build email message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}