# ========================================
# SERVER DEPLOYMENT SCRIPT (POWERSHELL)
# ========================================
# This script starts the NexSplit server using pre-built Docker images
# Usage: .\scripts\start-server.ps1

Write-Host "🚀 Starting NexSplit Server Deployment..." -ForegroundColor Green

# ========================================
# LOAD ENVIRONMENT VARIABLES (OPTIONAL)
# ========================================
Write-Host "Loading environment variables..." -ForegroundColor Yellow

# Check if .env.production exists and load it
if (Test-Path ".env.production") {
    Write-Host "Found .env.production file" -ForegroundColor Green
    
    # Load variables from .env.production file
    Get-Content ".env.production" | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "   Set $name" -ForegroundColor Gray
        }
    }
} else {
    Write-Host ".env.production not found, using default values" -ForegroundColor Yellow
}

# ========================================
# CHECK DOCKER
# ========================================
Write-Host "Checking Docker..." -ForegroundColor Yellow

# Check Docker
try {
    $dockerVersion = docker --version
    if ($dockerVersion) {
        Write-Host "   OK: Docker is installed" -ForegroundColor Green
    }
} catch {
    Write-Host "   ERROR: Docker is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check Docker Compose
try {
    $composeVersion = docker-compose --version
    if ($composeVersion) {
        Write-Host "   OK: Docker Compose is installed" -ForegroundColor Green
    }
} catch {
    Write-Host "   ERROR: Docker Compose is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check if docker-compose.server.yml exists
if (Test-Path "docker-compose.server.yml") {
    Write-Host "   OK: docker-compose.server.yml found" -ForegroundColor Green
} else {
    Write-Host "   ERROR: docker-compose.server.yml not found" -ForegroundColor Red
    exit 1
}

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose -f docker-compose.server.yml down 2>$null

# ========================================
# START SERVICES
# ========================================
Write-Host "Starting services with server configuration..." -ForegroundColor Green
docker-compose -f docker-compose.server.yml up -d

# ========================================
# WAIT FOR SERVICES TO START
# ========================================
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# ========================================
# CHECK SERVICE STATUS
# ========================================
Write-Host "Checking service status..." -ForegroundColor Yellow

# Check PostgreSQL
try {
    $postgresStatus = docker inspect nexsplit-postgres --format='{{.State.Status}}' 2>$null
    Write-Host "   ✅ PostgreSQL: $postgresStatus" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  PostgreSQL status unknown" -ForegroundColor Yellow
}

# Check Elasticsearch
try {
    $elasticsearchStatus = docker inspect nexsplit-elasticsearch --format='{{.State.Status}}' 2>$null
    Write-Host "   ✅ Elasticsearch: $elasticsearchStatus" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  Elasticsearch status unknown" -ForegroundColor Yellow
}

# Check Kibana
try {
    $kibanaStatus = docker inspect nexsplit-kibana --format='{{.State.Status}}' 2>$null
    Write-Host "   ✅ Kibana: $kibanaStatus" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  Kibana status unknown" -ForegroundColor Yellow
}

# Check Application
try {
    $appStatus = docker inspect nexsplit-app --format='{{.State.Status}}' 2>$null
    Write-Host "   ✅ NexSplit App: $appStatus" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  NexSplit App status unknown" -ForegroundColor Yellow
}

# ========================================
# SUCCESS
# ========================================
Write-Host ""
Write-Host "🎉 Server deployment completed!" -ForegroundColor Green
Write-Host "📊 Access URLs:" -ForegroundColor Cyan
Write-Host "   NexSplit App: http://localhost:8080" -ForegroundColor Cyan
Write-Host "   Swagger API: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "   Kibana Dashboard: http://localhost:5601" -ForegroundColor Cyan
Write-Host "   Elasticsearch API: http://localhost:9200" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Useful commands:" -ForegroundColor Yellow
Write-Host "   View logs: docker-compose -f docker-compose.server.yml logs -f" -ForegroundColor Gray
Write-Host "   Stop services: docker-compose -f docker-compose.server.yml down" -ForegroundColor Gray
Write-Host "   Restart services: docker-compose -f docker-compose.server.yml restart" -ForegroundColor Gray
Write-Host ""
Write-Host "📝 Note: This deployment uses pre-built DockerHub image: bhanukaviraj/nexsplit:dev" -ForegroundColor Magenta
