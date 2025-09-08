# Expense Tracking System Architecture

## Table of Contents

1. [System Overview](#system-overview)
2. [Database Schema](#database-schema)
3. [API Endpoints](#api-endpoints)
4. [Settlement Processing](#settlement-processing)
5. [Settlement Algorithms](#settlement-algorithms)
6. [Real-Time Updates](#real-time-updates)
7. [Implementation Architecture](#implementation-architecture)
8. [Response Consistency Mechanism](#response-consistency-mechanism)
9. [Performance & Monitoring](#performance--monitoring)
10. [Testing Strategy](#testing-strategy)
11. [Deployment](#deployment)

## Overview

This document outlines the complete architecture for the NexSplit expense tracking system, including database design, API endpoints, settlement algorithms, and implementation strategies.

## System Architecture

### **🏗️ High-Level Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Mobile App    │    │   Third Party   │
│ (React/Next.js) │    │   (React Native)│    │   Integrations  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │    API Gateway            │
                    │   (Load Balancer)         │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │   Spring Boot API         │
                    │   (NexSplit Backend)      │
                    └─────────────┬─────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
┌─────────▼─────────┐  ┌─────────▼─────────┐  ┌─────────▼─────────┐
│   PostgreSQL      │  │   Elasticsearch   │  │   File Storage..  │
│   (Main Database) │  │   (Logging)       │  │   (Attachments)   │
└───────────────────┘  └───────────────────┘  └───────────────────┘
```

### **🔧 Technology Stack**

- **Backend**: Spring Boot 3.5.3 with Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: JWT with OAuth2 (Google)
- **Async Processing**: Virtual Threads
- **HTTP Client**: RestClient (Spring 6.1+)
- **Logging**: Structured logging with Elasticsearch
- **Documentation**: OpenAPI 3.0 (Swagger)

## Database Schema

### **📊 Core Tables**

#### **1. Users Table**

```sql
CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    username VARCHAR(255) NOT NULL UNIQUE,
    contact_number VARCHAR(20),
    last_validation_code INTEGER DEFAULT 0,
    is_email_validate BOOLEAN DEFAULT FALSE,
    is_google_auth BOOLEAN DEFAULT FALSE,
    status TEXT CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
```

#### **2. Nex (Expense Groups) Table**

```sql
CREATE TABLE nex (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image_url VARCHAR(255),
    created_by CHAR(36) NOT NULL,
    settlement_type TEXT CHECK (settlement_type IN ('DETAILED', 'SIMPLIFIED')) NOT NULL DEFAULT 'DETAILED',
    is_archived BOOLEAN DEFAULT FALSE,
    nex_type TEXT CHECK (nex_type IN ('PERSONAL', 'GROUP')) DEFAULT 'GROUP',
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_nex_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);
```

#### **3. Nex Members Table**

```sql
CREATE TABLE nex_members (
    nex_id CHAR(36),
    user_id CHAR(36),
    role TEXT CHECK (role IN ('ADMIN', 'MEMBER')) NOT NULL,
    invited_at TIMESTAMP,
    joined_at TIMESTAMP,
    status TEXT CHECK (status IN ('PENDING', 'ACTIVE', 'LEFT')) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    PRIMARY KEY (nex_id, user_id),

    CONSTRAINT fk_nex_members_nex FOREIGN KEY (nex_id) REFERENCES nex(id),
    CONSTRAINT fk_nex_members_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### **4. Categories Table**

```sql
CREATE TABLE categories (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by CHAR(36) NOT NULL,
    nex_id CHAR(36),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_categories_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_categories_nex FOREIGN KEY (nex_id) REFERENCES nex(id)
);
```

#### **5. Expenses Table**

```sql
CREATE TABLE expenses (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    category_id CHAR(36) NOT NULL,
    description TEXT,
    nex_id CHAR(36) NOT NULL,
    created_by CHAR(36) NOT NULL,
    payer_id CHAR(36) NOT NULL,
    split_type TEXT CHECK (split_type IN ('PERCENTAGE', 'AMOUNT', 'EQUALLY')) NOT NULL DEFAULT 'EQUALLY',
    is_initial_payer_has BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_expenses_nex FOREIGN KEY (nex_id) REFERENCES nex(id),
    CONSTRAINT fk_expenses_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_expenses_payer FOREIGN KEY (payer_id) REFERENCES users(id)
);
```

#### **6. Splits Table**

```sql
CREATE TABLE splits (
    expense_id CHAR(36),
    user_id CHAR(36),
    percentage DECIMAL(5,2),
    amount DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    PRIMARY KEY (expense_id, user_id),

    CONSTRAINT fk_splits_expense FOREIGN KEY (expense_id) REFERENCES expenses(id),
    CONSTRAINT fk_splits_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### **7. Debts Table**

```sql
CREATE TABLE debts (
    id CHAR(36) PRIMARY KEY,
    debtor_id CHAR(36) NOT NULL,
    creditor_id CHAR(36) NOT NULL,
    creditor_type TEXT CHECK (creditor_type IN ('USER', 'EXPENSE')) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    expense_id CHAR(36) NOT NULL,
    payment_method VARCHAR(50),
    notes TEXT,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_debts_debtor FOREIGN KEY (debtor_id) REFERENCES users(id),
    CONSTRAINT fk_debts_creditor_user FOREIGN KEY (creditor_id) REFERENCES users(id),
    CONSTRAINT fk_debts_expense FOREIGN KEY (expense_id) REFERENCES expenses(id)
);
```

#### **8. Bills Table**

```sql
CREATE TABLE bills (
    id CHAR(36) PRIMARY KEY,
    nex_id CHAR(36) NOT NULL,
    created_by CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    due_date TIMESTAMP NOT NULL,
    frequency TEXT CHECK (frequency IN ('ONCE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')) NOT NULL,
    next_due_date TIMESTAMP,
    is_recurring BOOLEAN DEFAULT FALSE,
    is_paid BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_bills_nex FOREIGN KEY (nex_id) REFERENCES nex(id),
    CONSTRAINT fk_bills_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);
```

#### **9. Attachments Table**

```sql
CREATE TABLE attachments (
    id CHAR(36) PRIMARY KEY,
    expense_id CHAR(36) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    uploaded_by CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_attachments_expense FOREIGN KEY (expense_id) REFERENCES expenses(id),
    CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by) REFERENCES users(id)
);
```

#### **10. Notifications Table**

```sql
CREATE TABLE notifications (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    nex_id CHAR(36),
    type TEXT CHECK (type IN ('INVITE', 'REMINDER', 'INFO')),
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_nex FOREIGN KEY (nex_id) REFERENCES nex(id)
);
```

## API Endpoints

### **🔐 Authentication Endpoints**

```
POST   /api/v1/auth/register                    - User registration
POST   /api/v1/auth/login                       - User login
POST   /api/v1/auth/oauth2/verify               - OAuth2 token verification
POST   /api/v1/auth/refresh                     - Refresh access token (cookie)
POST   /api/v1/auth/refresh-token               - Refresh access token (body)
POST   /api/v1/auth/logout                      - User logout
POST   /api/v1/auth/verify-email                - Email verification
POST   /api/v1/auth/resend-email-verification   - Resend verification email
```

### **👤 User Management Endpoints**

```
GET    /api/v1/users/profile                    - Get user profile
PUT    /api/v1/users/profile                    - Update user profile
POST   /api/v1/users/change-password            - Change password
POST   /api/v1/users/request-password-reset     - Request password reset
POST   /api/v1/users/reset-password             - Reset password
DELETE /api/v1/users/deactivate                 - Deactivate account
GET    /api/v1/users/validate/email             - Validate email availability
GET    /api/v1/users/validate/username          - Validate username availability
POST   /api/v1/users/validate/password          - Validate password strength
```

### **📊 Nex (Expense Groups) Endpoints**

```
POST   /api/v1/nex                              - Create expense group
GET    /api/v1/nex                              - List user's expense groups
GET    /api/v1/nex/{nexId}                      - Get expense group details
PUT    /api/v1/nex/{nexId}                      - Update expense group
DELETE /api/v1/nex/{nexId}                      - Archive expense group
POST   /api/v1/nex/{nexId}/invite               - Invite user to group
PUT    /api/v1/nex/{nexId}/members/{userId}     - Update member role
DELETE /api/v1/nex/{nexId}/members/{userId}     - Remove member
GET    /api/v1/nex/{nexId}/members              - List group members
```

### **🏷️ Categories Endpoints**

```
POST   /api/v1/categories                       - Create category
GET    /api/v1/categories                       - List categories (global + nex-specific)
PUT    /api/v1/categories/{categoryId}          - Update category
DELETE /api/v1/categories/{categoryId}          - Delete category
GET    /api/v1/nex/{nexId}/categories           - List categories for specific nex
```

### **🏷️ Category Management System**

#### **Category Types & Architecture**

The system supports **3 types of categories** with different access patterns:

| Category Type           | `is_default` | `nex_id` | `created_by` | Access Level          |
| ----------------------- | ------------ | -------- | ------------ | --------------------- |
| **Default Categories**  | `true`       | `NULL`   | System UUID  | All users (read-only) |
| **Personal Categories** | `false`      | `NULL`   | User UUID    | Creator only          |
| **Nex Categories**      | `false`      | Nex UUID | User UUID    | Nex members           |

#### **Default Categories (`is_default = true`)**

**Purpose:**

- 🌍 **Global availability** - Available to ALL users in the system
- 🛡️ **Read-only access** - Users cannot modify or delete them
- 🏗️ **System-managed** - Created by the system, not individual users
- 📊 **Consistent naming** - Same categories available across all users

**Benefits:**

- ✅ **Immediate availability** - New users get 42 pre-created categories instantly
- ✅ **No setup required** - No need to create basic categories like "Food", "Transportation"
- ✅ **Standardization** - Consistent category names across all users
- ✅ **User experience** - Good starting point for expense organization

**Default Categories Available (42 total):**

**Food & Dining (4):** Food & Dining, Restaurants, Groceries, Coffee & Drinks
**Transportation (5):** Transportation, Fuel, Public Transport, Parking, Ride Sharing
**Entertainment (4):** Entertainment, Movies & Shows, Sports & Recreation, Gaming
**Shopping (4):** Shopping, Clothing, Electronics, Home & Garden
**Bills & Utilities (5):** Bills & Utilities, Electricity, Water, Internet & Phone, Rent
**Health & Medical (4):** Health & Medical, Doctor Visits, Medications, Fitness & Wellness
**Education (4):** Education, Tuition, Books & Supplies, Online Courses
**Travel (4):** Travel, Flights, Hotels, Vacation Activities
**Business (4):** Business, Office Supplies, Business Meals, Professional Development
**Miscellaneous (4):** Miscellaneous, Gifts, Charity & Donations, Pet Expenses

#### **Personal Categories (`is_default = false`, `nex_id = NULL`)**

**Purpose:**

- 👤 **User-specific** - Created by individual users for their personal use
- 🔒 **Private access** - Only visible to the creator
- ✏️ **Full CRUD** - Creator can create, read, update, delete
- 🎯 **Customization** - Users can create categories that match their specific needs

**Use Cases:**

- Personal expense tracking outside of group activities
- Custom categories for specific spending patterns
- Categories that don't fit standard default categories

#### **Nex Categories (`is_default = false`, `nex_id = NOT NULL`)**

**Purpose:**

- 👥 **Group-specific** - Shared within a specific expense group (nex)
- 🔐 **Member access** - All nex members can view and use
- ✏️ **Creator control** - Only the creator can modify/delete
- 🎯 **Group customization** - Categories specific to group activities

**Use Cases:**

- Group-specific expense categories (e.g., "Team Lunch", "Project Supplies")
- Categories relevant to the group's activities
- Shared categories for collaborative expense tracking

#### **Authorization & Security Matrix**

| Operation  | Default Categories | Personal Categories | Nex Categories  |
| ---------- | ------------------ | ------------------- | --------------- |
| **Read**   | ✅ All users       | ✅ Creator only     | ✅ Nex members  |
| **Create** | ❌ System only     | ✅ Creator          | ✅ Nex members  |
| **Update** | ❌ System only     | ✅ Creator only     | ✅ Creator only |
| **Delete** | ❌ System only     | ✅ Creator only     | ✅ Creator only |

#### **API Endpoints Details**

**Create Categories:**

```http
POST /api/v1/categories
Content-Type: application/json
Authorization: Bearer <token>

# Personal Category
{
  "name": "My Personal Category"
}

# Nex Category
{
  "name": "Group Category",
  "nexId": "nex-uuid-here"
}
```

**List Categories:**

```http
# All user categories (personal + default)
GET /api/v1/categories

# Personal categories only
GET /api/v1/categories/personal

# Default categories only
GET /api/v1/categories/default

# Nex-specific categories
GET /api/v1/nex/{nexId}/categories

# Paginated categories
GET /api/v1/categories/paginated?page=0&size=10
```

**Category Management:**

```http
# Get category details
GET /api/v1/categories/{categoryId}

# Update category (creator only)
PUT /api/v1/categories/{categoryId}
{
  "name": "Updated Category Name"
}

# Delete category (creator only, no expenses)
DELETE /api/v1/categories/{categoryId}
```

#### **Business Rules & Validation**

**Category Creation:**

- ✅ **Name uniqueness** - Within user's personal categories or within a nex
- ✅ **Required fields** - Name is mandatory
- ✅ **Length validation** - Name must be 1-255 characters
- ✅ **Authorization** - User must be nex member for group categories

**Category Updates:**

- ✅ **Creator only** - Only the creator can modify categories
- ✅ **Name uniqueness** - Check for conflicts when changing names
- ✅ **Default protection** - Default categories cannot be modified

**Category Deletion:**

- ✅ **Creator only** - Only the creator can delete categories
- ✅ **Expense check** - Cannot delete if category has existing expenses
- ✅ **Default protection** - Default categories cannot be deleted
- ✅ **Soft delete** - Categories are soft deleted (marked as deleted, not physically removed)

#### **Database Schema Details**

```sql
CREATE TABLE categories (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by CHAR(36) NOT NULL,
    nex_id CHAR(36),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_categories_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_categories_nex FOREIGN KEY (nex_id) REFERENCES nex(id)
);
```

**Key Fields:**

- `is_default` - Distinguishes system-generated vs user-generated categories
- `nex_id` - Links category to specific expense group (NULL for personal/default)
- `created_by` - Tracks who created the category for authorization

#### **Implementation Benefits**

**For Users:**

- 🚀 **Quick start** - 42 default categories available immediately
- 🎯 **Flexibility** - Create custom categories as needed
- 🔒 **Privacy** - Personal categories remain private
- 👥 **Collaboration** - Share categories within groups

**For System:**

- 📊 **Analytics** - Track category usage patterns
- 🔄 **Updates** - Add new default categories in future migrations
- 🛡️ **Security** - Proper authorization and access control
- 📈 **Scalability** - Efficient queries with proper indexing

**For Development:**

- 🧪 **Testing** - Comprehensive unit tests for all operations
- 📚 **Documentation** - OpenAPI documentation for all endpoints
- 🔍 **Monitoring** - Structured logging for business events
- 🚀 **Performance** - Optimized queries with proper database design

### **💰 Expenses Endpoints**

```
POST   /api/v1/expenses                         - Create expense
GET    /api/v1/expenses                         - List expenses (with filters)
GET    /api/v1/expenses/{expenseId}             - Get expense details
PUT    /api/v1/expenses/{expenseId}             - Update expense
DELETE /api/v1/expenses/{expenseId}             - Delete expense
POST   /api/v1/expenses/{expenseId}/splits      - Update expense splits
GET    /api/v1/nex/{nexId}/expenses             - List expenses for specific nex
GET    /api/v1/nex/{nexId}/expenses/summary     - Get expense summary
```

### **💸 Debts & Settlements Endpoints**

```
# Debt Management
GET    /api/v1/debts                            - List user's debts
GET    /api/v1/debts/summary                    - Get debt summary
POST   /api/v1/debts/{debtId}/settle            - Mark debt as settled

# Nex-Specific Debts
GET    /api/v1/nex/{nexId}/debts                - List debts for specific nex

# Settlement Management
GET    /api/v1/nex/{nexId}/settlements          - Get settlement summary
POST   /api/v1/nex/{nexId}/settlements/execute  - Execute settlements
GET    /api/v1/nex/{nexId}/settlements/available - Get available settlements for execution

# Settlement History (Database Views)
GET    /api/v1/nex/{nexId}/settlements/history  - Get settlement history (paginated)
GET    /api/v1/nex/{nexId}/settlements/history/user/{userId} - Get user-specific settlement history
GET    /api/v1/nex/{nexId}/settlements/analytics - Get settlement analytics

# User-Specific Settlements
GET    /api/v1/users/{userId}/settlements       - User's settlements across all nex
```

### **📋 Bills Endpoints**

```
POST   /api/v1/bills                            - Create bill
GET    /api/v1/bills                            - List bills
GET    /api/v1/bills/{billId}                   - Get bill details
PUT    /api/v1/bills/{billId}                   - Update bill
DELETE /api/v1/bills/{billId}                   - Delete bill
POST   /api/v1/bills/{billId}/pay               - Mark bill as paid
GET    /api/v1/nex/{nexId}/bills                - List bills for specific nex
```

### **📎 Attachments Endpoints**

```
POST   /api/v1/expenses/{expenseId}/attachments - Upload attachment
GET    /api/v1/expenses/{expenseId}/attachments - List attachments
DELETE /api/v1/attachments/{attachmentId}       - Delete attachment
```

### **🔔 Notifications Endpoints**

```
GET    /api/v1/notifications                    - List user notifications
PUT    /api/v1/notifications/{notificationId}/read - Mark as read
DELETE /api/v1/notifications/{notificationId}   - Delete notification
GET    /api/v1/notifications/unread-count       - Get unread count
```

### **📡 Real-Time Updates (SSE + Push Notifications) Endpoints**

```
# Push Notification Tokens
POST   /api/v1/notifications/tokens             - Register push token
DELETE /api/v1/notifications/tokens/{tokenId}   - Unregister push token
PUT    /api/v1/notifications/tokens/{tokenId}   - Update push token

# Real-Time Events (SSE Streams)
GET    /api/v1/events/nex/{nexId}/stream       - SSE stream for nex updates
GET    /api/v1/events/user/{userId}/stream      - SSE stream for user updates
POST   /api/v1/events/nex/{nexId}/broadcast    - Broadcast event to nex members
```

### **📈 Reports & Analytics Endpoints**

```
# Expense Analytics
GET    /api/v1/nex/{nexId}/expenses/analytics   - Expense analytics
GET    /api/v1/nex/{nexId}/expenses/summary     - Get expense summary

# Debt Analytics
GET    /api/v1/nex/{nexId}/debts/summary        - Debt summary

# Settlement Analytics
GET    /api/v1/nex/{nexId}/settlements/analytics - Settlement analytics
GET    /api/v1/nex/{nexId}/settlements/summary   - Settlement summary

# Reports
GET    /api/v1/reports/export                   - Export expense reports
GET    /api/v1/nex/{nexId}/reports/monthly      - Monthly expense report
GET    /api/v1/nex/{nexId}/reports/category     - Category-wise report
GET    /api/v1/nex/{nexId}/reports/settlements  - Settlement report
```

## Settlement Processing

### **🎯 Settlement Processing Overview**

The settlement processing module handles debt settlement using existing database fields without requiring additional tables. It leverages the existing `debts` table with fields like `settled_at`, `payment_method`, and `notes` to track settlements.

### **🏗️ Settlement Architecture**

#### **Key Design Principles**

1. **No Database Changes**: Uses existing debt fields for settlement tracking
2. **Flexible Settlement Types**: Supports both simplified and detailed settlement views
3. **Real-time Updates**: Automatic broadcasting of settlement events
4. **Audit Trail**: Complete tracking of settlement history
5. **Performance Optimized**: Efficient algorithms with minimal database queries

#### **Settlement Types**

1. **DETAILED**: Shows all individual debt transactions that can be settled separately
2. **SIMPLIFIED**: Shows minimum transactions to settle all debts (calculated from net balances)

### **📊 Settlement Processing Components**

#### **1. Settlement Execution Service**

```java
@Service
@Transactional
public class SettlementServiceImpl implements SettlementService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private NexService nexService;

    /**
     * Execute settlements using existing debt fields
     */
    @Override
    public SettlementExecutionResponse executeSettlements(
            String nexId,
            SettlementExecutionRequest request,
            String userId) {

        // Validate user has access to this nex
        if (!nexService.isMember(nexId, userId)) {
            throw new BusinessException("User is not a member of this nex", ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
        }

        List<Debt> settledDebts = new ArrayList<>();
        List<Debt> remainingDebts = new ArrayList<>();

        if ("SIMPLIFIED".equals(request.getSettlementType())) {
            settledDebts = executeSimplifiedSettlements(nexId, request, userId);
        } else if ("DETAILED".equals(request.getSettlementType())) {
            settledDebts = executeDetailedSettlements(nexId, request, userId);
        }

        // Get remaining debts
        remainingDebts = getRemainingDebts(nexId, request.getSettlementType());

        // Broadcast real-time update
        broadcastSettlementEvent(nexId, settledDebts, userId);

        return SettlementExecutionResponse.builder()
            .executedSettlements(convertDebtsToSettlementTransactions(settledDebts))
            .remainingSettlements(convertDebtsToSettlementTransactions(remainingDebts))
            .totalSettledAmount(calculateTotalSettledAmount(settledDebts))
            .settledCount(settledDebts.size())
            .remainingCount(remainingDebts.size())
            .nexId(nexId)
            .executionDate(LocalDateTime.now())
            .build();
    }

    /**
     * Mark debts as settled using existing fields
     */
    private List<Debt> markDebtsAsSettled(
            List<Debt> debts,
            SettlementExecutionRequest request,
            String userId) {

        List<Debt> settledDebts = new ArrayList<>();

        for (Debt debt : debts) {
            if (debt.getSettledAt() == null) {
                // Use existing fields to mark debt as settled
                debt.setSettledAt(request.getSettlementDate() != null ?
                    request.getSettlementDate() : LocalDateTime.now());
                debt.setPaymentMethod(request.getPaymentMethod());
                debt.setNotes(request.getNotes());

                // Save debt
                Debt savedDebt = debtRepository.save(debt);
                settledDebts.add(savedDebt);

                // Log audit event
                logSettlementEvent(savedDebt, userId);
            }
        }

        return settledDebts;
    }
}
```

#### **2. Settlement DTOs**

```java
@Data
@Builder
public class SettlementExecutionRequest {
    private String settlementType; // SIMPLIFIED, DETAILED
    private List<String> debtIds; // IDs of debts to settle
    private String paymentMethod; // Uses existing payment_method field
    private String notes; // Uses existing notes field
    private LocalDateTime settlementDate; // Uses existing settled_at field
    private boolean settleAll; // For simplified: settle all available debts
}

@Data
@Builder
public class SettlementExecutionResponse {
    private List<SettlementTransaction> executedSettlements;
    private List<SettlementTransaction> remainingSettlements;
    private BigDecimal totalSettledAmount;
    private int settledCount;
    private int remainingCount;
    private String nexId;
    private LocalDateTime executionDate;
}

@Data
@Builder
public class SettlementTransaction {
    private String id; // Debt ID
    private String fromUserId; // Debtor ID
    private String toUserId; // Creditor ID
    private BigDecimal amount;
    private SettlementType settlementType; // SIMPLIFIED, DETAILED
    private SettlementStatus status; // PENDING, SETTLED (based on settledAt)
    private List<String> relatedDebtIds; // For simplified: list of debt IDs
    private String expenseId; // From debt
    private String expenseTitle; // From related expense

    public enum SettlementType {
        SIMPLIFIED, DETAILED
    }

    public enum SettlementStatus {
        PENDING, SETTLED
    }
}
```

#### **3. Settlement Repository Methods**

```java
@Repository
public interface DebtRepository extends JpaRepository<Debt, String> {

    // Find unsettled debts for simplified settlement
    @Query("SELECT d FROM Debt d WHERE d.expense.nexId = :nexId AND d.settledAt IS NULL")
    List<Debt> findUnsettledDebtsForSimplifiedSettlement(@Param("nexId") String nexId);

    // Find unsettled debts by nex
    @Query("SELECT d FROM Debt d WHERE d.expense.nexId = :nexId AND d.settledAt IS NULL")
    List<Debt> findUnsettledByNexId(@Param("nexId") String nexId);

    // Find settled debts with pagination (using existing settledAt field)
    @Query("SELECT d FROM Debt d WHERE d.expense.nexId = :nexId AND d.settledAt IS NOT NULL")
    Page<Debt> findSettledDebtsByNexId(@Param("nexId") String nexId, Pageable pageable);

    // Find debts by expense
    @Query("SELECT d FROM Debt d WHERE d.expenseId = :expenseId")
    List<Debt> findByExpenseId(@Param("expenseId") String expenseId);
}
```

#### **4. Settlement Controller**

```java
@RestController
@RequestMapping("/api/v1/nex/{nexId}/settlements")
public class SettlementController {

    /**
     * Execute settlements by marking debts as settled
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<SettlementExecutionResponse>> executeSettlements(
            @PathVariable String nexId,
            @RequestBody SettlementExecutionRequest request,
            @AuthenticationPrincipal String userId) {

        SettlementExecutionResponse response = settlementService.executeSettlements(
            nexId, request, userId);

        return ResponseEntity.ok(ApiResponse.success(response, "Settlements executed successfully"));
    }

    /**
     * Get available settlements for execution
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<AvailableSettlementsResponse>> getAvailableSettlements(
            @PathVariable String nexId,
            @RequestParam(defaultValue = "SIMPLIFIED") String settlementType,
            @AuthenticationPrincipal String userId) {

        AvailableSettlementsResponse response = settlementService.getAvailableSettlements(
            nexId, settlementType, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get settlement history (from settled debts)
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PaginatedResponse<SettlementHistoryResponse>>> getSettlementHistory(
            @PathVariable String nexId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal String userId) {

        PaginatedResponse<SettlementHistoryResponse> response = settlementService.getSettlementHistory(
            nexId, page, size, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### **📝 Settlement Processing Examples**

#### **Simplified Settlement Scenarios**

```json
// 1. Settle all simplified settlements
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "SIMPLIFIED",
  "settleAll": true,
  "paymentMethod": "BANK_TRANSFER",
  "notes": "Monthly settlement",
  "settlementDate": "2024-01-15T10:30:00"
}

// 2. Settle specific debts (for simplified view)
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "SIMPLIFIED",
  "debtIds": ["debt-1", "debt-3", "debt-5"],
  "paymentMethod": "CASH",
  "notes": "Partial settlement",
  "settlementDate": "2024-01-15T10:30:00"
}
```

#### **Detailed Settlement Scenarios**

```json
// Settle specific debts individually
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "DETAILED",
  "debtIds": ["debt-1", "debt-5", "debt-8"],
  "paymentMethod": "DIGITAL_WALLET",
  "notes": "Settling specific expenses",
  "settlementDate": "2024-01-15T10:30:00"
}
```

#### **Get Settlement History**

```json
GET /api/v1/nex/{nexId}/settlements/history?page=0&size=20

// Response
{
  "success": true,
  "data": {
    "data": [
      {
        "debtId": "debt-1",
        "expenseId": "expense-1",
        "expenseTitle": "Dinner",
        "fromUserId": "user-1",
        "toUserId": "user-2",
        "amount": 25.00,
        "paymentMethod": "CASH",
        "notes": "Settled at restaurant",
        "settledAt": "2024-01-15T10:30:00",
        "settledBy": "user-1"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3
    }
  }
}
```

### **🔧 Settlement Processing Benefits**

1. **No Database Changes**: Uses existing debt fields (`settled_at`, `payment_method`, `notes`)
2. **No New Tables**: Everything works with the current schema
3. **Backward Compatible**: Existing data remains intact
4. **Simple Implementation**: Leverages existing debt structure
5. **Flexible Settlement**: Supports both simplified and detailed views
6. **Historical Data**: Settlement history available from settled debts
7. **Performance**: No additional joins or complex queries

## Settlement Algorithms

### **🎯 Settlement Strategy**

The system supports two settlement types:

1. **DETAILED**: Shows all individual debt transactions
2. **SIMPLIFIED**: Shows minimum transactions to settle all debts

### **📊 Settlement Calculation Process**

#### **Step 1: Calculate Net Balances**

```java
public Map<String, BigDecimal> calculateNetBalances(String nexId) {
    // Get all unsettled debts for the nex
    List<Debt> unsettledDebts = debtRepository.findUnsettledByNexId(nexId);

    Map<String, BigDecimal> netBalances = new HashMap<>();

    for (Debt debt : unsettledDebts) {
        // Add to debtor's negative balance
        netBalances.merge(debt.getDebtorId(), debt.getAmount().negate(), BigDecimal::add);

        // Add to creditor's positive balance
        netBalances.merge(debt.getCreditorId(), debt.getAmount(), BigDecimal::add);
    }

    return netBalances;
}
```

#### **Step 2: Simplified Settlement Algorithm**

```java
public List<SettlementTransaction> generateSimplifiedSettlements(String nexId) {
    // Calculate net balances
    Map<String, BigDecimal> netBalances = calculateNetBalances(nexId);

    // Separate creditors and debtors
    List<String> creditors = new ArrayList<>();
    List<String> debtors = new ArrayList<>();

    for (Map.Entry<String, BigDecimal> entry : netBalances.entrySet()) {
        if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
            creditors.add(entry.getKey());
        } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
            debtors.add(entry.getKey());
        }
    }

    // Sort by absolute balance (largest first)
    creditors.sort((a, b) -> netBalances.get(b).compareTo(netBalances.get(a)));
    debtors.sort((a, b) -> netBalances.get(a).compareTo(netBalances.get(b)));

    List<SettlementTransaction> settlements = new ArrayList<>();

    // Generate minimum transactions
    while (!creditors.isEmpty() && !debtors.isEmpty()) {
        String creditor = creditors.get(0);
        String debtor = debtors.get(0);

        BigDecimal creditorBalance = netBalances.get(creditor);
        BigDecimal debtorBalance = netBalances.get(debtor).abs();

        // Calculate settlement amount
        BigDecimal settlementAmount = creditorBalance.min(debtorBalance);

        // Create settlement transaction
        settlements.add(SettlementTransaction.builder()
            .fromUserId(debtor)
            .toUserId(creditor)
            .amount(settlementAmount)
            .description("Simplified settlement")
            .build());

        // Update balances
        creditorBalance = creditorBalance.subtract(settlementAmount);
        debtorBalance = debtorBalance.subtract(settlementAmount);

        netBalances.put(creditor, creditorBalance);
        netBalances.put(debtor, debtorBalance.negate());

        // Remove users with zero balance
        if (creditorBalance.compareTo(BigDecimal.ZERO) == 0) {
            creditors.remove(0);
        }
        if (debtorBalance.compareTo(BigDecimal.ZERO) == 0) {
            debtors.remove(0);
        }
    }

    return settlements;
}
```

#### **Step 3: Detailed Settlement Algorithm**

```java
public List<SettlementTransaction> generateDetailedSettlements(String nexId) {
    // Get all unsettled debts
    List<Debt> unsettledDebts = debtRepository.findUnsettledByNexId(nexId);

    // Group debts by creditor-debtor pairs
    Map<String, List<Debt>> debtGroups = new HashMap<>();

    for (Debt debt : unsettledDebts) {
        String key = debt.getCreditorId() + "->" + debt.getDebtorId();
        debtGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(debt);
    }

    List<SettlementTransaction> settlements = new ArrayList<>();

    // Create settlement for each group
    for (Map.Entry<String, List<Debt>> entry : debtGroups.entrySet()) {
        List<Debt> debts = entry.getValue();

        // Calculate total amount for this creditor-debtor pair
        BigDecimal totalAmount = debts.stream()
            .map(Debt::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get creditor and debtor IDs
        String[] ids = entry.getKey().split("->");
        String creditorId = ids[0];
        String debtorId = ids[1];

        // Create settlement transaction
        settlements.add(SettlementTransaction.builder()
            .fromUserId(debtorId)
            .toUserId(creditorId)
            .amount(totalAmount)
            .description("Detailed settlement")
            .expenseIds(debts.stream().map(Debt::getExpenseId).collect(Collectors.toList()))
            .build());
    }

    return settlements;
}
```

### **📝 Settlement Examples**

#### **Example 1: Simple Case**

```
Original Debts:
- Alice owes Bob: $50
- Bob owes Charlie: $30
- Charlie owes Alice: $20

Net Balances:
- Alice: -$30 (net debtor)
- Bob: +$20 (net creditor)
- Charlie: +$10 (net creditor)

Simplified Settlement:
- Alice -> Bob: $30
- Bob -> Charlie: $10

Total: 2 transactions
```

#### **Example 2: Complex Case**

```
Original Debts:
- Alice owes Bob: $100
- Bob owes Charlie: $80
- Charlie owes David: $60
- David owes Alice: $40
- Alice owes Charlie: $20

Net Balances:
- Alice: -$80 (net debtor)
- Bob: +$20 (net creditor)
- Charlie: +$40 (net creditor)
- David: +$20 (net creditor)

Simplified Settlement:
- Alice -> Bob: $20
- Alice -> Charlie: $40
- Alice -> David: $20

Total: 3 transactions
```

## Real-Time Updates with SSE + Push Notifications

### **🎯 Strategy for Web and Mobile Apps**

The system implements a standard approach combining:

1. **Server-Sent Events (SSE)** for active sessions (web/desktop/mobile foreground)
2. **Push Notifications** for mobile apps (background/offline)
3. **Automatic Connection Management** handled by mobile platforms
4. **Standard SSE Reconnection** strategies

#### **Event Types**

```java
public enum EventType {
    EXPENSE_CREATED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    DEBT_SETTLED,
    BILL_CREATED,
    BILL_PAID,
    SETTLEMENT_EXECUTED,
    NEX_UPDATED
}
```

#### **SSE Event Structure**

```java
public class SseEvent {
    private String id;
    private EventType type;
    private String nexId;
    private String userId;
    private Object data;
    private LocalDateTime timestamp;
    private String correlationId;
}
```

### **📡 SSE + Push Notifications Implementation**

#### **Push Notification Controller**

```java
@RestController
@RequestMapping("/api/v1/notifications")
public class PushNotificationController {

    @PostMapping("/tokens")
    public ResponseEntity<Void> registerPushToken(@RequestBody PushTokenRequest request,
                                                @AuthenticationPrincipal String userId) {
        pushNotificationService.registerToken(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tokens/{tokenId}")
    public ResponseEntity<Void> unregisterPushToken(@PathVariable String tokenId,
                                                  @AuthenticationPrincipal String userId) {
        pushNotificationService.unregisterToken(userId, tokenId);
        return ResponseEntity.ok().build();
    }
}
```

#### **Event Controller**

```java
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    @GetMapping(value = "/nex/{nexId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNexEvents(@PathVariable String nexId,
                                     @AuthenticationPrincipal String userId) {
        return eventService.createNexStream(nexId, userId);
    }

    @GetMapping(value = "/user/{userId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserEvents(@PathVariable String userId) {
        return eventService.createUserStream(userId);
    }

    @PostMapping("/nex/{nexId}/broadcast")
    public ResponseEntity<Void> broadcastEvent(@PathVariable String nexId,
                                             @RequestBody BroadcastEventRequest request) {
        eventService.broadcastToNex(nexId, request);
        return ResponseEntity.ok().build();
    }
}
```

#### **SSE Connection Manager**

```java
@Service
public class SseConnectionManager {

    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    public SseEmitter createConnection(String nexId, String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String key = nexId + ":" + userId;

        // Store connection
        activeConnections.put(key, emitter);
        connectionCount.incrementAndGet();

        // Auto-cleanup when connection closes
        emitter.onCompletion(() -> {
            activeConnections.remove(key);
            connectionCount.decrementAndGet();
        });

        emitter.onTimeout(() -> {
            activeConnections.remove(key);
            connectionCount.decrementAndGet();
        });

        emitter.onError(ex -> {
            activeConnections.remove(key);
            connectionCount.decrementAndGet();
        });

        return emitter;
    }

    public void broadcastToNex(String nexId, SseEvent event) {
        activeConnections.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(nexId + ":"))
            .forEach(entry -> {
                try {
                    entry.getValue().send(SseEmitter.event()
                        .id(event.getId())
                        .name(event.getType().name())
                        .data(event));
                } catch (IOException e) {
                    // Connection failed, remove it
                    activeConnections.remove(entry.getKey());
                    connectionCount.decrementAndGet();
                }
            });
    }

    public int getActiveConnectionCount() {
        return connectionCount.get();
    }
}
```

#### **Push Notification Service**

```java
@Service
public class PushNotificationService {

    private final Map<String, List<PushToken>> userPushTokens = new ConcurrentHashMap<>();

    @Async("asyncExecutor")
    public CompletableFuture<Void> sendPushNotification(String userId, String title, String body, Map<String, Object> data) {
        List<PushToken> tokens = userPushTokens.getOrDefault(userId, new ArrayList<>());

        tokens.forEach(token -> {
            try {
                // Send to Firebase/APNS based on platform
                if ("android".equals(token.getPlatform())) {
                    firebaseService.sendNotification(token.getToken(), title, body, data);
                } else if ("ios".equals(token.getPlatform())) {
                    apnsService.sendNotification(token.getToken(), title, body, data);
                }
            } catch (Exception e) {
                // Log error and continue with other tokens
                log.error("Failed to send push notification to token: {}", token.getToken(), e);
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    public void registerToken(String userId, PushTokenRequest request) {
        PushToken token = PushToken.builder()
            .userId(userId)
            .token(request.getToken())
            .platform(request.getPlatform())
            .deviceId(request.getDeviceId())
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();

        // Store in memory
        userPushTokens.computeIfAbsent(userId, k -> new ArrayList<>()).add(token);
    }

    public void unregisterToken(String userId, String tokenId) {
        List<PushToken> tokens = userPushTokens.get(userId);
        if (tokens != null) {
            tokens.removeIf(token -> token.getId().equals(tokenId));
        }
    }

    public int getRegisteredTokenCount() {
        return userPushTokens.values().stream()
            .mapToInt(List::size)
            .sum();
    }
}
```

#### **Event Service**

```java
@Service
public class EventService {

    @Autowired
    private SseConnectionManager sseConnectionManager;
    @Autowired
    private PushNotificationService pushNotificationService;

    public SseEmitter createNexStream(String nexId, String userId) {
        // Check if user is member of nex
        if (!nexMemberService.isMember(nexId, userId)) {
            throw new UnauthorizedException("User is not a member of this nex");
        }

        return sseConnectionManager.createConnection(nexId, userId);
    }

    public void broadcastToNex(String nexId, BroadcastEventRequest request) {
        List<String> memberIds = nexMemberService.getMemberIds(nexId);

        SseEvent event = SseEvent.builder()
            .id(UUID.randomUUID().toString())
            .type(request.getType())
            .nexId(nexId)
            .userId(request.getUserId())
            .data(request.getData())
            .timestamp(LocalDateTime.now())
            .correlationId(request.getCorrelationId())
            .build();

        // Broadcast to all active SSE connections for this nex
        sseConnectionManager.broadcastToNex(nexId, event);

        // Send push notifications to all members (for mobile apps in background)
        memberIds.forEach(memberId -> {
            pushNotificationService.sendPushNotification(
                memberId,
                getNotificationTitle(request.getType()),
                getNotificationBody(request.getType(), request.getData()),
                Map.of("eventType", request.getType().name(), "nexId", nexId)
            );
        });
    }

    public int getActiveConnectionCount() {
        return sseConnectionManager.getActiveConnectionCount();
    }
}
```

#### **Event Integration with Services**

```java
@Service
public class ExpenseService {

    @Autowired
    private EventService eventService;

    @Transactional
    public Expense createExpense(CreateExpenseRequest request, String userId) {
        Expense expense = // ... create expense logic

        // Broadcast real-time update
        BroadcastEventRequest eventRequest = BroadcastEventRequest.builder()
            .type(EventType.EXPENSE_CREATED)
            .userId(userId)
            .data(expense)
            .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
            .build();

        eventService.broadcastToNex(expense.getNexId(), eventRequest);

        return expense;
    }
}
```

#### **React Native Integration Example**

```javascript
// App.js or main component
import messaging from "@react-native-firebase/messaging";
import { EventSource } from "react-native-sse";

class NexSplitApp {
  constructor() {
    this.eventSources = new Map(); // Track SSE connections
  }

  async componentDidMount() {
    // Request permission for push notifications
    const authStatus = await messaging().requestPermission();

    // Get FCM token
    const token = await messaging().getToken();

    // Register token with backend
    await this.registerPushToken(token);

    // Setup SSE for active nex groups
    this.setupSSEConnections();
  }

  async registerPushToken(token) {
    await fetch("/api/v1/notifications/tokens", {
      method: "POST",
      headers: { Authorization: `Bearer ${this.state.accessToken}` },
      body: JSON.stringify({
        token: token,
        platform: Platform.OS,
        deviceId: DeviceInfo.getUniqueId(),
      }),
    });
  }

  setupSSEConnections() {
    // Connect to user's nex groups
    const nexGroups = await this.getUserNexGroups();

    nexGroups.forEach(nex => {
      this.connectToNexStream(nex.id);
    });
  }

  connectToNexStream(nexId) {
    const eventSource = new EventSource(`/api/v1/events/nex/${nexId}/stream`);

    // Store connection for cleanup
    this.eventSources.set(nexId, eventSource);

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      this.handleRealTimeUpdate(data);
    };

    // Handle automatic reconnection
    eventSource.onerror = () => {
      setTimeout(() => {
        this.connectToNexStream(nexId);
      }, 5000);
    };
  }

  handleRealTimeUpdate(event) {
    switch (event.type) {
      case "EXPENSE_CREATED":
        this.updateExpenseList(event.data);
        break;
      case "DEBT_SETTLED":
        this.updateDebtSummary(event.data);
        break;
    }
  }

  // Cleanup when app goes to background
  onAppBackground() {
    // Close all SSE connections
    this.eventSources.forEach(eventSource => {
      eventSource.close();
    });
    this.eventSources.clear();
  }

  // Reconnect when app comes to foreground
  onAppForeground() {
    this.setupSSEConnections();
  }
}
```

### **🔧 SSE + Push Notifications Configuration**

#### **WebMvcConfigurer for SSE**

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/events/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

#### **SSE + Push Notifications Health Monitoring**

```java
@Component
public class SseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        int activeSseConnections = eventService.getActiveConnectionCount();
        int registeredPushTokens = pushNotificationService.getRegisteredTokenCount();

        return Health.up()
            .withDetail("activeSseConnections", activeSseConnections)
            .withDetail("registeredPushTokens", registeredPushTokens)
            .build();
    }
}
```

## Response Consistency Mechanism

### Overview

The system implements industrial-grade response consistency following REST API best practices with standardized error handling, correlation tracking, and structured responses.

### Core Components

#### 1. Enhanced ApiResponse Structure

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private String correlationId;
    private LocalDateTime timestamp;
    private String requestId;
    private MetaInfo meta;
}
```

**Features:**

- **Generic Type Support**: `ApiResponse<T>` for type-safe responses
- **Correlation Tracking**: Links requests across distributed systems
- **Timestamp**: Automatic response timing
- **Metadata**: Error codes, documentation URLs, version info
- **Backward Compatibility**: Legacy methods preserved

#### 2. Standardized Error Codes

```java
public enum ErrorCode {
    // Authentication Errors
    AUTH_TOKEN_EXPIRED("AUTH_001", "AUTHENTICATION_ERROR"),
    AUTH_INVALID_CREDENTIALS("AUTH_002", "AUTHENTICATION_ERROR"),

    // Business Errors
    USER_NOT_FOUND("USER_001", "BUSINESS_ERROR"),
    NEX_MEMBER_LIMIT_EXCEEDED("NEX_002", "BUSINESS_ERROR"),

    // Validation Errors
    VALIDATION_REQUIRED_FIELD("VAL_001", "VALIDATION_ERROR"),
    VALIDATION_INVALID_AMOUNT("VAL_005", "VALIDATION_ERROR"),

    // System Errors
    INTERNAL_SERVER_ERROR("SYS_001", "SYSTEM_ERROR")
}
```

**Benefits:**

- **Consistent Error Handling**: Standardized across all endpoints
- **Client Integration**: Mobile apps can handle errors predictably
- **Monitoring**: Easy filtering and alerting by error type
- **Documentation**: Self-documenting error codes

#### 3. Enhanced Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        // Structured logging with correlation ID
        // Standardized error response with error codes
        // Automatic correlation ID injection
    }
}
```

**Features:**

- **Structured Logging**: All errors logged with context
- **Correlation ID Injection**: Automatic request tracking
- **Error Code Mapping**: Business exceptions to standard codes
- **Field-Level Validation**: Detailed validation error reporting

#### 4. Response Headers

```http
X-Correlation-ID: abc123
X-Request-ID: def456
X-Response-Time: 150ms
X-Rate-Limit-Remaining: 999
X-API-Version: 1.0.0
X-Server: NexSplit-API
```

**Headers Added:**

- **X-Correlation-ID**: Request tracing across services
- **X-Request-ID**: Unique request identifier
- **X-Response-Time**: Performance monitoring
- **X-Rate-Limit-Remaining**: Rate limiting info
- **X-API-Version**: API version tracking

#### 5. Pagination Support

```java
public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;
    private MetaInfo meta;

    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private String nextPageUrl;
        private String previousPageUrl;
    }
}
```

**Features:**

- **HATEOAS Links**: Self-documenting pagination URLs
- **Metadata**: Complete pagination information
- **Consistent Structure**: Same format across all list endpoints

### Usage Examples

#### Success Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Dinner Group",
    "members": 4
  },
  "message": "Nex created successfully",
  "timestamp": "2024-01-15T10:30:00",
  "correlationId": "abc123",
  "requestId": "def456"
}
```

#### Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "amount: must be greater than 0",
    "description: must not be empty"
  ],
  "timestamp": "2024-01-15T10:30:00",
  "correlationId": "abc123",
  "requestId": "def456",
  "meta": {
    "errorCode": "VAL_005",
    "errorType": "VALIDATION_ERROR",
    "documentationUrl": "/api/docs/errors/VAL_005"
  }
}
```

#### Paginated Response

```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Expense 1" },
    { "id": 2, "name": "Expense 2" }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false,
    "nextPageUrl": "/api/v1/expenses?page=1&size=10",
    "previousPageUrl": null
  },
  "timestamp": "2024-01-15T10:30:00",
  "correlationId": "abc123"
}
```

### Implementation Benefits

#### 1. **Mobile App Integration**

```typescript
class ApiClient {
  async request<T>(endpoint: string): Promise<ApiResponse<T>> {
    const response = await fetch(endpoint);
    const data = await response.json();

    // Consistent error handling
    if (!data.success) {
      throw new ApiError(data.message, data.meta?.errorCode);
    }

    return data;
  }
}
```

#### 2. **Monitoring & Debugging**

- **Correlation Tracking**: Link requests across services
- **Performance Monitoring**: Response time tracking
- **Error Aggregation**: Group by error codes
- **Request Tracing**: Full request lifecycle

#### 3. **API Documentation**

- **Self-Documenting**: Error codes with descriptions
- **Consistent Structure**: Predictable response format
- **HATEOAS**: Self-documenting pagination links

### Configuration

#### 1. **Filters Order**

```java
@Component
@Order(1)
public class CorrelationIdFilter implements Filter { }

@Component
@Order(2)
public class ResponseHeaderFilter implements Filter { }
```

#### 2. **Exception Handling Priority**

1. **Business Exceptions**: Custom business logic errors
2. **Validation Exceptions**: Input validation errors
3. **Authentication Exceptions**: Security-related errors
4. **System Exceptions**: Unexpected errors

#### 3. **Logging Integration**

- **Structured Logging**: JSON format for Elasticsearch
- **Correlation ID**: Automatic request tracking
- **Error Context**: Rich error information
- **Performance Metrics**: Response time logging

### Best Practices

#### 1. **Error Handling**

- Always use `BusinessException` for business rule violations
- Include correlation ID in all error responses
- Log errors with structured context
- Return appropriate HTTP status codes

#### 2. **Response Structure**

- Use generic `ApiResponse<T>` for type safety
- Include correlation ID in all responses
- Add meaningful error messages
- Use standardized error codes

#### 3. **Pagination**

- Always use `PaginatedResponse<T>` for list endpoints
- Include HATEOAS links for navigation
- Provide complete pagination metadata
- Support customizable page sizes

#### 4. **Headers**

- Always include correlation ID
- Add response time for monitoring
- Include API version for compatibility
- Provide rate limit information

This response consistency mechanism ensures a professional, maintainable, and user-friendly API that follows industry best practices and provides excellent developer experience.

## Implementation Architecture

### **🏗️ Service Layer Design**

```java
@Service
public class NexService {
    // Nex management
    public Nex createNex(CreateNexRequest request, String userId);
    public List<Nex> getUserNexes(String userId);
    public Nex getNexById(String nexId, String userId);
    public Nex updateNex(String nexId, UpdateNexRequest request, String userId);
    public void archiveNex(String nexId, String userId);
}

@Service
public class ExpenseService {
    // Expense management
    public Expense createExpense(CreateExpenseRequest request, String userId);
    public List<Expense> getExpenses(String nexId, ExpenseFilter filter);
    public Expense getExpenseById(String expenseId, String userId);
    public Expense updateExpense(String expenseId, UpdateExpenseRequest request, String userId);
    public void deleteExpense(String expenseId, String userId);
}

@Service
public class SettlementService {
    // Settlement calculations
    public SettlementSummary calculateSettlements(String nexId, String settlementType);
    public List<SettlementTransaction> generateSimplifiedSettlements(String nexId);
    public List<SettlementTransaction> generateDetailedSettlements(String nexId);
    public void executeSettlements(String nexId, List<SettlementTransaction> transactions);
}
```

### **🔐 Security Architecture**

#### **Authentication Flow**

1. **JWT Access Tokens**: 15-minute expiration
2. **JWT Refresh Tokens**: 7-day expiration with theft detection
3. **OAuth2 Integration**: Google authentication
4. **Virtual Threads**: Async processing for security operations

#### **Authorization Strategy**

```java
@PreAuthorize("hasRole('ADMIN') or @nexService.isMember(#nexId, #userId)")
public Nex updateNex(String nexId, UpdateNexRequest request, String userId);

@PreAuthorize("@nexService.isMember(#nexId, #userId)")
public List<Expense> getExpenses(String nexId, ExpenseFilter filter);
```

### **📊 Data Access Layer & Event Management**

#### **Repository Pattern**

````java
@Repository
public interface NexRepository extends JpaRepository<Nex, String> {
    List<Nex> findByMembersUserIdAndStatus(String userId, String status);
    Optional<Nex> findByIdAndMembersUserId(String nexId, String userId);
}

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByNexIdAndCreatedAtBetween(String nexId, LocalDateTime start, LocalDateTime end);
    BigDecimal sumAmountByNexIdAndCategoryId(String nexId, String categoryId);
}

#### **Event Repository**
```java
// No database table needed - events are handled in memory
// Event history can be retrieved from existing tables (expenses, debts, etc.)
// when needed for offline sync
````

````

### **🔄 Async Processing & Real-Time Events**

#### **Virtual Threads Integration**

```java
@Async("asyncExecutor")
public CompletableFuture<Void> sendExpenseNotification(String expenseId, String userId) {
    // Send notification asynchronously
    return CompletableFuture.completedFuture(null);
}

@Async("asyncExecutor")
public CompletableFuture<Void> processExpenseSplits(String expenseId) {
    // Process expense splits asynchronously
    return CompletableFuture.completedFuture(null);
}

#### **Real-Time Event Processing**
```java
@Async("asyncExecutor")
public CompletableFuture<Void> broadcastExpenseEvent(String expenseId, String nexId, String userId) {
    // Broadcast expense event to all nex members via SSE
    BroadcastEventRequest eventRequest = BroadcastEventRequest.builder()
        .type(EventType.EXPENSE_CREATED)
        .userId(userId)
        .data(expenseService.getExpenseById(expenseId))
        .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
        .build();

    eventService.broadcastToNex(nexId, eventRequest);
    return CompletableFuture.completedFuture(null);
}

@Async("asyncExecutor")
public CompletableFuture<Void> sendPushNotification(String userId, String message) {
    // Send push notification to user's devices
    return CompletableFuture.completedFuture(null);
}
````

````

## Performance Considerations & Real-Time Optimization

### **🚀 Database Optimization**

#### **Indexing Strategy**

```sql
-- Composite indexes for common queries
CREATE INDEX idx_expenses_nex_created ON expenses(nex_id, created_at);
CREATE INDEX idx_debts_nex_settled ON debts(expense_id, settled_at);
CREATE INDEX idx_nex_members_user_status ON nex_members(user_id, status);
````

#### **Query Optimization**

```java
// Use projections for large datasets
@Query("SELECT e.id, e.title, e.amount, e.createdAt FROM Expense e WHERE e.nexId = :nexId")
List<ExpenseSummary> findExpenseSummariesByNexId(@Param("nexId") String nexId);

// Pagination for large result sets
Page<Expense> findByNexId(String nexId, Pageable pageable);
```

### **💾 Caching Strategy**

#### **Redis Caching**

```java
@Cacheable("nex-settlements")
public SettlementSummary calculateSettlements(String nexId, String settlementType) {
    // Expensive calculation cached for 5 minutes
}

@CacheEvict("nex-settlements")
public void createExpense(CreateExpenseRequest request, String userId) {
    // Clear cache when expenses change
}
```

#### **SSE Connection Management**

```java
@Component
public class SseConnectionManager {

    private final Map<String, SseEmitter> sseConnections = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    public void addSseConnection(String key, SseEmitter emitter) {
        sseConnections.put(key, emitter);
        connectionCount.incrementAndGet();

        emitter.onCompletion(() -> {
            sseConnections.remove(key);
            connectionCount.decrementAndGet();
        });

        emitter.onTimeout(() -> {
            sseConnections.remove(key);
            connectionCount.decrementAndGet();
        });
    }

    public int getActiveSseConnectionCount() {
        return connectionCount.get();
    }
}
```

````

## Monitoring & Observability

### **📊 Structured Logging & Event Tracking**

```java
StructuredLoggingUtil.logBusinessEvent(
    "EXPENSE_CREATED",
    userId,
    "CREATE_EXPENSE",
    "SUCCESS",
    Map.of(
        "expenseId", expenseId,
        "nexId", nexId,
        "amount", amount,
        "currency", currency
    )
);

// Log SSE events
StructuredLoggingUtil.logBusinessEvent(
    "SSE_EVENT_SENT",
    userId,
    "REAL_TIME_UPDATE",
    "SUCCESS",
    Map.of(
        "eventType", "EXPENSE_CREATED",
        "nexId", nexId,
        "sseRecipients", sseRecipientCount,
        "pushRecipients", pushRecipientCount,
        "correlationId", correlationId
    )
);
```
```
````

````

### **🔍 Audit Trail**

```java
@Async("asyncExecutor")
public void logExpenseEvent(String expenseId, String userId, String action, String details) {
    AuditEvent event = AuditEvent.builder()
        .userId(userId)
        .eventType(action)
        .eventCategory("EXPENSE")
        .eventDetails(details)
        .severityLevel("MEDIUM")
        .build();

    auditEventRepository.save(event);
}
````

## Testing Strategy

### **🧪 Unit Tests**

```java
@Test
public void testSimplifiedSettlementCalculation() {
    // Given
    List<Debt> debts = createTestDebts();

    // When
    List<SettlementTransaction> settlements = settlementService.generateSimplifiedSettlements(nexId);

    // Then
    assertThat(settlements).hasSize(2);
    assertThat(settlements.get(0).getAmount()).isEqualTo(new BigDecimal("30"));
}
```

### **🔧 Integration Tests**

```java
@SpringBootTest
@Testcontainers
class ExpenseIntegrationTest {

    @Test
    public void testExpenseCreationFlow() {
        // Test complete expense creation flow
        // Including splits, debts, and notifications
    }
}
```

## Deployment & DevOps

### **🐳 Docker Configuration**

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/nexsplit-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### **📊 Health Checks**

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check database connectivity
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

## Conclusion

This architecture provides:

1. **Scalable Design**: Modular services with clear separation of concerns
2. **Security-First**: Comprehensive authentication and authorization
3. **Performance Optimized**: Efficient algorithms with caching and indexing
4. **Maintainable Code**: Clean architecture with comprehensive testing
5. **Observable System**: Structured logging and monitoring
6. **Flexible Settlements**: Both simplified and detailed settlement algorithms
7. **Standard Real-Time Updates**: SSE for active sessions, push notifications for background
8. **Mobile-Friendly**: Automatic connection management and reconnection strategies
9. **Event-Driven Architecture**: Asynchronous event processing for scalability

The system is designed to handle real-world expense tracking scenarios while maintaining high performance, security standards, and providing standard real-time updates for both web and mobile applications.
