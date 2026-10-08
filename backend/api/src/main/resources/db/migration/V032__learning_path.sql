-- ============================================================================
-- V032 — Phase 5 (B1): Learning path foundations.
--
-- Schema and backfill for the server-side learning path gate (ADR-015, ADR-016, ADR-018):
--   1. course_learning_paths: per-course gate switch, midterm anchor and reading fraction.
--   2. activities.path_step: identifies TEST, PRACTICE and MIDTERM activities in a lesson/course.
--      Partial unique index enforces at most one active TEST and one active PRACTICE per lesson.
--      Backfilled from existing QUIZ (TEST) and CODING_TASK (PRACTICE) by position.
--   3. lesson_progress: active reading time measurement, heartbeat tracking, waiver timestamp.
--      Backfilled read_completed_at = completed_at for historical COMPLETED rows.
--   4. quiz_attempt_grants: append-only extra attempts granted by admin, with an
--      anti-mutation trigger forbidding UPDATE and DELETE.
--   5. quiz_attempt_lock function and BEFORE INSERT trigger on quiz_attempts: takes transaction
--      advisory lock, enforces contiguous attempt numbering and allowance <= max_attempts + grants.
--   6. Permissions QUIZ_ATTEMPT_GRANT and LEARNING_PATH_MANAGE granted to ADMIN role.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. course_learning_paths
-- ----------------------------------------------------------------------------
CREATE TABLE course_learning_paths (
    course_id                uuid PRIMARY KEY REFERENCES courses(id) ON DELETE CASCADE,
    enabled                  boolean NOT NULL DEFAULT false,
    enabled_at               timestamptz,
    enabled_by               uuid REFERENCES users(id) ON DELETE SET NULL,
    midterm_activity_id      uuid REFERENCES activities(id) ON DELETE SET NULL,
    midterm_after_lesson_id  uuid REFERENCES lessons(id) ON DELETE SET NULL,
    midterm_opens_at         timestamptz,
    min_reading_fraction     numeric(3,2) NOT NULL DEFAULT 0.50
                             CHECK (min_reading_fraction >= 0.00 AND min_reading_fraction <= 1.00),
    created_at               timestamptz NOT NULL DEFAULT now(),
    updated_at               timestamptz NOT NULL DEFAULT now(),
    version                  bigint NOT NULL DEFAULT 0,
    CONSTRAINT chk_course_learning_paths_midterm_pair CHECK (
        (midterm_activity_id IS NULL AND midterm_after_lesson_id IS NULL) OR
        (midterm_activity_id IS NOT NULL AND midterm_after_lesson_id IS NOT NULL)
    )
);

CREATE INDEX idx_course_learning_paths_enabled_by     ON course_learning_paths(enabled_by);
CREATE INDEX idx_course_learning_paths_midterm_act    ON course_learning_paths(midterm_activity_id);
CREATE INDEX idx_course_learning_paths_midterm_lesson ON course_learning_paths(midterm_after_lesson_id);

CREATE TRIGGER trg_course_learning_paths_updated_at
    BEFORE UPDATE ON course_learning_paths
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE course_learning_paths IS
 'Learning path configuration per course: enable flag, midterm anchor lesson/activity and reading fraction.';

-- ----------------------------------------------------------------------------
-- 2. activities.path_step + CHECKs + backfill + partial unique index
-- ----------------------------------------------------------------------------
ALTER TABLE activities
    ADD COLUMN path_step text;

ALTER TABLE activities
    ADD CONSTRAINT chk_activities_path_step
        CHECK (path_step IS NULL OR path_step IN ('TEST', 'PRACTICE', 'MIDTERM')),
    ADD CONSTRAINT chk_activities_path_step_lesson
        CHECK (path_step IS NULL OR path_step = 'MIDTERM' OR lesson_id IS NOT NULL);

-- Backfill: first QUIZ by position per lesson -> TEST
WITH first_quiz AS (
    SELECT DISTINCT ON (lesson_id) id
    FROM activities
    WHERE lesson_id IS NOT NULL
      AND activity_type = 'QUIZ'
      AND status <> 'ARCHIVED'
    ORDER BY lesson_id, position ASC
)
UPDATE activities
SET path_step = 'TEST'
WHERE id IN (SELECT id FROM first_quiz);

-- Backfill: first CODING_TASK by position per lesson -> PRACTICE
WITH first_task AS (
    SELECT DISTINCT ON (lesson_id) id
    FROM activities
    WHERE lesson_id IS NOT NULL
      AND activity_type = 'CODING_TASK'
      AND status <> 'ARCHIVED'
    ORDER BY lesson_id, position ASC
)
UPDATE activities
SET path_step = 'PRACTICE'
WHERE id IN (SELECT id FROM first_task);

CREATE UNIQUE INDEX uq_activities_lesson_path_step
    ON activities (lesson_id, path_step)
    WHERE path_step IN ('TEST', 'PRACTICE') AND status <> 'ARCHIVED';

