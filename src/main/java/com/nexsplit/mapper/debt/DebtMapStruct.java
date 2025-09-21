package com.nexsplit.mapper.debt;

import com.nexsplit.dto.debt.DebtDto;
import com.nexsplit.dto.debt.DebtSummaryDto;
import com.nexsplit.dto.debt.CreateDebtRequest;
import com.nexsplit.dto.debt.UpdateDebtRequest;
import com.nexsplit.model.Debt;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Debt entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Debt entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DebtMapStruct {

    /**
     * Convert CreateDebtRequest to Debt entity.
     * 
     * @param request The create request DTO
     * @return Debt entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "settledAt", ignore = true)
    @Mapping(target = "debtor", ignore = true)
    @Mapping(target = "creditor", ignore = true)
    @Mapping(target = "expense", ignore = true)
    Debt toEntity(CreateDebtRequest request);

    /**
     * Convert Debt entity to DebtDto.
     * 
     * @param debt The debt entity
     * @return DebtDto
     */
    @Mapping(target = "debtorName", source = "debtor.fullName")
    @Mapping(target = "debtorEmail", source = "debtor.email")
    @Mapping(target = "creditorName", source = "creditor.fullName")
    @Mapping(target = "creditorEmail", source = "creditor.email")
    @Mapping(target = "expenseTitle", source = "expense.title")
    @Mapping(target = "isSettled", expression = "java(debt.isSettled())")
    DebtDto toDto(Debt debt);

    /**
     * Convert Debt entity to DebtSummaryDto.
     * 
     * @param debt The debt entity
     * @return DebtSummaryDto
     */
    @Mapping(target = "debtorName", source = "debtor.fullName")
    @Mapping(target = "creditorName", source = "creditor.fullName")
    @Mapping(target = "expenseTitle", source = "expense.title")
    @Mapping(target = "isSettled", expression = "java(debt.isSettled())")
    DebtSummaryDto toSummaryDto(Debt debt);

    /**
     * Update Debt entity from UpdateDebtRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request The update request DTO
     * @param debt    The debt entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "debtorId", ignore = true)
    @Mapping(target = "creditorId", ignore = true)
    @Mapping(target = "expenseId", ignore = true)
    @Mapping(target = "settledAt", ignore = true)
    @Mapping(target = "debtor", ignore = true)
    @Mapping(target = "creditor", ignore = true)
    @Mapping(target = "expense", ignore = true)
    void updateEntityFromRequest(UpdateDebtRequest request, @MappingTarget Debt debt);

    /**
     * Convert list of Debt entities to list of DebtDto.
     * 
     * @param debts List of debt entities
     * @return List of DebtDto
     */
    List<DebtDto> toDtoList(List<Debt> debts);

    /**
     * Convert list of Debt entities to list of DebtSummaryDto.
     * 
     * @param debts List of debt entities
     * @return List of DebtSummaryDto
     */
    List<DebtSummaryDto> toSummaryDtoList(List<Debt> debts);
}
