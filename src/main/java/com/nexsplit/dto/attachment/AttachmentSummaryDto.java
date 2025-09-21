package com.nexsplit.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary DTO for attachment information.
 * Contains essential attachment data for list views and summaries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentSummaryDto {

    private String id;
    private String expenseId;
    private String expenseTitle;
    private String fileUrl;
    private String fileType;
    private String fileExtension;
    private String uploadedBy;
    private String uploaderName;
    private LocalDateTime createdAt;
    private Boolean isImage;
    private Boolean isDocument;
}
