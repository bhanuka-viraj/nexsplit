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
 * BillParticipant entity representing participants in a bill.
 * 
 * This entity stores information about users who are participants in a bill,
 * including their share amount, payment status, and payment timestamp.
 * 
 * Database table: bill_participants
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "bill_participants")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BillParticipant extends BaseEntity {

    @EmbeddedId
    private BillParticipantId id;

    @Column(name = "share_amount", precision = 10, scale = 2)
    private BigDecimal shareAmount;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("billId")
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        if (paid == null) {
            paid = false;
        }
    }

    /**
     * Mark the participant as paid.
     * 
     * @param paidAt The timestamp when the payment was made
     */
    public void markAsPaid(LocalDateTime paidAt) {
        this.paid = true;
        this.paidAt = paidAt;
    }

    /**
     * Mark the participant as unpaid.
     */
    public void markAsUnpaid() {
        this.paid = false;
        this.paidAt = null;
    }

    /**
     * Check if the participant has paid.
     * 
     * @return true if the participant has paid, false otherwise
     */
    public boolean isPaid() {
        return Boolean.TRUE.equals(this.paid);
    }
}
