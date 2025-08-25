# Multi-stage build for Spring Boot application
# This approach creates a smaller final image by separating build and runtime environments

# ========================================
# STAGE 1: BUILD ENVIRONMENT
# ========================================
# Uses JDK (Java Development Kit) to compile the application
FROM eclipse-temurin:21-jdk-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper files first (for better layer caching)
# These files rarely change, so they're copied before dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the Maven wrapper executable
# This allows us to run Maven commands without installing Maven globally
RUN chmod +x mvnw

# Download all Maven dependencies
# This layer will be cached if pom.xml doesn't change, speeding up builds
RUN ./mvnw dependency:go-offline -B

# Copy the source code
# This is done after dependencies to leverage Docker layer caching
COPY src src

# Build the application and create the JAR file
# -DskipTests skips unit tests to speed up the build process
RUN ./mvnw clean package -DskipTests

# ========================================
# STAGE 2: RUNTIME ENVIRONMENT
# ========================================
# Uses JRE (Java Runtime Environment) - much smaller than JDK
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user for security
# This prevents the application from running with root privileges
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set the working directory for the runtime container
WORKDIR /app

# Copy the built JAR file from the build stage
# This is the only artifact we need from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the entrypoint script
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# Create logs directory and set proper ownership
# This ensures the application can write logs
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app && \
    chmod 755 /app/logs && \
    chmod +x /app/docker-entrypoint.sh

# Switch to the non-root user for security
USER appuser

# Expose the port the application runs on
# This is documentation - actual port mapping is done in docker-compose
EXPOSE 8080

# Health check configuration
# Docker will periodically check if the application is healthy
# - interval: check every 30 seconds
# - timeout: wait 3 seconds for response
# - start-period: wait 60 seconds before first check (app startup time)
# - retries: mark unhealthy after 3 failed attempts
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Command to run when the container starts
# This starts the Spring Boot application with proper setup
ENTRYPOINT ["/app/docker-entrypoint.sh"]
