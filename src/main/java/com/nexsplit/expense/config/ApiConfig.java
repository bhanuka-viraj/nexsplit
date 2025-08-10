package com.nexsplit.expense.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    public static final String API_VERSION = "/v1";
    public static final String API_BASE_PATH = "/api" + API_VERSION;

    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String[] allowedOrigins;

    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }
}