# 📧 Email System Implementation Plan

## 🎯 Overview

This document outlines the comprehensive plan for implementing email functionality in the NexSplit expense tracker application, specifically for:

- **Password Reset Emails**
- **Email Confirmation/Verification**
- **Welcome Emails** (optional)

## 🏗️ Architecture Overview

### **Email Service Architecture**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controllers   │───▶│  Email Service   │───▶│ Email Provider  │
│                 │    │                  │    │ (SMTP/API)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │  Email Templates │
                       │  (Thymeleaf)     │
                       └──────────────────┘
```

## 📋 Implementation Plan

### **Phase 1: Core Email Infrastructure**

#### **1.1 Dependencies & Configuration**

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

#### **1.2 Email Configuration Properties**

```yaml
# application.yml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
    test-connection: false

app:
  email:
    from: ${MAIL_FROM:noreply@nexsplit.com}
    from-name: ${MAIL_FROM_NAME:NexSplit}
    base-url: ${APP_BASE_URL:http://localhost:8080}
    templates:
      password-reset: password-reset
      email-confirmation: email-confirmation
      welcome: welcome
```

#### **1.3 Email Service Interface**

```java
public interface EmailService {
    void sendPasswordResetEmail(String to, String resetToken, String username);
    void sendEmailConfirmation(String to, String confirmationToken, String username);
    void sendWelcomeEmail(String to, String username);
    void sendEmail(String to, String subject, String htmlContent);
}
```

#### **1.4 Email Template Service**

```java
public interface EmailTemplateService {
    String generatePasswordResetEmail(String username, String resetUrl);
    String generateEmailConfirmationEmail(String username, String confirmationUrl);
    String generateWelcomeEmail(String username);
}
```

### **Phase 2: Email Templates**

#### **2.1 Thymeleaf Template Structure**

```
src/main/resources/templates/email/
├── password-reset.html
├── email-confirmation.html
├── welcome.html
└── base-email.html (layout template)
```

#### **2.2 Template Features**

- **Responsive Design**: Mobile-friendly emails
- **Brand Consistency**: NexSplit branding and colors
- **Security Warnings**: Clear expiration notices
- **Action Buttons**: Prominent CTA buttons
- **Fallback Text**: Plain text alternatives

### **Phase 3: Email Service Implementation**

#### **3.1 Core Email Service**

```java
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    // Implementation with:
    // - Async email sending
    // - Retry mechanism
    // - Error handling
    // - Rate limiting
    // - Email queuing
}
```

#### **3.2 Email Template Service**

```java
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {
    // Implementation with:
    // - Thymeleaf template processing
    // - Dynamic content generation
    // - Template caching
}
```

### **Phase 4: Integration Points**

#### **4.1 User Registration Flow**

```java
// In UserService.registerUser()
1. Create user with isEmailValidated = false
2. Generate email confirmation token
3. Send confirmation email
4. Return success response
```

#### **4.2 Password Reset Flow**

```java
// In UserService.requestPasswordReset()
1. Validate email exists
2. Generate secure reset token
3. Store token with expiration
4. Send password reset email
5. Return success response
```

#### **4.3 Email Confirmation Flow**

```java
// New endpoint: POST /api/v1/auth/confirm-email
1. Validate confirmation token
2. Mark email as verified
3. Update user status
4. Send welcome email (optional)
```

## 🔧 Technical Implementation Details

### **Email Token Management**

#### **Token Generation**

```java
public class EmailTokenService {
    public String generateSecureToken() {
        // Generate cryptographically secure token
        // Store with expiration and user association
    }

