package com.nexsplit.controller;

import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.attachment.AttachmentDto;
import com.nexsplit.dto.attachment.AttachmentSummaryDto;
import com.nexsplit.dto.attachment.CreateAttachmentRequest;
import com.nexsplit.dto.attachment.UpdateAttachmentRequest;
import com.nexsplit.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nexsplit.dto.PaginatedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for attachment management operations.
 * 
 * This controller provides endpoints for managing attachments including
 * file upload, download, deletion, and metadata operations. It follows
 * RESTful patterns and uses the ApiResponse wrapper for consistent
 * response formatting.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attachment Management", description = "APIs for managing file attachments and CDN operations")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    @Operation(summary = "Create a new attachment", description = "Create a new attachment record", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AttachmentDto>> createAttachment(
            @Valid @RequestBody CreateAttachmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating new attachment: {}", request);

        AttachmentDto attachment = attachmentService.createAttachment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attachment, "Attachment created successfully"));
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Upload a file and create attachment record", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AttachmentDto>> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Expense ID") @RequestParam(required = false) String expenseId,
            @Parameter(description = "Bill ID") @RequestParam(required = false) String billId,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Uploading file: {} for expense: {} or bill: {}", file.getOriginalFilename(), expenseId, billId);

        AttachmentDto attachment = attachmentService.uploadFile(file, expenseId, "system");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attachment, "File uploaded successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an attachment", description = "Update an existing attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AttachmentDto>> updateAttachment(
            @Parameter(description = "Attachment ID") @PathVariable String id,
            @Valid @RequestBody UpdateAttachmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating attachment: {} with request: {}", id, request);

        AttachmentDto attachment = attachmentService.updateAttachment(id, request);

        return ResponseEntity.ok(ApiResponse.success(attachment, "Attachment updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attachment", description = "Delete an attachment by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @Parameter(description = "Attachment ID") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting attachment: {}", id);

        attachmentService.deleteAttachment(id, "system");

        return ResponseEntity.ok(ApiResponse.success(null, "Attachment deleted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attachment by ID", description = "Retrieve an attachment by its ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AttachmentDto>> getAttachmentById(
            @Parameter(description = "Attachment ID") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Getting attachment by ID: {}", id);

        AttachmentDto attachment = attachmentService.getAttachmentById(id);

        return ResponseEntity.ok(ApiResponse.success(attachment, "Attachment retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all attachments", description = "Retrieve all attachments with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PaginatedResponse<AttachmentDto>>> getAllAttachments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Getting all attachments with pagination: page={}, size={}", page, size);

        PaginatedResponse<AttachmentDto> attachments = attachmentService.getAllAttachments(page, size);

        return ResponseEntity.ok(ApiResponse.success(attachments, "Attachments retrieved successfully"));
    }

    @GetMapping("/expense/{expenseId}")
    @Operation(summary = "Get attachments by expense ID", description = "Retrieve attachments for a specific expense", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PaginatedResponse<AttachmentDto>>> getAttachmentsByExpenseId(
            @Parameter(description = "Expense ID") @PathVariable String expenseId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Getting attachments by expense ID: {} with pagination: page={}, size={}", expenseId, page, size);

        PaginatedResponse<AttachmentDto> attachments = attachmentService.getAttachmentsByExpenseId(expenseId, page,
                size);

        return ResponseEntity.ok(ApiResponse.success(attachments, "Expense attachments retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get attachments by user ID", description = "Retrieve attachments for a specific user")
    public ResponseEntity<ApiResponse<PaginatedResponse<AttachmentDto>>> getAttachmentsByUserId(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.debug("Getting attachments by user ID: {} with pagination: page={}, size={}", userId, page, size);

        PaginatedResponse<AttachmentDto> attachments = attachmentService.getAttachmentsByUserId(userId, page, size);

        return ResponseEntity.ok(ApiResponse.success(attachments, "User attachments retrieved successfully"));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get attachment summary for user", description = "Get a summary of attachments for a specific user")
    public ResponseEntity<ApiResponse<List<AttachmentSummaryDto>>> getAttachmentSummaryByUserId(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.debug("Getting attachment summary for user: {}", userId);

        List<AttachmentSummaryDto> summary = attachmentService.getAttachmentSummaryByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Attachment summary retrieved successfully"));
    }

    @GetMapping("/expense/{expenseId}/summary")
    @Operation(summary = "Get attachment summary for expense", description = "Get a summary of attachments for a specific expense")
    public ResponseEntity<ApiResponse<List<AttachmentSummaryDto>>> getAttachmentSummaryByExpenseId(
            @Parameter(description = "Expense ID") @PathVariable String expenseId) {
        log.debug("Getting attachment summary for expense: {}", expenseId);

        List<AttachmentSummaryDto> summary = attachmentService.getAttachmentSummaryByExpenseId(expenseId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Expense attachment summary retrieved successfully"));
    }

    @GetMapping("/bill/{billId}/summary")
    @Operation(summary = "Get attachment summary for bill", description = "Get a summary of attachments for a specific bill")
    public ResponseEntity<ApiResponse<List<AttachmentSummaryDto>>> getAttachmentSummaryByBillId(
            @Parameter(description = "Bill ID") @PathVariable String billId) {
        log.debug("Getting attachment summary for bill: {}", billId);

        List<AttachmentSummaryDto> summary = attachmentService.getAttachmentSummaryByBillId(billId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Bill attachment summary retrieved successfully"));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download attachment", description = "Download an attachment file", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<byte[]> downloadAttachment(
            @Parameter(description = "Attachment ID") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Downloading attachment: {}", id);

        try {
            // Get attachment details (verify it exists)
            attachmentService.getAttachmentById(id);

            // TODO: Implement file download logic in service
            // For now, return a placeholder response
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"attachment\"")
                    .header("Content-Type", "application/octet-stream")
                    .body(new byte[0]);
        } catch (Exception e) {
            log.error("Failed to download attachment: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/url")
    @Operation(summary = "Get attachment URL", description = "Get the CDN URL for an attachment")
    public ResponseEntity<ApiResponse<String>> getAttachmentUrl(
            @Parameter(description = "Attachment ID") @PathVariable String id) {
        log.debug("Getting URL for attachment: {}", id);

        String url = attachmentService.getAttachmentUrl(id);

        return ResponseEntity.ok(ApiResponse.success(url, "Attachment URL retrieved successfully"));
    }

    @GetMapping("/{id}/presigned-url")
    @Operation(summary = "Get presigned URL", description = "Get a presigned URL for secure access to an attachment")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(
            @Parameter(description = "Attachment ID") @PathVariable String id,
            @Parameter(description = "Expiration minutes") @RequestParam(defaultValue = "60") int expirationMinutes) {
        log.debug("Getting presigned URL for attachment: {} with expiration: {} minutes", id, expirationMinutes);

        String presignedUrl = attachmentService.getPresignedUrl(id, expirationMinutes);

        return ResponseEntity.ok(ApiResponse.success(presignedUrl, "Presigned URL generated successfully"));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get attachment analytics", description = "Get analytics and statistics for attachments")
    public ResponseEntity<ApiResponse<Object>> getAttachmentAnalytics() {
        log.debug("Getting attachment analytics");

        Object analytics = attachmentService.getAttachmentAnalytics();

        return ResponseEntity.ok(ApiResponse.success(analytics, "Attachment analytics retrieved successfully"));
    }

    @GetMapping("/analytics/user/{userId}")
    @Operation(summary = "Get attachment analytics by user", description = "Get analytics and statistics for attachments by user")
    public ResponseEntity<ApiResponse<Object>> getAttachmentAnalyticsByUserId(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.debug("Getting attachment analytics for user: {}", userId);

        Object analytics = attachmentService.getAttachmentAnalyticsByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(analytics, "User attachment analytics retrieved successfully"));
    }

    @GetMapping("/analytics/expense/{expenseId}")
    @Operation(summary = "Get attachment analytics by expense", description = "Get analytics and statistics for attachments by expense")
    public ResponseEntity<ApiResponse<Object>> getAttachmentAnalyticsByExpenseId(
            @Parameter(description = "Expense ID") @PathVariable String expenseId) {
        log.debug("Getting attachment analytics for expense: {}", expenseId);

        Object analytics = attachmentService.getAttachmentAnalyticsByExpenseId(expenseId);

        return ResponseEntity.ok(ApiResponse.success(analytics, "Expense attachment analytics retrieved successfully"));
    }

    @GetMapping("/analytics/bill/{billId}")
    @Operation(summary = "Get attachment analytics by bill", description = "Get analytics and statistics for attachments by bill")
    public ResponseEntity<ApiResponse<Object>> getAttachmentAnalyticsByBillId(
            @Parameter(description = "Bill ID") @PathVariable String billId) {
        log.debug("Getting attachment analytics for bill: {}", billId);

        Object analytics = attachmentService.getAttachmentAnalyticsByBillId(billId);

        return ResponseEntity.ok(ApiResponse.success(analytics, "Bill attachment analytics retrieved successfully"));
    }
}
