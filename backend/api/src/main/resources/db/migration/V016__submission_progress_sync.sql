-- ============================================================================
-- V016 — Bridges the execution-worker (separate process, restricted role,
-- V014) back to progress (ADR-007's "progress recompute" scheduled work).
-- The worker cannot write this column — V014's grant list deliberately omits
-- it — only the API's own scheduled sweeper (progress.app.ProgressService)
-- ever sets it, after it has recorded the activity attempt.
-- ============================================================================

ALTER TABLE submissions ADD COLUMN progress_synced_at timestamptz;
CREATE INDEX idx_submissions_progress_sync ON submissions(status)
    WHERE progress_synced_at IS NULL;

COMMENT ON COLUMN submissions.progress_synced_at IS
 'Set by progress.app.ProgressService''s scheduled sweeper once a terminal submission''s
  activity attempt has been recorded into activity_progress. NULL means not yet processed.
  The execution-worker role (V014) has no grant on this column.';
