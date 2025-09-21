package com.nexsplit.service;

import com.nexsplit.dto.event.EventNotification;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Service interface for lightweight event notifications and SSE management.
 * 
 * This service provides methods for broadcasting lightweight event
 * notifications
 * via Server-Sent Events (SSE). The frontend receives these notifications and
 * triggers GET calls to fetch fresh data from the source of truth.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
public interface EventService {

    /**
     * Create SSE connection for a user to receive Nex-specific events.
     * 
     * @param userId The user ID
     * @param nexId  The Nex ID to subscribe to
     * @return SSE emitter for the connection
     */
    SseEmitter createNexEventStream(String userId, String nexId);

    /**
     * Broadcast expense added event to all Nex members.
     * 
     * @param nexId     The Nex ID
     * @param expenseId The expense ID
     * @param userId    The user who added the expense
     */
    void broadcastExpenseAdded(String nexId, String expenseId, String userId);

    /**
     * Broadcast expense updated event to all Nex members.
     * 
     * @param nexId     The Nex ID
     * @param expenseId The expense ID
     * @param userId    The user who updated the expense
     */
    void broadcastExpenseUpdated(String nexId, String expenseId, String userId);

    /**
     * Broadcast expense deleted event to all Nex members.
     * 
     * @param nexId     The Nex ID
     * @param expenseId The expense ID
     * @param userId    The user who deleted the expense
     */
    void broadcastExpenseDeleted(String nexId, String expenseId, String userId);

    /**
     * Broadcast debt settled event to all Nex members.
     * 
     * @param nexId  The Nex ID
     * @param debtId The debt ID
     * @param userId The user who settled the debt
     */
    void broadcastDebtSettled(String nexId, String debtId, String userId);

    /**
     * Broadcast member added event to all Nex members.
     * 
     * @param nexId    The Nex ID
     * @param memberId The new member ID
     * @param userId   The user who added the member
     */
    void broadcastMemberAdded(String nexId, String memberId, String userId);

    /**
     * Broadcast member removed event to all Nex members.
     * 
     * @param nexId    The Nex ID
     * @param memberId The removed member ID
     * @param userId   The user who removed the member
     */
    void broadcastMemberRemoved(String nexId, String memberId, String userId);

    /**
     * Broadcast settlement executed event to all Nex members.
     * 
     * @param nexId        The Nex ID
     * @param settlementId The settlement ID
     * @param userId       The user who executed the settlement
     */
    void broadcastSettlementExecuted(String nexId, String settlementId, String userId);

    /**
     * Broadcast invitation sent event to the invited user.
     * 
     * @param nexId     The Nex ID
     * @param userId    The invited user ID
     * @param inviterId The user who sent the invitation
     */
    void broadcastInvitationSent(String nexId, String userId, String inviterId);

    /**
     * Broadcast invitation accepted event to all Nex members.
     * 
     * @param nexId  The Nex ID
     * @param userId The user who accepted the invitation
     */
    void broadcastInvitationAccepted(String nexId, String userId);

    /**
     * Broadcast invitation declined event to Nex admins.
     * 
     * @param nexId  The Nex ID
     * @param userId The user who declined the invitation
     */
    void broadcastInvitationDeclined(String nexId, String userId);

    /**
     * Get active subscribers count for a specific Nex.
     * 
     * @param nexId The Nex ID
     * @return Number of active subscribers
     */
    int getActiveSubscribersCount(String nexId);

    /**
     * Get total active subscribers count.
     * 
     * @return Total number of active subscribers
     */
    int getTotalActiveSubscribersCount();

    /**
     * Clean up stale SSE connections.
     * 
     * @return Number of connections cleaned up
     */
    int cleanupStaleConnections();
}