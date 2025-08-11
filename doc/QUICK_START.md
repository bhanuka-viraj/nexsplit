# ⚡ Quick Start Guide

## 🚀 Get Running in 5 Minutes

### Prerequisites Check

- ✅ Java 21 installed
- ✅ PostgreSQL running on port 5432
- ✅ Git repository cloned

### Step 1: Setup Environment

```bash
# Copy environment template
cp env.development.template .env.development

# Edit with your values (optional - defaults will work)
notepad .env.development
```

### Step 2: Start Application

```bash
# Windows
.\start-dev.ps1

# Linux/Mac
chmod +x start-dev.sh
./start-dev.sh
```

### Step 3: Verify

```bash
# Check if application is running
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

## 🛠️ Manual Setup (Alternative)

If you prefer manual setup:

```bash
# Set environment variables
$env:DB_PASSWORD = "Viraj@2002"
$env:JWT_SECRET = "dev-jwt-secret-key-for-local-development-only"

# Start application
./mvnw spring-boot:run
```

## 📚 Next Steps

- Read `LOCAL_DEVELOPMENT.md` for detailed instructions
- Read `DOCKER.md` for Docker setup
- Read `SECURITY.md` for security best practices

## 🆘 Troubleshooting

**Application won't start?**

- Check if PostgreSQL is running: `psql -h localhost -U postgres -d nexsplit`
- Check Java version: `java -version`
- Check logs: `Get-Content logs/nexsplit.log -Tail 20`

**Environment variables not working?**

- Use the startup scripts: `.\start-dev.ps1`
- Or set manually: `$env:DB_PASSWORD = "Viraj@2002"`

**Database connection failed?**

- Ensure PostgreSQL is running on port 5432
- Check database exists: `createdb -U postgres nexsplit`

---

**Need more help?** Check `LOCAL_DEVELOPMENT.md` for comprehensive documentation.
