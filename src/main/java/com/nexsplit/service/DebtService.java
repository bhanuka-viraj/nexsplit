package com.nexsplit.service;

import com.nexsplit.dto.debt.CreateDebtRequest;
import com.nexsplit.dto.debt.DebtDto;
import com.nexsplit.dto.debt.DebtSummaryDto;
import com.nexsplit.dto.debt.UpdateDebtRequest;
import com.nexsplit.model.view.SettlementHistoryView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for debt management operations.
 * 
 * This service provides comprehensive debt management functionality including
 * debt creation, updates, retrieval, and balance queries using database views
 * for optimal performance.
 * 
 * NOTE: Settlement operations are handled by SettlementService.
 * This service focuses only on debt CRUD operations and balance queries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
public interface DebtService {

    /**
     * Create a new debt record.
     * 
     * @param request The debt creation request
     * @return Created debt DTO
     */
    DebtDto createDebt(CreateDebtRequest request);

    /**
     * Get debt by ID.
     * 
     * @param debtId The debt ID
     * @return Debt DTO
     */
    DebtDto getDebtById(String debtId);

    /**
     * Update an existing debt.
     * 
     * @param debtId  The debt ID
     * @param request The update request
     * @return Updated debt DTO
     */
    DebtDto updateDebt(String debtId, UpdateDebtRequest request);

    /**
     * Delete a debt (soft delete).
     * 
     * @param debtId    The debt ID
     * @param deletedBy The user ID who deleted the debt
     */
    void deleteDebt(String debtId, String deletedBy);

    /**
     * Delete a debt (soft delete).
     * 
     * @param debtId The debt ID
     */
    void deleteDebt(String debtId);

    /**
     * Get all debts for a specific user (as debtor or creditor).
     * 
     * @param userId The user ID
     * @return List of debt DTOs
     */
    List<DebtDto> getDebtsByUserId(String userId);

    /**
     * Get all debts for a specific user (as debtor or creditor) with pagination.
     * 
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Page of debt DTOs
     */
    Page<DebtDto> getDebtsByUserId(String userId, Pageable pageable);

    /**
     * Get all debts for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of debt DTOs
     */
    List<DebtDto> getDebtsByNexId(String nexId);

    /**
     * Get all debts for a specific nex group with pagination.
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination parameters
     * @return Page of debt DTOs
     */
    Page<DebtDto> getDebtsByNexId(String nexId, Pageable pageable);

    /**
     * Get all debts with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of debt DTOs
     */
    Page<DebtDto> getAllDebts(Pageable pageable);

    /**
     * Get all unsettled debts for a specific user.
     * 
     * @param userId The user ID
     * @return List of unsettled debt DTOs
     */
    List<DebtDto> getUnsettledDebtsByUserId(String userId);

    /**
     * Get all unsettled debts for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of unsettled debt DTOs
     */
    List<DebtDto> getUnsettledDebtsByNexId(String nexId);

    /**
     * Get debts between two specific users.
     * 
     * @param userId1 The first user ID
     * @param userId2 The second user ID
     * @return List of debt DTOs between the users
     */
    List<DebtDto> getDebtsBetweenUsers(String userId1, String userId2);

    /**
     * Get debts by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of debt DTOs for the expense
     */
    List<DebtDto> getDebtsByExpenseId(String expenseId);

    /**
     * Get settlement history for a user.
     * 
     * @param userId The user ID
     * @return List of settlement history records
     */
    List<SettlementHistoryView> getSettlementHistoryByUserId(String userId);

    /**
     * Get settlement history for a nex group.
     * 
     * @param nexId The nex ID
     * @return List of settlement history records
     */
    List<SettlementHistoryView> getSettlementHistoryByNexId(String nexId);

    /**
     * Get settlement history within a date range.
     * 
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of settlement history records
     */
    List<SettlementHistoryView> getSettlementHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get settlement statistics for a user.
     * 
     * @param userId The user ID
     * @return Settlement statistics
     */
    Object[] getSettlementStatisticsByUserId(String userId);

    /**
     * Get settlement statistics for a nex group.
     * 
     * @param nexId The nex ID
     * @return Settlement statistics
     */
    Object[] getSettlementStatisticsByNexId(String nexId);

    /**
     * Calculate user balance (total credit - total debt).
     * 
     * @param userId The user ID
     * @return Net balance amount
     */
    BigDecimal calculateUserBalance(String userId);

    /**
     * Get user balance (total credit - total debt).
     * 
     * @param userId The user ID
     * @return Net balance amount as BigDecimal
     */
    BigDecimal getUserBalance(String userId);

    /**
     * Get nex balances for all users in a nex group.
     * 
     * @param nexId The nex ID
     * @return List of debt summary DTOs with balances
     */
    List<DebtSummaryDto> getNexBalances(String nexId);

    /**
     * Get debts by payment method.
     * 
     * @param paymentMethod The payment method
     * @return List of debt DTOs
     */
    List<DebtDto> getDebtsByPaymentMethod(String paymentMethod);

    /**
     * Get debt summary for a user.
     * 
     * @param userId The user ID
     * @return List of debt summary DTOs
     */
    List<DebtSummaryDto> getDebtSummaryByUserId(String userId);

    /**
     * Get debt summary for a nex group.
     * 
     * @param nexId The nex ID
     * @return List of debt summary DTOs
     */
    List<DebtSummaryDto> getDebtSummaryByNexId(String nexId);
}
