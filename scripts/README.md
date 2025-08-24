# 📜 NexSplit Scripts Documentation

This directory contains all the automation scripts for the NexSplit application. These scripts provide easy-to-use commands for development, deployment, and monitoring.

## 📁 Script Overview

### **Development Scripts**

- `start-dev.ps1` / `start-dev.sh` - Local development environment
- `start-elasticsearch.ps1` / `start-elasticsearch.sh` - Full stack with monitoring
- `start-debug.ps1` / `start-debug.sh` - IntelliJ debugging environment

### **Server Scripts**

- `start-server.ps1` / `start-server.sh` - Server deployment using pre-built images

### **Production Scripts**

- `deploy-production.ps1` / `deploy-production.sh` - Production deployment
- `build-and-push.sh` - Docker image build and push

## 🚀 Quick Start

### **Windows (PowerShell)**

```powershell
# Development
.\scripts\start-dev.ps1
.\scripts\start-elasticsearch.ps1

# Server
.\scripts\start-server.ps1

# Production
.\scripts\deploy-production.ps1 v1.0.0
```

### **Linux/Mac (Bash)**

```bash
# Make scripts executable (first time only)
chmod +x scripts/*.sh

# Development
./scripts/start-dev.sh
./scripts/start-elasticsearch.sh

# Server
./scripts/start-server.sh

# Production
./scripts/deploy-production.sh v1.0.0
./scripts/build-and-push.sh v1.0.0
```

## 📋 Script Details

### **1. Development Scripts**

#### **start-dev.ps1 / start-dev.sh**

**Purpose**: Start the application for local development

**Features**:

- ✅ Loads environment variables from `.env.development`
- ✅ Validates required and optional variables
- ✅ Checks prerequisites (Java, Maven, PostgreSQL)
- ✅ Starts Spring Boot application with Maven
- ✅ Provides health check URLs

**Usage**:

```powershell
# Windows
.\scripts\start-dev.ps1

# Linux/Mac
./scripts/start-dev.sh
```

**Prerequisites**:

- Java 21 installed
- Maven (or Maven wrapper)
- PostgreSQL running on port 5432
- `.env.development` file (optional, uses defaults if not found)

**Environment Variables**:

- **Required**: `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`
- **Optional**: Email configuration (`MAIL_HOST`, `MAIL_PORT`, etc.)

---

#### **start-debug.ps1 / start-debug.sh**

**Purpose**: Start supporting services for IntelliJ debugging

**Features**:

- ✅ Loads environment variables from `.env.development`
- ✅ Starts only supporting services (no main application)
- ✅ PostgreSQL, Elasticsearch, Kibana, and Filebeat
- ✅ Health checks and service monitoring
- ✅ Perfect for IntelliJ IDEA debugging
- ✅ Provides detailed setup instructions

**Usage**:

```powershell
# Windows
.\scripts\start-debug.ps1

# Linux/Mac
./scripts/start-debug.sh
```

**Prerequisites**:

- Docker Desktop installed and running
- Docker Compose available
- `.env.development` file (optional, uses defaults if not found)

**Services Started**:

- **PostgreSQL**: `localhost:5432` (database)
- **Elasticsearch**: `localhost:9200` (search engine)
- **Kibana**: `http://localhost:5601` (log visualization)
- **Filebeat**: Log shipping to Elasticsearch

**Next Steps**:

1. Open IntelliJ IDEA
2. Open the NexSplit project
3. Set VM options: `-Dspring.profiles.active=dev`
4. Run `NexsplitApplication.main()`
5. Application will be available at `http://localhost:8080`

**Useful URLs**:

- Application: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Kibana: `http://localhost:5601`
- Elasticsearch: `http://localhost:9200`

**Environment Variables**:

- **Required**: `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`
- **Optional**: Email configuration (`MAIL_HOST`, `MAIL_PORT`, etc.)

---

#### **start-elasticsearch.ps1 / start-elasticsearch.sh**

**Purpose**: Start complete stack with Elasticsearch monitoring

**Features**:

- ✅ Loads environment variables from `.env.development`
- ✅ Starts PostgreSQL, Elasticsearch, Kibana, and application
- ✅ Performs health checks on all services
- ✅ Provides monitoring dashboard URLs

**Usage**:

```powershell
# Windows
.\scripts\start-elasticsearch.ps1

# Linux/Mac
./scripts/start-elasticsearch.sh
```

**Services Started**:

- **NexSplit App**: http://localhost:8080
- **Swagger API**: http://localhost:8080/swagger-ui.html
- **Kibana**: http://localhost:5601
- **Elasticsearch**: http://localhost:9200

**Prerequisites**:

- Docker and Docker Compose installed
- `.env.development` file (optional)

---

#### **start-server.ps1 / start-server.sh**

**Purpose**: Deploy server using pre-built Docker images

**Features**:

