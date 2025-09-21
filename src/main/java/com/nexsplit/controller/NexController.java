package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.nex.CreateNexRequest;
import com.nexsplit.dto.nex.NexDto;
import com.nexsplit.dto.nex.NexSummaryDto;
import com.nexsplit.dto.nex.UpdateNexRequest;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.service.NexService;
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
@RequestMapping(ApiConfig.API_BASE_PATH + "/nex")
@Tag(name = "Nex (Expense Groups)", description = "Expense group management endpoints")
@RequiredArgsConstructor
@Slf4j
public class NexController {

        private final NexService nexService;
        private final CategoryService categoryService;

        @PostMapping
        @Operation(summary = "Create Expense Group", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<NexDto>> createNex(
                        @Valid @RequestBody CreateNexRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "NEX_CREATED",
                                userId,
                                "CREATE_NEX",
                                "SUCCESS",
                                Map.of("nexName", request.getName(), "nexType", request.getNexType().name()));

                NexDto nex = nexService.createNex(request, userId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(nex, "Expense group created successfully"));
        }

        @GetMapping
        @Operation(summary = "List User's Active Expense Groups", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<NexDto>>> getUserNexes(
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<NexDto> response = nexService.getUserNexesPaginated(userId, page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Expense groups retrieved successfully"));
        }

        @GetMapping("/{nexId}")
        @Operation(summary = "Get Expense Group Details", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<NexDto>> getNexById(
                        @PathVariable String nexId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                NexDto nex = nexService.getNexById(nexId, userId);

                return ResponseEntity.ok(ApiResponse.success(nex, "Expense group details retrieved successfully"));
        }

        @PutMapping("/{nexId}")
        @Operation(summary = "Update Expense Group", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<NexDto>> updateNex(
                        @PathVariable String nexId,
                        @Valid @RequestBody UpdateNexRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "NEX_UPDATED",
                                userId,
                                "UPDATE_NEX",
                                "SUCCESS",
                                Map.of("nexId", nexId, "nexName", request.getName()));

                NexDto nex = nexService.updateNex(nexId, request, userId);

                return ResponseEntity.ok(ApiResponse.success(nex, "Expense group updated successfully"));
        }

        @DeleteMapping("/{nexId}")
        @Operation(summary = "Delete Expense Group", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> deleteNex(
                        @PathVariable String nexId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "NEX_DELETED",
                                userId,
                                "DELETE_NEX",
                                "SUCCESS",
                                Map.of("nexId", nexId));

                nexService.deleteNex(nexId, userId);

                return ResponseEntity.ok(ApiResponse.success(null, "Expense group deleted successfully"));
        }

        @GetMapping("/{nexId}/summary")
        @Operation(summary = "Get Expense Group Summary", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<NexSummaryDto>> getNexSummary(
                        @PathVariable String nexId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                NexSummaryDto summary = nexService.getNexSummary(nexId, userId);

                return ResponseEntity.ok(ApiResponse.success(summary, "Summary retrieved successfully"));
        }

        @GetMapping("/{nexId}/categories")
        @Operation(summary = "Get Expense Group Categories", description = "Get paginated categories for a specific expense group", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<CategorySummaryDto>>> getNexCategories(
                        @PathVariable String nexId,
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<CategorySummaryDto> response = categoryService.getCategoriesByNexId(nexId, userId,
                                page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Categories retrieved successfully"));
        }
}
