# CORS Configuration Guide

## Overview

The CORS (Cross-Origin Resource Sharing) configuration has been moved from hardcoded values in `ApiConfig.java` to configurable settings in `application.yml`, `application-docker.yml`, and environment variables.

## Configuration Options

### 1. Application Configuration Files

#### `application.yml` (Local Development)

```yaml
cors:
  allow-credentials: true
  allowed-origins: http://localhost:3000,http://127.0.0.1:3000
  allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
  allowed-headers: Authorization,Origin,Content-Type,Accept,Accept-Encoding,Accept-Language,Access-Control-Allow-Origin,Access-Control-Allow-Headers,Access-Control-Request-Method,X-Requested-With,X-Auth-Token,X-Xsrf-Token,Cache-Control,Id-Token,X-Correlation-ID
```

#### `application-docker.yml` (Docker/Production)

```yaml
cors:
  allow-credentials: true
  allowed-origins: http://localhost:3000,http://127.0.0.1:3000,http://localhost:8080,http://127.0.0.1:8080,http://localhost:8081,http://127.0.0.1:8081,http://localhost:8082,http://127.0.0.1:8082
  allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
  allowed-headers: Authorization,Origin,Content-Type,Accept,Accept-Encoding,Accept-Language,Access-Control-Allow-Origin,Access-Control-Allow-Headers,Access-Control-Request-Method,X-Requested-With,X-Auth-Token,X-Xsrf-Token,Cache-Control,Id-Token,X-Correlation-ID
```

### 2. Environment Variables

You can override CORS settings using environment variables:

```bash
# Set allowed origins (comma-separated)
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://app.yourdomain.com"

# Set allow credentials
export CORS_ALLOW_CREDENTIALS="true"

# Set allowed methods (comma-separated)
export CORS_ALLOWED_METHODS="GET,POST,PUT,DELETE,OPTIONS"

# Set allowed headers (comma-separated)
export CORS_ALLOWED_HEADERS="Authorization,Content-Type,X-Requested-With"
```

### 3. Docker Compose Environment Variables

In `docker-compose-dokeploy.yml`:

```yaml
environment:
  CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-http://localhost:3000,http://127.0.0.1:3000,http://localhost:8080,http://127.0.0.1:8080,http://localhost:8081,http://127.0.0.1:8081,http://localhost:8082,http://127.0.0.1:8082}
  CORS_ALLOW_CREDENTIALS: ${CORS_ALLOW_CREDENTIALS:-true}
  CORS_ALLOWED_METHODS: ${CORS_ALLOWED_METHODS:-GET,POST,PUT,PATCH,DELETE,OPTIONS}
  CORS_ALLOWED_HEADERS: ${CORS_ALLOWED_HEADERS:-Authorization,Origin,Content-Type,Accept,Accept-Encoding,Accept-Language,Access-Control-Allow-Origin,Access-Control-Allow-Headers,Access-Control-Request-Method,X-Requested-With,X-Auth-Token,X-Xsrf-Token,Cache-Control,Id-Token,X-Correlation-ID}
```

## Configuration Priority

The configuration is resolved in the following order (highest to lowest priority):

1. **Environment Variables** (e.g., `CORS_ALLOWED_ORIGINS`)
2. **Application Configuration Files** (e.g., `cors.allowed-origins`)
3. **Default Values** (hardcoded fallbacks)

## Usage Examples

### Development Environment

```bash
# Use default localhost origins
./mvnw spring-boot:run
```

### Production Environment

```bash
# Set production origins via environment variables
export CORS_ALLOWED_ORIGINS="https://nexsplit.com,https://app.nexsplit.com"
docker-compose -f docker-compose-dokeploy.yml up -d
```

### Docker with Custom Origins

```bash
# Override origins for specific deployment
CORS_ALLOWED_ORIGINS="https://staging.nexsplit.com" docker-compose -f docker-compose-dokeploy.yml up -d
```

## Security Considerations

1. **Never use `*` for allowed origins** in production
2. **Always specify exact domains** that need access
3. **Use HTTPS** for production origins
4. **Consider using `allow-credentials: false`** if cookies aren't needed
5. **Limit allowed methods** to only what's necessary

## Troubleshooting

### Common Issues

1. **CORS errors in browser console**

   - Check that the requesting origin is in `allowed-origins`
   - Verify the origin format (include protocol, domain, and port)

2. **Credentials not being sent**

   - Ensure `allow-credentials` is set to `true`
   - Check that the frontend includes `credentials: 'include'` in fetch requests

3. **Headers being blocked**
   - Verify all required headers are in `allowed-headers`
   - Check for typos in header names

### Debug Configuration

To see the current CORS configuration, check the application logs:

```bash
docker logs nexsplit-app | grep -i cors
```

## Migration from Hardcoded Values

The old hardcoded CORS configuration in `ApiConfig.java` has been replaced with configurable settings. The application will automatically use the new configuration system with backward compatibility through default values.
