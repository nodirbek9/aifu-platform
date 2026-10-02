-- ============================================================================
-- V014 — Restricted database role for the execution worker (ADR-006).
-- SECURITY BOUNDARY. A fully compromised worker must not be able to read a
-- password hash, a refresh token, the audit log, or the gradebook.
-- Do NOT widen these grants for convenience.
-- ============================================================================

DO $$
DECLARE
    worker_role text := coalesce(current_setting('aifu.worker_role', true), 'aifu_execution_worker');
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = worker_role) THEN
        -- No password here on purpose: it is set out-of-band (docker/postgres/init or the
        -- deployment pipeline) from DB_WORKER_PASSWORD. Never commit a password.
        EXECUTE format('CREATE ROLE %I LOGIN NOINHERIT', worker_role);
        RAISE NOTICE 'Created role % without a password. Set it from DB_WORKER_PASSWORD.', worker_role;
    END IF;

    EXECUTE format('GRANT CONNECT ON DATABASE %I TO %I', current_database(), worker_role);
    EXECUTE format('GRANT USAGE ON SCHEMA public TO %I', worker_role);

    -- Read only what is needed to run one submission.
    EXECUTE format('GRANT SELECT ON submissions, coding_tasks, coding_task_test_cases,
                    coding_task_languages, programming_languages TO %I', worker_role);

    -- Write only execution outcomes.
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON execution_jobs TO %I', worker_role);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON execution_results TO %I', worker_role);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON execution_test_results TO %I', worker_role);

    -- Narrow update of the submission outcome. Column-level so the worker cannot rewrite
    -- source_code, enrollment_id or attempt_number.
    EXECUTE format('GRANT UPDATE (status, verdict, score, max_score, tests_passed,
                    tests_total, completed_at, version) ON submissions TO %I', worker_role);

    -- Explicitly denied by omission: users, refresh_tokens, user_roles, audit_log,
    -- grade_entries, enrollments, quiz_*, files, notifications.
    EXECUTE format('REVOKE ALL ON ALL TABLES IN SCHEMA public FROM %I', worker_role);

    -- Re-grant after the blanket revoke (order matters).
    EXECUTE format('GRANT SELECT ON submissions, coding_tasks, coding_task_test_cases,
                    coding_task_languages, programming_languages TO %I', worker_role);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON execution_jobs, execution_results,
                    execution_test_results TO %I', worker_role);
    EXECUTE format('GRANT UPDATE (status, verdict, score, max_score, tests_passed,
                    tests_total, completed_at, version) ON submissions TO %I', worker_role);
    EXECUTE format('GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO %I', worker_role);
END $$;

COMMENT ON TABLE execution_jobs IS
 'Queue (ADR-007). The execution worker connects as the restricted role created in V014 and
  has no access to users, tokens, audit or grades. See skills/coding-execution-security.';
