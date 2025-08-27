package com.nexsplit.dto.nex;

import com.nexsplit.model.Nex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNexRequest {

    @NotBlank(message = "Nex name is required")
    @Size(min = 1, max = 255, message = "Nex name must be between 1 and 255 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    @NotNull(message = "Settlement type is required")
    private Nex.SettlementType settlementType;

    @NotNull(message = "Nex type is required")
    private Nex.NexType nexType;
}