- ✅ Loads environment variables from `.env.production` (optional)
- ✅ Uses pre-built DockerHub images
- ✅ Starts PostgreSQL, Elasticsearch, Kibana, and application
- ✅ Performs service status checks
- ✅ Provides monitoring dashboard URLs

**Usage**:

```powershell
# Windows
.\scripts\start-server.ps1

# Linux/Mac
./scripts/start-server.sh
```

**Services Started**:

- **NexSplit App**: http://localhost:8080
- **Swagger API**: http://localhost:8080/swagger-ui.html
- **Kibana**: http://localhost:5601
- **Elasticsearch**: http://localhost:9200

**Prerequisites**:

- Docker and Docker Compose installed
- `docker-compose.server.yml` file
- `.env.production` file (optional)

**Note**: This script uses pre-built DockerHub image `bhanukaviraj/nexsplit:dev`

---

### **2. Production Scripts**

#### **deploy-production.ps1 / deploy-production.sh**

**Purpose**: Deploy application to production environment

**Features**:

- ✅ **Required**: Loads environment variables from `.env.production`
- ✅ Validates critical production variables
- ✅ Checks Docker and Docker Compose installation
- ✅ Pulls latest Docker image
- ✅ Deploys with health checks
- ✅ Provides rollback capability

**Usage**:

```powershell
# Windows
.\scripts\deploy-production.ps1 v1.0.0
.\scripts\deploy-production.ps1          # Uses 'latest' tag

# Linux/Mac
./scripts/deploy-production.sh v1.0.0
./scripts/deploy-production.sh           # Uses 'latest' tag
```

**Prerequisites**:

- Docker and Docker Compose installed
- `.env.production` file (required)
- `docker-compose.prod.yml` file
- DockerHub access for image pulling

**Critical Environment Variables** (must be set in `.env.production`):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

**Optional Environment Variables**:

- Email configuration (`MAIL_HOST`, `MAIL_PORT`, etc.)

**Deployment Process**:

1. Load and validate environment variables
2. Check Docker prerequisites
3. Pull latest Docker image
4. Stop existing containers
5. Start production containers
6. Perform health checks
7. Report deployment status

---

#### **build-and-push.sh**

**Purpose**: Build and push Docker image to DockerHub

**Features**:

- ✅ Loads environment variables from `.env.production` (optional)
- ✅ Builds Docker image with specified version
- ✅ Tags image as both version and 'latest'
- ✅ Pushes to DockerHub
- ✅ Shows available image tags

**Usage**:

```bash
# Build and push specific version
./scripts/build-and-push.sh v1.0.0

# Build and push as 'latest'
./scripts/build-and-push.sh
```

**Prerequisites**:

- Docker installed
- DockerHub account with repository access
- `.env.production` file (optional, for build context)

**Configuration**:
Edit the script to change:

- `DOCKER_USERNAME`: Your DockerHub username
- `IMAGE_NAME`: Repository name (default: "nexsplit")

---

## 🔧 Environment Setup

### **Development Environment**

```bash
# Copy template
cp env.development.template .env.development

# Edit with your values
notepad .env.development  # Windows
nano .env.development     # Linux/Mac
```

**Example `.env.development`**:

```bash
# Database
DB_PASSWORD=Viraj@2002

# JWT
JWT_SECRET=dev-jwt-secret-key-for-local-development-only
JWT_EXPIRATION=60

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id-for-development
GOOGLE_CLIENT_SECRET=your-google-client-secret-for-development

# Email (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@nexsplit.com
MAIL_FROM_NAME=NexSplit
APP_BASE_URL=http://localhost:8080
EMAIL_RATE_LIMIT=10
EMAIL_DAILY_LIMIT=50

# Application
SPRING_PROFILES_ACTIVE=dev
FRONTEND_URL=http://localhost:3000
```

### **Production Environment**

```bash
# Copy template
cp env.production.template .env.production

# Edit with your production values
notepad .env.production  # Windows
nano .env.production     # Linux/Mac
```

**Example `.env.production`**:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/nexsplit
SPRING_DATASOURCE_USERNAME=nexsplit_prod_user
SPRING_DATASOURCE_PASSWORD=super-secure-production-password-123

# JWT
JWT_SECRET=your-super-secure-256-bit-production-jwt-secret-key-here
JWT_EXPIRATION=3600

# OAuth2
GOOGLE_CLIENT_ID=your-production-google-client-id
GOOGLE_CLIENT_SECRET=your-production-google-client-secret

# Email (Production)
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
MAIL_FROM=noreply@yourdomain.com
MAIL_FROM_NAME=NexSplit
APP_BASE_URL=https://yourdomain.com
EMAIL_RATE_LIMIT=100
EMAIL_DAILY_LIMIT=1000

