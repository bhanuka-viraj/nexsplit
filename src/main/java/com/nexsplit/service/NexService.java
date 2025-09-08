package com.nexsplit.service;

import com.nexsplit.dto.nex.CreateNexRequest;
import com.nexsplit.dto.nex.NexDto;
import com.nexsplit.dto.nex.NexSummaryDto;
import com.nexsplit.dto.nex.UpdateNexRequest;
import com.nexsplit.dto.PaginatedResponse;

public interface NexService {

    /**
     * Create a new expense group (nex)
     */
    NexDto createNex(CreateNexRequest request, String userId);

    /**
     * Get expense group by ID (with authorization check)
     */
    NexDto getNexById(String nexId, String userId);

    /**
     * Update expense group
     */
    NexDto updateNex(String nexId, UpdateNexRequest request, String userId);

    /**
     * Delete expense group (admin only)
     */
    void deleteNex(String nexId, String userId);

    /**
     * Check if user is a member of the nex
     */
    boolean isMember(String nexId, String userId);

    /**
     * Check if user is an admin of the nex
     */
    boolean isAdmin(String nexId, String userId);

    /**
     * Get paginated list of user's active nex groups (excludes pending invitations)
     */
    PaginatedResponse<NexDto> getUserNexesPaginated(String userId, int page, int size);

    /**
     * Get nex summary statistics
     */
    NexSummaryDto getNexSummary(String nexId, String userId);
}
