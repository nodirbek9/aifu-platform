-- ============================================================================
-- V007 — The generic, multilingual, multi-subject content engine.
-- Course -> Module -> Lesson -> ContentBlock, plus Activity as the polymorphic
-- "thing a student does". This is the multi-subject extension point (brief §6/§12).
-- ============================================================================

CREATE TABLE course_modules (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id        uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    position         int NOT NULL CHECK (position >= 0),
    code             text,
    status           text NOT NULL DEFAULT 'DRAFT'
                     CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    is_required      boolean NOT NULL DEFAULT true,
    estimated_minutes int CHECK (estimated_minutes >= 0),
    available_from   timestamptz,
    -- Honest provenance: both supplied syllabi give FLAT topic lists with no chapters,
    -- so any module grouping is a platform decision until AIFU confirms it.
    grouping_source  text NOT NULL DEFAULT 'PLATFORM_INFERRED'
                     CHECK (grouping_source IN ('SYLLABUS_EXPLICIT','PLATFORM_INFERRED','TEACHER_DEFINED')),
    published_at     timestamptz,
    deleted_at       timestamptz,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    version          bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_module_position UNIQUE (course_id, position) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_course_modules_course ON course_modules(course_id, position)
    WHERE deleted_at IS NULL;
CREATE TRIGGER trg_course_modules_updated_at BEFORE UPDATE ON course_modules
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE course_module_translations (
    module_id   uuid NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
    locale      text NOT NULL REFERENCES locales(code),
    title       text NOT NULL CHECK (length(btrim(title)) > 0),
    description text,
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector(fts_config(locale), coalesce(title, '')), 'A') ||
        setweight(to_tsvector(fts_config(locale), coalesce(description, '')), 'C')
    ) STORED,
    PRIMARY KEY (module_id, locale)
);
CREATE INDEX idx_module_tr_fts ON course_module_translations USING gin (search_vector);

CREATE TABLE lessons (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id        uuid NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
    position         int NOT NULL CHECK (position >= 0),
    code             text,                       -- syllabus ordinal: "M7", "S12", "A3", "MT9"
    session_type     text NOT NULL CHECK (session_type IN
                     ('LECTURE','SEMINAR','PRACTICAL','LABORATORY','INDEPENDENT_STUDY','COURSE_WORK')),
    allocated_hours  numeric(5,2) CHECK (allocated_hours >= 0),   -- syllabus hours for the topic
    estimated_minutes int CHECK (estimated_minutes >= 0),          -- platform reading/doing time
    difficulty       text CHECK (difficulty IN ('INTRODUCTORY','EASY','MEDIUM','HARD','ADVANCED')),
    is_required      boolean NOT NULL DEFAULT true,
    status           text NOT NULL DEFAULT 'DRAFT'
                     CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    available_from   timestamptz,
    published_at     timestamptz,
    deleted_at       timestamptz,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by       uuid REFERENCES users(id) ON DELETE SET NULL,
    version          bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_lesson_position UNIQUE (module_id, position) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_lessons_module  ON lessons(module_id, position) WHERE deleted_at IS NULL;
CREATE INDEX idx_lessons_session ON lessons(session_type);
CREATE TRIGGER trg_lessons_updated_at BEFORE UPDATE ON lessons
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON COLUMN lessons.session_type IS
 'Mirrors the institutional form. INDEPENDENT_STUDY is not secondary: it is 60% of IAKTT1105
  hours and 64% of DAS1110 hours. The same topic legitimately appears in two tracks
  (IAKTT1105 S7 and MT9 are both Big Data), so lessons are NOT deduplicated by title.';

CREATE TABLE lesson_translations (
    lesson_id  uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    locale     text NOT NULL REFERENCES locales(code),
    title      text NOT NULL CHECK (length(btrim(title)) > 0),
    summary    text,
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector(fts_config(locale), coalesce(title, '')),   'A') ||
        setweight(to_tsvector(fts_config(locale), coalesce(summary, '')), 'B')
    ) STORED,
    PRIMARY KEY (lesson_id, locale)
);
CREATE INDEX idx_lesson_tr_fts   ON lesson_translations USING gin (search_vector);
CREATE INDEX idx_lesson_tr_title ON lesson_translations USING gin (title gin_trgm_ops);

CREATE TABLE lesson_learning_outcomes (
    lesson_id           uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    learning_outcome_id uuid NOT NULL REFERENCES learning_outcomes(id) ON DELETE CASCADE,
    PRIMARY KEY (lesson_id, learning_outcome_id)
);
CREATE INDEX idx_lesson_outcomes_outcome ON lesson_learning_outcomes(learning_outcome_id);

CREATE TABLE lesson_prerequisites (
    lesson_id          uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    required_lesson_id uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    PRIMARY KEY (lesson_id, required_lesson_id),
    CONSTRAINT ck_lesson_prereq_not_self CHECK (lesson_id <> required_lesson_id)
);

