package com.nexsplit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for user balance data from database view.
 * 
 * This DTO represents data from the user_balance_view and provides
 * comprehensive balance information for users.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceDto {

    private String userId;
    private String userName;
    private String userEmail;
    private String username;
    private BigDecimal totalDebt;
    private BigDecimal totalCredit;
    private BigDecimal netBalance;
    private Integer activeDebtCount;
    private Integer activeCreditCount;
    private Integer totalExpensesCreated;
    private BigDecimal totalExpenseAmountCreated;
    private Integer totalExpensesPaid;
    private BigDecimal totalExpenseAmountPaid;
}
