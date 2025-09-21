package com.nexsplit.repository;

import com.nexsplit.model.view.SettlementHistoryView;
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
 * Repository for settlement operations using database views.
 * 
 * This repository provides optimized queries for settlement tracking and
 * analytics using the settlement_history_view as the primary data access
 * method.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface SettlementRepository extends JpaRepository<SettlementHistoryView, String> {

    /**
     * Find all settlements for a specific user (as debtor or creditor).
     * 
     * @param userId The user ID
     * @return List of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE debtor_id = :userId OR creditor_id = :userId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findByUserId(@Param("userId") String userId);

    /**
     * Find all settlements for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE nex_id = :nexId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findByNexId(@Param("nexId") String nexId);

    /**
     * Find all unsettled debts for a specific user.
     * 
     * @param userId The user ID
     * @return List of unsettled settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE (debtor_id = :userId OR creditor_id = :userId)
            AND is_settled = false
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findUnsettledByUserId(@Param("userId") String userId);

    /**
     * Find all unsettled debts for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of unsettled settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE nex_id = :nexId AND is_settled = false
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findUnsettledByNexId(@Param("nexId") String nexId);

    /**
     * Find settlements between two specific users.
     * 
     * @param userId1 The first user ID
     * @param userId2 The second user ID
     * @return List of settlement history records between the users
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE (debtor_id = :userId1 AND creditor_id = :userId2)
            OR (debtor_id = :userId2 AND creditor_id = :userId1)
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findBetweenUsers(@Param("userId1") String userId1,
            @Param("userId2") String userId2);

    /**
     * Find settlements within a date range.
     * 
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of settlement history records within the date range
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE debt_created_at BETWEEN :startDate AND :endDate
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find settlements by payment method.
     * 
     * @param paymentMethod The payment method
     * @return List of settlement history records with the specified payment method
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE payment_method = :paymentMethod
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findByPaymentMethod(@Param("paymentMethod") String paymentMethod);

    /**
     * Find settlements by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of settlement history records for the expense
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE expense_id = :expenseId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    List<SettlementHistoryView> findByExpenseId(@Param("expenseId") String expenseId);

    /**
     * Get settlement statistics for a user.
     * 
     * @param userId The user ID
     * @return Settlement statistics
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_settlements,
                COUNT(CASE WHEN is_settled = true THEN 1 END) as settled_count,
                COUNT(CASE WHEN is_settled = false THEN 1 END) as unsettled_count,
                SUM(CASE WHEN debtor_id = :userId AND is_settled = false THEN amount ELSE 0 END) as total_debt,
                SUM(CASE WHEN creditor_id = :userId AND is_settled = false THEN amount ELSE 0 END) as total_credit,
                AVG(CASE WHEN is_settled = true THEN settlement_hours END) as avg_settlement_time_hours
            FROM settlement_history_view
            WHERE debtor_id = :userId OR creditor_id = :userId
            """, nativeQuery = true)
    Optional<Object[]> getSettlementStatistics(@Param("userId") String userId);

    /**
     * Get settlement statistics for a nex group.
     * 
     * @param nexId The nex ID
     * @return Settlement statistics
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_settlements,
                COUNT(CASE WHEN is_settled = true THEN 1 END) as settled_count,
                COUNT(CASE WHEN is_settled = false THEN 1 END) as unsettled_count,
                SUM(CASE WHEN is_settled = false THEN amount ELSE 0 END) as total_unsettled_amount,
                AVG(CASE WHEN is_settled = true THEN settlement_hours END) as avg_settlement_time_hours
            FROM settlement_history_view
            WHERE nex_id = :nexId
            """, nativeQuery = true)
    Optional<Object[]> getSettlementStatisticsByNex(@Param("nexId") String nexId);

    /**
     * Find all settlements for a specific user with pagination.
     * 
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE debtor_id = :userId OR creditor_id = :userId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    Page<SettlementHistoryView> findByUserIdPaginated(@Param("userId") String userId, Pageable pageable);

    /**
     * Find all settlements for a specific nex with pagination.
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE nex_id = :nexId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    Page<SettlementHistoryView> findByNexIdPaginated(@Param("nexId") String nexId, Pageable pageable);

    /**
     * Find all settlements with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    Page<SettlementHistoryView> findAllPaginated(Pageable pageable);

    /**
     * Find all settlements for a specific user with pagination.
     * 
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE debtor_id = :userId OR creditor_id = :userId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    Page<SettlementHistoryView> findByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * Find all settlements for a specific nex with pagination.
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    @Query(value = """
            SELECT * FROM settlement_history_view
            WHERE nex_id = :nexId
            ORDER BY debt_created_at DESC
            """, nativeQuery = true)
    Page<SettlementHistoryView> findByNexId(@Param("nexId") String nexId, Pageable pageable);
}
