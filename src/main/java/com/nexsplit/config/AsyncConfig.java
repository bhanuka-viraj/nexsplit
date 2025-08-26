package com.nexsplit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous processing using virtual threads
 * 
 * WHY WE NEED THIS:
 * =================
 * 1. DEFAULT BEHAVIOR: Spring Boot's default async executor uses platform
 * threads
 * - Limited scalability (typically 200-400 threads max)
 * - High memory usage per thread (~1MB per thread)
 * - Context switching overhead
 * 
 * 2. VIRTUAL THREADS BENEFITS:
 * - Can handle millions of concurrent operations
 * - Much lower memory usage (~1KB per virtual thread)
 * - Better performance for I/O-bound operations
 * - Automatic scheduling by JVM
 * 
 * 3. USE CASES IN NEXSPLIT:
 * - External API calls (HTTP requests)
 * - File processing (PDF generation, reports)
 * - Background data processing
 * - Audit logging
 * - Notification sending
 * 
 * HOW TO USE:
 * ===========
 * 1. Add @Async annotation to methods that should run asynchronously
 * 2. Methods can return CompletableFuture<T> for better control
 * 3. Use @Async("asyncExecutor") to specify this executor
 * 
 * EXAMPLES:
 * =========
 * @Async("asyncExecutor")
 * public CompletableFuture<String> callExternalApiAsync(String url) {
 * // This will run in a virtual thread
 * return CompletableFuture.completedFuture("API call completed");
 * }
 * 
 * @Async("asyncExecutor")
 * public void logAuditEventAsync(AuditEvent event) {
 * // Background logging without blocking the main thread
 * auditService.log(event);
 * }
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Configure async executor using virtual threads
     * 
     * CONFIGURATION EXPLANATION:
     * - Core Pool Size: 10 (minimum threads always available)
     * - Max Pool Size: 100 (maximum threads for burst traffic)
     * - Queue Capacity: 500 (tasks waiting to be executed)
     * - Virtual Threads: Each task gets its own virtual thread
     * 
     * VIRTUAL THREAD ADVANTAGES:
     * - Automatic scaling based on workload
     * - No manual thread pool management needed
     * - Better resource utilization
     * - Reduced memory footprint
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Use virtual threads for better scalability
        executor.setTaskDecorator(runnable -> {
            // Create virtual thread for each task
            // This is the key difference from platform threads
            return Thread.ofVirtual().unstarted(runnable);
        });

        // Configure pool size for virtual threads
        // These numbers are much higher than platform threads
        executor.setCorePoolSize(10); // Minimum threads always running
        executor.setMaxPoolSize(100); // Maximum threads for peak load
        executor.setQueueCapacity(500); // Tasks waiting in queue
        executor.setThreadNamePrefix("Async-"); // Easy identification in logs
        executor.initialize();

        return executor;
    }

    /**
     * Alternative executor for CPU-intensive tasks
     * 
     * WHY SEPARATE EXECUTOR:
     * - Virtual threads are great for I/O operations
     * - CPU-intensive tasks should use platform threads
     * - Prevents blocking the virtual thread scheduler
     * 
     * USE CASES:
     * - Complex calculations
     * - Data processing
     * - Encryption/decryption
     * - Image processing
     */
    @Bean(name = "cpuIntensiveExecutor")
    public Executor cpuIntensiveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Use platform threads for CPU-intensive tasks
        executor.setCorePoolSize(4); // Match CPU cores
        executor.setMaxPoolSize(8); // 2x CPU cores for burst
        executor.setQueueCapacity(100); // Smaller queue for CPU tasks
        executor.setThreadNamePrefix("CPU-");
        executor.initialize();

        return executor;
    }

    /**
     * Configure scheduled task executor using virtual threads
     * 
     * WHY VIRTUAL THREADS FOR SCHEDULED TASKS:
     * - Most scheduled tasks are I/O bound (cleanup, health checks, etc.)
     * - Virtual threads handle I/O operations efficiently
     * - Better resource utilization for background tasks
     * - Automatic scaling based on task load
     * 
     * SCHEDULED TASKS IN NEXSPLIT:
     * - Rate limit cleanup (every 5 minutes)
     * - Database cleanup (daily)
     * - Health checks (every minute)
     * - Cache refresh (periodic)
     * 
     * CONFIGURATION:
     * - Core Pool Size: 5 (minimum for scheduled tasks)
     * - Max Pool Size: 20 (enough for concurrent scheduled tasks)
     * - Queue Capacity: 100 (scheduled tasks queue)
     */
    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Use virtual threads for scheduled tasks
        executor.setTaskDecorator(runnable -> {
            return Thread.ofVirtual().unstarted(runnable);
        });

        // Configure for scheduled tasks
        executor.setCorePoolSize(5); // Minimum threads for scheduled tasks
        executor.setMaxPoolSize(20); // Maximum for concurrent scheduled tasks
        executor.setQueueCapacity(100); // Queue for scheduled tasks
        executor.setThreadNamePrefix("Scheduled-"); // Easy identification
        executor.initialize();

        return executor;
    }
}
