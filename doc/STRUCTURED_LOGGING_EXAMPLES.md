# Structured Logging Examples for NexSplit

This document provides examples of how to use `StructuredLoggingUtil` for Elasticsearch integration in your NexSplit application.

## Overview

The `StructuredLoggingUtil` automatically sends structured logs to Elasticsearch via the Logback configuration. All logs are automatically indexed in Elasticsearch and can be viewed in Kibana dashboards.

## Log Categories

### 1. Business Events

For user actions, registrations, logins, etc.

```java
// User registration
StructuredLoggingUtil.logBusinessEvent(
    "USER_REGISTRATION",
    user.getEmail(),
    "CREATE_ACCOUNT",
    "SUCCESS",
    Map.of(
        "source", "WEB",
        "ipAddress", ipAddress,
        "userAgent", userAgent,
        "userType", "REGULAR"
    )
);

// User login
StructuredLoggingUtil.logBusinessEvent(
    "USER_LOGIN",
    email,
    "AUTHENTICATE",
    "SUCCESS",
    Map.of(
        "source", "WEB",
        "ipAddress", ipAddress,
        "userAgent", userAgent,
        "authMethod", "EMAIL_PASSWORD"
    )
);

// Profile update
StructuredLoggingUtil.logBusinessEvent(
    "PROFILE_UPDATE",
    email,
    "UPDATE_PROFILE",
    "SUCCESS",
    Map.of(
        "source", "WEB",
        "updatedFields", "firstName,lastName"
    )
);
```

### 2. Security Events

For authentication failures, suspicious activity, etc.

```java
// Failed login attempt
StructuredLoggingUtil.logSecurityEvent(
    "LOGIN_FAILURE",
    email,
    ipAddress,
    userAgent,
    "MEDIUM",
    Map.of(
        "attemptCount", 3,
        "reason", "INVALID_PASSWORD",
        "blocked", false
    )
);

// Suspicious activity
StructuredLoggingUtil.logSecurityEvent(
    "SUSPICIOUS_ACTIVITY",
    email,
    ipAddress,
    userAgent,
    "HIGH",
    Map.of(
        "activityType", "MULTIPLE_FAILED_LOGINS",
        "timeWindow", "5 minutes",
        "attempts", 10
    )
);
```

### 3. Performance Events

For method execution times, slow queries, etc.

```java
// Slow database query
StructuredLoggingUtil.logPerformanceEvent(
    "UserService.findByEmail",
    1500L,
    "SUCCESS",
    Map.of(
        "queryType", "SELECT",
        "table", "users",
        "rowsReturned", 1
    )
);

// Method execution time
StructuredLoggingUtil.logPerformanceEvent(
    "AuthController.login",
    200L,
    "SUCCESS",
    Map.of(
        "validationTime", 50L,
        "databaseTime", 100L,
        "tokenGenerationTime", 50L
    )
);
```

### 4. Error Events

For exceptions and system failures.

```java
// Database connection error
StructuredLoggingUtil.logErrorEvent(
    "DATABASE_CONNECTION_ERROR",
    "Connection timeout to PostgreSQL",
    e.getStackTrace().toString(),
    Map.of(
        "database", "nexsplit_prod",
        "retryCount", 3,
        "connectionPool", "HikariCP"
    )
);

// Validation error
StructuredLoggingUtil.logErrorEvent(
    "VALIDATION_ERROR",
    "Invalid email format provided",
    e.getStackTrace().toString(),
    Map.of(
        "field", "email",
        "value", "invalid-email",
        "validationRule", "EMAIL_FORMAT"
    )
);
```

## Controller Examples

### AuthController

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request, HttpServletResponse response) {
    String email = loginRequest.getEmail();
    String password = loginRequest.getPassword();
    String ipAddress = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");

    try {
        String accessToken = userServiceImpl.loginUser(email, password);

        // Log successful login
        StructuredLoggingUtil.logBusinessEvent(
            "USER_LOGIN",
            email,
            "AUTHENTICATE",
            "SUCCESS",
            Map.of(
                "source", "WEB",
                "ipAddress", ipAddress,
                "userAgent", userAgent,
                "authMethod", "EMAIL_PASSWORD"
            )
        );

        // ... rest of login logic
        return ResponseEntity.ok(authResponse);

    } catch (SecurityException e) {
        // Log failed login attempt
        StructuredLoggingUtil.logSecurityEvent(
            "LOGIN_FAILURE",
            email,
            ipAddress,
            userAgent,
            "MEDIUM",
            Map.of(
                "reason", e.getMessage(),
                "attemptCount", getFailedAttempts(email)
            )
        );
        throw e;
    }
}
```

### UserController

```java
@PutMapping("/profile")
public ResponseEntity<UserProfileDto> updateUserProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UpdateUserDto updateUserDto) {

    String email = userDetails.getUsername();

    try {
        UserProfileDto updatedProfile = userServiceImpl.updateUserProfile(email, updateUserDto);

        // Log successful profile update
        StructuredLoggingUtil.logBusinessEvent(
            "PROFILE_UPDATE",
            email,
            "UPDATE_PROFILE",
            "SUCCESS",
            Map.of(
                "source", "WEB",
                "updatedFields", getUpdatedFields(updateUserDto)
            )
        );

        return ResponseEntity.ok(updatedProfile);

    } catch (Exception e) {
        // Log profile update error
        StructuredLoggingUtil.logErrorEvent(
            "PROFILE_UPDATE_ERROR",
            "Failed to update user profile",
            e.getStackTrace().toString(),
            Map.of(
                "email", LoggingUtil.maskEmail(email),
                "updateFields", updateUserDto.toString()
            )
        );
        throw e;
    }
}
```

## Service Layer Examples

### UserService

```java
@Service
public class UserServiceImpl implements UserService {

