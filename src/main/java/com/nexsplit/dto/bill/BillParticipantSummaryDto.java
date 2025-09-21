package com.nexsplit.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary DTO for bill participant information.
 * Contains essential bill participant data for list views and summaries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillParticipantSummaryDto {

    private String billId;
    private String userId;
    private String userName;
    private String billTitle;
    private BigDecimal shareAmount;
    private Boolean paid;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private Boolean isPaid;
}
