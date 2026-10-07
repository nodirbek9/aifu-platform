-- ============================================================================
-- V027 — Announcements to the whole university, to all students, or to all teachers.
--
-- Until now an announcement went to the students of one course or of one group. The study office
-- also writes to everyone at once: a holiday, an exam week, a meeting of all teachers. audience
-- says who it is for; course_id / group_id are set only for COURSE / GROUP.
-- ============================================================================

ALTER TABLE announcements
    ADD COLUMN audience text NOT NULL DEFAULT 'COURSE'
        CHECK (audience IN ('COURSE', 'GROUP', 'EVERYONE', 'STUDENTS', 'TEACHERS'));

UPDATE announcements SET audience = CASE WHEN course_id IS NOT NULL THEN 'COURSE' ELSE 'GROUP' END;

ALTER TABLE announcements ALTER COLUMN audience DROP DEFAULT;
ALTER TABLE announcements DROP CONSTRAINT ck_announcement_audience;
ALTER TABLE announcements ADD CONSTRAINT ck_announcement_audience CHECK (
       (audience = 'COURSE' AND course_id IS NOT NULL AND group_id IS NULL)
    OR (audience = 'GROUP' AND group_id IS NOT NULL AND course_id IS NULL)
    OR (audience IN ('EVERYONE', 'STUDENTS', 'TEACHERS') AND course_id IS NULL AND group_id IS NULL));

-- an insert that names only its course or its group (older seeds, scripts) gets the audience
-- those imply; NOT NULL is checked after this trigger
CREATE FUNCTION announcement_default_audience() RETURNS trigger AS $$
BEGIN
    IF NEW.audience IS NULL THEN
        NEW.audience := CASE WHEN NEW.course_id IS NOT NULL THEN 'COURSE'
                             WHEN NEW.group_id IS NOT NULL THEN 'GROUP' END;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_announcements_default_audience BEFORE INSERT ON announcements
    FOR EACH ROW EXECUTE FUNCTION announcement_default_audience();

CREATE INDEX idx_announcements_audience ON announcements(audience, created_at DESC)
    WHERE audience IN ('EVERYONE', 'STUDENTS', 'TEACHERS');

COMMENT ON COLUMN announcements.audience IS
 'COURSE (its students), GROUP (its students), EVERYONE, STUDENTS or TEACHERS (the study office only).';
