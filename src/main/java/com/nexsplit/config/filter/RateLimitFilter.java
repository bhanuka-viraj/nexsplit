package com.nexsplit.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.dto.RateLimitInfo;
import com.nexsplit.service.RateLimitService;
import com.nexsplit.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to apply rate limiting to incoming requests
 * Extracts client identifier from JWT token (email) or IP address
 * Applies endpoint-specific rate limits
 */
@Component
@Order(3) // After CorrelationIdFilter and ResponseHeaderFilter
@Slf4j
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract client identifier (email from JWT token)
        String clientId = extractClientId(httpRequest);
        String endpoint = httpRequest.getRequestURI();

        // Check rate limit
        if (!rateLimitService.isAllowed(clientId, endpoint)) {
            // Rate limit exceeded
            RateLimitInfo info = rateLimitService.getRateLimitInfo(clientId);

            // Set HTTP status and headers
            httpResponse.setStatus(429); // Too Many Requests
            addRateLimitHeaders(httpResponse, info);

            // Return error response
            ApiResponse<Void> errorResponse = ApiResponse.<Void>error(
                    "Rate limit exceeded. Please try again later.",
                    ErrorCode.RATE_LIMIT_EXCEEDED);
            errorResponse.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        // Add rate limit headers to successful response
        RateLimitInfo info = rateLimitService.getRateLimitInfo(clientId);
        if (info != null) {
            addRateLimitHeaders(httpResponse, info);
        }

        chain.doFilter(request, response);
    }

    /**
     * Extract client identifier from request
     * Priority: JWT token email > IP address
     */
    private String extractClientId(HttpServletRequest request) {
        // Try to extract from JWT token first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String email = jwtUtil.getEmailFromToken(token);
                if (email != null && !email.isEmpty()) {
                    return email; // Use email as client identifier
                }
            } catch (Exception e) {
                log.debug("Failed to extract email from JWT token: {}", e.getMessage());
            }
        }

        // Fallback to IP address
        String ipAddress = getClientIpAddress(request);
        log.debug("Using IP address as client identifier: {}", ipAddress);
        return ipAddress;
    }

    /**
     * Get client IP address considering proxy headers
     */
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

    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, RateLimitInfo info) {
        response.addHeader("X-Rate-Limit-Limit", String.valueOf(info.getMaxRequests()));
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(info.getRemaining()));
        response.addHeader("X-Rate-Limit-Reset", String.valueOf(info.getResetTime()));
        response.addHeader("X-Rate-Limit-Window", "60"); // 60 seconds window
    }
}
