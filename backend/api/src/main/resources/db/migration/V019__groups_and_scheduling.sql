-- ============================================================================
-- V019 — Student groups as an entity, and the timetable ("dars jadvali"):
--        rooms, the bell schedule (pair slots), holidays, timetables per term,
--        entries, one-off exceptions, and the planned teaching load ("yuklama").
--
-- Product spec: docs/01-PRODUCT-SPEC.md S-02, T-02, A-02..A-04. Design: docs/02-ARCHITECTURE.md §4.
-- ============================================================================

-- ---------------------------------------------------------------- groups
CREATE TABLE student_groups (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code                 text NOT NULL UNIQUE CHECK (length(btrim(code)) > 0),   -- "BK-DI26/1"
    programme_id         uuid REFERENCES academic_programmes(id) ON DELETE RESTRICT,
    course_year          int CHECK (course_year BETWEEN 1 AND 6),
    admission_year       int CHECK (admission_year BETWEEN 1990 AND 2200),
    study_form           text NOT NULL DEFAULT 'FULL_TIME'
                         CHECK (study_form IN ('FULL_TIME','PART_TIME','EVENING','DISTANCE')),
    instruction_language text NOT NULL DEFAULT 'uz' REFERENCES locales(code),
    planned_size         int CHECK (planned_size >= 0),
    curator_user_id      uuid REFERENCES users(id) ON DELETE SET NULL,
    status               text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','ARCHIVED')),
    created_at           timestamptz NOT NULL DEFAULT now(),
    updated_at           timestamptz NOT NULL DEFAULT now(),
    version              bigint NOT NULL DEFAULT 0
);
CREATE INDEX idx_student_groups_programme ON student_groups(programme_id);
CREATE INDEX idx_student_groups_curator   ON student_groups(curator_user_id);
CREATE TRIGGER trg_student_groups_updated_at BEFORE UPDATE ON student_groups
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE student_groups IS
 'An academic group ("guruh"). student_profiles.group_code references code, so the code a
  registrar types stays the membership key and old import paths keep working.';

-- Existing free-text group codes become real groups.
UPDATE student_profiles SET group_code = NULLIF(btrim(group_code), '');
INSERT INTO student_groups (code)
SELECT DISTINCT group_code FROM student_profiles WHERE group_code IS NOT NULL
ON CONFLICT (code) DO NOTHING;

-- A student created or imported with a group code the registry does not know yet must not fail:
-- the group is created on the fly (status ACTIVE, details filled in later by the admin).
CREATE OR REPLACE FUNCTION ensure_student_group() RETURNS trigger AS $$
BEGIN
    NEW.group_code := NULLIF(btrim(NEW.group_code), '');
    IF NEW.group_code IS NOT NULL THEN
        INSERT INTO student_groups (code) VALUES (NEW.group_code) ON CONFLICT (code) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_student_profiles_group BEFORE INSERT OR UPDATE OF group_code ON student_profiles
    FOR EACH ROW EXECUTE FUNCTION ensure_student_group();

ALTER TABLE student_profiles
    ADD CONSTRAINT fk_student_profiles_group FOREIGN KEY (group_code)
    REFERENCES student_groups(code) ON UPDATE CASCADE ON DELETE SET NULL;

-- ---------------------------------------------------------------- rooms, bell schedule, holidays
CREATE TABLE rooms (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    building   text NOT NULL DEFAULT '',
    number     text NOT NULL CHECK (length(btrim(number)) > 0),
    name       text,
    capacity   int NOT NULL CHECK (capacity > 0),
    room_type  text NOT NULL DEFAULT 'CLASSROOM'
               CHECK (room_type IN ('LECTURE_HALL','CLASSROOM','COMPUTER_LAB','LABORATORY','OTHER')),
    is_active  boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version    bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_room UNIQUE (building, number)
);
CREATE TRIGGER trg_rooms_updated_at BEFORE UPDATE ON rooms
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE pair_slots (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    pair_no    int NOT NULL UNIQUE CHECK (pair_no BETWEEN 1 AND 12),
    starts_at  time NOT NULL,
    ends_at    time NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_pair_slot_range CHECK (ends_at > starts_at)
);
CREATE TRIGGER trg_pair_slots_updated_at BEFORE UPDATE ON pair_slots
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE pair_slots IS
 'The bell schedule: one row per "juftlik" (pair). The six rows below are a DEFAULT the admin is
  expected to correct to the real AIFU schedule (PENDING-INPUTS.md); nothing in code depends on
  the times.';
