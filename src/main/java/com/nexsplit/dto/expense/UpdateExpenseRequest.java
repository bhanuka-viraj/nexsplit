package com.nexsplit.dto.expense;

import com.nexsplit.model.Expense;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating an existing expense.
 * Contains fields that can be modified for an expense.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency;

    private String categoryId;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String payerId;

    private Expense.SplitType splitType;

    private Boolean isInitialPayerHas;

    /**
     * List of splits for this expense.
     * If provided, will recalculate all splits and debts.
     */
    private List<UpdateSplitRequest> splits;

    /**
     * Request DTO for updating a split within an expense.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSplitRequest {

        @NotBlank(message = "User ID is required")
        private String userId;

        /**
         * Percentage for PERCENTAGE split type.
         * Must be between 0 and 100.
         */
        @DecimalMin(value = "0.00", message = "Percentage must be non-negative")
        @DecimalMax(value = "100.00", message = "Percentage must not exceed 100")
        @Digits(integer = 3, fraction = 2, message = "Percentage must have at most 3 integer digits and 2 decimal places")
        private BigDecimal percentage;

        /**
         * Amount for AMOUNT split type.
         * Must be positive.
         */
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
        private BigDecimal amount;

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        private String notes;
    }
}
