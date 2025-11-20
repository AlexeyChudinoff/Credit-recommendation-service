-- Таблица для статистики правил
CREATE TABLE IF NOT EXISTS rule_statistics (
    rule_id UUID NOT NULL PRIMARY KEY,
    execution_count BIGINT NOT NULL DEFAULT 0
);