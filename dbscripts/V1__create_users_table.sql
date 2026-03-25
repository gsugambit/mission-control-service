CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    date_created TIMESTAMPTZ NOT NULL,
    date_modified TIMESTAMPTZ NOT NULL
);
