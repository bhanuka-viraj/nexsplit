# NexSplit Deployment Options

This document explains the different deployment configurations available for NexSplit and when to use each one.

## 🚀 **Deployment Configurations**

### 1. **Development Environment** (`docker-compose.yml`)

**Use for:** Local development and testing

**Features:**

- ✅ Local build from Dockerfile
- ✅ Elasticsearch and Kibana for monitoring
- ✅ PostgreSQL database
- ✅ Full logging and debugging capabilities

**Start with:**

```powershell
.\start-elasticsearch.ps1
```

**Access URLs:**

- App: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Kibana: http://localhost:5601
- Elasticsearch: http://localhost:9200

---

### 2. **Server Deployment** (`docker-compose.server.yml`)

**Use for:** Server deployment with pre-built DockerHub image

**Features:**

- ✅ Uses pre-built image: `bhanukaviraj/nexsplit:dev`
- ✅ Elasticsearch and Kibana for monitoring
- ✅ PostgreSQL database
- ✅ No local build required
- ✅ Faster deployment

**Start with:**

```powershell
.\start-server.ps1
```

**Access URLs:**

- App: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Kibana: http://localhost:5601
- Elasticsearch: http://localhost:9200

---

### 3. **Production Deployment** (`docker-compose.prod.yml`)

**Use for:** Production server deployment with enhanced security

**Features:**

- ✅ Uses production image: `bhanukaviraj/nexsplit:latest`
- ✅ Elasticsearch with security enabled
- ✅ Enhanced security configurations
- ✅ Production-optimized settings
- ✅ Required environment variables

**Start with:**

```powershell
docker-compose -f docker-compose.prod.yml up -d
```

**Required Environment Variables:**

```bash
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret
ELASTIC_PASSWORD=your_elasticsearch_password
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

---

## 📋 **Comparison Table**

| Feature           | Development | Server          | Production         |
| ----------------- | ----------- | --------------- | ------------------ |
| **Image Source**  | Local build | DockerHub (dev) | DockerHub (latest) |
| **Elasticsearch** | ✅          | ✅              | ✅ (Secure)        |
| **Kibana**        | ✅          | ✅              | ✅                 |
| **Security**      | Basic       | Basic           | Enhanced           |
| **Build Time**    | Slow        | Fast            | Fast               |
| **Use Case**      | Development | Testing/Staging | Production         |

---

## 🔧 **Environment Variables**

### **Development/Server (Optional)**

```bash
DB_PASSWORD=Viraj@2002
JWT_SECRET=dev-jwt-secret-key
JWT_EXPIRATION=60
GOOGLE_CLIENT_ID=google-client-id
GOOGLE_CLIENT_SECRET=google-client-secret
```

### **Production (Required)**

```bash
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret
ELASTIC_PASSWORD=your_elasticsearch_password
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

---

## 🎯 **When to Use Each Option**

### **Use Development (`docker-compose.yml`) when:**

- Working on new features
- Debugging issues
- Testing locally
- Need full control over the build process

### **Use Server (`docker-compose.server.yml`) when:**

- Deploying to a test server
- Using pre-built images
- Want faster deployment
- Don't need to modify the application code

### **Use Production (`docker-compose.prod.yml`) when:**

- Deploying to production
- Need enhanced security
- Want production-optimized settings
- Have all required environment variables

---

## 🚀 **Quick Start Commands**

### **For Development:**

```powershell
# Start with Elasticsearch monitoring
.\start-elasticsearch.ps1

# Or start app only
.\start-dev.ps1
```

### **For Server Deployment:**

```powershell
# Start with pre-built image
.\start-server.ps1
```

### **For Production:**

```powershell
# Set environment variables first, then:
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📊 **Monitoring & Logs**

All configurations include:

- **Elasticsearch**: Log aggregation and search
- **Kibana**: Log visualization and dashboards
- **Structured Logging**: Business, Security, Performance, Error events

### **View Logs:**

```bash
# Development/Server
docker-compose logs -f

# Production
docker-compose -f docker-compose.prod.yml logs -f
```

### **Access Kibana:**

- Development: http://localhost:5601
- Server: http://localhost:5601
- Production: http://localhost:5601

---

## 🔒 **Security Considerations**

### **Development/Server:**

- Basic security settings
- Elasticsearch security disabled
- Default passwords (change for production)

### **Production:**

- Enhanced security settings
- Elasticsearch security enabled
- Required secure passwords
- Production-optimized configurations

---

## 📝 **Notes**

1. **Always use production configuration for live servers**
2. **Set secure passwords in production**
3. **Monitor logs through Kibana**
4. **Use health checks to ensure services are running**
5. **Backup data volumes regularly**
