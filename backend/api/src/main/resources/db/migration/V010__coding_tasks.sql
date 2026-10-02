-- ============================================================================
-- V010 — Coding education (brief §17). Language-agnostic by design.
-- ============================================================================

CREATE TABLE programming_languages (
    code             text PRIMARY KEY CHECK (code ~ '^[a-z0-9_+-]+$'),
    display_name     text NOT NULL,
    version_label    text NOT NULL,
    -- Opaque key the sandbox adapter maps to a Judge0 language id or a Docker image digest.
    executor_key     text NOT NULL,
    compile_required boolean NOT NULL DEFAULT false,
    source_filename  text NOT NULL,
    is_enabled       boolean NOT NULL DEFAULT false,
    notes            text,
    created_at       timestamptz NOT NULL DEFAULT now()
);

-- Python and Go are enabled per the product requirement (brief §17).
INSERT INTO programming_languages
 (code, display_name, version_label, executor_key, compile_required, source_filename, is_enabled, notes) VALUES
 ('python', 'Python', '3.11', 'judge0:71', false, 'main.py', true,  'Product requirement.'),
 ('go',     'Go',     '1.21', 'judge0:95', true,  'main.go', true,  'Product requirement; compile stage required.'),
 -- DELIBERATELY DISABLED, DO NOT DELETE: the supplied DAS1110 syllabus is a C++ course
 -- (Visual Studio, pointers, templates) while the product brief specifies Python/Go.
 -- Enabling C++ is one row + a sandbox image. See PENDING-INPUTS.md §2.
 ('cpp',    'C++',    '17',   'judge0:54', true,  'main.cpp', false,
  'Required by the DAS1110 syllabus but not by the product brief. Unresolved conflict: PENDING-INPUTS.md §2.');

CREATE TABLE coding_tasks (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id         uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    code              text,
    difficulty        text CHECK (difficulty IN ('INTRODUCTORY','EASY','MEDIUM','HARD','ADVANCED')),
    status            text NOT NULL DEFAULT 'DRAFT'
                      CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    max_points        numeric(6,2) NOT NULL DEFAULT 100 CHECK (max_points > 0),
    scoring_strategy  text NOT NULL DEFAULT 'PER_TEST_WEIGHTED' CHECK (scoring_strategy IN
                      ('ALL_OR_NOTHING','PER_TEST_WEIGHTED','PER_TEST_EQUAL')),
    max_attempts      int CHECK (max_attempts IS NULL OR max_attempts >= 1),
    -- Per-task limits. ExecutionProperties CLAMPS these to the configured ceiling; a task
    -- may tighten a limit but can never exceed the platform maximum (ADR-006).
    cpu_time_limit_ms  int CHECK (cpu_time_limit_ms  IS NULL OR cpu_time_limit_ms  BETWEEN 100 AND 60000),
    wall_time_limit_ms int CHECK (wall_time_limit_ms IS NULL OR wall_time_limit_ms BETWEEN 100 AND 120000),
    memory_limit_kb    int CHECK (memory_limit_kb    IS NULL OR memory_limit_kb    BETWEEN 16384 AND 1048576),
    published_at      timestamptz,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    created_by        uuid REFERENCES users(id) ON DELETE SET NULL,
    version           bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_coding_task_code UNIQUE (course_id, code)
);
CREATE INDEX idx_coding_tasks_course ON coding_tasks(course_id, status);
CREATE TRIGGER trg_coding_tasks_updated_at BEFORE UPDATE ON coding_tasks
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE coding_task_translations (
    coding_task_id uuid NOT NULL REFERENCES coding_tasks(id) ON DELETE CASCADE,
    locale         text NOT NULL REFERENCES locales(code),
    title          text NOT NULL,
    description    text NOT NULL,
    instructions   text,
    constraints_text text,
    explanation    text,     -- reference explanation; teacher-visible only until solved
    PRIMARY KEY (coding_task_id, locale)
);

CREATE TABLE coding_task_languages (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    coding_task_id uuid NOT NULL REFERENCES coding_tasks(id) ON DELETE CASCADE,
    language_code  text NOT NULL REFERENCES programming_languages(code) ON DELETE RESTRICT,
    starter_code   text,
    -- TEACHER-ONLY. Must never appear in a student DTO. See skills/coding-execution-security.
    solution_code  text,
    CONSTRAINT uq_coding_task_language UNIQUE (coding_task_id, language_code)
);
COMMENT ON COLUMN coding_task_languages.solution_code IS
 'NEVER serialize to a student. Enforced by using a distinct teacher-only response mapper.';

