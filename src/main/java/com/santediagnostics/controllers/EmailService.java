package com.santediagnostics.utils;

import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    
    // Configure these with your email settings
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "santediagnostics@gmail.com"; // Change this
    private static final String EMAIL_PASSWORD = "your-app-password"; // Use App Password for Gmail
    
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String verificationLink = "http://localhost:8080/verify?token=" + token;
        String subject = "Verify Your Sante Diagnostics Account";
        
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Welcome to Sante Diagnostics!</h2>
                    <p>Dear """ + fullName + """,</p>
                    <p>Thank you for registering with Sante Diagnostics LIMS. Please verify your email address by clicking the link below:</p>
                    <p><a href="""" + verificationLink + """ 
                          style="background-color: #3498db; color: white; padding: 10px 20px; 
                                 text-decoration: none; border-radius: 5px;">
                        Verify Email Address
                    </a></p>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="color: #3498db;">""" + verificationLink + """</p>
                    <p>This link will expire in 24 hours.</p>
                    <hr>
                    <p style="color: #7f8c8d; font-size: 12px;">If you did not create an account, please ignore this email.</p>
                </div>
            </body>
            </html>
        """;
        
        sendEmail(toEmail, subject, htmlContent);
    }
    
    public void sendResultReadyEmail(String toEmail, String fullName, String testName, String requestId) {
        String subject = "Your Test Results Are Ready - Sante Diagnostics";
        
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Your Results Are Ready</h2>
                    <p>Dear """ + fullName + """,</p>
                    <p>We are pleased to inform you that your <strong>""" + testName + """</strong> test results are now available.</p>
                    <p>Please log in to your Sante Diagnostics account to view and download your results.</p>
                    <p><a href="#" style="background-color: #3498db; color: white; padding: 10px 20px; 
                          text-decoration: none; border-radius: 5px;">
                        View Your Results
                    </a></p>
                    <hr>
                    <p style="color: #7f8c8d; font-size: 12px;">Thank you for choosing Sante Diagnostics.</p>
                </div>
            </body>
            </html>
        """;
        
        sendEmail(toEmail, subject, htmlContent);
    }
    
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
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
            
            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}