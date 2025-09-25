-- src/test/resources/test-data.sql

-- Очистка таблиц перед тестами (если нужно)
DELETE FROM transactions;
DELETE FROM products;
DELETE FROM users;

-- Вставка тестовых пользователей (соответствует реальной схеме)
INSERT INTO users (id, name, email, created_at) VALUES
('cd515076-5d8a-44be-930e-8d4fcb79f42d', 'Invest User', 'invest@bank.com', CURRENT_TIMESTAMP),
('d4a4d619-9a0c-4fc5-b0cb-76c49409546b', 'Saving User', 'saving@bank.com', CURRENT_TIMESTAMP),
('1f9b149c-6577-448a-bc94-16bea229b71a', 'Credit User', 'credit@bank.com', CURRENT_TIMESTAMP);

-- Вставка тестовых продуктов (ВАЖНО: id как UUID, type соответствует ProductType enum)
INSERT INTO products (id, name, type, description, created_at) VALUES
('147f6a0f-3b91-413b-ab99-87f081d60d5a', 'Debit Card Basic', 'DEBIT', 'Базовая дебетовая карта', CURRENT_TIMESTAMP),
('59efc529-2fff-41af-baff-90ccd7402925', 'Premium Saving', 'SAVING', 'Премиальный сберегательный счет', CURRENT_TIMESTAMP),
('ab138afb-f3ba-4a93-b74f-0fcee86d447f', 'Simple Credit', 'CREDIT', 'Простой кредитный продукт', CURRENT_TIMESTAMP),
('d87f6a0f-3b91-413b-ab99-87f081d60d5b', 'Invest Portfolio', 'INVEST', 'Инвестиционный портфель', CURRENT_TIMESTAMP);

-- Вставка тестовых транзакций для проверки бизнес-логики

-- Пользователь 1: Подходит для Invest 500 (имеет DEBIT, нет INVEST, SAVING > 1000)
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type, transaction_date) VALUES
('txn-1-1', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 50000.00, 'DEPOSIT', CURRENT_TIMESTAMP),  -- DEBIT депозит
('txn-1-2', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '59efc529-2fff-41af-baff-90ccd7402925', 1500.00, 'DEPOSIT', CURRENT_TIMESTAMP); -- SAVING депозит > 1000

-- Пользователь 2: Подходит для Top Saving (имеет DEBIT, депозиты >= 50000)
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type, transaction_date) VALUES
('txn-2-1', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 60000.00, 'DEPOSIT', CURRENT_TIMESTAMP),  -- DEBIT депозит >= 50000
('txn-2-2', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 10000.00, 'WITHDRAWAL', CURRENT_TIMESTAMP); -- DEBIT траты < депозитов

-- Пользователь 3: Подходит для Простого кредита (нет CREDIT, траты > 100000)
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type, transaction_date) VALUES
('txn-3-1', '1f9b149c-6577-448a-bc94-16bea229b71a', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 200000.00, 'DEPOSIT', CURRENT_TIMESTAMP),  -- DEBIT депозит
('txn-3-2', '1f9b149c-6577-448a-bc94-16bea229b71a', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 150000.00, 'WITHDRAWAL', CURRENT_TIMESTAMP); -- DEBIT траты > 100000

-- Пользователь 4: Не подходит ни для чего (для негативных тестов)
INSERT INTO users (id, name, email, created_at) VALUES
('a1b2c3d4-5e6f-7g8h-9i0j-k1l2m3n4o5p6', 'No Recommendations User', 'none@bank.com', CURRENT_TIMESTAMP);

INSERT INTO transactions (id, user_id, product_id, amount, transaction_type, transaction_date) VALUES
('txn-4-1', 'a1b2c3d4-5e6f-7g8h-9i0j-k1l2m3n4o5p6', '147f6a0f-3b91-413b-ab99-87f081d60d5a', 1000.00, 'DEPOSIT', CURRENT_TIMESTAMP);