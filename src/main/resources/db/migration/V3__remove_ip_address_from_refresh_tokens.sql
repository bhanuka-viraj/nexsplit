-- ========================================
-- V3: REMOVE IP ADDRESS FROM REFRESH TOKENS
-- ========================================
-- This migration removes the ip_address column from refresh_tokens table
-- Since IP addresses are dynamic and cause false positives, we're removing
-- IP storage but keeping IP logging for audit purposes

-- Remove the ip_address column from refresh_tokens table
ALTER TABLE refresh_tokens DROP COLUMN ip_address;

-- Remove the index on ip_address since the column no longer exists
DROP INDEX IF EXISTS idx_refresh_tokens_ip_address;
