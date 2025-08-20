package com.nexsplit.service.impl;

import com.nexsplit.service.EmailService;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Email service implementation with async operations
 * Uses virtual threads for better scalability and performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${app.email.from:noreply@nexsplit.com}")
    private String fromEmail;

    @Value("${app.email.from-name:NexSplit}")
    private String fromName;

    @Value("${app.email.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Send password reset email asynchronously
     * 
     * BENEFITS:
     * - Non-blocking operation (user gets immediate response)
     * - SMTP operations run in virtual thread
     * - Comprehensive logging and monitoring
     * - Error handling with fallback
     */
    @Async("asyncExecutor")
    @Override
    public CompletableFuture<String> sendPasswordResetEmailAsync(String email, int resetToken, String username) {
        try {
            log.info("Starting to send password reset email to: {}", email);

            // Simulate SMTP operation (I/O-bound)
            TimeUnit.MILLISECONDS.sleep(500);

            // Generate email content
            String subject = "Password Reset Request - NexSplit";
            String htmlContent = generatePasswordResetEmailContent(username, resetToken);

            // TODO: Replace with actual SMTP implementation
            // JavaMailSender.send(createMimeMessage(to, subject, htmlContent));

            // Log the email sending event
            StructuredLoggingUtil.logBusinessEvent(
                    "EMAIL_SENT",
                    email,
                    "PASSWORD_RESET_EMAIL",
                    "SUCCESS",
                    java.util.Map.of(
                            "subject", subject,
                            "username", username,
                            "thread", Thread.currentThread().getName()));

            log.info("Password reset email sent successfully to: {}", email);
            return CompletableFuture.completedFuture("Password reset email sent to " + email);

        } catch (InterruptedException e) {
            log.error("Failed to send password reset email to: {}", email, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send welcome email asynchronously for new users
     * 
     * BENEFITS:
     * - Non-blocking user registration flow
     * - Personalized welcome message
     * - Background email processing
     */
    @Async("asyncExecutor")
    @Override
    public CompletableFuture<String> sendWelcomeEmailAsync(String email, String fullName) {
        try {
            log.info("Starting to send welcome email to: {}", email);

            // Simulate SMTP operation (I/O-bound)
            TimeUnit.MILLISECONDS.sleep(300);

            // Generate email content
            String subject = "Welcome to NexSplit!";
            String htmlContent = generateWelcomeEmailContent(fullName);

            // TODO: Replace with actual SMTP implementation
            // JavaMailSender.send(createMimeMessage(to, subject, htmlContent));

            // Log the email sending event
            StructuredLoggingUtil.logBusinessEvent(
                    "EMAIL_SENT",
                    email,
                    "WELCOME_EMAIL",
                    "SUCCESS",
                    java.util.Map.of(
                            "subject", subject,
                            "fullName", fullName,
                            "thread", Thread.currentThread().getName()));

            log.info("Welcome email sent successfully to: {}", email);
            return CompletableFuture.completedFuture("Welcome email sent to " + email);

        } catch (InterruptedException e) {
            log.error("Failed to send welcome email to: {}", email, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send email confirmation asynchronously
     * 
     * BENEFITS:
     * - Non-blocking email confirmation flow
     * - Secure token-based confirmation
     * - Background processing
     */
    @Async("asyncExecutor")
    @Override
    public CompletableFuture<String> sendEmailConfirmationAsync(String email, String confirmationToken,
            String username) {
        try {
            log.info("Starting to send email confirmation to: {}", email);

            // Simulate SMTP operation (I/O-bound)
            TimeUnit.MILLISECONDS.sleep(400);

            // Generate email content
            String subject = "Confirm Your Email - NexSplit";
            String htmlContent = generateEmailConfirmationContent(username, confirmationToken);

            // TODO: Replace with actual SMTP implementation
            // JavaMailSender.send(createMimeMessage(to, subject, htmlContent));

            // Log the email sending event
            StructuredLoggingUtil.logBusinessEvent(
                    "EMAIL_SENT",
                    email,
                    "EMAIL_CONFIRMATION",
                    "SUCCESS",
                    java.util.Map.of(
                            "subject", subject,
                            "username", username,
                            "thread", Thread.currentThread().getName()));

            log.info("Email confirmation sent successfully to: {}", email);
            return CompletableFuture.completedFuture("Email confirmation sent to " + email);

        } catch (InterruptedException e) {
            log.error("Failed to send email confirmation to: {}", email, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Failed to send email confirmation to: {}", email, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send generic email asynchronously
     * 
     * BENEFITS:
     * - Flexible email sending for any purpose
     * - Non-blocking operation
     * - Custom HTML content support
     */
    @Async("asyncExecutor")
    @Override
    public CompletableFuture<String> sendEmailAsync(String to, String subject, String htmlContent) {
        try {
            log.info("Starting to send generic email to: {}", to);

            // Simulate SMTP operation (I/O-bound)
            TimeUnit.MILLISECONDS.sleep(600);

            // TODO: Replace with actual SMTP implementation
            // JavaMailSender.send(createMimeMessage(to, subject, htmlContent));

            // Log the email sending event
            StructuredLoggingUtil.logBusinessEvent(
                    "EMAIL_SENT",
                    to,
                    "GENERIC_EMAIL",
                    "SUCCESS",
                    java.util.Map.of(
                            "subject", subject,
                            "thread", Thread.currentThread().getName()));

            log.info("Generic email sent successfully to: {}", to);
            return CompletableFuture.completedFuture("Email sent to " + to);

        } catch (InterruptedException e) {
            log.error("Failed to send generic email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Failed to send generic email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generate password reset email content
     */
    private String generatePasswordResetEmailContent(String username, int resetToken) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Password Reset - NexSplit</title>
                </head>
                <body>
                    <h2>Hello %s!</h2>
                    <p>You requested a password reset for your NexSplit account.</p>
                    <p>Your reset token is: <strong>%06d</strong></p>
                    <p>This token will expire in 15 minutes.</p>
                    <p>If you didn't request this reset, please ignore this email.</p>
                    <p>Best regards,<br>The NexSplit Team</p>
                </body>
                </html>
                """, username, resetToken);
    }

    /**
     * Generate welcome email content
     */
    private String generateWelcomeEmailContent(String fullName) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <title>Welcome to NexSplit!</title>
                        </head>
                        <body>
                            <h2>Welcome to NexSplit, %s!</h2>
                            <p>Thank you for joining NexSplit. We're excited to help you manage your expenses and split bills with friends and family.</p>
                            <p>Get started by creating your first expense group and adding some expenses!</p>
                            <p>If you have any questions, feel free to reach out to our support team.</p>
                            <p>Best regards,<br>The NexSplit Team</p>
                        </body>
                        </html>
                        """,
                fullName);
    }

    /**
     * Generate email confirmation content
     */
    private String generateEmailConfirmationContent(String username, String confirmationToken) {
        String confirmationUrl = baseUrl + "/api/v1/auth/confirm-email?token=" + confirmationToken;

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Confirm Your Email - NexSplit</title>
                </head>
                <body>
                    <h2>Hello %s!</h2>
                    <p>Please confirm your email address to complete your NexSplit registration.</p>
                    <p><a href="%s">Click here to confirm your email</a></p>
                    <p>Or copy and paste this link in your browser: %s</p>
                    <p>This link will expire in 24 hours.</p>
                    <p>Best regards,<br>The NexSplit Team</p>
                </body>
                </html>
                """, username, confirmationUrl, confirmationUrl);
    }
}
