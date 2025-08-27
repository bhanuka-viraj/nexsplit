package com.nexsplit.dto.nex;

import com.nexsplit.model.Nex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NexDto {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String createdBy;
    private Nex.SettlementType settlementType;
    private Boolean isArchived;
    private Nex.NexType nexType;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // Additional fields for response
    private String creatorName;
    private String creatorUsername;
    private Integer memberCount;
    private List<NexMemberDto> members;
    private Integer expenseCount;
    private Integer categoryCount;
}
