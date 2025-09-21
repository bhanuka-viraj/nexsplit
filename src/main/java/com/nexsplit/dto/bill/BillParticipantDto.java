package com.nexsplit.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for bill participant data.
 * Contains complete bill participant information.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillParticipantDto {

    private String billId;
    private String userId;
    private String userName;
    private String userEmail;
    private String billTitle;
    private BigDecimal shareAmount;
    private Boolean paid;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Boolean isPaid;
}
