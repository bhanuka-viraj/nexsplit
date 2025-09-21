package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.response.TestEmailResponse;
import com.nexsplit.service.AuditService;
import com.nexsplit.service.EmailService;
import com.nexsplit.util.JwtUtil;
import com.nexsplit.util.LoggingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * REST Controller for admin and development operations.
 * 
 * This controller provides endpoints for administrative tasks and development
 * utilities.
 * All endpoints require admin authentication.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/admin")
@Tag(name = "Admin Operations", description = "Administrative and development endpoints")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final EmailService emailService;
    private final AuditService auditService;
    private final JwtUtil jwtUtil;

    @PostMapping("/test-email")
    @Operation(summary = "Send Test Email", description = "Send a test email to verify email configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TestEmailResponse>> sendTestEmail(
            @RequestParam @Email String email,
            HttpServletRequest httpRequest,
            HttpServletResponse response,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // Verify admin access
            String currentUser = jwtUtil.getEmailFromCurrentToken();
            if (currentUser == null) {
                log.error("Failed to extract email from JWT token");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication failed"));
            }

            // Send test email (using simple email method for now)
            try {
                emailService.sendSimpleEmail(email, "Test Email", "This is a test email from the admin controller.")
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to send test email", e);
            }

            // Log the test email attempt as a system event
            auditService.logSystemEventAsync(
                    "EMAIL_TEST_SENT",
                    "Test email sent to " + LoggingUtil.maskEmail(email) + " from "
                            + getClientIpAddress(httpRequest));

            log.info("Test email sent successfully to: {}", LoggingUtil.maskEmail(email));

            TestEmailResponse testEmailResponse = TestEmailResponse.builder()
                    .success(true)
                    .message("Test email sent successfully")
                    .email(LoggingUtil.maskEmail(email))
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(testEmailResponse, "Test email sent successfully"));

        } catch (Exception e) {
            log.error("Test email failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send test email: " + e.getMessage()));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
