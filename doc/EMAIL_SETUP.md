# Email Setup Guide

## Current Configuration

The email functionality is configured in both `src/main/resources/application.yml` (for local development) and `src/main/resources/application-docker.yml` (for Docker deployment) with proxy bypass settings to handle network restrictions.

### Key Features

- **Gmail SMTP** with app password authentication
- **Proxy bypass** settings to handle VPN/proxy interference
- **Retry logic** with 3 attempts and exponential backoff
- **Debug logging** enabled for troubleshooting
- **Email templates** for welcome, verification, and password reset emails

### Configuration Details

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: nexsplit.mail@gmail.com
    password: mplpwfxipmngldqz
    properties:
      # Basic SMTP settings
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
      mail.smtp.ssl.trust: smtp.gmail.com
      mail.smtp.connectiontimeout: 60000
      mail.smtp.timeout: 60000
      mail.smtp.writetimeout: 60000

      # Debug logging
      mail.debug: true
```

### Testing Email

Use the `AuthController` endpoint to test email functionality:

- `POST /api/v1/auth/test-email` - Send a test email to verify email configuration

**Example:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/test-email \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@example.com"}'
```

### Troubleshooting

If you encounter connection issues:

1. **Check logs** for detailed SMTP connection information
2. **Test the email endpoint** using the AuthController test-email endpoint
3. **Test network connectivity** to smtp.gmail.com:587
4. **Check proxy settings** - the configuration includes proxy bypass

### Environment Variables

You can override settings using environment variables:

- `MAIL_HOST` - SMTP host (default: smtp.gmail.com)
- `MAIL_PORT` - SMTP port (default: 587)
- `MAIL_USERNAME` - Email username
- `MAIL_PASSWORD` - Email password/app password

### Production Deployment

For production deployment with Docker Compose:

1. **Set environment variables** in your `.env` file:

   ```bash
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=nexsplit.mail@gmail.com
   MAIL_PASSWORD=your-gmail-app-password
   ```

2. **Deploy with dokeploy**:

   ```bash
   docker-compose -f docker-compose-dokeploy.yml up -d
   ```

3. **Test email functionality**:
   ```bash
   curl -X POST http://your-server:8080/api/v1/auth/test-email \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com"}'
   ```

### Security Notes

- Use app passwords for Gmail (not regular passwords)
- Enable 2FA on the Gmail account
- Store sensitive credentials in environment variables for production
