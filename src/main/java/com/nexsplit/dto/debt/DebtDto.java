package com.nexsplit.dto.debt;

import com.nexsplit.model.Debt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for debt data.
 * Contains complete debt information.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtDto {

    private String id;
    private String debtorId;
    private String debtorName;
    private String debtorEmail;
    private String creditorId;
    private String creditorName;
    private String creditorEmail;
    private Debt.CreditorType creditorType;
    private BigDecimal amount;
    private String expenseId;
    private String expenseTitle;
    private String paymentMethod;
    private String notes;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Boolean isSettled;
}
