-- V2__users_and_sessions.sql
-- Creates users & sessions tables for opaque cookie sessions.


CREATE EXTENSION IF NOT EXISTS pgcrypto; -- for gen_random_uuid()


-- USERS
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    bio TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT users_email_len CHECK (char_length(email) BETWEEN 3 AND 254),
    CONSTRAINT users_display_name_len CHECK (char_length(display_name) BETWEEN 2 AND 50),
    CONSTRAINT users_bio_len CHECK (bio IS NULL OR char_length(bio) <= 500)
    );


-- Enforce case-insensitive uniqueness for email
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_uidx ON users (lower(email));


-- SESSIONS
CREATE TABLE IF NOT EXISTS sessions (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
    );


CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON sessions(expires_at);


-- Trigger to maintain updated_at on users
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();