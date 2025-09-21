package com.nexsplit.dto.bill;

import com.nexsplit.model.Bill;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new bill.
 * Contains all necessary information to create a bill.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillRequest {

    @NotBlank(message = "Nex ID is required")
    private String nexId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    @Builder.Default
    private String currency = "USD"; // TODO: Replace with configuration property

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    @NotNull(message = "Frequency is required")
    private Bill.Frequency frequency;

    private LocalDateTime nextDueDate;

    @Builder.Default
    private Boolean isRecurring = false;

    @Builder.Default
    private Boolean isPaid = false;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
