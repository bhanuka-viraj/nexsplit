package com.nexsplit.model.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing the settlement_history_view database view.
 * 
 * This entity provides optimized access to settlement and debt information
 * with user details and expense context for comprehensive settlement tracking.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "settlement_history_view")
@Data
@EqualsAndHashCode(callSuper = false)
public class SettlementHistoryView {

    @Id
    @Column(name = "debt_id")
    private String debtId;

    @Column(name = "debtor_id")
    private String debtorId;

    @Column(name = "debtor_name")
    private String debtorName;

    @Column(name = "debtor_email")
    private String debtorEmail;

    @Column(name = "creditor_id")
    private String creditorId;

    @Column(name = "creditor_name")
    private String creditorName;

    @Column(name = "creditor_email")
    private String creditorEmail;

    @Column(name = "creditor_type")
    private String creditorType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "expense_id")
    private String expenseId;

    @Column(name = "expense_title")
    private String expenseTitle;

    @Column(name = "expense_amount")
    private BigDecimal expenseAmount;

    @Column(name = "expense_currency")
    private String expenseCurrency;

    @Column(name = "nex_id")
    private String nexId;

    @Column(name = "nex_name")
    private String nexName;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "debt_notes")
    private String debtNotes;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "debt_created_at")
    private LocalDateTime debtCreatedAt;

    @Column(name = "debt_modified_at")
    private LocalDateTime debtModifiedAt;

    @Column(name = "is_settled")
    private Boolean isSettled;

    @Column(name = "settlement_hours")
    private Double settlementHours;
}
