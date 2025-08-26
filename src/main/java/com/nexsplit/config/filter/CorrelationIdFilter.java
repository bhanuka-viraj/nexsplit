package com.nexsplit.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add correlation IDs to requests for distributed tracing
 * This enables tracking requests across different components and services
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get correlation ID from header or generate new one
        String correlationId = getCorrelationId(httpRequest);

        // Add correlation ID to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // Add correlation ID to response headers
        httpResponse.addHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            log.debug("Processing request with correlation ID: {} - URI: {}",
                    correlationId, httpRequest.getRequestURI());

            chain.doFilter(request, response);

            log.debug("Completed request with correlation ID: {} - Status: {}",
                    correlationId, httpResponse.getStatus());

        } finally {
            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Get correlation ID from request header or generate new one
     */
    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation ID: {}", correlationId);
        }

        return correlationId;
    }

    /**
     * Get current correlation ID from MDC
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }
}
