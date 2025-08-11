# 🚀 Production Deployment Guide

## 📋 Production Tagging Strategy

### **Version Management**

**1. Semantic Versioning (Recommended)**

```bash
# Format: vMAJOR.MINOR.PATCH
v1.0.0  # First production release
v1.0.1  # Bug fix
v1.1.0  # New feature
v2.0.0  # Breaking change
```

**2. Environment-Specific Tags**

```bash
dev      # Development builds
staging  # Pre-production testing
prod     # Production builds
latest   # Current production (default)
```

### **Production Build Process**

**Step 1: Build Production Image**

```bash
# Build with production tag
docker build -t bhanukaviraj/nexsplit:v1.0.0 .

# Also tag as latest
docker tag bhanukaviraj/nexsplit:v1.0.0 bhanukaviraj/nexsplit:latest
```

**Step 2: Push to DockerHub**

```bash
# Push versioned tag
docker push bhanukaviraj/nexsplit:v1.0.0

# Push latest tag
docker push bhanukaviraj/nexsplit:latest
```

**Step 3: Deploy to Production**

```bash
# On production server
docker pull bhanukaviraj/nexsplit:v1.0.0
docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Environment Variables for Production

### **Production Environment File**

**Create `.env.production`:**

```bash
# ========================================
# PRODUCTION ENVIRONMENT VARIABLES
# ========================================

# DATABASE CONFIGURATION
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/nexsplit
SPRING_DATASOURCE_USERNAME=nexsplit_prod_user
SPRING_DATASOURCE_PASSWORD=super-secure-production-password-123

# JWT CONFIGURATION
JWT_SECRET=your-super-secure-256-bit-production-jwt-secret-key-here
JWT_EXPIRATION=3600

# OAUTH2 CONFIGURATION
GOOGLE_CLIENT_ID=your-production-google-client-id
GOOGLE_CLIENT_SECRET=your-production-google-client-secret

# APPLICATION CONFIGURATION
SPRING_PROFILES_ACTIVE=production
FRONTEND_URL=https://nexsplit.com

# SERVER CONFIGURATION
SERVER_PORT=8080
```

### **Production Docker Compose**

**Create `docker-compose.prod.yml`:**

```yaml
version: "3.8"
services:
  nexsplit-app:
    image: bhanukaviraj/nexsplit:v1.0.0 # Specific version
    container_name: nexsplit-app-prod
    env_file:
      - ../.env.production
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped
    healthcheck:
      test:
        [
          "CMD",
          "wget",
          "--no-verbose",
          "--tries=1",
          "--spider",
          "http://localhost:8080/actuator/health",
        ]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - nexsplit-network

networks:
  nexsplit-network:
    driver: bridge
```

## 🔄 Production Deployment Workflow

### **Automated Production Deployment Script**

**Create `deploy-production.sh`:**

```bash
#!/bin/bash

# ========================================
# PRODUCTION DEPLOYMENT SCRIPT
# ========================================

set -e

VERSION=${1:-latest}
ENVIRONMENT=${2:-production}

echo "🚀 Deploying NexSplit $VERSION to $ENVIRONMENT..."

# ========================================
# VALIDATION
# ========================================
if [ -z "$VERSION" ]; then
    echo "❌ Error: Version is required"
    echo "Usage: ./deploy-production.sh <version> [environment]"
    exit 1
fi

# ========================================
# PULL LATEST IMAGE
# ========================================
echo "📥 Pulling image bhanukaviraj/nexsplit:$VERSION..."
docker pull bhanukaviraj/nexsplit:$VERSION

# ========================================
# BACKUP CURRENT DEPLOYMENT
# ========================================
echo "💾 Creating backup..."
if docker ps -q -f name=nexsplit-app-prod | grep -q .; then
    docker tag bhanukaviraj/nexsplit:$(docker inspect --format='{{.Config.Image}}' nexsplit-app-prod | cut -d: -f2) bhanukaviraj/nexsplit:backup-$(date +%Y%m%d-%H%M%S)
fi

# ========================================
# STOP CURRENT CONTAINER
# ========================================
echo "🛑 Stopping current container..."
docker-compose -f docker-compose.prod.yml down

# ========================================
# UPDATE IMAGE TAG
# ========================================
echo "🏷️ Updating image tag to $VERSION..."
sed -i "s|image: bhanukaviraj/nexsplit:.*|image: bhanukaviraj/nexsplit:$VERSION|" docker-compose.prod.yml

# ========================================
# START NEW CONTAINER
# ========================================
echo "🚀 Starting new container..."
docker-compose -f docker-compose.prod.yml up -d

# ========================================
# HEALTH CHECK
# ========================================
echo "🏥 Performing health check..."
sleep 30

