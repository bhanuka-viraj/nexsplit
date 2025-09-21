package com.nexsplit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration properties.
 * 
 * This configuration class provides access to application-specific
 * configuration properties defined in application.yml.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@Configuration
public class AppConfig {

    @Value("${app.default.currency}")
    private String defaultCurrency;
}
