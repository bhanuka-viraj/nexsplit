-- Remove soft delete functionality from splits table
-- Since splits don't need soft delete functionality, we'll remove the soft delete columns

-- Drop indexes first
DROP INDEX IF EXISTS idx_splits_is_deleted;
DROP INDEX IF EXISTS idx_splits_deleted_at;

-- Remove soft delete columns from splits table
ALTER TABLE splits 
DROP COLUMN IF EXISTS deleted_at,
DROP COLUMN IF EXISTS deleted_by,
DROP COLUMN IF EXISTS is_deleted;
