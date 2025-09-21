package com.nexsplit.service.impl;

import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.model.Debt;
import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import com.nexsplit.model.view.SettlementHistoryView;
import com.nexsplit.repository.DebtRepository;
import com.nexsplit.repository.NexRepository;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.repository.SettlementRepository;
import com.nexsplit.service.SettlementService;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SettlementService for Nex-centric settlement operations.
 * 
 * This service provides comprehensive settlement functionality including
 * settlement execution, debt optimization, and settlement analytics
 * using the Nex-centric approach from the architecture document.
 * 
 * @author NexSplit Team
 * @version 2.0
 * @since 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private final DebtRepository debtRepository;
    private final NexRepository nexRepository;
    private final NexMemberRepository nexMemberRepository;
    private final SettlementRepository settlementRepository;

    @Override
    public SettlementExecutionResponse executeSettlements(String nexId, SettlementExecutionRequest request,
            String userId) {
        log.info("Executing settlements for Nex: {} by user: {} with type: {}", nexId, userId,
                request.getSettlementType());

        // Validate Nex exists and user is member
        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

        // TODO: Add Nex membership validation
        // if (!nexService.isMember(nexId, userId)) {
        // throw new BusinessException("User is not a member of this Nex",
        // ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
        // }

        // Check user role and settlement permissions based on Nex type
        boolean isAdmin = isUserAdmin(nexId, userId);
        boolean canSettleAll = isAdmin || request.isSettleAll();

        // Settlement permissions based on Nex type
        if (nex.getNexType() == Nex.NexType.PERSONAL) {
            // PERSONAL Nex: Users can only settle their own debts, admins can settle all
            if (!isAdmin && request.isSettleAll()) {
                throw new BusinessException("Only admins can settle all debts in a PERSONAL Nex",
                        ErrorCode.AUTHZ_SETTLEMENT_DENIED);
            }
        } else {
            // GROUP Nex: All users can settle all debts (group behavior)
            // No additional restrictions for settleAll in GROUP Nex
        }

        List<SettlementTransaction> executedSettlements = new ArrayList<>();
        List<SettlementTransaction> remainingSettlements = new ArrayList<>();

        // Execute settlements based on Nex's settlement type
        if ("SIMPLIFIED".equals(request.getSettlementType())) {
            executedSettlements = executeSimplifiedSettlements(nexId, request, userId);
        } else if ("DETAILED".equals(request.getSettlementType())) {
            executedSettlements = executeDetailedSettlements(nexId, request, userId);
        } else {
            // Use Nex's default settlement type
            if (nex.getSettlementType() == Nex.SettlementType.SIMPLIFIED) {
                executedSettlements = executeSimplifiedSettlements(nexId, request, userId);
            } else {
                executedSettlements = executeDetailedSettlements(nexId, request, userId);
            }
        }

        // Get remaining settlements
        remainingSettlements = getRemainingSettlements(nexId, request.getSettlementType());

        // Calculate total settled amount
        BigDecimal totalSettledAmount = executedSettlements.stream()
                .map(SettlementTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Log business event
        StructuredLoggingUtil.logBusinessEvent(
                "SETTLEMENTS_EXECUTED",
                userId,
                "EXECUTE_SETTLEMENTS",
                "SUCCESS",
                Map.of(
                        "nexId", nexId,
                        "settlementType", request.getSettlementType(),
                        "executedCount", executedSettlements.size(),
                        "totalSettledAmount", totalSettledAmount));

        return new SettlementExecutionResponse(
                executedSettlements,
                remainingSettlements,
                totalSettledAmount,
                executedSettlements.size(),
                remainingSettlements.size(),
                nexId,
                LocalDateTime.now());
    }

    @Override
    public AvailableSettlementsResponse getAvailableSettlements(String nexId, String settlementType, String userId) {
        log.info("Getting available settlements for Nex: {} with type: {} by user: {}", nexId, settlementType, userId);

        // Validate Nex exists and user is member
        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

        // TODO: Add Nex membership validation
        // if (!isUserMember(nexId, userId)) {
        // throw new BusinessException("User is not a member of this Nex",
        // ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
        // }

        List<SettlementTransaction> availableSettlements;

        if ("SIMPLIFIED".equals(settlementType)) {
            availableSettlements = generateSimplifiedSettlements(nexId);
        } else if ("DETAILED".equals(settlementType)) {
            availableSettlements = generateDetailedSettlements(nexId);
        } else {
            // Use Nex's default settlement type
            if (nex.getSettlementType() == Nex.SettlementType.SIMPLIFIED) {
                availableSettlements = generateSimplifiedSettlements(nexId);
            } else {
                availableSettlements = generateDetailedSettlements(nexId);
            }
        }

        // Filter settlements based on Nex type
        if (nex.getNexType() == Nex.NexType.PERSONAL) {
            // PERSONAL Nex: User sees only their own settlements
            availableSettlements = filterSettlementsForUser(availableSettlements, userId);
            log.debug("Filtered settlements for PERSONAL Nex - user {} can see {} settlements", userId,
                    availableSettlements.size());
        } else {
            // GROUP Nex: User sees all settlements
            log.debug("Showing all settlements for GROUP Nex - user {} can see {} settlements", userId,
                    availableSettlements.size());
        }

        // Calculate total amount
        BigDecimal totalAmount = availableSettlements.stream()
                .map(SettlementTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AvailableSettlementsResponse(
                availableSettlements,
                settlementType,
                nexId,
                availableSettlements.size(),
                totalAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementHistoryView> getSettlementHistoryByNexId(String nexId, Pageable pageable) {
        log.debug("Getting settlement history for Nex: {}", nexId);
        return settlementRepository.findByNexId(nexId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementHistoryView> getSettlementHistoryByUserId(String userId, Pageable pageable) {
        log.debug("Getting settlement history for user: {}", userId);
        return settlementRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementAnalytics getSettlementAnalyticsByNexId(String nexId) {
        log.debug("Getting settlement analytics for Nex: {}", nexId);

        // Get settlement history for the Nex
        List<SettlementHistoryView> settlements = settlementRepository.findByNexId(nexId);

        int totalSettlements = settlements.size();
        int settledCount = (int) settlements.stream().filter(s -> s.getSettledAt() != null).count();
        int unsettledCount = totalSettlements - settledCount;

        BigDecimal totalSettledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnsettledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() == null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average settlement time
        double averageSettlementTimeHours = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .mapToDouble(s -> {
                    if (s.getSettlementHours() != null) {
                        return s.getSettlementHours();
                    }
                    return 0.0;
                })
                .average()
                .orElse(0.0);

        return new SettlementAnalytics(
                totalSettlements,
                settledCount,
                unsettledCount,
                totalSettledAmount,
                totalUnsettledAmount,
                averageSettlementTimeHours,
                nexId,
                null);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementAnalytics getSettlementAnalyticsByUserId(String userId) {
        log.debug("Getting settlement analytics for user: {}", userId);

        // Get settlement history for the user
        List<SettlementHistoryView> settlements = settlementRepository.findByUserId(userId);

        int totalSettlements = settlements.size();
        int settledCount = (int) settlements.stream().filter(s -> s.getSettledAt() != null).count();
        int unsettledCount = totalSettlements - settledCount;

        BigDecimal totalSettledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnsettledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() == null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average settlement time
        double averageSettlementTimeHours = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .mapToDouble(s -> {
                    if (s.getSettlementHours() != null) {
                        return s.getSettlementHours();
                    }
                    return 0.0;
                })
                .average()
                .orElse(0.0);

        return new SettlementAnalytics(
                totalSettlements,
                settledCount,
                unsettledCount,
                totalSettledAmount,
                totalUnsettledAmount,
                averageSettlementTimeHours,
                null,
                userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementSummary getSettlementSummaryByNexId(String nexId) {
        log.debug("Getting settlement summary for Nex: {}", nexId);

        // Get settlement history for the Nex
        List<SettlementHistoryView> settlements = settlementRepository.findByNexId(nexId);

        int totalDebts = settlements.size();
        int settledDebts = (int) settlements.stream().filter(s -> s.getSettledAt() != null).count();
        int unsettledDebts = totalDebts - settledDebts;

        BigDecimal totalAmount = settlements.stream()
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal settledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unsettledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() == null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime lastSettlementDate = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .map(SettlementHistoryView::getSettledAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new SettlementSummary(
                nexId,
                null,
                totalDebts,
                settledDebts,
                unsettledDebts,
                totalAmount,
                settledAmount,
                unsettledAmount,
                lastSettlementDate);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementSummary getSettlementSummaryByUserId(String userId) {
        log.debug("Getting settlement summary for user: {}", userId);

        // Get settlement history for the user
        List<SettlementHistoryView> settlements = settlementRepository.findByUserId(userId);

        int totalDebts = settlements.size();
        int settledDebts = (int) settlements.stream().filter(s -> s.getSettledAt() != null).count();
        int unsettledDebts = totalDebts - settledDebts;

        BigDecimal totalAmount = settlements.stream()
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal settledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unsettledAmount = settlements.stream()
                .filter(s -> s.getSettledAt() == null)
                .map(SettlementHistoryView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime lastSettlementDate = settlements.stream()
                .filter(s -> s.getSettledAt() != null)
                .map(SettlementHistoryView::getSettledAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new SettlementSummary(
                null,
                userId,
                totalDebts,
                settledDebts,
                unsettledDebts,
                totalAmount,
                settledAmount,
                unsettledAmount,
                lastSettlementDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calculateNetBalances(String nexId) {
        log.debug("Calculating net balances for Nex: {}", nexId);

        // Get all unsettled debts for the Nex
        List<Debt> unsettledDebts = debtRepository.findUnsettledByNexId(nexId);

        Map<String, BigDecimal> netBalances = new HashMap<>();

        for (Debt debt : unsettledDebts) {
            // Add to debtor's negative balance
            netBalances.merge(debt.getDebtorId(), debt.getAmount().negate(), BigDecimal::add);

            // Add to creditor's positive balance
            netBalances.merge(debt.getCreditorId(), debt.getAmount(), BigDecimal::add);
        }

        return netBalances;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementTransaction> generateSimplifiedSettlements(String nexId) {
        log.debug("Generating simplified settlements for Nex: {}", nexId);

        // Calculate net balances
        Map<String, BigDecimal> netBalances = calculateNetBalances(nexId);

        // Separate creditors and debtors
        List<String> creditors = new ArrayList<>();
        List<String> debtors = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : netBalances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry.getKey());
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry.getKey());
            }
        }

        // Sort by absolute balance (largest first)
        creditors.sort((a, b) -> netBalances.get(b).compareTo(netBalances.get(a)));
        debtors.sort((a, b) -> netBalances.get(a).compareTo(netBalances.get(b)));

        List<SettlementTransaction> settlements = new ArrayList<>();

        // Generate minimum transactions
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            String creditor = creditors.get(0);
            String debtor = debtors.get(0);

            BigDecimal creditorBalance = netBalances.get(creditor);
            BigDecimal debtorBalance = netBalances.get(debtor).abs();

            // Calculate settlement amount
            BigDecimal settlementAmount = creditorBalance.min(debtorBalance);

            // Create settlement transaction
            settlements.add(new SettlementTransaction(
                    UUID.randomUUID().toString(),
                    debtor,
                    creditor,
                    settlementAmount,
                    SettlementType.SIMPLIFIED,
                    SettlementStatus.PENDING,
                    nexId));

            // Update balances
            creditorBalance = creditorBalance.subtract(settlementAmount);
            debtorBalance = debtorBalance.subtract(settlementAmount);

            netBalances.put(creditor, creditorBalance);
            netBalances.put(debtor, debtorBalance.negate());

            // Remove users with zero balance
            if (creditorBalance.compareTo(BigDecimal.ZERO) == 0) {
                creditors.remove(0);
            }
            if (debtorBalance.compareTo(BigDecimal.ZERO) == 0) {
                debtors.remove(0);
            }
        }

        log.info("Generated {} simplified settlements for Nex: {}", settlements.size(), nexId);
        return settlements;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementTransaction> generateDetailedSettlements(String nexId) {
        log.debug("Generating detailed settlements for Nex: {}", nexId);

        // Get all unsettled debts
        List<Debt> unsettledDebts = debtRepository.findUnsettledByNexId(nexId);

        // Group debts by creditor-debtor pairs
        Map<String, List<Debt>> debtGroups = new HashMap<>();

        for (Debt debt : unsettledDebts) {
            String key = debt.getCreditorId() + "->" + debt.getDebtorId();
            debtGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(debt);
        }

        List<SettlementTransaction> settlements = new ArrayList<>();

        // Create settlement for each group
        for (Map.Entry<String, List<Debt>> entry : debtGroups.entrySet()) {
            List<Debt> debts = entry.getValue();

            // Calculate total amount for this creditor-debtor pair
            BigDecimal totalAmount = debts.stream()
                    .map(Debt::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Get creditor and debtor IDs
            String[] ids = entry.getKey().split("->");
            String creditorId = ids[0];
            String debtorId = ids[1];

            // Create settlement transaction
            settlements.add(new SettlementTransaction(
                    UUID.randomUUID().toString(),
                    debtorId,
                    creditorId,
                    totalAmount,
                    SettlementType.DETAILED,
                    SettlementStatus.PENDING,
                    nexId));
        }

        log.info("Generated {} detailed settlements for Nex: {}", settlements.size(), nexId);
        return settlements;
    }

    /**
     * Execute simplified settlements.
     */
    private List<SettlementTransaction> executeSimplifiedSettlements(String nexId, SettlementExecutionRequest request,
            String userId) {
        log.info("Executing simplified settlements for Nex: {}", nexId);

        List<SettlementTransaction> settlements = generateSimplifiedSettlements(nexId);
        List<SettlementTransaction> executedSettlements = new ArrayList<>();

        if (request.isSettleAll()) {
            // Settle all available settlements
            for (SettlementTransaction settlement : settlements) {
                if (executeSettlementTransaction(settlement, request, userId)) {
                    executedSettlements.add(settlement);
                }
            }
        } else if (request.getSettlementIds() != null && !request.getSettlementIds().isEmpty()) {
            // Validate user can settle these specific settlement transactions
            validateUserCanSettleSettlements(userId, request.getSettlementIds());

            // Settle specific settlement transactions
            for (String settlementId : request.getSettlementIds()) {
                // Find the settlement transaction by ID
                SettlementTransaction settlement = settlements.stream()
                        .filter(s -> s.getId().equals(settlementId))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("Settlement transaction not found: " + settlementId,
                                ErrorCode.DEBT_NOT_FOUND));

                // Execute the settlement transaction
                if (executeSettlementTransaction(settlement, request, userId)) {
                    executedSettlements.add(settlement);
                }
            }
        }

        return executedSettlements;
    }

    /**
     * Execute detailed settlements.
     */
    private List<SettlementTransaction> executeDetailedSettlements(String nexId, SettlementExecutionRequest request,
            String userId) {
        log.info("Executing detailed settlements for Nex: {}", nexId);

        List<SettlementTransaction> settlements = generateDetailedSettlements(nexId);
        List<SettlementTransaction> executedSettlements = new ArrayList<>();

        if (request.isSettleAll()) {
            // Settle all available settlements
            for (SettlementTransaction settlement : settlements) {
                if (executeSettlementTransaction(settlement, request, userId)) {
                    executedSettlements.add(settlement);
                }
            }
        } else if (request.getSettlementIds() != null && !request.getSettlementIds().isEmpty()) {
            // Validate user can settle these specific settlement transactions
            validateUserCanSettleSettlements(userId, request.getSettlementIds());

            // Settle specific settlement transactions
            for (String settlementId : request.getSettlementIds()) {
                // Find the settlement transaction by ID
                SettlementTransaction settlement = settlements.stream()
                        .filter(s -> s.getId().equals(settlementId))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("Settlement transaction not found: " + settlementId,
                                ErrorCode.DEBT_NOT_FOUND));

                // Execute the settlement transaction
                if (executeSettlementTransaction(settlement, request, userId)) {
                    executedSettlements.add(settlement);
                }
            }
        }

        return executedSettlements;
    }

    /**
     * Execute a settlement transaction.
     */
    private boolean executeSettlementTransaction(SettlementTransaction settlement, SettlementExecutionRequest request,
            String userId) {
        try {
            // Find and settle debts between the two users
            List<Debt> debtsToSettle = debtRepository.findUnsettledDebtsBetweenUsers(
                    settlement.getFromUserId(),
                    settlement.getToUserId(),
                    settlement.getNexId());

            if (debtsToSettle.isEmpty()) {
                log.warn("No unsettled debts found between users {} and {} in Nex {}",
                        settlement.getFromUserId(), settlement.getToUserId(), settlement.getNexId());
                return false;
            }

            // Mark debts as settled
            LocalDateTime settledAt = request.getSettlementDate() != null ? request.getSettlementDate()
                    : LocalDateTime.now();
            for (Debt debt : debtsToSettle) {
                debt.markAsSettled(settledAt);
                debt.setPaymentMethod(request.getPaymentMethod());
                debt.setNotes(request.getNotes());
                debtRepository.save(debt);
            }

            settlement.setStatus(SettlementStatus.SETTLED);
            settlement.setExecutedAt(settledAt);

            return true;
        } catch (Exception e) {
            log.error("Failed to execute settlement transaction: {}", settlement.getId(), e);
            return false;
        }
    }

    /**
     * Get remaining settlements.
     */
    private List<SettlementTransaction> getRemainingSettlements(String nexId, String settlementType) {
        // Get all unsettled debts for the Nex
        List<Debt> unsettledDebts = debtRepository.findUnsettledByNexId(nexId);

        return unsettledDebts.stream()
                .map(debt -> new SettlementTransaction(
                        debt.getId(),
                        debt.getDebtorId(),
                        debt.getCreditorId(),
                        debt.getAmount(),
                        "SIMPLIFIED".equals(settlementType) ? SettlementType.SIMPLIFIED : SettlementType.DETAILED,
                        SettlementStatus.PENDING,
                        nexId))
                .collect(Collectors.toList());
    }

    /**
     * Check if user is admin of the Nex.
     */
    private boolean isUserAdmin(String nexId, String userId) {
        return nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .map(member -> member.getRole() == NexMember.MemberRole.ADMIN)
                .orElse(false);
    }

    /**
     * Check if user is member of the Nex.
     */
    private boolean isUserMember(String nexId, String userId) {
        return nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .map(member -> member.getStatus() == NexMember.MemberStatus.ACTIVE)
                .orElse(false);
    }

    /**
     * Validate that user can settle specific debts.
     */
    private void validateUserCanSettleDebts(String userId, List<String> debtIds) {
        for (String debtId : debtIds) {
            Debt debt = debtRepository.findById(debtId)
                    .orElseThrow(() -> EntityNotFoundException.debtNotFound(debtId));

            // User can only settle debts where they are debtor or creditor
            if (!userId.equals(debt.getDebtorId()) && !userId.equals(debt.getCreditorId())) {
                throw new BusinessException(
                        String.format("User %s cannot settle debt %s - not involved in this debt", userId, debtId),
                        ErrorCode.AUTHZ_SETTLEMENT_DENIED);
            }
        }
    }

    /**
     * Validate that user can settle specific settlement transactions.
     */
    private void validateUserCanSettleSettlements(String userId, List<String> settlementIds) {
        // For now, we'll validate that the user is involved in the settlement
        // transactions
        // This is a simplified validation - in a real implementation, you might want to
        // validate against the actual settlement transaction data
        log.debug("Validating user {} can settle settlement transactions: {}", userId, settlementIds);
        // TODO: Implement proper validation based on settlement transaction data
    }

    /**
     * Filter settlements to show only those where the user is involved (debtor or
     * creditor).
     * Used for PERSONAL Nex types where users should only see their own
     * settlements.
     */
    private List<SettlementTransaction> filterSettlementsForUser(List<SettlementTransaction> settlements,
            String userId) {
        return settlements.stream()
                .filter(settlement -> userId.equals(settlement.getFromUserId())
                        || userId.equals(settlement.getToUserId()))
                .collect(Collectors.toList());
    }
}
