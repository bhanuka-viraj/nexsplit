# 🚀 Local Development Guide

## 📋 Prerequisites

Before running the application locally, ensure you have the following installed:

- **Java 21** (OpenJDK or Oracle JDK)
- **Maven** (or use the Maven wrapper included in the project)
- **PostgreSQL 15+** (running locally on port 5432)
- **Git** (for version control)

## 🔧 Environment Setup

### 1. Clone and Navigate to Project

```bash
git clone <your-repository-url>
cd nexsplit
```

### 2. Database Setup

**Start PostgreSQL:**

```bash
# Windows (if using PostgreSQL installer)
# PostgreSQL should be running as a service

# Or using Docker
docker run --name postgres-local -e POSTGRES_DB=nexsplit -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=Viraj@2002 -p 5432:5432 -d postgres:15-alpine
```

**Verify Database Connection:**

```bash
# Test connection
psql -h localhost -U postgres -d nexsplit
# Enter password when prompted: Viraj@2002
```

### 3. Environment Variables Setup

**Option A: Using .env.development file (Recommended)**

1. Copy the template:

```bash
cp env.development.template .env.development
```

2. Edit the file with your values:

```bash
# Windows PowerShell
notepad .env.development

# Or using any text editor
code .env.development
```

3. Set the values in `.env.development`:

```bash
# ========================================
# DEVELOPMENT ENVIRONMENT VARIABLES
# ========================================

# DATABASE CONFIGURATION
DB_PASSWORD=Viraj@2002

# JWT CONFIGURATION
JWT_SECRET=dev-jwt-secret-key-for-local-development-only
JWT_EXPIRATION=60

# OAUTH2 CONFIGURATION
GOOGLE_CLIENT_ID=your-google-client-id-for-development
GOOGLE_CLIENT_SECRET=your-google-client-secret-for-development

# APPLICATION CONFIGURATION
SPRING_PROFILES_ACTIVE=dev
FRONTEND_URL=http://localhost:3000
```

**Option B: Setting Environment Variables Manually**

**Windows PowerShell:**

```powershell
$env:DB_PASSWORD = "Viraj@2002"
$env:JWT_SECRET = "dev-jwt-secret-key-for-local-development-only"
$env:JWT_EXPIRATION = "60"
$env:GOOGLE_CLIENT_ID = "your-google-client-id-for-development"
$env:GOOGLE_CLIENT_SECRET = "your-google-client-secret-for-development"
```

**Windows Command Prompt:**

```cmd
set DB_PASSWORD=Viraj@2002
set JWT_SECRET=dev-jwt-secret-key-for-local-development-only
set JWT_EXPIRATION=60
set GOOGLE_CLIENT_ID=your-google-client-id-for-development
set GOOGLE_CLIENT_SECRET=your-google-client-secret-for-development
```

**Linux/Mac:**

```bash
export DB_PASSWORD=Viraj@2002
export JWT_SECRET=dev-jwt-secret-key-for-local-development-only
export JWT_EXPIRATION=60
export GOOGLE_CLIENT_ID=your-google-client-id-for-development
export GOOGLE_CLIENT_SECRET=your-google-client-secret-for-development
```

## 🏃‍♂️ Running the Application

### Method 1: Using Startup Scripts (Recommended)

**Windows PowerShell:**

```powershell
# Run the automated startup script
.\start-dev.ps1
```

**Linux/Mac:**

```bash
# Make script executable (first time only)
chmod +x start-dev.sh

# Run the automated startup script
./start-dev.sh
```

### Method 2: Using Maven Wrapper (Manual)

```bash
# Start the application
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

### Method 2: Using Maven Directly

```bash
# If you have Maven installed globally
mvn spring-boot:run
```

### Method 3: Build and Run JAR

```bash
# Build the application
./mvnw clean package

# Run the JAR file
java -jar target/nexsplit-0.0.1-SNAPSHOT.jar
```

## ✅ Verification

### 1. Check Application Status

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### 2. Check Application Logs

```bash
# View logs in real-time
tail -f logs/nexsplit.log

# Or check recent logs
Get-Content logs/nexsplit.log -Tail 20  # Windows PowerShell
```

### 3. Verify Environment Variables

**Check if environment variables are loaded:**

```bash
# Windows PowerShell
echo $env:DB_PASSWORD
echo $env:JWT_SECRET

