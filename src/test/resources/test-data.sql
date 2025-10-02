-- src/test/resources/test-data.sql
-- Очистка таблиц перед тестами
DELETE FROM transactions;
DELETE FROM products;
DELETE FROM users;

-- Вставка тестовых пользователей
INSERT INTO users (id, name, email, created_at) VALUES
('cd515076-5d8a-44be-930e-8d4fcb79f42d', 'Invest User', 'invest@bank.com', CURRENT_TIMESTAMP),
('d4a4d619-9a0c-4fc5-b0cb-76c49409546b', 'Saving User', 'saving@bank.com', CURRENT_TIMESTAMP),
('1f9b149c-6577-448a-bc94-16bea229b71a', 'Credit User', 'credit@bank.com', CURRENT_TIMESTAMP),
('a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6', 'No Recommendations User', 'none@bank.com', CURRENT_TIMESTAMP);

-- Вставка тестовых продуктов
INSERT INTO products (id, name, type, description, created_at) VALUES
('147f6a0f-3b91-413b-ab99-87f081d60d5a', 'Debit Card Basic', 'DEBIT', 'Базовая дебетовая карта', CURRENT_TIMESTAMP),
('59efc529-2fff-41af-baff-90ccd7402925', 'Premium Saving', 'SAVING', 'Премиальный сберегательный счет', CURRENT_TIMESTAMP),
('ab138afb-f3ba-4a93-b74f-0fcee86d447f', 'Simple Credit', 'CREDIT', 'Простой кредитный продукт', CURRENT_TIMESTAMP),
('d87f6a0f-3b91-413b-ab99-87f081d60d5b', 'Invest Portfolio', 'INVEST', 'Инвестиционный портфель', CURRENT_TIMESTAMP);

-- Вставка тестовых транзакций

-- Пользователь 1: Подходит ТОЛЬКО для Invest 500
-- (имеет DEBIT, нет INVEST, SAVING > 1000)
INSERT INTO transactions (id, user_id, product_id, amount, type, transaction_date) VALUES
('txn-1-1', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 20000.00, 'DEPOSIT', CURRENT_TIMESTAMP),
('txn-1-2', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 15000.00, 'WITHDRAWAL', CURRENT_TIMESTAMP),
('txn-1-3', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '59efc529-2fff-41af-baff-90ccd7402925', 1500.00, 'DEPOSIT', CURRENT_TIMESTAMP);

-- Пользователь 2: Подходит ТОЛЬКО для Top Saving
-- (имеет DEBIT, депозиты >= 50000, баланс положительный, но ЕСТЬ INVEST)
INSERT INTO transactions (id, user_id, product_id, amount, type, transaction_date) VALUES
('txn-2-1', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 60000.00, 'DEPOSIT', CURRENT_TIMESTAMP),
('txn-2-2', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 10000.00, 'WITHDRAWAL', CURRENT_TIMESTAMP),
('txn-2-3', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', 'd87f6a0f-3b91-413b-ab99-87f081d60d5b', 5000.00, 'DEPOSIT', CURRENT_TIMESTAMP);

-- Пользователь 3: Подходит ТОЛЬКО для Простого кредита
-- (нет CREDIT, траты > 100000, баланс положительный, депозиты < 50000 - не подходит для Top Saving)
INSERT INTO transactions (id, user_id, product_id, amount, type, transaction_date) VALUES
('txn-3-1', '1f9b149c-6577-448a-bc94-16bea229b71a', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 160000.00, 'DEPOSIT', CURRENT_TIMESTAMP),  -- DEBIT депозит > трат (положительный баланс)
('txn-3-2', '1f9b149c-6577-448a-bc94-16bea229b71a', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 150000.00, 'WITHDRAWAL', CURRENT_TIMESTAMP), -- DEBIT траты > 100000
('txn-3-3', '1f9b149c-6577-448a-bc94-16bea229b71a', '59efc529-2fff-41af-baff-90ccd7402925', 500.00, 'DEPOSIT', CURRENT_TIMESTAMP); -- SAVING депозит < 1000

-- Пользователь 4: Не подходит ни для чего
INSERT INTO transactions (id, user_id, product_id, amount, type, transaction_date) VALUES
('txn-4-1', 'a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 1000.00, 'DEPOSIT', CURRENT_TIMESTAMP);