package com.nexsplit.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Filter DTO for expense queries.
 * Contains criteria for filtering and searching expenses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseFilter {

    /**
     * Filter by expense group (nex) ID.
     */
    private String nexId;

    /**
     * Filter by category ID.
     */
    private String categoryId;

    /**
     * Filter by payer user ID.
     */
    private String payerId;

    /**
     * Filter by creator user ID.
     */
    private String createdBy;

    /**
     * Filter by user ID (expenses where user is involved as payer or in splits).
     */
    private String userId;

    /**
     * Filter by minimum amount.
     */
    private BigDecimal minAmount;

    /**
     * Filter by maximum amount.
     */
    private BigDecimal maxAmount;

    /**
     * Filter by currency.
     */
    private String currency;

    /**
     * Filter by split type.
     */
    private String splitType;

    /**
     * Filter by settlement status.
     * true = fully settled, false = has unsettled debts, null = all
     */
    private Boolean isFullySettled;

    /**
     * Filter by date range - start date.
     */
    private LocalDateTime startDate;

    /**
     * Filter by date range - end date.
     */
    private LocalDateTime endDate;

    /**
     * Search term for title and description.
     */
    private String searchTerm;

    /**
     * Sort field.
     * Options: createdAt, modifiedAt, amount, title
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Sort direction.
     * Options: ASC, DESC
     */
    @Builder.Default
    private String sortDirection = "DESC";
}
