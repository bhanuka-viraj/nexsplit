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
public class NexMemberId implements Serializable {

    @Column(name = "nex_id", columnDefinition = "CHAR(36)")
    private String nexId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;
}
