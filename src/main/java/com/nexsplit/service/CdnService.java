package com.nexsplit.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service interface for CDN operations.
 * 
 * This service provides methods for file upload, download, deletion, and
 * management operations across different CDN providers. It abstracts the
 * complexity of different CDN implementations behind a unified interface.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
public interface CdnService {

    /**
     * Upload a file to the CDN.
     * 
     * @param file     The file to upload
     * @param path     The path where the file should be stored
     * @param metadata Optional metadata for the file
     * @return The CDN URL of the uploaded file
     * @throws Exception if upload fails
     */
    String uploadFile(MultipartFile file, String path, Map<String, String> metadata) throws Exception;

    /**
     * Upload a file from input stream to the CDN.
     * 
     * @param inputStream The input stream of the file
     * @param path        The path where the file should be stored
     * @param contentType The content type of the file
     * @param metadata    Optional metadata for the file
     * @return The CDN URL of the uploaded file
     * @throws Exception if upload fails
     */
    String uploadFile(InputStream inputStream, String path, String contentType, Map<String, String> metadata)
            throws Exception;

    /**
     * Download a file from the CDN.
     * 
     * @param path The path of the file to download
     * @return The input stream of the file
     * @throws Exception if download fails
     */
    InputStream downloadFile(String path) throws Exception;

    /**
     * Delete a file from the CDN.
     * 
     * @param path The path of the file to delete
     * @return true if deletion was successful
     * @throws Exception if deletion fails
     */
    boolean deleteFile(String path) throws Exception;

    /**
     * Check if a file exists in the CDN.
     * 
     * @param path The path of the file to check
     * @return true if file exists
     * @throws Exception if check fails
     */
    boolean fileExists(String path) throws Exception;

    /**
     * Get file metadata from the CDN.
     * 
     * @param path The path of the file
     * @return Map of file metadata
     * @throws Exception if metadata retrieval fails
     */
    Map<String, String> getFileMetadata(String path) throws Exception;

    /**
     * List files in a directory.
     * 
     * @param directory The directory path
     * @return List of file paths
     * @throws Exception if listing fails
     */
    List<String> listFiles(String directory) throws Exception;

    /**
     * Generate a presigned URL for file access.
     * 
     * @param path              The path of the file
     * @param expirationMinutes URL expiration time in minutes
     * @return Presigned URL
     * @throws Exception if URL generation fails
     */
    String generatePresignedUrl(String path, int expirationMinutes) throws Exception;

    /**
     * Copy a file within the CDN.
     * 
     * @param sourcePath      The source file path
     * @param destinationPath The destination file path
     * @return true if copy was successful
     * @throws Exception if copy fails
     */
    boolean copyFile(String sourcePath, String destinationPath) throws Exception;

    /**
     * Move a file within the CDN.
     * 
     * @param sourcePath      The source file path
     * @param destinationPath The destination file path
     * @return true if move was successful
     * @throws Exception if move fails
     */
    boolean moveFile(String sourcePath, String destinationPath) throws Exception;

    /**
     * Get the CDN provider name.
     * 
     * @return CDN provider name
     */
    String getProviderName();

    /**
     * Get the CDN base URL.
     * 
     * @return CDN base URL
     */
    String getBaseUrl();

    /**
     * Validate file before upload.
     * 
     * @param file The file to validate
     * @return true if file is valid
     */
    boolean validateFile(MultipartFile file);

    /**
     * Get file size from CDN.
     * 
     * @param path The path of the file
     * @return File size in bytes
     * @throws Exception if size retrieval fails
     */
    long getFileSize(String path) throws Exception;

    /**
     * Get file content type from CDN.
     * 
     * @param path The path of the file
     * @return File content type
     * @throws Exception if content type retrieval fails
     */
    String getFileContentType(String path) throws Exception;

    /**
     * Clean up expired files.
     * 
     * @return Number of files cleaned up
     * @throws Exception if cleanup fails
     */
    int cleanupExpiredFiles() throws Exception;

    /**
     * Get CDN storage statistics.
     * 
     * @return Map of storage statistics
     * @throws Exception if statistics retrieval fails
     */
    Map<String, Object> getStorageStatistics() throws Exception;
}
