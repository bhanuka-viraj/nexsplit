package com.nexsplit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Configuration
public class ApiConfig {
        public static final String API_VERSION = "/v1";
        public static final String API_BASE_PATH = "/api" + API_VERSION;

        @Value("${CORS_ALLOWED_ORIGINS:${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}}")
        private List<String> allowedOriginsList;

        @Value("${CORS_ALLOW_CREDENTIALS:${cors.allow-credentials:true}}")
        private boolean allowedCredentials;

        @Value("${CORS_ALLOWED_METHODS:${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}}")
        private List<String> allowedMethodsList;

        @Value("${CORS_ALLOWED_HEADERS:${cors.allowed-headers:Authorization,Origin,Content-Type,Accept,X-Requested-With}}")
        private List<String> allowedHeadersList;

        public List<String> getAllowedOrigins() {
                return allowedOriginsList;
        }

        public List<String> getAllowedMethods() {
                return allowedMethodsList;
        }

        public List<String> getAllowedHeaders() {
                return allowedHeadersList;
        }
}