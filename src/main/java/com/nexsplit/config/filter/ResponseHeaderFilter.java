package com.nexsplit.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add standard response headers for monitoring and debugging
 * Adds headers like X-Request-ID, X-Response-Time, X-Rate-Limit-Remaining
 */
@Component
@Order(2)
@Slf4j
public class ResponseHeaderFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String RESPONSE_TIME_HEADER = "X-Response-Time";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-Rate-Limit-Remaining";
    private static final String API_VERSION_HEADER = "X-API-Version";
    private static final String SERVER_HEADER = "X-Server";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate request ID if not present
        String requestId = getRequestId(httpRequest);

        // Record start time
        long startTime = System.currentTimeMillis();

        // Add standard headers
        addStandardHeaders(httpResponse, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Calculate response time
            long responseTime = System.currentTimeMillis() - startTime;

            // Add response time header
            httpResponse.addHeader(RESPONSE_TIME_HEADER, responseTime + "ms");

            // Add rate limit header (placeholder - implement actual rate limiting later)
            httpResponse.addHeader(RATE_LIMIT_REMAINING_HEADER, "999");

            log.debug("Request completed - ID: {}, Time: {}ms, Status: {}",
                    requestId, responseTime, httpResponse.getStatus());
        }
    }

    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    private void addStandardHeaders(HttpServletResponse response, String requestId) {
        response.addHeader(REQUEST_ID_HEADER, requestId);
        response.addHeader(API_VERSION_HEADER, "1.0.0");
        response.addHeader(SERVER_HEADER, "NexSplit-API");
        response.addHeader("X-Powered-By", "Spring Boot");
    }
}
