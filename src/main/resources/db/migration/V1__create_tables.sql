-- Users table
CREATE TABLE users (
                       id CHAR(36) PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255), -- Nullable for OAuth users
                       name VARCHAR(255) NOT NULL,
                       contact_number VARCHAR(20) NOT NULL, -- Changed to VARCHAR for flexibility
                       role ENUM('USER', 'ADMIN') NOT NULL,
                       created_at DATETIME NOT NULL
);

-- Groups table
CREATE TABLE `groups` (
                          id CHAR(36) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          created_by CHAR(36) NOT NULL,
                          created_at DATETIME NOT NULL,
                          FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Group_Members junction table
CREATE TABLE group_members (
                               group_id CHAR(36),
                               user_id CHAR(36),
                               PRIMARY KEY (group_id, user_id),
                               FOREIGN KEY (group_id) REFERENCES `groups`(id),
                               FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Categories table
CREATE TABLE categories (
                            id CHAR(36) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            created_by CHAR(36) NOT NULL,
                            FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Recurring_Rules table
CREATE TABLE recurring_rules (
                                 id CHAR(36) PRIMARY KEY,
                                 expense_id CHAR(36), -- Nullable for template rules
                                 frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
                                 day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'),
                                 day_of_month INT CHECK (day_of_month BETWEEN 1 AND 31),
                                 month_of_year INT CHECK (month_of_year BETWEEN 1 AND 12),
                                 default_amount DECIMAL(10,2) NOT NULL,
                                 created_at DATETIME NOT NULL,
                                 FOREIGN KEY (expense_id) REFERENCES expenses(id)
);

-- Expenses table
CREATE TABLE expenses (
                          id CHAR(36) PRIMARY KEY,
                          amount DECIMAL(10,2) NOT NULL,
                          category VARCHAR(255) NOT NULL,
                          description TEXT,
                          group_id CHAR(36) NOT NULL, -- Enforced non-nullable for group requirement
                          user_id CHAR(36) NOT NULL,
                          created_at DATETIME NOT NULL,
                          recurring_rule_id CHAR(36), -- Link to recurrence rule
                          is_recurring_instance BOOLEAN DEFAULT FALSE, -- Track recurring instances
                          FOREIGN KEY (group_id) REFERENCES `groups`(id),
                          FOREIGN KEY (user_id) REFERENCES users(id),
                          FOREIGN KEY (recurring_rule_id) REFERENCES recurring_rules(id)
);

-- Debts table
CREATE TABLE debts (
                       id CHAR(36) PRIMARY KEY,
                       debtor_id CHAR(36) NOT NULL,
                       creditor_id CHAR(36) NOT NULL,
                       amount DECIMAL(10,2) NOT NULL,
                       expense_id CHAR(36) NOT NULL,
                       settled_at DATETIME,
                       FOREIGN KEY (debtor_id) REFERENCES users(id),
                       FOREIGN KEY (creditor_id) REFERENCES users(id),
                       FOREIGN KEY (expense_id) REFERENCES expenses(id)
);

-- Indexes for performance
CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_group_id ON expenses(group_id);
CREATE INDEX idx_expenses_recurring_rule_id ON expenses(recurring_rule_id);
CREATE INDEX idx_debts_debtor_id ON debts(debtor_id);
CREATE INDEX idx_debts_creditor_id ON debts(creditor_id);
CREATE INDEX idx_recurring_rules_expense_id ON recurring_rules(expense_id);














# -- Users table
# CREATE TABLE users (
#         id CHAR(36) PRIMARY KEY,
#         email VARCHAR(255) UNIQUE NOT NULL,
#         password VARCHAR(255),
#         name VARCHAR(255) NOT NULL,
#         contact_number int NOT NULL,
#         role ENUM('USER', 'ADMIN') NOT NULL,
#         created_at DATETIME NOT NULL
# );
#
# -- Groups table
# CREATE TABLE `groups` (
#         id CHAR(36) PRIMARY KEY,
#         name VARCHAR(255) NOT NULL,
#         created_by CHAR(36) NOT NULL,
#         created_at DATETIME NOT NULL,
#         FOREIGN KEY (created_by) REFERENCES users(id)
# );
#
# -- Group_Members junction table
# CREATE TABLE group_members (
#         group_id CHAR(36),
#         user_id CHAR(36),
#         PRIMARY KEY (group_id, user_id),
#         FOREIGN KEY (group_id) REFERENCES `groups`(id),
#         FOREIGN KEY (user_id) REFERENCES users(id)
# );
#
# -- Categories table
# CREATE TABLE categories (
#         id CHAR(36) PRIMARY KEY,
#         name VARCHAR(255) NOT NULL,
#         created_by CHAR(36) NOT NULL,
#         FOREIGN KEY (created_by) REFERENCES users(id)
# );
#
# -- Expenses table
# CREATE TABLE expenses (
#         id CHAR(36) PRIMARY KEY,
#         amount DECIMAL(10,2) NOT NULL,
#         category VARCHAR(255) NOT NULL,
#         description TEXT,
#         group_id CHAR(36),
#         user_id CHAR(36) NOT NULL,
#         created_at DATETIME NOT NULL,
#         FOREIGN KEY (group_id) REFERENCES `groups`(id),
#         FOREIGN KEY (user_id) REFERENCES users(id)
# );
#
# -- Debts table
# CREATE TABLE debts (
#         id CHAR(36) PRIMARY KEY,
#         debtor_id CHAR(36) NOT NULL,
#         creditor_id CHAR(36) NOT NULL,
#         amount DECIMAL(10,2) NOT NULL,
#         expense_id CHAR(36) NOT NULL,
#         settled_at DATETIME,
#         FOREIGN KEY (debtor_id) REFERENCES users(id),
#         FOREIGN KEY (creditor_id) REFERENCES users(id),
#         FOREIGN KEY (expense_id) REFERENCES expenses(id)
# );
#
# -- Indexes for performance
# CREATE INDEX idx_expenses_user_id ON expenses(user_id);
# CREATE INDEX idx_expenses_group_id ON expenses(group_id);
# CREATE INDEX idx_debts_debtor_id ON debts(debtor_id);
# CREATE INDEX idx_debts_creditor_id ON debts(creditor_id);