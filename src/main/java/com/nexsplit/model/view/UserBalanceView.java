package com.nexsplit.model.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * JPA entity representing the user_balance_view database view.
 * 
 * This entity provides optimized access to user balance calculations
 * showing total debts, credits, and net balance for comprehensive user
 * financial tracking.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "user_balance_view")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserBalanceView {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "username")
    private String username;

    @Column(name = "total_debt")
    private BigDecimal totalDebt;

    @Column(name = "total_credit")
    private BigDecimal totalCredit;

    @Column(name = "net_balance")
    private BigDecimal netBalance;

    @Column(name = "active_debt_count")
    private Long activeDebtCount;

    @Column(name = "active_credit_count")
    private Long activeCreditCount;

    @Column(name = "total_expenses_created")
    private Long totalExpensesCreated;

    @Column(name = "total_expense_amount_created")
    private BigDecimal totalExpenseAmountCreated;

    @Column(name = "total_expenses_paid")
    private Long totalExpensesPaid;

    @Column(name = "total_expense_amount_paid")
    private BigDecimal totalExpenseAmountPaid;
}
