package com.nexsplit.mapper.nex;

import com.nexsplit.dto.nex.CreateNexRequest;
import com.nexsplit.dto.nex.NexDto;
import com.nexsplit.dto.nex.NexSummaryDto;
import com.nexsplit.dto.nex.UpdateNexRequest;
import com.nexsplit.model.Nex;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Nex entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Nex entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NexMapStruct {

    /**
     * Convert CreateNexRequest to Nex entity.
     * 
     * @param request The create request DTO
     * @return Nex entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    Nex toEntity(CreateNexRequest request);

    /**
     * Convert Nex entity to NexDto.
     * 
     * @param nex The nex entity
     * @return NexDto
     */
    @Mapping(target = "creatorName", source = "creator.fullName")
    @Mapping(target = "memberCount", expression = "java(nex.getMembers() != null ? nex.getMembers().size() : 0)")
    @Mapping(target = "expenseCount", expression = "java(nex.getExpenses() != null ? nex.getExpenses().size() : 0)")
    @Mapping(target = "totalExpenseAmount", expression = "java(calculateTotalExpenseAmount(nex))")
    NexDto toDto(Nex nex);

    /**
     * Convert Nex entity to NexSummaryDto.
     * 
     * @param nex The nex entity
     * @return NexSummaryDto
     */
    @Mapping(target = "creatorName", source = "creator.fullName")
    NexSummaryDto toSummaryDto(Nex nex);

    /**
     * Update Nex entity from UpdateNexRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request The update request DTO
     * @param nex     The nex entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntityFromRequest(UpdateNexRequest request, @MappingTarget Nex nex);

    /**
     * Convert list of Nex entities to list of NexDto.
     * 
     * @param nexList List of nex entities
     * @return List of NexDto
     */
    List<NexDto> toDtoList(List<Nex> nexList);

    /**
     * Convert list of Nex entities to list of NexSummaryDto.
     * 
     * @param nexList List of nex entities
     * @return List of NexSummaryDto
     */
    List<NexSummaryDto> toSummaryDtoList(List<Nex> nexList);

    /**
     * Calculate total expense amount for a nex.
     * 
     * @param nex The nex entity
     * @return Total expense amount
     */
    default java.math.BigDecimal calculateTotalExpenseAmount(Nex nex) {
        if (nex.getExpenses() == null || nex.getExpenses().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return nex.getExpenses().stream()
                .map(expense -> expense.getAmount() != null ? expense.getAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
