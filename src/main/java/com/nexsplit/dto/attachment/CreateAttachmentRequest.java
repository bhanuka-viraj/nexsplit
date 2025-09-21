package com.nexsplit.dto.attachment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new attachment.
 * Contains all necessary information to create an attachment.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttachmentRequest {

    @NotBlank(message = "Expense ID is required")
    private String expenseId;

    @NotBlank(message = "File URL is required")
    @Size(max = 500, message = "File URL must not exceed 500 characters")
    private String fileUrl;

    @Size(max = 50, message = "File type must not exceed 50 characters")
    private String fileType;

    @NotBlank(message = "Uploaded by is required")
    private String uploadedBy;
}
