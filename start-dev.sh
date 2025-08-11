#!/bin/bash

# ========================================
# LOCAL DEVELOPMENT STARTUP SCRIPT
# ========================================
# This script loads environment variables and starts the application
# Usage: ./start-dev.sh

set -e

echo "🚀 Starting NexSplit Local Development Environment..."

# ========================================
# LOAD ENVIRONMENT VARIABLES
# ========================================
echo "📋 Loading environment variables..."

# Check if .env.development exists
if [ -f ".env.development" ]; then
    echo "✅ Found .env.development file"
    
    # Load variables from .env.development file
    while IFS= read -r line; do
        # Skip comments and empty lines
        if [[ ! "$line" =~ ^[[:space:]]*# ]] && [[ -n "$line" ]]; then
            if [[ "$line" =~ ^([^=]+)=(.*)$ ]]; then
                name="${BASH_REMATCH[1]}"
                value="${BASH_REMATCH[2]}"
                export "$name=$value"
                echo "   Set $name"
            fi
        fi
    done < ".env.development"
else
    echo "⚠️  .env.development not found, using default values"
    
    # Set default development values
    export DB_PASSWORD="Viraj@2002"
    export JWT_SECRET="dev-jwt-secret-key-for-local-development-only"
    export JWT_EXPIRATION="60"
    export GOOGLE_CLIENT_ID="google-client-id"
    export GOOGLE_CLIENT_SECRET="google-client-secret"
    export SPRING_PROFILES_ACTIVE="dev"
    export FRONTEND_URL="http://localhost:3000"
fi

# ========================================
# VERIFY ENVIRONMENT VARIABLES
# ========================================
echo "🔍 Verifying environment variables..."

required_vars=("DB_PASSWORD" "JWT_SECRET" "JWT_EXPIRATION")
for var in "${required_vars[@]}"; do
    if [ -n "${!var}" ]; then
        echo "   ✅ $var is set"
    else
        echo "   ❌ $var is not set"
        exit 1
    fi
done

# ========================================
# CHECK PREREQUISITES
# ========================================
echo "🔧 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    echo "   ✅ Java is installed"
    java -version
else
    echo "   ❌ Java is not installed or not in PATH"
    exit 1
fi

# Check Maven wrapper
if [ -f "mvnw" ]; then
    echo "   ✅ Maven wrapper found"
    chmod +x mvnw
else
    echo "   ❌ Maven wrapper not found"
    exit 1
fi

# Check if PostgreSQL is running (optional check)
if command -v nc &> /dev/null; then
    if nc -z localhost 5432 2>/dev/null; then
        echo "   ✅ PostgreSQL is running on port 5432"
    else
        echo "   ⚠️  PostgreSQL might not be running on port 5432"
    fi
else
    echo "   ⚠️  Could not check PostgreSQL connection (netcat not available)"
fi

# ========================================
# START APPLICATION
# ========================================
echo "🏃‍♂️ Starting Spring Boot application..."
echo "   Application will be available at: http://localhost:8080"
echo "   Health check: http://localhost:8080/actuator/health"
echo "   Press Ctrl+C to stop the application"
echo ""

# Start the application
./mvnw spring-boot:run
