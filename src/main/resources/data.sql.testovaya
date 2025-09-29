-- Очистка старых данных
DELETE FROM transactions;
DELETE FROM products;

-- Вставка продуктов
INSERT INTO products (id, name, type) VALUES
('11111111-1111-1111-1111-111111111111', 'Дебетовая карта', 'DEBIT'),
('22222222-2222-2222-2222-222222222222', 'Накопительный счет', 'SAVING'),
('33333333-3333-3333-3333-333333333333', 'Инвестиционный счет', 'INVEST'),
('44444444-4444-4444-4444-444444444444', 'Кредитная карта', 'CREDIT');

-- Транзакции для пользователя cd515076-5d8a-44be-930e-8d4fcb79f42d (Invest 500)
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '11111111-1111-1111-1111-111111111111', 5000.00, 'DEPOSIT'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '22222222-2222-2222-2222-222222222222', 1500.00, 'DEPOSIT'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'cd515076-5d8a-44be-930e-8d4fcb79f42d', '11111111-1111-1111-1111-111111111111', 2000.00, 'WITHDRAWAL');

-- Транзакции для пользователя d4a4d619-9a0c-4fc5-b0cb-76c49409546b (Top Saving)
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type) VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', '11111111-1111-1111-1111-111111111111', 60000.00, 'DEPOSIT'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'd4a4d619-9a0c-4fc5-b0cb-76c49409546b', '11111111-1111-1111-1111-111111111111', 40000.00, 'WITHDRAWAL');

-- Транзакции для пользователя 1f9b149c-6577-448a-bc94-16bea229b71a (Простой кредит)
INSERT INTO transactions (id, user_id, product_id, amount, transaction_type) VALUES
('ffffffff-ffff-ffff-ffff-ffffffffffff', '1f9b149c-6577-448a-bc94-16bea229b71a', '11111111-1111-1111-1111-111111111111', 120000.00, 'WITHDRAWAL'),
('gggggggg-gggg-gggg-gggg-gggggggggggg', '1f9b149c-6577-448a-bc94-16bea229b71a', '11111111-1111-1111-1111-111111111111', 130000.00, 'DEPOSIT');