-- V4__add_timezone_to_users.sql
-- Adds missing timezone column required by UserEntity (field `timezone`).
-- Immutability: prior migrations remain untouched.
-- Safe/idempotent: uses IF NOT EXISTS + guarded constraint creation.

ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone TEXT;

-- Optional length constraint (kept generous; adjust later if UX defines stricter list)
DO $$ BEGIN
    ALTER TABLE users ADD CONSTRAINT users_timezone_len CHECK (timezone IS NULL OR char_length(timezone) BETWEEN 2 AND 100);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

