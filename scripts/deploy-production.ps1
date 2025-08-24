# ========================================
# PRODUCTION DEPLOYMENT SCRIPT (POWERSHELL)
# ========================================
# This script loads production environment variables and deploys the application
# Usage: .\deploy-production.ps1 [version]
# Example: .\deploy-production.ps1 v1.0.0

param(
    [string]$Version = "latest"
)

Write-Host "🚀 Starting NexSplit Production Deployment..." -ForegroundColor Green
Write-Host "📦 Version: $Version" -ForegroundColor Cyan

# ========================================
# LOAD PRODUCTION ENVIRONMENT VARIABLES
# ========================================
Write-Host "Loading production environment variables..." -ForegroundColor Yellow

# Check if .env.production exists
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
    Write-Host "ERROR: .env.production file not found!" -ForegroundColor Red
    Write-Host "Please create .env.production file from env.production.template" -ForegroundColor Yellow
    Write-Host "Example: Copy env.production.template to .env.production and fill in your values" -ForegroundColor Yellow
    exit 1
}

# ========================================
# VERIFY CRITICAL ENVIRONMENT VARIABLES
# ========================================
Write-Host "Verifying critical environment variables..." -ForegroundColor Yellow

$criticalVars = @(
    "SPRING_DATASOURCE_URL", 
    "SPRING_DATASOURCE_USERNAME", 
    "SPRING_DATASOURCE_PASSWORD",
    "JWT_SECRET",
    "GOOGLE_CLIENT_ID",
    "GOOGLE_CLIENT_SECRET"
)

foreach ($var in $criticalVars) {
    if ([Environment]::GetEnvironmentVariable($var, "Process")) {
        Write-Host "   OK: $var is set" -ForegroundColor Green
    } else {
        Write-Host "   ERROR: $var is not set" -ForegroundColor Red
        Write-Host "   Please check your .env.production file" -ForegroundColor Yellow
        exit 1
    }
}

# ========================================
# VERIFY EMAIL CONFIGURATION
# ========================================
Write-Host "Verifying email configuration..." -ForegroundColor Yellow

$emailVars = @("MAIL_HOST", "MAIL_PORT", "MAIL_USERNAME", "MAIL_PASSWORD", "MAIL_FROM", "MAIL_FROM_NAME", "APP_BASE_URL")
$emailConfigured = $true

foreach ($var in $emailVars) {
    if ([Environment]::GetEnvironmentVariable($var, "Process")) {
        Write-Host "   OK: $var is set" -ForegroundColor Green
    } else {
        Write-Host "   WARNING: $var is not set" -ForegroundColor Yellow
        $emailConfigured = $false
    }
}

if (-not $emailConfigured) {
    Write-Host "   Email functionality will be limited" -ForegroundColor Yellow
}

# ========================================
# CHECK DOCKER AND DOCKER COMPOSE
# ========================================
Write-Host "Checking Docker prerequisites..." -ForegroundColor Yellow

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

# Check if docker-compose.prod.yml exists
if (Test-Path "docker-compose.prod.yml") {
    Write-Host "   OK: docker-compose.prod.yml found" -ForegroundColor Green
} else {
    Write-Host "   ERROR: docker-compose.prod.yml not found" -ForegroundColor Red
    exit 1
}

# ========================================
# PULL LATEST IMAGE
# ========================================
Write-Host "Pulling latest Docker image..." -ForegroundColor Yellow

try {
    docker pull "bhanukaviraj/nexsplit:$Version"
    Write-Host "   OK: Image pulled successfully" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: Failed to pull image" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
    exit 1
}

# ========================================
# STOP EXISTING CONTAINERS
# ========================================
Write-Host "Stopping existing containers..." -ForegroundColor Yellow

try {
    docker-compose -f docker-compose.prod.yml down
    Write-Host "   OK: Existing containers stopped" -ForegroundColor Green
} catch {
    Write-Host "   INFO: No existing containers to stop" -ForegroundColor Yellow
}

# ========================================
# START PRODUCTION CONTAINERS
# ========================================
Write-Host "Starting production containers..." -ForegroundColor Green

try {
    docker-compose -f docker-compose.prod.yml up -d
    Write-Host "   OK: Production containers started" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: Failed to start containers" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
    exit 1
}

# ========================================
# HEALTH CHECK
# ========================================
Write-Host "Performing health check..." -ForegroundColor Yellow

$maxAttempts = 10
$attempt = 0
$healthy = $false

while ($attempt -lt $maxAttempts -and -not $healthy) {
    $attempt++
    Write-Host "   Attempt $attempt/$maxAttempts..." -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            $healthy = $true
            Write-Host "   ✅ Application is healthy!" -ForegroundColor Green
        }
    } catch {
        Write-Host "   ⏳ Waiting for application to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    }
}

if (-not $healthy) {
    Write-Host "   ❌ Application failed health check" -ForegroundColor Red
    Write-Host "   Checking container logs..." -ForegroundColor Yellow
    docker-compose -f docker-compose.prod.yml logs --tail=50
    exit 1
}

# ========================================
# SUCCESS
# ========================================
Write-Host ""
Write-Host "🎉 Production deployment completed successfully!" -ForegroundColor Green
Write-Host "📊 Application URL: $env:APP_BASE_URL" -ForegroundColor Cyan
Write-Host "📋 Health Check: $env:APP_BASE_URL/actuator/health" -ForegroundColor Cyan
Write-Host "📧 Email configured: $emailConfigured" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Useful commands:" -ForegroundColor Yellow
Write-Host "   View logs: docker-compose -f docker-compose.prod.yml logs -f" -ForegroundColor Gray
Write-Host "   Stop services: docker-compose -f docker-compose.prod.yml down" -ForegroundColor Gray
Write-Host "   Restart services: docker-compose -f docker-compose.prod.yml restart" -ForegroundColor Gray