INSERT INTO pair_slots (pair_no, starts_at, ends_at) VALUES
 (1, '08:30', '09:50'), (2, '10:00', '11:20'), (3, '11:30', '12:50'),
 (4, '13:30', '14:50'), (5, '15:00', '16:20'), (6, '16:30', '17:50');

CREATE TABLE holidays (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    on_date    date NOT NULL UNIQUE,
    name       text NOT NULL CHECK (length(btrim(name)) > 0),
    created_at timestamptz NOT NULL DEFAULT now()
);

-- Odd/even ("toq/juft") weeks are counted from the term's first week.
ALTER TABLE terms ADD COLUMN first_week_parity text NOT NULL DEFAULT 'ODD'
    CHECK (first_week_parity IN ('ODD','EVEN'));

-- ---------------------------------------------------------------- timetable
CREATE TABLE timetables (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    term_id      uuid NOT NULL REFERENCES terms(id) ON DELETE RESTRICT,
    name         text NOT NULL CHECK (length(btrim(name)) > 0),
    status       text NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    published_at timestamptz,
    published_by uuid REFERENCES users(id) ON DELETE SET NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    created_by   uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by   uuid REFERENCES users(id) ON DELETE SET NULL,
    version      bigint NOT NULL DEFAULT 0
);
CREATE INDEX idx_timetables_term ON timetables(term_id);
-- Students and teachers read exactly one timetable per term.
CREATE UNIQUE INDEX uq_timetable_published ON timetables(term_id) WHERE status = 'PUBLISHED';
CREATE TRIGGER trg_timetables_updated_at BEFORE UPDATE ON timetables
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE timetable_entries (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    timetable_id    uuid NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,
    course_id       uuid NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    session_type    text NOT NULL CHECK (session_type IN ('LECTURE','SEMINAR','PRACTICAL','LABORATORY')),
    teacher_user_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    room_id         uuid REFERENCES rooms(id) ON DELETE RESTRICT,          -- NULL = online / no room
    day_of_week     int NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),      -- ISO: 1 = Monday
    pair_slot_id    uuid NOT NULL REFERENCES pair_slots(id) ON DELETE RESTRICT,
    week_parity     text NOT NULL DEFAULT 'EVERY' CHECK (week_parity IN ('EVERY','ODD','EVEN')),
    valid_from      date,
    valid_to        date,
    subgroup        text CHECK (subgroup IS NULL OR length(btrim(subgroup)) > 0),
    note            text,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    created_by      uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by      uuid REFERENCES users(id) ON DELETE SET NULL,
    version         bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_timetable_entry_range CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_to >= valid_from)
);
CREATE INDEX idx_tt_entries_timetable ON timetable_entries(timetable_id, day_of_week, pair_slot_id);
CREATE INDEX idx_tt_entries_teacher   ON timetable_entries(teacher_user_id);
CREATE INDEX idx_tt_entries_room      ON timetable_entries(room_id);
CREATE INDEX idx_tt_entries_course    ON timetable_entries(course_id);
CREATE INDEX idx_tt_entries_slot      ON timetable_entries(pair_slot_id);
CREATE TRIGGER trg_tt_entries_updated_at BEFORE UPDATE ON timetable_entries
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE timetable_entries IS
 'One recurring class. Double booking (teacher / room / group in the same slot with overlapping
  parity and dates) is refused by TimetableConflictDetector; writers hold a row lock on the
  timetable, so two admins cannot both pass the check.';

