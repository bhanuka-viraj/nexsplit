package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.nex.InvitationDto;
import com.nexsplit.service.NexMemberService;
import com.nexsplit.util.StructuredLoggingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/user/invitations")
@Tag(name = "User Invitations", description = "User invitation management endpoints")
@RequiredArgsConstructor
@Slf4j
public class UserInvitationController {

    private final NexMemberService nexMemberService;

    @GetMapping("/pending")
    @Operation(summary = "Get Pending Invitations", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<InvitationDto>>> getPendingInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();

        List<InvitationDto> pendingInvitations = nexMemberService.getPendingInvitations(userId);

        return ResponseEntity.ok(ApiResponse.success(pendingInvitations, "Pending invitations retrieved successfully"));
    }

    @PostMapping("/{nexId}/respond")
    @Operation(summary = "Respond to Invitation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> respondToInvitation(
            @PathVariable String nexId,
            @RequestParam boolean accept,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();

        if (accept) {
            // Log business event
            StructuredLoggingUtil.logBusinessEvent(
                    "INVITATION_ACCEPTED",
                    userId,
                    "ACCEPT_INVITATION",
                    "SUCCESS",
                    Map.of("nexId", nexId));

            nexMemberService.acceptInvitation(nexId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Invitation accepted successfully"));
        } else {
            // Log business event
            StructuredLoggingUtil.logBusinessEvent(
                    "INVITATION_DECLINED",
                    userId,
                    "DECLINE_INVITATION",
                    "SUCCESS",
                    Map.of("nexId", nexId));

            nexMemberService.declineInvitation(nexId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Invitation declined successfully"));
        }
    }
}
