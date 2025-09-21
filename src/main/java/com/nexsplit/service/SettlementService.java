package com.nexsplit.service;

import com.nexsplit.model.view.SettlementHistoryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Nex-centric settlement operations.
 * 
 * This service provides comprehensive settlement functionality including
 * settlement execution, debt optimization, and settlement analytics
 * using the Nex-centric approach from the architecture document.
 * 
 * @author NexSplit Team
 * @version 2.0
 * @since 2.0
 */
public interface SettlementService {

    /**
     * Execute settlements for a specific Nex group.
     * Uses the Nex's settlement type (SIMPLIFIED or DETAILED) to determine
     * behavior.
     * 
     * @param nexId   The Nex group ID
     * @param request The settlement execution request
     * @param userId  The user executing the settlement
     * @return Settlement execution response
     */
    SettlementExecutionResponse executeSettlements(String nexId, SettlementExecutionRequest request, String userId);

    /**
     * Get available settlements for a specific Nex group.
     * Shows what settlements can be executed based on the Nex's settlement type.
     * 
     * @param nexId          The Nex group ID
     * @param settlementType The settlement type (SIMPLIFIED or DETAILED)
     * @param userId         The requesting user
     * @return Available settlements response
     */
    AvailableSettlementsResponse getAvailableSettlements(String nexId, String settlementType, String userId);

    /**
     * Get settlement history for a specific Nex group.
     * 
     * @param nexId    The Nex group ID
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    Page<SettlementHistoryView> getSettlementHistoryByNexId(String nexId, Pageable pageable);

    /**
     * Get settlement history for a specific user across all Nex groups.
     * 
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Page of settlement history records
     */
    Page<SettlementHistoryView> getSettlementHistoryByUserId(String userId, Pageable pageable);

    /**
     * Get settlement analytics for a specific Nex group.
     * 
     * @param nexId The Nex group ID
     * @return Settlement analytics
     */
    SettlementAnalytics getSettlementAnalyticsByNexId(String nexId);

    /**
     * Get settlement analytics for a specific user.
     * 
     * @param userId The user ID
     * @return Settlement analytics
     */
    SettlementAnalytics getSettlementAnalyticsByUserId(String userId);

    /**
     * Get settlement summary for a specific Nex group.
     * 
     * @param nexId The Nex group ID
     * @return Settlement summary
     */
    SettlementSummary getSettlementSummaryByNexId(String nexId);

    /**
     * Get settlement summary for a specific user.
     * 
     * @param userId The user ID
     * @return Settlement summary
     */
    SettlementSummary getSettlementSummaryByUserId(String userId);

    /**
     * Calculate net balances for a specific Nex group.
     * Used internally for settlement calculations.
     * 
     * @param nexId The Nex group ID
     * @return Map of user ID to net balance
     */
    java.util.Map<String, BigDecimal> calculateNetBalances(String nexId);

    /**
     * Generate simplified settlements for a specific Nex group.
     * Calculates minimum transactions to settle all debts.
     * 
     * @param nexId The Nex group ID
     * @return List of simplified settlement transactions
     */
    List<SettlementTransaction> generateSimplifiedSettlements(String nexId);

    /**
     * Generate detailed settlements for a specific Nex group.
     * Shows all individual debt transactions.
     * 
     * @param nexId The Nex group ID
     * @return List of detailed settlement transactions
     */
    List<SettlementTransaction> generateDetailedSettlements(String nexId);

    /**
     * Settlement execution request data class.
     */
    class SettlementExecutionRequest {
        private String settlementType; // SIMPLIFIED, DETAILED
        private List<String> settlementIds; // IDs of settlement transactions to execute
        private String paymentMethod; // Uses existing payment_method field
        private String notes; // Uses existing notes field
        private LocalDateTime settlementDate; // Uses existing settled_at field
        private boolean settleAll; // For simplified: settle all available settlements

        // Constructors
        public SettlementExecutionRequest() {
        }

