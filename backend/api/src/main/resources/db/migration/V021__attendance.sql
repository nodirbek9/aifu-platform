-- ============================================================================
-- V021 — Class sessions and attendance (product spec T-03, T-10, S-10, T-04.3).
--
-- A class session is one class actually on the calendar: a course's class for some groups on a
-- date and pair. It carries what the teacher records about that class: the lesson (topic) taught,
-- private notes, and the attendance of each student.
--
-- A session is identified by WHAT it is (course, date, pair, groups, subgroup), not by the
-- timetable entry it came from: publishing a corrected copy of a timetable gives every class a new
-- entry id, and the journal must survive that. entry_id is only a pointer back, and may go NULL.
-- ============================================================================

CREATE TABLE class_sessions (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id            uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    on_date              date NOT NULL,
    pair_no              int  NOT NULL CHECK (pair_no BETWEEN 1 AND 12),
    session_type         text NOT NULL CHECK (session_type IN ('LECTURE','SEMINAR','PRACTICAL','LABORATORY')),
    -- The class's group ids, sorted and comma-joined: part of the identity (see the header).
    groups_key           text NOT NULL CHECK (length(groups_key) > 0),
    subgroup             text NOT NULL DEFAULT '',          -- '' = the whole group
    entry_id             uuid REFERENCES timetable_entries(id) ON DELETE SET NULL,
    teacher_user_id      uuid REFERENCES users(id) ON DELETE SET NULL,
    lesson_id            uuid REFERENCES lessons(id) ON DELETE SET NULL,
    topic                text,                              -- free text when no lesson fits
    notes                text,                              -- the teacher's private notes
    attendance_marked_at timestamptz,
    attendance_marked_by uuid REFERENCES users(id) ON DELETE SET NULL,
    created_at           timestamptz NOT NULL DEFAULT now(),
    updated_at           timestamptz NOT NULL DEFAULT now(),
    created_by           uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by           uuid REFERENCES users(id) ON DELETE SET NULL,
    version              bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_class_session UNIQUE (course_id, on_date, pair_no, groups_key, subgroup)
);
CREATE INDEX idx_class_sessions_course  ON class_sessions(course_id, on_date);
CREATE INDEX idx_class_sessions_entry   ON class_sessions(entry_id);
CREATE INDEX idx_class_sessions_teacher ON class_sessions(teacher_user_id);
CREATE INDEX idx_class_sessions_lesson  ON class_sessions(lesson_id);
CREATE TRIGGER trg_class_sessions_updated_at BEFORE UPDATE ON class_sessions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE class_sessions IS
 'One class on a concrete date: the lesson taught, the teacher''s notes and (through
  attendance_records) who was there. Created the first time a teacher saves something for it.';

CREATE TABLE class_session_groups (
    session_id uuid NOT NULL REFERENCES class_sessions(id) ON DELETE CASCADE,
    group_id   uuid NOT NULL REFERENCES student_groups(id) ON DELETE RESTRICT,
    PRIMARY KEY (session_id, group_id)
);
CREATE INDEX idx_class_session_groups_group ON class_session_groups(group_id);

CREATE TABLE attendance_records (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id      uuid NOT NULL REFERENCES class_sessions(id) ON DELETE CASCADE,
    student_user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          text NOT NULL CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED')),
    note            text,
    marked_by       uuid REFERENCES users(id) ON DELETE SET NULL,
    marked_at       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_attendance_record UNIQUE (session_id, student_user_id)
);
CREATE INDEX idx_attendance_student ON attendance_records(student_user_id);
CREATE INDEX idx_attendance_marker  ON attendance_records(marked_by);
COMMENT ON TABLE attendance_records IS
 'One student''s mark for one class. A student with no row was simply not marked (for example the
  other half of a split group); only marked classes count towards the attendance percentage.';

-- ---------------------------------------------------------------- permissions
INSERT INTO permissions (code, description, domain) VALUES
 ('ATTENDANCE_MANAGE','Open class sessions, link the lesson taught, mark attendance, read the journal','attendance'),
 ('ATTENDANCE_SELF_READ','View one''s own attendance','attendance');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('ATTENDANCE_MANAGE')
WHERE r.code IN ('ADMIN','TEACHER');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('ATTENDANCE_SELF_READ')
WHERE r.code = 'STUDENT';
