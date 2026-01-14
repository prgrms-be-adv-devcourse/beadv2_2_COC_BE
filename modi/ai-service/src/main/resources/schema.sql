CREATE SCHEMA IF NOT EXISTS ai;
CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA ai;

CREATE TABLE IF NOT EXISTS ai.product_embedding (
    product_id BIGINT PRIMARY KEY,
    name TEXT,
    description TEXT,
    category TEXT,
    specs JSONB,
    status TEXT,
    embedding vector(1536)
);

CREATE INDEX IF NOT EXISTS product_embedding_idx
    ON ai.product_embedding USING ivfflat (embedding vector_l2_ops)
    WITH (lists = 100);
