package com.nexsplit.dto.bill;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing bill participant.
 * Contains fields that can be modified for a bill participant.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBillParticipantRequest {

    @DecimalMin(value = "0.01", message = "Share amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Share amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal shareAmount;

    private Boolean paid;
}
