package com.nexsplit.service.impl;

import com.nexsplit.dto.event.EventNotification;
import com.nexsplit.model.NexMember;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.service.EventService;
import com.nexsplit.service.SseManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of EventService for lightweight event notifications and SSE
 * management.
 * 
 * This service provides lightweight event notifications via SSE. The frontend
 * receives these notifications and triggers GET calls to fetch fresh data
 * from the source of truth.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final SseManager sseManager;
    private final NexMemberRepository nexMemberRepository;

    // Track Nex-specific subscriptions
    private final Map<String, Map<String, SseEmitter>> nexSubscriptions = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createNexEventStream(String userId, String nexId) {
        log.info("Creating SSE connection for user {} to Nex {}", userId, nexId);

        // Verify user is member of this Nex
        if (!isNexMember(nexId, userId)) {
            log.warn("User {} is not a member of Nex {}", userId, nexId);
            throw new SecurityException("Not a member of this Nex");
        }

        // Create SSE emitter
        SseEmitter emitter = new SseEmitter(30000L); // 30 second timeout

        // Add to Nex-specific subscriptions
        nexSubscriptions.computeIfAbsent(nexId, k -> new ConcurrentHashMap<>())
                .put(userId, emitter);

        // Set up completion and error handlers
        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for user {} in Nex {}", userId, nexId);
            removeNexSubscription(nexId, userId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE connection timeout for user {} in Nex {}", userId, nexId);
            removeNexSubscription(nexId, userId);
        });

        emitter.onError(ex -> {
            log.error("SSE connection error for user {} in Nex {}", userId, nexId, ex);
            removeNexSubscription(nexId, userId);
        });

        log.info("SSE connection created for user {} in Nex {}", userId, nexId);
        return emitter;
    }

    @Override
    public void broadcastExpenseAdded(String nexId, String expenseId, String userId) {
        log.debug("Broadcasting expense added event for Nex {}: expense {}", nexId, expenseId);

        EventNotification notification = EventNotification.builder()
                .eventType("EXPENSE_ADDED")
                .nexId(nexId)
                .entityId(expenseId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("New expense added")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastExpenseUpdated(String nexId, String expenseId, String userId) {
        log.debug("Broadcasting expense updated event for Nex {}: expense {}", nexId, expenseId);

        EventNotification notification = EventNotification.builder()
                .eventType("EXPENSE_UPDATED")
                .nexId(nexId)
                .entityId(expenseId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("Expense updated")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastExpenseDeleted(String nexId, String expenseId, String userId) {
        log.debug("Broadcasting expense deleted event for Nex {}: expense {}", nexId, expenseId);

        EventNotification notification = EventNotification.builder()
                .eventType("EXPENSE_DELETED")
                .nexId(nexId)
                .entityId(expenseId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("Expense deleted")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastDebtSettled(String nexId, String debtId, String userId) {
        log.debug("Broadcasting debt settled event for Nex {}: debt {}", nexId, debtId);

        EventNotification notification = EventNotification.builder()
                .eventType("DEBT_SETTLED")
                .nexId(nexId)
                .entityId(debtId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("Debt settled")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastMemberAdded(String nexId, String memberId, String userId) {
        log.debug("Broadcasting member added event for Nex {}: member {}", nexId, memberId);

        EventNotification notification = EventNotification.builder()
                .eventType("MEMBER_ADDED")
                .nexId(nexId)
                .entityId(memberId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("New member added")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastMemberRemoved(String nexId, String memberId, String userId) {
        log.debug("Broadcasting member removed event for Nex {}: member {}", nexId, memberId);

        EventNotification notification = EventNotification.builder()
                .eventType("MEMBER_REMOVED")
                .nexId(nexId)
                .entityId(memberId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("Member removed")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastSettlementExecuted(String nexId, String settlementId, String userId) {
        log.debug("Broadcasting settlement executed event for Nex {}: settlement {}", nexId, settlementId);

        EventNotification notification = EventNotification.builder()
                .eventType("SETTLEMENT_EXECUTED")
                .nexId(nexId)
                .entityId(settlementId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("Settlement executed")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastInvitationSent(String nexId, String userId, String inviterId) {
        log.debug("Broadcasting invitation sent event for Nex {}: user {} invited by {}", nexId, userId, inviterId);

        EventNotification notification = EventNotification.builder()
                .eventType("INVITATION_SENT")
                .nexId(nexId)
                .entityId(userId)
                .userId(inviterId)
                .timestamp(LocalDateTime.now())
                .message("You have been invited to join a Nex group")
                .build();

        // Send to the invited user specifically
        sseManager.broadcastToUser(userId, notification);
    }

    @Override
    public void broadcastInvitationAccepted(String nexId, String userId) {
        log.debug("Broadcasting invitation accepted event for Nex {}: user {}", nexId, userId);

        EventNotification notification = EventNotification.builder()
                .eventType("INVITATION_ACCEPTED")
                .nexId(nexId)
                .entityId(userId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("New member joined the Nex group")
                .build();

        broadcastToNex(nexId, notification);
    }

    @Override
    public void broadcastInvitationDeclined(String nexId, String userId) {
        log.debug("Broadcasting invitation declined event for Nex {}: user {}", nexId, userId);

        EventNotification notification = EventNotification.builder()
                .eventType("INVITATION_DECLINED")
                .nexId(nexId)
                .entityId(userId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .message("Invitation declined")
                .build();

        // Send to Nex admins only
        broadcastToNexAdmins(nexId, notification);
    }

    @Override
    public int getActiveSubscribersCount(String nexId) {
        Map<String, SseEmitter> subscribers = nexSubscriptions.get(nexId);
        return subscribers != null ? subscribers.size() : 0;
    }

    @Override
    public int getTotalActiveSubscribersCount() {
        return nexSubscriptions.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    @Override
    public int cleanupStaleConnections() {
        int cleanedUp = 0;

        for (Map.Entry<String, Map<String, SseEmitter>> nexEntry : nexSubscriptions.entrySet()) {
            String nexId = nexEntry.getKey();
            Map<String, SseEmitter> subscribers = nexEntry.getValue();

            subscribers.entrySet().removeIf(entry -> {
                SseEmitter emitter = entry.getValue();
                try {
                    emitter.send(SseEmitter.event().name("ping").data("ping"));
                    return false; // Connection is alive
                } catch (Exception e) {
                    log.debug("Removing stale connection for user {} in Nex {}", entry.getKey(), nexId);
                    return true; // Connection is stale
                }
            });

            cleanedUp += subscribers.size();
        }

        log.info("Cleaned up {} stale SSE connections", cleanedUp);
        return cleanedUp;
    }

    /**
     * Broadcast event notification to all subscribers of a specific Nex.
     */
    private void broadcastToNex(String nexId, EventNotification notification) {
        Map<String, SseEmitter> subscribers = nexSubscriptions.get(nexId);
        if (subscribers == null || subscribers.isEmpty()) {
            log.debug("No subscribers for Nex {}", nexId);
            return;
        }

        log.debug("Broadcasting to {} subscribers in Nex {}", subscribers.size(), nexId);

        subscribers.entrySet().removeIf(entry -> {
            String userId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            try {
                emitter.send(SseEmitter.event()
                        .name("event")
                        .data(notification));
                return false; // Keep connection
            } catch (Exception e) {
                log.warn("Failed to send event to user {} in Nex {}", userId, nexId, e);
                return true; // Remove connection
            }
        });
    }

    /**
     * Remove a user's subscription from a Nex.
     */
    private void removeNexSubscription(String nexId, String userId) {
        Map<String, SseEmitter> subscribers = nexSubscriptions.get(nexId);
        if (subscribers != null) {
            subscribers.remove(userId);
            if (subscribers.isEmpty()) {
                nexSubscriptions.remove(nexId);
            }
        }
    }

    /**
     * Broadcast event notification to Nex admins only.
     */
    private void broadcastToNexAdmins(String nexId, EventNotification notification) {
        Map<String, SseEmitter> subscribers = nexSubscriptions.get(nexId);
        if (subscribers == null || subscribers.isEmpty()) {
            log.debug("No subscribers for Nex {}", nexId);
            return;
        }

        log.debug("Broadcasting to admins in Nex {}", nexId);

        subscribers.entrySet().removeIf(entry -> {
            String userId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            // Check if user is admin
            if (!isNexAdmin(nexId, userId)) {
                return false; // Keep connection but don't send
            }

            try {
                emitter.send(SseEmitter.event()
                        .name("event")
                        .data(notification));
                return false; // Keep connection
            } catch (Exception e) {
                log.warn("Failed to send event to admin {} in Nex {}", userId, nexId, e);
                return true; // Remove connection
            }
        });
    }

    /**
     * Check if user is a member of the Nex.
     */
    private boolean isNexMember(String nexId, String userId) {
        return nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .map(member -> member.getStatus() == NexMember.MemberStatus.ACTIVE && !member.isDeleted())
                .orElse(false);
    }

    /**
     * Check if user is an admin of the Nex.
     */
    private boolean isNexAdmin(String nexId, String userId) {
        return nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .map(member -> member.getRole() == NexMember.MemberRole.ADMIN &&
                        member.getStatus() == NexMember.MemberStatus.ACTIVE &&
                        !member.isDeleted())
                .orElse(false);
    }
}