package com.nexsplit.dto.debt;

import com.nexsplit.model.Debt;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new debt.
 * Contains all necessary information to create a debt.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDebtRequest {

    @NotBlank(message = "Debtor ID is required")
    private String debtorId;

    @NotBlank(message = "Creditor ID is required")
    private String creditorId;

    @NotNull(message = "Creditor type is required")
    private Debt.CreditorType creditorType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Expense ID is required")
    private String expenseId;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
