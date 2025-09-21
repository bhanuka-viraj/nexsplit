package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.nex.InviteMemberRequest;
import com.nexsplit.dto.nex.NexMemberDto;
import com.nexsplit.dto.nex.UpdateMemberRoleRequest;
import com.nexsplit.service.NexMemberService;
import com.nexsplit.util.StructuredLoggingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/nex/{nexId}/members")
@Tag(name = "Nex Members", description = "Expense group member management endpoints")
@RequiredArgsConstructor
@Slf4j
public class NexMemberController {

        private final NexMemberService nexMemberService;

        @PostMapping("/invite")
        @Operation(summary = "Invite Member", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> inviteMember(
                        @PathVariable String nexId,
                        @Valid @RequestBody InviteMemberRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String inviterId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "MEMBER_INVITED",
                                inviterId,
                                "INVITE_MEMBER",
                                "SUCCESS",
                                Map.of("nexId", nexId, "inviteeEmail", request.getEmail(), "role",
                                                request.getRole().name()));

                nexMemberService.inviteMember(nexId, request, inviterId);

                return ResponseEntity.ok(ApiResponse.success(null, "Member invited successfully"));
        }

        @PutMapping("/{memberId}/role")
        @Operation(summary = "Update Member Role", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> updateMemberRole(
                        @PathVariable String nexId,
                        @PathVariable String memberId,
                        @Valid @RequestBody UpdateMemberRoleRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String adminId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "MEMBER_ROLE_UPDATED",
                                adminId,
                                "UPDATE_MEMBER_ROLE",
                                "SUCCESS",
                                Map.of("nexId", nexId, "memberId", memberId, "newRole", request.getRole().name()));

                nexMemberService.updateMemberRole(nexId, memberId, request, adminId);

                return ResponseEntity.ok(ApiResponse.success(null, "Member role updated successfully"));
        }

        @DeleteMapping("/{memberId}")
        @Operation(summary = "Remove Member", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> removeMember(
                        @PathVariable String nexId,
                        @PathVariable String memberId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String adminId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "MEMBER_REMOVED",
                                adminId,
                                "REMOVE_MEMBER",
                                "SUCCESS",
                                Map.of("nexId", nexId, "memberId", memberId));

                nexMemberService.removeMember(nexId, memberId, adminId);

                return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
        }

        @PostMapping("/leave")
        @Operation(summary = "Leave Nex", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<Void>> leaveNex(
                        @PathVariable String nexId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "USER_LEFT_NEX",
                                userId,
                                "LEAVE_NEX",
                                "SUCCESS",
                                Map.of("nexId", nexId));

                nexMemberService.leaveNex(nexId, userId);

                return ResponseEntity.ok(ApiResponse.success(null, "Left nex successfully"));
        }

        @GetMapping
        @Operation(summary = "List Nex Members", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<PaginatedResponse<NexMemberDto>>> getNexMembers(
                        @PathVariable String nexId,
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @AuthenticationPrincipal UserDetails userDetails) {

                String userId = userDetails.getUsername();

                PaginatedResponse<NexMemberDto> response = nexMemberService.getNexMembers(nexId, userId, page, size);

                return ResponseEntity.ok(ApiResponse.success(response, "Members retrieved successfully"));
        }
}
