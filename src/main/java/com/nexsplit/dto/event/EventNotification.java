package com.nexsplit.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight event notification for SSE broadcasting.
 * 
 * This DTO contains only essential information needed for real-time
 * notifications. The frontend will use this to trigger GET calls
 * to fetch fresh data from the source of truth.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventNotification {

    /**
     * Type of event that occurred
     */
    private String eventType;

    /**
     * ID of the Nex group this event belongs to
     */
    private String nexId;

    /**
     * ID of the entity that was affected (expense, debt, etc.)
     */
    private String entityId;

    /**
     * ID of the user who triggered the event
     */
    private String userId;

    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;

    /**
     * Optional message for the event
     */
    private String message;
}
