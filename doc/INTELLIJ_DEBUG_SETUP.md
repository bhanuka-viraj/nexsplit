# IntelliJ Debug Setup Guide

This guide explains how to set up IntelliJ IDEA for debugging the NexSplit application with supporting services running in Docker containers.

## 🎯 Overview

The debug setup allows you to:

- Run the main application directly in IntelliJ for easy debugging
- Use breakpoints, step-through debugging, and hot reload
- Have supporting services (database, logging) running in Docker
- Access all logs and monitoring tools

## 📋 Prerequisites

- **IntelliJ IDEA** (Community or Ultimate edition)
- **Java 21** installed and configured
- **Docker Desktop** installed and running
- **Git** for version control

## 🚀 Quick Start

### Step 1: Start Supporting Services

Run one of the debug startup scripts:

**Windows (PowerShell):**

```powershell
.\scripts\start-debug.ps1
```

**Linux/macOS (Bash):**

```bash
./scripts/start-debug.sh
```

This will start:

- PostgreSQL Database (localhost:5432)
- Elasticsearch (localhost:9200)
- Kibana (http://localhost:5601)
- Filebeat (log shipping)

### Step 2: Open Project in IntelliJ

1. **Open IntelliJ IDEA**
2. **Open Project**: Select the NexSplit project folder
3. **Wait for indexing** to complete
4. **Verify Java 21**: Go to `File → Project Structure → Project SDK`

### Step 3: Configure Run Configuration

1. **Go to**: `Run → Edit Configurations...`
2. **Click**: `+` → `Application`
3. **Configure**:
   - **Name**: `NexSplit Debug`
   - **Main class**: `com.nexsplit.NexsplitApplication`
   - **Module**: `nexsplit.main`
   - **VM options**: `-Dspring.profiles.active=dev`
   - **Working directory**: `$MODULE_DIR$`
   - **Use classpath of module**: `nexsplit.main`

### Step 4: Set Environment Variables (Optional)

If you want to override environment variables in IntelliJ:

1. **In Run Configuration**: Go to `Environment variables`
2. **Add variables**:
   ```
   DB_PASSWORD=Viraj@2002
   JWT_SECRET=dev-jwt-secret-key-for-local-development-only
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   MAIL_FROM=your-email@gmail.com
   MAIL_FROM_NAME=NexSplit
   APP_BASE_URL=http://localhost:8080
   ```

### Step 5: Run and Debug

1. **Click**: `Run` or `Debug` button
2. **Wait**: For application to start (should see Spring Boot banner)
3. **Verify**: Application is running at http://localhost:8080

## 🔧 Configuration Details

### Application Properties

The application uses the `dev` profile when running in IntelliJ:

```yaml
# application.yml (dev profile)
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/nexsplit
    username: postgres
    password: ${DB_PASSWORD:Viraj@2002}
```

### Database Connection

- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `nexsplit`
- **Username**: `postgres`
- **Password**: From environment variable or default

### Logging Configuration

Logs are written to:

- **Console**: IntelliJ console output
- **File**: `logs/nexsplit.log` (relative to project root)
- **Elasticsearch**: Via Filebeat for centralized logging

## 🐛 Debugging Features

### Breakpoints

Set breakpoints in:

- **Controllers**: `AuthController`, `UserController`
- **Services**: `UserServiceImpl`
- **Repositories**: Database operations
- **Utilities**: `JwtUtil`, `PasswordUtil`

### Hot Reload

Enable hot reload for faster development:

1. **Install**: Spring Boot DevTools (already in pom.xml)
2. **Enable**: Automatic restart in IntelliJ
3. **Trigger**: Save files to auto-restart

### Database Debugging

- **View**: Database connections in IntelliJ Database tool
- **Query**: Use IntelliJ's database console
- **Monitor**: SQL queries in application logs

## 📊 Monitoring and Logs

### Kibana Dashboard

Access Kibana at http://localhost:5601 to:

- View structured logs
- Create custom dashboards
- Monitor application performance
- Search and filter logs

### Application Logs

Logs are available in:

- **IntelliJ Console**: Real-time application logs
- **File**: `logs/nexsplit.log`
- **Kibana**: Centralized log aggregation

### Health Checks

Monitor application health:

- **Application**: http://localhost:8080/actuator/health
- **Database**: PostgreSQL health check
- **Elasticsearch**: http://localhost:9200/\_cluster/health

## 🔍 Common Debug Scenarios

### Email Service Debugging

1. **Set breakpoint** in `UserServiceImpl.requestPasswordReset()`
2. **Register a new user** to trigger email sending
3. **Step through** the email sending process
4. **Check logs** for SMTP connection details

### Authentication Debugging

1. **Set breakpoint** in `AuthController.register()`
2. **Register a new user** via API
3. **Step through** JWT token generation
4. **Verify** user creation in database

### Database Operations

1. **Set breakpoint** in `UserServiceImpl.registerUser()`
2. **Monitor** SQL queries in logs
3. **Check** database state in IntelliJ Database tool
4. **Verify** transaction rollback on errors

## 🛠️ Troubleshooting

### Application Won't Start

**Check:**

1. **Java version**: Ensure Java 21 is set
2. **Port conflicts**: Ensure port 8080 is free
3. **Database connection**: Verify PostgreSQL is running
4. **Environment variables**: Check `.env.development` file

### Database Connection Issues

**Solutions:**

1. **Verify Docker**: `docker ps` to check PostgreSQL container
2. **Check credentials**: Verify `DB_PASSWORD` environment variable
3. **Test connection**: Use IntelliJ Database tool
4. **Restart services**: Run debug script again

### Email Service Issues

**Debug steps:**

1. **Check SMTP settings**: Verify Gmail App Password
2. **Set breakpoints**: In `UserServiceImpl` methods
3. **Monitor logs**: Look for SMTP connection errors
4. **Test manually**: Use email service directly

### Logging Issues

**Solutions:**

1. **Check Filebeat**: `docker logs nexsplit-filebeat-debug`
2. **Verify Elasticsearch**: http://localhost:9200
3. **Check Kibana**: http://localhost:5601
4. **If Filebeat fails to start due to Kibana not being ready:**
   ```bash
   # Wait for Kibana to be healthy, then restart Filebeat
   docker-compose -f docker-compose.debug.yml up filebeat -d
   ```
5. **Restart services**: If logs aren't appearing

## 🧹 Cleanup

### Stop Debug Services

```bash
# Stop all debug containers
docker-compose -f docker-compose.debug.yml down

# Remove volumes (optional - will delete data)
docker-compose -f docker-compose.debug.yml down -v
```

### Reset Database

```bash
# Remove PostgreSQL volume
docker volume rm nexsplit_postgres_data_debug

# Restart services
./scripts/start-debug.sh
```

## 📚 Additional Resources

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **IntelliJ IDEA Documentation**: https://www.jetbrains.com/idea/documentation/
- **Docker Documentation**: https://docs.docker.com/
- **Elasticsearch Documentation**: https://www.elastic.co/guide/index.html

## 🆘 Support

If you encounter issues:

1. **Check logs**: Application and Docker container logs
2. **Verify configuration**: Environment variables and properties
3. **Restart services**: Use debug scripts to restart
4. **Check documentation**: This guide and project README
5. **Create issue**: On the project repository

---

**Happy Debugging! 🐛✨**
