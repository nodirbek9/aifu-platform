-- ============================================================================
-- V008 — Four-level progress (brief §15). NOT a single percentage.
-- Everything hangs off enrollment_id, so cross-student access is unrepresentable.
-- ============================================================================

CREATE TABLE course_progress (
    enrollment_id       uuid PRIMARY KEY REFERENCES enrollments(id) ON DELETE CASCADE,
    status              text NOT NULL DEFAULT 'NOT_STARTED'
                        CHECK (status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED')),
    completion_percent  numeric(5,2) NOT NULL DEFAULT 0
                        CHECK (completion_percent BETWEEN 0 AND 100),
    lessons_total       int NOT NULL DEFAULT 0 CHECK (lessons_total >= 0),
    lessons_completed   int NOT NULL DEFAULT 0 CHECK (lessons_completed >= 0),
    activities_total    int NOT NULL DEFAULT 0 CHECK (activities_total >= 0),
    activities_completed int NOT NULL DEFAULT 0 CHECK (activities_completed >= 0),
    time_spent_seconds  bigint NOT NULL DEFAULT 0 CHECK (time_spent_seconds >= 0),
    started_at          timestamptz,
    last_accessed_at    timestamptz,
    completed_at        timestamptz,
    updated_at          timestamptz NOT NULL DEFAULT now(),
    version             bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_course_progress_counts CHECK (lessons_completed <= lessons_total)
);
CREATE TRIGGER trg_course_progress_updated_at BEFORE UPDATE ON course_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE module_progress (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id      uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    module_id          uuid NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
    status             text NOT NULL DEFAULT 'NOT_STARTED'
                       CHECK (status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED')),
    completion_percent numeric(5,2) NOT NULL DEFAULT 0 CHECK (completion_percent BETWEEN 0 AND 100),
    started_at         timestamptz,
    last_accessed_at   timestamptz,
    completed_at       timestamptz,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    version            bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_module_progress UNIQUE (enrollment_id, module_id)
);
CREATE INDEX idx_module_progress_module ON module_progress(module_id);
CREATE TRIGGER trg_module_progress_updated_at BEFORE UPDATE ON module_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE lesson_progress (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id      uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id          uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    status             text NOT NULL DEFAULT 'NOT_STARTED'
                       CHECK (status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED')),
    view_count         int NOT NULL DEFAULT 0 CHECK (view_count >= 0),
    time_spent_seconds bigint NOT NULL DEFAULT 0 CHECK (time_spent_seconds >= 0),
    first_accessed_at  timestamptz,
    last_accessed_at   timestamptz,
    completed_at       timestamptz,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    version            bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_lesson_progress UNIQUE (enrollment_id, lesson_id),
    CONSTRAINT ck_lesson_progress_completed CHECK (status <> 'COMPLETED' OR completed_at IS NOT NULL)
);
CREATE INDEX idx_lesson_progress_lesson ON lesson_progress(lesson_id, status);
CREATE TRIGGER trg_lesson_progress_updated_at BEFORE UPDATE ON lesson_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE lesson_progress IS
 'Answers "which students completed lesson X" (brief §26) with one indexed query.
  Completion is idempotent: a second completion does not re-count or move completed_at.';

CREATE TABLE activity_progress (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id  uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    activity_id    uuid NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    status         text NOT NULL DEFAULT 'NOT_STARTED'
                   CHECK (status IN ('NOT_STARTED','IN_PROGRESS','SUBMITTED','COMPLETED','FAILED')),
    attempts_used  int NOT NULL DEFAULT 0 CHECK (attempts_used >= 0),
    best_score     numeric(6,2) CHECK (best_score >= 0),
    last_score     numeric(6,2) CHECK (last_score >= 0),
    assigned_variant_no int CHECK (assigned_variant_no >= 1),
    first_attempt_at timestamptz,
    last_attempt_at  timestamptz,
    completed_at     timestamptz,
    updated_at       timestamptz NOT NULL DEFAULT now(),
    version          bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_activity_progress UNIQUE (enrollment_id, activity_id)
);
CREATE INDEX idx_activity_progress_activity ON activity_progress(activity_id, status);
CREATE TRIGGER trg_activity_progress_updated_at BEFORE UPDATE ON activity_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
