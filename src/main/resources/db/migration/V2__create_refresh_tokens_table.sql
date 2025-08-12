-- ========================================
-- REFRESH TOKENS TABLE MIGRATION
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
    ip_address VARCHAR(45), -- IPv6 compatible
    user_agent TEXT,
    
    -- Foreign key constraint
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance 
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);

-- ========================================
-- CONFIGURATION UPDATES
-- ========================================

-- Update JWT configuration for shorter access tokens
-- Access tokens: 15 minutes (more secure)
-- Refresh tokens: 7 days (with rotation)
