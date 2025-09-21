-- ========================================
-- V5: DATABASE VIEWS FOR OPTIMAL PERFORMANCE
-- ========================================
-- This migration creates database views as the PRIMARY data access method
-- Views provide optimized queries for complex business operations
-- All views are based on the actual database schema from V1-V4

-- ========================================
-- SETTLEMENT HISTORY VIEW
-- ========================================
-- Primary view for debt settlement tracking and analytics
-- Provides comprehensive settlement information with user details
CREATE VIEW settlement_history_view AS
SELECT 
    d.id as debt_id,
    d.debtor_id,
    debtor.first_name || ' ' || debtor.last_name as debtor_name,
    debtor.email as debtor_email,
    d.creditor_id,
    creditor.first_name || ' ' || creditor.last_name as creditor_name,
    creditor.email as creditor_email,
    d.creditor_type,
    d.amount,
    d.expense_id,
    e.title as expense_title,
    e.amount as expense_amount,
    e.currency as expense_currency,
    e.nex_id,
    n.name as nex_name,
    d.payment_method,
    d.notes as debt_notes,
    d.settled_at,
    d.created_at as debt_created_at,
    d.modified_at as debt_modified_at,
    CASE 
        WHEN d.settled_at IS NOT NULL THEN true 
        ELSE false 
    END as is_settled,
    EXTRACT(EPOCH FROM (d.settled_at - d.created_at))/3600 as settlement_hours
FROM debts d
JOIN users debtor ON d.debtor_id = debtor.id
JOIN users creditor ON d.creditor_id = creditor.id
JOIN expenses e ON d.expense_id = e.id
JOIN nex n ON e.nex_id = n.id
WHERE d.is_deleted = false
  AND debtor.is_deleted = false
  AND creditor.is_deleted = false
  AND e.is_deleted = false
  AND n.is_deleted = false;

-- ========================================
-- EXPENSE SUMMARY VIEW
-- ========================================
-- Optimized view for expense analytics and reporting
-- Provides expense data with related entity information
CREATE VIEW expense_summary_view AS
SELECT 
    e.id as expense_id,
    e.title,
    e.amount,
    e.currency,
    e.category_id,
    c.name as category_name,
    e.description,
    e.nex_id,
    n.name as nex_name,
    e.created_by,
    creator.first_name || ' ' || creator.last_name as created_by_name,
    creator.email as created_by_email,
    e.payer_id,
    payer.first_name || ' ' || payer.last_name as payer_name,
    payer.email as payer_email,
    e.split_type,
    e.is_initial_payer_has,
    e.created_at,
    e.modified_at,
    COUNT(s.expense_id) as split_count,
    COUNT(d.id) as debt_count,
    COUNT(CASE WHEN d.settled_at IS NULL THEN 1 END) as unsettled_debt_count,
    COALESCE(SUM(CASE WHEN d.settled_at IS NULL THEN d.amount ELSE 0 END), 0) as unsettled_amount,
    CASE 
        WHEN COUNT(CASE WHEN d.settled_at IS NULL THEN 1 END) = 0 THEN true 
        ELSE false 
    END as is_fully_settled,
    COUNT(a.id) as attachment_count
FROM expenses e
LEFT JOIN categories c ON e.category_id = c.id AND c.is_deleted = false
LEFT JOIN nex n ON e.nex_id = n.id AND n.is_deleted = false
LEFT JOIN users creator ON e.created_by = creator.id AND creator.is_deleted = false
LEFT JOIN users payer ON e.payer_id = payer.id AND payer.is_deleted = false
LEFT JOIN splits s ON e.id = s.expense_id
LEFT JOIN debts d ON e.id = d.expense_id AND d.is_deleted = false
LEFT JOIN attachments a ON e.id = a.expense_id AND a.is_deleted = false
WHERE e.is_deleted = false
GROUP BY e.id, e.title, e.amount, e.currency, e.category_id, c.name, e.description,
         e.nex_id, n.name, e.created_by, creator.first_name, creator.last_name, creator.email,
         e.payer_id, payer.first_name, payer.last_name, payer.email, e.split_type,
         e.is_initial_payer_has, e.created_at, e.modified_at;

