CREATE TABLE users (
                       id CHAR(36) PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255),
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       username VARCHAR(255) NOT NULL,
                       contact_number VARCHAR(20) NOT NULL,
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
                     modified_at TIMESTAMP NOT NULL
);

CREATE TABLE nex_members (
                             nex_id CHAR(36),
                             user_id CHAR(36),
                             role TEXT CHECK (role IN ('ADMIN', 'MEMBER')) NOT NULL,
                             invited_at TIMESTAMP,
                             joined_at TIMESTAMP,
                             status TEXT CHECK (status IN ('PENDING', 'ACTIVE', 'LEFT')) DEFAULT 'ACTIVE',
                             created_at TIMESTAMP NOT NULL,
                             modified_at TIMESTAMP NOT NULL,
                             PRIMARY KEY (nex_id, user_id)
);

CREATE TABLE categories (
                            id CHAR(36) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            created_by CHAR(36) NOT NULL,
                            nex_id CHAR(36),
                            is_default BOOLEAN DEFAULT FALSE,
                            created_at TIMESTAMP NOT NULL,
                            modified_at TIMESTAMP NOT NULL
);

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
                          modified_at TIMESTAMP NOT NULL
);

CREATE TABLE splits (
                        expense_id CHAR(36),
                        user_id CHAR(36),
                        percentage DECIMAL(5,2),
                        amount DECIMAL(10,2),
                        notes TEXT,
                        created_at TIMESTAMP NOT NULL,
                        modified_at TIMESTAMP NOT NULL,
                        PRIMARY KEY (expense_id, user_id)
);

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
                       modified_at TIMESTAMP NOT NULL
);

CREATE TABLE attachments (
                             id CHAR(36) PRIMARY KEY,
                             expense_id CHAR(36) NOT NULL,
                             file_url VARCHAR(500) NOT NULL,
                             file_type VARCHAR(50),
                             uploaded_by CHAR(36) NOT NULL,
                             created_at TIMESTAMP NOT NULL
);

CREATE TABLE notifications (
                               id CHAR(36) PRIMARY KEY,
                               user_id CHAR(36) NOT NULL,
                               nex_id CHAR(36),
                               type TEXT CHECK (type IN ('INVITE', 'REMINDER', 'INFO')),
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP NOT NULL
);

CREATE TABLE activity_logs (
                               id CHAR(36) PRIMARY KEY,
                               user_id CHAR(36),
                               action VARCHAR(255) NOT NULL,
                               target_table VARCHAR(50),
                               target_id CHAR(36),
                               description TEXT,
                               created_at TIMESTAMP NOT NULL
);

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
                       modified_at TIMESTAMP NOT NULL
);

CREATE TABLE bill_participants (
                                   bill_id CHAR(36),
                                   user_id CHAR(36),
                                   share_amount DECIMAL(10,2),
                                   paid BOOLEAN DEFAULT FALSE,
                                   paid_at TIMESTAMP,
                                   created_at TIMESTAMP NOT NULL,
                                   PRIMARY KEY (bill_id, user_id)
);

ALTER TABLE nex
    ADD CONSTRAINT nex_created_by FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE nex_members
    ADD CONSTRAINT nex_members_nex FOREIGN KEY (nex_id) REFERENCES nex(id),
    ADD CONSTRAINT nex_members_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE categories
    ADD CONSTRAINT category_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    ADD CONSTRAINT category_nex FOREIGN KEY (nex_id) REFERENCES nex(id);

ALTER TABLE expenses
    ADD CONSTRAINT expenses_category FOREIGN KEY (category_id) REFERENCES categories(id),
    ADD CONSTRAINT expenses_nex FOREIGN KEY (nex_id) REFERENCES nex(id),
    ADD CONSTRAINT expenses_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    ADD CONSTRAINT expenses_payer FOREIGN KEY (payer_id) REFERENCES users(id);

ALTER TABLE splits
    ADD CONSTRAINT splits_expense FOREIGN KEY (expense_id) REFERENCES expenses(id),
    ADD CONSTRAINT splits_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE debts
    ADD CONSTRAINT debts_debtor FOREIGN KEY (debtor_id) REFERENCES users(id),
    ADD CONSTRAINT debts_creditor_user FOREIGN KEY (creditor_id) REFERENCES users(id),
    ADD CONSTRAINT debts_creditor_expense FOREIGN KEY (creditor_id) REFERENCES expenses(id),
    ADD CONSTRAINT debts_expense FOREIGN KEY (expense_id) REFERENCES expenses(id);

ALTER TABLE attachments
    ADD CONSTRAINT attachments_expense FOREIGN KEY (expense_id) REFERENCES expenses(id),
    ADD CONSTRAINT attachments_user FOREIGN KEY (uploaded_by) REFERENCES users(id);

ALTER TABLE notifications
    ADD CONSTRAINT notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT notifications_nex FOREIGN KEY (nex_id) REFERENCES nex(id);

ALTER TABLE activity_logs
    ADD CONSTRAINT activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE bills
    ADD CONSTRAINT bills_nex FOREIGN KEY (nex_id) REFERENCES nex(id),
    ADD CONSTRAINT bills_created_by FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE bill_participants
    ADD CONSTRAINT bill_participants_bill FOREIGN KEY (bill_id) REFERENCES bills(id),
    ADD CONSTRAINT bill_participants_user FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

CREATE INDEX idx_nex_created_by ON nex(created_by);
CREATE INDEX idx_nex_settlement_type ON nex(settlement_type);
CREATE INDEX idx_nex_nex_type ON nex(nex_type);
CREATE INDEX idx_nex_is_archived ON nex(is_archived);

CREATE INDEX idx_nex_members_nex_id_user_id ON nex_members(nex_id, user_id);
CREATE INDEX idx_nex_members_status ON nex_members(status);

CREATE INDEX idx_categories_nex_id ON categories(nex_id);
CREATE INDEX idx_categories_created_by ON categories(created_by);
CREATE INDEX idx_categories_is_default ON categories(is_default);

CREATE INDEX idx_expenses_nex_id ON expenses(nex_id);
CREATE INDEX idx_expenses_category_id ON expenses(category_id);
CREATE INDEX idx_expenses_created_by ON expenses(created_by);
CREATE INDEX idx_expenses_payer_id ON expenses(payer_id);
CREATE INDEX idx_expenses_split_type ON expenses(split_type);
CREATE INDEX idx_expenses_is_initial_payer_has ON expenses(is_initial_payer_has);

CREATE INDEX idx_splits_expense_id_user_id ON splits(expense_id, user_id);

CREATE INDEX idx_debts_debtor_id ON debts(debtor_id);
CREATE INDEX idx_debts_creditor_id ON debts(creditor_id);
CREATE INDEX idx_debts_creditor_type ON debts(creditor_type);
CREATE INDEX idx_debts_expense_id ON debts(expense_id);

CREATE INDEX idx_attachments_expense_id ON attachments(expense_id);
CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_nex_id ON notifications(nex_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_target ON activity_logs(target_table, target_id);

CREATE INDEX idx_bills_nex_id ON bills(nex_id);
CREATE INDEX idx_bills_created_by ON bills(created_by);
CREATE INDEX idx_bills_is_recurring ON bills(is_recurring);
CREATE INDEX idx_bills_is_paid ON bills(is_paid);

CREATE INDEX idx_bill_participants_bill_id_user_id ON bill_participants(bill_id, user_id);
CREATE INDEX idx_bill_participants_paid ON bill_participants(paid);