# Database Schema Analysis Report

## Overview

This document provides a comprehensive analysis of the current database schema based on migration files V1-V4 and compares it with existing entity models.

## Database Tables Analysis

### 1. Core Authentication Tables (V1)

#### users

- **Primary Key**: `id` (CHAR(36))
- **Unique Constraints**: `email`, `username`
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `email` VARCHAR(255) NOT NULL UNIQUE
  - `password_hash` VARCHAR(255)
  - `first_name` VARCHAR(100)
  - `last_name` VARCHAR(100)
  - `username` VARCHAR(255) NOT NULL UNIQUE
  - `contact_number` VARCHAR(20)
  - `last_validation_code` INTEGER DEFAULT 0
  - `is_email_validate` BOOLEAN DEFAULT FALSE
  - `is_google_auth` BOOLEAN DEFAULT FALSE
  - `status` TEXT CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE'
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL
  - `deleted_at` TIMESTAMP

#### audit_events

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `user_id` → users(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `user_id` CHAR(36) REFERENCES users(id)
  - `event_type` VARCHAR(255) NOT NULL
  - `event_category` VARCHAR(100) NOT NULL
  - `event_details` TEXT
  - `ip_address` VARCHAR(45)
  - `user_agent` TEXT
  - `severity_level` VARCHAR(20) NOT NULL
  - `timestamp` TIMESTAMP NOT NULL
  - `created_at` TIMESTAMP NOT NULL

#### refresh_tokens

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `user_id` → users(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `token_hash` VARCHAR(255) NOT NULL UNIQUE
  - `user_id` CHAR(36) NOT NULL REFERENCES users(id)
  - `family_id` CHAR(36) NOT NULL
  - `expires_at` TIMESTAMP NOT NULL
  - `is_used` BOOLEAN DEFAULT FALSE
  - `is_revoked` BOOLEAN DEFAULT FALSE
  - `created_at` TIMESTAMP NOT NULL
  - `used_at` TIMESTAMP
  - `user_agent` TEXT

### 2. Business Tables (V2)

#### nex (Expense Groups)

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `created_by` → users(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `name` VARCHAR(255) NOT NULL
  - `description` VARCHAR(255)
  - `image_url` VARCHAR(255)
  - `created_by` CHAR(36) NOT NULL REFERENCES users(id)
  - `settlement_type` TEXT CHECK (settlement_type IN ('DETAILED', 'SIMPLIFIED')) NOT NULL DEFAULT 'DETAILED'
  - `is_archived` BOOLEAN DEFAULT FALSE
  - `nex_type` TEXT CHECK (nex_type IN ('PERSONAL', 'GROUP')) DEFAULT 'GROUP'
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### nex_members

- **Primary Key**: Composite (`nex_id`, `user_id`)
- **Foreign Keys**: `nex_id` → nex(id), `user_id` → users(id)
- **Columns**:
  - `nex_id` CHAR(36) REFERENCES nex(id)
  - `user_id` CHAR(36) REFERENCES users(id)
  - `role` TEXT CHECK (role IN ('ADMIN', 'MEMBER')) NOT NULL
  - `invited_at` TIMESTAMP
  - `joined_at` TIMESTAMP
  - `status` TEXT CHECK (status IN ('PENDING', 'ACTIVE', 'LEFT')) DEFAULT 'ACTIVE'
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### categories

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `created_by` → users(id), `nex_id` → nex(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `name` VARCHAR(255) NOT NULL
  - `created_by` CHAR(36) NOT NULL REFERENCES users(id)
  - `nex_id` CHAR(36) REFERENCES nex(id)
  - `is_default` BOOLEAN DEFAULT FALSE
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### expenses

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `category_id` → categories(id), `nex_id` → nex(id), `created_by` → users(id), `payer_id` → users(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `title` VARCHAR(255)
  - `amount` DECIMAL(10,2) NOT NULL
  - `currency` VARCHAR(10) DEFAULT 'USD'
  - `category_id` CHAR(36) NOT NULL REFERENCES categories(id)
  - `description` TEXT
  - `nex_id` CHAR(36) NOT NULL REFERENCES nex(id)
  - `created_by` CHAR(36) NOT NULL REFERENCES users(id)
  - `payer_id` CHAR(36) NOT NULL REFERENCES users(id)
  - `split_type` TEXT CHECK (split_type IN ('PERCENTAGE', 'AMOUNT', 'EQUALLY')) NOT NULL DEFAULT 'EQUALLY'
  - `is_initial_payer_has` BOOLEAN DEFAULT FALSE
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### splits

- **Primary Key**: Composite (`expense_id`, `user_id`)
- **Foreign Keys**: `expense_id` → expenses(id), `user_id` → users(id)
- **Columns**:
  - `expense_id` CHAR(36) REFERENCES expenses(id)
  - `user_id` CHAR(36) REFERENCES users(id)
  - `percentage` DECIMAL(5,2)
  - `amount` DECIMAL(10,2)
  - `notes` TEXT
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### debts

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `debtor_id` → users(id), `creditor_id` → users(id), `expense_id` → expenses(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `debtor_id` CHAR(36) NOT NULL REFERENCES users(id)
  - `creditor_id` CHAR(36) NOT NULL REFERENCES users(id)
  - `creditor_type` TEXT CHECK (creditor_type IN ('USER', 'EXPENSE')) NOT NULL
  - `amount` DECIMAL(10,2) NOT NULL
  - `expense_id` CHAR(36) NOT NULL REFERENCES expenses(id)
  - `payment_method` VARCHAR(50)
  - `notes` TEXT
  - `settled_at` TIMESTAMP
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### attachments

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `expense_id` → expenses(id), `uploaded_by` → users(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `expense_id` CHAR(36) NOT NULL REFERENCES expenses(id)
  - `file_url` VARCHAR(500) NOT NULL
  - `file_type` VARCHAR(50)
  - `uploaded_by` CHAR(36) NOT NULL REFERENCES users(id)
  - `created_at` TIMESTAMP NOT NULL

#### notifications

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `user_id` → users(id), `nex_id` → nex(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `user_id` CHAR(36) NOT NULL REFERENCES users(id)
  - `nex_id` CHAR(36) REFERENCES nex(id)
  - `type` TEXT CHECK (type IN ('INVITE', 'REMINDER', 'INFO'))
  - `message` TEXT NOT NULL
  - `is_read` BOOLEAN DEFAULT FALSE
  - `created_at` TIMESTAMP NOT NULL

#### bills

- **Primary Key**: `id` (CHAR(36))
- **Foreign Keys**: `nex_id` → nex(id), `created_by` → users(id)
- **Columns**:
  - `id` CHAR(36) PRIMARY KEY
  - `nex_id` CHAR(36) NOT NULL REFERENCES nex(id)
  - `created_by` CHAR(36) NOT NULL REFERENCES users(id)
  - `title` VARCHAR(255) NOT NULL
  - `amount` DECIMAL(10,2) NOT NULL
  - `currency` VARCHAR(10) DEFAULT 'USD'
  - `due_date` TIMESTAMP NOT NULL
  - `frequency` TEXT CHECK (frequency IN ('ONCE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')) NOT NULL
  - `next_due_date` TIMESTAMP
  - `is_recurring` BOOLEAN DEFAULT FALSE
  - `is_paid` BOOLEAN DEFAULT FALSE
  - `notes` TEXT
  - `created_at` TIMESTAMP NOT NULL
  - `modified_at` TIMESTAMP NOT NULL

#### bill_participants

- **Primary Key**: Composite (`bill_id`, `user_id`)
- **Foreign Keys**: `bill_id` → bills(id), `user_id` → users(id)
- **Columns**:
  - `bill_id` CHAR(36) REFERENCES bills(id)
  - `user_id` CHAR(36) REFERENCES users(id)
  - `share_amount` DECIMAL(10,2)
  - `paid` BOOLEAN DEFAULT FALSE
  - `paid_at` TIMESTAMP
  - `created_at` TIMESTAMP NOT NULL

### 3. Soft Delete Fields (V3)

All business tables have soft delete fields added:

- `deleted_at` TIMESTAMP NULL
- `deleted_by` VARCHAR(36) NULL
- `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE

### 4. System Data (V4)

- System user created: `00000000-0000-0000-0000-000000000000`
- 42 default categories inserted with system user as creator

## Entity-Database Consistency Analysis

### ✅ CORRECT MAPPINGS

#### User Entity

- All fields match database schema correctly
- Proper enum mapping for status
- Correct column definitions and constraints

#### Bill Entity

- All fields match database schema correctly
- Proper enum mapping for frequency
- Correct column definitions and constraints

#### Attachment Entity

- All fields match database schema correctly
- Correct column definitions and constraints

#### Debt Entity

- All fields match database schema correctly
- Proper enum mapping for creditor_type
- Correct column definitions and constraints

### ⚠️ ISSUES FOUND

#### 1. Missing Entities

- **Notification** entity missing (table exists in database)
- **BillParticipant** entity missing (table exists in database)

#### 2. ExpenseMapper Syntax Errors

- Lines 52, 86, 129, 175 have syntax issues (missing braces, incomplete method signatures)
- The mapper appears to be complete but may have compilation issues

#### 3. BaseEntity vs Individual Timestamps

- Some entities (Bill, Debt, Attachment) use individual timestamp fields
- Other entities (Expense, Nex, Category, Split, NexMember) extend BaseEntity
- This creates inconsistency in timestamp handling

#### 4. Missing Soft Delete Fields

- Bill, Debt, Attachment entities don't extend BaseEntity
- They're missing soft delete fields that exist in database (V3 migration)

## Recommendations

### 1. Create Missing Entities

- Create Notification entity
- Create BillParticipant entity

### 2. Fix Entity Consistency

- Make all entities extend BaseEntity for consistent timestamp and soft delete handling
- Or create a separate base class for entities that don't need soft delete

### 3. Fix ExpenseMapper

- Resolve syntax errors in ExpenseMapper
- Replace with MapStruct implementation

### 4. Database Views Strategy

Based on the schema analysis, the following views should be created:

- `settlement_history_view` - For debt settlement tracking
- `expense_summary_view` - For expense analytics
- `user_balance_view` - For user balance calculations
- `nex_analytics_view` - For group analytics
- `attachment_summary_view` - For file management

## Performance Indexes

All necessary indexes are already created in the migration files for optimal performance.

## Next Steps

1. Fix ExpenseMapper syntax errors
2. Create missing entities (Notification, BillParticipant)
3. Standardize entity inheritance (BaseEntity)
4. Implement MapStruct mappers
5. Create database views
6. Implement missing services and controllers
