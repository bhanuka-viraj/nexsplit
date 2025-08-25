#!/bin/sh

# Ensure logs directory exists and has proper permissions
mkdir -p /app/logs
chmod 755 /app/logs

# Start the Spring Boot application
exec java -jar app.jar
