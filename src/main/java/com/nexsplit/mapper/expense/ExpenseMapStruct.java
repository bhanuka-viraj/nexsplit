package com.nexsplit.mapper.expense;

import com.nexsplit.dto.expense.CreateExpenseRequest;
import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.dto.expense.ExpenseSummaryDto;
import com.nexsplit.dto.expense.UpdateExpenseRequest;
import com.nexsplit.model.Expense;
import com.nexsplit.model.Debt;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Expense entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Expense entities and their corresponding DTOs.
 * It handles complex mappings including nested objects like splits and debts.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {
        SplitMapStruct.class, com.nexsplit.mapper.debt.DebtMapStruct.class })
public interface ExpenseMapStruct {

    /**
     * Convert CreateExpenseRequest to Expense entity.
     * 
     * @param request The create request DTO
     * @return Expense entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "nex", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "payer", ignore = true)
    @Mapping(target = "splits", ignore = true)
    @Mapping(target = "debts", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    Expense toEntity(CreateExpenseRequest request);

    /**
     * Convert Expense entity to ExpenseDto.
     * 
     * @param expense The expense entity
     * @return ExpenseDto
     */
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "nexName", source = "nex.name")
    @Mapping(target = "createdByName", source = "creator.fullName")
    @Mapping(target = "payerName", source = "payer.fullName")
    @Mapping(target = "splits", source = "splits")
    @Mapping(target = "debts", source = "debts")
    ExpenseDto toDto(Expense expense);

    /**
     * Convert Expense entity to ExpenseSummaryDto.
     * 
     * @param expense The expense entity
     * @return ExpenseSummaryDto
     */
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "payerName", source = "payer.fullName")
    @Mapping(target = "createdByName", source = "creator.fullName")
    ExpenseSummaryDto toSummaryDto(Expense expense);

    /**
     * Update Expense entity from UpdateExpenseRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request The update request DTO
     * @param expense The expense entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "nex", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "payer", ignore = true)
    @Mapping(target = "splits", ignore = true)
    @Mapping(target = "debts", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateEntityFromRequest(UpdateExpenseRequest request, @MappingTarget Expense expense);

    /**
     * Convert list of Expense entities to list of ExpenseDto.
     * 
     * @param expenses List of expense entities
     * @return List of ExpenseDto
     */
    List<ExpenseDto> toDtoList(List<Expense> expenses);

    /**
     * Convert list of Expense entities to list of ExpenseSummaryDto.
     * 
     * @param expenses List of expense entities
     * @return List of ExpenseSummaryDto
     */
    List<ExpenseSummaryDto> toSummaryDtoList(List<Expense> expenses);

    /**
     * Check if expense is fully settled.
     * 
     * @param expense The expense entity
     * @return true if fully settled, false otherwise
     */
    default boolean isFullySettled(Expense expense) {
        if (expense.getDebts() == null || expense.getDebts().isEmpty()) {
            return true;
        }
        return expense.getDebts().stream()
                .allMatch(debt -> debt.getSettledAt() != null);
    }

    /**
     * Calculate unsettled amount for expense.
     * 
     * @param expense The expense entity
     * @return Total unsettled amount
     */
    default java.math.BigDecimal calculateUnsettledAmount(Expense expense) {
        if (expense.getDebts() == null || expense.getDebts().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return expense.getDebts().stream()
                .filter(debt -> debt.getSettledAt() == null)
                .map(Debt::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
