# 📋 Logging Guidelines for NexSplit

## Table of Contents

1. [Overview](#overview)
2. [Logging Architecture](#logging-architecture)
3. [Structured Logging](#structured-logging)
4. [Audit Logging](#audit-logging)
5. [Email Logging](#email-logging)
6. [Security Logging](#security-logging)
7. [Log Levels](#log-levels)
8. [Best Practices](#best-practices)
9. [Configuration](#configuration)
10. [Monitoring and Alerting](#monitoring-and-alerting)

---

## Overview

NexSplit implements a comprehensive logging strategy with multiple layers:

- **Structured Logging**: Technical logs for monitoring and debugging
- **Audit Logging**: Business events for compliance and security
- **Email Logging**: Communication tracking
- **Security Logging**: Authentication and security events

### Key Principles

1. **Dual Logging**: Both structured logs and audit trails
2. **Async Operations**: Non-blocking logging for performance
3. **Virtual Threads**: Scalable logging infrastructure
4. **Compliance Ready**: GDPR, SOX, and security audit support
5. **Kibana Optimized**: Structured format for easy querying

---

## Logging Architecture

### Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │  Structured     │    │   Audit Events  │
│     Layer       │───▶│   Logging       │    │   Database      │
│                 │    │   (JSON)        │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       ▼                       ▼
         │              ┌─────────────────┐    ┌─────────────────┐
         │              │   Log Files     │    │   Kibana/ELK    │
         │              │   (logs/)       │    │   Stack         │
         │              └─────────────────┘    └─────────────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│   Email Service │    │   Security      │
│   (Async)       │    │   Monitoring    │
└─────────────────┘    └─────────────────┘
```

---

## Structured Logging

### Purpose

Technical logs for application monitoring, debugging, and performance analysis.

### Implementation

- **Utility**: `StructuredLoggingUtil`
- **Format**: JSON for Kibana compatibility
- **Location**: `logs/nexsplit.log`
- **Thread**: Virtual threads for scalability

### Usage Examples

```java
// Business event logging
StructuredLoggingUtil.logBusinessEvent(
    "USER_REGISTRATION",
    userId,
    "REGISTRATION_SUCCESS",
    "SUCCESS",
    Map.of(
        "email", maskedEmail,
        "username", username,
        "thread", Thread.currentThread().getName()
    )
);

// Error event logging
StructuredLoggingUtil.logErrorEvent(
    "AUTHENTICATION_FAILURE",
    "Invalid credentials",
    exception.getMessage(),
    Map.of("email", maskedEmail, "ip", ipAddress)
);
```

---

## Audit Logging

### Purpose

Business-level audit trail for compliance, security, and long-term record keeping.

### Implementation

- **Service**: `AuditService`
- **Model**: `AuditEvent`
- **Repository**: `AuditEventRepository`
- **Async**: Virtual thread execution

### Event Categories

#### 1. Security Events

```java
auditService.logSecurityEventAsync(
    userId,
    "TOKEN_THEFT",
    "Family compromised - all tokens revoked. Family ID: " + familyId
);
```

#### 2. Authentication Events

```java
auditService.logAuthenticationEventAsync(
    userId,
    "LOGIN_SUCCESS",
    ipAddress,
    userAgent,
    "Email/password login successful"
);
```

#### 3. User Actions

```java
auditService.logUserActionAsync(
    userId,
    "PROFILE_UPDATE",
    "User profile updated successfully"
);
```

#### 4. System Events

```java
auditService.logSystemEventAsync(
    "CLEANUP_COMPLETED",
    "Expired tokens cleaned up successfully"
);
```

### Severity Levels

| Level      | Description                 | Examples                                 |
| ---------- | --------------------------- | ---------------------------------------- |
| `LOW`      | Informational events        | System maintenance, routine operations   |
| `MEDIUM`   | Normal user actions         | Login, profile updates, password changes |
| `HIGH`     | Security events             | Token theft, suspicious activity         |
| `CRITICAL` | Critical security incidents | Account compromise, data breach          |

---

## Email Logging

### Purpose

Track email communications for user engagement and troubleshooting.

### Implementation

- **Async**: Virtual thread execution
- **Integration**: Both structured and audit logging

### Email Types

#### 1. Password Reset Emails

```java
// Password reset token generated (email sending removed)
    .exceptionally(throwable -> {
        log.error("Failed to send password reset email to: {}", maskedEmail, throwable);
        return "Email sending failed";
    });
```

#### 2. Welcome Emails

```java
// Welcome email sending removed
    .exceptionally(throwable -> {
        log.error("Failed to send welcome email to: {}", maskedEmail, throwable);
        return "Email sending failed";
    });
```

---

## Security Logging

### Purpose

Comprehensive security monitoring and incident response.

### Implementation

- **Integration**: Multiple services and controllers
- **Real-time**: Immediate security event detection
- **Comprehensive**: IP tracking, user agent, geolocation

### Security Events

#### 1. Authentication Monitoring

```java
// Login success
auditService.logAuthenticationEventAsync(
    user.getId(),
    "LOGIN_SUCCESS",
    getClientIpAddress(request),
    request.getHeader("User-Agent"),
    "Email/password login successful"
);

// Login failure
auditService.logSecurityEventAsync(
    null, // No user ID for failed attempts
    "LOGIN_FAILURE",
    "Failed login attempt for email: " + maskedEmail
);
```

#### 2. Token Security

```java
// Token theft detection
auditService.logSecurityEventAsync(
    compromisedToken.getUserId(),
    "TOKEN_THEFT",
    "Family compromised - all tokens revoked. Family ID: " + familyId
);
```

---

## Log Levels

### Standard Log Levels

| Level   | Usage               | Examples                                  |
| ------- | ------------------- | ----------------------------------------- |
| `ERROR` | Application errors  | Exceptions, failed operations             |
| `WARN`  | Potential issues    | Deprecated features, performance warnings |
| `INFO`  | General information | User actions, system events               |
| `DEBUG` | Detailed debugging  | Method entry/exit, variable values        |
| `TRACE` | Very detailed       | Loop iterations, detailed state           |

### Configuration

```yaml
logging:
  level:
    com.nexsplit: INFO
    com.nexsplit.security: DEBUG
    com.nexsplit.service: INFO
    com.nexsplit.controller: INFO
    org.springframework.security: WARN
```

---

## Best Practices

### 1. Log Message Guidelines

#### ✅ Good Examples

```java
// Clear, descriptive messages
log.info("User registered successfully: {}", LoggingUtil.maskEmail(email));
log.error("Failed to send email to: {}", maskedEmail, exception);
log.debug("Processing request for user: {}", userId);
```

#### ❌ Bad Examples

```java
// Vague messages
log.info("Something happened");
log.error("Error occurred");

// Sensitive data exposure
log.info("User password: {}", password);
log.debug("Credit card: {}", cardNumber);
```

### 2. Performance Considerations

#### Async Logging

```java
// ✅ Non-blocking audit logging
@Async("asyncExecutor")
public void logSecurityEventAsync(String userId, String eventType, String details) {
    // Logging operations
}
```

#### Virtual Threads

```java
// ✅ Virtual thread executor for logging
@Bean(name = "asyncExecutor")
public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(runnable -> {
        return Thread.ofVirtual().unstarted(runnable);
    });
    return executor;
}
```

### 3. Data Privacy

#### Sensitive Data Masking

```java
// ✅ Masked email logging
log.info("Processing request for: {}", LoggingUtil.maskEmail(email));

// ❌ Raw sensitive data
log.info("Processing request for: {}", email);
```

### 4. Error Handling

#### Comprehensive Error Logging

```java
try {
    // Business logic
} catch (SpecificException e) {
    log.error("Specific error occurred for user: {}", userId, e);
    auditService.logSecurityEventAsync(userId, "ERROR", e.getMessage());
} catch (Exception e) {
    log.error("Unexpected error for user: {}", userId, e);
    StructuredLoggingUtil.logErrorEvent(
        "UNEXPECTED_ERROR",
        "Unexpected exception",
        e.getMessage(),
        Map.of("userId", userId, "class", e.getClass().getSimpleName())
    );
}
```

---

## Configuration

### Application Properties

```yaml
# Logging Configuration
logging:
  file:
    name: logs/nexsplit.log
    max-size: 100MB
    max-history: 30
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n"
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n"
  level:
    com.nexsplit: INFO
    com.nexsplit.security: DEBUG
    org.springframework.security: WARN

# Email Configuration
app:
  email:
    from: ${MAIL_FROM:noreply@nexsplit.com}
    from-name: ${MAIL_FROM_NAME:NexSplit}
    base-url: ${APP_BASE_URL:http://localhost:8080}

# Audit Configuration
audit:
  retention:
    days: 2555 # 7 years for compliance
  cleanup:
    enabled: true
    schedule: "0 2 * * *" # Daily at 2 AM
```

---

## Monitoring and Alerting

### Key Metrics

#### 1. Application Health

- **Error Rate**: Percentage of failed requests
- **Response Time**: API endpoint performance
- **Throughput**: Requests per second
- **Memory Usage**: JVM heap utilization

#### 2. Security Metrics

- **Failed Login Attempts**: Authentication failures
- **Token Theft Events**: Security incidents
- **Suspicious Activity**: Unusual user behavior
- **Account Lockouts**: Security measures triggered

#### 3. Business Metrics

- **User Registrations**: New account creation
- **Email Delivery**: Communication success rate
- **Profile Updates**: User engagement
- **Password Changes**: Security awareness

### Alerting Rules

#### Critical Alerts

```yaml
# Security Incidents
- alert: TokenTheftDetected
  expr: increase(audit_events_total{event_type="TOKEN_THEFT"}[5m]) > 0
  for: 0m
  labels:
    severity: critical
  annotations:
    summary: "Token theft detected"
    description: "Refresh token compromise detected in the last 5 minutes"

# High Error Rate
- alert: HighErrorRate
  expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "High error rate detected"
    description: "Error rate is {{ $value }} errors per second"
```

---

## Compliance and Security

### GDPR Compliance

#### Data Retention

```yaml
audit:
  retention:
    personal_data: 30 days
    business_data: 7 years
    security_data: 10 years
```

#### Data Deletion

```java
@Scheduled(cron = "0 2 * * *") // Daily at 2 AM
public void cleanupExpiredData() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
    auditEventRepository.deleteByTimestampBefore(cutoff);
}
```

### SOX Compliance

#### Audit Trail Requirements

- **Data Integrity**: Immutable audit records
- **Access Control**: Secure audit log access
- **Retention**: 7-year minimum retention
- **Monitoring**: Real-time security monitoring

---

## Conclusion

This comprehensive logging strategy ensures:

1. **Observability**: Complete application visibility
2. **Security**: Comprehensive security monitoring
3. **Compliance**: Audit trail for regulatory requirements
4. **Performance**: Non-blocking async operations
5. **Scalability**: Virtual thread support
6. **Maintainability**: Structured and organized logging

The dual logging approach (structured + audit) provides both technical monitoring capabilities and business compliance requirements, making NexSplit ready for production deployment and regulatory scrutiny.
