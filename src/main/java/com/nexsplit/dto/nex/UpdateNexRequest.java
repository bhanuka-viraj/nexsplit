package com.nexsplit.dto.nex;

import com.nexsplit.model.Nex;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNexRequest {

    @Size(min = 1, max = 255, message = "Nex name must be between 1 and 255 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    private Nex.SettlementType settlementType;
    private Boolean isArchived;
    private Nex.NexType nexType;

}
