-- src/test/resources/test-data.sql

-- Очистка таблиц перед тестами
DELETE FROM transactions;
DELETE FROM products;
DELETE FROM users;

-- Вставка тестовых пользователей
INSERT INTO users (id, name, email) VALUES
('cd515076-5d8a-44be-930e-8d4fcb79f42d', 'Test User 1', 'test1@bank.com'),
('d4a4d619-9a0c-4fc5-b0cb-76c49409546b', 'Test User 2', 'test2@bank.com');

-- Вставка тестовых продуктов
INSERT INTO products (id, name, type, description) VALUES
('product-debit-1', 'Debit Card', 'DEBIT', 'Test debit product'),
('product-saving-1', 'Saving Account', 'SAVING', 'Test saving product');

-- Вставка тестовых транзакций
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type, transaction_date) VALUES
('txn-1', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', 'product-debit-1', 1000.00, 'DEPOSIT', CURRENT_TIMESTAMP),
('txn-2', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', 'product-saving-1', 5000.00, 'DEPOSIT', CURRENT_TIMESTAMP);