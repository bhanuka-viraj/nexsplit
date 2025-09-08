-- ========================================
-- V1: AUTHENTICATION AND USER MANAGEMENT TABLES
-- ========================================
-- This migration contains core authentication and user management tables
-- Includes user accounts, refresh tokens, and audit events

-- ========================================
-- USERS TABLE
-- ========================================
CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    username VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20),
    last_validation_code INTEGER DEFAULT 0,
    is_email_validate BOOLEAN DEFAULT FALSE,
    is_google_auth BOOLEAN DEFAULT FALSE,
    status TEXT CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_username_unique UNIQUE (username)
);

-- ========================================
-- AUDIT EVENTS TABLE
-- ========================================
-- This table stores comprehensive audit events for compliance and security
-- Provides business-level audit trail separate from application logs
CREATE TABLE audit_events (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36),
    event_type VARCHAR(255) NOT NULL,
    event_category VARCHAR(100) NOT NULL,
    event_details TEXT,
    ip_address VARCHAR(45), -- IPv6 compatible
    user_agent TEXT,
    severity_level VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_audit_events_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ========================================
-- REFRESH TOKENS TABLE
-- ========================================
-- This table stores refresh tokens with security features
-- Implements token rotation and theft detection
CREATE TABLE refresh_tokens (
    id CHAR(36) PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    user_id CHAR(36) NOT NULL,
    family_id CHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    user_agent TEXT,
    
    -- Foreign key constraint
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Users table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_username ON users(username);

-- Audit events table indexes
CREATE INDEX idx_audit_events_user_id ON audit_events(user_id);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events_event_category ON audit_events(event_category);
CREATE INDEX idx_audit_events_severity_level ON audit_events(severity_level);
CREATE INDEX idx_audit_events_timestamp ON audit_events(timestamp);
CREATE INDEX idx_audit_events_ip_address ON audit_events(ip_address);
CREATE INDEX idx_audit_events_user_timestamp ON audit_events(user_id, timestamp);

-- Refresh tokens table indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens(family_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_is_used ON refresh_tokens(is_used);
CREATE INDEX idx_refresh_tokens_is_revoked ON refresh_tokens(is_revoked);