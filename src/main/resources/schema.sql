-- src/main/resources/schema.sql

-- Обновленная схема с правильными типами для H2
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('DEBIT', 'CREDIT', 'SAVING', 'INVEST')),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAW')),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS dynamic_rules (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Обновленная таблица rule_queries
CREATE TABLE IF NOT EXISTS rule_queries (
    id VARCHAR(36) PRIMARY KEY,
    rule_id VARCHAR(36) NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    query_value VARCHAR(255),
    is_negated BOOLEAN NOT NULL DEFAULT FALSE,  -- ДОБАВИТЬ ЭТУ СТРОКУ!
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES dynamic_rules(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rule_statistics (
    rule_id VARCHAR(36) PRIMARY KEY,
    execution_count BIGINT NOT NULL DEFAULT 0
);