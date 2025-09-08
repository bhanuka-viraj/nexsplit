package com.nexsplit.dto.expense;

import com.nexsplit.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for expense data.
 * Contains complete expense information including splits and related data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {

    private String id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private String categoryId;
    private String categoryName;
    private String description;
    private String nexId;
    private String nexName;
    private String createdBy;
    private String createdByName;
    private String payerId;
    private String payerName;
    private Expense.SplitType splitType;
    private Boolean isInitialPayerHas;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * List of splits for this expense.
     */
    private List<SplitDto> splits;

    /**
     * List of debts generated from this expense.
     */
    private List<DebtDto> debts;

    /**
     * DTO for split information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDto {
        private String userId;
        private String userName;
        private BigDecimal percentage;
        private BigDecimal amount;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    /**
     * DTO for debt information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DebtDto {
        private String id;
        private String debtorId;
        private String debtorName;
        private String creditorId;
        private String creditorName;
        private BigDecimal amount;
        private String paymentMethod;
        private String notes;
        private LocalDateTime settledAt;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
