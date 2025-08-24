# ========================================
# NEXSPLIT DEBUG ENVIRONMENT STARTUP SCRIPT
# ========================================
# This script starts the necessary containers for IntelliJ debugging
# It includes: PostgreSQL, Elasticsearch, Kibana, and Filebeat
# The main application will run directly in IntelliJ

param(
    [switch]$SkipEnvCheck = $false
)

# ========================================
# SCRIPT CONFIGURATION
# ========================================
$ScriptName = "start-debug.ps1"
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$EnvFile = Join-Path $ProjectRoot ".env.development"
$DockerComposeFile = Join-Path $ProjectRoot "docker-compose.debug.yml"

# ========================================
# COLOR OUTPUT FUNCTIONS
# ========================================
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# ========================================
# SCRIPT HEADER
# ========================================
Write-Host "=========================================" -ForegroundColor Blue
Write-Host "NEXSPLIT DEBUG ENVIRONMENT STARTUP" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Blue
Write-Host "This script starts supporting services for IntelliJ debugging" -ForegroundColor White
Write-Host ""

# ========================================
# PREREQUISITE CHECKS
# ========================================
Write-Info "Checking prerequisites..."

# Check if Docker is installed and running
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker is not installed or not in PATH"
    Write-Info "Please install Docker Desktop and try again"
    exit 1
}

# Check if Docker is running
try {
    docker version | Out-Null
    Write-Success "Docker is running"
} catch {
    Write-Error "Docker is not running"
    Write-Info "Please start Docker Desktop and try again"
    exit 1
}

# Check if Docker Compose is available
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Error "Docker Compose is not available"
    Write-Info "Please ensure Docker Compose is installed"
    exit 1
}

Write-Success "All prerequisites are satisfied"

# ========================================
# ENVIRONMENT VARIABLES
# ========================================
Write-Info "Loading environment variables..."

# Check if .env.development exists
if (Test-Path $EnvFile) {
    Write-Info "Loading environment variables from $EnvFile"
    
    # Load environment variables from .env.development
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Info "Loaded: $name"
        }
    }
    Write-Success "Environment variables loaded successfully"
} else {
    Write-Warning ".env.development file not found at $EnvFile"
    Write-Info "Using default environment variables"
    
    # Set default values for development
    $defaultEnvVars = @{
        "DB_PASSWORD" = "Viraj@2002"
        "JWT_SECRET" = "dev-jwt-secret-key-for-local-development-only"
        "JWT_EXPIRATION" = "60"
        "MAIL_HOST" = "smtp.gmail.com"
        "MAIL_PORT" = "587"
        "MAIL_USERNAME" = "noreply@nexsplit.com"
        "MAIL_PASSWORD" = "your-app-password"
        "MAIL_FROM" = "noreply@nexsplit.com"
        "MAIL_FROM_NAME" = "NexSplit"
        "APP_BASE_URL" = "http://localhost:8080"
        "EMAIL_RATE_LIMIT" = "10"
        "EMAIL_DAILY_LIMIT" = "50"
        "SPRING_PROFILES_ACTIVE" = "dev"
    }
    
    foreach ($key in $defaultEnvVars.Keys) {
        [Environment]::SetEnvironmentVariable($key, $defaultEnvVars[$key], "Process")
        Write-Info "Set default: $key"
    }
}

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
Write-Info "Stopping any existing debug containers..."

try {
    docker-compose -f $DockerComposeFile down --remove-orphans
    Write-Success "Existing containers stopped"
} catch {
    Write-Warning "No existing containers to stop or error occurred"
}

# ========================================
# START DEBUG SERVICES
# ========================================
Write-Info "Starting debug services..."

try {
    # Start services in detached mode
    docker-compose -f $DockerComposeFile up -d
    
    Write-Success "Debug services started successfully"
    Write-Info "Waiting for services to be ready..."
    
    # Wait for services to be healthy
    $maxAttempts = 30
    $attempt = 0
    $allHealthy = $false
    
    while ($attempt -lt $maxAttempts -and -not $allHealthy) {
        $attempt++
        Write-Info "Health check attempt $attempt/$maxAttempts"
        
        $postgresHealthy = docker inspect --format='{{.State.Health.Status}}' nexsplit-postgres-debug 2>$null
        $elasticsearchHealthy = docker inspect --format='{{.State.Health.Status}}' nexsplit-elasticsearch-debug 2>$null
        $kibanaHealthy = docker inspect --format='{{.State.Health.Status}}' nexsplit-kibana-debug 2>$null
        
        if ($postgresHealthy -eq "healthy" -and $elasticsearchHealthy -eq "healthy" -and $kibanaHealthy -eq "healthy") {
            $allHealthy = $true
            Write-Success "All services are healthy!"
        } else {
            Write-Info "Waiting for services to be ready... (PostgreSQL: $postgresHealthy, Elasticsearch: $elasticsearchHealthy, Kibana: $kibanaHealthy)"
            Start-Sleep -Seconds 10
        }
    }
    
    if (-not $allHealthy) {
        Write-Warning "Some services may not be fully ready, but continuing..."
    }
    
} catch {
    Write-Error "Failed to start debug services: $($_.Exception.Message)"
    exit 1
}

# ========================================
# DISPLAY SERVICE INFORMATION
# ========================================
Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "DEBUG ENVIRONMENT READY!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services running:" -ForegroundColor White
Write-Host "  • PostgreSQL Database: localhost:5432" -ForegroundColor Cyan
Write-Host "  • Elasticsearch: localhost:9200" -ForegroundColor Cyan
Write-Host "  • Kibana: http://localhost:5601" -ForegroundColor Cyan
Write-Host "  • Filebeat: Log shipping to Elasticsearch" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "  1. Open IntelliJ IDEA" -ForegroundColor Yellow
Write-Host "  2. Open the NexSplit project" -ForegroundColor Yellow
Write-Host "  3. Set VM options: -Dspring.profiles.active=dev" -ForegroundColor Yellow
Write-Host "  4. Run NexsplitApplication.main()" -ForegroundColor Yellow
Write-Host "  5. Application will be available at: http://localhost:8080" -ForegroundColor Yellow
Write-Host ""
Write-Host "Useful URLs:" -ForegroundColor White
Write-Host "  • Application: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  • Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  • Kibana: http://localhost:5601" -ForegroundColor Cyan
Write-Host "  • Elasticsearch: http://localhost:9200" -ForegroundColor Cyan
Write-Host ""
Write-Host "To stop services:" -ForegroundColor White
Write-Host "  docker-compose -f docker-compose.debug.yml down" -ForegroundColor Yellow
Write-Host ""

# ========================================
# OPTIONAL: OPEN KIBANA
# ========================================
$openKibana = Read-Host "Would you like to open Kibana in your browser? (y/n)"
if ($openKibana -eq "y" -or $openKibana -eq "Y") {
    Write-Info "Opening Kibana..."
    Start-Process "http://localhost:5601"
}

Write-Success "Debug environment setup complete!"
