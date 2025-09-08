package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", insertable = false, updatable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploader;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
