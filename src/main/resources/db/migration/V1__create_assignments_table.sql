-- Flyway migration: Create assignments table

CREATE TABLE assignments (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    status VARCHAR(50),
    owner VARCHAR(255),
    due_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);