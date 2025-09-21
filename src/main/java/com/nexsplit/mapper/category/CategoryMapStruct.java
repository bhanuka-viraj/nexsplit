package com.nexsplit.mapper.category;

import com.nexsplit.dto.category.CreateCategoryRequest;
import com.nexsplit.dto.category.UpdateCategoryRequest;
import com.nexsplit.dto.category.CategoryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.model.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Category entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Category entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapStruct {

    /**
     * Convert CreateCategoryRequest to Category entity.
     * 
     * @param request The create request DTO
     * @return Category entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "nex", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    /**
     * Convert Category entity to CategoryDto.
     * 
     * @param category The category entity
     * @return CategoryDto
     */
    @Mapping(target = "creatorName", source = "creator.fullName")
    @Mapping(target = "nexName", source = "nex.name")
    @Mapping(target = "expenseCount", expression = "java(category.getExpenses() != null ? (long) category.getExpenses().size() : 0L)")
    @Mapping(target = "totalExpenseAmount", expression = "java(calculateTotalExpenseAmount(category))")
    CategoryDto toDto(Category category);

    /**
     * Convert Category entity to CategorySummaryDto.
     * 
     * @param category The category entity
     * @return CategorySummaryDto
     */
    @Mapping(target = "creatorName", source = "creator.fullName")
    @Mapping(target = "nexName", source = "nex.name")
    CategorySummaryDto toSummaryDto(Category category);

    /**
     * Update Category entity from UpdateCategoryRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request  The update request DTO
     * @param category The category entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "nexId", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "nex", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget Category category);

    /**
     * Convert list of Category entities to list of CategoryDto.
     * 
     * @param categories List of category entities
     * @return List of CategoryDto
     */
    List<CategoryDto> toDtoList(List<Category> categories);

    /**
     * Convert list of Category entities to list of CategorySummaryDto.
     * 
     * @param categories List of category entities
     * @return List of CategorySummaryDto
     */
    List<CategorySummaryDto> toSummaryDtoList(List<Category> categories);

    /**
     * Calculate total expense amount for a category.
     * 
     * @param category The category entity
     * @return Total expense amount
     */
    default java.math.BigDecimal calculateTotalExpenseAmount(Category category) {
        if (category.getExpenses() == null || category.getExpenses().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return category.getExpenses().stream()
                .map(expense -> expense.getAmount() != null ? expense.getAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
