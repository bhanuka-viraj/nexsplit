package com.nexsplit.controller;

import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.bill.BillDto;
import com.nexsplit.dto.bill.BillSummaryDto;
import com.nexsplit.dto.bill.BillParticipantDto;
import com.nexsplit.dto.bill.CreateBillRequest;
import com.nexsplit.dto.bill.UpdateBillRequest;
import com.nexsplit.service.BillService;
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
 * REST Controller for bill management operations.
 * 
 * This controller provides endpoints for managing bills including creation,
 * updates, retrieval, and participant management. It follows RESTful patterns
 * and uses the ApiResponse wrapper for consistent response formatting.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bill Management", description = "APIs for managing bills and bill participants")
public class BillController {

    private final BillService billService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Create a new bill", description = "Create a new bill with participants")
    public ResponseEntity<ApiResponse<BillDto>> createBill(@Valid @RequestBody CreateBillRequest request) {
        log.info("Creating new bill: {}", request);

        BillDto bill = billService.createBill(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bill, "Bill created successfully"));
    }

    @PutMapping("/{billId}")
    @Operation(summary = "Update a bill", description = "Update an existing bill")
    public ResponseEntity<ApiResponse<BillDto>> updateBill(
            @Parameter(description = "Bill ID") @PathVariable String billId,
            @Valid @RequestBody UpdateBillRequest request) {
        log.info("Updating bill: {} with request: {}", billId, request);

        BillDto bill = billService.updateBill(billId, request);

        return ResponseEntity.ok(ApiResponse.success(bill, "Bill updated successfully"));
    }

    @DeleteMapping("/{billId}")
    @Operation(summary = "Delete a bill", description = "Delete a bill by ID")
    public ResponseEntity<ApiResponse<Void>> deleteBill(
            @Parameter(description = "Bill ID") @PathVariable String billId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting bill: {}", billId);

        String deletedBy = jwtUtil.getEmailFromCurrentToken();
        if (deletedBy == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication failed"));
        }

        billService.deleteBill(billId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "Bill deleted successfully"));
    }

    @GetMapping("/{billId}")
    @Operation(summary = "Get bill by ID", description = "Retrieve a bill by its ID")
    public ResponseEntity<ApiResponse<BillDto>> getBillById(
            @Parameter(description = "Bill ID") @PathVariable String billId) {
        log.debug("Getting bill by ID: {}", billId);

        BillDto bill = billService.getBillById(billId);

        return ResponseEntity.ok(ApiResponse.success(bill, "Bill retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all bills", description = "Retrieve all bills with pagination")
    public ResponseEntity<ApiResponse<Page<BillDto>>> getAllBills(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Getting all bills with pagination: page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy,
                sortDir);

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<BillDto> bills = billService.getAllBills(pageable);

        return ResponseEntity.ok(ApiResponse.success(bills, "Bills retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bills by user ID", description = "Retrieve bills for a specific user")
    public ResponseEntity<ApiResponse<Page<BillDto>>> getBillsByUserId(
            @Parameter(description = "User ID") @PathVariable String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Getting bills by user ID: {} with pagination: page={}, size={}", userId, page, size);

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<BillDto> bills = billService.getBillsByUserId(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(bills, "User bills retrieved successfully"));
    }

    @GetMapping("/nex/{nexId}")
    @Operation(summary = "Get bills by nex ID", description = "Retrieve bills for a specific nex group")
    public ResponseEntity<ApiResponse<Page<BillDto>>> getBillsByNexId(
            @Parameter(description = "Nex ID") @PathVariable String nexId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Getting bills by nex ID: {} with pagination: page={}, size={}", nexId, page, size);

        // Create Pageable with validation
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<BillDto> bills = billService.getBillsByNexId(nexId, pageable);

        return ResponseEntity.ok(ApiResponse.success(bills, "Nex bills retrieved successfully"));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get bill summary for user", description = "Get a summary of bills for a specific user")
    public ResponseEntity<ApiResponse<List<BillSummaryDto>>> getBillSummaryByUserId(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.debug("Getting bill summary for user: {}", userId);

        List<BillSummaryDto> summary = billService.getBillSummaryByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Bill summary retrieved successfully"));
    }

    @GetMapping("/nex/{nexId}/summary")
    @Operation(summary = "Get bill summary for nex", description = "Get a summary of bills for a specific nex group")
    public ResponseEntity<ApiResponse<List<BillSummaryDto>>> getBillSummaryByNexId(
            @Parameter(description = "Nex ID") @PathVariable String nexId) {
        log.debug("Getting bill summary for nex: {}", nexId);

        List<BillSummaryDto> summary = billService.getBillSummaryByNexId(nexId);

        return ResponseEntity.ok(ApiResponse.success(summary, "Nex bill summary retrieved successfully"));
    }

    @PostMapping("/{billId}/participants")
    @Operation(summary = "Add participant to bill", description = "Add a new participant to a bill")
    public ResponseEntity<ApiResponse<BillParticipantDto>> addParticipant(
            @Parameter(description = "Bill ID") @PathVariable String billId,
            @Parameter(description = "User ID") @RequestParam String userId,
            @Parameter(description = "Share amount") @RequestParam Double shareAmount) {
        log.info("Adding participant {} to bill: {}", userId, billId);

        BillParticipantDto participant = billService.addParticipant(billId, userId, shareAmount);

        return ResponseEntity.ok(ApiResponse.success(participant, "Participant added successfully"));
    }

    @DeleteMapping("/{billId}/participants/{userId}")
    @Operation(summary = "Remove participant from bill", description = "Remove a participant from a bill")
    public ResponseEntity<ApiResponse<BillDto>> removeParticipant(
            @Parameter(description = "Bill ID") @PathVariable String billId,
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Removing participant {} from bill: {}", userId, billId);

        billService.removeParticipant(billId, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Participant removed successfully"));
    }

    @PutMapping("/{billId}/participants/{userId}/pay")
    @Operation(summary = "Mark participant as paid", description = "Mark a participant's share as paid")
    public ResponseEntity<ApiResponse<BillParticipantDto>> markParticipantAsPaid(
            @Parameter(description = "Bill ID") @PathVariable String billId,
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Marking participant {} as paid for bill: {}", userId, billId);

        BillParticipantDto participant = billService.markParticipantAsPaid(billId, userId);

        return ResponseEntity.ok(ApiResponse.success(participant, "Participant marked as paid successfully"));
    }

    @PutMapping("/{billId}/participants/{userId}/unpay")
    @Operation(summary = "Mark participant as unpaid", description = "Mark a participant's share as unpaid")
    public ResponseEntity<ApiResponse<BillParticipantDto>> markParticipantAsUnpaid(
            @Parameter(description = "Bill ID") @PathVariable String billId,
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Marking participant {} as unpaid for bill: {}", userId, billId);

        BillParticipantDto participant = billService.markParticipantAsUnpaid(billId, userId);

        return ResponseEntity.ok(ApiResponse.success(participant, "Participant marked as unpaid successfully"));
    }

    @GetMapping("/{billId}/participants")
    @Operation(summary = "Get bill participants", description = "Get all participants for a bill")
    public ResponseEntity<ApiResponse<List<BillParticipantDto>>> getBillParticipants(
            @Parameter(description = "Bill ID") @PathVariable String billId) {
        log.debug("Getting participants for bill: {}", billId);

        List<BillParticipantDto> participants = billService.getBillParticipants(billId);

        return ResponseEntity.ok(ApiResponse.success(participants, "Bill participants retrieved successfully"));
    }

    @GetMapping("/{billId}/analytics")
    @Operation(summary = "Get bill analytics", description = "Get analytics and statistics for a bill")
    public ResponseEntity<ApiResponse<Object>> getBillAnalytics(
            @Parameter(description = "Bill ID") @PathVariable String billId) {
        log.debug("Getting analytics for bill: {}", billId);

        Object analytics = billService.getBillAnalytics(billId);

        return ResponseEntity.ok(ApiResponse.success(analytics, "Bill analytics retrieved successfully"));
    }
}
