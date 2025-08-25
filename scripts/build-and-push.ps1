# ========================================
# DOCKER BUILD AND PUSH SCRIPT FOR NEXSPLIT (PowerShell)
# ========================================
# This script automates the process of building and pushing Docker images to DockerHub
# Usage: .\build-and-push.ps1 [version]
# Example: .\build-and-push.ps1 v1.0.0

# Stop on any error
$ErrorActionPreference = "Stop"

# ========================================
# CONFIGURATION VARIABLES
# ========================================
# DockerHub username - change this to your DockerHub username
$DOCKER_USERNAME = "bhanukaviraj"

# Docker image name (repository name)
$IMAGE_NAME = "nexsplit"

# Default version tag if none provided
$DEFAULT_VERSION = "latest"

# ========================================
# VERSION HANDLING
# ========================================
# Get version from command line argument or use default
# If no argument is provided, uses 'latest'
$VERSION = if ($args[0]) { $args[0] } else { $DEFAULT_VERSION }

# ========================================
# LOAD ENVIRONMENT VARIABLES (OPTIONAL)
# ========================================
Write-Host "Loading environment variables..." -ForegroundColor Cyan

# Check if .env.production exists and load it (for build context)
if (Test-Path ".env.production") {
    Write-Host "Found .env.production file" -ForegroundColor Green
    
    # Load variables from .env.production file
    Get-Content ".env.production" | ForEach-Object {
        if ($_ -match "^([^=]+)=(.*)$" -and $_ -notmatch "^#") {
            $name = $matches[1]
            $value = $matches[2]
            Set-Item -Path "env:$name" -Value $value
            Write-Host "   Set $name" -ForegroundColor Gray
        }
    }
} else {
    Write-Host ".env.production not found, using default values" -ForegroundColor Yellow
}

# ========================================
# SCRIPT EXECUTION
# ========================================
Write-Host "Building and pushing NexSplit Docker image..." -ForegroundColor Green
Write-Host "Image: $DOCKER_USERNAME/$IMAGE_NAME`:$VERSION" -ForegroundColor Cyan

# ========================================
# DOCKER BUILD STAGE
# ========================================
Write-Host "Building Docker image..." -ForegroundColor Yellow
# Build the Docker image with the specified tag
# The '.' at the end specifies the build context (current directory)
docker build -t "$DOCKER_USERNAME/$IMAGE_NAME`:$VERSION" .

# ========================================
# IMAGE TAGGING STAGE
# ========================================
# If a specific version was provided (not 'latest'), also tag it as 'latest'
# This ensures we always have a 'latest' tag pointing to the most recent version
if ($VERSION -ne "latest") {
    Write-Host "Tagging as latest..." -ForegroundColor Yellow
    docker tag "$DOCKER_USERNAME/$IMAGE_NAME`:$VERSION" "$DOCKER_USERNAME/$IMAGE_NAME`:latest"
}

# ========================================
# DOCKERHUB PUSH STAGE
# ========================================
Write-Host "Pushing to DockerHub..." -ForegroundColor Yellow
# Push the versioned image to DockerHub
docker push "$DOCKER_USERNAME/$IMAGE_NAME`:$VERSION"

# If we created a 'latest' tag, push that too
if ($VERSION -ne "latest") {
    docker push "$DOCKER_USERNAME/$IMAGE_NAME`:latest"
}

# ========================================
# SUCCESS MESSAGE
# ========================================
Write-Host "Successfully built and pushed $DOCKER_USERNAME/$IMAGE_NAME`:$VERSION" -ForegroundColor Green
Write-Host "Your image is now available on DockerHub!" -ForegroundColor Green
