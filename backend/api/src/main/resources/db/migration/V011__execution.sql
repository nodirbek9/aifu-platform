-- ============================================================================
-- V011 — Execution job queue and results (ADR-006, ADR-007).
-- The queue is PostgreSQL. No broker. The API enqueues inside the submission
-- transaction; the separate worker claims with FOR UPDATE SKIP LOCKED.
-- ============================================================================

CREATE TABLE execution_jobs (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id    uuid NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    status           text NOT NULL DEFAULT 'PENDING' CHECK (status IN
                     ('PENDING','CLAIMED','RUNNING','SUCCEEDED','FAILED','DEAD','CANCELLED')),
    priority         int NOT NULL DEFAULT 0,
    attempt_count    int NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    max_attempts     int NOT NULL DEFAULT 3 CHECK (max_attempts >= 1),
    available_at     timestamptz NOT NULL DEFAULT now(),
    claimed_by       text,
    claimed_at       timestamptz,
    lease_expires_at timestamptz,
    started_at       timestamptz,
    finished_at      timestamptz,
    last_error       text,
    external_ref     text,      -- e.g. a Judge0 batch token
    created_at       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_execution_job_submission UNIQUE (submission_id)
);
-- The claim index. Keep it aligned with ExecutionJobQueue's claim statement.
CREATE INDEX idx_execution_jobs_claimable ON execution_jobs(priority DESC, available_at)
    WHERE status = 'PENDING';
CREATE INDEX idx_execution_jobs_lease ON execution_jobs(lease_expires_at)
    WHERE status IN ('CLAIMED','RUNNING');
COMMENT ON TABLE execution_jobs IS
 'ADR-007. Claim:
    UPDATE execution_jobs SET status=''CLAIMED'', ... WHERE id IN (
      SELECT id FROM execution_jobs WHERE status=''PENDING'' AND available_at <= now()
      ORDER BY priority DESC, available_at FOR UPDATE SKIP LOCKED LIMIT :n) RETURNING *;
  A job whose lease_expires_at has passed is reclaimed by the sweeper, never duplicated
  (uq_execution_job_submission guarantees one job per submission).';

CREATE TABLE execution_results (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   uuid NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    job_id          uuid REFERENCES execution_jobs(id) ON DELETE SET NULL,
    engine          text NOT NULL,      -- judge0 | docker-run | noop
    engine_version  text,
    sandbox_profile text NOT NULL,      -- records the limit set actually applied
    compile_success boolean,
    compile_output  text,               -- truncated to aifu.execution.max-output-bytes
    exit_code       int,
    total_time_ms   int CHECK (total_time_ms IS NULL OR total_time_ms >= 0),
    max_memory_kb   int CHECK (max_memory_kb IS NULL OR max_memory_kb >= 0),
    output_truncated boolean NOT NULL DEFAULT false,
    internal_error  text,
    created_at      timestamptz NOT NULL DEFAULT now(),
    -- Idempotent handler (ADR-007 at-least-once): a re-run replaces this row.
    CONSTRAINT uq_execution_result_submission UNIQUE (submission_id)
);
CREATE INDEX idx_execution_results_job ON execution_results(job_id);

CREATE TABLE execution_test_results (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_result_id uuid NOT NULL REFERENCES execution_results(id) ON DELETE CASCADE,
    test_case_id        uuid NOT NULL REFERENCES coding_task_test_cases(id) ON DELETE CASCADE,
    passed              boolean NOT NULL,
    verdict             text NOT NULL CHECK (verdict IN
                        ('ACCEPTED','WRONG_ANSWER','RUNTIME_ERROR','TIME_LIMIT_EXCEEDED',
                         'MEMORY_LIMIT_EXCEEDED','OUTPUT_LIMIT_EXCEEDED','SKIPPED','INTERNAL_ERROR')),
    time_ms             int CHECK (time_ms IS NULL OR time_ms >= 0),
    memory_kb           int CHECK (memory_kb IS NULL OR memory_kb >= 0),
    exit_code           int,
    -- Captured for teachers/debugging. The student-facing mapper omits these entirely for
    -- test cases whose visibility is HIDDEN.
    stdout_excerpt      text,
    stderr_excerpt      text,
    points_awarded      numeric(6,2) NOT NULL DEFAULT 0 CHECK (points_awarded >= 0),
    CONSTRAINT uq_execution_test_result UNIQUE (execution_result_id, test_case_id)
);
CREATE INDEX idx_execution_test_results_case ON execution_test_results(test_case_id, passed);
