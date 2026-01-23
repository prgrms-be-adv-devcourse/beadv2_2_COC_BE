CREATE SCHEMA IF NOT EXISTS support;

CREATE TABLE IF NOT EXISTS support.shedlock (
    name VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
