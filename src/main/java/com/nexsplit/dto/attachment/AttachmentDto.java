package com.nexsplit.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for attachment data.
 * Contains complete attachment information.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {

    private String id;
    private String expenseId;
    private String expenseTitle;
    private String fileUrl;
    private String fileType;
    private String fileExtension;
    private String uploadedBy;
    private String uploaderName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Boolean isImage;
    private Boolean isDocument;
}
