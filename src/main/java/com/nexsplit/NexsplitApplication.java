package com.nexsplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for NexSplit expense tracking system.
 * 
 * NexSplit is a comprehensive expense tracking and splitting application that
 * allows
 * users to create groups (nex), add expenses, split costs among members, and
 * track
 * settlements. The application provides real-time updates, file attachments,
 * and
 * comprehensive analytics.
 * 
 * Key Features:
 * - User authentication and authorization (JWT + OAuth2)
 * - Group (nex) management with member roles
 * - Expense tracking with multiple split types
 * - Debt calculation and settlement tracking
 * - File attachment support with CDN integration
 * - Real-time updates via Server-Sent Events (SSE)
 * - Comprehensive analytics and reporting
 * - Email notifications and reminders
 * - Rate limiting and security features
 * 
 * Architecture:
 * - Spring Boot 3.5.3 with Java 21
 * - PostgreSQL database with Flyway migrations
 * - MapStruct for DTO-entity mapping
 * - Database views for optimized queries
 * - CDN integration for file management
 * - Async processing for background tasks
 * - Comprehensive logging and monitoring
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class NexsplitApplication {

	/**
	 * Main entry point for the NexSplit application.
	 * 
	 * This method initializes the Spring Boot application with all necessary
	 * configurations including async processing, scheduling, and JPA auditing.
	 * 
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(NexsplitApplication.class, args);
	}
}
