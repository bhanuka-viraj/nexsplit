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
     * VIRTUAL THREADS IMPLEMENTATION:
     * - Each async task gets its own virtual thread
     * - Unlimited scalability (can handle millions of concurrent operations)
     * - Low memory usage (~1KB per virtual thread vs ~1MB per platform thread)
     * - Perfect for I/O-bound operations (email, HTTP, database)
     * 
     * HOW IT WORKS:
     * - SimpleAsyncTaskExecutor.doExecute() is called for each task
     * - Thread.ofVirtual().start() creates and immediately starts a virtual thread
     * - The virtual thread executes the task and terminates when done
     * - No thread pool management needed - JVM handles scheduling
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return new org.springframework.core.task.SimpleAsyncTaskExecutor("Async-") {
            @Override
            protected void doExecute(Runnable task) {
                // Create and start virtual thread for each async task
                Thread.ofVirtual()
                        .name("Async-" + System.currentTimeMillis() % 10000)
                        .start(task);
            }
        };
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
     * SCHEDULED TASKS WITH VIRTUAL THREADS:
     * - Rate limit cleanup (every 5 minutes)
     * - Database cleanup (daily)
     * - Health checks (every minute)
     * - Cache refresh (periodic)
     * 
     * BENEFITS:
     * - Most scheduled tasks are I/O bound (perfect for virtual threads)
     * - Better resource utilization for background operations
     * - Automatic scaling based on task load
     * - No thread pool management needed
     */
    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        return new org.springframework.core.task.SimpleAsyncTaskExecutor("Scheduled-") {
            @Override
            protected void doExecute(Runnable task) {
                // Create and start virtual thread for each scheduled task
                Thread.ofVirtual()
                        .name("Scheduled-" + System.currentTimeMillis() % 10000)
                        .start(task);
            }
        };
    }
}
