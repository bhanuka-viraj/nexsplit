package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bill entity representing recurring or one-time bills.
 * 
 * This entity stores information about bills including title, amount, currency,
 * due dates, frequency, and payment status. Bills can be recurring or one-time
 * and are associated with nex groups and created by users.
 * 
 * Database table: bills
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "bills")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Bill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "nex_id", nullable = false, columnDefinition = "CHAR(36)")
    private String nexId;

    @Column(name = "created_by", nullable = false, columnDefinition = "CHAR(36)")
    private String createdBy;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency;

    @Column(name = "next_due_date")
    private LocalDateTime nextDueDate;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nex_id", insertable = false, updatable = false)
    private Nex nex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BillParticipant> participants;

    public enum Frequency {
        ONCE, DAILY, WEEKLY, MONTHLY, YEARLY
    }

    @PrePersist
    protected void onCreate() {
        // Ensure BaseEntity default values are set
        ensureDefaultValues();

        // Set Bill-specific default values
        if (currency == null) {
            currency = "USD"; // TODO: Use CurrencyUtil.getDefaultCurrency() when Spring context is available
        }
        if (isRecurring == null) {
            isRecurring = false;
        }
        if (isPaid == null) {
            isPaid = false;
        }
    }
}
