package com.nexsplit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for BillParticipant entity.
 * 
 * This embeddable class represents the composite primary key for the
 * bill_participants table, consisting of bill_id and user_id.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillParticipantId implements Serializable {

    @Column(name = "bill_id", columnDefinition = "CHAR(36)")
    private String billId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;
}
