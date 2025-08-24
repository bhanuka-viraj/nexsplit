# NexSplit - Expense Tracker with Elasticsearch Monitoring

NexSplit is a comprehensive expense tracking application that can track and manage individual and shared expenses. The application includes advanced logging and monitoring capabilities using Elasticsearch and Kibana.

## Features

- **User Management**: Registration, authentication, and profile management
- **Expense Tracking**: Track individual and shared expenses
- **OAuth2 Integration**: Google OAuth2 authentication
- **JWT Security**: Secure token-based authentication

- **Structured Logging**: Comprehensive logging with correlation IDs
- **Elasticsearch Integration**: Advanced log aggregation and search
- **Kibana Dashboards**: Real-time monitoring and analytics
- **Performance Monitoring**: AOP-based performance tracking
- **Security Monitoring**: Comprehensive security event tracking

## Technology Stack

- **Backend**: Spring Boot 3.5.3 with Java 21
- **Database**: PostgreSQL 15
- **Security**: Spring Security with JWT

- **Logging**: Logback with structured logging
- **Monitoring**: Elasticsearch 8.11.0 + Kibana 8.11.0
- **Containerization**: Docker & Docker Compose
- **API Documentation**: OpenAPI 3.0 (Swagger)

## Quick Start

### Prerequisites

- Docker Desktop
- Java 21 (for local development)
- Maven 3.8+ (for local development)

### Option 1: Full Stack with Monitoring (Recommended)

Start the complete application with Elasticsearch and Kibana monitoring:

```powershell
# Windows PowerShell
.\scripts\start-elasticsearch.ps1

# Linux/Mac
./scripts/start-elasticsearch.sh

# Or manually
docker-compose up -d --build
```

### Option 2: Application Only

Start just the application and database:

```powershell
# Windows PowerShell
.\scripts\start-dev.ps1

# Linux/Mac
./scripts/start-dev.sh

# Or manually
docker-compose -f docker-compose.yml up -d postgres nexsplit-app
```

## Access URLs

Once started, you can access:

- **NexSplit Application**: http://localhost:8080
- **Swagger API Documentation**: http://localhost:8080/swagger-ui.html
- **Kibana Dashboard**: http://localhost:5601
- **Elasticsearch API**: http://localhost:9200

## Monitoring & Logging

### Elasticsearch Integration

The application automatically sends structured logs to Elasticsearch with the following indexes:

- **Business Events**: `nexsplit-logs-business-*`
- **Security Events**: `nexsplit-logs-security-*`
- **Performance Events**: `nexsplit-logs-performance-*`
- **Error Events**: `nexsplit-logs-error-*`

### Kibana Dashboards

Create monitoring dashboards in Kibana:

1. Open Kibana at http://localhost:5601
2. Create index patterns for each log type
3. Build dashboards for:
   - Security monitoring
   - Performance tracking
   - Business analytics
   - Error tracking

See `doc/KIBANA_DASHBOARDS.md` for detailed dashboard setup instructions.

### Log Categories

The application generates structured logs for:

- **Business Events**: User registrations, logins, profile updates
- **Security Events**: Authentication failures, suspicious activity
- **Performance Events**: Method execution times, slow queries
- **Error Events**: Exceptions and system failures

## Development

### Local Development

```bash
# Clone the repository
git clone <repository-url>
cd nexsplit

# Set up environment variables
cp env.development.template .env.development
# Edit .env.development with your configuration

# Run with scripts (recommended)
# Windows
.\scripts\start-dev.ps1

# Linux/Mac
./scripts/start-dev.sh

# Or run with Maven directly
./mvnw spring-boot:run
```

### Docker Development

```bash
# Start development environment with monitoring
# Windows
.\scripts\start-elasticsearch.ps1

# Linux/Mac
./scripts/start-elasticsearch.sh

# Or manually
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Configuration

### Environment Variables

Key environment variables:

- `DB_PASSWORD`: PostgreSQL password
- `JWT_SECRET`: JWT signing secret
- `GOOGLE_CLIENT_ID`: Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth2 client secret

### Logging Configuration

Logging is configured via `logback-spring.xml` with:

- Structured JSON logging for Elasticsearch
- Correlation ID tracking
- Security-aware data masking
- Performance monitoring

## API Documentation

The API is documented using OpenAPI 3.0 and available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Security Features

- JWT-based authentication
- OAuth2 integration with Google
- Password hashing with BCrypt
- CORS configuration
- Request correlation tracking
- Security event logging

## Monitoring Features

- Real-time log aggregation
- Performance metrics tracking
- Security event monitoring
- Business analytics
- Error tracking and alerting
- Request tracing with correlation IDs

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure ports 5432, 8080, 9200, 5601 are available
2. **Docker Issues**: Make sure Docker Desktop is running
3. **Elasticsearch Health**: Check `curl http://localhost:9200/_cluster/health`
4. **Log Issues**: Check application logs in `logs/` directory

### Useful Commands

```bash
# Check service health
docker-compose ps

# View logs
docker-compose logs -f

# Restart services
docker-compose restart

# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# Check Kibana status
curl http://localhost:5601/api/status

# Production deployment
# Windows
.\scripts\deploy-production.ps1 v1.0.0

# Linux/Mac
./scripts/deploy-production.sh v1.0.0

# Build and push Docker image
./scripts/build-and-push.sh v1.0.0
```

## Documentation

- [Scripts Documentation](scripts/README.md) - Complete guide to all automation scripts
- [Logging Guidelines](LOGGING_GUIDELINES.md)
- [Kibana Dashboard Setup](doc/KIBANA_DASHBOARDS.md)
- [Deployment Guide](doc/DEPLOYMENT_GUIDE.md)
- [Security Guidelines](doc/SECURITY.md)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
