CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username_lower
    ON users (LOWER(username));

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower
    ON users (LOWER(email));
