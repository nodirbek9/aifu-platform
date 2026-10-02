-- ============================================================================
-- V006 — Enrollment: the spine of all student-owned data (brief §14).
-- Every progress row, attempt, submission and grade hangs off enrollment_id,
-- which makes "another student's data" structurally unrepresentable.
-- ============================================================================

CREATE TABLE enrollments (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id          uuid NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    student_user_id    uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status             text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN
                       ('PENDING','ACTIVE','SUSPENDED','COMPLETED','FAILED','WITHDRAWN')),
    attempt_no         int NOT NULL DEFAULT 1 CHECK (attempt_no >= 1),  -- retaking a course
    source             text NOT NULL DEFAULT 'ADMIN'
                       CHECK (source IN ('ADMIN','SELF','IMPORT','AUTOMATIC')),
    enrolled_at        timestamptz NOT NULL DEFAULT now(),
    starts_on          date,
    completed_at       timestamptz,
    withdrawn_at       timestamptz,
    -- Derived from published grade_entries by FinalScoreCalculator. Never hand-edited.
    final_score        numeric(6,2) CHECK (final_score >= 0),
    final_grade_symbol text,
    final_grade_at     timestamptz,
    admitted_to_final  boolean,
    academic_debt      boolean NOT NULL DEFAULT false,
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid REFERENCES users(id) ON DELETE SET NULL,
    version            bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_enrollment_attempt UNIQUE (course_id, student_user_id, attempt_no),
    CONSTRAINT ck_enrollment_completed CHECK (status <> 'COMPLETED' OR completed_at IS NOT NULL),
    CONSTRAINT ck_enrollment_withdrawn CHECK (status <> 'WITHDRAWN' OR withdrawn_at IS NOT NULL)
);

-- THE duplicate-enrollment invariant (brief §35/§60). Enforced by the DATABASE so two
-- concurrent requests cannot both win. History is preserved because terminal states are
-- excluded, so withdraw -> re-enroll creates a second row rather than mutating the first.
CREATE UNIQUE INDEX uq_enrollment_active
    ON enrollments(course_id, student_user_id)
    WHERE status IN ('PENDING','ACTIVE','SUSPENDED');

CREATE INDEX idx_enrollments_student ON enrollments(student_user_id, status);
CREATE INDEX idx_enrollments_course  ON enrollments(course_id, status);
CREATE TRIGGER trg_enrollments_updated_at BEFORE UPDATE ON enrollments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Append-only history. Never deleted, never updated (brief §14: "preserve meaningful history").
CREATE TABLE enrollment_events (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    event_type    text NOT NULL CHECK (event_type IN
                  ('CREATED','ACTIVATED','SUSPENDED','RESUMED','COMPLETED','FAILED',
                   'WITHDRAWN','GRADE_FINALIZED','REINSTATED')),
    from_status   text,
    to_status     text,
    actor_user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    reason        text,
    occurred_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_enrollment_events_enrollment ON enrollment_events(enrollment_id, occurred_at);
