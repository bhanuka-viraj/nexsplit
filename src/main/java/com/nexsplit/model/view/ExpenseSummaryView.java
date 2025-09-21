package com.nexsplit.model.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing the expense_summary_view database view.
 * 
 * This entity provides optimized access to expense analytics and reporting
 * with related entity information for comprehensive expense tracking.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "expense_summary_view")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExpenseSummaryView {

    @Id
    @Column(name = "expense_id")
    private String expenseId;

    @Column(name = "title")
    private String title;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "nex_id")
    private String nexId;

    @Column(name = "nex_name")
    private String nexName;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "created_by_email")
    private String createdByEmail;

    @Column(name = "payer_id")
    private String payerId;

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "split_type")
    private String splitType;

    @Column(name = "is_initial_payer_has")
    private Boolean isInitialPayerHas;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "split_count")
    private Long splitCount;

    @Column(name = "debt_count")
    private Long debtCount;

    @Column(name = "unsettled_debt_count")
    private Long unsettledDebtCount;

    @Column(name = "unsettled_amount")
    private BigDecimal unsettledAmount;

    @Column(name = "is_fully_settled")
    private Boolean isFullySettled;

    @Column(name = "attachment_count")
    private Long attachmentCount;
}
