package com.nexsplit.repository;

import com.nexsplit.model.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Attachment entity.
 * 
 * This repository provides methods for querying attachments including
 * expense-specific, bill-specific, and user-specific attachments.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    /**
     * Find attachments by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of attachments
     */
    List<Attachment> findByExpenseIdOrderByCreatedAtDesc(String expenseId);

    /**
     * Find attachments by uploaded by user ID.
     * 
     * @param uploadedBy The user ID who uploaded the attachment
     * @return List of attachments
     */
    List<Attachment> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);

    /**
     * Find attachments by file type.
     * 
     * @param fileType The file type
     * @return List of attachments
     */
    List<Attachment> findByFileTypeOrderByCreatedAtDesc(String fileType);

    /**
     * Find attachments by file type.
     * 
     * @param fileType The file type
     * @param pageable Pagination parameters
     * @return Page of attachments
     */
    Page<Attachment> findByFileType(String fileType, Pageable pageable);

    /**
     * Find attachments by file extension.
     * 
     * @param extension The file extension
     * @param pageable  Pagination parameters
     * @return Page of attachments
     */
    @Query("SELECT a FROM Attachment a WHERE LOWER(a.fileUrl) LIKE LOWER(CONCAT('%.', :extension)) ORDER BY a.createdAt DESC")
    Page<Attachment> findByFileExtension(@Param("extension") String extension, Pageable pageable);

    /**
     * Count attachments by expense ID.
     * 
     * @param expenseId The expense ID
     * @return Count of attachments
     */
    long countByExpenseId(String expenseId);

    /**
     * Count attachments by user ID.
     * 
     * @param userId The user ID
     * @return Count of attachments
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.expenseId IN (SELECT e.id FROM Expense e WHERE e.payerId = :userId)")
    long countByUserId(@Param("userId") String userId);

    /**
     * Count attachments by file type.
     * 
     * @param fileType The file type
     * @return Count of attachments
     */
    long countByFileType(String fileType);

    /**
     * Delete attachments by expense ID.
     * 
     * @param expenseId The expense ID
     */
    void deleteByExpenseId(String expenseId);

    /**
     * Find attachments by nex ID (through expense relationship).
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination parameters
     * @return Page of attachments
     */
    @Query("SELECT a FROM Attachment a WHERE a.expenseId IN (SELECT e.id FROM Expense e WHERE e.nexId = :nexId) ORDER BY a.createdAt DESC")
    Page<Attachment> findByNexId(@Param("nexId") String nexId, Pageable pageable);

    /**
     * Find attachments by nex ID (through expense relationship).
     * 
     * @param nexId The nex ID
     * @return List of attachments
     */
    @Query("SELECT a FROM Attachment a WHERE a.expenseId IN (SELECT e.id FROM Expense e WHERE e.nexId = :nexId) ORDER BY a.createdAt DESC")
    List<Attachment> findByNexId(@Param("nexId") String nexId);

    /**
     * Find attachments by expense ID with pagination.
     * 
     * @param expenseId The expense ID
     * @param pageable  Pagination parameters
     * @return Page of attachments
     */
    Page<Attachment> findByExpenseId(String expenseId, Pageable pageable);

    /**
     * Find attachments by uploaded by user ID with pagination.
     * 
     * @param uploadedBy The user ID who uploaded the attachment
     * @param pageable   Pagination parameters
     * @return Page of attachments
     */
    Page<Attachment> findByUploadedBy(String uploadedBy, Pageable pageable);

    /**
     * Count attachments by nex ID.
     * 
     * @param nexId The nex ID
     * @return Count of attachments
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.expenseId IN (SELECT e.id FROM Expense e WHERE e.nexId = :nexId)")
    long countByNexId(@Param("nexId") String nexId);
}
