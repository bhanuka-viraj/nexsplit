-- Add soft delete fields to existing tables
-- This migration adds the necessary columns for soft delete functionality

-- ========================================
-- USERS TABLE (Missing from original migration)
-- ========================================
-- Add missing soft delete columns to users table to match BaseEntity
ALTER TABLE users 
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- ========================================
-- NEX AND MEMBERS (Already implemented in code)
-- ========================================
-- Add soft delete fields to nex table
ALTER TABLE nex 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add soft delete fields to nex_members table
ALTER TABLE nex_members 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- ========================================
-- EXPENSE MANAGEMENT TABLES
-- ========================================
-- Add soft delete fields to categories table
ALTER TABLE categories 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add soft delete fields to expenses table
ALTER TABLE expenses 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add soft delete fields to splits table
ALTER TABLE splits 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add soft delete fields to debts table
ALTER TABLE debts 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add soft delete fields to attachments table
ALTER TABLE attachments 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- ========================================
-- BILL MANAGEMENT TABLES
-- ========================================
-- Add soft delete fields to bills table
ALTER TABLE bills 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add soft delete fields to bill_participants table
ALTER TABLE bill_participants 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- ========================================
-- NOTIFICATION TABLES
-- ========================================
-- Add soft delete fields to notifications table
ALTER TABLE notifications 
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36) NULL,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;


-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================
-- Users indexes
CREATE INDEX idx_users_is_deleted ON users(is_deleted);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Nex and members indexes
CREATE INDEX idx_nex_is_deleted ON nex(is_deleted);
CREATE INDEX idx_nex_members_is_deleted ON nex_members(is_deleted);
CREATE INDEX idx_nex_deleted_at ON nex(deleted_at);
CREATE INDEX idx_nex_members_deleted_at ON nex_members(deleted_at);

-- Categories indexes
CREATE INDEX idx_categories_is_deleted ON categories(is_deleted);
CREATE INDEX idx_categories_deleted_at ON categories(deleted_at);

-- Expenses indexes
CREATE INDEX idx_expenses_is_deleted ON expenses(is_deleted);
CREATE INDEX idx_expenses_deleted_at ON expenses(deleted_at);

-- Splits indexes
CREATE INDEX idx_splits_is_deleted ON splits(is_deleted);
CREATE INDEX idx_splits_deleted_at ON splits(deleted_at);

-- Debts indexes
CREATE INDEX idx_debts_is_deleted ON debts(is_deleted);
CREATE INDEX idx_debts_deleted_at ON debts(deleted_at);

-- Attachments indexes
CREATE INDEX idx_attachments_is_deleted ON attachments(is_deleted);
CREATE INDEX idx_attachments_deleted_at ON attachments(deleted_at);

-- Bills indexes
CREATE INDEX idx_bills_is_deleted ON bills(is_deleted);
CREATE INDEX idx_bills_deleted_at ON bills(deleted_at);

-- Bill participants indexes
CREATE INDEX idx_bill_participants_is_deleted ON bill_participants(is_deleted);
CREATE INDEX idx_bill_participants_deleted_at ON bill_participants(deleted_at);

-- Notifications indexes
CREATE INDEX idx_notifications_is_deleted ON notifications(is_deleted);
CREATE INDEX idx_notifications_deleted_at ON notifications(deleted_at);


-- Note: We keep is_archived for nex table as it will be used for archive functionality later