-- ----------------------------------------------------------------------------
-- 3. lesson_progress new columns + backfill
-- ----------------------------------------------------------------------------
ALTER TABLE lesson_progress
    ADD COLUMN active_reading_seconds   int NOT NULL DEFAULT 0 CHECK (active_reading_seconds >= 0),
    ADD COLUMN last_heartbeat_at        timestamptz,
    ADD COLUMN max_block_position       int,
    ADD COLUMN reached_end_at           timestamptz,
    ADD COLUMN required_reading_seconds int,
    ADD COLUMN read_completed_at        timestamptz,
    ADD COLUMN path_waived_at           timestamptz;

CREATE INDEX idx_lesson_progress_enr_heartbeat
    ON lesson_progress (enrollment_id, last_heartbeat_at);

-- Backfill: read_completed_at = completed_at where status = 'COMPLETED'
UPDATE lesson_progress
   SET read_completed_at = completed_at
 WHERE status = 'COMPLETED'
   AND completed_at IS NOT NULL;

-- ----------------------------------------------------------------------------
-- 4. quiz_attempt_grants + no-update/no-delete trigger
-- ----------------------------------------------------------------------------
CREATE TABLE quiz_attempt_grants (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id       uuid NOT NULL REFERENCES quizzes(id) ON DELETE RESTRICT,
    enrollment_id uuid NOT NULL REFERENCES enrollments(id) ON DELETE RESTRICT,
    granted_by    uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reason        text NOT NULL CHECK (length(btrim(reason)) >= 5),
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_quiz_attempt_grants_quiz_enr   ON quiz_attempt_grants(quiz_id, enrollment_id);
CREATE INDEX idx_quiz_attempt_grants_enrollment ON quiz_attempt_grants(enrollment_id);
CREATE INDEX idx_quiz_attempt_grants_granted_by ON quiz_attempt_grants(granted_by);

COMMENT ON TABLE quiz_attempt_grants IS
 'Append-only allowance of extra quiz attempts granted by admins.';

CREATE OR REPLACE FUNCTION forbid_quiz_attempt_grants_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $fn$
BEGIN
    RAISE EXCEPTION 'quiz_attempt_grants rows are append-only and cannot be updated or deleted'
        USING ERRCODE = 'check_violation';
END;
$fn$;

CREATE TRIGGER trg_quiz_attempt_grants_immutable
    BEFORE UPDATE OR DELETE ON quiz_attempt_grants
    FOR EACH ROW EXECUTE FUNCTION forbid_quiz_attempt_grants_mutation();

-- ----------------------------------------------------------------------------
-- 5. quiz_attempt_lock function + BEFORE INSERT trigger on quiz_attempts
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION quiz_attempt_lock(p_quiz_id uuid, p_enrollment_id uuid)
RETURNS void
LANGUAGE plpgsql
AS $fn$
BEGIN
    PERFORM pg_advisory_xact_lock(hashtextextended(p_quiz_id::text || p_enrollment_id::text, 0::bigint));
END;
$fn$;

CREATE OR REPLACE FUNCTION trg_quiz_attempts_before_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $fn$
DECLARE
    v_max_attempt  int;
    v_max_attempts int;
    v_grants       int;
BEGIN
    PERFORM quiz_attempt_lock(NEW.quiz_id, NEW.enrollment_id);

    SELECT coalesce(max(attempt_number), 0)
      INTO v_max_attempt
      FROM quiz_attempts
     WHERE quiz_id = NEW.quiz_id
       AND enrollment_id = NEW.enrollment_id;

    IF NEW.attempt_number <> v_max_attempt + 1 THEN
        RAISE EXCEPTION 'Attempt number must be contiguous: expected %, got %',
            v_max_attempt + 1, NEW.attempt_number
            USING ERRCODE = 'check_violation';
    END IF;

    SELECT max_attempts
      INTO v_max_attempts
      FROM quizzes
     WHERE id = NEW.quiz_id;

    IF v_max_attempts IS NOT NULL THEN
        SELECT count(*)
          INTO v_grants
          FROM quiz_attempt_grants
         WHERE quiz_id = NEW.quiz_id
           AND enrollment_id = NEW.enrollment_id;

        IF NEW.attempt_number > (v_max_attempts + v_grants) THEN
            RAISE EXCEPTION 'Attempt limit reached: max attempts is %, grants is %, attempted %',
                v_max_attempts, v_grants, NEW.attempt_number
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;

    RETURN NEW;
END;
$fn$;

CREATE TRIGGER trg_quiz_attempts_lock_and_limit
    BEFORE INSERT ON quiz_attempts
    FOR EACH ROW EXECUTE FUNCTION trg_quiz_attempts_before_insert();

-- ----------------------------------------------------------------------------
-- 6. Permissions for ADMIN (pattern of V022)
-- ----------------------------------------------------------------------------
INSERT INTO permissions (code, description, domain) VALUES
 ('QUIZ_ATTEMPT_GRANT', 'Grant additional quiz attempts to students', 'assessment'),
 ('LEARNING_PATH_MANAGE', 'Configure learning path rules and waivers for courses', 'progress');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('QUIZ_ATTEMPT_GRANT', 'LEARNING_PATH_MANAGE')
WHERE r.code = 'ADMIN';