        public SettlementExecutionRequest(String settlementType, List<String> settlementIds, String paymentMethod,
                String notes, LocalDateTime settlementDate, boolean settleAll) {
            this.settlementType = settlementType;
            this.settlementIds = settlementIds;
            this.paymentMethod = paymentMethod;
            this.notes = notes;
            this.settlementDate = settlementDate;
            this.settleAll = settleAll;
        }

        // Getters and setters
        public String getSettlementType() {
            return settlementType;
        }

        public void setSettlementType(String settlementType) {
            this.settlementType = settlementType;
        }

        public List<String> getSettlementIds() {
            return settlementIds;
        }

        public void setSettlementIds(List<String> settlementIds) {
            this.settlementIds = settlementIds;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public LocalDateTime getSettlementDate() {
            return settlementDate;
        }

        public void setSettlementDate(LocalDateTime settlementDate) {
            this.settlementDate = settlementDate;
        }

        public boolean isSettleAll() {
            return settleAll;
        }

        public void setSettleAll(boolean settleAll) {
            this.settleAll = settleAll;
        }
    }

    /**
     * Settlement execution response data class.
     */
    class SettlementExecutionResponse {
        private List<SettlementTransaction> executedSettlements;
        private List<SettlementTransaction> remainingSettlements;
        private BigDecimal totalSettledAmount;
        private int settledCount;
        private int remainingCount;
        private String nexId;
        private LocalDateTime executionDate;

        // Constructors
        public SettlementExecutionResponse() {
        }

        public SettlementExecutionResponse(List<SettlementTransaction> executedSettlements,
                List<SettlementTransaction> remainingSettlements,
                BigDecimal totalSettledAmount, int settledCount, int remainingCount,
                String nexId, LocalDateTime executionDate) {
            this.executedSettlements = executedSettlements;
            this.remainingSettlements = remainingSettlements;
            this.totalSettledAmount = totalSettledAmount;
            this.settledCount = settledCount;
            this.remainingCount = remainingCount;
            this.nexId = nexId;
            this.executionDate = executionDate;
        }

        // Getters and setters
        public List<SettlementTransaction> getExecutedSettlements() {
            return executedSettlements;
        }

        public void setExecutedSettlements(List<SettlementTransaction> executedSettlements) {
            this.executedSettlements = executedSettlements;
        }

        public List<SettlementTransaction> getRemainingSettlements() {
            return remainingSettlements;
        }

        public void setRemainingSettlements(List<SettlementTransaction> remainingSettlements) {
            this.remainingSettlements = remainingSettlements;
        }

        public BigDecimal getTotalSettledAmount() {
            return totalSettledAmount;
        }

        public void setTotalSettledAmount(BigDecimal totalSettledAmount) {
            this.totalSettledAmount = totalSettledAmount;
        }

        public int getSettledCount() {
            return settledCount;
        }

        public void setSettledCount(int settledCount) {
            this.settledCount = settledCount;
        }

        public int getRemainingCount() {
            return remainingCount;
        }

        public void setRemainingCount(int remainingCount) {
            this.remainingCount = remainingCount;
        }

        public String getNexId() {
            return nexId;
        }

        public void setNexId(String nexId) {
            this.nexId = nexId;
        }

        public LocalDateTime getExecutionDate() {
            return executionDate;
        }

        public void setExecutionDate(LocalDateTime executionDate) {
            this.executionDate = executionDate;
        }
    }

    /**
     * Available settlements response data class.
     */
    class AvailableSettlementsResponse {
        private List<SettlementTransaction> availableSettlements;
        private String settlementType;
        private String nexId;
        private int totalAvailable;
        private BigDecimal totalAmount;

        // Constructors
        public AvailableSettlementsResponse() {
        }

        public AvailableSettlementsResponse(List<SettlementTransaction> availableSettlements, String settlementType,
                String nexId, int totalAvailable, BigDecimal totalAmount) {
            this.availableSettlements = availableSettlements;
            this.settlementType = settlementType;
            this.nexId = nexId;
            this.totalAvailable = totalAvailable;
            this.totalAmount = totalAmount;
        }

