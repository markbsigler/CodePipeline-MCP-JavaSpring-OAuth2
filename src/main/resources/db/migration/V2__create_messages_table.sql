-- Flyway migration for messages table
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    content VARCHAR(255) NOT NULL CHECK (length(trim(content)) > 0),
    sender VARCHAR(255) NOT NULL CHECK (length(trim(sender)) > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
