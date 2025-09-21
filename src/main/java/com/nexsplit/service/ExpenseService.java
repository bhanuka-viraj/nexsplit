package com.nexsplit.service;

import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.expense.CreateExpenseRequest;
import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.dto.expense.ExpenseFilter;
import com.nexsplit.model.view.ExpenseSummaryView;
import com.nexsplit.dto.expense.UpdateExpenseRequest;

/**
 * Service interface for expense management.
 * Provides business logic for expense operations including CRUD operations,
 * split calculations, and debt generation.
 */
public interface ExpenseService {

    /**
     * Create a new expense with automatic split calculation and debt generation.
     * 
     * @param request The expense creation request
     * @param userId  The ID of the user creating the expense
     * @return Created expense DTO
     */
    ExpenseDto createExpense(CreateExpenseRequest request, String userId);

    /**
     * Get expense by ID with full details.
     * 
     * @param expenseId The expense ID
     * @param userId    The ID of the user requesting the expense
     * @return Expense DTO
     */
    ExpenseDto getExpenseById(String expenseId, String userId);

    /**
     * Get expenses with filtering and pagination.
     * 
     * @param filter The filter criteria
     * @param userId The ID of the user requesting expenses
     * @param page   Page number (0-based)
     * @param size   Page size
     * @return Paginated response of expense DTOs
     */
    PaginatedResponse<ExpenseDto> getExpenses(ExpenseFilter filter, String userId, int page, int size);

    /**
     * Get expenses by nex ID with pagination.
     * 
     * @param nexId  The nex ID
     * @param userId The ID of the user requesting expenses
     * @param page   Page number (0-based)
     * @param size   Page size
     * @return Paginated response of expense DTOs
     */
    PaginatedResponse<ExpenseDto> getExpensesByNexId(String nexId, String userId, int page, int size);

    /**
     * Get expenses where user is involved (as payer or in splits).
     * 
     * @param userId The ID of the user
     * @param page   Page number (0-based)
     * @param size   Page size
     * @return Paginated response of expense DTOs
     */
    PaginatedResponse<ExpenseDto> getExpensesByUserInvolvement(String userId, int page, int size);

    /**
     * Update an existing expense.
     * This will recalculate splits and debts if necessary.
     * 
     * @param expenseId The expense ID
     * @param request   The update request
     * @param userId    The ID of the user updating the expense
     * @return Updated expense DTO
     */
    ExpenseDto updateExpense(String expenseId, UpdateExpenseRequest request, String userId);

    /**
     * Delete an expense (soft delete).
     * This will also soft delete related splits and debts.
     * 
     * @param expenseId The expense ID
     * @param userId    The ID of the user deleting the expense
     */
    void deleteExpense(String expenseId, String userId);

    /**
     * Get expense summary for a specific nex.
     * 
     * @param nexId  The nex ID
     * @param userId The ID of the user requesting the summary
     * @return Expense summary DTO
     */
    ExpenseSummaryView getExpenseSummary(String nexId, String userId);

    /**
     * Search expenses by title or description.
     * 
     * @param searchTerm The search term
     * @param userId     The ID of the user searching
     * @param page       Page number (0-based)
     * @param size       Page size
     * @return Paginated response of expense DTOs
     */
    PaginatedResponse<ExpenseDto> searchExpenses(String searchTerm, String userId, int page, int size);

    /**
     * Get expenses by category ID.
     * 
     * @param categoryId The category ID
     * @param userId     The ID of the user requesting expenses
     * @param page       Page number (0-based)
     * @param size       Page size
     * @return Paginated response of expense DTOs
     */
    PaginatedResponse<ExpenseDto> getExpensesByCategoryId(String categoryId, String userId, int page, int size);

    /**
     * Get expenses by payer ID.
     * 
     * @param payerId The payer ID
     * @param userId  The ID of the user requesting expenses
     * @param page    Page number (0-based)
     * @param size    Page size
     * @return Paginated response of expense DTOs
     */
    PaginatedResponse<ExpenseDto> getExpensesByPayerId(String payerId, String userId, int page, int size);

    /**
     * Check if user has access to an expense.
     * 
     * @param expenseId The expense ID
     * @param userId    The user ID
     * @return True if user has access
     */
    boolean hasAccessToExpense(String expenseId, String userId);

    /**
     * Check if user can modify an expense.
     * 
     * @param expenseId The expense ID
     * @param userId    The user ID
     * @return True if user can modify
     */
    boolean canModifyExpense(String expenseId, String userId);
}
