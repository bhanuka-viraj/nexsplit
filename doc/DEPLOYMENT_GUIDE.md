# 🚀 Deployment Guide: DockerHub to Contabo Server

## 📋 Overview

This guide covers:

1. **Building and pushing development version to DockerHub**
2. **Deploying to Contabo server with Dokeploy**
3. **Environment configuration for production**

## 🐳 Step 1: Push Development Version to DockerHub

### Prerequisites

- Docker installed and running
- DockerHub account (`bhanukaviraj`)
- Logged into DockerHub: `docker login`

### Build and Push Process

**Option A: Using the Automated Script (Recommended)**

```bash
# Run the build and push script
./build-and-push.sh v1.0.0-dev

# Or for latest development version
./build-and-push.sh dev
```

**Option B: Manual Process**

```bash
# 1. Build the Docker image
docker build -t bhanukaviraj/nexsplit:dev .

# 2. Tag for DockerHub
docker tag bhanukaviraj/nexsplit:dev bhanukaviraj/nexsplit:latest

# 3. Push to DockerHub
docker push bhanukaviraj/nexsplit:dev
docker push bhanukaviraj/nexsplit:latest
```

### Verify Push Success

```bash
# Check if image is available on DockerHub
docker pull bhanukaviraj/nexsplit:dev

# List local images
docker images | grep nexsplit
```

## 🖥️ Step 2: Deploy to Contabo Server with Dokeploy

### Prerequisites

- Contabo server with Dokeploy installed
- SSH access to your server
- Domain name configured (optional but recommended)

### Server Setup

**1. Connect to Your Contabo Server**

```bash
ssh root@your-server-ip
```

**2. Create Application Directory**

```bash
mkdir -p /opt/nexsplit
cd /opt/nexsplit
```

**3. Create Production Environment File**

```bash
# Create production environment file
nano .env.production
```

**Add the following content:**

```bash
# ========================================
# PRODUCTION ENVIRONMENT VARIABLES
# ========================================

# DATABASE CONFIGURATION
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/nexsplit
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_secure_production_password

# JWT CONFIGURATION
JWT_SECRET=your-super-secure-production-jwt-secret-key-here
JWT_EXPIRATION=3600

# OAUTH2 CONFIGURATION
GOOGLE_CLIENT_ID=your-production-google-client-id
GOOGLE_CLIENT_SECRET=your-production-google-client-secret

# APPLICATION CONFIGURATION
SPRING_PROFILES_ACTIVE=production
FRONTEND_URL=https://your-domain.com

# SERVER CONFIGURATION
SERVER_PORT=8080
```

**4. Create Docker Compose File for Production**

```bash
nano docker-compose.prod.yml
```

**Add the following content:**

```yaml
version: "3.8"
services:
  nexsplit-app:
    image: bhanukaviraj/nexsplit:dev
    container_name: nexsplit-app-prod
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-3600}
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-production}
    ports:
      - "8080:8080"
    networks:
      - nexsplit-network
    volumes:
      - nexsplit_logs:/app/logs
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

volumes:
  nexsplit_logs:
networks:
  nexsplit-network:
    driver: bridge
```

### Deploy with Dokeploy

**1. Create Dokeploy Configuration**

```bash
nano dokeploy.yml
```

**Add the following content:**

```yaml
version: "3.8"
services:
  nexsplit:
    image: bhanukaviraj/nexsplit:dev
    container_name: nexsplit-app
    environment:
      - SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=${JWT_EXPIRATION}
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
      - SPRING_PROFILES_ACTIVE=production
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
```

**2. Deploy with Dokeploy**

```bash
# Deploy the application
dokeploy up -d

# Check deployment status
dokeploy ps

# View logs
dokeploy logs -f nexsplit
```

**3. Alternative: Using Docker Compose Directly**

```bash
# Load environment variables
source .env.production

# Deploy with docker-compose
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

## 🔧 Step 3: Database Setup on Contabo Server

### Option A: External Database (Recommended)

```bash
# If using external database service (AWS RDS, DigitalOcean, etc.)
# Update SPRING_DATASOURCE_URL in .env.production
```

### Option B: Local PostgreSQL on Server

```bash
# Install PostgreSQL
apt update
apt install postgresql postgresql-contrib

