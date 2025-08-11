# 🐳 Docker Setup for NexSplit

This document explains how to build, run, and deploy the NexSplit expense tracker application using Docker.

## 📋 Prerequisites

- Docker installed on your system
- Docker Compose installed
- DockerHub account (for pushing images)

## 🏗️ Project Structure

```
nexsplit/
├── Dockerfile                 # Multi-stage Docker build
├── .dockerignore             # Files to exclude from build
├── docker-compose.yml        # Development environment
├── docker-compose.prod.yml   # Production environment
├── build-and-push.sh         # Build and push script
├── env.production.template   # Production environment template
├── env.development.template  # Development environment template
└── src/main/resources/
    └── application-docker.yml # Docker-specific configuration
```

## 🚀 Quick Start

### 1. Development Environment

```bash
# Copy and configure environment files
cp env.development.template .env.development
nano .env.development  # Edit with your values

# Start the entire stack (app + database)
docker-compose up -d

# View logs
docker-compose logs -f nexsplit-app

# Stop the stack
docker-compose down
```

### 2. Production Deployment

```bash
# Copy and configure production environment
cp env.production.template .env.production
nano .env.production  # Edit with your production values

# Build and push to DockerHub
./build-and-push.sh v1.0.0

# Deploy using production compose
docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Detailed Instructions

### Building the Docker Image

```bash
# Build locally
docker build -t nexsplit:latest .

# Build with specific version
docker build -t bhanukaviraj/nexsplit:v1.0.0 .
```

### Running Locally

```bash
# Development with database
docker-compose up -d

# Production (requires external database)
docker-compose -f docker-compose.prod.yml up -d
```

### Pushing to DockerHub

```bash
# Login to DockerHub
docker login

# Use the build script
./build-and-push.sh v1.0.0

# Or manually
docker tag nexsplit:latest bhanukaviraj/nexsplit:v1.0.0
docker push bhanukaviraj/nexsplit:v1.0.0
```

## 🔄 How to Change Values Without Rebuilding

### 1. Development Environment

**Change values in `.env.development`:**

```bash
# Edit the environment file
nano .env.development

# Change any value, for example:
JWT_SECRET=new-dev-secret-key
DB_PASSWORD=new-local-password
```

**Restart Docker containers:**

```bash
# Stop containers
docker-compose down

# Start containers (will pick up new environment variables)
docker-compose up -d

# Or restart just the application container
docker-compose restart nexsplit-app
```

### 2. Production Environment

**Change values in `.env.production`:**

```bash
# Edit the environment file
nano .env.production

# Change any value
JWT_SECRET=new-production-secret
SPRING_DATASOURCE_PASSWORD=new-db-password
```

**Reload and restart:**

```bash
# Load new environment variables
source .env.production

# Restart production containers
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### 3. Runtime Environment Variable Changes

**For running containers:**

```bash
# Stop the container
docker stop nexsplit-app

# Start with new environment variables
docker run -e JWT_SECRET=new-secret -e DB_PASSWORD=new-password nexsplit:latest

# Or update environment variables in running container
docker exec nexsplit-app sh -c "export JWT_SECRET=new-secret && java -jar app.jar"
```

## 🚀 How to Run the Application

### 1. Local Development (No Docker)

**Prerequisites:**

- Java 21 installed
- PostgreSQL running locally
- Maven installed (or use Maven wrapper)

**Setup:**

```bash
# Copy and configure development environment
cp env.development.template .env.development
nano .env.development  # Edit with your values

# Load environment variables
source .env.development  # Linux/Mac
# OR
Get-Content .env.development | ForEach-Object {
    if($_ -match '^([^#].+)=(.+)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}  # Windows PowerShell
```

**Run the application:**

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/nexsplit-0.0.1-SNAPSHOT.jar
```

**Verify it's running:**

```bash
# Check if application is responding
curl http://localhost:8080/actuator/health

# Check logs
tail -f logs/nexsplit.log
```

### 2. Docker Development

**Prerequisites:**

- Docker installed
- Docker Compose installed

**Setup:**

```bash
# Copy and configure development environment
cp env.development.template .env.development
nano .env.development  # Edit with your values
```

**Run with Docker Compose:**

```bash
# Start all services (app + database)
docker-compose up -d

# View logs
docker-compose logs -f nexsplit-app

# Check status
docker-compose ps
```

**Stop and cleanup:**

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (database data)
docker-compose down -v
```

### 3. Docker Production

**Prerequisites:**

- Docker installed
- Docker Compose installed
- External PostgreSQL database configured

**Setup:**

