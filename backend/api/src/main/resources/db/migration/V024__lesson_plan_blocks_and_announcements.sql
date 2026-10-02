-- ============================================================================
-- V024 — The teacher's lesson plan, and announcements (product spec T-03.4, T-11).
-- ============================================================================

-- ---------------------------------------------------------------- lesson plan block
-- A LESSON_PLAN block is the teacher's plan of a class (objectives, timing, materials, questions,
-- homework). It is always TEACHER_ONLY: students never receive it.
ALTER TABLE content_blocks DROP CONSTRAINT content_blocks_block_type_check;
ALTER TABLE content_blocks ADD CONSTRAINT content_blocks_block_type_check CHECK (block_type IN
    ('TEXT','IMAGE','VIDEO','AUDIO','CODE_EXAMPLE','CALLOUT','DIAGRAM','TABLE',
     'CASE_STUDY','FILE','LINK','EMBED','QUIZ_REF','CODING_TASK_REF','ACTIVITY_REF','LESSON_PLAN'));
ALTER TABLE content_blocks ADD CONSTRAINT ck_content_block_lesson_plan_private
    CHECK (block_type <> 'LESSON_PLAN' OR visibility = 'TEACHER_ONLY');

-- ---------------------------------------------------------------- announcements
CREATE TABLE announcements (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Exactly one audience: the students of a course, or a student group.
    course_id      uuid REFERENCES courses(id) ON DELETE CASCADE,
    group_id       uuid REFERENCES student_groups(id) ON DELETE CASCADE,
    author_user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    title          text NOT NULL CHECK (length(btrim(title)) > 0),
    body           text NOT NULL,          -- rich text (HTML), sanitized on write
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_announcement_audience CHECK ((course_id IS NULL) <> (group_id IS NULL))
);
CREATE INDEX idx_announcements_course ON announcements(course_id, created_at DESC);
CREATE INDEX idx_announcements_group  ON announcements(group_id, created_at DESC);
CREATE INDEX idx_announcements_author ON announcements(author_user_id);
CREATE TRIGGER trg_announcements_updated_at BEFORE UPDATE ON announcements
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE announcements IS
 'A message from a teacher or the study office to the students of one course or one group.
  Posting one also sends each recipient an ANNOUNCEMENT notification.';

-- No new permission: ANNOUNCEMENT_WRITE exists since V003 and teachers and admins hold it.
