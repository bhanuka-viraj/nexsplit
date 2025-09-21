package com.nexsplit.service;

import com.nexsplit.dto.attachment.AttachmentDto;
import com.nexsplit.dto.attachment.AttachmentSummaryDto;
import com.nexsplit.dto.attachment.CreateAttachmentRequest;
import com.nexsplit.dto.attachment.UpdateAttachmentRequest;
import com.nexsplit.dto.PaginatedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for attachment management operations.
 * 
 * This service provides comprehensive attachment management functionality
 * including
 * file upload, download, deletion, and CDN integration for optimal file
 * delivery.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
public interface AttachmentService {

    /**
     * Upload a file and create an attachment record.
     * 
     * @param file       The file to upload
     * @param expenseId  The expense ID to associate with
     * @param uploadedBy The user ID who uploaded the file
     * @return Created attachment DTO
     */
    AttachmentDto uploadFile(MultipartFile file, String expenseId, String uploadedBy);

    /**
     * Create an attachment record for an existing file.
     * 
     * @param request The attachment creation request
     * @return Created attachment DTO
     */
    AttachmentDto createAttachment(CreateAttachmentRequest request);

    /**
     * Get attachment by ID.
     * 
     * @param attachmentId The attachment ID
     * @return Attachment DTO
     */
    AttachmentDto getAttachmentById(String attachmentId);

    /**
     * Get all attachments with pagination.
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated response of attachment DTOs
     */
    PaginatedResponse<AttachmentDto> getAllAttachments(int page, int size);

    /**
     * Get attachments by expense ID with pagination.
     * 
     * @param expenseId The expense ID
     * @param page      Page number (0-based)
     * @param size      Page size
     * @return Paginated response of attachment DTOs
     */
    PaginatedResponse<AttachmentDto> getAttachmentsByExpenseId(String expenseId, int page, int size);

    /**
     * Get attachments by bill ID with pagination.
     * 
     * @param billId The bill ID
     * @param page   Page number (0-based)
     * @param size   Page size
     * @return Paginated response of attachment DTOs
     */

    /**
     * Get attachments by user ID with pagination.
     * 
     * @param userId The user ID
     * @param page   Page number (0-based)
     * @param size   Page size
     * @return Paginated response of attachment DTOs
     */
    PaginatedResponse<AttachmentDto> getAttachmentsByUserId(String userId, int page, int size);

    /**
     * Get attachment summary by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of attachment summary DTOs
     */
    List<AttachmentSummaryDto> getAttachmentSummaryByExpenseId(String expenseId);

    /**
     * Get attachment summary by bill ID.
     * 
     * @param billId The bill ID
     * @return List of attachment summary DTOs
     */
    List<AttachmentSummaryDto> getAttachmentSummaryByBillId(String billId);

    /**
     * Get attachment URL.
     * 
     * @param attachmentId The attachment ID
     * @return Attachment URL
     */
    String getAttachmentUrl(String attachmentId);

    /**
     * Get presigned URL for attachment.
     * 
     * @param attachmentId      The attachment ID
     * @param expirationMinutes Expiration time in minutes
     * @return Presigned URL
     */
    String getPresignedUrl(String attachmentId, int expirationMinutes);

    /**
     * Get attachment analytics.
     * 
     * @return Attachment analytics
     */
    Object getAttachmentAnalytics();

    /**
     * Get attachment analytics by user ID.
     * 
     * @param userId The user ID
     * @return Attachment analytics
     */
    Object getAttachmentAnalyticsByUserId(String userId);

    /**
     * Get attachment analytics by expense ID.
     * 
     * @param expenseId The expense ID
     * @return Attachment analytics
     */
    Object getAttachmentAnalyticsByExpenseId(String expenseId);

    /**
     * Get attachment analytics by bill ID.
     * 
     * @param billId The bill ID
     * @return Attachment analytics
     */
    Object getAttachmentAnalyticsByBillId(String billId);

    /**
     * Update an existing attachment.
     * 
     * @param attachmentId The attachment ID
     * @param request      The update request
     * @return Updated attachment DTO
     */
    AttachmentDto updateAttachment(String attachmentId, UpdateAttachmentRequest request);

    /**
     * Delete an attachment (soft delete).
     * 
     * @param attachmentId The attachment ID
     * @param deletedBy    The user ID who deleted the attachment
     */
    void deleteAttachment(String attachmentId, String deletedBy);

    /**
     * Get all attachments for a specific expense.
     * 
     * @param expenseId The expense ID
     * @return List of attachment DTOs
     */
    List<AttachmentDto> getAttachmentsByExpenseId(String expenseId);

    /**
     * Get all attachments uploaded by a specific user.
     * 
     * @param userId The user ID
     * @return List of attachment DTOs
     */
    List<AttachmentDto> getAttachmentsByUploadedBy(String userId);