```bash
# Copy and configure production environment
cp env.production.template .env.production
nano .env.production  # Edit with your production values

# Generate secure secrets
JWT_SECRET=$(openssl rand -base64 64)
echo "JWT_SECRET=$JWT_SECRET" >> .env.production
```

**Build and push Docker image:**

```bash
# Build and push to DockerHub
./build-and-push.sh v1.0.0
```

**Deploy:**

```bash
# Load production environment variables
source .env.production

# Deploy with production compose
docker-compose -f docker-compose.prod.yml up -d

# Check deployment
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f
```

## 📥 How to Load Environment Variables

### 1. Linux/Mac (Bash/Zsh)

**Load from file:**

```bash
# Load all variables from file
source .env.development

# Or export specific variables
export JWT_SECRET=my-secret
export DB_PASSWORD=my-password
```

**Check loaded variables:**

```bash
# List all environment variables
env | grep JWT
env | grep DB

# Check specific variable
echo $JWT_SECRET
```

### 2. Windows PowerShell

**Load from file:**

```powershell
# Load all variables from file
Get-Content .env.development | ForEach-Object {
    if($_ -match '^([^#].+)=(.+)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}

# Or set specific variables
$env:JWT_SECRET = "my-secret"
$env:DB_PASSWORD = "my-password"
```

**Check loaded variables:**

```powershell
# List environment variables
Get-ChildItem Env: | Where-Object { $_.Name -like "*JWT*" }
Get-ChildItem Env: | Where-Object { $_.Name -like "*DB*" }

# Check specific variable
echo $env:JWT_SECRET
```

### 3. Windows Command Prompt

**Load from file:**

```cmd
# Load all variables from file
for /f "tokens=1,2 delims==" %a in (.env.development) do set %a=%b

# Or set specific variables
set JWT_SECRET=my-secret
set DB_PASSWORD=my-password
```

**Check loaded variables:**

```cmd
# List environment variables
set | findstr JWT
set | findstr DB

# Check specific variable
echo %JWT_SECRET%
```

### 4. Docker Environment Variables

**Pass to Docker run:**

```bash
# Individual variables
docker run -e JWT_SECRET=my-secret -e DB_PASSWORD=my-password nexsplit:latest

# From environment file
docker run --env-file .env.production nexsplit:latest

# From current environment
docker run --env JWT_SECRET --env DB_PASSWORD nexsplit:latest
```

**Docker Compose environment:**

```yaml
# In docker-compose.yml
environment:
  - JWT_SECRET=${JWT_SECRET}
  - DB_PASSWORD=${DB_PASSWORD}

# Or use env_file
env_file:
  - .env.production
```

## 🌍 Environment Variables

### Development

The development environment uses default values from `application.yml`.

### Production

Set these environment variables for production:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/nexsplit
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your-super-secure-jwt-secret
JWT_EXPIRATION=3600

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=https://your-domain.com/login/oauth2/code/google

# Application
SPRING_PROFILES_ACTIVE=production
FRONTEND_URL=https://your-frontend-domain.com
```

## 📊 Health Checks

The application includes health checks:

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check Docker container health
docker ps
```

## 🔍 Troubleshooting

### Common Issues

1. **Port already in use**

   ```bash
   # Change port in docker-compose.yml
   ports:
     - "8081:8080"  # Use different host port
   ```

2. **Database connection issues**

   ```bash
   # Check database logs
   docker-compose logs postgres

   # Check application logs
   docker-compose logs nexsplit-app
   ```

3. **Permission issues**

   ```bash
   # Make build script executable
   chmod +x build-and-push.sh
   ```

4. **Environment variable issues**

   ```bash
   # Check if variables are loaded
   env | grep JWT
   env | grep DB

   # Reload environment variables
   source .env.development
   ```

### Useful Commands

```bash
# View running containers
docker ps

# View logs
docker-compose logs -f

# Execute commands in container
docker-compose exec nexsplit-app sh

# Clean up
docker-compose down -v
docker system prune -f
```

## 🚀 Deployment on Server

### 1. Pull the Image

```bash
docker pull bhanukaviraj/nexsplit:latest
```

### 2. Set Environment Variables

```bash
# Copy environment template
cp env.production.template .env.production

# Edit with your production values
nano .env.production

# Load environment variables
source .env.production
```

### 3. Run the Container

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 4. Verify Deployment

```bash
# Check if container is running
docker ps

# Test the application
curl http://your-server-ip:8080/actuator/health
```

## 📝 Notes

- The application runs on port 8080 inside the container
- Logs are stored in `/app/logs/` inside the container
- Database migrations run automatically on startup
- Health checks are configured to monitor application status
- The application uses a non-root user for security

## 🔗 Useful Links

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
