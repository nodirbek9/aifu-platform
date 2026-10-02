-- ============================================================================
-- V009 — Reusable assessment engine (brief §16). Attempt history is append-only.
-- ============================================================================

CREATE TABLE quizzes (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id            uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    code                 text,
    status               text NOT NULL DEFAULT 'DRAFT'
                         CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    max_points           numeric(6,2) NOT NULL DEFAULT 0 CHECK (max_points >= 0),
    passing_percent      numeric(5,2) NOT NULL DEFAULT 60 CHECK (passing_percent BETWEEN 0 AND 100),
    time_limit_seconds   int CHECK (time_limit_seconds IS NULL OR time_limit_seconds > 0),
    max_attempts         int CHECK (max_attempts IS NULL OR max_attempts >= 1),
    question_pool_size   int CHECK (question_pool_size IS NULL OR question_pool_size >= 1),
    shuffle_questions    boolean NOT NULL DEFAULT false,
    shuffle_options      boolean NOT NULL DEFAULT false,
    reveal_policy        text NOT NULL DEFAULT 'AFTER_LAST_ATTEMPT' CHECK (reveal_policy IN
                         ('NEVER','IMMEDIATELY','AFTER_ATTEMPT','AFTER_LAST_ATTEMPT','AFTER_DUE_DATE')),
    published_at         timestamptz,
    created_at           timestamptz NOT NULL DEFAULT now(),
    updated_at           timestamptz NOT NULL DEFAULT now(),
    created_by           uuid REFERENCES users(id) ON DELETE SET NULL,
    version              bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_quiz_code UNIQUE (course_id, code)
);
CREATE INDEX idx_quizzes_course ON quizzes(course_id, status);
CREATE TRIGGER trg_quizzes_updated_at BEFORE UPDATE ON quizzes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON COLUMN quizzes.reveal_policy IS
 'Controls when correct answers/explanations may be serialized to a student. The student DTO
  mapper honours this; it is never a client-side concern.';

CREATE TABLE quiz_translations (
    quiz_id      uuid NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    locale       text NOT NULL REFERENCES locales(code),
    title        text NOT NULL,
    description  text,
    instructions text,
    PRIMARY KEY (quiz_id, locale)
);

