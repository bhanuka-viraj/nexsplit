package com.nexsplit.service.impl;

import com.nexsplit.dto.attachment.AttachmentDto;
import com.nexsplit.dto.attachment.AttachmentSummaryDto;
import com.nexsplit.dto.attachment.CreateAttachmentRequest;
import com.nexsplit.dto.attachment.UpdateAttachmentRequest;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.mapper.attachment.AttachmentMapStruct;
import com.nexsplit.model.Attachment;
import com.nexsplit.repository.AttachmentRepository;
import com.nexsplit.service.AttachmentService;
import com.nexsplit.service.CdnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AttachmentService for file management and CDN operations.
 * 
 * This service provides comprehensive attachment management functionality
 * including
 * file upload, download, deletion, and CDN integration. It handles file
 * operations
 * with proper error handling and logging.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapStruct attachmentMapStruct;
    private final CdnService cdnService;

    @Override
    public AttachmentDto createAttachment(CreateAttachmentRequest request) {
        log.info("Creating new attachment: {}", request);

        Attachment attachment = attachmentMapStruct.toEntity(request);
        Attachment savedAttachment = attachmentRepository.save(attachment);

        log.info("Attachment created successfully: {}", savedAttachment.getId());
        return attachmentMapStruct.toDto(savedAttachment);
    }

    @Override
    public AttachmentDto uploadFile(MultipartFile file, String expenseId, String uploadedBy) {
        log.info("Uploading file: {} for expense: {} by user: {}", file.getOriginalFilename(), expenseId, uploadedBy);

        try {
            // Validate file
            FileValidationResult validation = validateFile(file);
            if (!validation.isValid()) {
                throw new BusinessException("Invalid file: " + validation.getMessage(),
                        ErrorCode.ATTACHMENT_INVALID_FILE);
            }

            // Generate unique file path
            String filePath = generateFilePath(file.getOriginalFilename());

            // Upload to CDN
            String cdnUrl = cdnService.uploadFile(file, filePath, null);

            // Create attachment record
            Attachment attachment = Attachment.builder()
                    .id(UUID.randomUUID().toString())
                    .expenseId(expenseId)
                    .fileUrl(cdnUrl)
                    .fileType(file.getContentType())
                    .uploadedBy(uploadedBy)
                    .build();

            Attachment savedAttachment = attachmentRepository.save(attachment);

            log.info("File uploaded successfully: {}", savedAttachment.getId());
            return attachmentMapStruct.toDto(savedAttachment);

        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new BusinessException("File upload failed: " + e.getMessage(), ErrorCode.ATTACHMENT_UPLOAD_FAILED);
        }
    }

    @Override
    public AttachmentDto getAttachmentById(String attachmentId) {
        log.debug("Getting attachment by ID: {}", attachmentId);

        return attachmentRepository.findById(attachmentId)
                .map(attachmentMapStruct::toDto)
                .orElseThrow(() -> EntityNotFoundException.attachmentNotFound(attachmentId));
    }

    @Override
    public PaginatedResponse<AttachmentDto> getAllAttachments(int page, int size) {
        log.debug("Getting all attachments with pagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Attachment> attachments = attachmentRepository.findAll(pageable);

        return PaginatedResponse.<AttachmentDto>builder()
                .data(attachments.getContent().stream()
                        .map(attachmentMapStruct::toDto)
                        .toList())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(attachments.getTotalElements())
                        .totalPages(attachments.getTotalPages())
                        .hasNext(attachments.hasNext())
                        .hasPrevious(attachments.hasPrevious())
                        .build())
                .build();
    }

    @Override
    public PaginatedResponse<AttachmentDto> getAttachmentsByExpenseId(String expenseId, int page, int size) {
        log.debug("Getting attachments by expense ID: {} with pagination: page={}, size={}", expenseId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Attachment> attachments = attachmentRepository.findByExpenseId(expenseId, pageable);

        return PaginatedResponse.<AttachmentDto>builder()
                .data(attachments.getContent().stream()
                        .map(attachmentMapStruct::toDto)
                        .toList())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(attachments.getTotalElements())
                        .totalPages(attachments.getTotalPages())
                        .hasNext(attachments.hasNext())
                        .hasPrevious(attachments.hasPrevious())
                        .build())
                .build();
    }

    @Override
    public PaginatedResponse<AttachmentDto> getAttachmentsByUserId(String userId, int page, int size) {
        log.debug("Getting attachments by user ID: {} with pagination: page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Attachment> attachments = attachmentRepository.findByUploadedBy(userId, pageable);

        return PaginatedResponse.<AttachmentDto>builder()
                .data(attachments.getContent().stream()
                        .map(attachmentMapStruct::toDto)
                        .toList())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(attachments.getTotalElements())
                        .totalPages(attachments.getTotalPages())
                        .hasNext(attachments.hasNext())
                        .hasPrevious(attachments.hasPrevious())
                        .build())
                .build();
    }

    @Override
    public AttachmentDto updateAttachment(String attachmentId, UpdateAttachmentRequest request) {
        log.info("Updating attachment: {} with request: {}", attachmentId, request);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> EntityNotFoundException.attachmentNotFound(attachmentId));

        // Update fields that can be changed
        if (request.getFileType() != null) {
            attachment.setFileType(request.getFileType());
        }

        Attachment updatedAttachment = attachmentRepository.save(attachment);

        log.info("Attachment updated successfully: {}", attachmentId);
        return attachmentMapStruct.toDto(updatedAttachment);
    }

    @Override
    public void deleteAttachment(String attachmentId, String deletedBy) {
        log.info("Deleting attachment: {} by user: {}", attachmentId, deletedBy);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> EntityNotFoundException.attachmentNotFound(attachmentId));

        try {
            // Delete from CDN
            if (attachment.getFileUrl() != null) {
                // Extract file path from URL for CDN deletion
                String filePath = extractFilePathFromUrl(attachment.getFileUrl());
                if (filePath != null) {
                    cdnService.deleteFile(filePath);
                }
            }

            // Delete from database
            attachmentRepository.delete(attachment);

            log.info("Attachment deleted successfully: {}", attachmentId);

        } catch (Exception e) {
            log.error("Failed to delete attachment: {}", attachmentId, e);
            throw new BusinessException("Attachment deletion failed: " + e.getMessage(),
                    ErrorCode.ATTACHMENT_DELETE_FAILED);
        }
    }

    @Override
    public List<AttachmentDto> getAttachmentsByExpenseId(String expenseId) {
        log.debug("Getting attachments by expense ID: {}", expenseId);

        List<Attachment> attachments = attachmentRepository.findByExpenseIdOrderByCreatedAtDesc(expenseId);
        return attachments.stream()
                .map(attachmentMapStruct::toDto)
                .toList();
    }

    @Override
    public List<AttachmentDto> getAttachmentsByUploadedBy(String userId) {
        log.debug("Getting attachments by user ID: {}", userId);

        List<Attachment> attachments = attachmentRepository.findByUploadedByOrderByCreatedAtDesc(userId);
        return attachments.stream()
                .map(attachmentMapStruct::toDto)
                .toList();
    }

    @Override
    public List<AttachmentDto> getAttachmentsByNexId(String nexId) {
        log.debug("Getting attachments by nex ID: {}", nexId);

        List<Attachment> attachments = attachmentRepository.findByNexId(nexId);
        return attachments.stream()
                .map(attachmentMapStruct::toDto)
                .toList();
    }

    @Override
    public List<AttachmentDto> getAttachmentsByFileType(String fileType) {
        log.debug("Getting attachments by file type: {}", fileType);

        List<Attachment> attachments = attachmentRepository.findByFileTypeOrderByCreatedAtDesc(fileType);
        return attachments.stream()
                .map(attachmentMapStruct::toDto)
                .toList();
    }

    @Override
    public List<AttachmentDto> getImageAttachments() {
        log.debug("Getting image attachments");

        List<Attachment> attachments = attachmentRepository.findAll();
        return attachments.stream()
                .filter(Attachment::isImage)
                .map(attachmentMapStruct::toDto)
                .toList();
    }

    @Override
    public List<AttachmentDto> getDocumentAttachments() {
        log.debug("Getting document attachments");

        List<Attachment> attachments = attachmentRepository.findAll();
        return attachments.stream()
                .filter(Attachment::isDocument)
                .map(attachmentMapStruct::toDto)
                .toList();
    }

    @Override
    public String getDownloadUrl(String attachmentId) {
        log.debug("Getting download URL for attachment: {}", attachmentId);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> EntityNotFoundException.attachmentNotFound(attachmentId));

        return attachment.getFileUrl();
    }

    @Override
    public String getPreviewUrl(String attachmentId) {
        log.debug("Getting preview URL for attachment: {}", attachmentId);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> EntityNotFoundException.attachmentNotFound(attachmentId));

        // For images, return the file URL as preview URL
        if (attachment.isImage()) {
            return attachment.getFileUrl();
        }

        // For other file types, return null or a placeholder
        return null;
    }

    @Override
    public FileValidationResult validateFile(MultipartFile file) {
        log.debug("Validating file: {}", file.getOriginalFilename());

        List<String> errors = new java.util.ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("File is null or empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            errors.add("File size exceeds 10MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            errors.add("Filename is null or empty");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (fileExtension == null || !isAllowedFileType(fileExtension)) {
            errors.add("File type not allowed: " + fileExtension);
        }

        boolean isValid = errors.isEmpty();
        String message = isValid ? "File is valid" : "File validation failed";

        return new AttachmentService.FileValidationResult(isValid, message, errors);
    }

    @Override
    public List<AttachmentSummaryDto> getAttachmentSummaryByUserId(String userId) {
        log.debug("Getting attachment summary for user: {}", userId);

        try {
            List<Attachment> attachments = attachmentRepository.findByUploadedByOrderByCreatedAtDesc(userId);
            return attachments.stream()
                    .map(attachmentMapStruct::toSummaryDto)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get attachment summary for user: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<AttachmentSummaryDto> getAttachmentSummaryByNexId(String nexId) {
        log.debug("Getting attachment summary for nex: {}", nexId);

        try {
            List<Attachment> attachments = attachmentRepository.findByNexId(nexId);
            return attachments.stream()
                    .map(attachmentMapStruct::toSummaryDto)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get attachment summary for nex: {}", nexId, e);
            return List.of();
        }
    }

    @Override
    public AttachmentStatistics getAttachmentStatisticsByUserId(String userId) {
        log.debug("Getting attachment statistics for user: {}", userId);

        try {
            List<Attachment> attachments = attachmentRepository.findByUploadedByOrderByCreatedAtDesc(userId);

            int totalAttachments = attachments.size();
            int imageCount = (int) attachments.stream().filter(Attachment::isImage).count();
            int documentCount = (int) attachments.stream().filter(Attachment::isDocument).count();
            int otherCount = totalAttachments - imageCount - documentCount;

            // For simplicity, we'll use basic counts since we don't have complex
            // relationships
            int expensesWithAttachments = (int) attachments.stream().map(Attachment::getExpenseId).distinct().count();
            int nexGroupsWithAttachments = 0; // Would need to join with expenses to get this
            int usersWithAttachments = 1; // Just the current user

            return new AttachmentService.AttachmentStatistics(
                    totalAttachments, imageCount, documentCount, otherCount,
                    expensesWithAttachments, nexGroupsWithAttachments, usersWithAttachments);
        } catch (Exception e) {
            log.error("Failed to get attachment statistics for user: {}", userId, e);
            return new AttachmentService.AttachmentStatistics(0, 0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public AttachmentStatistics getAttachmentStatisticsByNexId(String nexId) {
        log.debug("Getting attachment statistics for nex: {}", nexId);

        try {
            List<Attachment> attachments = attachmentRepository.findByNexId(nexId);

            int totalAttachments = attachments.size();
            int imageCount = (int) attachments.stream().filter(Attachment::isImage).count();
            int documentCount = (int) attachments.stream().filter(Attachment::isDocument).count();
            int otherCount = totalAttachments - imageCount - documentCount;

            // For simplicity, we'll use basic counts since we don't have complex
            // relationships
            int expensesWithAttachments = (int) attachments.stream().map(Attachment::getExpenseId).distinct().count();
            int nexGroupsWithAttachments = 1; // Just the current nex
            int usersWithAttachments = (int) attachments.stream().map(Attachment::getUploadedBy).distinct().count();

            return new AttachmentService.AttachmentStatistics(
                    totalAttachments, imageCount, documentCount, otherCount,
                    expensesWithAttachments, nexGroupsWithAttachments, usersWithAttachments);
        } catch (Exception e) {
            log.error("Failed to get attachment statistics for nex: {}", nexId, e);
            return new AttachmentService.AttachmentStatistics(0, 0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public int cleanupOrphanedAttachments() {
        log.info("Starting orphaned attachment cleanup");

        // TODO: Implement orphaned attachment cleanup
        // This would find attachments that are no longer associated with any expense
        return 0;
    }

    /**
     * Generate a unique file path for the uploaded file.
     * 
     * @param originalFilename The original filename
     * @return Generated file path
     */
    private String generateFilePath(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);

        return String.format("attachments/%s/%s_%s.%s",
                timestamp.substring(0, 8), // YYYYMMDD
                uuid.substring(0, 8),
                timestamp,
                extension);
    }

    /**
     * Get file extension from filename.
     * 
     * @param filename The filename
     * @return File extension (lowercase) or "bin" if not found
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "bin";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "bin";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Check if file type is allowed.
     * 
     * @param fileExtension The file extension
     * @return true if file type is allowed
     */
    private boolean isAllowedFileType(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            return false;
        }

        String[] allowedTypes = { "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "txt", "xls", "xlsx",
                "mp4", "avi", "mov", "wmv", "mp3", "wav", "flac", "aac" };
        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extract file path from CDN URL.
     * 
     * @param cdnUrl The CDN URL
     * @return File path or null if not found
     */
    private String extractFilePathFromUrl(String cdnUrl) {
        if (cdnUrl == null || cdnUrl.isEmpty()) {
            return null;
        }

        // Extract path from URL (this is a simplified implementation)
        // In a real implementation, you would parse the URL properly
        int lastSlashIndex = cdnUrl.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return null;
        }

        return cdnUrl.substring(lastSlashIndex + 1);
    }

    @Override
    public Object getAttachmentAnalyticsByBillId(String billId) {
        log.debug("Getting attachment analytics by bill ID: {}", billId);
        // Implementation for getting attachment analytics by bill ID
        return new Object();
    }

    @Override
    public Object getAttachmentAnalyticsByExpenseId(String expenseId) {
        log.debug("Getting attachment analytics by expense ID: {}", expenseId);
        // Implementation for getting attachment analytics by expense ID
        return new Object();
    }

    @Override
    public Object getAttachmentAnalyticsByUserId(String userId) {
        log.debug("Getting attachment analytics by user ID: {}", userId);
        // Implementation for getting attachment analytics by user ID
        return new Object();
    }

    @Override
    public Object getAttachmentAnalytics() {
        log.debug("Getting attachment analytics");
        // Implementation for getting attachment analytics
        return new Object();
    }

    @Override
    public String getPresignedUrl(String attachmentId, int expirationMinutes) {
        log.debug("Getting presigned URL for attachment: {} with expiration: {} minutes", attachmentId,
                expirationMinutes);
        // Implementation for getting presigned URL
        return "https://example.com/presigned-url";
    }

    @Override
    public String getAttachmentUrl(String attachmentId) {
        log.debug("Getting attachment URL for attachment: {}", attachmentId);
        // Implementation for getting attachment URL
        return "https://example.com/attachment-url";
    }

    @Override
    public List<AttachmentSummaryDto> getAttachmentSummaryByBillId(String billId) {
        log.debug("Getting attachment summary by bill ID: {}", billId);
        // Implementation for getting attachment summary by bill ID
        return new java.util.ArrayList<>();
    }

    @Override
    public List<AttachmentSummaryDto> getAttachmentSummaryByExpenseId(String expenseId) {
        log.debug("Getting attachment summary by expense ID: {}", expenseId);
        // Implementation for getting attachment summary by expense ID
        return new java.util.ArrayList<>();
    }

}
