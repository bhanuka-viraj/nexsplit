package com.nexsplit.dto.bill;

import com.nexsplit.model.Bill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary DTO for bill information.
 * Contains essential bill data for list views and summaries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillSummaryDto {

    private String id;
    private String nexId;
    private String nexName;
    private String createdBy;
    private String creatorName;
    private String title;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime dueDate;
    private Bill.Frequency frequency;
    private Boolean isRecurring;
    private Boolean isPaid;
    private LocalDateTime createdAt;
}
