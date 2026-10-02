-- ============================================================================
-- V005 — Academic calendar, courses (a subject delivered in a term), teachers,
--        hour budgets, assessment rubric, grade scales, materials, syllabus approval.
-- ============================================================================

CREATE TABLE academic_years (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code           text NOT NULL UNIQUE,        -- "2025/2026"
    starts_on      date NOT NULL,
    ends_on        date NOT NULL,
    is_current     boolean NOT NULL DEFAULT false,
    is_placeholder boolean NOT NULL DEFAULT false,   -- see PENDING-INPUTS.md §4
    CONSTRAINT ck_academic_year_range CHECK (ends_on > starts_on)
);
CREATE UNIQUE INDEX uq_academic_year_current ON academic_years(is_current) WHERE is_current;

CREATE TABLE terms (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    academic_year_id uuid NOT NULL REFERENCES academic_years(id) ON DELETE CASCADE,
    ordinal          int NOT NULL CHECK (ordinal BETWEEN 1 AND 4),   -- semester number
    name             text NOT NULL,
    starts_on        date NOT NULL,
    ends_on          date NOT NULL,
    is_placeholder   boolean NOT NULL DEFAULT false,
    CONSTRAINT uq_term_ordinal UNIQUE (academic_year_id, ordinal),
    CONSTRAINT ck_term_range CHECK (ends_on > starts_on)
);

CREATE TABLE courses (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id            uuid NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT,
    term_id               uuid NOT NULL REFERENCES terms(id) ON DELETE RESTRICT,
    programme_id          uuid REFERENCES academic_programmes(id) ON DELETE RESTRICT,
    cohort_code           text NOT NULL DEFAULT 'DEFAULT',   -- distinguishes parallel streams
    credits               numeric(4,1) NOT NULL CHECK (credits > 0 AND credits <= 60),
    total_hours           int NOT NULL CHECK (total_hours > 0),
    weekly_contact_hours  numeric(4,1) CHECK (weekly_contact_hours >= 0),
    instruction_language  text NOT NULL DEFAULT 'uz' REFERENCES locales(code),
    required_locales      text[] NOT NULL DEFAULT ARRAY['uz'],   -- ADR-005: publish gate
    delivery_mode         text NOT NULL DEFAULT 'FULL_TIME'
                          CHECK (delivery_mode IN ('FULL_TIME','PART_TIME','EVENING','DISTANCE')),
    assessment_form       text CHECK (assessment_form IN ('EXAM','CREDIT','DIFFERENTIATED_CREDIT','PROJECT')),
    grade_scale_id        uuid,       -- FK added after grade_scales below
    status                text NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED','CLOSED')),
    enrollment_opens_at   timestamptz,
    enrollment_closes_at  timestamptz,
    max_enrollment        int CHECK (max_enrollment > 0),
    published_at          timestamptz,
    deleted_at            timestamptz,
    created_at            timestamptz NOT NULL DEFAULT now(),
    updated_at            timestamptz NOT NULL DEFAULT now(),
    created_by            uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by            uuid REFERENCES users(id) ON DELETE SET NULL,
    version               bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_course_enrollment_window CHECK (enrollment_closes_at IS NULL
        OR enrollment_opens_at IS NULL OR enrollment_closes_at > enrollment_opens_at),
    CONSTRAINT ck_course_required_locales CHECK (cardinality(required_locales) > 0)
);
CREATE UNIQUE INDEX uq_course_delivery ON courses(subject_id, term_id, cohort_code)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_courses_term    ON courses(term_id);
CREATE INDEX idx_courses_status  ON courses(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_courses_subject ON courses(subject_id);
CREATE TRIGGER trg_courses_updated_at BEFORE UPDATE ON courses
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE courses IS
 'One delivery of a subject in one term. Credits/hours live HERE, not on the subject:
  DAS1110 is 6 credits/180h in semester 1 and 5 credits/150h in semester 2.';

CREATE TABLE course_translations (
    course_id   uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    locale      text NOT NULL REFERENCES locales(code),
    title       text NOT NULL CHECK (length(btrim(title)) > 0),
    description text,
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector(fts_config(locale), coalesce(title, '')),       'A') ||
        setweight(to_tsvector(fts_config(locale), coalesce(description, '')), 'C')
    ) STORED,
    PRIMARY KEY (course_id, locale)
);
CREATE INDEX idx_course_tr_fts ON course_translations USING gin (search_vector);