    /**
     * Get all attachments for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of attachment DTOs
     */
    List<AttachmentDto> getAttachmentsByNexId(String nexId);

    /**
     * Get attachments by file type.
     * 
     * @param fileType The file type
     * @return List of attachment DTOs
     */
    List<AttachmentDto> getAttachmentsByFileType(String fileType);

    /**
     * Get image attachments.
     * 
     * @return List of image attachment DTOs
     */
    List<AttachmentDto> getImageAttachments();

    /**
     * Get document attachments.
     * 
     * @return List of document attachment DTOs
     */
    List<AttachmentDto> getDocumentAttachments();

    /**
     * Get attachment download URL.
     * 
     * @param attachmentId The attachment ID
     * @return Download URL
     */
    String getDownloadUrl(String attachmentId);

    /**
     * Get attachment preview URL (for images).
     * 
     * @param attachmentId The attachment ID
     * @return Preview URL
     */
    String getPreviewUrl(String attachmentId);

    /**
     * Validate file before upload.
     * 
     * @param file The file to validate
     * @return Validation result
     */
    FileValidationResult validateFile(MultipartFile file);

    /**
     * Get attachment summary for a user.
     * 
     * @param userId The user ID
     * @return List of attachment summary DTOs
     */
    List<AttachmentSummaryDto> getAttachmentSummaryByUserId(String userId);

    /**
     * Get attachment summary for a nex group.
     * 
     * @param nexId The nex ID
     * @return List of attachment summary DTOs
     */
    List<AttachmentSummaryDto> getAttachmentSummaryByNexId(String nexId);

    /**
     * Get attachment statistics for a user.
     * 
     * @param userId The user ID
     * @return Attachment statistics
     */
    AttachmentStatistics getAttachmentStatisticsByUserId(String userId);

    /**
     * Get attachment statistics for a nex group.
     * 
     * @param nexId The nex ID
     * @return Attachment statistics
     */
    AttachmentStatistics getAttachmentStatisticsByNexId(String nexId);

    /**
     * Clean up orphaned attachments.
     * This method removes attachments that are no longer associated with any
     * expense.
     * 
     * @return Number of attachments cleaned up
     */
    int cleanupOrphanedAttachments();

    /**
     * File validation result data class.
     */
    class FileValidationResult {
        private boolean valid;
        private String message;
        private List<String> errors;

        public FileValidationResult(boolean valid, String message, List<String> errors) {
            this.valid = valid;
            this.message = message;
            this.errors = errors;
        }

        // Getters and setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }

    /**
     * Attachment statistics data class.
     */
    class AttachmentStatistics {
        private int totalAttachments;
        private int imageCount;
        private int documentCount;
        private int otherCount;
        private int expensesWithAttachments;
        private int nexGroupsWithAttachments;
        private int usersWithAttachments;

        public AttachmentStatistics(int totalAttachments, int imageCount, int documentCount, int otherCount,
                int expensesWithAttachments, int nexGroupsWithAttachments, int usersWithAttachments) {
            this.totalAttachments = totalAttachments;
            this.imageCount = imageCount;
            this.documentCount = documentCount;
            this.otherCount = otherCount;
            this.expensesWithAttachments = expensesWithAttachments;
            this.nexGroupsWithAttachments = nexGroupsWithAttachments;
            this.usersWithAttachments = usersWithAttachments;
        }

        // Getters and setters
        public int getTotalAttachments() {
            return totalAttachments;
        }

        public void setTotalAttachments(int totalAttachments) {
            this.totalAttachments = totalAttachments;
        }

        public int getImageCount() {
            return imageCount;
        }

        public void setImageCount(int imageCount) {
            this.imageCount = imageCount;
        }

        public int getDocumentCount() {
            return documentCount;
        }

        public void setDocumentCount(int documentCount) {
            this.documentCount = documentCount;
        }

        public int getOtherCount() {
            return otherCount;
        }

        public void setOtherCount(int otherCount) {
            this.otherCount = otherCount;
        }

        public int getExpensesWithAttachments() {
            return expensesWithAttachments;
        }

        public void setExpensesWithAttachments(int expensesWithAttachments) {
            this.expensesWithAttachments = expensesWithAttachments;
        }

        public int getNexGroupsWithAttachments() {
            return nexGroupsWithAttachments;
        }

        public void setNexGroupsWithAttachments(int nexGroupsWithAttachments) {
            this.nexGroupsWithAttachments = nexGroupsWithAttachments;
        }

        public int getUsersWithAttachments() {
            return usersWithAttachments;
        }

        public void setUsersWithAttachments(int usersWithAttachments) {
            this.usersWithAttachments = usersWithAttachments;
        }
    }
}
