#!/bin/bash

# ========================================
# NEXSPLIT DEBUG ENVIRONMENT STARTUP SCRIPT
# ========================================
# This script starts the necessary containers for IntelliJ debugging
# It includes: PostgreSQL, Elasticsearch, Kibana, and Filebeat
# The main application will run directly in IntelliJ

# ========================================
# SCRIPT CONFIGURATION
# ========================================
SCRIPT_NAME="start-debug.sh"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env.development"
DOCKER_COMPOSE_FILE="$PROJECT_ROOT/docker-compose.debug.yml"

# ========================================
# COLOR OUTPUT FUNCTIONS
# ========================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

write_info() {
    echo -e "${CYAN}[INFO]${NC} $1"
}

write_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

write_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

write_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ========================================
# SCRIPT HEADER
# ========================================
echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}NEXSPLIT DEBUG ENVIRONMENT STARTUP${NC}"
echo -e "${BLUE}=========================================${NC}"
echo -e "${WHITE}This script starts supporting services for IntelliJ debugging${NC}"
echo ""

# ========================================
# PREREQUISITE CHECKS
# ========================================
write_info "Checking prerequisites..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    write_error "Docker is not installed or not in PATH"
    write_info "Please install Docker and try again"
    exit 1
fi

# Check if Docker is running
if ! docker version &> /dev/null; then
    write_error "Docker is not running"
    write_info "Please start Docker and try again"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    write_error "Docker Compose is not available"
    write_info "Please ensure Docker Compose is installed"
    exit 1
fi

write_success "All prerequisites are satisfied"

# ========================================
# ENVIRONMENT VARIABLES
# ========================================
write_info "Loading environment variables..."

# Check if .env.development exists
if [ -f "$ENV_FILE" ]; then
    write_info "Loading environment variables from $ENV_FILE"
    
    # Load environment variables from .env.development
    while IFS= read -r line; do
        if [[ $line =~ ^([^#][^=]+)=(.*)$ ]]; then
            name="${BASH_REMATCH[1]}"
            value="${BASH_REMATCH[2]}"
            export "$name=$value"
            write_info "Loaded: $name"
        fi
    done < "$ENV_FILE"
    write_success "Environment variables loaded successfully"
else
    write_warning ".env.development file not found at $ENV_FILE"
    write_info "Using default environment variables"
    
    # Set default values for development
    export DB_PASSWORD="Viraj@2002"
    export JWT_SECRET="dev-jwt-secret-key-for-local-development-only"
    export JWT_EXPIRATION="60"
    export MAIL_HOST="smtp.gmail.com"
    export MAIL_PORT="587"
    export MAIL_USERNAME="noreply@nexsplit.com"
    export MAIL_PASSWORD="your-app-password"
    export MAIL_FROM="noreply@nexsplit.com"
    export MAIL_FROM_NAME="NexSplit"
    export APP_BASE_URL="http://localhost:8080"
    export EMAIL_RATE_LIMIT="10"
    export EMAIL_DAILY_LIMIT="50"
    export SPRING_PROFILES_ACTIVE="dev"
    
    write_info "Default environment variables set"
fi

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
write_info "Stopping any existing debug containers..."

if docker-compose -f "$DOCKER_COMPOSE_FILE" down --remove-orphans; then
    write_success "Existing containers stopped"
else
    write_warning "No existing containers to stop or error occurred"
fi

# ========================================
# START DEBUG SERVICES
# ========================================
write_info "Starting debug services..."

if docker-compose -f "$DOCKER_COMPOSE_FILE" up -d; then
    write_success "Debug services started successfully"
    write_info "Waiting for services to be ready..."
    
    # Wait for services to be healthy
    max_attempts=30
    attempt=0
    all_healthy=false
    
    while [ $attempt -lt $max_attempts ] && [ "$all_healthy" = false ]; do
        attempt=$((attempt + 1))
        write_info "Health check attempt $attempt/$max_attempts"
        
        postgres_healthy=$(docker inspect --format='{{.State.Health.Status}}' nexsplit-postgres-debug 2>/dev/null || echo "unhealthy")
        elasticsearch_healthy=$(docker inspect --format='{{.State.Health.Status}}' nexsplit-elasticsearch-debug 2>/dev/null || echo "unhealthy")
        kibana_healthy=$(docker inspect --format='{{.State.Health.Status}}' nexsplit-kibana-debug 2>/dev/null || echo "unhealthy")
        
        if [ "$postgres_healthy" = "healthy" ] && [ "$elasticsearch_healthy" = "healthy" ] && [ "$kibana_healthy" = "healthy" ]; then
            all_healthy=true
            write_success "All services are healthy!"
        else
            write_info "Waiting for services to be ready... (PostgreSQL: $postgres_healthy, Elasticsearch: $elasticsearch_healthy, Kibana: $kibana_healthy)"
            sleep 10
        fi
    done
    
    if [ "$all_healthy" = false ]; then
        write_warning "Some services may not be fully ready, but continuing..."
    fi
else
    write_error "Failed to start debug services"
    exit 1
fi

# ========================================
# DISPLAY SERVICE INFORMATION
# ========================================
echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}DEBUG ENVIRONMENT READY!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${WHITE}Services running:${NC}"
echo -e "  ${CYAN}• PostgreSQL Database: localhost:5432${NC}"
echo -e "  ${CYAN}• Elasticsearch: localhost:9200${NC}"
echo -e "  ${CYAN}• Kibana: http://localhost:5601${NC}"
echo -e "  ${CYAN}• Filebeat: Log shipping to Elasticsearch${NC}"
echo ""
echo -e "${WHITE}Next steps:${NC}"
echo -e "  ${YELLOW}1. Open IntelliJ IDEA${NC}"
echo -e "  ${YELLOW}2. Open the NexSplit project${NC}"
echo -e "  ${YELLOW}3. Set VM options: -Dspring.profiles.active=dev${NC}"
echo -e "  ${YELLOW}4. Run NexsplitApplication.main()${NC}"
echo -e "  ${YELLOW}5. Application will be available at: http://localhost:8080${NC}"
echo ""
echo -e "${WHITE}Useful URLs:${NC}"
echo -e "  ${CYAN}• Application: http://localhost:8080${NC}"
echo -e "  ${CYAN}• Swagger UI: http://localhost:8080/swagger-ui.html${NC}"
echo -e "  ${CYAN}• Kibana: http://localhost:5601${NC}"
echo -e "  ${CYAN}• Elasticsearch: http://localhost:9200${NC}"
echo ""
echo -e "${WHITE}To stop services:${NC}"
echo -e "  ${YELLOW}docker-compose -f docker-compose.debug.yml down${NC}"
echo ""

# ========================================
# OPTIONAL: OPEN KIBANA
# ========================================
read -p "Would you like to open Kibana in your browser? (y/n): " open_kibana
if [[ $open_kibana =~ ^[Yy]$ ]]; then
    write_info "Opening Kibana..."
    if command -v xdg-open &> /dev/null; then
        xdg-open "http://localhost:5601"
    elif command -v open &> /dev/null; then
        open "http://localhost:5601"
    else
        write_warning "Could not automatically open browser. Please visit: http://localhost:5601"
    fi
fi

write_success "Debug environment setup complete!"
