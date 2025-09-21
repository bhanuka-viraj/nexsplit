package com.nexsplit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for CDN integration.
 * 
 * This class provides configuration properties for CDN providers including
 * AWS S3, Google Cloud Storage, Azure Blob, and CloudFlare R2.
 * All properties are configurable via application properties.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "nexsplit.cdn")
@Data
public class CdnConfig {

    /**
     * CDN provider type.
     * Supported values: aws-s3, google-cloud, azure-blob, cloudflare-r2
     */
    private String provider = "aws-s3";

    /**
     * CDN base URL for file access.
     */
    private String baseUrl;

    /**
     * CDN region for the storage service.
     */
    private String region;

    /**
     * CDN bucket/container name.
     */
    private String bucket;

    /**
     * CDN access key ID.
     */
    private String accessKeyId;

    /**
     * CDN secret access key.
     */
    private String secretAccessKey;

    /**
     * CDN endpoint URL (for custom endpoints).
     */
    private String endpoint;

    /**
     * Maximum file size in bytes (default: 10MB).
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * Allowed file types/extensions.
     */
    private String[] allowedFileTypes = {
            "jpg", "jpeg", "png", "gif", "webp", // Images
            "pdf", "doc", "docx", "txt", "xls", "xlsx", // Documents
            "mp4", "avi", "mov", "wmv", // Videos
            "mp3", "wav", "flac", "aac" // Audio
    };

    /**
     * File upload timeout in milliseconds (default: 30 seconds).
     */
    private long uploadTimeout = 30000;

    /**
     * File download timeout in milliseconds (default: 60 seconds).
     */
    private long downloadTimeout = 60000;

    /**
     * Enable file compression for uploads.
     */
    private boolean enableCompression = true;

    /**
     * Enable file encryption for uploads.
     */
    private boolean enableEncryption = false;

    /**
     * File retention period in days (0 = never expire).
     */
    private int retentionDays = 0;

    /**
     * Enable CDN caching.
     */
    private boolean enableCaching = true;

    /**
     * CDN cache TTL in seconds (default: 1 hour).
     */
    private int cacheTtl = 3600;

    /**
     * Enable automatic file cleanup.
     */
    private boolean enableAutoCleanup = true;

    /**
     * Cleanup schedule (cron expression).
     */
    private String cleanupSchedule = "0 0 2 * * ?"; // Daily at 2 AM

    /**
     * AWS S3 specific configuration.
     */
    @Data
    public static class AwsS3Config {
        private String bucket;
        private String region;
        private String accessKeyId;
        private String secretAccessKey;
        private String endpoint;
        private boolean pathStyleAccess = false;
        private boolean useAccelerateEndpoint = false;
    }

    /**
     * Google Cloud Storage specific configuration.
     */
    @Data
    public static class GoogleCloudConfig {
        private String bucket;
        private String projectId;
        private String credentialsPath;
        private String serviceAccountKey;
    }

    /**
     * Azure Blob Storage specific configuration.
     */
    @Data
    public static class AzureBlobConfig {
        private String container;
        private String accountName;
        private String accountKey;
        private String connectionString;
        private String endpoint;
    }

    /**
     * CloudFlare R2 specific configuration.
     */
    @Data
    public static class CloudFlareR2Config {
        private String bucket;
        private String accountId;
        private String accessKeyId;
        private String secretAccessKey;
        private String endpoint;
    }

    /**
     * Get the appropriate configuration based on the provider.
     * 
     * @return Provider-specific configuration
     */
    public Object getProviderConfig() {
        return switch (provider.toLowerCase()) {
            case "aws-s3" -> new AwsS3Config();
            case "google-cloud" -> new GoogleCloudConfig();
            case "azure-blob" -> new AzureBlobConfig();
            case "cloudflare-r2" -> new CloudFlareR2Config();
            default -> throw new IllegalArgumentException("Unsupported CDN provider: " + provider);
        };
    }

    /**
     * Check if the CDN configuration is valid.
     * 
     * @return true if configuration is valid
     */
    public boolean isValid() {
        return provider != null && !provider.isEmpty() &&
                baseUrl != null && !baseUrl.isEmpty() &&
                bucket != null && !bucket.isEmpty();
    }

    /**
     * Get the CDN provider display name.
     * 
     * @return Provider display name
     */
    public String getProviderDisplayName() {
        return switch (provider.toLowerCase()) {
            case "aws-s3" -> "Amazon S3";
            case "google-cloud" -> "Google Cloud Storage";
            case "azure-blob" -> "Azure Blob Storage";
            case "cloudflare-r2" -> "CloudFlare R2";
            default -> provider;
        };
    }
}
