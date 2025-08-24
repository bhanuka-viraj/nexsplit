# ========================================
# LOCAL DEVELOPMENT STARTUP SCRIPT
# ========================================
# This script loads environment variables and starts the application
# Usage: .\start-dev.ps1

Write-Host "Starting NexSplit Local Development Environment..." -ForegroundColor Green

# ========================================
# LOAD ENVIRONMENT VARIABLES
# ========================================
Write-Host "Loading environment variables..." -ForegroundColor Yellow

# Check if .env.development exists
if (Test-Path ".env.development") {
    Write-Host "Found .env.development file" -ForegroundColor Green
    
    # Load variables from .env.development file
    Get-Content ".env.development" | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "   Set $name" -ForegroundColor Gray
        }
    }
} else {
    Write-Host ".env.development not found, using default values" -ForegroundColor Yellow
    
    # Set default development values
    $env:DB_PASSWORD = "Viraj@2002"
    $env:JWT_SECRET = "dev-jwt-secret-key-for-local-development-only"
    $env:JWT_EXPIRATION = "60"
    $env:GOOGLE_CLIENT_ID = "google-client-id"
    $env:GOOGLE_CLIENT_SECRET = "google-client-secret"
    $env:SPRING_PROFILES_ACTIVE = "dev"
    $env:FRONTEND_URL = "http://localhost:3000"
    
    # Set default email configuration (optional)
    $env:MAIL_HOST = "smtp.gmail.com"
    $env:MAIL_PORT = "587"
    $env:MAIL_USERNAME = "noreply@nexsplit.com"
    $env:MAIL_PASSWORD = "your-app-password"
    $env:MAIL_FROM = "noreply@nexsplit.com"
    $env:MAIL_FROM_NAME = "NexSplit"
    $env:APP_BASE_URL = "http://localhost:8080"
    $env:EMAIL_RATE_LIMIT = "10"
    $env:EMAIL_DAILY_LIMIT = "50"
}

# ========================================
# VERIFY ENVIRONMENT VARIABLES
# ========================================
Write-Host "Verifying environment variables..." -ForegroundColor Yellow

$requiredVars = @("DB_PASSWORD", "JWT_SECRET", "JWT_EXPIRATION")
$optionalVars = @("MAIL_HOST", "MAIL_PORT", "MAIL_USERNAME", "MAIL_PASSWORD", "MAIL_FROM", "MAIL_FROM_NAME", "APP_BASE_URL", "EMAIL_RATE_LIMIT", "EMAIL_DAILY_LIMIT")
foreach ($var in $requiredVars) {
    if ([Environment]::GetEnvironmentVariable($var, "Process")) {
        Write-Host "   OK: $var is set" -ForegroundColor Green
    } else {
        Write-Host "   ERROR: $var is not set" -ForegroundColor Red
    }
}

Write-Host "Checking optional email variables..." -ForegroundColor Yellow
foreach ($var in $optionalVars) {
    if ([Environment]::GetEnvironmentVariable($var, "Process")) {
        Write-Host "   OK: $var is set" -ForegroundColor Green
    } else {
        Write-Host "   INFO: $var is not set (email functionality will be limited)" -ForegroundColor Yellow
    }
}

# ========================================
# CHECK PREREQUISITES
# ========================================
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion) {
        Write-Host "   OK: Java is installed" -ForegroundColor Green
    }
} catch {
    Write-Host "   ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check Maven wrapper
if (Test-Path "mvnw.cmd") {
    Write-Host "   OK: Maven wrapper found" -ForegroundColor Green
} else {
    Write-Host "   ERROR: Maven wrapper not found" -ForegroundColor Red
    exit 1
}

# Check if PostgreSQL is running (optional check)
try {
    $pgCheck = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
    if ($pgCheck.TcpTestSucceeded) {
        Write-Host "   OK: PostgreSQL is running on port 5432" -ForegroundColor Green
    } else {
        Write-Host "   WARNING: PostgreSQL might not be running on port 5432" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   WARNING: Could not check PostgreSQL connection" -ForegroundColor Yellow
}

# ========================================
# START APPLICATION
# ========================================
Write-Host "Starting Spring Boot application..." -ForegroundColor Green
Write-Host "   Application will be available at: http://localhost:8080" -ForegroundColor Cyan
Write-Host "   Health check: http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "   Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host ""

# Start the application
try {
    .\mvnw.cmd spring-boot:run
} catch {
    Write-Host "ERROR: Failed to start application" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