CREATE TABLE questions (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id       uuid NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    position      int NOT NULL CHECK (position >= 0),
    question_type text NOT NULL CHECK (question_type IN
                  ('SINGLE_CHOICE','MULTIPLE_CHOICE','TRUE_FALSE','SHORT_ANSWER','NUMERIC',
                   'MATCHING','ORDERING')),
    points        numeric(6,2) NOT NULL DEFAULT 1 CHECK (points > 0),
    difficulty    text CHECK (difficulty IN ('INTRODUCTORY','EASY','MEDIUM','HARD','ADVANCED')),
    is_required   boolean NOT NULL DEFAULT true,
    -- Partial credit for MULTIPLE_CHOICE, tolerance for NUMERIC, etc.
    settings      jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    version       bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_question_position UNIQUE (quiz_id, position) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_questions_quiz ON questions(quiz_id, position);
CREATE TRIGGER trg_questions_updated_at BEFORE UPDATE ON questions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE question_translations (
    question_id uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    locale      text NOT NULL REFERENCES locales(code),
    prompt      text NOT NULL,
    explanation text,     -- revealed only per quizzes.reveal_policy
    hint        text,
    PRIMARY KEY (question_id, locale)
);

CREATE TABLE answer_options (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    position    int NOT NULL CHECK (position >= 0),
    is_correct  boolean NOT NULL DEFAULT false,
    match_key   text,     -- pairing key for MATCHING questions
    CONSTRAINT uq_answer_option_position UNIQUE (question_id, position) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_answer_options_question ON answer_options(question_id, position);
COMMENT ON COLUMN answer_options.is_correct IS
 'NEVER serialized into a student-facing DTO before quizzes.reveal_policy allows it.
  Enforced by using a separate mapper for student vs teacher responses.';

CREATE TABLE answer_option_translations (
    option_id uuid NOT NULL REFERENCES answer_options(id) ON DELETE CASCADE,
    locale    text NOT NULL REFERENCES locales(code),
    text      text NOT NULL,
    feedback  text,
    PRIMARY KEY (option_id, locale)
);

CREATE TABLE question_answer_keys (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id    uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    match_mode     text NOT NULL CHECK (match_mode IN
                   ('EXACT','CASE_INSENSITIVE','TRIMMED','REGEX','NUMERIC_TOLERANCE')),
    expected_value text NOT NULL,
    tolerance      numeric(12,6) CHECK (tolerance IS NULL OR tolerance >= 0),
    locale         text REFERENCES locales(code),   -- NULL = language independent
    CONSTRAINT ck_answer_key_tolerance CHECK (match_mode <> 'NUMERIC_TOLERANCE' OR tolerance IS NOT NULL)
);
CREATE INDEX idx_answer_keys_question ON question_answer_keys(question_id);

CREATE TABLE quiz_attempts (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id         uuid NOT NULL REFERENCES quizzes(id) ON DELETE RESTRICT,
    enrollment_id   uuid NOT NULL REFERENCES enrollments(id) ON DELETE RESTRICT,
    activity_id     uuid REFERENCES activities(id) ON DELETE SET NULL,
    attempt_number  int NOT NULL CHECK (attempt_number >= 1),
    status          text NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN
                    ('IN_PROGRESS','SUBMITTED','GRADED','EXPIRED','ABANDONED')),
    score           numeric(6,2) CHECK (score >= 0),
    max_score       numeric(6,2) CHECK (max_score >= 0),
    percent         numeric(5,2) CHECK (percent BETWEEN 0 AND 100),
    passed          boolean,
    locale          text REFERENCES locales(code),
    started_at      timestamptz NOT NULL DEFAULT now(),
    expires_at      timestamptz,
    submitted_at    timestamptz,
    graded_at       timestamptz,
    idempotency_key text,
    version         bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_quiz_attempt_number UNIQUE (quiz_id, enrollment_id, attempt_number),
    CONSTRAINT ck_attempt_submitted CHECK (status NOT IN ('SUBMITTED','GRADED') OR submitted_at IS NOT NULL)
);
-- A student may hold at most one open attempt per quiz (brief §35: repeated quiz submission).
CREATE UNIQUE INDEX uq_quiz_attempt_open ON quiz_attempts(quiz_id, enrollment_id)
    WHERE status = 'IN_PROGRESS';
CREATE INDEX idx_quiz_attempts_enrollment ON quiz_attempts(enrollment_id, quiz_id);
CREATE INDEX idx_quiz_attempts_status     ON quiz_attempts(status) WHERE status = 'IN_PROGRESS';

-- Snapshot of the questions actually served, with the order used. Keeps an attempt
-- reproducible even if the teacher later edits the quiz.
CREATE TABLE quiz_attempt_questions (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id       uuid NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id      uuid NOT NULL REFERENCES questions(id) ON DELETE RESTRICT,
    served_position  int NOT NULL,
    points_possible  numeric(6,2) NOT NULL CHECK (points_possible >= 0),
    points_awarded   numeric(6,2) CHECK (points_awarded >= 0),
    is_correct       boolean,
    response_text    text,
    response_number  numeric(18,6),
    responded_at     timestamptz,
    graded_at        timestamptz,
    grader_note      text,
    CONSTRAINT uq_attempt_question UNIQUE (attempt_id, question_id)
);
CREATE INDEX idx_attempt_questions_attempt  ON quiz_attempt_questions(attempt_id, served_position);
CREATE INDEX idx_attempt_questions_question ON quiz_attempt_questions(question_id);

CREATE TABLE quiz_attempt_selected_options (
    attempt_question_id uuid NOT NULL REFERENCES quiz_attempt_questions(id) ON DELETE CASCADE,
    option_id           uuid NOT NULL REFERENCES answer_options(id) ON DELETE RESTRICT,
    ordinal             int,      -- for ORDERING questions
    PRIMARY KEY (attempt_question_id, option_id)
);
