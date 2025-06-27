-- Create the messages table if it doesn't exist
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT NOT NULL,
    sender VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT
);

-- Create an index on the sender column for better query performance
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender);

-- Create the assignments table if it doesn't exist
CREATE TABLE IF NOT EXISTS assignments (
    id VARCHAR(255) PRIMARY KEY,
    assignment_id VARCHAR(255) NOT NULL UNIQUE,
    srid VARCHAR(255) NOT NULL,
    application VARCHAR(255),
    stream VARCHAR(255),
    owner VARCHAR(255),
    status VARCHAR(255),
    release_id VARCHAR(255),
    setid VARCHAR(255),
    level VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
