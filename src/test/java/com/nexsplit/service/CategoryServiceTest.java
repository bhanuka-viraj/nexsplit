package com.nexsplit.service;

import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.category.CategoryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.dto.category.CreateCategoryRequest;
import com.nexsplit.dto.category.UpdateCategoryRequest;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.mapper.category.CategoryMapper;
import com.nexsplit.model.Category;
import com.nexsplit.repository.CategoryRepository;
import com.nexsplit.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private NexService nexService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;
    private CreateCategoryRequest createRequest;
    private UpdateCategoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("test-category-id")
                .name("Test Category")
                .createdBy("test-user-id")
                .nexId(null)
                .isDefault(false)
                .build();

        testCategoryDto = CategoryDto.builder()
                .id("test-category-id")
                .name("Test Category")
                .createdBy("test-user-id")
                .nexId(null)
                .isDefault(false)
                .build();

        createRequest = CreateCategoryRequest.builder()
                .name("Test Category")
                .nexId(null)
                .build();

        updateRequest = UpdateCategoryRequest.builder()
                .name("Updated Category")
                .build();
    }

    @Test
    void createCategory_Success() {
        // Given
        when(categoryRepository.existsByNameAndCreatedBy(anyString(), anyString())).thenReturn(false);
        when(categoryMapper.toEntity(any(CreateCategoryRequest.class))).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(testCategoryDto);

        // When
        CategoryDto result = categoryService.createCategory(createRequest, "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals("test-category-id", result.getId());
        assertEquals("Test Category", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // Given
        when(categoryRepository.existsByNameAndCreatedBy(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            categoryService.createCategory(createRequest, "test-user-id");
        });
    }

    @Test
    void getPersonalCategories_Success() {
        // Given
        List<Category> personalCategories = Arrays.asList(testCategory);
        Page<Category> personalCategoriesPage = new PageImpl<>(personalCategories);
        CategorySummaryDto testCategorySummaryDto = CategorySummaryDto.builder()
                .id("test-category-id")
                .name("Test Category")
                .build();

        when(categoryRepository.findPersonalCategoriesByUserId(anyString(), any(Pageable.class)))
                .thenReturn(personalCategoriesPage);
        when(categoryMapper.toSummaryDto(any(Category.class))).thenReturn(testCategorySummaryDto);

        // When
        PaginatedResponse<CategorySummaryDto> result = categoryService.getPersonalCategories("test-user-id", 0, 10);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("test-category-id", result.getData().get(0).getId());
        assertEquals(1, result.getPagination().getTotalElements());
    }

    @Test
    void getCategoryById_Success() {
        // Given
        when(categoryRepository.findByIdAndNotDeleted(anyString())).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(testCategoryDto);

        // When
        CategoryDto result = categoryService.getCategoryById("test-category-id", "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals("test-category-id", result.getId());
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.findByIdAndNotDeleted(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            categoryService.getCategoryById("non-existent-id", "test-user-id");
        });
    }

    @Test
    void updateCategory_Success() {
        // Given
        when(categoryRepository.findByIdAndNotDeleted(anyString())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndCreatedBy(anyString(), anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(testCategoryDto);

        // When
        CategoryDto result = categoryService.updateCategory("test-category-id", updateRequest, "test-user-id");

        // Then
        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        // Given
        testCategory.setExpenses(null); // No expenses
        when(categoryRepository.findByIdAndNotDeleted(anyString())).thenReturn(Optional.of(testCategory));

        // When
        categoryService.deleteCategory("test-category-id", "test-user-id");

        // Then
        verify(categoryRepository).softDeleteById("test-category-id", "test-user-id");
    }
}
