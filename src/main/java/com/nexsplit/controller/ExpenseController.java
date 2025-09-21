package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.expense.CreateExpenseRequest;
import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.dto.expense.ExpenseFilter;
import com.nexsplit.model.view.ExpenseSummaryView;
import com.nexsplit.dto.expense.UpdateExpenseRequest;
import com.nexsplit.service.ExpenseService;
import com.nexsplit.util.StructuredLoggingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for expense management.
 * Provides endpoints for creating, reading, updating, and deleting expenses.
 */
@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/expenses")
@Tag(name = "Expenses", description = "Expense management endpoints")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

        private final ExpenseService expenseService;

        @PostMapping
        @Operation(summary = "Create Expense", description = "Create a new expense with automatic split calculation and debt generation", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(
                        @Valid @RequestBody CreateExpenseRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "EXPENSE_CREATED",
                                userId,
                                "CREATE_EXPENSE",
                                "SUCCESS",
                                Map.of(
                                                "nexId", request.getNexId(),
                                                "amount", request.getAmount(),
                                                "currency", request.getCurrency(),
                                                "splitType", request.getSplitType().name()));

                ExpenseDto expense = expenseService.createExpense(request, userId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(expense, "Expense created successfully"));
        }

        @GetMapping("/{expenseId}")
        @Operation(summary = "Get Expense Details", description = "Get detailed information about a specific expense", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<ExpenseDto>> getExpenseById(
                        @PathVariable String expenseId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                ExpenseDto expense = expenseService.getExpenseById(expenseId, userId);

                return ResponseEntity.ok(ApiResponse.success(expense, "Expense details retrieved successfully"));
        }

        @GetMapping
        @Operation(summary = "List Expenses", description = "Get paginated list of expenses with optional filtering", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseDto>>> getExpenses(
                        @Parameter(description = "Filter by nex ID") @RequestParam(required = false) String nexId,
                        @Parameter(description = "Filter by category ID") @RequestParam(required = false) String categoryId,
                        @Parameter(description = "Filter by payer ID") @RequestParam(required = false) String payerId,
                        @Parameter(description = "Filter by user involvement") @RequestParam(required = false) String userId,
                        @Parameter(description = "Search term for title/description") @RequestParam(required = false) String search,
                        @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String currentUserId = userDetails.getUsername();
                log.info("Controller received request for expenses from userId: {}, nexId filter: {}", currentUserId,
                                nexId);

                // Build filter
                ExpenseFilter filter = ExpenseFilter.builder()
                                .nexId(nexId)
                                .categoryId(categoryId)
                                .payerId(payerId)
                                .userId(userId)
                                .searchTerm(search)
                                .sortBy(sortBy)
                                .sortDirection(sortDirection)
                                .build();

                PaginatedResponse<ExpenseDto> response = expenseService.getExpenses(filter, currentUserId, page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Expenses retrieved successfully"));
        }

        @GetMapping("/nex/{nexId}")
        @Operation(summary = "Get Expenses by Nex", description = "Get paginated expenses for a specific expense group", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseDto>>> getExpensesByNexId(
                        @PathVariable String nexId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();
                log.info("Controller received request for nexId: {} from userId: {}", nexId, userId);

                PaginatedResponse<ExpenseDto> response = expenseService.getExpensesByNexId(nexId, userId, page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Expenses retrieved successfully"));
        }

        @GetMapping("/user/{userId}/involvement")
        @Operation(summary = "Get Expenses by User Involvement", description = "Get expenses where a specific user is involved (as payer or in splits)", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseDto>>> getExpensesByUserInvolvement(
                        @PathVariable String userId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                PaginatedResponse<ExpenseDto> response = expenseService.getExpensesByUserInvolvement(userId, page,
                                size);

                return ResponseEntity.ok(ApiResponse.success(response, "Expenses retrieved successfully"));
        }

        @PutMapping("/{expenseId}")
        @Operation(summary = "Update Expense", description = "Update an existing expense. This will recalculate splits and debts if necessary.", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(
                        @PathVariable String expenseId,
                        @Valid @RequestBody UpdateExpenseRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "EXPENSE_UPDATED",
                                userId,
                                "UPDATE_EXPENSE",
                                "SUCCESS",
                                Map.of("expenseId", expenseId));

                ExpenseDto expense = expenseService.updateExpense(expenseId, request, userId);

                return ResponseEntity.ok(ApiResponse.success(expense, "Expense updated successfully"));
        }

        @DeleteMapping("/{expenseId}")
        @Operation(summary = "Delete Expense", description = "Delete an expense (soft delete). This will also soft delete related splits and debts.", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> deleteExpense(
                        @PathVariable String expenseId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "EXPENSE_DELETED",
                                userId,
                                "DELETE_EXPENSE",
                                "SUCCESS",
                                Map.of("expenseId", expenseId));

                expenseService.deleteExpense(expenseId, userId);

                return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted successfully"));
        }

        @GetMapping("/nex/{nexId}/summary")
        @Operation(summary = "Get Expense Summary", description = "Get expense summary for a specific expense group", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<ExpenseSummaryView>> getExpenseSummary(
                        @PathVariable String nexId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                ExpenseSummaryView summary = expenseService.getExpenseSummary(nexId, userId);

                return ResponseEntity.ok(ApiResponse.success(summary, "Expense summary retrieved successfully"));
        }

        @GetMapping("/search")
        @Operation(summary = "Search Expenses", description = "Search expenses by title or description", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseDto>>> searchExpenses(
                        @Parameter(description = "Search term") @RequestParam String q,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<ExpenseDto> response = expenseService.searchExpenses(q, userId, page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Search results retrieved successfully"));
        }

        @GetMapping("/category/{categoryId}")
        @Operation(summary = "Get Expenses by Category", description = "Get expenses for a specific category", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseDto>>> getExpensesByCategoryId(
                        @PathVariable String categoryId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<ExpenseDto> response = expenseService.getExpensesByCategoryId(categoryId, userId,
                                page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Expenses retrieved successfully"));
        }

        @GetMapping("/payer/{payerId}")
        @Operation(summary = "Get Expenses by Payer", description = "Get expenses paid by a specific user", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseDto>>> getExpensesByPayerId(
                        @PathVariable String payerId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<ExpenseDto> response = expenseService.getExpensesByPayerId(payerId, userId, page,
                                size);

                return ResponseEntity.ok(ApiResponse.success(response, "Expenses retrieved successfully"));
        }
}
