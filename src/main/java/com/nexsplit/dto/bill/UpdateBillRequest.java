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
 * Request DTO for updating an existing bill.
 * Contains fields that can be modified for a bill.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBillRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    private Bill.Frequency frequency;

    private LocalDateTime nextDueDate;

    private Boolean isRecurring;

    private Boolean isPaid;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