for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Deployment successful! Application is healthy."
        break
    else
        echo "⏳ Waiting for application to start... (attempt $i/10)"
        sleep 10
    fi
done

if [ $i -eq 10 ]; then
    echo "❌ Deployment failed! Application is not responding."
    echo "🔄 Rolling back to previous version..."
    docker-compose -f docker-compose.prod.yml down
    docker tag bhanukaviraj/nexsplit:backup-$(date +%Y%m%d-%H%M%S) bhanukaviraj/nexsplit:latest
    docker-compose -f docker-compose.prod.yml up -d
    exit 1
fi

# ========================================
# CLEANUP OLD IMAGES
# ========================================
echo "🧹 Cleaning up old images..."
docker image prune -f

echo "🎉 Production deployment completed successfully!"
echo "📊 Application URL: https://your-domain.com"
echo "📋 Health Check: https://your-domain.com/actuator/health"
```

**Make executable:**

```bash
chmod +x deploy-production.sh
```

## 🏷️ Tag Management Best Practices

### **Version Tagging Strategy**

**1. Development Cycle:**

```bash
# Feature development
docker build -t bhanukaviraj/nexsplit:dev .

# Staging testing
docker build -t bhanukaviraj/nexsplit:staging .

# Production release
docker build -t bhanukaviraj/nexsplit:v1.0.0 .
docker tag bhanukaviraj/nexsplit:v1.0.0 bhanukaviraj/nexsplit:latest
```

**2. Hotfix Process:**

```bash
# Emergency fix
docker build -t bhanukaviraj/nexsplit:v1.0.1 .
docker tag bhanukaviraj/nexsplit:v1.0.1 bhanukaviraj/nexsplit:latest
```

**3. Rollback Strategy:**

```bash
# Rollback to previous version
docker tag bhanukaviraj/nexsplit:v1.0.0 bhanukaviraj/nexsplit:latest
./deploy-production.sh v1.0.0
```

### **Tag Naming Conventions**

```bash
# Semantic versions
v1.0.0          # Major.Minor.Patch
v1.0.0-rc1      # Release candidate
v1.0.0-beta1    # Beta version

# Environment tags
dev             # Development
staging         # Staging/Testing
prod            # Production
latest          # Current production

# Feature tags
feature-auth    # Feature branch
hotfix-login    # Hotfix branch
```

## 🔐 Security for Production

### **Environment Variable Security**

**1. Never Commit Secrets:**

```bash
# ❌ WRONG - Don't do this
echo "DB_PASSWORD=secret123" >> .env.production
git add .env.production
git commit -m "Add production config"

# ✅ CORRECT - Do this
echo "DB_PASSWORD=secret123" >> .env.production
echo ".env.production" >> .gitignore
```

**2. Use Secret Management:**

```bash
# Docker secrets (for Swarm)
echo "secret123" | docker secret create db_password -

# Kubernetes secrets
kubectl create secret generic db-secret \
  --from-literal=password=secret123
```

**3. Rotate Secrets Regularly:**

```bash
# Generate new JWT secret
JWT_SECRET=$(openssl rand -base64 64)
echo "JWT_SECRET=$JWT_SECRET" >> .env.production
```

## 📊 Monitoring Production

### **Health Checks**

**1. Application Health:**

```bash
# Check application status
curl -f http://localhost:8080/actuator/health

# Check specific components
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/disk
```

**2. Container Health:**

```bash
# Check container status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Check resource usage
docker stats nexsplit-app-prod
```

**3. Log Monitoring:**

```bash
# View application logs
docker logs -f nexsplit-app-prod

# Monitor error logs
docker logs nexsplit-app-prod 2>&1 | grep ERROR
```

## 🔄 Continuous Deployment

### **Automated Pipeline**

**1. Build and Push:**

```bash
# Automated build script
./build-and-push.sh v1.0.0
```

**2. Deploy to Production:**

```bash
# Automated deployment
./deploy-production.sh v1.0.0
```

**3. Post-Deployment Verification:**

```bash
# Automated health checks
./health-check.sh
```

---

## 📋 Summary

### **Key Points:**

1. **Tags serve different purposes**: `dev` for development, `latest` for current production
2. **Environment variables are runtime, not build-time**: They're injected when containers start
3. **Production should use versioned tags**: `v1.0.0`, `v1.0.1`, etc.
4. **Always have rollback capability**: Keep previous versions tagged
5. **Security first**: Never commit secrets to version control

### **Production Checklist:**

- [ ] Use semantic versioning (`v1.0.0`)
- [ ] Set up proper environment variables
- [ ] Configure health checks
- [ ] Set up monitoring and logging
- [ ] Plan rollback strategy
- [ ] Secure secrets management
- [ ] Test deployment process
