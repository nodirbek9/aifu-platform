-- ============================================================================
-- V033 — Quiz settings of the learning path (phase 5, ADR-016).
--
-- A lesson test (the quiz behind an activity with path_step = 'TEST') is passed at 60 %, allows
-- two attempts and draws 5 questions per attempt from the quiz's bank; the answers are shown
-- after the last attempt. A quiz behind a PRACTICE activity (a checked-answer task) has no
-- attempt limit. The midterm (path_step = 'MIDTERM') is configured by the administrator.
--
-- Nothing in quiz_attempts is touched: the attempt trigger of V032 only checks INSERTs, so a
-- student who already made three attempts keeps them, and gets no further attempt unless an
-- administrator grants one. Idempotent. On a fresh database no activity has a path_step yet when
-- this runs (V032 backfills only existing rows), so it is a no-op there; the dev seed V914 does
-- the same for the dev content.
-- ============================================================================

UPDATE quizzes q
SET max_attempts = 2,
    passing_percent = 60,
    reveal_policy = 'AFTER_LAST_ATTEMPT'
WHERE q.id IN (SELECT a.reference_id
               FROM activities a
               WHERE a.activity_type = 'QUIZ'
                 AND a.path_step = 'TEST'
                 AND a.reference_id IS NOT NULL)
  AND (q.max_attempts IS DISTINCT FROM 2
       OR q.passing_percent IS DISTINCT FROM 60
       OR q.reveal_policy IS DISTINCT FROM 'AFTER_LAST_ATTEMPT');

-- 5 questions per attempt, only where the bank has at least 5
UPDATE quizzes q
SET question_pool_size = 5
WHERE q.id IN (SELECT a.reference_id
               FROM activities a
               WHERE a.activity_type = 'QUIZ'
                 AND a.path_step = 'TEST'
                 AND a.reference_id IS NOT NULL)
  AND (SELECT count(*) FROM questions qu WHERE qu.quiz_id = q.id) >= 5
  AND q.question_pool_size IS DISTINCT FROM 5;

-- a checked-answer practice task has no attempt limit
UPDATE quizzes q
SET max_attempts = NULL
WHERE q.id IN (SELECT a.reference_id
               FROM activities a
               WHERE a.activity_type = 'QUIZ'
                 AND a.path_step = 'PRACTICE'
                 AND a.reference_id IS NOT NULL)
  AND q.max_attempts IS NOT NULL;
