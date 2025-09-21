package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Debt entity representing debts between users.
 * 
 * This entity stores information about debts created from expense splits,
 * including debtor, creditor, amount, payment method, and settlement status.
 * Debts are automatically generated when expenses are split among users.
 * 
 * Database table: debts
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "debts")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Debt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "debtor_id", nullable = false, columnDefinition = "CHAR(36)")
    private String debtorId;

    @Column(name = "creditor_id", nullable = false, columnDefinition = "CHAR(36)")
    private String creditorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "creditor_type", nullable = false)
    private CreditorType creditorType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_id", nullable = false, columnDefinition = "CHAR(36)")
    private String expenseId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_id", insertable = false, updatable = false)
    private User debtor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_id", insertable = false, updatable = false)
    private User creditor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", insertable = false, updatable = false)
    private Expense expense;

    public enum CreditorType {
        USER, EXPENSE
    }

    /**
     * Mark the debt as settled.
     * 
     * @param settledAt The timestamp when the debt was settled
     */
    public void markAsSettled(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    /**
     * Mark the debt as unsettled.
     */
    public void markAsUnsettled() {
        this.settledAt = null;
    }

    /**
     * Check if the debt is settled.
     * 
     * @return true if the debt is settled, false otherwise
     */
    public boolean isSettled() {
        return this.settledAt != null;
    }

    /**
     * Ensure default values are set before persisting
     */
    @PrePersist
    protected void onPrePersist() {
        ensureDefaultValues();
    }
}
