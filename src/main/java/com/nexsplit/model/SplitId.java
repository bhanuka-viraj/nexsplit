package com.nexsplit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitId implements Serializable {

    @Column(name = "expense_id", columnDefinition = "CHAR(36)")
    private String expenseId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;
}
