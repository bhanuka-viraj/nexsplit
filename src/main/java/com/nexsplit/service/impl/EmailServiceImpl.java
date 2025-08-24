package com.nexsplit.service.impl;

import com.nexsplit.service.EmailService;
import com.nexsplit.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Implementation of EmailService for sending emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${MAIL_FROM_NAME:NexSplit}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            log.info("Sending simple email to: {}", LoggingUtil.maskEmail(to));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false); // Plain text

            mailSender.send(message);

            log.info("Simple email sent successfully to: {}", LoggingUtil.maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", LoggingUtil.maskEmail(to), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken, String username) {
        try {
            log.info("Sending password reset email to: {}", LoggingUtil.maskEmail(to));

            String subject = "NexSplit - Password Reset Request";

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetToken", resetToken);
            context.setVariable("subject", subject);

            // Process the template
            String htmlContent = templateEngine.process("email/password-reset", context);

            // Send the email
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", LoggingUtil.maskEmail(to), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            log.info("Sending welcome email to: {}", LoggingUtil.maskEmail(to));

            String subject = "Welcome to NexSplit!";

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("subject", subject);

            // Process the template
            String htmlContent = templateEngine.process("email/welcome", context);

            // Send the email
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", LoggingUtil.maskEmail(to), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Override
    public void sendEmailVerification(String to, String verificationToken, String username) {
        try {
            log.info("Sending email verification to: {}", LoggingUtil.maskEmail(to));

            String subject = "Verify Your Email - NexSplit";

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationToken", verificationToken);
            context.setVariable("subject", subject);
            context.setVariable("appBaseUrl", appBaseUrl);

            // Process the template
            String htmlContent = templateEngine.process("email/email-verification", context);

            // Send the email
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", LoggingUtil.maskEmail(to), e);
            throw new RuntimeException("Failed to send email verification", e);
        }
    }

    @Override
    public void sendTestPreviewEmail(String to, String username) {
        try {
            log.info("Sending test preview email to: {}", LoggingUtil.maskEmail(to));

            String subject = "Email Template Preview - NexSplit";

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("subject", subject);

            // Process the template
            String htmlContent = templateEngine.process("email/test-preview", context);

            // Send the email
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send test preview email to: {}", LoggingUtil.maskEmail(to), e);
            throw new RuntimeException("Failed to send test preview email", e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // HTML content

        mailSender.send(message);

        log.info("HTML email sent successfully to: {}", LoggingUtil.maskEmail(to));
    }
}
