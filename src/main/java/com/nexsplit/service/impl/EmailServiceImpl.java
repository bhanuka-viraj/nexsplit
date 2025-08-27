package com.nexsplit.service.impl;

import com.nexsplit.service.EmailService;
import com.nexsplit.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of EmailService for sending emails asynchronously
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
    @Async("asyncExecutor")
    public CompletableFuture<Void> sendSimpleEmail(String to, String subject, String text) {
        try {
            log.info("Sending simple email to: {} with subject: '{}'", LoggingUtil.maskEmail(to), subject);
            log.debug("Email configuration - From: {}, Host: {}", LoggingUtil.maskEmail(fromEmail),
                    mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl
                            ? ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getHost()
                            : "unknown");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false); // Plain text

            log.debug("MimeMessage created successfully, attempting to send...");
            mailSender.send(message);

            log.info("Simple email sent successfully to: {}", LoggingUtil.maskEmail(to));
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {} - Error: {} - Type: {}",
                    LoggingUtil.maskEmail(to), e.getMessage(), e.getClass().getSimpleName(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Failed to send email: " + e.getMessage(), e));
        }
    }

    @Override
    @Async("asyncExecutor")
    public CompletableFuture<Void> sendPasswordResetEmail(String to, String resetToken, String username) {
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

            log.info("Password reset email sent successfully to: {}", LoggingUtil.maskEmail(to));
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", LoggingUtil.maskEmail(to), e);
            return CompletableFuture.failedFuture(new RuntimeException("Failed to send password reset email", e));
        }
    }

    @Override
    @Async("asyncExecutor")
    public CompletableFuture<Void> sendWelcomeEmail(String to, String username) {
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

            log.info("Welcome email sent successfully to: {}", LoggingUtil.maskEmail(to));
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", LoggingUtil.maskEmail(to), e);
            return CompletableFuture.failedFuture(new RuntimeException("Failed to send welcome email", e));
        }
    }

    @Override
    @Async("asyncExecutor")
    public CompletableFuture<Void> sendEmailVerification(String to, String verificationToken, String username) {
        try {
            log.info("Sending email verification to: {}", LoggingUtil.maskEmail(to));

            String subject = "NexSplit - Email Verification";

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationToken", verificationToken);
            context.setVariable("subject", subject);

            // Process the template
            String htmlContent = templateEngine.process("email/email-verification", context);

            // Send the email
            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email verification sent successfully to: {}", LoggingUtil.maskEmail(to));
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", LoggingUtil.maskEmail(to), e);
            return CompletableFuture.failedFuture(new RuntimeException("Failed to send email verification", e));
        }
    }

    @Override
    @Async("asyncExecutor")
    public CompletableFuture<Void> sendTestPreviewEmail(String to, String username) {
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

            log.info("Test preview email sent successfully to: {}", LoggingUtil.maskEmail(to));
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send test preview email to: {}", LoggingUtil.maskEmail(to), e);
            return CompletableFuture.failedFuture(new RuntimeException("Failed to send test preview email", e));
        }
    }

    /**
     * Send HTML email using Thymeleaf template with retry logic
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        int maxRetries = 3;
        int retryCount = 0;

        log.debug("Preparing to send HTML email to: {} with subject: '{}'", LoggingUtil.maskEmail(to), subject);

        while (retryCount < maxRetries) {
            try {
                log.debug("Creating MimeMessage for HTML email (attempt {}/{})", retryCount + 1, maxRetries);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true); // HTML content

                log.debug("MimeMessage created successfully, attempting to send HTML email...");
                mailSender.send(message);

                log.info("HTML email sent successfully to: {} (attempt {})", LoggingUtil.maskEmail(to), retryCount + 1);
                return; // Success, exit retry loop

            } catch (Exception e) {
                retryCount++;
                log.warn("Failed to send HTML email to: {} (attempt {}/{}): {} - Type: {}",
                        LoggingUtil.maskEmail(to), retryCount, maxRetries, e.getMessage(),
                        e.getClass().getSimpleName());

                if (retryCount >= maxRetries) {
                    log.error("All retry attempts failed for HTML email to: {} - Final error: {}",
                            LoggingUtil.maskEmail(to), e.getMessage(), e);
                    throw new MessagingException(
                            "Failed to send HTML email after " + maxRetries + " attempts: " + e.getMessage(), e);
                }

                // Wait before retrying (exponential backoff)
                try {
                    long delay = 1000 * retryCount; // 1s, 2s, 3s delays
                    log.debug("Waiting {}ms before retry attempt {}", delay, retryCount + 1);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new MessagingException("HTML email sending interrupted", ie);
                }
            }
        }
    }

}
