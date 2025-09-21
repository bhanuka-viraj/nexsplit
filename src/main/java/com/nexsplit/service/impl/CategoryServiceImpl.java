package com.nexsplit.service.impl;

import com.nexsplit.dto.category.CategoryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.dto.category.CreateCategoryRequest;
import com.nexsplit.dto.category.UpdateCategoryRequest;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.mapper.category.CategoryMapStruct;
import com.nexsplit.model.Category;
import com.nexsplit.repository.CategoryRepository;
import com.nexsplit.service.CategoryService;
import com.nexsplit.service.NexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapStruct categoryMapStruct;
    private final NexService nexService;

    @Override
    public CategoryDto createCategory(CreateCategoryRequest request, String userId) {
        log.info("Creating category: {} for user: {}", request.getName(), userId);

        // Validate category name uniqueness
        if (request.getNexId() != null) {
            // Group category - check if name exists in the nex
            if (categoryRepository.existsByNameAndNexId(request.getName(), request.getNexId())) {
                throw new BusinessException("Category name already exists in this group",
                        ErrorCode.CATEGORY_NAME_EXISTS);
            }

            // Check if user is member of the nex
            if (!nexService.isMember(request.getNexId(), userId)) {
                throw new BusinessException("Access denied", ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
            }
        } else {
            // Personal category - check if name exists for the user
            if (categoryRepository.existsByNameAndCreatedBy(request.getName(), userId)) {
                throw new BusinessException("Category name already exists", ErrorCode.CATEGORY_NAME_EXISTS);
            }
        }

        // Create category entity
        Category category = categoryMapStruct.toEntity(request);
        category.setCreatedBy(userId);

        // Save category
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully: {}", savedCategory.getId());

        return categoryMapStruct.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CategorySummaryDto> getCategoriesByNexId(String nexId, String userId, int page, int size) {
        log.info("Getting paginated categories for nex: {} by user: {}, page: {}, size: {}", nexId, userId, page, size);

        // Check if user is member of the nex
        if (!nexService.isMember(nexId, userId)) {
            throw new BusinessException("Access denied", ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Category> categoryPage = categoryRepository.findByNexIdPaginated(nexId, pageable);

        List<CategorySummaryDto> categoryDtos = categoryPage.getContent().stream()
                .map(categoryMapStruct::toSummaryDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<CategorySummaryDto>builder()
                .data(categoryDtos)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(categoryPage.getTotalElements())
                        .totalPages(categoryPage.getTotalPages())
                        .hasNext(categoryPage.hasNext())
                        .hasPrevious(categoryPage.hasPrevious())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CategorySummaryDto> getPersonalCategories(String userId, int page, int size) {
        log.info("Getting paginated personal categories for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Category> categoryPage = categoryRepository.findPersonalCategoriesByUserIdPaginated(userId, pageable);

        List<CategorySummaryDto> categoryDtos = categoryPage.getContent().stream()
                .map(categoryMapStruct::toSummaryDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<CategorySummaryDto>builder()
                .data(categoryDtos)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(categoryPage.getTotalElements())
                        .totalPages(categoryPage.getTotalPages())
                        .hasNext(categoryPage.hasNext())
                        .hasPrevious(categoryPage.hasPrevious())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CategorySummaryDto> getDefaultCategories(int page, int size) {
        log.info("Getting paginated default categories, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Category> categoryPage = categoryRepository.findDefaultCategoriesPaginated(pageable);

        List<CategorySummaryDto> categoryDtos = categoryPage.getContent().stream()
                .map(categoryMapStruct::toSummaryDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<CategorySummaryDto>builder()
                .data(categoryDtos)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(categoryPage.getTotalElements())
                        .totalPages(categoryPage.getTotalPages())
                        .hasNext(categoryPage.hasNext())
                        .hasPrevious(categoryPage.hasPrevious())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(String categoryId, String userId) {
        log.info("Getting category: {} for user: {}", categoryId, userId);

        Category category = categoryRepository.findByIdAndNotDeleted(categoryId)
                .orElseThrow(() -> EntityNotFoundException.categoryNotFound(categoryId));

        // Check if user can access this category
        if (!canAccessCategory(categoryId, userId)) {
            throw new BusinessException("Access denied", ErrorCode.AUTHZ_CATEGORY_ACCESS_DENIED);
        }

        return categoryMapStruct.toDto(category);
    }

    @Override
    public CategoryDto updateCategory(String categoryId, UpdateCategoryRequest request, String userId) {
        log.info("Updating category: {} by user: {}", categoryId, userId);

        Category category = categoryRepository.findByIdAndNotDeleted(categoryId)
                .orElseThrow(() -> EntityNotFoundException.categoryNotFound(categoryId));

        // Prevent modification of default categories
        if (category.getIsDefault() != null && category.getIsDefault()) {
            throw new BusinessException("Cannot modify default categories", ErrorCode.AUTHZ_CATEGORY_ACCESS_DENIED);
        }

        // Check if user can modify this category
        if (!category.getCreatedBy().equals(userId)) {
            throw new BusinessException("Access denied", ErrorCode.AUTHZ_CATEGORY_ACCESS_DENIED);
        }

        // Check if name is being changed and if it conflicts
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (category.getNexId() != null) {
                // Group category
                if (categoryRepository.existsByNameAndNexId(request.getName(), category.getNexId())) {
                    throw new BusinessException("Category name already exists in this group",
                            ErrorCode.CATEGORY_NAME_EXISTS);
                }
            } else {
                // Personal category
                if (categoryRepository.existsByNameAndCreatedBy(request.getName(), userId)) {
                    throw new BusinessException("Category name already exists", ErrorCode.CATEGORY_NAME_EXISTS);
                }
            }
        }

        // Update category
        categoryMapStruct.updateEntityFromRequest(request, category);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully: {}", categoryId);

        return categoryMapStruct.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(String categoryId, String userId) {
        log.info("Deleting category: {} by user: {}", categoryId, userId);

        Category category = categoryRepository.findByIdAndNotDeleted(categoryId)
                .orElseThrow(() -> EntityNotFoundException.categoryNotFound(categoryId));

        // Prevent deletion of default categories
        if (category.getIsDefault() != null && category.getIsDefault()) {
            throw new BusinessException("Cannot delete default categories", ErrorCode.AUTHZ_CATEGORY_ACCESS_DENIED);
        }

        // Check if user can delete this category
        if (!category.getCreatedBy().equals(userId)) {
            throw new BusinessException("Access denied", ErrorCode.AUTHZ_CATEGORY_ACCESS_DENIED);
        }

        // Check if category has expenses
        if (category.getExpenses() != null && !category.getExpenses().isEmpty()) {
            throw new BusinessException("Cannot delete category with existing expenses",
                    ErrorCode.CATEGORY_HAS_EXPENSES);
        }

        // Soft delete category
        categoryRepository.softDeleteById(categoryId, userId);

        log.info("Category deleted successfully: {}", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessCategory(String categoryId, String userId) {
        Category category = categoryRepository.findByIdAndNotDeleted(categoryId)
                .orElse(null);

        if (category == null) {
            return false;
        }

        // If it's a default category (created by system user), allow access to any
        // authenticated user
        if (category.getIsDefault() != null && category.getIsDefault()) {
            return true;
        }

        // If it's a personal category, check if user is the creator
        if (category.getNexId() == null) {
            return category.getCreatedBy().equals(userId);
        }

        // If it's a group category, check if user is member of the nex
        return nexService.isMember(category.getNexId(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String categoryId) {
        return categoryRepository.existsByIdAndNotDeleted(categoryId);
    }
}
