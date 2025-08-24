package com.nexsplit.service;

/**
 * Service interface for sending emails
 */
public interface EmailService {

    /**
     * Send a simple text email
     * 
     * @param to      recipient email address
     * @param subject email subject
     * @param text    email body text
     */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Send password reset email
     * 
     * @param to         recipient email address
     * @param resetToken password reset token
     * @param username   user's username
     */
    void sendPasswordResetEmail(String to, String resetToken, String username);

    /**
     * Send welcome email to new user
     * 
     * @param to       recipient email address
     * @param username user's username
     */
    void sendWelcomeEmail(String to, String username);

    /**
     * Send email verification email
     * 
     * @param to                recipient email address
     * @param verificationToken email verification token
     * @param username          user's username
     */
    void sendEmailVerification(String to, String verificationToken, String username);

    /**
     * Send test preview email showing all templates
     * 
     * @param to       recipient email address
     * @param username user's username
     */
    void sendTestPreviewEmail(String to, String username);
}
