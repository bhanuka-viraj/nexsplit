package com.nexsplit.repository;

import com.nexsplit.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Expense entity.
 * Provides database operations for expense management.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {

    /**
     * Find expenses by nex ID with pagination.
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination information
     * @return Page of expenses
     */
    Page<Expense> findByNexIdAndIsDeletedFalse(String nexId, Pageable pageable);

    /**
     * Find expenses by nex ID.
     * 
     * @param nexId The nex ID
     * @return List of expenses
     */
    List<Expense> findByNexIdAndIsDeletedFalseOrderByCreatedAtDesc(String nexId);

    /**
     * Find expense by ID and ensure it's not deleted.
     * 
     * @param expenseId The expense ID
     * @return Optional expense
     */
    Optional<Expense> findByIdAndIsDeletedFalse(String expenseId);

    /**
     * Find expenses by category ID.
     * 
     * @param categoryId The category ID
     * @return List of expenses
     */
    List<Expense> findByCategoryIdAndIsDeletedFalseOrderByCreatedAtDesc(String categoryId);

    /**
     * Find expenses by payer ID.
     * 
     * @param payerId The payer ID
     * @return List of expenses
     */
    List<Expense> findByPayerIdAndIsDeletedFalseOrderByCreatedAtDesc(String payerId);

    /**
     * Find expenses by creator ID.
     * 
     * @param createdBy The creator ID
     * @return List of expenses
     */
    List<Expense> findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc(String createdBy);

    /**
     * Find expenses by date range.
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return List of expenses
     */
    List<Expense> findByCreatedAtBetweenAndIsDeletedFalseOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find expenses by amount range.
     * 
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     * @return List of expenses
     */
    List<Expense> findByAmountBetweenAndIsDeletedFalseOrderByCreatedAtDesc(
            BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find expenses by currency.
     * 
     * @param currency The currency
     * @return List of expenses
     */
    List<Expense> findByCurrencyAndIsDeletedFalseOrderByCreatedAtDesc(String currency);

    /**
     * Find expenses by split type.
     * 
     * @param splitType The split type
     * @return List of expenses
     */
    List<Expense> findBySplitTypeAndIsDeletedFalseOrderByCreatedAtDesc(Expense.SplitType splitType);

    /**
     * Find expenses where user is involved (as payer or in splits).
     * 
     * @param userId The user ID
     * @return List of expenses
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN e.splits s " +
            "WHERE (e.payerId = :userId OR s.user.id = :userId) " +
            "AND e.isDeleted = false " +
            "ORDER BY e.createdAt DESC")
    List<Expense> findExpensesByUserInvolvement(@Param("userId") String userId);

    /**
     * Find expenses where user is involved with pagination.
     * 
     * @param userId   The user ID
     * @param pageable Pagination information
     * @return Page of expenses
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN e.splits s " +
            "WHERE (e.payerId = :userId OR s.user.id = :userId) " +
            "AND e.isDeleted = false " +
            "ORDER BY e.createdAt DESC")
    Page<Expense> findExpensesByUserInvolvement(@Param("userId") String userId, Pageable pageable);

    /**
     * Find expenses by nex ID and user involvement.
     * 
     * @param nexId  The nex ID
     * @param userId The user ID
     * @return List of expenses
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN e.splits s " +
            "WHERE e.nexId = :nexId " +
            "AND (e.payerId = :userId OR s.user.id = :userId) " +
            "AND e.isDeleted = false " +
            "ORDER BY e.createdAt DESC")
    List<Expense> findExpensesByNexIdAndUserInvolvement(@Param("nexId") String nexId, @Param("userId") String userId);

    /**
     * Find expenses by nex ID and user involvement with pagination.
     * 
     * @param nexId    The nex ID
     * @param userId   The user ID
     * @param pageable Pagination information
     * @return Page of expenses
     */
    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN e.splits s " +
            "WHERE e.nexId = :nexId " +
            "AND (e.payerId = :userId OR s.user.id = :userId) " +
            "AND e.isDeleted = false " +
            "ORDER BY e.createdAt DESC")
    Page<Expense> findExpensesByNexIdAndUserInvolvement(@Param("nexId") String nexId, @Param("userId") String userId,
            Pageable pageable);

    /**
     * Search expenses by title or description.
     * 
     * @param searchTerm The search term
     * @return List of expenses
     */
    @Query("SELECT e FROM Expense e " +
            "WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND e.isDeleted = false " +
            "ORDER BY e.createdAt DESC")
    List<Expense> searchExpensesByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Search expenses by title or description with pagination.
     * 
     * @param searchTerm The search term
     * @param pageable   Pagination information
     * @return Page of expenses
     */
    @Query("SELECT e FROM Expense e " +
            "WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND e.isDeleted = false " +
            "ORDER BY e.createdAt DESC")
    Page<Expense> searchExpensesByTitleOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Calculate total amount of expenses by nex ID.
     * 
     * @param nexId The nex ID
     * @return Total amount
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.nexId = :nexId AND e.isDeleted = false")
    BigDecimal calculateTotalAmountByNexId(@Param("nexId") String nexId);

    /**
     * Calculate total amount of expenses by category ID.
     * 
     * @param categoryId The category ID
     * @return Total amount
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.categoryId = :categoryId AND e.isDeleted = false")
    BigDecimal calculateTotalAmountByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Count expenses by nex ID.
     * 
     * @param nexId The nex ID
     * @return Count of expenses
     */
    long countByNexIdAndIsDeletedFalse(String nexId);

    /**
     * Count expenses by category ID.
     * 
     * @param categoryId The category ID
     * @return Count of expenses
     */
    long countByCategoryIdAndIsDeletedFalse(String categoryId);

    /**
     * Count expenses by payer ID.
     * 
     * @param payerId The payer ID
     * @return Count of expenses
     */
    long countByPayerIdAndIsDeletedFalse(String payerId);

    /**
     * Soft delete expense by ID.
     * 
     * @param expenseId The expense ID
     * @param deletedBy The user ID who deleted the expense
     */
    @Query("UPDATE Expense e SET e.isDeleted = true, e.deletedAt = CURRENT_TIMESTAMP, e.deletedBy = :deletedBy " +
            "WHERE e.id = :expenseId")
    void softDeleteById(@Param("expenseId") String expenseId, @Param("deletedBy") String deletedBy);
}
