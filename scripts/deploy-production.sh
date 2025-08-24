#!/bin/bash

# ========================================
# PRODUCTION DEPLOYMENT SCRIPT (BASH)
# ========================================
# This script loads production environment variables and deploys the application
# Usage: ./deploy-production.sh [version]
# Example: ./deploy-production.sh v1.0.0

set -e

VERSION=${1:-latest}

echo "🚀 Starting NexSplit Production Deployment..."
echo "📦 Version: $VERSION"

# ========================================
# LOAD PRODUCTION ENVIRONMENT VARIABLES
# ========================================
echo "📋 Loading production environment variables..."

# Check if .env.production exists
if [ -f ".env.production" ]; then
    echo "✅ Found .env.production file"
    
    # Load variables from .env.production file
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
    done < ".env.production"
else
    echo "❌ ERROR: .env.production file not found!"
    echo "Please create .env.production file from env.production.template"
    echo "Example: cp env.production.template .env.production && edit .env.production"
    exit 1
fi

# ========================================
# VERIFY CRITICAL ENVIRONMENT VARIABLES
# ========================================
echo "🔍 Verifying critical environment variables..."

critical_vars=(
    "SPRING_DATASOURCE_URL"
    "SPRING_DATASOURCE_USERNAME"
    "SPRING_DATASOURCE_PASSWORD"
    "JWT_SECRET"
    "GOOGLE_CLIENT_ID"
    "GOOGLE_CLIENT_SECRET"
)

for var in "${critical_vars[@]}"; do
    if [ -n "${!var}" ]; then
        echo "   ✅ $var is set"
    else
        echo "   ❌ $var is not set"
        echo "   Please check your .env.production file"
        exit 1
    fi
done

# ========================================
# VERIFY EMAIL CONFIGURATION
# ========================================
echo "📧 Verifying email configuration..."

email_vars=("MAIL_HOST" "MAIL_PORT" "MAIL_USERNAME" "MAIL_PASSWORD" "MAIL_FROM" "MAIL_FROM_NAME" "APP_BASE_URL")
email_configured=true

for var in "${email_vars[@]}"; do
    if [ -n "${!var}" ]; then
        echo "   ✅ $var is set"
    else
        echo "   ⚠️  $var is not set"
        email_configured=false
    fi
done

if [ "$email_configured" = false ]; then
    echo "   Email functionality will be limited"
fi

# ========================================
# CHECK DOCKER AND DOCKER COMPOSE
# ========================================
echo "🐳 Checking Docker prerequisites..."

# Check Docker
if command -v docker &> /dev/null; then
    echo "   ✅ Docker is installed"
    docker --version
else
    echo "   ❌ Docker is not installed or not in PATH"
    exit 1
fi

# Check Docker Compose
if command -v docker-compose &> /dev/null; then
    echo "   ✅ Docker Compose is installed"
    docker-compose --version
else
    echo "   ❌ Docker Compose is not installed or not in PATH"
    exit 1
fi

# Check if docker-compose.prod.yml exists
if [ -f "docker-compose.prod.yml" ]; then
    echo "   ✅ docker-compose.prod.yml found"
else
    echo "   ❌ docker-compose.prod.yml not found"
    exit 1
fi

# ========================================
# PULL LATEST IMAGE
# ========================================
echo "📥 Pulling latest Docker image..."

if docker pull "bhanukaviraj/nexsplit:$VERSION"; then
    echo "   ✅ Image pulled successfully"
else
    echo "   ❌ Failed to pull image"
    exit 1
fi

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
echo "🛑 Stopping existing containers..."

if docker-compose -f docker-compose.prod.yml down; then
    echo "   ✅ Existing containers stopped"
else
    echo "   ℹ️  No existing containers to stop"
fi

# ========================================
# START PRODUCTION CONTAINERS
# ========================================
echo "🚀 Starting production containers..."

if docker-compose -f docker-compose.prod.yml up -d; then
    echo "   ✅ Production containers started"
else
    echo "   ❌ Failed to start containers"
    exit 1
fi

# ========================================
# HEALTH CHECK
# ========================================
echo "🏥 Performing health check..."

max_attempts=10
attempt=0
healthy=false

while [ $attempt -lt $max_attempts ] && [ "$healthy" = false ]; do
    attempt=$((attempt + 1))
    echo "   Attempt $attempt/$max_attempts..."
    
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        healthy=true
        echo "   ✅ Application is healthy!"
    else
        echo "   ⏳ Waiting for application to start..."
        sleep 10
    fi
done

if [ "$healthy" = false ]; then
    echo "   ❌ Application failed health check"
    echo "   Checking container logs..."
    docker-compose -f docker-compose.prod.yml logs --tail=50
    exit 1
fi

# ========================================
# SUCCESS
# ========================================
echo ""
echo "🎉 Production deployment completed successfully!"
echo "📊 Application URL: $APP_BASE_URL"
echo "📋 Health Check: $APP_BASE_URL/actuator/health"
echo "📧 Email configured: $email_configured"
echo ""
echo "📋 Useful commands:"
echo "   View logs: docker-compose -f docker-compose.prod.yml logs -f"
echo "   Stop services: docker-compose -f docker-compose.prod.yml down"
echo "   Restart services: docker-compose -f docker-compose.prod.yml restart"