CREATE TABLE course_teachers (
    course_id       uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    teacher_user_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    teacher_role    text NOT NULL DEFAULT 'LEAD'
                    CHECK (teacher_role IN ('LEAD','ASSISTANT','GRADER','GUEST')),
    assigned_at     timestamptz NOT NULL DEFAULT now(),
    assigned_by     uuid REFERENCES users(id) ON DELETE SET NULL,
    PRIMARY KEY (course_id, teacher_user_id)
);
CREATE INDEX idx_course_teachers_teacher ON course_teachers(teacher_user_id);
COMMENT ON TABLE course_teachers IS
 'Many-to-many: a teacher teaches many courses, a course may have many teachers (brief §7).
  This table is the authority for CourseAccessGuard.requireTeacherOf().';

-- The institutional hour budget per session type: 30/30/90 (IAKTT1105), 60/60/210 (DAS1110).
CREATE TABLE course_session_allocations (
    course_id       uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    session_type    text NOT NULL CHECK (session_type IN
                    ('LECTURE','SEMINAR','PRACTICAL','LABORATORY','INDEPENDENT_STUDY','COURSE_WORK')),
    allocated_hours int NOT NULL CHECK (allocated_hours >= 0),
    PRIMARY KEY (course_id, session_type)
);
COMMENT ON TABLE course_session_allocations IS
 'Declared hour budget. HourBudgetValidator reconciles it against the sum of lesson hours.
  LABORATORY is retained because the institutional form contains it, even though both
  supplied syllabi allocate zero lab hours (PENDING-INPUTS.md §11).';

-- Machine-computable version of "Talabalar bilimini baholash". Tree-shaped: JN has children.
CREATE TABLE assessment_components (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id           uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    parent_component_id uuid REFERENCES assessment_components(id) ON DELETE CASCADE,
    code                text NOT NULL,        -- JN, JN_ATTENDANCE, ON, YaN
    kind                text NOT NULL CHECK (kind IN
                        ('CONTINUOUS','MIDTERM','FINAL','ATTENDANCE','INDEPENDENT_WORK',
                         'PRACTICAL_ACTIVITY','PROJECT','COURSE_WORK')),
    max_points          numeric(6,2) NOT NULL CHECK (max_points > 0),
    min_pass_percent    numeric(5,2) CHECK (min_pass_percent BETWEEN 0 AND 100),
    blocks_final        boolean NOT NULL DEFAULT false,
    assessment_form     text CHECK (assessment_form IN ('WRITTEN','ORAL','MIXED','PRACTICAL','TEST','PROJECT')),
    display_order       int NOT NULL DEFAULT 0,
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_assessment_component_code UNIQUE (course_id, code),
    CONSTRAINT ck_component_not_self_parent CHECK (parent_component_id IS NULL OR parent_component_id <> id)
);
CREATE INDEX idx_assessment_components_course ON assessment_components(course_id);
CREATE INDEX idx_assessment_components_parent ON assessment_components(parent_component_id);
CREATE TRIGGER trg_assessment_components_updated_at BEFORE UPDATE ON assessment_components
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON COLUMN assessment_components.blocks_final IS
 'IAKTT1105: a student earning <60% of JN or of ON is NOT admitted to the final exam.
  min_pass_percent + blocks_final encode that gate; FinalScoreCalculator applies it.';