# Start PostgreSQL service
systemctl start postgresql
systemctl enable postgresql

# Create database and user
sudo -u postgres psql

# In PostgreSQL prompt:
CREATE DATABASE nexsplit;
CREATE USER nexsplit_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE nexsplit TO nexsplit_user;
\q

# Update .env.production with local database details
```

## 🌐 Step 4: Nginx Configuration (Optional)

**1. Install Nginx**

```bash
apt install nginx
```

**2. Create Nginx Configuration**

```bash
nano /etc/nginx/sites-available/nexsplit
```

**Add the following content:**

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**3. Enable Site**

```bash
ln -s /etc/nginx/sites-available/nexsplit /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx
```

## 🔒 Step 5: SSL Certificate (Optional)

**Using Let's Encrypt:**

```bash
# Install Certbot
apt install certbot python3-certbot-nginx

# Get SSL certificate
certbot --nginx -d your-domain.com -d www.your-domain.com

# Auto-renewal
crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

## ✅ Step 6: Verification

**1. Check Application Health**

```bash
# Health check
curl http://localhost:8080/actuator/health

# Or if using domain
curl https://your-domain.com/actuator/health
```

**2. Check Application Logs**

```bash
# View application logs
docker logs nexsplit-app

# Or with docker-compose
docker-compose -f docker-compose.prod.yml logs -f
```

**3. Monitor Resources**

```bash
# Check container status
docker ps

# Check resource usage
docker stats nexsplit-app
```

## 🔄 Step 7: Continuous Deployment

### Automated Deployment Script

```bash
nano deploy.sh
```

**Add the following content:**

```bash
#!/bin/bash

# ========================================
# AUTOMATED DEPLOYMENT SCRIPT
# ========================================

set -e

echo "Starting deployment..."

# Pull latest image
docker pull bhanukaviraj/nexsplit:dev

# Stop existing container
docker-compose -f docker-compose.prod.yml down

# Start with new image
docker-compose -f docker-compose.prod.yml up -d

# Wait for health check
echo "Waiting for application to start..."
sleep 30

# Health check
if curl -f http://localhost:8080/actuator/health; then
    echo "Deployment successful!"
else
    echo "Deployment failed!"
    exit 1
fi
```

**Make executable and run:**

```bash
chmod +x deploy.sh
./deploy.sh
```

## 🆘 Troubleshooting

### Common Issues

**1. Application Won't Start**

```bash
# Check logs
docker logs nexsplit-app

# Check environment variables
docker exec nexsplit-app env | grep SPRING
```

**2. Database Connection Issues**

```bash
# Test database connection
docker exec nexsplit-app wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health

# Check database logs
docker logs postgres-container
```

**3. Port Already in Use**

```bash
# Find process using port 8080
netstat -tulpn | grep :8080

# Kill process
kill -9 <process_id>
```

**4. Memory Issues**

```bash
# Check memory usage
free -h

# Increase swap if needed
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## 📊 Monitoring

### Application Monitoring

```bash
# Monitor application logs
tail -f /opt/nexsplit/logs/nexsplit.log

# Monitor system resources
htop

# Monitor Docker containers
docker stats
```

### Log Rotation

```bash
# Create logrotate configuration
nano /etc/logrotate.d/nexsplit

# Add:
/opt/nexsplit/logs/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    create 644 root root
}
```

## 🔄 Update Process

### For Future Updates

**1. Build and Push New Version**

```bash
# Local machine
./build-and-push.sh v1.0.1-dev
```

**2. Deploy to Server**

```bash
# On server
cd /opt/nexsplit
./deploy.sh
```

**3. Rollback if Needed**

```bash
# Rollback to previous version
docker tag bhanukaviraj/nexsplit:previous-version bhanukaviraj/nexsplit:dev
./deploy.sh
```

---

**🎉 Your application is now deployed and running on your Contabo server!**

**Next Steps:**

- Set up monitoring and alerting
- Configure backup strategies
- Set up CI/CD pipeline for automated deployments
- Configure domain and SSL certificate
