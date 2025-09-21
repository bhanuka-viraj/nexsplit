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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for user-specific settlement operations.
 * 
 * This controller provides endpoints for managing settlements from a user's
 * perspective across all Nex groups they are members of.
 * 
 * @author NexSplit Team
 * @version 2.0
 * @since 2.0
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/settlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Settlement Management", description = "APIs for managing user-specific settlements across all Nex groups")
public class UserSettlementController {

    private final SettlementService settlementService;

    /**
     * Get settlement history for a specific user across all Nex groups.
     * 
     * @param userId        The user ID
     * @param page          Page number (0-based)
     * @param size          Page size
     * @param sortBy        Sort field
     * @param sortDirection Sort direction (ASC/DESC)
     * @param userDetails   The authenticated user
     * @return Page of settlement history records
     */
    @GetMapping("/history")
    @Operation(summary = "Get user settlement history", description = "Get settlement history for a specific user across all Nex groups", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<SettlementHistoryView>>> getUserSettlementHistory(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "settledAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting settlement history for user: {} by user: {}", userId, userDetails.getUsername());

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        Page<SettlementHistoryView> history = settlementService.getSettlementHistoryByUserId(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(history, "User settlement history retrieved successfully"));
    }

    /**
     * Get settlement analytics for a specific user.
     * 
     * @param userId      The user ID
     * @param userDetails The authenticated user
     * @return Settlement analytics
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get user settlement analytics", description = "Get settlement analytics for a specific user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SettlementService.SettlementAnalytics>> getUserSettlementAnalytics(
            @Parameter(description = "User ID") @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting settlement analytics for user: {} by user: {}", userId, userDetails.getUsername());

        SettlementService.SettlementAnalytics analytics = settlementService.getSettlementAnalyticsByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(analytics, "User settlement analytics retrieved successfully"));
    }

    /**
     * Get settlement summary for a specific user.
     * 
     * @param userId      The user ID
     * @param userDetails The authenticated user
     * @return Settlement summary
     */
    @GetMapping("/summary")
    @Operation(summary = "Get user settlement summary", description = "Get settlement summary for a specific user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SettlementService.SettlementSummary>> getUserSettlementSummary(
            @Parameter(description = "User ID") @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting settlement summary for user: {} by user: {}", userId, userDetails.getUsername());

        SettlementService.SettlementSummary summary = settlementService.getSettlementSummaryByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(summary, "User settlement summary retrieved successfully"));
    }
}
