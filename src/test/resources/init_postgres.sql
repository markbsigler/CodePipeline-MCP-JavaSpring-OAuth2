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
