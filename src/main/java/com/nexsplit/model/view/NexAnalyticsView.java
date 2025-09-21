package com.nexsplit.model.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing the nex_analytics_view database view.
 * 
 * This entity provides optimized access to nex analytics and insights
 * with comprehensive statistics for nex group management and reporting.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "nex_analytics_view")
@Data
@EqualsAndHashCode(callSuper = false)
public class NexAnalyticsView {

    @Id
    @Column(name = "nex_id")
    private String nexId;

    @Column(name = "nex_name")
    private String nexName;

    @Column(name = "description")
    private String description;

    @Column(name = "settlement_type")
    private String settlementType;

    @Column(name = "nex_type")
    private String nexType;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "creator_email")
    private String creatorEmail;

    @Column(name = "nex_created_at")
    private LocalDateTime nexCreatedAt;

    @Column(name = "nex_modified_at")
    private LocalDateTime nexModifiedAt;

    // Member statistics
    @Column(name = "total_members")
    private Long totalMembers;

    @Column(name = "active_members")
    private Long activeMembers;

    @Column(name = "admin_count")
    private Long adminCount;

    // Expense statistics
    @Column(name = "total_expenses")
    private Long totalExpenses;

    @Column(name = "total_expense_amount")
    private BigDecimal totalExpenseAmount;

    @Column(name = "average_expense_amount")
    private BigDecimal averageExpenseAmount;

    @Column(name = "max_expense_amount")
    private BigDecimal maxExpenseAmount;

    @Column(name = "min_expense_amount")
    private BigDecimal minExpenseAmount;

    // Debt statistics
    @Column(name = "total_debts")
    private Long totalDebts;

    @Column(name = "unsettled_debts")
    private Long unsettledDebts;

    @Column(name = "unsettled_debt_amount")
    private BigDecimal unsettledDebtAmount;

    // Category statistics
    @Column(name = "total_categories")
    private Long totalCategories;

    @Column(name = "default_categories")
    private Long defaultCategories;

    // Bill statistics
    @Column(name = "total_bills")
    private Long totalBills;

    @Column(name = "paid_bills")
    private Long paidBills;

    @Column(name = "total_bill_amount")
    private BigDecimal totalBillAmount;

    // Recent activity
    @Column(name = "last_expense_date")
    private LocalDateTime lastExpenseDate;

    @Column(name = "last_debt_date")
    private LocalDateTime lastDebtDate;

    @Column(name = "last_bill_date")
    private LocalDateTime lastBillDate;
}
