-- ============================================================================
-- V004 — Subject catalogue, programme mapping, prerequisites, learning outcomes.
-- Evidence: docs/academic-analysis/syllabus-analysis.md §1
-- ============================================================================

CREATE TABLE subjects (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code                text NOT NULL,                      -- IAKTT1105, DAS1110
    department_id        uuid NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    subject_kind        text NOT NULL DEFAULT 'MANDATORY'
                        CHECK (subject_kind IN ('MANDATORY','ELECTIVE','OPTIONAL')),
    default_instruction_language text NOT NULL DEFAULT 'uz' REFERENCES locales(code),
    status              text NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    published_at        timestamptz,
    deleted_at          timestamptz,
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now(),
    created_by          uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by          uuid REFERENCES users(id) ON DELETE SET NULL,
    version             bigint NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX uq_subjects_code ON subjects(code) WHERE deleted_at IS NULL;
CREATE INDEX idx_subjects_department ON subjects(department_id);
CREATE INDEX idx_subjects_status     ON subjects(status) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_subjects_updated_at BEFORE UPDATE ON subjects
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE subjects IS
 'The stable catalogue entity ("fan"). Lessons NEVER hang off a subject - they hang off a course
  (one delivery in one term). DAS1110 spans two semesters with different credits, which is the
  proof this split is required. See docs/domain/domain-model.md §1.';

CREATE TABLE subject_translations (
    subject_id  uuid NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    locale      text NOT NULL REFERENCES locales(code),
    title       text NOT NULL CHECK (length(btrim(title)) > 0),
    short_title text,
    summary     text,
    description text,
    aim         text,        -- "Fan maqsadi"
    objectives  text,        -- "Fanning vazifasi"
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector(fts_config(locale), coalesce(title, '')),       'A') ||
        setweight(to_tsvector(fts_config(locale), coalesce(summary, '')),     'B') ||
        setweight(to_tsvector(fts_config(locale), coalesce(description, '')), 'C')
    ) STORED,
    PRIMARY KEY (subject_id, locale)
);
CREATE INDEX idx_subject_tr_fts   ON subject_translations USING gin (search_vector);
CREATE INDEX idx_subject_tr_title ON subject_translations USING gin (title gin_trgm_ops);

CREATE TABLE subject_programmes (
    subject_id        uuid NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    programme_id      uuid NOT NULL REFERENCES academic_programmes(id) ON DELETE CASCADE,
    curriculum_ordinal text,     -- e.g. "1.09" from the DAS1110 curriculum
    recommended_year  int CHECK (recommended_year BETWEEN 1 AND 7),
    recommended_term  int CHECK (recommended_term BETWEEN 1 AND 14),
    PRIMARY KEY (subject_id, programme_id)
);
CREATE INDEX idx_subject_programmes_programme ON subject_programmes(programme_id);

CREATE TABLE subject_prerequisites (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id              uuid NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    prerequisite_subject_id uuid REFERENCES subjects(id) ON DELETE RESTRICT,
    requirement_type        text NOT NULL DEFAULT 'REQUIRED'
                            CHECK (requirement_type IN ('REQUIRED','RECOMMENDED')),
    -- Some syllabi state prerequisites as prose only (IAKTT1105: "boshlangich matematik
    -- bilimlar"). Keep the verbatim text when there is no coded subject to point at.
    free_text_note          text,
    created_at              timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_prereq_not_self CHECK (prerequisite_subject_id IS NULL
                                         OR prerequisite_subject_id <> subject_id),
    CONSTRAINT ck_prereq_has_target CHECK (prerequisite_subject_id IS NOT NULL
                                           OR free_text_note IS NOT NULL)
);
CREATE UNIQUE INDEX uq_subject_prereq ON subject_prerequisites(subject_id, prerequisite_subject_id)
    WHERE prerequisite_subject_id IS NOT NULL;

CREATE TABLE learning_outcomes (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id    uuid NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    code          text NOT NULL,        -- TN1, TN2, ...
    category      text NOT NULL
                  CHECK (category IN ('KNOWLEDGE','SKILL','COMPETENCY','ATTITUDE')),
    display_order int NOT NULL DEFAULT 0,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_learning_outcome_code UNIQUE (subject_id, code)
);
CREATE TRIGGER trg_learning_outcomes_updated_at BEFORE UPDATE ON learning_outcomes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE learning_outcomes IS
 '"Tailim natijalari" TN1..TNn. Categories mirror the headings used by both syllabi:
  "Bilimlar jihatidan" -> KNOWLEDGE, "Konikmalar" -> SKILL, "Kasbiy kompetensiyalar" -> COMPETENCY.';

CREATE TABLE learning_outcome_translations (
    learning_outcome_id uuid NOT NULL REFERENCES learning_outcomes(id) ON DELETE CASCADE,
    locale              text NOT NULL REFERENCES locales(code),
    statement           text NOT NULL,
    PRIMARY KEY (learning_outcome_id, locale)
);