CREATE TABLE coding_task_hints (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    coding_task_id uuid NOT NULL REFERENCES coding_tasks(id) ON DELETE CASCADE,
    position       int NOT NULL CHECK (position >= 0),
    penalty_percent numeric(5,2) NOT NULL DEFAULT 0 CHECK (penalty_percent BETWEEN 0 AND 100),
    CONSTRAINT uq_hint_position UNIQUE (coding_task_id, position)
);
CREATE TABLE coding_task_hint_translations (
    hint_id uuid NOT NULL REFERENCES coding_task_hints(id) ON DELETE CASCADE,
    locale  text NOT NULL REFERENCES locales(code),
    body    text NOT NULL,
    PRIMARY KEY (hint_id, locale)
);

CREATE TABLE coding_task_test_cases (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    coding_task_id     uuid NOT NULL REFERENCES coding_tasks(id) ON DELETE CASCADE,
    position           int NOT NULL CHECK (position >= 0),
    name               text,
    visibility         text NOT NULL DEFAULT 'HIDDEN'
                       CHECK (visibility IN ('VISIBLE','HIDDEN')),
    is_example         boolean NOT NULL DEFAULT false,
    weight             numeric(6,2) NOT NULL DEFAULT 1 CHECK (weight > 0),
    stdin              text NOT NULL DEFAULT '',
    expected_stdout    text NOT NULL DEFAULT '',
    comparison_mode    text NOT NULL DEFAULT 'TRIMMED' CHECK (comparison_mode IN
                       ('EXACT','TRIMMED','IGNORE_WHITESPACE','CASE_INSENSITIVE','NUMERIC_TOLERANCE','REGEX')),
    numeric_tolerance  numeric(12,6) CHECK (numeric_tolerance IS NULL OR numeric_tolerance >= 0),
    cpu_time_limit_ms  int CHECK (cpu_time_limit_ms IS NULL OR cpu_time_limit_ms BETWEEN 100 AND 60000),
    created_at         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_test_case_position UNIQUE (coding_task_id, position),
    CONSTRAINT ck_test_case_tolerance CHECK (comparison_mode <> 'NUMERIC_TOLERANCE' OR numeric_tolerance IS NOT NULL)
);
CREATE INDEX idx_test_cases_task ON coding_task_test_cases(coding_task_id, position);
COMMENT ON TABLE coding_task_test_cases IS
 'HIDDEN cases: neither expected_stdout nor the actual stdout/stderr is ever returned to a
  student. Only pass/fail, time and the verdict are. Teachers see everything.';

CREATE TABLE submissions (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    coding_task_id    uuid NOT NULL REFERENCES coding_tasks(id) ON DELETE RESTRICT,
    enrollment_id     uuid NOT NULL REFERENCES enrollments(id) ON DELETE RESTRICT,
    activity_id       uuid REFERENCES activities(id) ON DELETE SET NULL,
    language_code     text NOT NULL REFERENCES programming_languages(code) ON DELETE RESTRICT,
    attempt_number    int NOT NULL CHECK (attempt_number >= 1),
    source_code       text NOT NULL,
    source_size_bytes int NOT NULL CHECK (source_size_bytes > 0),
    status            text NOT NULL DEFAULT 'QUEUED' CHECK (status IN
                      ('QUEUED','RUNNING','COMPLETED','FAILED','CANCELLED')),
    verdict           text CHECK (verdict IN
                      ('ACCEPTED','PARTIAL','WRONG_ANSWER','COMPILATION_ERROR','RUNTIME_ERROR',
                       'TIME_LIMIT_EXCEEDED','MEMORY_LIMIT_EXCEEDED','OUTPUT_LIMIT_EXCEEDED',
                       'INTERNAL_ERROR')),
    score             numeric(6,2) CHECK (score >= 0),
    max_score         numeric(6,2) CHECK (max_score >= 0),
    tests_passed      int CHECK (tests_passed >= 0),
    tests_total       int CHECK (tests_total >= 0),
    idempotency_key   text,
    submitted_at      timestamptz NOT NULL DEFAULT now(),
    completed_at      timestamptz,
    version           bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_submission_attempt UNIQUE (coding_task_id, enrollment_id, attempt_number),
    CONSTRAINT ck_submission_tests CHECK (tests_passed IS NULL OR tests_total IS NULL
                                          OR tests_passed <= tests_total)
);
CREATE UNIQUE INDEX uq_submission_idempotency ON submissions(enrollment_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
CREATE INDEX idx_submissions_enrollment ON submissions(enrollment_id, coding_task_id);
CREATE INDEX idx_submissions_task       ON submissions(coding_task_id, status);
CREATE INDEX idx_submissions_pending    ON submissions(status) WHERE status IN ('QUEUED','RUNNING');
COMMENT ON TABLE submissions IS
 'Append-only: a re-submission is a NEW row with the next attempt_number. Never overwritten,
  never deleted - a submission is evidence of what the student wrote.';
