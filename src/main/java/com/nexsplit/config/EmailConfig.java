package com.nexsplit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Email configuration for robust SMTP settings
 */
@Configuration
@Slf4j
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();

        // SMTP Authentication
        props.put("mail.smtp.auth", "true");

        // STARTTLS Configuration
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // SSL Configuration for Gmail
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Socket Factory Configuration (only for port 465)
        if (port == 465) {
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        // Timeout Configuration (increased for reliability)
        props.put("mail.smtp.connectiontimeout", "30000"); // 60 seconds
        props.put("mail.smtp.timeout", "30000"); // 60 seconds
        props.put("mail.smtp.writetimeout", "30000"); // 60 seconds

        // Additional timeout settings for better reliability
        props.put("mail.smtp.socketFactory.timeout", "60000");
        props.put("mail.smtp.socketFactory.connectiontimeout", "60000");

        // Additional Gmail-specific settings
        props.put("mail.smtp.quitwait", "false");
        props.put("mail.smtp.ehlo", "true");
        props.put("mail.smtp.helo", "true");

        // Gmail-specific connection settings
        props.put("mail.smtp.connectionpoolsize", "10");
        props.put("mail.smtp.connectionpooltimeout", "300000"); // 5 minutes
        props.put("mail.smtp.allowreadonlyselect", "false");

        // Debug settings (enable for troubleshooting)
        props.put("mail.debug", "true");

        log.info("Email configuration initialized for host: {}:{}", host, port);

        return mailSender;
    }
}
