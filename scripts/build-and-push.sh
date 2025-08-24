#!/bin/bash

# ========================================
# DOCKER BUILD AND PUSH SCRIPT FOR NEXSPLIT
# ========================================
# This script automates the process of building and pushing Docker images to DockerHub
# Usage: ./build-and-push.sh [version]
# Example: ./build-and-push.sh v1.0.0

# Exit immediately if any command fails
# This ensures the script stops on any error
set -e

# ========================================
# CONFIGURATION VARIABLES
# ========================================
# DockerHub username - change this to your DockerHub username
DOCKER_USERNAME="bhanukaviraj"

# Docker image name (repository name)
IMAGE_NAME="nexsplit"

# Default version tag if none provided
DEFAULT_VERSION="latest"

# ========================================
# VERSION HANDLING
# ========================================
# Get version from command line argument or use default
# If no argument is provided, uses 'latest'
VERSION=${1:-$DEFAULT_VERSION}

# ========================================
# LOAD ENVIRONMENT VARIABLES (OPTIONAL)
# ========================================
echo "📋 Loading environment variables..."

# Check if .env.production exists and load it (for build context)
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
# SCRIPT EXECUTION
# ========================================
echo "🐳 Building and pushing NexSplit Docker image..."
echo "📦 Image: $DOCKER_USERNAME/$IMAGE_NAME:$VERSION"

# ========================================
# DOCKER BUILD STAGE
# ========================================
echo "🔨 Building Docker image..."
# Build the Docker image with the specified tag
# The '.' at the end specifies the build context (current directory)
docker build -t $DOCKER_USERNAME/$IMAGE_NAME:$VERSION .

# ========================================
# IMAGE TAGGING STAGE
# ========================================
# If a specific version was provided (not 'latest'), also tag it as 'latest'
# This ensures we always have a 'latest' tag pointing to the most recent version
if [ "$VERSION" != "latest" ]; then
    echo "🏷️  Tagging as latest..."
    docker tag $DOCKER_USERNAME/$IMAGE_NAME:$VERSION $DOCKER_USERNAME/$IMAGE_NAME:latest
fi

# ========================================
# DOCKERHUB PUSH STAGE
# ========================================
echo "📤 Pushing to DockerHub..."
# Push the versioned image to DockerHub
docker push $DOCKER_USERNAME/$IMAGE_NAME:$VERSION

# If we created a 'latest' tag, push that too
if [ "$VERSION" != "latest" ]; then
    docker push $DOCKER_USERNAME/$IMAGE_NAME:latest
fi

# ========================================
# SUCCESS MESSAGE
# ========================================
echo "✅ Successfully built and pushed $DOCKER_USERNAME/$IMAGE_NAME:$VERSION"
echo "🎉 Your image is now available on DockerHub!"

# ========================================
# IMAGE INFORMATION
# ========================================
# Show all available tags for this image
echo ""
echo "📋 Available tags:"
# List all Docker images matching our repository name
docker images | grep $DOCKER_USERNAME/$IMAGE_NAME
