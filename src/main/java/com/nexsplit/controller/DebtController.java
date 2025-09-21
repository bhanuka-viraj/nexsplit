package com.nexsplit.controller;

import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.debt.DebtDto;
import com.nexsplit.dto.debt.DebtSummaryDto;
import com.nexsplit.dto.debt.CreateDebtRequest;
import com.nexsplit.dto.debt.UpdateDebtRequest;
import com.nexsplit.service.DebtService;
import com.nexsplit.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for debt management operations.
 * 
 * This controller provides endpoints for managing debts including creation,
 * updates, retrieval, and balance operations. It follows RESTful patterns
 * and uses the ApiResponse wrapper for consistent response formatting.
 * 
 * NOTE: Settlement operations are handled by SettlementController.
 * This controller focuses only on debt CRUD operations and balance queries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Debt Management", description = "APIs for managing debts (CRUD operations and balance queries)")
public class DebtController {

    private final DebtService debtService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Create a new debt", description = "Create a new debt between users")
    public ResponseEntity<ApiResponse<DebtDto>> createDebt(@Valid @RequestBody CreateDebtRequest request) {
        log.info("Creating new debt: {}", request);

        DebtDto debt = debtService.createDebt(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(debt, "Debt created successfully"));
    }

    @PutMapping("/{debtId}")
    @Operation(summary = "Update a debt", description = "Update an existing debt")
    public ResponseEntity<ApiResponse<DebtDto>> updateDebt(
            @Parameter(description = "Debt ID") @PathVariable String debtId,
            @Valid @RequestBody UpdateDebtRequest request) {
        log.info("Updating debt: {} with request: {}", debtId, request);

        DebtDto debt = debtService.updateDebt(debtId, request);

        return ResponseEntity.ok(ApiResponse.success(debt, "Debt updated successfully"));
    }

    @DeleteMapping("/{debtId}")
    @Operation(summary = "Delete a debt", description = "Delete a debt by ID")
    public ResponseEntity<ApiResponse<Void>> deleteDebt(
            @Parameter(description = "Debt ID") @PathVariable String debtId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting debt: {}", debtId);

        String deletedBy = jwtUtil.getEmailFromCurrentToken();
        if (deletedBy == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication failed"));
        }

        debtService.deleteDebt(debtId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "Debt deleted successfully"));
    }

    @GetMapping("/{debtId}")
    @Operation(summary = "Get debt by ID", description = "Retrieve a debt by its ID")
    public ResponseEntity<ApiResponse<DebtDto>> getDebtById(
            @Parameter(description = "Debt ID") @PathVariable String debtId) {
        log.debug("Getting debt by ID: {}", debtId);

        DebtDto debt = debtService.getDebtById(debtId);

        return ResponseEntity.ok(ApiResponse.success(debt, "Debt retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all debts", description = "Retrieve all debts with pagination")
    public ResponseEntity<ApiResponse<Page<DebtDto>>> getAllDebts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Getting all debts with pagination: page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy,
                sortDir);

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<DebtDto> debts = debtService.getAllDebts(pageable);

        return ResponseEntity.ok(ApiResponse.success(debts, "Debts retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get debts by user ID", description = "Retrieve debts for a specific user")
    public ResponseEntity<ApiResponse<Page<DebtDto>>> getDebtsByUserId(
            @Parameter(description = "User ID") @PathVariable String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Getting debts by user ID: {} with pagination: page={}, size={}", userId, page, size);

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<DebtDto> debts = debtService.getDebtsByUserId(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(debts, "User debts retrieved successfully"));
    }

    @GetMapping("/nex/{nexId}")
    @Operation(summary = "Get debts by nex ID", description = "Retrieve debts for a specific nex group")
    public ResponseEntity<ApiResponse<Page<DebtDto>>> getDebtsByNexId(
            @Parameter(description = "Nex ID") @PathVariable String nexId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Getting debts by nex ID: {} with pagination: page={}, size={}", nexId, page, size);

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<DebtDto> debts = debtService.getDebtsByNexId(nexId, pageable);

        return ResponseEntity.ok(ApiResponse.success(debts, "Nex debts retrieved successfully"));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get debt summary for user", description = "Get a summary of debts for a specific user")
    public ResponseEntity<ApiResponse<List<DebtSummaryDto>>> getDebtSummaryByUserId(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.debug("Getting debt summary for user: {}", userId);

        List<DebtSummaryDto> summary = debtService.getDebtSummaryByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Debt summary retrieved successfully"));
    }

    @GetMapping("/nex/{nexId}/summary")
    @Operation(summary = "Get debt summary for nex", description = "Get a summary of debts for a specific nex group")
    public ResponseEntity<ApiResponse<List<DebtSummaryDto>>> getDebtSummaryByNexId(
            @Parameter(description = "Nex ID") @PathVariable String nexId) {
        log.debug("Getting debt summary for nex: {}", nexId);

        List<DebtSummaryDto> summary = debtService.getDebtSummaryByNexId(nexId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Nex debt summary retrieved successfully"));
    }

    @GetMapping("/user/{userId}/balance")
    @Operation(summary = "Get user balance", description = "Get the net balance for a user across all debts")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getUserBalance(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.debug("Getting balance for user: {}", userId);

        java.math.BigDecimal balance = debtService.getUserBalance(userId);

        return ResponseEntity.ok(ApiResponse.success(balance, "User balance retrieved successfully"));
    }

    @GetMapping("/nex/{nexId}/balances")
    @Operation(summary = "Get nex balances", description = "Get balances for all users in a nex group")
    public ResponseEntity<ApiResponse<List<DebtSummaryDto>>> getNexBalances(
            @Parameter(description = "Nex ID") @PathVariable String nexId) {
        log.debug("Getting balances for nex: {}", nexId);

        List<DebtSummaryDto> balances = debtService.getNexBalances(nexId);

        return ResponseEntity.ok(ApiResponse.success(balances, "Nex balances retrieved successfully"));
    }
}
