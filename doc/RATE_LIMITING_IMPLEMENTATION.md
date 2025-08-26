# Rate Limiting Implementation Guide

## Overview

This document describes the in-memory rate limiting implementation for the NexSplit API. The system provides protection against abuse, ensures fair usage, and maintains system stability.

## Architecture

### Core Components

#### 1. RateLimitInfo

```java
public class RateLimitInfo {
    private int requestCount;           // Current request count
    private LocalDateTime windowStart;  // When this window started
    private final int maxRequests;      // Max requests allowed
    private final int windowSeconds;    // Time window in seconds
}
```

**Features:**

- **Fixed Window Rate Limiting**: Simple time-based windows
- **Automatic Reset**: Windows reset when expired
- **Thread-Safe**: Safe for concurrent access
- **Memory Efficient**: ~200 bytes per entry

#### 2. RateLimitService

```java
public interface RateLimitService {
    boolean isAllowed(String clientId);
    boolean isAllowed(String clientId, String endpoint);
    RateLimitInfo getRateLimitInfo(String clientId);
    void cleanupExpiredEntries();
}
```

**Features:**

- **Endpoint-Specific Limits**: Different limits for different endpoints
- **Client Identification**: Uses email from JWT token or IP address
- **Automatic Cleanup**: Removes expired entries to prevent memory leaks
- **Structured Logging**: Logs rate limit violations for monitoring

#### 3. RateLimitFilter

```java
@Component
@Order(3) // After CorrelationIdFilter and ResponseHeaderFilter
public class RateLimitFilter implements Filter {
    // Intercepts all requests and applies rate limiting
}
```

**Features:**

- **Request Interception**: Filters all incoming requests
- **Client Extraction**: Extracts email from JWT token or falls back to IP
- **Response Headers**: Adds rate limit headers to all responses
- **Error Handling**: Returns 429 status with proper error response

## Client Identification Strategy

### Priority Order:

1. **JWT Token Email** (Primary): `sub` claim from JWT token
2. **IP Address** (Fallback): Client's IP address

### Implementation:

```java
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
    return ipAddress;
}
```

### Benefits:

- **User-Based**: Rate limits follow users across devices
- **Secure**: Uses authenticated user identity
- **Fallback**: Works for unauthenticated requests
- **Proxy Support**: Handles X-Forwarded-For headers

## Rate Limit Configuration

### Default Limits:

```java
private static final int DEFAULT_REQUESTS = 100;  // 100 requests
private static final int DEFAULT_WINDOW = 60;     // per minute
```

### Endpoint-Specific Limits:

```java
private static final Map<String, Integer> ENDPOINT_LIMITS = Map.of(
    "/api/v1/auth/login", 10,      // 10 login attempts per minute
    "/api/v1/auth/register", 5,    // 5 registrations per minute
    "/api/v1/auth/verify-email", 10, // 10 email verifications per minute
    "/api/v1/auth/reset-password", 5, // 5 password resets per minute
    "/api/v1/expenses", 200,       // 200 expense operations per minute
    "/api/v1/nex", 100,            // 100 nex operations per minute
    "/api/v1/events/stream", 100   // 100 SSE connections per minute
);
```

### Rationale:

- **Authentication Endpoints**: Lower limits to prevent brute force attacks
- **Business Endpoints**: Higher limits for normal usage
- **SSE Endpoints**: Moderate limits for real-time updates

## Response Headers

### Success Response:

```http
HTTP/1.1 200 OK
X-Rate-Limit-Limit: 100
X-Rate-Limit-Remaining: 85
X-Rate-Limit-Reset: 1642234560
X-Rate-Limit-Window: 60
```

### Rate Limit Exceeded:

```http
HTTP/1.1 429 Too Many Requests
X-Rate-Limit-Limit: 100
X-Rate-Limit-Remaining: 0
X-Rate-Limit-Reset: 1642234560
X-Rate-Limit-Window: 60
Retry-After: 60

{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "errorCode": "RATE_001",
  "errorType": "RATE_LIMIT_ERROR",
  "timestamp": "2024-01-15T10:30:00",
  "correlationId": "abc123"
}
```

## Memory Management

### Automatic Cleanup:

```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void cleanupExpiredEntries() {
    rateLimits.entrySet().removeIf(entry ->
        entry.getValue().isWindowExpired()
    );
}
```

### Memory Usage Estimation:

```java
// Example: 10,000 active users
// Each RateLimitInfo: ~200 bytes
// Total memory: 10,000 * 200 bytes = 2MB
// Very manageable for most applications
```

### Benefits:

- **Prevents Memory Leaks**: Removes expired entries
- **Efficient**: Only runs every 5 minutes
- **Monitored**: Logs cleanup statistics
- **Thread-Safe**: Uses ConcurrentHashMap

## Virtual Threads for Scheduled Tasks

### Configuration:

```java
@Bean(name = "scheduledTaskExecutor")
public Executor scheduledTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Use virtual threads for scheduled tasks
    executor.setTaskDecorator(runnable -> {
        return Thread.ofVirtual().unstarted(runnable);
    });

    executor.setCorePoolSize(5);  // Minimum threads
    executor.setMaxPoolSize(20);  // Maximum threads
    executor.setQueueCapacity(100); // Queue size
    executor.setThreadNamePrefix("Scheduled-");

    return executor;
}
```

