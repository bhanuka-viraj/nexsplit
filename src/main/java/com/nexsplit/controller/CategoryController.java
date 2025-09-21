package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.category.CategoryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.dto.category.CreateCategoryRequest;
import com.nexsplit.dto.category.UpdateCategoryRequest;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.service.CategoryService;
import com.nexsplit.util.StructuredLoggingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/categories")
@Tag(name = "Categories", description = "Category management endpoints")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

        private final CategoryService categoryService;

        @PostMapping
        @Operation(summary = "Create Category", description = "Create a new category (personal or group-specific)", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
                        @Valid @RequestBody CreateCategoryRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "CATEGORY_CREATED",
                                userId,
                                "CREATE_CATEGORY",
                                "SUCCESS",
                                Map.of("categoryName", request.getName(), "nexId", request.getNexId()));

                CategoryDto category = categoryService.createCategory(request, userId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(category, "Category created successfully"));
        }

        @GetMapping
        @Operation(summary = "List User Categories", description = "Get paginated personal categories for the current user", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<CategorySummaryDto>>> getUserCategories(
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<CategorySummaryDto> response = categoryService.getPersonalCategories(userId, page,
                                size);

                return ResponseEntity.ok(ApiResponse.success(response, "Personal categories retrieved successfully"));
        }

        @GetMapping("/default")
        @Operation(summary = "List Default Categories", description = "Get paginated default categories available to all users", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<CategorySummaryDto>>> getDefaultCategories(
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                PaginatedResponse<CategorySummaryDto> response = categoryService.getDefaultCategories(page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Default categories retrieved successfully"));
        }

        @GetMapping("/{categoryId}")
        @Operation(summary = "Get Category Details", description = "Get category details by ID", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
                        @PathVariable String categoryId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                CategoryDto category = categoryService.getCategoryById(categoryId, userId);

                return ResponseEntity.ok(ApiResponse.success(category, "Category details retrieved successfully"));
        }

        @PutMapping("/{categoryId}")
        @Operation(summary = "Update Category", description = "Update category details", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
                        @PathVariable String categoryId,
                        @Valid @RequestBody UpdateCategoryRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "CATEGORY_UPDATED",
                                userId,
                                "UPDATE_CATEGORY",
                                "SUCCESS",
                                Map.of("categoryId", categoryId, "categoryName", request.getName()));

                CategoryDto category = categoryService.updateCategory(categoryId, request, userId);

                return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
        }

        @DeleteMapping("/{categoryId}")
        @Operation(summary = "Delete Category", description = "Delete a category (only if it has no expenses)", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> deleteCategory(
                        @PathVariable String categoryId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "CATEGORY_DELETED",
                                userId,
                                "DELETE_CATEGORY",
                                "SUCCESS",
                                Map.of("categoryId", categoryId));

                categoryService.deleteCategory(categoryId, userId);

                return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
        }
}