# Application
SPRING_PROFILES_ACTIVE=production
FRONTEND_URL=https://yourdomain.com
SERVER_PORT=8080
```

---

## 🔄 Workflow Examples

### **Development Workflow**

```bash
# 1. Set up environment
cp env.development.template .env.development
# Edit .env.development with your values

# 2. Start development
./scripts/start-dev.sh

# 3. Or start with monitoring
./scripts/start-elasticsearch.sh
```

### **Production Deployment Workflow**

```bash
# 1. Set up production environment
cp env.production.template .env.production
# Edit .env.production with production values

# 2. Build and push image
./scripts/build-and-push.sh v1.0.0

# 3. Deploy to production
./scripts/deploy-production.sh v1.0.0
```

### **Hotfix Workflow**

```bash
# 1. Build hotfix version
./scripts/build-and-push.sh v1.0.1

# 2. Deploy hotfix
./scripts/deploy-production.sh v1.0.1

# 3. Rollback if needed
./scripts/deploy-production.sh v1.0.0
```

---

## 🛠️ Troubleshooting

### **Common Issues**

#### **Environment Variables Not Loading**

```bash
# Check if .env file exists
ls -la .env.development
ls -la .env.production

# Verify file format (no spaces around =)
cat .env.development
```

#### **Docker Issues**

```bash
# Check Docker installation
docker --version
docker-compose --version

# Check Docker daemon
docker ps

# Check Docker Hub access
docker login
```

#### **Health Check Failures**

```bash
# Check container logs
docker-compose logs -f

# Check specific service
docker-compose logs nexsplit-app

# Check health endpoint manually
curl http://localhost:8080/actuator/health
```

#### **Permission Issues (Linux/Mac)**

```bash
# Make scripts executable
chmod +x scripts/*.sh

# Check permissions
ls -la scripts/
```

### **Useful Commands**

#### **Development**

```bash
# View application logs
tail -f logs/nexsplit.log

# Check database connection
psql -h localhost -U postgres -d nexsplit

# Restart application
./scripts/start-dev.sh
```

#### **Production**

```bash
# View production logs
docker-compose -f docker-compose.prod.yml logs -f

# Check production health
curl https://yourdomain.com/actuator/health

# Restart production services
docker-compose -f docker-compose.prod.yml restart

# Rollback to previous version
./scripts/deploy-production.sh v1.0.0
```

#### **Monitoring**

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# Check Kibana status
curl http://localhost:5601/api/status

# View monitoring logs
docker-compose logs elasticsearch kibana
```

---

## 📊 Script Features Summary

| Script                    | Platform  | Purpose      | Environment Loading   | Health Checks   | Error Handling   |
| ------------------------- | --------- | ------------ | --------------------- | --------------- | ---------------- |
| `start-dev.ps1`           | Windows   | Development  | ✅ `.env.development` | ✅ Application  | ✅ Comprehensive |
| `start-dev.sh`            | Linux/Mac | Development  | ✅ `.env.development` | ✅ Application  | ✅ Comprehensive |
| `start-elasticsearch.ps1` | Windows   | Full Stack   | ✅ `.env.development` | ✅ All Services | ✅ Comprehensive |
| `start-elasticsearch.sh`  | Linux/Mac | Full Stack   | ✅ `.env.development` | ✅ All Services | ✅ Comprehensive |
| `start-server.ps1`        | Windows   | Server       | ✅ `.env.production`  | ✅ All Services | ✅ Comprehensive |
| `start-server.sh`         | Linux/Mac | Server       | ✅ `.env.production`  | ✅ All Services | ✅ Comprehensive |
| `deploy-production.ps1`   | Windows   | Production   | ✅ `.env.production`  | ✅ Application  | ✅ Rollback      |
| `deploy-production.sh`    | Linux/Mac | Production   | ✅ `.env.production`  | ✅ Application  | ✅ Rollback      |
| `build-and-push.sh`       | Linux/Mac | Docker Build | ✅ `.env.production`  | ❌ N/A          | ✅ Build Errors  |

---

## 🔐 Security Notes

### **Development**

- Never commit `.env.development` to version control
- Use weak passwords only for development
- JWT secrets should be unique per environment

### **Production**

- Never commit `.env.production` to version control
- Use strong, unique passwords for production
- Rotate secrets regularly
- Use secret management systems when possible

### **Environment Variables Best Practices**

1. Use descriptive names: `DB_PASSWORD` not `PASS`
2. Set appropriate defaults in `application.yml`
3. Validate required variables on startup
4. Document all variables in templates

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Environment Variables Best Practices](https://12factor.net/config)

---

## 🆘 Getting Help

If you encounter issues:

1. **Check the logs**: Use the logging commands above
2. **Verify environment variables**: Ensure all required variables are set
3. **Check prerequisites**: Verify Docker, Java, PostgreSQL installation
4. **Review this documentation**: Check troubleshooting section
5. **Check application health**: Use health check endpoints

---

**Happy Deploying! 🚀**
