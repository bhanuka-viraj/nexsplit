package com.nexsplit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for RestClient
 * Replaces RestTemplate with Spring's modern HTTP client
 * Used for external API calls like Google OAuth2 validation and Elasticsearch
 * operations
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .build();
    }

}
