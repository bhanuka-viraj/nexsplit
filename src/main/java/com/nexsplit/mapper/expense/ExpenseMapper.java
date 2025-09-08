package com.nexsplit.mapper.expense;

import com.nexsplit.dto.expense.CreateExpenseRequest;
import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.dto.expense.ExpenseSummaryDto;
import com.nexsplit.dto.expense.UpdateExpenseRequest;
import com.nexsplit.model.Expense;
import com.nexsplit.model.Split;
import com.nexsplit.model.Debt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Expense entities and DTOs.
 * Handles mapping of expense data including splits and debts.
 */
@Component
public class ExpenseMapper {

    /**
     * Convert CreateExpenseRequest to Expense entity.
     * 
     * @param request The create request
     * @return Expense entity
     */
    public Expense toEntity(CreateExpenseRequest request) {
        if (request == null) {
            return null;
        }

        return Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .categoryId(request.getCategoryId())
                .description(request.getDescription())
                .nexId(request.getNexId())
                .payerId(request.getPayerId())
                .splitType(request.getSplitType())
                .isInitialPayerHas(request.getIsInitialPayerHas())
                .build();
    }

    /**
     * Convert Expense entity to ExpenseDto.
     * 
     * @param expense The expense entity
     * @return ExpenseDto
     */
    public ExpenseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }

        return ExpenseDto.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .categoryId(expense.getCategoryId())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .description(expense.getDescription())
                .nexId(expense.getNexId())
                .nexName(expense.getNex() != null ? expense.getNex().getName() : null)
                .createdBy(expense.getCreatedBy())
                .createdByName(expense.getCreator() != null ? expense.getCreator().getFullName() : null)
                .payerId(expense.getPayerId())
                .payerName(expense.getPayer() != null ? expense.getPayer().getFullName() : null)
                .splitType(expense.getSplitType())
                .isInitialPayerHas(expense.getIsInitialPayerHas())
                .createdAt(expense.getCreatedAt())
                .modifiedAt(expense.getModifiedAt())
                .splits(mapSplitsToDto(expense.getSplits()))
                .debts(mapDebtsToDto(expense.getDebts()))
                .build();
    }

    /**
     * Convert Expense entity to ExpenseSummaryDto.
     * 
     * @param expense The expense entity
     * @return ExpenseSummaryDto
     */
    public ExpenseSummaryDto toSummaryDto(Expense expense) {
        if (expense == null) {
            return null;
        }

        // Calculate split count
        int splitCount = expense.getSplits() != null ? expense.getSplits().size() : 0;

        // Calculate unsettled amount
        java.math.BigDecimal unsettledAmount = java.math.BigDecimal.ZERO;
        boolean isFullySettled = true;
        if (expense.getDebts() != null) {
            unsettledAmount = expense.getDebts().stream()
                    .filter(debt -> debt.getSettledAt() == null)
                    .map(Debt::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            isFullySettled = unsettledAmount.compareTo(java.math.BigDecimal.ZERO) == 0;
        }

        return ExpenseSummaryDto.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .description(expense.getDescription())
                .payerName(expense.getPayer() != null ? expense.getPayer().getFullName() : null)
                .createdByName(expense.getCreator() != null ? expense.getCreator().getFullName() : null)
                .createdAt(expense.getCreatedAt())
                .modifiedAt(expense.getModifiedAt())
                .splitCount(splitCount)
                .isFullySettled(isFullySettled)
                .unsettledAmount(unsettledAmount)
                .build();
    }

    /**
     * Update Expense entity from UpdateExpenseRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request The update request
     * @param expense The expense entity to update
     */
    public void updateEntityFromRequest(UpdateExpenseRequest request, Expense expense) {
        if (request == null || expense == null) {
            return;
        }

        if (request.getTitle() != null) {
            expense.setTitle(request.getTitle());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            expense.setCurrency(request.getCurrency());
        }
        if (request.getCategoryId() != null) {
            expense.setCategoryId(request.getCategoryId());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getPayerId() != null) {
            expense.setPayerId(request.getPayerId());
        }
        if (request.getSplitType() != null) {
            expense.setSplitType(request.getSplitType());
        }
        if (request.getIsInitialPayerHas() != null) {
            expense.setIsInitialPayerHas(request.getIsInitialPayerHas());
        }
    }

    /**
     * Map splits to DTOs.
     * 
     * @param splits List of split entities
     * @return List of split DTOs
     */
    private List<ExpenseDto.SplitDto> mapSplitsToDto(List<Split> splits) {
        if (splits == null) {
            return null;
        }

        return splits.stream()
                .map(this::mapSplitToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map a single split to DTO.
     * 
     * @param split The split entity
     * @return Split DTO
     */
    private ExpenseDto.SplitDto mapSplitToDto(Split split) {
        if (split == null) {
            return null;
        }

        return ExpenseDto.SplitDto.builder()
                .userId(split.getId().getUserId())
                .userName(split.getUser() != null ? split.getUser().getFullName() : null)
                .percentage(split.getPercentage())
                .amount(split.getAmount())
                .notes(split.getNotes())
                .createdAt(split.getCreatedAt())
                .modifiedAt(split.getModifiedAt())
                .build();
    }

    /**
     * Map debts to DTOs.
     * 
     * @param debts List of debt entities
     * @return List of debt DTOs
     */
    private List<ExpenseDto.DebtDto> mapDebtsToDto(List<Debt> debts) {
        if (debts == null) {
            return null;
        }

        return debts.stream()
                .map(this::mapDebtToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map a single debt to DTO.
     * 
     * @param debt The debt entity
     * @return Debt DTO
     */
    private ExpenseDto.DebtDto mapDebtToDto(Debt debt) {
        if (debt == null) {
            return null;
        }

        return ExpenseDto.DebtDto.builder()
                .id(debt.getId())
                .debtorId(debt.getDebtorId())
                .debtorName(debt.getDebtor() != null ? debt.getDebtor().getFullName() : null)
                .creditorId(debt.getCreditorId())
                .creditorName(debt.getCreditor() != null ? debt.getCreditor().getFullName() : null)
                .amount(debt.getAmount())
                .paymentMethod(debt.getPaymentMethod())
                .notes(debt.getNotes())
                .settledAt(debt.getSettledAt())
                .createdAt(debt.getCreatedAt())
                .modifiedAt(debt.getModifiedAt())
                .build();
    }
}
