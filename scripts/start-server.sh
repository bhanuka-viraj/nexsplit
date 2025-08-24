#!/bin/bash

# ========================================
# SERVER DEPLOYMENT SCRIPT (BASH)
# ========================================
# This script starts the NexSplit server using pre-built Docker images
# Usage: ./scripts/start-server.sh

set -e

echo "🚀 Starting NexSplit Server Deployment..."

# ========================================
# LOAD ENVIRONMENT VARIABLES (OPTIONAL)
# ========================================
echo "📋 Loading environment variables..."

# Check if .env.production exists and load it
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
    echo "⚠️  .env.production not found, using default values"
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

# Check if docker-compose.server.yml exists
if [ -f "docker-compose.server.yml" ]; then
    echo "   ✅ docker-compose.server.yml found"
else
    echo "   ❌ docker-compose.server.yml not found"
    exit 1
fi

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose.server.yml down 2>/dev/null || true

# ========================================
# START SERVICES
# ========================================
echo "🚀 Starting services with server configuration..."
docker-compose -f docker-compose.server.yml up -d

# ========================================
# WAIT FOR SERVICES TO START
# ========================================
echo "⏳ Waiting for services to start..."
sleep 20

# ========================================
# CHECK SERVICE STATUS
# ========================================
echo "🔍 Checking service status..."

# Check PostgreSQL
if docker inspect nexsplit-postgres --format='{{.State.Status}}' 2>/dev/null; then
    echo "   ✅ PostgreSQL is running"
else
    echo "   ⚠️  PostgreSQL status unknown"
fi

# Check Elasticsearch
if docker inspect nexsplit-elasticsearch --format='{{.State.Status}}' 2>/dev/null; then
    echo "   ✅ Elasticsearch is running"
else
    echo "   ⚠️  Elasticsearch status unknown"
fi

# Check Kibana
if docker inspect nexsplit-kibana --format='{{.State.Status}}' 2>/dev/null; then
    echo "   ✅ Kibana is running"
else
    echo "   ⚠️  Kibana status unknown"
fi

# Check Application
if docker inspect nexsplit-app --format='{{.State.Status}}' 2>/dev/null; then
    echo "   ✅ NexSplit App is running"
else
    echo "   ⚠️  NexSplit App status unknown"
fi

# ========================================
# SUCCESS
# ========================================
echo ""
echo "🎉 Server deployment completed!"
echo "📊 Access URLs:"
echo "   NexSplit App: http://localhost:8080"
echo "   Swagger API: http://localhost:8080/swagger-ui.html"
echo "   Kibana Dashboard: http://localhost:5601"
echo "   Elasticsearch API: http://localhost:9200"
echo ""
echo "📋 Useful commands:"
echo "   View logs: docker-compose -f docker-compose.server.yml logs -f"
echo "   Stop services: docker-compose -f docker-compose.server.yml down"
echo "   Restart services: docker-compose -f docker-compose.server.yml restart"
echo ""
echo "📝 Note: This deployment uses pre-built DockerHub image: bhanukaviraj/nexsplit:dev"
