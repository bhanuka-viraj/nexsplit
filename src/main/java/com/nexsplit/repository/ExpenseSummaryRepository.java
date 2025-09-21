package com.nexsplit.repository;

import com.nexsplit.model.view.ExpenseSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for expense summary operations using database views.
 * 
 * This repository provides optimized queries for expense analytics and
 * reporting using the expense_summary_view as the primary data access method.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ExpenseSummaryRepository extends JpaRepository<ExpenseSummaryView, String> {

    /**
     * Find all expenses for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of expense summary records
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE nex_id = :nexId
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findByNexId(@Param("nexId") String nexId);

    /**
     * Find all expenses created by a specific user.
     * 
     * @param userId The user ID
     * @return List of expense summary records
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE created_by = :userId
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findByCreatedBy(@Param("userId") String userId);

    /**
     * Find all expenses paid by a specific user.
     * 
     * @param userId The user ID
     * @return List of expense summary records
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE payer_id = :userId
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findByPayerId(@Param("userId") String userId);

    /**
     * Find all expenses in a specific category.
     * 
     * @param categoryId The category ID
     * @return List of expense summary records
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE category_id = :categoryId
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Find expenses within a date range.
     * 
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of expense summary records within the date range
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE created_at BETWEEN :startDate AND :endDate
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find expenses within an amount range.
     * 
     * @param minAmount The minimum amount
     * @param maxAmount The maximum amount
     * @return List of expense summary records within the amount range
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE amount BETWEEN :minAmount AND :maxAmount
            ORDER BY amount DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Find fully settled expenses.
     * 
     * @return List of fully settled expense summary records
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE is_fully_settled = true
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findFullySettled();

    /**
     * Find partially settled expenses.
     * 
     * @return List of partially settled expense summary records
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE is_fully_settled = false
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findPartiallySettled();

    /**
     * Find expenses with attachments.
     * 
     * @return List of expense summary records with attachments
     */
    @Query(value = """
            SELECT * FROM expense_summary_view
            WHERE attachment_count > 0
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ExpenseSummaryView> findWithAttachments();

    /**
     * Get expense statistics for a nex group.
     * 
     * @param nexId The nex ID
     * @return Expense statistics
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_expenses,
                SUM(amount) as total_amount,
                AVG(amount) as average_amount,
                MAX(amount) as max_amount,
                MIN(amount) as min_amount,
                COUNT(CASE WHEN is_fully_settled = true THEN 1 END) as settled_count,
                COUNT(CASE WHEN is_fully_settled = false THEN 1 END) as unsettled_count,
                SUM(unsettled_amount) as total_unsettled_amount,
                COUNT(CASE WHEN attachment_count > 0 THEN 1 END) as expenses_with_attachments
            FROM expense_summary_view
            WHERE nex_id = :nexId
            """, nativeQuery = true)
    Optional<Object[]> getExpenseStatistics(@Param("nexId") String nexId);

    /**
     * Get expense statistics for a user.
     * 
     * @param userId The user ID
     * @return Expense statistics
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_expenses,
                SUM(amount) as total_amount,
                AVG(amount) as average_amount,
                MAX(amount) as max_amount,
                MIN(amount) as min_amount,
                COUNT(CASE WHEN is_fully_settled = true THEN 1 END) as settled_count,
                COUNT(CASE WHEN is_fully_settled = false THEN 1 END) as unsettled_count,
                SUM(unsettled_amount) as total_unsettled_amount
            FROM expense_summary_view
            WHERE created_by = :userId OR payer_id = :userId
            """, nativeQuery = true)
    Optional<Object[]> getExpenseStatisticsByUser(@Param("userId") String userId);

    /**
     * Get expense statistics by category.
     * 
     * @param categoryId The category ID
     * @return Expense statistics
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_expenses,
                SUM(amount) as total_amount,
                AVG(amount) as average_amount,
                MAX(amount) as max_amount,
                MIN(amount) as min_amount
            FROM expense_summary_view
            WHERE category_id = :categoryId
            """, nativeQuery = true)
    Optional<Object[]> getExpenseStatisticsByCategory(@Param("categoryId") String categoryId);
}
