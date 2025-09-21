package com.nexsplit.dto.bill;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new bill participant.
 * Contains all necessary information to create a bill participant.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillParticipantRequest {

    @NotBlank(message = "Bill ID is required")
    private String billId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Share amount is required")
    @DecimalMin(value = "0.01", message = "Share amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Share amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal shareAmount;
}
