package com.nexsplit.dto.debt;

import com.nexsplit.model.Debt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary DTO for debt information.
 * Contains essential debt data for list views and summaries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtSummaryDto {

    private String id;
    private String debtorId;
    private String debtorName;
    private String creditorId;
    private String creditorName;
    private Debt.CreditorType creditorType;
    private BigDecimal amount;
    private String expenseId;
    private String expenseTitle;
    private String paymentMethod;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private Boolean isSettled;
}
