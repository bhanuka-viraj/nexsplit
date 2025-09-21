package com.nexsplit.dto.nex;

import com.nexsplit.model.Nex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for nex analytics data from database view.
 * 
 * This DTO represents data from the nex_analytics_view and provides
 * comprehensive analytics and insights for nex groups.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NexAnalyticsDto {

    private String nexId;
    private String nexName;
    private String description;
    private Nex.SettlementType settlementType;
    private Nex.NexType nexType;
    private Boolean isArchived;
    private String createdBy;
    private String creatorName;
    private String creatorEmail;
    private LocalDateTime nexCreatedAt;
    private LocalDateTime nexModifiedAt;

    // Member statistics
    private Integer totalMembers;
    private Integer activeMembers;
    private Integer adminCount;

    // Expense statistics
    private Integer totalExpenses;
    private BigDecimal totalExpenseAmount;
    private BigDecimal averageExpenseAmount;
    private BigDecimal maxExpenseAmount;
    private BigDecimal minExpenseAmount;

    // Debt statistics
    private Integer totalDebts;
    private Integer unsettledDebts;
    private BigDecimal unsettledDebtAmount;

    // Category statistics
    private Integer totalCategories;
    private Integer defaultCategories;

    // Bill statistics
    private Integer totalBills;
    private Integer paidBills;
    private BigDecimal totalBillAmount;

    // Recent activity
    private LocalDateTime lastExpenseDate;
    private LocalDateTime lastDebtDate;
    private LocalDateTime lastBillDate;
}