    public User registerUser(UserDto userDto) {
        long startTime = System.currentTimeMillis();

        try {
            // Registration logic
            User user = userRepository.save(newUser);

            long duration = System.currentTimeMillis() - startTime;

            // Log performance event
            StructuredLoggingUtil.logPerformanceEvent(
                "UserService.registerUser",
                duration,
                "SUCCESS",
                Map.of(
                    "userType", "REGULAR",
                    "validationTime", validationTime,
                    "databaseTime", databaseTime
                )
            );

            return user;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Log error event
            StructuredLoggingUtil.logErrorEvent(
                "USER_REGISTRATION_ERROR",
                "Failed to register user",
                e.getStackTrace().toString(),
                Map.of(
                    "email", LoggingUtil.maskEmail(userDto.getEmail()),
                    "duration", duration
                )
            );
            throw e;
        }
    }
}
```

## Best Practices

### 1. Always Include Context

```java
// ✅ Good - Rich context
StructuredLoggingUtil.logBusinessEvent(
    "USER_LOGIN",
    email,
    "AUTHENTICATE",
    "SUCCESS",
    Map.of(
        "source", "WEB",
        "ipAddress", ipAddress,
        "userAgent", userAgent,
        "authMethod", "EMAIL_PASSWORD",
        "sessionId", sessionId
    )
);

// ❌ Bad - Minimal context
StructuredLoggingUtil.logBusinessEvent(
    "USER_LOGIN",
    email,
    "AUTHENTICATE",
    "SUCCESS",
    null
);
```

### 2. Use Appropriate Event Types

```java
// Business events for user actions
StructuredLoggingUtil.logBusinessEvent(...);

// Security events for auth failures, suspicious activity
StructuredLoggingUtil.logSecurityEvent(...);

// Performance events for timing information
StructuredLoggingUtil.logPerformanceEvent(...);

// Error events for exceptions and failures
StructuredLoggingUtil.logErrorEvent(...);
```

### 3. Mask Sensitive Data

```java
// ✅ Good - Masked sensitive data
StructuredLoggingUtil.logBusinessEvent(
    "USER_LOGIN",
    LoggingUtil.maskEmail(email),  // Masked email
    "AUTHENTICATE",
    "SUCCESS",
    Map.of("token", LoggingUtil.maskSensitiveData(token))  // Masked token
);

// ❌ Bad - Exposed sensitive data
StructuredLoggingUtil.logBusinessEvent(
    "USER_LOGIN",
    email,  // Exposed email
    "AUTHENTICATE",
    "SUCCESS",
    Map.of("token", token)  // Exposed token
);
```

### 4. Consistent Event Names

```java
// Use consistent event names across the application
"USER_REGISTRATION"
"USER_LOGIN"
"USER_LOGOUT"
"PROFILE_UPDATE"
"PROFILE_VIEW"
"PASSWORD_CHANGE"
"PASSWORD_RESET"
```

## Elasticsearch Indexes

Your logs will be automatically indexed in Elasticsearch with the following patterns:

- **Business Events**: `nexsplit-logs-business-*`
- **Security Events**: `nexsplit-logs-security-*`
- **Performance Events**: `nexsplit-logs-performance-*`
- **Error Events**: `nexsplit-logs-error-*`

## Kibana Queries

### Find All User Logins

```
eventType: BUSINESS_EVENT AND eventName: USER_LOGIN
```

### Find Failed Login Attempts

```
eventType: SECURITY_EVENT AND eventName: LOGIN_FAILURE
```

### Find Slow Methods (>1000ms)

```
eventType: PERFORMANCE_EVENT AND durationMs:>1000
```

### Find Recent Errors

```
eventType: ERROR_EVENT AND @timestamp:[now-1h TO now]
```

## Testing

To test your structured logging:

1. Start your application with Elasticsearch
2. Make API calls to generate logs
3. Check Kibana to see the logs appear in real-time
4. Create dashboards to visualize the data

## Monitoring

Monitor these key metrics in Kibana:

- **User Registration Rate**: `eventName: USER_REGISTRATION`
- **Login Success Rate**: `eventName: USER_LOGIN AND result: SUCCESS`
- **Security Events**: `eventType: SECURITY_EVENT`
- **Error Rate**: `eventType: ERROR_EVENT`
- **Performance Issues**: `eventType: PERFORMANCE_EVENT AND durationMs:>1000`
