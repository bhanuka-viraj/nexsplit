package com.nexsplit.repository;

import com.nexsplit.model.Debt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Debt entity.
 * Provides database operations for debt management.
 */
@Repository
public interface DebtRepository extends JpaRepository<Debt, String> {

    /**
     * Find debts by debtor ID.
     * 
     * @param debtorId The debtor ID
     * @return List of debts
     */
    List<Debt> findByDebtorIdOrderByCreatedAtDesc(String debtorId);

    /**
     * Find debts by creditor ID.
     * 
     * @param creditorId The creditor ID
     * @return List of debts
     */
    List<Debt> findByCreditorIdOrderByCreatedAtDesc(String creditorId);

    /**
     * Find debts by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of debts
     */
    List<Debt> findByExpenseIdOrderByCreatedAtDesc(String expenseId);

    /**
     * Find unsettled debts by debtor ID.
     * 
     * @param debtorId The debtor ID
     * @return List of unsettled debts
     */
    List<Debt> findByDebtorIdAndSettledAtIsNullOrderByCreatedAtDesc(String debtorId);

    /**
     * Find unsettled debts by creditor ID.
     * 
     * @param creditorId The creditor ID
     * @return List of unsettled debts
     */
    List<Debt> findByCreditorIdAndSettledAtIsNullOrderByCreatedAtDesc(String creditorId);

    /**
     * Find unsettled debts by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of unsettled debts
     */
    List<Debt> findByExpenseIdAndSettledAtIsNullOrderByCreatedAtDesc(String expenseId);

    /**
     * Find settled debts by debtor ID.
     * 
     * @param debtorId The debtor ID
     * @return List of settled debts
     */
    List<Debt> findByDebtorIdAndSettledAtIsNotNullOrderBySettledAtDesc(String debtorId);

    /**
     * Find settled debts by creditor ID.
     * 
     * @param creditorId The creditor ID
     * @return List of settled debts
     */
    List<Debt> findByCreditorIdAndSettledAtIsNotNullOrderBySettledAtDesc(String creditorId);

    /**
     * Find debts between two users (debtor and creditor).
     * 
     * @param debtorId   The debtor ID
     * @param creditorId The creditor ID
     * @return List of debts
     */
    List<Debt> findByDebtorIdAndCreditorIdOrderByCreatedAtDesc(String debtorId, String creditorId);

    /**
     * Find unsettled debts between two users.
     * 
     * @param debtorId   The debtor ID
     * @param creditorId The creditor ID
     * @return List of unsettled debts
     */
    List<Debt> findByDebtorIdAndCreditorIdAndSettledAtIsNullOrderByCreatedAtDesc(String debtorId, String creditorId);

    /**
     * Find debts by nex ID (through expense relationship).
     * 
     * @param nexId The nex ID
     * @return List of debts
     */
    @Query("SELECT d FROM Debt d JOIN d.expense e WHERE e.nexId = :nexId ORDER BY d.createdAt DESC")
    List<Debt> findByNexId(@Param("nexId") String nexId);

    /**
     * Find unsettled debts by nex ID.
     * 
     * @param nexId The nex ID
     * @return List of unsettled debts
     */
    @Query("SELECT d FROM Debt d JOIN d.expense e WHERE e.nexId = :nexId AND d.settledAt IS NULL ORDER BY d.createdAt DESC")
    List<Debt> findUnsettledByNexId(@Param("nexId") String nexId);

    /**
     * Find settled debts by nex ID with pagination.
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination information
     * @return Page of settled debts
     */
    @Query("SELECT d FROM Debt d JOIN d.expense e WHERE e.nexId = :nexId AND d.settledAt IS NOT NULL ORDER BY d.settledAt DESC")
    Page<Debt> findSettledDebtsByNexId(@Param("nexId") String nexId, Pageable pageable);

    /**
     * Find debts by user ID (as debtor or creditor).
     * 
     * @param userId The user ID
     * @return List of debts
     */
    @Query("SELECT d FROM Debt d WHERE d.debtorId = :userId OR d.creditorId = :userId ORDER BY d.createdAt DESC")
    List<Debt> findByUserId(@Param("userId") String userId);

    /**
     * Find unsettled debts by user ID.
     * 
     * @param userId The user ID
     * @return List of unsettled debts
     */
    @Query("SELECT d FROM Debt d WHERE (d.debtorId = :userId OR d.creditorId = :userId) AND d.settledAt IS NULL ORDER BY d.createdAt DESC")
    List<Debt> findUnsettledByUserId(@Param("userId") String userId);

    /**
     * Find settled debts by user ID.
     * 
     * @param userId The user ID
     * @return List of settled debts
     */
    @Query("SELECT d FROM Debt d WHERE (d.debtorId = :userId OR d.creditorId = :userId) AND d.settledAt IS NOT NULL ORDER BY d.settledAt DESC")
    List<Debt> findSettledByUserId(@Param("userId") String userId);

    /**
     * Calculate total debt amount by debtor ID.
     * 
     * @param debtorId The debtor ID
     * @return Total debt amount
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.debtorId = :debtorId AND d.settledAt IS NULL")
    BigDecimal calculateTotalDebtAmountByDebtorId(@Param("debtorId") String debtorId);

    /**
     * Calculate total credit amount by creditor ID.
     * 
     * @param creditorId The creditor ID
     * @return Total credit amount
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.creditorId = :creditorId AND d.settledAt IS NULL")
    BigDecimal calculateTotalCreditAmountByCreditorId(@Param("creditorId") String creditorId);

    /**
     * Calculate net balance for a user (credits - debts).
     * 
     * @param userId The user ID
     * @return Net balance (positive = user is owed money, negative = user owes
     *         money)
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN d.creditorId = :userId THEN d.amount ELSE -d.amount END), 0) " +
            "FROM Debt d WHERE (d.debtorId = :userId OR d.creditorId = :userId) AND d.settledAt IS NULL")
    BigDecimal calculateNetBalanceByUserId(@Param("userId") String userId);

    /**
     * Count debts by debtor ID.
     * 
     * @param debtorId The debtor ID
     * @return Count of debts
     */
    long countByDebtorId(String debtorId);

    /**
     * Count debts by creditor ID.
     * 
     * @param creditorId The creditor ID
     * @return Count of debts
     */
    long countByCreditorId(String creditorId);

    /**
     * Count unsettled debts by debtor ID.
     * 
     * @param debtorId The debtor ID
     * @return Count of unsettled debts
     */
    long countByDebtorIdAndSettledAtIsNull(String debtorId);

    /**
     * Count unsettled debts by creditor ID.
     * 
     * @param creditorId The creditor ID
     * @return Count of unsettled debts
     */
    long countByCreditorIdAndSettledAtIsNull(String creditorId);

    /**
     * Delete debts by expense ID.
     * 
     * @param expenseId The expense ID
     */
    void deleteByExpenseId(String expenseId);
}
