# 🔐 Security Configuration Management

This document explains how to properly manage secrets and sensitive configuration in the NexSplit application.

## **🚨 Security Principles**

### **1. Never Commit Secrets to Version Control**

- ❌ **Never** hardcode passwords, API keys, or secrets in source code
- ❌ **Never** commit `.env` files to Git
- ✅ **Always** use environment variables for sensitive data
- ✅ **Always** use templates for environment configuration

### **2. Environment-Specific Configuration**

- **Development**: Use local environment files (not committed to Git)
- **Staging**: Use environment variables in deployment platform
- **Production**: Use secure secret management systems

## **📁 Environment File Management**

### **Development Environment**

```bash
# Copy the development template
cp env.development.template .env.development

# Edit with your local values
nano .env.development
```

**Development Environment Variables:**

```bash
# Database (local development)
DB_PASSWORD=your-local-password

# JWT (development only)
JWT_SECRET=dev-jwt-secret-key-for-local-development-only

# OAuth2 (development)
GOOGLE_CLIENT_ID=your-dev-google-client-id
GOOGLE_CLIENT_SECRET=your-dev-google-client-secret
```

### **Production Environment**

```bash
# Copy the production template
cp env.production.template .env.production

# Edit with your production values
nano .env.production
```

**Production Environment Variables:**

```bash
# Database (production)
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/nexsplit
SPRING_DATASOURCE_USERNAME=your_production_username
SPRING_DATASOURCE_PASSWORD=your-super-secure-production-password

# JWT (production)
JWT_SECRET=your-super-secure-jwt-secret-key-here

# OAuth2 (production)
GOOGLE_CLIENT_ID=your-production-google-client-id
GOOGLE_CLIENT_SECRET=your-production-google-client-secret
```

## **🔄 How to Change Values Without Rebuilding**

### **1. Local Development (No Docker)**

**Change values in `.env.development`:**

```bash
# Edit the environment file
nano .env.development

# Change any value, for example:
JWT_SECRET=new-dev-secret-key
DB_PASSWORD=new-local-password
```

**Reload environment variables:**

```bash
# Linux/Mac
source .env.development

# Windows PowerShell
Get-Content .env.development | ForEach-Object {
    if($_ -match '^([^#].+)=(.+)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}

# Windows Command Prompt
for /f "tokens=1,2 delims==" %a in (.env.development) do set %a=%b
```

**Restart the application:**

```bash
# Stop the current application (Ctrl+C)
# Then restart
./mvnw spring-boot:run
```

### **2. Docker Development**

**Change values in `.env.development`:**

```bash
# Edit the environment file
nano .env.development

# Change any value
JWT_SECRET=new-docker-dev-secret
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

### **3. Docker Production**

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

### **4. Runtime Environment Variable Changes**

**For running containers:**

```bash
# Stop the container
docker stop nexsplit-app

# Start with new environment variables
docker run -e JWT_SECRET=new-secret -e DB_PASSWORD=new-password nexsplit:latest

# Or update environment variables in running container
docker exec nexsplit-app sh -c "export JWT_SECRET=new-secret && java -jar app.jar"
```

## **🚀 How to Run the Application**

### **1. Local Development (No Docker)**

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

### **2. Docker Development**

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

### **3. Docker Production**

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

## **📥 How to Load Environment Variables**

### **1. Linux/Mac (Bash/Zsh)**

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

### **2. Windows PowerShell**

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

### **3. Windows Command Prompt**

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

### **4. Docker Environment Variables**

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

## **🔧 Docker Environment Management**

### **Development with Docker Compose**

```bash
# Set environment variables for development
export DB_PASSWORD=your-local-password
export JWT_SECRET=dev-jwt-secret

# Start development environment
docker-compose up -d
```

### **Production with Docker Compose**

```bash
# Load production environment variables
source .env.production

# Start production environment
docker-compose -f docker-compose.prod.yml up -d
```

## **🔐 Secret Generation**

### **Generate Secure JWT Secret**

```bash
# Generate a secure 64-character JWT secret
openssl rand -base64 64

# Example output: dG9rZW4tc2VjcmV0LWtleS1mb3ItcHJvZHVjdGlvbi1lbnZpcm9ubWVudC1vbmx5
```

### **Generate Secure Database Password**

```bash
# Generate a secure database password
openssl rand -base64 32

# Example output: c2VjdXJlLXBhc3N3b3JkLWZvci1kYXRhYmFzZQ==
```

## **🚀 Deployment Security**

### **Local Development**

1. **Copy Template**: `cp env.development.template .env.development`
2. **Set Values**: Edit `.env.development` with your local values
3. **Load Variables**: `source .env.development`
4. **Run Application**: `./mvnw spring-boot:run` or `docker-compose up -d`

### **Production Deployment**

1. **Copy Template**: `cp env.production.template .env.production`
2. **Generate Secrets**: Use secure secret generation commands
3. **Set Values**: Edit `.env.production` with production values
4. **Load Variables**: `source .env.production`
5. **Deploy**: `docker-compose -f docker-compose.prod.yml up -d`

### **Cloud Deployment**

1. **Set Environment Variables**: In your cloud platform (AWS, GCP, Azure)
2. **Use Secret Management**: AWS Secrets Manager, Google Secret Manager, etc.
3. **Deploy**: Use your cloud platform's deployment tools

## **🔍 Security Checklist**

### **Before Committing Code**

- [ ] No hardcoded passwords in source code
- [ ] No `.env` files committed to Git
- [ ] All secrets use environment variables
- [ ] Templates are provided for environment setup

### **Before Production Deployment**

- [ ] Strong, unique passwords for all services
- [ ] Secure JWT secret (64+ characters)
- [ ] Production OAuth2 credentials configured
- [ ] Database credentials are secure
- [ ] Environment variables are properly set

### **Ongoing Security**

- [ ] Regular secret rotation
- [ ] Monitor for exposed secrets
- [ ] Use secret management services in production
- [ ] Implement proper access controls

## **⚠️ Common Security Mistakes**

### **❌ What NOT to Do**

```bash
# DON'T: Hardcode secrets in application.yml
jwt:
  secret: my-super-secret-key

# DON'T: Commit .env files
git add .env.production

# DON'T: Use weak secrets
JWT_SECRET=123456

# DON'T: Share secrets in code reviews
```

### **✅ What TO Do**

```bash
# DO: Use environment variables
jwt:
  secret: ${JWT_SECRET}

# DO: Use templates
cp env.production.template .env.production

# DO: Generate strong secrets
JWT_SECRET=$(openssl rand -base64 64)

# DO: Use secret management services
# AWS Secrets Manager, Google Secret Manager, etc.
```

## **🔧 Troubleshooting**

### **Missing Environment Variables**

```bash
# Error: Could not resolve placeholder 'JWT_SECRET'
# Solution: Set the environment variable
export JWT_SECRET=your-secret-key
```

### **Docker Environment Issues**

```bash
# Error: Environment variable not found
# Solution: Pass environment variables to Docker
docker run -e JWT_SECRET=your-secret your-image
```

### **Production Deployment Issues**

```bash
# Error: Database connection failed
# Solution: Check environment variables are set
echo $SPRING_DATASOURCE_PASSWORD
```

### **Environment Variable Loading Issues**

```bash
# Error: Variables not loaded
# Solution: Check file format and reload
cat .env.development  # Check file content
source .env.development  # Reload variables
env | grep JWT  # Verify variables are loaded
```

## **📚 Additional Resources**

- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
- [Docker Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [12 Factor App - Config](https://12factor.net/config)
- [OWASP Security Guidelines](https://owasp.org/www-project-top-ten/)
