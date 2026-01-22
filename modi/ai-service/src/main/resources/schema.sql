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

CREATE TABLE IF NOT EXISTS ai.product_moderation_result (
    id UUID PRIMARY KEY,
    product_id BIGINT NOT NULL,
    decision TEXT NOT NULL,
    score DOUBLE PRECISION,
    reasons TEXT,
    message TEXT,
    request_event_id TEXT,
    source TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS ai.product_moderation_outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(30) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL,
    processed_at TIMESTAMP NULL,
    last_error VARCHAR(500),
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_moderation_outbox_status_created
    ON ai.product_moderation_outbox (status, created_at);