        // Getters and setters
        public List<SettlementTransaction> getAvailableSettlements() {
            return availableSettlements;
        }

        public void setAvailableSettlements(List<SettlementTransaction> availableSettlements) {
            this.availableSettlements = availableSettlements;
        }

        public String getSettlementType() {
            return settlementType;
        }

        public void setSettlementType(String settlementType) {
            this.settlementType = settlementType;
        }

        public String getNexId() {
            return nexId;
        }

        public void setNexId(String nexId) {
            this.nexId = nexId;
        }

        public int getTotalAvailable() {
            return totalAvailable;
        }

        public void setTotalAvailable(int totalAvailable) {
            this.totalAvailable = totalAvailable;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    /**
     * Settlement transaction data class.
     */
    class SettlementTransaction {
        private String id; // Debt ID
        private String fromUserId; // Debtor ID
        private String toUserId; // Creditor ID
        private BigDecimal amount;
        private SettlementType settlementType; // SIMPLIFIED, DETAILED
        private SettlementStatus status; // PENDING, SETTLED (based on settledAt)
        private List<String> relatedDebtIds; // For simplified: list of debt IDs
        private String expenseId; // From debt
        private String expenseTitle; // From related expense
        private String nexId; // Nex group ID
        private LocalDateTime createdAt;
        private LocalDateTime executedAt;

        // Constructors
        public SettlementTransaction() {
        }

        public SettlementTransaction(String id, String fromUserId, String toUserId, BigDecimal amount,
                SettlementType settlementType, SettlementStatus status, String nexId) {
            this.id = id;
            this.fromUserId = fromUserId;
            this.toUserId = toUserId;
            this.amount = amount;
            this.settlementType = settlementType;
            this.status = status;
            this.nexId = nexId;
            this.createdAt = LocalDateTime.now();
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFromUserId() {
            return fromUserId;
        }

        public void setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
        }

        public String getToUserId() {
            return toUserId;
        }

        public void setToUserId(String toUserId) {
            this.toUserId = toUserId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public SettlementType getSettlementType() {
            return settlementType;
        }

        public void setSettlementType(SettlementType settlementType) {
            this.settlementType = settlementType;
        }

        public SettlementStatus getStatus() {
            return status;
        }

        public void setStatus(SettlementStatus status) {
            this.status = status;
        }

        public List<String> getRelatedDebtIds() {
            return relatedDebtIds;
        }

        public void setRelatedDebtIds(List<String> relatedDebtIds) {
            this.relatedDebtIds = relatedDebtIds;
        }

        public String getExpenseId() {
            return expenseId;
        }

        public void setExpenseId(String expenseId) {
            this.expenseId = expenseId;
        }

        public String getExpenseTitle() {
            return expenseTitle;
        }

        public void setExpenseTitle(String expenseTitle) {
            this.expenseTitle = expenseTitle;
        }

        public String getNexId() {
            return nexId;
        }

        public void setNexId(String nexId) {
            this.nexId = nexId;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getExecutedAt() {
            return executedAt;
        }

        public void setExecutedAt(LocalDateTime executedAt) {
            this.executedAt = executedAt;
        }
    }

    /**
     * Settlement analytics data class.
     */
    class SettlementAnalytics {
        private int totalSettlements;
        private int settledCount;
        private int unsettledCount;
        private BigDecimal totalSettledAmount;
        private BigDecimal totalUnsettledAmount;
        private double averageSettlementTimeHours;
        private String nexId;
        private String userId; // null for nex analytics

        // Constructors
        public SettlementAnalytics() {
        }

        public SettlementAnalytics(int totalSettlements, int settledCount, int unsettledCount,
                BigDecimal totalSettledAmount, BigDecimal totalUnsettledAmount,
                double averageSettlementTimeHours, String nexId, String userId) {
            this.totalSettlements = totalSettlements;
            this.settledCount = settledCount;
            this.unsettledCount = unsettledCount;
            this.totalSettledAmount = totalSettledAmount;
            this.totalUnsettledAmount = totalUnsettledAmount;
            this.averageSettlementTimeHours = averageSettlementTimeHours;
            this.nexId = nexId;
            this.userId = userId;
        }

        // Getters and setters
        public int getTotalSettlements() {
            return totalSettlements;
        }

        public void setTotalSettlements(int totalSettlements) {
            this.totalSettlements = totalSettlements;
        }

        public int getSettledCount() {
            return settledCount;
        }

        public void setSettledCount(int settledCount) {
            this.settledCount = settledCount;
        }

        public int getUnsettledCount() {
            return unsettledCount;
        }

        public void setUnsettledCount(int unsettledCount) {
            this.unsettledCount = unsettledCount;
        }

        public BigDecimal getTotalSettledAmount() {
            return totalSettledAmount;
        }

        public void setTotalSettledAmount(BigDecimal totalSettledAmount) {
            this.totalSettledAmount = totalSettledAmount;
        }

        public BigDecimal getTotalUnsettledAmount() {
            return totalUnsettledAmount;
        }

        public void setTotalUnsettledAmount(BigDecimal totalUnsettledAmount) {
            this.totalUnsettledAmount = totalUnsettledAmount;
        }

        public double getAverageSettlementTimeHours() {
            return averageSettlementTimeHours;
        }

        public void setAverageSettlementTimeHours(double averageSettlementTimeHours) {
            this.averageSettlementTimeHours = averageSettlementTimeHours;
        }

        public String getNexId() {
            return nexId;
        }

        public void setNexId(String nexId) {
            this.nexId = nexId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    /**
     * Settlement summary data class.
     */
    class SettlementSummary {
        private String nexId;
        private String userId; // null for nex summary
        private int totalDebts;
        private int settledDebts;
        private int unsettledDebts;
        private BigDecimal totalAmount;
        private BigDecimal settledAmount;
        private BigDecimal unsettledAmount;
        private LocalDateTime lastSettlementDate;

        // Constructors
        public SettlementSummary() {
        }

        public SettlementSummary(String nexId, String userId, int totalDebts, int settledDebts, int unsettledDebts,
                BigDecimal totalAmount, BigDecimal settledAmount, BigDecimal unsettledAmount,
                LocalDateTime lastSettlementDate) {
            this.nexId = nexId;
            this.userId = userId;
            this.totalDebts = totalDebts;
            this.settledDebts = settledDebts;
            this.unsettledDebts = unsettledDebts;
            this.totalAmount = totalAmount;
            this.settledAmount = settledAmount;
            this.unsettledAmount = unsettledAmount;
            this.lastSettlementDate = lastSettlementDate;
        }

        // Getters and setters
        public String getNexId() {
            return nexId;
        }

        public void setNexId(String nexId) {
            this.nexId = nexId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getTotalDebts() {
            return totalDebts;
        }

        public void setTotalDebts(int totalDebts) {
            this.totalDebts = totalDebts;
        }

        public int getSettledDebts() {
            return settledDebts;
        }

        public void setSettledDebts(int settledDebts) {
            this.settledDebts = settledDebts;
        }

        public int getUnsettledDebts() {
            return unsettledDebts;
        }

        public void setUnsettledDebts(int unsettledDebts) {
            this.unsettledDebts = unsettledDebts;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getSettledAmount() {
            return settledAmount;
        }

        public void setSettledAmount(BigDecimal settledAmount) {
            this.settledAmount = settledAmount;
        }

        public BigDecimal getUnsettledAmount() {
            return unsettledAmount;
        }

        public void setUnsettledAmount(BigDecimal unsettledAmount) {
            this.unsettledAmount = unsettledAmount;
        }

        public LocalDateTime getLastSettlementDate() {
            return lastSettlementDate;
        }

        public void setLastSettlementDate(LocalDateTime lastSettlementDate) {
            this.lastSettlementDate = lastSettlementDate;
        }
    }

    /**
     * Settlement type enum.
     */
    enum SettlementType {
        SIMPLIFIED, DETAILED
    }

    /**
     * Settlement status enum.
     */
    enum SettlementStatus {
        PENDING, SETTLED
    }
}