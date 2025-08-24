# Async Processing and Virtual Threads Guide

## Overview

This guide explains how to use asynchronous processing and virtual threads in the NexSplit application for better performance and scalability.

## Table of Contents

1. [Why We Need AsyncConfig](#why-we-need-asyncconfig)
2. [Virtual Threads vs Platform Threads](#virtual-threads-vs-platform-threads)
3. [Configuration Details](#configuration-details)
4. [How to Use Async Processing](#how-to-use-async-processing)
5. [Real-World Examples](#real-world-examples)
6. [Best Practices](#best-practices)
7. [Testing Async Operations](#testing-async-operations)

## Why We Need AsyncConfig

### Problem with Default Spring Boot Async

**Without AsyncConfig:**

```java
// Spring Boot's default async executor
- Uses platform threads (limited to ~200-400 threads)
- High memory usage (~1MB per thread)
- Context switching overhead
- Poor scalability for I/O operations
```

**With AsyncConfig:**

```java
// Our custom async executor with virtual threads
- Can handle millions of concurrent operations
- Low memory usage (~1KB per virtual thread)
- Better performance for I/O-bound operations
- Automatic scheduling by JVM
```

### Key Benefits

1. **Better Scalability**: Handle thousands of concurrent requests
2. **Lower Memory Usage**: Virtual threads use much less memory
3. **Improved Performance**: Better for I/O-bound operations
4. **Simpler Programming**: No complex async/await patterns needed

## Virtual Threads vs Platform Threads

### Platform Threads (Traditional)

```java
// Limited by OS thread limits
Thread platformThread = new Thread(() -> {
    // This uses a real OS thread
    // Limited by system resources
});
```

### Virtual Threads (Modern)

```java
// Unlimited scalability
Thread virtualThread = Thread.ofVirtual().unstarted(() -> {
    // This uses a virtual thread
    // Can have millions of these
});
```

### Comparison Table

| Aspect                | Platform Threads    | Virtual Threads            |
| --------------------- | ------------------- | -------------------------- |
| **Memory Usage**      | ~1MB per thread     | ~1KB per thread            |
| **Scalability**       | 200-400 threads     | Millions of threads        |
| **Best For**          | CPU-intensive tasks | I/O-bound operations       |
| **Context Switching** | Expensive           | Cheap                      |
| **Blocking**          | Blocks OS thread    | Blocks virtual thread only |

## Configuration Details

### AsyncConfig.java

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Use virtual threads for I/O operations
        executor.setTaskDecorator(runnable ->
            Thread.ofVirtual().unstarted(runnable)
        );

        // High numbers possible with virtual threads
        executor.setCorePoolSize(10);      // Minimum threads
        executor.setMaxPoolSize(100);      // Maximum threads
        executor.setQueueCapacity(500);    // Waiting tasks
        executor.setThreadNamePrefix("Async-");
        executor.initialize();

        return executor;
    }

    @Bean(name = "cpuIntensiveExecutor")
    public Executor cpuIntensiveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Use platform threads for CPU-intensive tasks
        executor.setCorePoolSize(4);       // Match CPU cores
        executor.setMaxPoolSize(8);        // 2x CPU cores
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("CPU-");
        executor.initialize();

        return executor;
    }
}
```

### application.yml

```yaml
server:
  port: 8080
  # Enable virtual threads for HTTP requests
  threads:
    virtual: true
```

## How to Use Async Processing

### 1. Basic Async Method

```java
@Service
public class NotificationService {

    @Async("asyncExecutor")
    public CompletableFuture<String> sendNotificationAsync(String to, String subject) {
        // This runs in a virtual thread
        notificationService.send(to, subject);
        return CompletableFuture.completedFuture("Notification sent");
    }
}
```

### 2. Fire-and-Forget Pattern

```java
@Async("asyncExecutor")
public void logAuditEventAsync(String userId, String action) {
    // Don't wait for result
    auditService.log(userId, action);
}
```

### 3. Parallel Operations

```java
public CompletableFuture<String> processUserRegistration(String email) {
    // Start multiple operations in parallel
    CompletableFuture<String> notificationFuture = sendNotificationAsync(email);
    CompletableFuture<String> pdfFuture = generatePdfAsync(email);
    CompletableFuture<String> apiFuture = callExternalApiAsync(email);

    // Wait for all to complete
    return CompletableFuture.allOf(notificationFuture, pdfFuture, apiFuture)
        .thenApply(v -> "All operations completed");
}
```

### 4. Error Handling

```java
@Async("asyncExecutor")
public CompletableFuture<String> riskyOperationAsync() {
    try {
        // Risky operation
        return CompletableFuture.completedFuture("Success");
    } catch (Exception e) {
        return CompletableFuture.failedFuture(e);
    }
}
```

## Real-World Examples

### Notification Service

```java
@Service
public class NotificationService {

    @Async("asyncExecutor")
    public CompletableFuture<String> sendWelcomeNotificationAsync(String email, String name) {
        try {
            // Simulate notification operation
            Thread.sleep(500);

            // Log the event
            StructuredLoggingUtil.logBusinessEvent(
                "NOTIFICATION_SENT",
                email,
                "SEND_WELCOME_NOTIFICATION",
                "SUCCESS",
                Map.of("recipientName", name)
            );

            return CompletableFuture.completedFuture("Welcome notification sent to " + email);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

### Audit Logging

```java
@Service
public class AuditService {

    @Async("asyncExecutor")
    public void logUserActionAsync(String userId, String action, String details) {
        try {
            // Database write operation
            auditRepository.save(new AuditLog(userId, action, details));

            // Structured logging
            StructuredLoggingUtil.logBusinessEvent(
                "AUDIT_LOG",
                userId,
                action,
                "SUCCESS",
                Map.of("details", details)
            );
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        }
    }
}
```

### PDF Generation

```java
@Service
public class ReportService {

    @Async("cpuIntensiveExecutor")  // Use CPU executor for heavy processing
    public CompletableFuture<String> generatePdfReportAsync(String userId, String reportType) {
        try {
            // CPU-intensive PDF generation
            byte[] pdfBytes = pdfGenerator.generate(reportType);

            // Save to storage
            String filePath = storageService.save(pdfBytes, userId + "_" + reportType + ".pdf");

            return CompletableFuture.completedFuture(filePath);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

## Best Practices

### 1. Choose the Right Executor

```java
// For I/O operations (email, HTTP, database)
@Async("asyncExecutor")

// For CPU-intensive operations (PDF generation, calculations)
@Async("cpuIntensiveExecutor")
```

### 2. Handle Exceptions Properly

```java
@Async("asyncExecutor")
public CompletableFuture<String> safeAsyncOperation() {
    try {
        // Your async operation
        return CompletableFuture.completedFuture("Success");
    } catch (Exception e) {
        log.error("Async operation failed", e);
        return CompletableFuture.failedFuture(e);
    }
}
```

### 3. Use CompletableFuture for Better Control

```java
// Good: Return CompletableFuture
@Async("asyncExecutor")
public CompletableFuture<String> operationWithResult() {
    return CompletableFuture.completedFuture("Result");
}

// Also good: Fire-and-forget
@Async("asyncExecutor")
public void fireAndForgetOperation() {
    // No return value needed
}
```

### 4. Set Reasonable Timeouts

```java
public String operationWithTimeout() {
    CompletableFuture<String> future = asyncService.someOperation();

    try {
        return future.get(5, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        return "Operation timed out";
    }
}
```

### 5. Avoid Blocking Operations in Virtual Threads

```java
// Bad: CPU-intensive in virtual thread
@Async("asyncExecutor")
public void heavyCalculation() {
    // This blocks the virtual thread scheduler
    for (int i = 0; i < 1000000; i++) {
        Math.sqrt(i);
    }
}

// Good: Use CPU executor for heavy calculations
@Async("cpuIntensiveExecutor")
public void heavyCalculation() {
    // This uses platform threads
    for (int i = 0; i < 1000000; i++) {
        Math.sqrt(i);
    }
}
```

## Testing Async Operations

### Unit Testing

```java
@SpringBootTest
class AsyncServiceTest {

    @Autowired
    private AsyncExampleService asyncService;

    @Test
    void testAsyncEmailSending() throws Exception {
        // Start async operation
        CompletableFuture<String> future = asyncService.sendNotificationAsync("test@example.com", "Test", "Content");

        // Wait for result
        String result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertThat(result).contains("Email sent");
    }
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AsyncControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testAsyncEndpoint() {
        // Test async endpoint
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/async-examples/fire-and-forget?email=test@example.com",
            null,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Operation started in background");
    }
}
```

## Performance Monitoring

### Logging Async Operations

```java
@Async("asyncExecutor")
public CompletableFuture<String> monitoredAsyncOperation() {
    long startTime = System.currentTimeMillis();

    try {
        // Your async operation
        String result = performOperation();

        // Log performance
        long duration = System.currentTimeMillis() - startTime;
        StructuredLoggingUtil.logPerformanceEvent(
            "monitoredAsyncOperation",
            duration,
            "SUCCESS",
            Map.of("thread", Thread.currentThread().getName())
        );

        return CompletableFuture.completedFuture(result);
    } catch (Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        StructuredLoggingUtil.logPerformanceEvent(
            "monitoredAsyncOperation",
            duration,
            "FAILURE",
            Map.of("error", e.getMessage())
        );
        return CompletableFuture.failedFuture(e);
    }
}
```

## Summary

The `AsyncConfig` is essential because:

1. **Default Spring Boot async is limited** - Uses platform threads with poor scalability
2. **Virtual threads provide better performance** - Can handle millions of concurrent operations
3. **Different executors for different tasks** - I/O vs CPU-intensive operations
4. **Better resource utilization** - Lower memory usage and better scheduling

### Key Takeaways

- ✅ Use `@Async("asyncExecutor")` for I/O operations
- ✅ Use `@Async("cpuIntensiveExecutor")` for CPU-intensive tasks
- ✅ Return `CompletableFuture<T>` for better control
- ✅ Handle exceptions properly
- ✅ Set reasonable timeouts
- ✅ Monitor performance with structured logging

This configuration enables your NexSplit application to handle high concurrency efficiently while maintaining good performance and resource utilization.
