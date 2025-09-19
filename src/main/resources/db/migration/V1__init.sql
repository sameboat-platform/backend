-- Enable UUIDs if needed
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
                                     id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    role         TEXT NOT NULL,              -- 'peer' | 'mentor' | 'professional'
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS stories (
                                       id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id  UUID NOT NULL REFERENCES users(id),
    title      TEXT NOT NULL,
    content    TEXT NOT NULL,
    triggers   JSONB DEFAULT '[]'::jsonb,    -- array of trigger tags
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS trust_events (
                                            id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id),
    event_type TEXT NOT NULL,                -- 'verification' | 'upvote' | 'report' ...
    weight     INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );