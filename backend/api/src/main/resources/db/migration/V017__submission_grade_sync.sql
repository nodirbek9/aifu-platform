-- ============================================================================
-- V017 — Bridges the execution-worker (separate process, restricted role,
-- V014) to grading (ADR-011/ADR-007), the same shape V016 already gives
-- progress. A SEPARATE marker column from V016's progress_synced_at: grading
-- and progress are independent consumers polling the same submissions table,
-- and conflating their own bookkeeping into one column would make each
-- consumer's sweep interfere with the other's completeness.
-- ============================================================================

ALTER TABLE submissions ADD COLUMN grade_synced_at timestamptz;
CREATE INDEX idx_submissions_grade_sync ON submissions(status)
    WHERE grade_synced_at IS NULL;

COMMENT ON COLUMN submissions.grade_synced_at IS
 'Set by grading.app.GradeEntryService''s scheduled sweeper once a terminal submission''s
  grade_entry has been recorded. NULL means not yet processed. The execution-worker role
  (V014) has no grant on this column — same reasoning as progress_synced_at (V016).';
