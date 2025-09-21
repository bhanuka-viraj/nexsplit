package com.nexsplit.mapper.expense;

import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.model.Split;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Split entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Split entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SplitMapStruct {

    /**
     * Convert Split entity to SplitDto.
     * 
     * @param split The split entity
     * @return SplitDto
     */
    @Mapping(target = "userId", source = "id.userId")
    @Mapping(target = "userName", source = "user.fullName")
    ExpenseDto.SplitDto toDto(Split split);

    /**
     * Convert list of Split entities to list of SplitDto.
     * 
     * @param splits List of split entities
     * @return List of SplitDto
     */
    List<ExpenseDto.SplitDto> toDtoList(List<Split> splits);
}
