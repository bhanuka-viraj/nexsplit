package com.nexsplit.repository;

import com.nexsplit.model.BillParticipant;
import com.nexsplit.model.BillParticipantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BillParticipant entity operations.
 *
 * This repository provides data access methods for bill participant management
 * including
 * CRUD operations, filtering by various criteria, and pagination support.
 *
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface BillParticipantRepository extends JpaRepository<BillParticipant, BillParticipantId> {

    /**
     * Find all bill participants that are not deleted.
     *
     * @return List of non-deleted bill participants
     */
    List<BillParticipant> findByIsDeletedFalse();

    /**
     * Find all bill participants that are not deleted with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of non-deleted bill participants
     */
    Page<BillParticipant> findByIsDeletedFalse(Pageable pageable);

    /**
     * Find bill participants by bill ID that are not deleted.
     *
     * @param billId The bill ID
     * @return List of participants for the specified bill
     */
    List<BillParticipant> findByBillIdAndIsDeletedFalse(String billId);

    /**
     * Find bill participants by bill ID that are not deleted with pagination.
     *
     * @param billId   The bill ID
     * @param pageable Pagination parameters
     * @return Page of participants for the specified bill
     */
    Page<BillParticipant> findByBillIdAndIsDeletedFalse(String billId, Pageable pageable);

    /**
     * Find bill participants by user ID that are not deleted.
     *
     * @param userId The user ID
     * @return List of participants for the specified user
     */
    List<BillParticipant> findByUserIdAndIsDeletedFalse(String userId);

    /**
     * Find bill participants by user ID that are not deleted with pagination.
     *
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Page of participants for the specified user
     */
    Page<BillParticipant> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    /**
     * Find a specific bill participant by bill ID and user ID.
     *
     * @param billId The bill ID
     * @param userId The user ID
     * @return Optional containing the bill participant if found
     */
    Optional<BillParticipant> findByBillIdAndUserId(String billId, String userId);

    /**
     * Find bill participants by payment status that are not deleted.
     *
     * @param paid The payment status
     * @return List of participants with the specified payment status
     */
    List<BillParticipant> findByPaidAndIsDeletedFalse(boolean paid);

    /**
     * Find bill participants by bill ID and payment status that are not deleted.
     *
     * @param billId The bill ID
     * @param paid   The payment status
     * @return List of participants for the specified bill with the specified
     *         payment status
     */
    List<BillParticipant> findByBillIdAndPaidAndIsDeletedFalse(String billId, boolean paid);

    /**
     * Find bill participants by user ID and payment status that are not deleted.
     *
     * @param userId The user ID
     * @param paid   The payment status
     * @return List of participants for the specified user with the specified
     *         payment status
     */
    List<BillParticipant> findByUserIdAndPaidAndIsDeletedFalse(String userId, boolean paid);

    /**
     * Count bill participants by bill ID that are not deleted.
     *
     * @param billId The bill ID
     * @return Count of participants for the specified bill
     */
    long countByBillIdAndIsDeletedFalse(String billId);

    /**
     * Count bill participants by user ID that are not deleted.
     *
     * @param userId The user ID
     * @return Count of participants for the specified user
     */
    long countByUserIdAndIsDeletedFalse(String userId);

    /**
     * Count bill participants by bill ID and payment status that are not deleted.
     *
     * @param billId The bill ID
     * @param paid   The payment status
     * @return Count of participants for the specified bill with the specified
     *         payment status
     */
    long countByBillIdAndPaidAndIsDeletedFalse(String billId, boolean paid);

    /**
     * Count bill participants by user ID and payment status that are not deleted.
     *
     * @param userId The user ID
     * @param paid   The payment status
     * @return Count of participants for the specified user with the specified
     *         payment status
     */
    long countByUserIdAndPaidAndIsDeletedFalse(String userId, boolean paid);

    /**
     * Find bill participants created between specified dates that are not deleted.
     *
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of participants created between the specified dates
     */
    List<BillParticipant> findByCreatedAtBetweenAndIsDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find bill participants paid between specified dates that are not deleted.
     *
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of participants paid between the specified dates
     */
    List<BillParticipant> findByPaidAtBetweenAndIsDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find bill participants by share amount range that are not deleted.
     *
     * @param minAmount The minimum share amount
     * @param maxAmount The maximum share amount
     * @return List of participants within the specified share amount range
     */
    @Query("SELECT bp FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.shareAmount BETWEEN :minAmount AND :maxAmount")
    List<BillParticipant> findByShareAmountBetweenAndIsDeletedFalse(@Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount);

    /**
     * Find bill participants by bill ID and share amount range that are not
     * deleted.
     *
     * @param billId    The bill ID
     * @param minAmount The minimum share amount
     * @param maxAmount The maximum share amount
     * @return List of participants for the specified bill within the specified
     *         share amount range
     */
    @Query("SELECT bp FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.billId = :billId AND bp.shareAmount BETWEEN :minAmount AND :maxAmount")
    List<BillParticipant> findByBillIdAndShareAmountBetweenAndIsDeletedFalse(@Param("billId") String billId,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount);

    /**
     * Find bill participants by user ID and share amount range that are not
     * deleted.
     *
     * @param userId    The user ID
     * @param minAmount The minimum share amount
     * @param maxAmount The maximum share amount
     * @return List of participants for the specified user within the specified
     *         share amount range
     */
    @Query("SELECT bp FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.userId = :userId AND bp.shareAmount BETWEEN :minAmount AND :maxAmount")
    List<BillParticipant> findByUserIdAndShareAmountBetweenAndIsDeletedFalse(@Param("userId") String userId,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount);

    /**
     * Find overdue bill participants that are not deleted.
     *
     * @return List of overdue bill participants
     */
    @Query("SELECT bp FROM BillParticipant bp JOIN bp.bill b WHERE bp.isDeleted = false AND bp.paid = false AND b.dueDate < CURRENT_TIMESTAMP")
    List<BillParticipant> findOverdueParticipants();

    /**
     * Find bill participants that need payment reminders.
     *
     * @param reminderDate The date to check for reminders
     * @return List of participants that need payment reminders
     */
    @Query("SELECT bp FROM BillParticipant bp JOIN bp.bill b WHERE bp.isDeleted = false AND bp.paid = false AND b.dueDate <= :reminderDate")
    List<BillParticipant> findParticipantsNeedingReminders(@Param("reminderDate") LocalDateTime reminderDate);

    /**
     * Calculate total share amount for a bill.
     *
     * @param billId The bill ID
     * @return Total share amount for the specified bill
     */
    @Query("SELECT COALESCE(SUM(bp.shareAmount), 0) FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.billId = :billId")
    java.math.BigDecimal calculateTotalShareAmountByBillId(@Param("billId") String billId);

    /**
     * Calculate total paid amount for a bill.
     *
     * @param billId The bill ID
     * @return Total paid amount for the specified bill
     */
    @Query("SELECT COALESCE(SUM(bp.shareAmount), 0) FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.billId = :billId AND bp.paid = true")
    java.math.BigDecimal calculateTotalPaidAmountByBillId(@Param("billId") String billId);

    /**
     * Calculate total unpaid amount for a bill.
     *
     * @param billId The bill ID
     * @return Total unpaid amount for the specified bill
     */
    @Query("SELECT COALESCE(SUM(bp.shareAmount), 0) FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.billId = :billId AND bp.paid = false")
    java.math.BigDecimal calculateTotalUnpaidAmountByBillId(@Param("billId") String billId);

    /**
     * Calculate total amount owed by a user across all bills.
     *
     * @param userId The user ID
     * @return Total amount owed by the specified user
     */
    @Query("SELECT COALESCE(SUM(bp.shareAmount), 0) FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.userId = :userId AND bp.paid = false")
    java.math.BigDecimal calculateTotalOwedAmountByUserId(@Param("userId") String userId);

    /**
     * Calculate total amount paid by a user across all bills.
     *
     * @param userId The user ID
     * @return Total amount paid by the specified user
     */
    @Query("SELECT COALESCE(SUM(bp.shareAmount), 0) FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.userId = :userId AND bp.paid = true")
    java.math.BigDecimal calculateTotalPaidAmountByUserId(@Param("userId") String userId);

    /**
     * Find bill participants by multiple user IDs that are not deleted.
     *
     * @param userIds List of user IDs
     * @return List of participants for the specified users
     */
    @Query("SELECT bp FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.userId IN :userIds")
    List<BillParticipant> findByUserIdInAndIsDeletedFalse(@Param("userIds") List<String> userIds);

    /**
     * Find bill participants by multiple bill IDs that are not deleted.
     *
     * @param billIds List of bill IDs
     * @return List of participants for the specified bills
     */
    @Query("SELECT bp FROM BillParticipant bp WHERE bp.isDeleted = false AND bp.id.billId IN :billIds")
    List<BillParticipant> findByBillIdInAndIsDeletedFalse(@Param("billIds") List<String> billIds);

    /**
     * Delete all participants for a specific bill (soft delete).
     *
     * @param billId    The bill ID
     * @param deletedBy The user ID who deleted the participants
     * @param deletedAt The deletion timestamp
     */
    @Query("UPDATE BillParticipant bp SET bp.isDeleted = true, bp.deletedBy = :deletedBy, bp.deletedAt = :deletedAt WHERE bp.id.billId = :billId AND bp.isDeleted = false")
    void softDeleteByBillId(@Param("billId") String billId, @Param("deletedBy") String deletedBy,
            @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * Delete all participants for a specific user (soft delete).
     *
     * @param userId    The user ID
     * @param deletedBy The user ID who deleted the participants
     * @param deletedAt The deletion timestamp
     */
    @Query("UPDATE BillParticipant bp SET bp.isDeleted = true, bp.deletedBy = :deletedBy, bp.deletedAt = :deletedAt WHERE bp.id.userId = :userId AND bp.isDeleted = false")
    void softDeleteByUserId(@Param("userId") String userId, @Param("deletedBy") String deletedBy,
            @Param("deletedAt") LocalDateTime deletedAt);
}
