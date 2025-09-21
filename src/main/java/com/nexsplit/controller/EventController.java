package com.nexsplit.controller;

import com.nexsplit.dto.ApiResponse;
import com.nexsplit.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST Controller for lightweight event notifications and SSE management.
 * 
 * This controller provides endpoints for SSE connections and event statistics.
 * The frontend subscribes to Nex-specific events and triggers GET calls
 * to fetch fresh data when events are received.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Notifications", description = "APIs for real-time event notifications via SSE")
public class EventController {

    private final EventService eventService;

    @GetMapping("/nex/{nexId}/stream")
    @Operation(summary = "Subscribe to Nex events", description = "Subscribe to real-time events for a specific Nex via SSE")
    public SseEmitter subscribeToNexEvents(
            @Parameter(description = "Nex ID") @PathVariable String nexId,
            @Parameter(description = "User ID") @RequestParam String userId) {

        log.info("User {} subscribing to events for Nex {}", userId, nexId);

        return eventService.createNexEventStream(userId, nexId);
    }

    @GetMapping("/nex/{nexId}/subscribers/count")
    @Operation(summary = "Get Nex subscribers count", description = "Get the number of active SSE subscribers for a specific Nex")
    public ResponseEntity<ApiResponse<Integer>> getNexSubscribersCount(
            @Parameter(description = "Nex ID") @PathVariable String nexId) {

        log.debug("Getting subscribers count for Nex: {}", nexId);

        int count = eventService.getActiveSubscribersCount(nexId);

        return ResponseEntity.ok(ApiResponse.success(count, "Nex subscribers count retrieved successfully"));
    }

    @GetMapping("/subscribers/count")
    @Operation(summary = "Get total subscribers count", description = "Get the total number of active SSE subscribers")
    public ResponseEntity<ApiResponse<Integer>> getTotalSubscribersCount() {

        log.debug("Getting total subscribers count");

        int count = eventService.getTotalActiveSubscribersCount();

        return ResponseEntity.ok(ApiResponse.success(count, "Total subscribers count retrieved successfully"));
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Clean up stale connections", description = "Manually trigger cleanup of stale SSE connections")
    public ResponseEntity<ApiResponse<Integer>> cleanupStaleConnections() {

        log.info("Manually triggering SSE connection cleanup");

        int cleanedUp = eventService.cleanupStaleConnections();

        return ResponseEntity.ok(ApiResponse.success(cleanedUp, "Stale connections cleaned up successfully"));
    }
}