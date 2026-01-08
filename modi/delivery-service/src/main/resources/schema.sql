CREATE SCHEMA IF NOT EXISTS delivery;

CREATE TABLE IF NOT EXISTS delivery.shedlock (
    name VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