# Linux/Mac
echo $DB_PASSWORD
echo $JWT_SECRET
```

## 🔍 Troubleshooting

### Common Issues

**1. Database Connection Failed**

```
Error: Failed to load driver class org.postgresql.Driver
```

**Solution:**

- Ensure PostgreSQL is running on port 5432
- Verify database credentials in environment variables
- Check if database `nexsplit` exists

**2. Environment Variables Not Loading**

```
Error: Could not resolve placeholder 'DB_PASSWORD'
```

**Solution:**

- Verify environment variables are set correctly
- Restart the application after setting variables
- Check if using the correct shell/terminal

**3. Port Already in Use**

```
Error: Web server failed to start. Port 8080 was already in use.
```

**Solution:**

```bash
# Find process using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080  # Linux/Mac

# Kill the process
taskkill /PID <process_id> /F  # Windows
kill -9 <process_id>  # Linux/Mac
```

**4. JWT Secret Issues**

```
Error: JWT token validation failed
```

**Solution:**

- Ensure `JWT_SECRET` environment variable is set
- Use a strong, unique secret for development
- Restart application after changing JWT secret

## 📁 Project Structure

```
nexsplit/
├── src/
│   ├── main/
│   │   ├── java/com/nexsplit/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── model/           # JPA entities
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── mapper/          # Entity-DTO mappers
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.yml  # Main configuration
│   │       └── db/migration/    # Database migrations
│   └── test/                    # Test classes
├── logs/                        # Application logs
├── .env.development            # Development environment variables
├── env.development.template    # Template for environment variables
└── pom.xml                     # Maven configuration
```

## 🔄 Development Workflow

### 1. Daily Development

```bash
# 1. Start PostgreSQL (if not running as service)
# 2. Set environment variables
$env:DB_PASSWORD = "Viraj@2002"
$env:JWT_SECRET = "dev-jwt-secret-key-for-local-development-only"

# 3. Run application
./mvnw spring-boot:run

# 4. Make changes to code
# 5. Application auto-reloads (if using spring-boot:run)
```

### 2. Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### 3. Database Changes

```bash
# Create new migration
# Add SQL file to src/main/resources/db/migration/
# Format: V2__description.sql

# Run application (Flyway will apply migrations automatically)
./mvnw spring-boot:run
```

## 🛑 Stopping the Application

### Graceful Shutdown

```bash
# Press Ctrl+C in the terminal running the application
# Or send SIGTERM signal
```

### Force Stop

```bash
# Find Java process
Get-Process java | Where-Object {$_.ProcessName -eq "java"}

# Kill process
Stop-Process -Name java -Force
```

## 📊 Monitoring

### Application Metrics

```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Available actuator endpoints
curl http://localhost:8080/actuator
```

### Database Monitoring

```bash
# Connect to database
psql -h localhost -U postgres -d nexsplit

# Check tables
\dt

# Check data
SELECT * FROM users LIMIT 5;
```

## 🔐 Security Notes

### Development Security

1. **Never commit `.env.development` to Git**
2. **Use weak passwords only for development**
3. **JWT secrets should be unique per environment**
4. **OAuth2 credentials should be development-specific**

### Environment Variables FAQ

**Q: Do I need to set environment variables manually even if I have `.env.development` file?**
A: **YES** - Spring Boot doesn't automatically read `.env` files like Node.js. You have three options:

1. **Use the startup scripts** (Recommended): `.\start-dev.ps1` or `./start-dev.sh`
2. **Set manually each time**: `$env:DB_PASSWORD = "Viraj@2002"`
3. **Use a `.env` loader library**: Add `spring-dotenv` dependency (not recommended for production)

**Q: Why do we have both `.env.development` and fallback values in `application.yml`?**
A: The `.env.development` file serves as a template and documentation. The fallback values in `application.yml` ensure the application works even if environment variables aren't set, making it more robust.

**Q: Can I rename `.env.development.template` to `.env.development`?**
A: **YES** - That's exactly what you should do! The `.template` files are just examples. Rename them and set your actual values.

### Environment Variable Best Practices

1. **Use descriptive names**: `DB_PASSWORD` not `PASS`
2. **Set appropriate defaults**: Fallback values in `application.yml`
3. **Validate on startup**: Check required variables
4. **Document all variables**: Keep templates updated

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Environment Variables Best Practices](https://12factor.net/config)

## 🆘 Getting Help

If you encounter issues:

1. **Check the logs**: `logs/nexsplit.log`
2. **Verify environment variables**: Use `echo $env:VARIABLE_NAME`
3. **Test database connection**: `psql -h localhost -U postgres -d nexsplit`
4. **Check application health**: `curl http://localhost:8080/actuator/health`
5. **Review this documentation**: Check troubleshooting section

---

**Happy Coding! 🎉**
