package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Attachment entity representing file attachments for expenses.
 * 
 * This entity stores information about files attached to expenses,
 * including file URL, file type, and upload metadata. Attachments
 * are associated with expenses and uploaded by users.
 * 
 * Database table: attachments
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "attachments")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "expense_id", nullable = false, columnDefinition = "CHAR(36)")
    private String expenseId;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "uploaded_by", nullable = false, columnDefinition = "CHAR(36)")
    private String uploadedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", insertable = false, updatable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploader;

    /**
     * Get the file extension from the file URL.
     * 
     * @return the file extension or null if not found
     */
    public String getFileExtension() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        int lastDotIndex = fileUrl.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileUrl.length() - 1) {
            return null;
        }
        return fileUrl.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Check if the attachment is an image file.
     * 
     * @return true if the file is an image, false otherwise
     */
    public boolean isImage() {
        String extension = getFileExtension();
        return extension != null && (extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("gif") || extension.equals("webp"));
    }

    /**
     * Check if the attachment is a document file.
     * 
     * @return true if the file is a document, false otherwise
     */
    public boolean isDocument() {
        String extension = getFileExtension();
        return extension != null && (extension.equals("pdf") || extension.equals("doc") ||
                extension.equals("docx") || extension.equals("txt") || extension.equals("xls") ||
                extension.equals("xlsx"));
    }

    /**
     * Ensure default values are set before persisting
     */
    @PrePersist
    protected void onPrePersist() {
        ensureDefaultValues();
    }
}