### Why Virtual Threads for Scheduled Tasks:

- **I/O Bound**: Most scheduled tasks are I/O operations (cleanup, logging)
- **Efficient**: Virtual threads handle I/O better than platform threads
- **Scalable**: Can handle many concurrent scheduled tasks
- **Resource Efficient**: Lower memory usage

## Monitoring and Logging

### Structured Logging:

```java
// Rate limit violations
StructuredLoggingUtil.logSecurityEvent(
    "RATE_LIMIT_EXCEEDED",
    clientId,
    "unknown",
    "unknown",
    "MEDIUM",
    Map.of(
        "endpoint", endpoint,
        "limit", info.getMaxRequests(),
        "window", DEFAULT_WINDOW,
        "remaining", info.getRemaining()
    )
);

// Cleanup operations
StructuredLoggingUtil.logBusinessEvent(
    "RATE_LIMIT_CLEANUP",
    "system",
    "cleanup_expired_entries",
    "SUCCESS",
    Map.of(
        "entriesRemoved", removed,
        "currentSize", afterSize
    )
);
```

### Kibana Dashboards:

- **Rate Limit Violations**: Monitor abuse patterns
- **Cleanup Statistics**: Track memory usage
- **Endpoint Usage**: Identify popular endpoints
- **Client Patterns**: Detect suspicious behavior

## Usage Examples

### Scenario 1: Normal User Activity

```http
# User makes 50 requests in 1 minute
GET /api/v1/expenses
Authorization: Bearer user123-token

# Response headers
X-Rate-Limit-Limit: 200
X-Rate-Limit-Remaining: 150
X-Rate-Limit-Reset: 1642234560
```

### Scenario 2: Rate Limit Exceeded

```http
# User makes 201st request in 1 minute
GET /api/v1/expenses
Authorization: Bearer user123-token

# Response: 429 Too Many Requests
HTTP/1.1 429 Too Many Requests
X-Rate-Limit-Limit: 200
X-Rate-Limit-Remaining: 0
X-Rate-Limit-Reset: 1642234560
Retry-After: 60
```

### Scenario 3: Window Reset

```http
# User makes request after window expires
GET /api/v1/expenses
Authorization: Bearer user123-token

# Response headers (window reset)
X-Rate-Limit-Limit: 200
X-Rate-Limit-Remaining: 199
X-Rate-Limit-Reset: 1642234620
```

## Best Practices

### 1. Client Integration

```typescript
// Mobile app should handle rate limit responses
class ApiClient {
  async request<T>(endpoint: string): Promise<ApiResponse<T>> {
    const response = await fetch(endpoint);

    if (response.status === 429) {
      const retryAfter = response.headers.get("Retry-After");
      throw new RateLimitError(retryAfter);
    }

    return response.json();
  }
}
```

### 2. Monitoring

- **Alert on High Violations**: Set up alerts for unusual rate limit activity
- **Track Cleanup Performance**: Monitor memory usage and cleanup efficiency
- **Analyze Patterns**: Identify endpoints with high rate limit usage

### 3. Configuration

- **Start Conservative**: Begin with lower limits and adjust based on usage
- **Monitor Impact**: Track how rate limits affect user experience
- **Gradual Adjustments**: Make small changes and observe effects

## Future Enhancements

### 1. Redis-Based Rate Limiting

- **Distributed**: Works across multiple server instances
- **Persistent**: Survives application restarts
- **Scalable**: Handles high user counts

### 2. Dynamic Rate Limiting

- **User Tiers**: Different limits for free vs premium users
- **Time-Based**: Different limits during peak hours
- **Geographic**: Different limits by region

### 3. Advanced Algorithms

- **Sliding Window**: Smoother traffic distribution
- **Token Bucket**: Better burst handling
- **Leaky Bucket**: Controlled rate limiting

## Troubleshooting

### Common Issues:

#### 1. Rate Limits Too Strict

```java
// Adjust limits in RateLimitServiceImpl
private static final int DEFAULT_REQUESTS = 200; // Increase from 100
```

#### 2. Memory Usage High

```java
// Reduce cleanup interval
@Scheduled(fixedRate = 60000) // Every 1 minute instead of 5
```

#### 3. Client Identification Issues

```java
// Check JWT token extraction
String email = jwtUtil.getEmailFromToken(token);
log.debug("Extracted email: {}", email);
```

### Debug Mode:

```java
// Enable debug logging
logging.level.com.nexsplit.config.filter.RateLimitFilter=DEBUG
logging.level.com.nexsplit.service.impl.RateLimitServiceImpl=DEBUG
```

## Conclusion

The in-memory rate limiting implementation provides:

- **Security**: Protection against abuse and brute force attacks
- **Performance**: Fast in-memory operations with minimal overhead
- **Scalability**: Efficient for moderate user bases
- **Monitoring**: Comprehensive logging and metrics
- **Flexibility**: Easy to configure and adjust

This implementation is suitable for the current NexSplit deployment and can be upgraded to Redis-based rate limiting when horizontal scaling is needed.
