package com.nexsplit.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary DTO for expense information.
 * Contains essential expense data for list views and summaries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryDto {

    private String id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private String categoryName;
    private String description;
    private String payerName;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * Number of people this expense is split among.
     */
    private Integer splitCount;

    /**
     * Whether the expense has been fully settled.
     */
    private Boolean isFullySettled;

    /**
     * Total amount of unsettled debts for this expense.
     */
    private BigDecimal unsettledAmount;

    /**
     * Total number of expenses (for nex-level summary).
     */
    private Integer totalExpenses;

    /**
     * Total expense amount (for nex-level summary).
     */
    private BigDecimal totalExpenseAmount;
}
