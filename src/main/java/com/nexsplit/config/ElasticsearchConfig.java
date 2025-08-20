package com.nexsplit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Configuration class for Elasticsearch integration
 * Provides connection management for Elasticsearch
 */
@Configuration
@Slf4j
public class ElasticsearchConfig {

    @Value("${elasticsearch.host:elasticsearch}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Value("${elasticsearch.protocol:http}")
    private String elasticsearchProtocol;

    @Value("${elasticsearch.index-prefix:nexsplit-logs}")
    private String indexPrefix;

    @Value("${elasticsearch.environment:development}")
    private String environment;

    /**
     * RestTemplate bean for Elasticsearch HTTP operations
     */
    @Bean
    public RestTemplate elasticsearchRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Check Elasticsearch health status
     */
    public boolean isElasticsearchHealthy() {
        try {
            String healthUrl = String.format("%s://%s:%d/_cluster/health",
                    elasticsearchProtocol, elasticsearchHost, elasticsearchPort);

            ResponseEntity<Map> response = elasticsearchRestTemplate().getForEntity(healthUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> healthData = response.getBody();
                String status = (String) healthData.get("status");
                log.info("Elasticsearch health check - Status: {}, Cluster: {}, Nodes: {}",
                        status, healthData.get("cluster_name"), healthData.get("number_of_nodes"));
                return "green".equals(status) || "yellow".equals(status);
            } else {
                log.warn("Elasticsearch health check failed - Status code: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.warn("Elasticsearch health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get Elasticsearch base URL
     */
    public String getElasticsearchUrl() {
        return String.format("%s://%s:%d", elasticsearchProtocol, elasticsearchHost, elasticsearchPort);
    }

    /**
     * Get index name for current environment
     */
    public String getIndexName() {
        return String.format("%s-%s-%s", indexPrefix, environment,
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")));
    }

    /**
     * Get index pattern for current environment
     */
    public String getIndexPattern() {
        return String.format("%s-%s-*", indexPrefix, environment);
    }
}
