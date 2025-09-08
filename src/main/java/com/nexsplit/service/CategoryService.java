package com.nexsplit.service;

import com.nexsplit.dto.category.CategoryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.dto.category.CreateCategoryRequest;
import com.nexsplit.dto.category.UpdateCategoryRequest;
import com.nexsplit.dto.PaginatedResponse;

public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryDto createCategory(CreateCategoryRequest request, String userId);

    /**
     * Get paginated categories for a specific nex
     */
    PaginatedResponse<CategorySummaryDto> getCategoriesByNexId(String nexId, String userId, int page, int size);

    /**
     * Get paginated personal categories for a user
     */
    PaginatedResponse<CategorySummaryDto> getPersonalCategories(String userId, int page, int size);

    /**
     * Get paginated default categories
     */
    PaginatedResponse<CategorySummaryDto> getDefaultCategories(int page, int size);

    /**
     * Get category by ID
     */
    CategoryDto getCategoryById(String categoryId, String userId);

    /**
     * Update category
     */
    CategoryDto updateCategory(String categoryId, UpdateCategoryRequest request, String userId);

    /**
     * Delete category
     */
    void deleteCategory(String categoryId, String userId);

    /**
     * Check if user can access category
     */
    boolean canAccessCategory(String categoryId, String userId);

    /**
     * Check if category exists and is not deleted
     */
    boolean existsById(String categoryId);
}
