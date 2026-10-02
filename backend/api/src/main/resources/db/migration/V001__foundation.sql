-- ============================================================================
-- V001 — Extensions, shared functions and triggers.
-- ADR-009: Flyway owns the schema. Hibernate only validates it.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;     -- case-insensitive email / username
CREATE EXTENSION IF NOT EXISTS unaccent;   -- FTS normalization (ADR-010)
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- typo-tolerant title matching

-- Keeps updated_at honest without relying on the application.
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Maps a locale to a PostgreSQL text-search configuration.
-- NOTE: PostgreSQL ships no Uzbek dictionary, so 'uz' falls back to 'simple'
-- (tokenization + unaccent, NO stemming). This is a documented limitation, see ADR-010.
-- Must be IMMUTABLE to be usable in a GENERATED column.
CREATE OR REPLACE FUNCTION fts_config(p_locale text) RETURNS regconfig AS $$
    SELECT CASE lower(p_locale)
               WHEN 'ru' THEN 'russian'::regconfig
               WHEN 'en' THEN 'english'::regconfig
               ELSE 'simple'::regconfig
           END;
$$ LANGUAGE sql IMMUTABLE STRICT;

-- The three supported content locales (ADR-005). Adding a locale is an INSERT, not an ALTER.
CREATE TABLE locales (
    code        text PRIMARY KEY CHECK (code ~ '^[a-z]{2}$'),
    name_native text NOT NULL,
    name_en     text NOT NULL,
    is_enabled  boolean NOT NULL DEFAULT true,
    sort_order  int NOT NULL DEFAULT 0
);
INSERT INTO locales (code, name_native, name_en, sort_order) VALUES
    ('uz', 'O''zbekcha', 'Uzbek',   1),
    ('ru', 'Русский',   'Russian', 2),
    ('en', 'English',   'English', 3);

COMMENT ON FUNCTION fts_config IS 'Locale -> regconfig. uz has no dictionary; uses simple. See ADR-010.';
