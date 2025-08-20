# NexSplit Server Deployment Script
Write-Host "Starting NexSplit Server Deployment..." -ForegroundColor Green

# Check Docker
try {
    docker version | Out-Null
    Write-Host "✓ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not running. Please start Docker first." -ForegroundColor Red
    exit 1
}

# Stop existing containers
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose -f docker-compose.server.yml down 2>$null

# Start services using server configuration
Write-Host "Starting services with server configuration..." -ForegroundColor Yellow
docker-compose -f docker-compose.server.yml up -d

# Wait for services to start
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# Check service status
Write-Host "Checking service status..." -ForegroundColor Yellow

try {
    $postgresStatus = docker inspect nexsplit-postgres --format='{{.State.Status}}' 2>$null
    Write-Host "PostgreSQL: $postgresStatus" -ForegroundColor Green
} catch {
    Write-Host "PostgreSQL: Not available" -ForegroundColor Yellow
}

try {
    $elasticsearchStatus = docker inspect nexsplit-elasticsearch --format='{{.State.Status}}' 2>$null
    Write-Host "Elasticsearch: $elasticsearchStatus" -ForegroundColor Green
} catch {
    Write-Host "Elasticsearch: Not available" -ForegroundColor Yellow
}

try {
    $kibanaStatus = docker inspect nexsplit-kibana --format='{{.State.Status}}' 2>$null
    Write-Host "Kibana: $kibanaStatus" -ForegroundColor Green
} catch {
    Write-Host "Kibana: Not available" -ForegroundColor Yellow
}

try {
    $appStatus = docker inspect nexsplit-app --format='{{.State.Status}}' 2>$null
    Write-Host "NexSplit App: $appStatus" -ForegroundColor Green
} catch {
    Write-Host "NexSplit App: Not available" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Server deployment completed! Access URLs:" -ForegroundColor Green
Write-Host "• NexSplit App: http://localhost:8080" -ForegroundColor Cyan
Write-Host "• Swagger API: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "• Kibana Dashboard: http://localhost:5601" -ForegroundColor Cyan
Write-Host "• Elasticsearch API: http://localhost:9200" -ForegroundColor Cyan
Write-Host ""
Write-Host "To view logs: docker-compose -f docker-compose.server.yml logs -f" -ForegroundColor Yellow
Write-Host "To stop services: docker-compose -f docker-compose.server.yml down" -ForegroundColor Yellow
Write-Host ""
Write-Host "Note: This deployment uses pre-built DockerHub image: bhanukaviraj/nexsplit:dev" -ForegroundColor Magenta
