#!/bin/bash

# ========================================
# ELASTICSEARCH STARTUP SCRIPT (BASH)
# ========================================
# This script starts the complete NexSplit stack with Elasticsearch monitoring
# Usage: ./start-elasticsearch.sh

set -e

echo "🚀 Starting NexSplit with Elasticsearch Monitoring..."

# ========================================
# LOAD ENVIRONMENT VARIABLES (OPTIONAL)
# ========================================
echo "📋 Loading environment variables..."

# Check if .env.development exists and load it
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
fi

# ========================================
# CHECK DOCKER
# ========================================
echo "🐳 Checking Docker..."

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

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
echo "🛑 Stopping existing containers..."
docker-compose down

# ========================================
# START COMPLETE STACK
# ========================================
echo "🚀 Starting complete NexSplit stack..."
docker-compose up -d --build

# ========================================
# WAIT FOR SERVICES TO START
# ========================================
echo "⏳ Waiting for services to start..."
sleep 30

# ========================================
# VERIFY SERVICES
# ========================================
echo "🔍 Verifying services..."

# Check application health
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ✅ NexSplit App is healthy"
else
    echo "   ⚠️  NexSplit App might still be starting..."
fi

# Check Elasticsearch
if curl -f http://localhost:9200/_cluster/health > /dev/null 2>&1; then
    echo "   ✅ Elasticsearch is healthy"
else
    echo "   ⚠️  Elasticsearch might still be starting..."
fi

# Check Kibana
if curl -f http://localhost:5601/api/status > /dev/null 2>&1; then
    echo "   ✅ Kibana is healthy"
else
    echo "   ⚠️  Kibana might still be starting..."
fi

# ========================================
# SUCCESS
# ========================================
echo ""
echo "🎉 All services started successfully!"
echo "📊 Access URLs:"
echo "   NexSplit App: http://localhost:8080"
echo "   Swagger API: http://localhost:8080/swagger-ui.html"
echo "   Kibana: http://localhost:5601"
echo "   Elasticsearch: http://localhost:9200"
echo ""
echo "📋 Useful commands:"
echo "   View logs: docker-compose logs -f"
echo "   Stop services: docker-compose down"
echo "   Restart services: docker-compose restart"