CREATE TABLE grade_scales (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code       text NOT NULL UNIQUE,
    name       text NOT NULL,
    max_score  numeric(6,2) NOT NULL DEFAULT 100,
    pass_score numeric(6,2) NOT NULL DEFAULT 60
);
CREATE TABLE grade_scale_bands (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    grade_scale_id uuid NOT NULL REFERENCES grade_scales(id) ON DELETE CASCADE,
    min_score      numeric(6,2) NOT NULL,
    max_score      numeric(6,2) NOT NULL,
    symbol         text NOT NULL,       -- '5','4','3','2'
    label          text NOT NULL,
    is_passing     boolean NOT NULL,
    CONSTRAINT ck_band_range CHECK (max_score >= min_score),
    CONSTRAINT uq_band_symbol UNIQUE (grade_scale_id, symbol)
);
ALTER TABLE courses
    ADD CONSTRAINT fk_courses_grade_scale
    FOREIGN KEY (grade_scale_id) REFERENCES grade_scales(id) ON DELETE RESTRICT;
CREATE INDEX idx_courses_grade_scale ON courses(grade_scale_id);

-- Verbatim from the DAS1110 "Talabalar bilimini baholash mezoni" section.
INSERT INTO grade_scales (code, name, max_score, pass_score)
VALUES ('AIFU_100_5', 'AIFU 100-point scale mapped to the 5-point national scale', 100, 60);
INSERT INTO grade_scale_bands (grade_scale_id, min_score, max_score, symbol, label, is_passing)
SELECT id, v.lo, v.hi, v.sym, v.lbl, v.pass FROM grade_scales,
 (VALUES (90,100,'5','A''lo (excellent)',true),
         (80,89.99,'4','Yaxshi (good)',true),
         (60,79.99,'3','Qoniqarli (satisfactory)',true),
         (0,59.99,'2','Qoniqarsiz (unsatisfactory)',false)) AS v(lo,hi,sym,lbl,pass)
WHERE code = 'AIFU_100_5';

CREATE TABLE course_materials (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id          uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    material_type      text NOT NULL CHECK (material_type IN
                       ('PRIMARY_LITERATURE','SUPPLEMENTARY_LITERATURE','WEB_RESOURCE',
                        'EXTERNAL_COURSE','LEGAL_DOCUMENT','SOFTWARE','OTHER')),
    position           int NOT NULL DEFAULT 0,
    citation           text,
    url                text,
    file_id            uuid,            -- FK added in V013 (files)
    source_institution text,            -- e.g. "MIT", "UC Berkeley", "NUS", "LSE"
    ranking_note       text,            -- e.g. "QS 14 / THE 2"
    mapped_topic       text,            -- the local topic the benchmark maps to
    created_at         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_material_has_source CHECK (citation IS NOT NULL OR url IS NOT NULL OR file_id IS NOT NULL)
);
CREATE INDEX idx_course_materials_course ON course_materials(course_id, material_type);
COMMENT ON COLUMN course_materials.source_institution IS
 'Supports the IAKTT1105 QS/THE Top-300 benchmarking table: enrichment links attached to the
  exact topic they map to, instead of an appendix.';

-- Approval trail: both documents carry kafedra + Kengash protocol numbers and named reviewers.
CREATE TABLE syllabus_revisions (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id           uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    revision            int NOT NULL CHECK (revision >= 1),
    status              text NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','UNDER_REVIEW','APPROVED','SUPERSEDED','REJECTED')),
    authors             jsonb NOT NULL DEFAULT '[]'::jsonb,
    reviewers           jsonb NOT NULL DEFAULT '[]'::jsonb,
    department_protocol_no   text,
    department_protocol_date date,
    council_protocol_no      text,
    council_protocol_date    date,
    approved_by         uuid REFERENCES users(id) ON DELETE SET NULL,
    approved_at         timestamptz,
    source_document_ref text,     -- filename under academic-materials/syllabi/
    notes               text,
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_syllabus_revision UNIQUE (course_id, revision)
);
CREATE TRIGGER trg_syllabus_revisions_updated_at BEFORE UPDATE ON syllabus_revisions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