-- ========================================
-- USER BALANCE VIEW
-- ========================================
-- Comprehensive view for user balance calculations
-- Shows total debts, credits, and net balance for each user
CREATE VIEW user_balance_view AS
SELECT 
    u.id as user_id,
    u.first_name || ' ' || u.last_name as user_name,
    u.email as user_email,
    u.username,
    -- Total amount user owes to others
    COALESCE(SUM(CASE WHEN d.settled_at IS NULL THEN d.amount ELSE 0 END), 0) as total_debt,
    -- Total amount others owe to user
    COALESCE(SUM(CASE WHEN c.settled_at IS NULL THEN c.amount ELSE 0 END), 0) as total_credit,
    -- Net balance (positive = user is owed money, negative = user owes money)
    COALESCE(SUM(CASE WHEN c.settled_at IS NULL THEN c.amount ELSE 0 END), 0) - 
    COALESCE(SUM(CASE WHEN d.settled_at IS NULL THEN d.amount ELSE 0 END), 0) as net_balance,
    -- Count of active debts and credits
    COUNT(CASE WHEN d.settled_at IS NULL THEN 1 END) as active_debt_count,
    COUNT(CASE WHEN c.settled_at IS NULL THEN 1 END) as active_credit_count,
    -- Total expenses created by user
    COUNT(DISTINCT e.id) as total_expenses_created,
    -- Total amount of expenses created by user
    COALESCE(SUM(DISTINCT e.amount), 0) as total_expense_amount_created,
    -- Total expenses paid by user
    COUNT(DISTINCT ep.id) as total_expenses_paid,
    -- Total amount of expenses paid by user
    COALESCE(SUM(DISTINCT ep.amount), 0) as total_expense_amount_paid
FROM users u
LEFT JOIN debts d ON u.id = d.debtor_id AND d.is_deleted = false
LEFT JOIN debts c ON u.id = c.creditor_id AND c.is_deleted = false
LEFT JOIN expenses e ON u.id = e.created_by AND e.is_deleted = false
LEFT JOIN expenses ep ON u.id = ep.payer_id AND ep.is_deleted = false
WHERE u.is_deleted = false
GROUP BY u.id, u.first_name, u.last_name, u.email, u.username;

-- ========================================
-- NEX ANALYTICS VIEW
-- ========================================
-- Comprehensive analytics view for nex groups
-- Provides detailed statistics and insights for each nex
CREATE VIEW nex_analytics_view AS
SELECT 
    n.id as nex_id,
    n.name as nex_name,
    n.description,
    n.settlement_type,
    n.nex_type,
    n.is_archived,
    n.created_by,
    creator.first_name || ' ' || creator.last_name as creator_name,
    creator.email as creator_email,
    n.created_at as nex_created_at,
    n.modified_at as nex_modified_at,
    -- Member statistics
    COUNT(DISTINCT nm.user_id) as total_members,
    COUNT(DISTINCT CASE WHEN nm.status = 'ACTIVE' THEN nm.user_id END) as active_members,
    COUNT(DISTINCT CASE WHEN nm.role = 'ADMIN' THEN nm.user_id END) as admin_count,
    -- Expense statistics
    COUNT(DISTINCT e.id) as total_expenses,
    COALESCE(SUM(e.amount), 0) as total_expense_amount,
    COALESCE(AVG(e.amount), 0) as average_expense_amount,
    COALESCE(MAX(e.amount), 0) as max_expense_amount,
    COALESCE(MIN(e.amount), 0) as min_expense_amount,
    -- Debt statistics
    COUNT(DISTINCT d.id) as total_debts,
    COUNT(DISTINCT CASE WHEN d.settled_at IS NULL THEN d.id END) as unsettled_debts,
    COALESCE(SUM(CASE WHEN d.settled_at IS NULL THEN d.amount ELSE 0 END), 0) as unsettled_debt_amount,
    -- Category statistics
    COUNT(DISTINCT c.id) as total_categories,
    COUNT(DISTINCT CASE WHEN c.is_default = true THEN c.id END) as default_categories,
    -- Bill statistics
    COUNT(DISTINCT b.id) as total_bills,
    COUNT(DISTINCT CASE WHEN b.is_paid = true THEN b.id END) as paid_bills,
    COALESCE(SUM(b.amount), 0) as total_bill_amount,
    -- Recent activity
    MAX(e.created_at) as last_expense_date,
    MAX(d.created_at) as last_debt_date,
    MAX(b.created_at) as last_bill_date
FROM nex n
LEFT JOIN users creator ON n.created_by = creator.id AND creator.is_deleted = false
LEFT JOIN nex_members nm ON n.id = nm.nex_id AND nm.is_deleted = false
LEFT JOIN expenses e ON n.id = e.nex_id AND e.is_deleted = false
LEFT JOIN debts d ON e.id = d.expense_id AND d.is_deleted = false
LEFT JOIN categories c ON n.id = c.nex_id AND c.is_deleted = false
LEFT JOIN bills b ON n.id = b.nex_id AND b.is_deleted = false
WHERE n.is_deleted = false
GROUP BY n.id, n.name, n.description, n.settlement_type, n.nex_type, n.is_archived,
         n.created_by, creator.first_name, creator.last_name, creator.email,
         n.created_at, n.modified_at;

