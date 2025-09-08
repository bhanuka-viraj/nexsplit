package com.nexsplit.mapper.category;

import com.nexsplit.dto.category.CategoryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.dto.category.CreateCategoryRequest;
import com.nexsplit.dto.category.UpdateCategoryRequest;
import com.nexsplit.model.Category;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        String creatorName = null;
        String creatorUsername = null;
        if (category.getCreator() != null) {
            creatorName = category.getCreator().getFirstName() + " " + category.getCreator().getLastName();
            creatorUsername = category.getCreator().getUsername();
        }

        String nexName = null;
        if (category.getNex() != null) {
            nexName = category.getNex().getName();
        }

        int expenseCount = category.getExpenses() != null ? category.getExpenses().size() : 0;

        // Calculate total expense amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (category.getExpenses() != null) {
            totalAmount = category.getExpenses().stream()
                    .map(expense -> expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .createdBy(category.getCreatedBy())
                .nexId(category.getNexId())
                .isDefault(category.getIsDefault())
                .createdAt(category.getCreatedAt())
                .modifiedAt(category.getModifiedAt())
                .creatorName(creatorName)
                .creatorUsername(creatorUsername)
                .nexName(nexName)
                .expenseCount(expenseCount)
                .totalExpenseAmount(totalAmount.longValue())
                .build();
    }

    /**
     * Lightweight mapping for better performance - excludes expensive calculations
     */
    public CategorySummaryDto toSummaryDto(Category category) {
        if (category == null) {
            return null;
        }

        String creatorName = null;
        String creatorUsername = null;
        if (category.getCreator() != null) {
            creatorName = category.getCreator().getFirstName() + " " + category.getCreator().getLastName();
            creatorUsername = category.getCreator().getUsername();
        }

        String nexName = null;
        if (category.getNex() != null) {
            nexName = category.getNex().getName();
        }

        return CategorySummaryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .createdBy(category.getCreatedBy())
                .nexId(category.getNexId())
                .isDefault(category.getIsDefault())
                .createdAt(category.getCreatedAt())
                .modifiedAt(category.getModifiedAt())
                .creatorName(creatorName)
                .creatorUsername(creatorUsername)
                .nexName(nexName)
                .build();
    }

    public Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }

        return Category.builder()
                .name(request.getName())
                .nexId(request.getNexId())
                .isDefault(false) // New categories are never default by default
                .build();
    }

    public void updateEntityFromRequest(UpdateCategoryRequest request, Category category) {
        if (request == null || category == null) {
            return;
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }
    }
}