    public boolean validateToken(String token, TokenType type) {
        // Validate token exists, not expired, not used
    }
}
```

#### **Token Storage Strategy**

```sql
-- New table: email_tokens
CREATE TABLE email_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    token_type VARCHAR(50) NOT NULL, -- 'PASSWORD_RESET', 'EMAIL_CONFIRMATION'
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(token_hash)
);
```

### **Email Security Features**

#### **Rate Limiting**

```java
@Component
public class EmailRateLimiter {
    // Implement rate limiting per email/IP
    // Prevent email bombing attacks
    // Track and block suspicious activity
}
```

#### **Token Security**

- **Cryptographic Tokens**: Use secure random generation
- **Short Expiration**: 15-30 minutes for reset tokens
- **Single Use**: Tokens invalidated after use
- **Hash Storage**: Store token hashes, not plain tokens

### **Email Provider Options**

#### **Primary Options**

1. **Gmail SMTP** (Development/Testing)
2. **SendGrid** (Production - Recommended)
3. **Amazon SES** (Production - Cost-effective)
4. **Mailgun** (Production - Developer-friendly)

#### **Configuration Strategy**

```yaml
# Environment-specific configurations
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
spring:
  config:
    activate:
      on-profile: dev
  mail:
    host: smtp.gmail.com
    # Development settings
---
spring:
  config:
    activate:
      on-profile: prod
  mail:
    host: smtp.sendgrid.net
    # Production settings
```

## 📧 Email Templates Design

### **Password Reset Email Template**

```html
<!-- password-reset.html -->
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Reset Your Password - NexSplit</title>
  </head>
  <body>
    <div class="email-container">
      <div class="header">
        <img src="logo.png" alt="NexSplit Logo" />
        <h1>Reset Your Password</h1>
      </div>

      <div class="content">
        <p>Hello <strong th:text="${username}">User</strong>,</p>

        <p>
          We received a request to reset your password. Click the button below
          to create a new password:
        </p>

        <div class="button-container">
          <a href th:href="${resetUrl}" class="reset-button">
            Reset Password
          </a>
        </div>

        <p class="warning">
          ⚠️ This link will expire in 30 minutes for security reasons.
        </p>

        <p>
          If you didn't request this password reset, please ignore this email.
        </p>
      </div>

      <div class="footer">
        <p>Best regards,<br />The NexSplit Team</p>
      </div>
    </div>
  </body>
</html>
```

### **Email Confirmation Template**

```html
<!-- email-confirmation.html -->
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Confirm Your Email - NexSplit</title>
  </head>
  <body>
    <div class="email-container">
      <div class="header">
        <img src="logo.png" alt="NexSplit Logo" />
        <h1>Welcome to NexSplit!</h1>
      </div>

      <div class="content">
        <p>Hello <strong th:text="${username}">User</strong>,</p>

        <p>
          Thank you for signing up for NexSplit! Please confirm your email
          address to complete your registration:
        </p>

        <div class="button-container">
          <a href th:href="${confirmationUrl}" class="confirm-button">
            Confirm Email Address
          </a>
        </div>

        <p class="info">This link will expire in 24 hours.</p>
      </div>

      <div class="footer">
        <p>Welcome aboard!<br />The NexSplit Team</p>
      </div>
    </div>
  </body>
</html>
```

## 🔄 Integration with Existing Code

### **User Registration Enhancement**

```java
// Modify UserService.registerUser()
public UserProfileDto registerUser(UserDto userDto) {
    // Existing validation logic...

    User user = User.builder()
        .email(userDto.getEmail())
        .passwordHash(passwordEncoder.encode(userDto.getPassword()))
        .firstName(userDto.getFirstName())
        .lastName(userDto.getLastName())
        .username(userDto.getUsername())
        .contactNumber(userDto.getContactNumber())
        .isEmailValidated(false) // New field
        .status(UserStatus.ACTIVE)
        .build();

    User savedUser = userRepository.save(user);

    // Generate and send confirmation email
    String confirmationToken = emailTokenService.generateToken(savedUser.getId(), TokenType.EMAIL_CONFIRMATION);
    emailService.sendEmailConfirmation(savedUser.getEmail(), confirmationToken, savedUser.getUsername());

    return userMapper.toProfileDto(savedUser);
}
```

### **Password Reset Enhancement**

```java
// Modify UserService.requestPasswordReset()
public void requestPasswordReset(String email) {
    User user = userRepository.findActiveUserByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User not found"));

    // Generate secure reset token
    String resetToken = emailTokenService.generateToken(user.getId(), TokenType.PASSWORD_RESET);

    // Send password reset email
    emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getUsername());

    log.info("Password reset email sent to: {}", email);
}
```

## 🚀 Deployment Considerations

### **Environment Variables**

```bash
# Production Environment Variables
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
MAIL_FROM=noreply@nexsplit.com
MAIL_FROM_NAME=NexSplit
APP_BASE_URL=https://nexsplit.com
```

### **Docker Configuration**

```yaml
# docker-compose.prod.yml
environment:
  - MAIL_HOST=${MAIL_HOST}
  - MAIL_PORT=${MAIL_PORT}
  - MAIL_USERNAME=${MAIL_USERNAME}
  - MAIL_PASSWORD=${MAIL_PASSWORD}
  - MAIL_FROM=${MAIL_FROM}
  - MAIL_FROM_NAME=${MAIL_FROM_NAME}
  - APP_BASE_URL=${APP_BASE_URL}
