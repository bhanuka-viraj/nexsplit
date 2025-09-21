package com.nexsplit.controller;

import com.nexsplit.dto.ApiResponse;
import com.nexsplit.model.view.SettlementHistoryView;
import com.nexsplit.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Nex-centric settlement management operations.
 * 
 * This controller provides endpoints for managing settlements within specific
 * Nex groups, following the architecture document's Nex-centric approach.
 * It provides a complete settlement workflow including execution, history,
 * and analytics.
 * 
 * @author NexSplit Team
 * @version 2.0
 * @since 2.0
 */
@RestController
@RequestMapping("/api/v1/nex/{nexId}/settlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nex Settlement Management", description = "APIs for managing settlements within Nex groups")
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * Execute settlements for a specific Nex group.
     * Uses the Nex's settlement type to determine behavior.
     * 
     * @param nexId       The Nex group ID
     * @param request     The settlement execution request
     * @param userDetails The authenticated user
     * @return Settlement execution response
     */
    @PostMapping("/execute")
    @Operation(summary = "Execute settlements", description = "Execute settlements for a specific Nex group using the Nex's settlement type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SettlementService.SettlementExecutionResponse>> executeSettlements(
            @Parameter(description = "Nex group ID") @PathVariable String nexId,
            @Valid @RequestBody SettlementService.SettlementExecutionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Executing settlements for Nex: {} by user: {}", nexId, userDetails.getUsername());

        SettlementService.SettlementExecutionResponse response = settlementService.executeSettlements(
                nexId, request, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(response, "Settlements executed successfully"));
    }

    /**
     * Get available settlements for a specific Nex group.
     * Shows what settlements can be executed based on the Nex's settlement type.
     * 
     * @param nexId          The Nex group ID
     * @param settlementType The settlement type (SIMPLIFIED or DETAILED)
     * @param userDetails    The authenticated user
     * @return Available settlements response
     */
    @GetMapping("/available")
    @Operation(summary = "Get available settlements", description = "Get available settlements for a specific Nex group", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SettlementService.AvailableSettlementsResponse>> getAvailableSettlements(
            @Parameter(description = "Nex group ID") @PathVariable String nexId,
            @Parameter(description = "Settlement type") @RequestParam(defaultValue = "SIMPLIFIED") String settlementType,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting available settlements for Nex: {} with type: {} by user: {}",
                nexId, settlementType, userDetails.getUsername());

        SettlementService.AvailableSettlementsResponse response = settlementService.getAvailableSettlements(
                nexId, settlementType, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(response, "Available settlements retrieved successfully"));
    }

    /**
     * Get settlement history for a specific Nex group.
     * 
     * @param nexId         The Nex group ID
     * @param page          Page number (0-based)
     * @param size          Page size
     * @param sortBy        Sort field
     * @param sortDirection Sort direction (ASC/DESC)
     * @param userDetails   The authenticated user
     * @return Page of settlement history records
     */
    @GetMapping("/history")
    @Operation(summary = "Get settlement history", description = "Get settlement history for a specific Nex group", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<SettlementHistoryView>>> getSettlementHistory(
            @Parameter(description = "Nex group ID") @PathVariable String nexId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "settledAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting settlement history for Nex: {} by user: {}", nexId, userDetails.getUsername());

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        Page<SettlementHistoryView> history = settlementService.getSettlementHistoryByNexId(nexId, pageable);

        return ResponseEntity.ok(ApiResponse.success(history, "Settlement history retrieved successfully"));
    }

    /**
     * Get settlement analytics for a specific Nex group.
     * 
     * @param nexId       The Nex group ID
     * @param userDetails The authenticated user
     * @return Settlement analytics
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get settlement analytics", description = "Get settlement analytics for a specific Nex group", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SettlementService.SettlementAnalytics>> getSettlementAnalytics(
            @Parameter(description = "Nex group ID") @PathVariable String nexId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting settlement analytics for Nex: {} by user: {}", nexId, userDetails.getUsername());

        SettlementService.SettlementAnalytics analytics = settlementService.getSettlementAnalyticsByNexId(nexId);

        return ResponseEntity.ok(ApiResponse.success(analytics, "Settlement analytics retrieved successfully"));
    }

    /**
     * Get settlement summary for a specific Nex group.
     * 
     * @param nexId       The Nex group ID
     * @param userDetails The authenticated user
     * @return Settlement summary
     */
    @GetMapping("/summary")
    @Operation(summary = "Get settlement summary", description = "Get settlement summary for a specific Nex group", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SettlementService.SettlementSummary>> getSettlementSummary(
            @Parameter(description = "Nex group ID") @PathVariable String nexId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting settlement summary for Nex: {} by user: {}", nexId, userDetails.getUsername());

        SettlementService.SettlementSummary summary = settlementService.getSettlementSummaryByNexId(nexId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Settlement summary retrieved successfully"));
    }
}
