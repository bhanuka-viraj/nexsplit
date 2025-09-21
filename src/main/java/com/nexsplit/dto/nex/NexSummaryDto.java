package com.nexsplit.dto.nex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NexSummaryDto {

    private String nexId;
    private String nexName;
    private String creatorName;
}