```

### **Monitoring & Logging**

```java
// Email service monitoring
@Component
public class EmailMetrics {
    // Track email success/failure rates
    // Monitor delivery times
    // Alert on high failure rates
    // Log email events for debugging
}
```

## 🔒 Security Best Practices

### **Email Security**

1. **SPF/DKIM/DMARC**: Configure email authentication
2. **Rate Limiting**: Prevent email abuse
3. **Token Security**: Secure token generation and storage
4. **HTTPS Links**: All email links use HTTPS
5. **Unsubscribe**: Include unsubscribe links (for marketing emails)

### **Data Protection**

1. **GDPR Compliance**: Clear privacy notices
2. **Data Minimization**: Only collect necessary data
3. **Secure Storage**: Encrypt sensitive data
4. **Audit Logging**: Track email activities

## 📊 Testing Strategy

### **Unit Tests**

```java
@Test
void testPasswordResetEmailGeneration() {
    // Test email template generation
    // Test token validation
    // Test email service methods
}
```

### **Integration Tests**

```java
@Test
void testPasswordResetFlow() {
    // Test complete password reset flow
    // Test email sending (with test provider)
    // Test token expiration
}
```

### **Email Testing**

1. **Email Provider Testing**: Test with real email providers
2. **Template Testing**: Test across different email clients
3. **Mobile Testing**: Test responsive design
4. **Spam Testing**: Ensure emails don't go to spam

## 📈 Future Enhancements

### **Advanced Features**

1. **Email Preferences**: User email preference management
2. **Email Templates**: Admin-configurable templates
3. **Email Analytics**: Track open rates, click rates
4. **Transactional Emails**: Order confirmations, receipts
5. **Marketing Emails**: Newsletter, promotions

### **Scalability**

1. **Email Queuing**: Redis-based email queue
2. **Multiple Providers**: Fallback email providers
3. **Email Scheduling**: Scheduled email sending
4. **Template Versioning**: A/B testing for templates

## 🎯 Implementation Priority

### **Phase 1 (High Priority)**

1. Core email infrastructure
2. Password reset emails
3. Basic email templates
4. Security implementation

### **Phase 2 (Medium Priority)**

1. Email confirmation flow
2. Welcome emails
3. Advanced templates
4. Monitoring and logging

### **Phase 3 (Low Priority)**

1. Email preferences
2. Analytics
3. Advanced features
4. Marketing emails

## 📝 Success Metrics

### **Technical Metrics**

- Email delivery rate > 99%
- Email open rate > 20%
- Password reset completion rate > 80%
- Email confirmation rate > 90%

### **User Experience Metrics**

- Time to reset password < 5 minutes
- Email confirmation completion < 24 hours
- User satisfaction with email flow > 4.5/5

This comprehensive plan ensures a robust, secure, and scalable email system that follows industry best practices and integrates seamlessly with the existing NexSplit application.
