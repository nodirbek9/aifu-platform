-- ============================================================================
-- V012 — Append-only gradebook (ADR-011) + feedback.
-- Published grades are NEVER overwritten. Corrections supersede.
-- ============================================================================

CREATE TABLE grade_entries (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id           uuid NOT NULL REFERENCES enrollments(id) ON DELETE RESTRICT,
    assessment_component_id uuid REFERENCES assessment_components(id) ON DELETE RESTRICT,
    activity_id             uuid REFERENCES activities(id) ON DELETE SET NULL,
    source                  text NOT NULL CHECK (source IN
                            ('QUIZ','CODING','MANUAL','ATTENDANCE','IMPORT','CALCULATED')),
    source_ref_type         text CHECK (source_ref_type IN ('QUIZ_ATTEMPT','SUBMISSION','MANUAL','NONE')),
    source_ref_id           uuid,
    score                   numeric(6,2) NOT NULL CHECK (score >= 0),
    max_score               numeric(6,2) NOT NULL CHECK (max_score > 0),
    status                  text NOT NULL DEFAULT 'DRAFT' CHECK (status IN
                            ('DRAFT','PUBLISHED','REVISED','VOIDED')),
    revision                int NOT NULL DEFAULT 1 CHECK (revision >= 1),
    supersedes_id           uuid REFERENCES grade_entries(id) ON DELETE RESTRICT,
    void_reason             text,
    evaluator_user_id       uuid REFERENCES users(id) ON DELETE SET NULL,
    evaluated_at            timestamptz NOT NULL DEFAULT now(),
    published_at            timestamptz,
    created_at              timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_grade_within_max CHECK (score <= max_score),
    CONSTRAINT ck_grade_voided_reason CHECK (status <> 'VOIDED' OR void_reason IS NOT NULL),
    CONSTRAINT ck_grade_published_at CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);
CREATE INDEX idx_grade_entries_enrollment ON grade_entries(enrollment_id, status);
CREATE INDEX idx_grade_entries_component  ON grade_entries(assessment_component_id);
CREATE INDEX idx_grade_entries_source     ON grade_entries(source_ref_type, source_ref_id);
-- At most ONE current (published) entry per (enrollment, component, source ref).
CREATE UNIQUE INDEX uq_grade_entry_current
    ON grade_entries(enrollment_id, assessment_component_id, source_ref_type, source_ref_id)
    WHERE status = 'PUBLISHED';

-- ADR-011: the database refuses to rewrite a published grade, whatever the application does.
CREATE OR REPLACE FUNCTION grade_entry_guard() RETURNS trigger AS $$
BEGIN
    IF OLD.status = 'PUBLISHED' AND (
           NEW.score IS DISTINCT FROM OLD.score
        OR NEW.max_score IS DISTINCT FROM OLD.max_score
        OR NEW.enrollment_id IS DISTINCT FROM OLD.enrollment_id
        OR NEW.assessment_component_id IS DISTINCT FROM OLD.assessment_component_id
        OR NEW.source_ref_id IS DISTINCT FROM OLD.source_ref_id
    ) THEN
        RAISE EXCEPTION
          'Published grade_entry % is immutable. Insert a superseding revision instead (ADR-011).',
          OLD.id USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    -- A published entry may only move to REVISED or VOIDED.
    IF OLD.status = 'PUBLISHED' AND NEW.status NOT IN ('PUBLISHED','REVISED','VOIDED') THEN
        RAISE EXCEPTION 'Illegal grade_entry status transition % -> % (ADR-011).',
          OLD.status, NEW.status USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_grade_entry_immutable BEFORE UPDATE ON grade_entries
    FOR EACH ROW EXECUTE FUNCTION grade_entry_guard();

CREATE OR REPLACE FUNCTION grade_entry_no_delete() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'grade_entries is append-only. Use status = VOIDED (ADR-011).'
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_grade_entry_no_delete BEFORE DELETE ON grade_entries
    FOR EACH ROW EXECUTE FUNCTION grade_entry_no_delete();

-- Consumers read this, not the table, so they never deal with revisions.
CREATE VIEW v_current_grade_entries AS
SELECT * FROM grade_entries WHERE status = 'PUBLISHED';

CREATE TABLE feedback (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type    text NOT NULL CHECK (target_type IN
                   ('SUBMISSION','QUIZ_ATTEMPT','GRADE_ENTRY','ACTIVITY','ENROLLMENT')),
    target_id      uuid NOT NULL,
    author_user_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    visibility     text NOT NULL DEFAULT 'STUDENT_VISIBLE'
                   CHECK (visibility IN ('STUDENT_VISIBLE','INTERNAL')),
    body_markdown  text NOT NULL CHECK (length(btrim(body_markdown)) > 0),
    body_sanitized_html text,       -- produced by ContentSanitizer on write
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    edited_at      timestamptz
);
CREATE INDEX idx_feedback_target ON feedback(target_type, target_id);
CREATE TRIGGER trg_feedback_updated_at BEFORE UPDATE ON feedback
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
