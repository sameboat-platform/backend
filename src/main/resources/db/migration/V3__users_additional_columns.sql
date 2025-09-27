-- V3__users_additional_columns.sql
-- Brings legacy users table (from V1) up to the schema expected by V2 design
-- WITHOUT modifying earlier migration files (immutability rule).
-- Adds missing columns + constraints + trigger if they do not already exist.

-- Add columns (idempotent)
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Backfill password_hash for existing rows if NULL (dev stub)
UPDATE users SET password_hash = 'DEV-STUB' WHERE password_hash IS NULL;
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;

-- Constraints (conditionally create)
DO $$ BEGIN
    ALTER TABLE users ADD CONSTRAINT users_email_len CHECK (char_length(email) BETWEEN 3 AND 254);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
    ALTER TABLE users ADD CONSTRAINT users_display_name_len CHECK (char_length(display_name) BETWEEN 2 AND 50);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
    ALTER TABLE users ADD CONSTRAINT users_bio_len CHECK (bio IS NULL OR char_length(bio) <= 500);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Unique index for case-insensitive email lookup
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_uidx ON users (lower(email));

-- Trigger function & trigger for updated_at maintenance
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
    CREATE TRIGGER trg_users_updated_at
        BEFORE UPDATE ON users
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

