-- ============================================================================
-- V022 — "Run" without grading, and hints revealed one at a time
--        (product spec S-04.4, S-05.1, S-05.4, S-05.5, T-05).
--
-- A code run is NOT a submission: it has no attempt number, is never graded, never reaches the
-- gradebook or progress, and is deleted after a retention period. It goes through the same
-- execution worker and sandbox as a submission (the API still never runs code, ADR-006).
--
--   CUSTOM    the student's code with the stdin they typed
--   EXAMPLES  the student's code against the task's EXAMPLE test cases only (the ones a student
--             can already read) — hidden tests are never touched
--   VERIFY    a teacher's reference solution against ALL test cases of the task
-- ============================================================================

CREATE TABLE code_runs (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    coding_task_id   uuid REFERENCES coding_tasks(id) ON DELETE CASCADE,   -- NULL = free run
    language_code    text NOT NULL REFERENCES programming_languages(code) ON DELETE RESTRICT,
    mode             text NOT NULL CHECK (mode IN ('CUSTOM','EXAMPLES','VERIFY')),
    source_code      text NOT NULL,
    stdin            text NOT NULL DEFAULT '',
    status           text NOT NULL DEFAULT 'QUEUED'
                     CHECK (status IN ('QUEUED','RUNNING','COMPLETED','FAILED')),
    -- queue (same claim pattern as execution_jobs: FOR UPDATE SKIP LOCKED + a lease)
    attempt_count    int NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    available_at     timestamptz NOT NULL DEFAULT now(),
    claimed_by       text,
    lease_expires_at timestamptz,
    -- outcome
    verdict          text CHECK (verdict IN
                     ('OK','ACCEPTED','WRONG_ANSWER','COMPILATION_ERROR','RUNTIME_ERROR',
                      'TIME_LIMIT_EXCEEDED','MEMORY_LIMIT_EXCEEDED','OUTPUT_LIMIT_EXCEEDED',
                      'INTERNAL_ERROR')),
    compile_output   text,
    stdout           text,          -- CUSTOM only
    stderr           text,          -- CUSTOM only
    exit_code        int,
    time_ms          int CHECK (time_ms IS NULL OR time_ms >= 0),
    memory_kb        int CHECK (memory_kb IS NULL OR memory_kb >= 0),
    tests_passed     int,
    tests_total      int,
    internal_error   text,
    created_at       timestamptz NOT NULL DEFAULT now(),
    started_at       timestamptz,
    completed_at     timestamptz,
    CONSTRAINT ck_code_run_task CHECK (mode = 'CUSTOM' OR coding_task_id IS NOT NULL)
);
CREATE INDEX idx_code_runs_claimable ON code_runs(available_at) WHERE status = 'QUEUED';
CREATE INDEX idx_code_runs_lease     ON code_runs(lease_expires_at) WHERE status = 'RUNNING';
CREATE INDEX idx_code_runs_user      ON code_runs(user_id, created_at DESC);
CREATE INDEX idx_code_runs_task      ON code_runs(coding_task_id, mode, created_at DESC);
CREATE INDEX idx_code_runs_language  ON code_runs(language_code);
CREATE INDEX idx_code_runs_created   ON code_runs(created_at);
COMMENT ON TABLE code_runs IS
 'Ungraded runs ("Run", "Run examples", "Verify solutions"). Short-lived: the API deletes old
  rows. Never joined to grades, progress or attempts.';

CREATE TABLE code_run_test_results (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id         uuid NOT NULL REFERENCES code_runs(id) ON DELETE CASCADE,
    test_case_id   uuid NOT NULL REFERENCES coding_task_test_cases(id) ON DELETE CASCADE,
    position       int NOT NULL,
    passed         boolean NOT NULL,
    verdict        text NOT NULL,
    time_ms        int,
    memory_kb      int,
    exit_code      int,
    stdout_excerpt text,
    stderr_excerpt text,
    CONSTRAINT uq_code_run_test UNIQUE (run_id, test_case_id)
);
CREATE INDEX idx_code_run_tests_case ON code_run_test_results(test_case_id);

-- ---------------------------------------------------------------- hints, one at a time
CREATE TABLE coding_task_hint_reveals (
    hint_id     uuid NOT NULL REFERENCES coding_task_hints(id) ON DELETE CASCADE,
    user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    revealed_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (hint_id, user_id)
);
CREATE INDEX idx_hint_reveals_user ON coding_task_hint_reveals(user_id);
COMMENT ON TABLE coding_task_hint_reveals IS
 'Which hints a student has opened. A hint''s text reaches a student only after a row exists
  here; teachers can see how many hints a student needed.';

-- ---------------------------------------------------------------- permission
INSERT INTO permissions (code, description, domain) VALUES
 ('CODE_RUN','Run code in the sandbox without grading (custom input, examples)','coding');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'CODE_RUN'
WHERE r.code IN ('ADMIN','TEACHER','STUDENT');

-- ---------------------------------------------------------------- execution worker role (V014)
-- The worker claims a run, reads its code and input, and writes the outcome. It may not create
-- or delete runs, and cannot change whose run it is or what was asked.
DO $$
DECLARE
    worker_role text := coalesce(current_setting('aifu.worker_role', true), 'aifu_execution_worker');
BEGIN
    EXECUTE format('GRANT SELECT ON code_runs TO %I', worker_role);
    EXECUTE format('GRANT UPDATE (status, attempt_count, available_at, claimed_by, lease_expires_at,
                    verdict, compile_output, stdout, stderr, exit_code, time_ms, memory_kb,
                    tests_passed, tests_total, internal_error, started_at, completed_at)
                    ON code_runs TO %I', worker_role);
    EXECUTE format('GRANT SELECT, INSERT, DELETE ON code_run_test_results TO %I', worker_role);
END $$;