CREATE TABLE timetable_entry_groups (
    entry_id uuid NOT NULL REFERENCES timetable_entries(id) ON DELETE CASCADE,
    group_id uuid NOT NULL REFERENCES student_groups(id) ON DELETE RESTRICT,
    PRIMARY KEY (entry_id, group_id)
);
CREATE INDEX idx_tt_entry_groups_group ON timetable_entry_groups(group_id);

CREATE TABLE timetable_exceptions (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id            uuid NOT NULL REFERENCES timetable_entries(id) ON DELETE CASCADE,
    on_date             date NOT NULL,
    kind                text NOT NULL CHECK (kind IN ('CANCELLED','MOVED','REPLACED')),
    new_date            date,
    new_pair_slot_id    uuid REFERENCES pair_slots(id) ON DELETE RESTRICT,
    new_room_id         uuid REFERENCES rooms(id) ON DELETE RESTRICT,
    new_teacher_user_id uuid REFERENCES users(id) ON DELETE RESTRICT,
    reason              text,
    created_at          timestamptz NOT NULL DEFAULT now(),
    created_by          uuid REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_tt_exception UNIQUE (entry_id, on_date),
    CONSTRAINT ck_tt_exception_moved CHECK (kind <> 'MOVED' OR (new_date IS NOT NULL AND new_pair_slot_id IS NOT NULL))
);
CREATE INDEX idx_tt_exceptions_new_date ON timetable_exceptions(new_date);
CREATE INDEX idx_tt_exceptions_slot     ON timetable_exceptions(new_pair_slot_id);
CREATE INDEX idx_tt_exceptions_room     ON timetable_exceptions(new_room_id);
CREATE INDEX idx_tt_exceptions_teacher  ON timetable_exceptions(new_teacher_user_id);

-- ---------------------------------------------------------------- planned teaching load
CREATE TABLE teaching_load_items (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    term_id         uuid NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    course_id       uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    group_id        uuid NOT NULL REFERENCES student_groups(id) ON DELETE CASCADE,
    session_type    text NOT NULL CHECK (session_type IN ('LECTURE','SEMINAR','PRACTICAL','LABORATORY')),
    teacher_user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    planned_hours   int NOT NULL CHECK (planned_hours >= 0),      -- academic hours for the term
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_teaching_load UNIQUE (term_id, course_id, group_id, session_type)
);
CREATE INDEX idx_teaching_load_course  ON teaching_load_items(course_id);
CREATE INDEX idx_teaching_load_group   ON teaching_load_items(group_id);
CREATE INDEX idx_teaching_load_teacher ON teaching_load_items(teacher_user_id);
CREATE TRIGGER trg_teaching_load_updated_at BEFORE UPDATE ON teaching_load_items
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE teaching_load_items IS
 'The department''s "yuklama": hours a group must receive for a course and session type in a
  term. The timetable builder compares it with the hours actually scheduled.';

-- ---------------------------------------------------------------- permissions
INSERT INTO permissions (code, description, domain) VALUES
 ('GROUP_READ','View student groups and their rosters','scheduling'),
 ('GROUP_MANAGE','Create and edit student groups, move students between groups','scheduling'),
 ('TIMETABLE_READ','View the published timetable','scheduling'),
 ('TIMETABLE_MANAGE','Build and publish timetables; manage rooms, bell schedule, holidays, load','scheduling');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
  ON p.code IN ('GROUP_READ','GROUP_MANAGE','TIMETABLE_READ','TIMETABLE_MANAGE')
WHERE r.code = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('GROUP_READ','TIMETABLE_READ')
WHERE r.code = 'TEACHER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('TIMETABLE_READ')
WHERE r.code = 'STUDENT';

-- ---------------------------------------------------------------- notification type
ALTER TABLE notifications DROP CONSTRAINT notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check CHECK (type IN
    ('ENROLLMENT_CREATED','LESSON_COMPLETED','QUIZ_GRADED','SUBMISSION_GRADED',
     'TEACHER_FEEDBACK','ANNOUNCEMENT','GRADE_PUBLISHED','DEADLINE_REMINDER',
     'ACCOUNT_STATUS_CHANGED','TIMETABLE_CHANGED'));