-- ========================================
-- ATTACHMENT SUMMARY VIEW
-- ========================================
-- Optimized view for attachment management and file operations
-- Provides attachment data with expense and user information
CREATE VIEW attachment_summary_view AS
SELECT 
    a.id as attachment_id,
    a.expense_id,
    e.title as expense_title,
    e.amount as expense_amount,
    e.currency as expense_currency,
    a.file_url,
    a.file_type,
    a.uploaded_by,
    uploader.first_name || ' ' || uploader.last_name as uploader_name,
    uploader.email as uploader_email,
    a.created_at,
    -- File metadata
    CASE 
        WHEN a.file_type IN ('jpg', 'jpeg', 'png', 'gif', 'webp') THEN 'image'
        WHEN a.file_type IN ('pdf', 'doc', 'docx', 'txt', 'xls', 'xlsx') THEN 'document'
        ELSE 'other'
    END as file_category,
    -- File size estimation (placeholder - would need actual file size in real implementation)
    CASE 
        WHEN a.file_type IN ('jpg', 'jpeg', 'png', 'gif', 'webp') THEN 'image'
        WHEN a.file_type IN ('pdf', 'doc', 'docx', 'txt', 'xls', 'xlsx') THEN 'document'
        ELSE 'other'
    END as estimated_file_type,
    -- Related entity information
    e.nex_id,
    n.name as nex_name,
    e.category_id,
    c.name as category_name
FROM attachments a
JOIN expenses e ON a.expense_id = e.id AND e.is_deleted = false
JOIN users uploader ON a.uploaded_by = uploader.id AND uploader.is_deleted = false
LEFT JOIN nex n ON e.nex_id = n.id AND n.is_deleted = false
LEFT JOIN categories c ON e.category_id = c.id AND c.is_deleted = false
WHERE a.is_deleted = false;

-- ========================================
-- PERFORMANCE INDEXES FOR VIEWS
-- ========================================

-- Indexes for settlement_history_view
CREATE INDEX idx_settlement_history_debtor_id ON debts(debtor_id) WHERE is_deleted = false;
CREATE INDEX idx_settlement_history_creditor_id ON debts(creditor_id) WHERE is_deleted = false;
CREATE INDEX idx_settlement_history_expense_id ON debts(expense_id) WHERE is_deleted = false;
CREATE INDEX idx_settlement_history_settled_at ON debts(settled_at) WHERE is_deleted = false;

-- Indexes for expense_summary_view
CREATE INDEX idx_expense_summary_nex_id ON expenses(nex_id) WHERE is_deleted = false;
CREATE INDEX idx_expense_summary_category_id ON expenses(category_id) WHERE is_deleted = false;
CREATE INDEX idx_expense_summary_created_by ON expenses(created_by) WHERE is_deleted = false;
CREATE INDEX idx_expense_summary_payer_id ON expenses(payer_id) WHERE is_deleted = false;

-- Indexes for user_balance_view
CREATE INDEX idx_user_balance_debtor_id ON debts(debtor_id) WHERE is_deleted = false AND settled_at IS NULL;
CREATE INDEX idx_user_balance_creditor_id ON debts(creditor_id) WHERE is_deleted = false AND settled_at IS NULL;

-- Indexes for nex_analytics_view
CREATE INDEX idx_nex_analytics_created_by ON nex(created_by) WHERE is_deleted = false;
CREATE INDEX idx_nex_analytics_is_archived ON nex(is_archived) WHERE is_deleted = false;

-- Indexes for attachment_summary_view
CREATE INDEX idx_attachment_summary_expense_id ON attachments(expense_id) WHERE is_deleted = false;
CREATE INDEX idx_attachment_summary_uploaded_by ON attachments(uploaded_by) WHERE is_deleted = false;
CREATE INDEX idx_attachment_summary_file_type ON attachments(file_type) WHERE is_deleted = false;

-- ========================================
-- VIEW COMMENTS FOR DOCUMENTATION
-- ========================================

COMMENT ON VIEW settlement_history_view IS 'Primary view for debt settlement tracking and analytics. Provides comprehensive settlement information with user details.';
COMMENT ON VIEW expense_summary_view IS 'Optimized view for expense analytics and reporting. Provides expense data with related entity information.';
COMMENT ON VIEW user_balance_view IS 'Comprehensive view for user balance calculations. Shows total debts, credits, and net balance for each user.';
COMMENT ON VIEW nex_analytics_view IS 'Comprehensive analytics view for nex groups. Provides detailed statistics and insights for each nex.';
COMMENT ON VIEW attachment_summary_view IS 'Optimized view for attachment management and file operations. Provides attachment data with expense and user information.';
