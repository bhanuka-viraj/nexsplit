package com.nexsplit.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ApiConfig {
        public static final String API_VERSION = "/v1";
        public static final String API_BASE_PATH = "/api" + API_VERSION;

        // values will be set in application.yml or env
        private final String[] allowedOrigins = {
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
        };

        private final boolean allowedCredentials = true;

        private final String[] allowedMethods = {
                        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        };

        private final String[] allowedHeaders = {
                        "Authorization", "Origin", "Content-Type", "Accept", "X-Requested-With"
        };

}