-- A lesson is never one giant text column (brief §12).
CREATE TABLE content_blocks (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id    uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    position     int NOT NULL CHECK (position >= 0),
    block_type   text NOT NULL CHECK (block_type IN
                 ('TEXT','IMAGE','VIDEO','AUDIO','CODE_EXAMPLE','CALLOUT','DIAGRAM','TABLE',
                  'CASE_STUDY','FILE','LINK','EMBED','QUIZ_REF','CODING_TASK_REF','ACTIVITY_REF')),
    is_required  boolean NOT NULL DEFAULT true,
    visibility   text NOT NULL DEFAULT 'ALL'
                 CHECK (visibility IN ('ALL','STUDENT_ONLY','TEACHER_ONLY')),
    -- Non-localized, type-specific configuration (language of a code sample, aspect ratio,
    -- callout variant, referenced quiz/task id). Validated per block_type in Java.
    settings     jsonb NOT NULL DEFAULT '{}'::jsonb,
    file_id      uuid,                    -- FK added in V013
    deleted_at   timestamptz,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    version      bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_content_block_position UNIQUE (lesson_id, position) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_content_blocks_lesson ON content_blocks(lesson_id, position)
    WHERE deleted_at IS NULL;
CREATE TRIGGER trg_content_blocks_updated_at BEFORE UPDATE ON content_blocks
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE content_block_translations (
    block_id   uuid NOT NULL REFERENCES content_blocks(id) ON DELETE CASCADE,
    locale     text NOT NULL REFERENCES locales(code),
    -- ADR-005: JSONB here because the payload shape is polymorphic per block_type.
    -- ALWAYS sanitized on write by ContentSanitizer. Never trusted on read.
    payload    jsonb NOT NULL,
    -- Flattened, sanitized text for FTS only. NEVER returned to a client.
    plain_text text,
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector(fts_config(locale), coalesce(plain_text, ''))
    ) STORED,
    PRIMARY KEY (block_id, locale)
);
CREATE INDEX idx_content_block_tr_fts ON content_block_translations USING gin (search_vector);

-- ===================== THE multi-subject extension point =====================
-- Anything a student DOES is an activity. Adding Cybersecurity case studies or Networks
-- configuration exercises = a new activity_type + handler. No schema redesign. Ever.
CREATE TABLE activities (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id               uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    module_id               uuid REFERENCES course_modules(id) ON DELETE CASCADE,
    lesson_id               uuid REFERENCES lessons(id) ON DELETE CASCADE,
    position                int NOT NULL DEFAULT 0,
    activity_type           text NOT NULL CHECK (activity_type IN
                            ('QUIZ','CODING_TASK','MANUAL_TASK','CASE_STUDY','PRESENTATION',
                             'GROUP_PROJECT','FILE_SUBMISSION','DISCUSSION')),
    -- Polymorphic reference resolved by the handler for the activity_type.
    reference_id            uuid,
    assessment_component_id uuid REFERENCES assessment_components(id) ON DELETE SET NULL,
    is_graded               boolean NOT NULL DEFAULT true,
    max_points              numeric(6,2) CHECK (max_points >= 0),
    weight                  numeric(6,2) CHECK (weight >= 0),
    attempt_limit           int CHECK (attempt_limit IS NULL OR attempt_limit >= 1),
    collaboration_mode      text NOT NULL DEFAULT 'INDIVIDUAL'
                            CHECK (collaboration_mode IN ('INDIVIDUAL','GROUP')),
    -- DAS1110 requires "variativ" independent tasks: per-student variant assignment.
    variant_strategy        text NOT NULL DEFAULT 'NONE'
                            CHECK (variant_strategy IN ('NONE','DETERMINISTIC_BY_STUDENT','RANDOM')),
    available_from          timestamptz,
    due_at                  timestamptz,
    status                  text NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    created_at              timestamptz NOT NULL DEFAULT now(),
    updated_at              timestamptz NOT NULL DEFAULT now(),
    version                 bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_activity_placement CHECK (lesson_id IS NOT NULL OR module_id IS NOT NULL),
    -- Brief §60: a graded activity must know where its points go.
    CONSTRAINT ck_activity_graded_component CHECK (NOT is_graded OR assessment_component_id IS NOT NULL),
    CONSTRAINT ck_activity_graded_points    CHECK (NOT is_graded OR max_points IS NOT NULL)
);
CREATE INDEX idx_activities_course ON activities(course_id, status);
CREATE INDEX idx_activities_lesson ON activities(lesson_id, position);
CREATE INDEX idx_activities_module ON activities(module_id, position);
CREATE INDEX idx_activities_type   ON activities(activity_type);
CREATE INDEX idx_activities_reference ON activities(activity_type, reference_id);
CREATE TRIGGER trg_activities_updated_at BEFORE UPDATE ON activities
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE activities IS
 'Polymorphic graded work. reference_id points at a quiz / coding_task / (future) other
  artifact, resolved by the ActivityHandler registered for the activity_type. No code in this
  repository may branch on a subject code or subject name.';

CREATE TABLE activity_translations (
    activity_id  uuid NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    locale       text NOT NULL REFERENCES locales(code),
    title        text NOT NULL,
    instructions text,
    PRIMARY KEY (activity_id, locale)
);

CREATE TABLE activity_variants (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id  uuid NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    variant_no   int NOT NULL CHECK (variant_no >= 1),
    reference_id uuid,
    payload      jsonb NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT uq_activity_variant UNIQUE (activity_id, variant_no)
);
COMMENT ON TABLE activity_variants IS
 'DAS1110: "mustaqil ish topshiriqlari variativ tavsifga ega bolishi lozim".
  Assignment is deterministic: hash(student_user_id, activity_id) % variant_count.';
