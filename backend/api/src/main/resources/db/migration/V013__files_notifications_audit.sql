-- ============================================================================
-- V013 — File metadata (ADR-008), notifications, audit log, idempotency records.
-- ============================================================================

CREATE TABLE files (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    storage_key       text NOT NULL UNIQUE,     -- provider-opaque; never a client filename
    bucket            text,
    provider          text NOT NULL CHECK (provider IN ('LOCAL','S3')),
    original_filename text NOT NULL,
    content_type      text NOT NULL,
    size_bytes        bigint NOT NULL CHECK (size_bytes > 0),
    checksum_sha256   text NOT NULL,
    visibility        text NOT NULL DEFAULT 'AUTHORIZED'
                      CHECK (visibility IN ('PUBLIC','AUTHORIZED','PRIVATE')),
    status            text NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE','DELETED','QUARANTINED')),
    uploaded_by       uuid REFERENCES users(id) ON DELETE SET NULL,
    created_at        timestamptz NOT NULL DEFAULT now(),
    deleted_at        timestamptz
);
CREATE INDEX idx_files_checksum ON files(checksum_sha256);
CREATE INDEX idx_files_uploader ON files(uploaded_by);
COMMENT ON COLUMN files.storage_key IS
 'Generated, content-addressed. The client filename is metadata only and is NEVER used as a
  path component (path traversal). Access is authorized via the owning domain object, so a
  file id is not a bearer capability.';

ALTER TABLE content_blocks   ADD CONSTRAINT fk_content_blocks_file
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE SET NULL;
ALTER TABLE course_materials ADD CONSTRAINT fk_course_materials_file
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE SET NULL;
CREATE INDEX idx_content_blocks_file ON content_blocks(file_id);

CREATE TABLE notifications (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         text NOT NULL CHECK (type IN
                 ('ENROLLMENT_CREATED','LESSON_COMPLETED','QUIZ_GRADED','SUBMISSION_GRADED',
                  'TEACHER_FEEDBACK','ANNOUNCEMENT','GRADE_PUBLISHED','DEADLINE_REMINDER',
                  'ACCOUNT_STATUS_CHANGED')),
    channel      text NOT NULL DEFAULT 'IN_APP'
                 CHECK (channel IN ('IN_APP','EMAIL','PUSH')),  -- only IN_APP is dispatched in v1
    status       text NOT NULL DEFAULT 'PENDING'
                 CHECK (status IN ('PENDING','SENT','READ','FAILED','SUPPRESSED')),
    payload      jsonb NOT NULL DEFAULT '{}'::jsonb,
    -- Idempotency: the same logical event cannot create two notifications (brief §61).
    dedupe_key   text,
    created_at   timestamptz NOT NULL DEFAULT now(),
    sent_at      timestamptz,
    read_at      timestamptz
);
CREATE UNIQUE INDEX uq_notification_dedupe ON notifications(recipient_user_id, dedupe_key)
    WHERE dedupe_key IS NOT NULL;
CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id, status, created_at DESC);

CREATE TABLE audit_log (
    id            bigserial PRIMARY KEY,
    occurred_at   timestamptz NOT NULL DEFAULT now(),
    -- Deliberately NO FK to users(id). AuditService.write() commits via Propagation.REQUIRES_NEW
    -- specifically so an audit row survives a caller's rollback/still-open transaction (see its
    -- class javadoc) — but a FK here needs a lock on the referenced users row, and the caller
    -- (e.g. login success, password change, role/status change) very often holds an uncommitted
    -- UPDATE lock on that exact row at the moment it audits itself. That is an unavoidable,
    -- Postgres-undetectable self-deadlock: the FK-validation lock can never be granted because
    -- the outer transaction is waiting on THIS insert to return before it can commit and release
    -- it. `actor_label` is captured precisely so the audit trail does not need a live FK to stay
    -- meaningful after the user row changes or is removed.
    actor_user_id uuid,
    actor_label   text,           -- preserved even if the user row is later removed
    action        text NOT NULL,
    entity_type   text,
    entity_id     uuid,
    outcome       text NOT NULL DEFAULT 'SUCCESS' CHECK (outcome IN ('SUCCESS','FAILURE','DENIED')),
    request_id    text,
    ip_address    inet,
    user_agent    text,
    details       jsonb NOT NULL DEFAULT '{}'::jsonb
);
CREATE INDEX idx_audit_log_occurred ON audit_log(occurred_at DESC);
CREATE INDEX idx_audit_log_actor    ON audit_log(actor_user_id, occurred_at DESC);
CREATE INDEX idx_audit_log_entity   ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_action   ON audit_log(action, occurred_at DESC);
COMMENT ON TABLE audit_log IS
 'Append-only. NEVER store a credential, token, or student source code in details.
  Auth events, role changes, publishing, grading, enrollment and admin actions land here.';

CREATE OR REPLACE FUNCTION audit_log_no_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_log is append-only.' USING ERRCODE = 'integrity_constraint_violation';
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_audit_log_no_update BEFORE UPDATE ON audit_log
    FOR EACH ROW EXECUTE FUNCTION audit_log_no_mutation();
CREATE TRIGGER trg_audit_log_no_delete BEFORE DELETE ON audit_log
    FOR EACH ROW EXECUTE FUNCTION audit_log_no_mutation();

-- Generic Idempotency-Key support (brief §61) for non-trivially-idempotent POSTs.
CREATE TABLE idempotency_records (
    idempotency_key text NOT NULL,
    user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint        text NOT NULL,
    request_hash    text NOT NULL,
    response_status int,
    response_body   jsonb,
    created_at      timestamptz NOT NULL DEFAULT now(),
    expires_at      timestamptz NOT NULL,
    PRIMARY KEY (idempotency_key, user_id, endpoint)
);
CREATE INDEX idx_idempotency_expiry ON idempotency_records(expires_at);

-- Simple, greppable curated synonyms for Uzbek search, which has no stemmer (ADR-010).
CREATE TABLE search_synonyms (
    id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    locale  text NOT NULL REFERENCES locales(code),
    term    text NOT NULL,
    synonym text NOT NULL,
    CONSTRAINT uq_search_synonym UNIQUE (locale, term, synonym)
);
