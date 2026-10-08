-- ============================================================================
-- V914 (DEV ONLY) — path steps and test settings for the dev content (phase 5).
--
-- On a fresh database V032 runs before the dev seeds, so the dev activities have no path_step.
-- This sets them with the same rule as the V032 backfill (the first QUIZ per lesson is the TEST,
-- the first CODING_TASK the PRACTICE) and applies the quiz settings of V033. It does NOT enable
-- any course's learning path: that is an administrator's action. Idempotent.
-- ============================================================================

WITH first_quiz AS (
    SELECT DISTINCT ON (lesson_id) id
    FROM activities
    WHERE lesson_id IS NOT NULL AND activity_type = 'QUIZ' AND status <> 'ARCHIVED'
    ORDER BY lesson_id, position ASC
)
UPDATE activities
SET path_step = 'TEST'
WHERE id IN (SELECT id FROM first_quiz)
  AND path_step IS NULL
  AND NOT EXISTS (SELECT 1 FROM activities t
                  WHERE t.lesson_id = activities.lesson_id AND t.path_step = 'TEST'
                    AND t.status <> 'ARCHIVED');

WITH first_task AS (
    SELECT DISTINCT ON (lesson_id) id
    FROM activities
    WHERE lesson_id IS NOT NULL AND activity_type = 'CODING_TASK' AND status <> 'ARCHIVED'
    ORDER BY lesson_id, position ASC
)
UPDATE activities
SET path_step = 'PRACTICE'
WHERE id IN (SELECT id FROM first_task)
  AND path_step IS NULL
  AND NOT EXISTS (SELECT 1 FROM activities t
                  WHERE t.lesson_id = activities.lesson_id AND t.path_step = 'PRACTICE'
                    AND t.status <> 'ARCHIVED');

UPDATE quizzes q
SET max_attempts = 2,
    passing_percent = 60,
    reveal_policy = 'AFTER_LAST_ATTEMPT'
WHERE q.id IN (SELECT a.reference_id
               FROM activities a
               WHERE a.activity_type = 'QUIZ'
                 AND a.path_step = 'TEST'
                 AND a.reference_id IS NOT NULL);

UPDATE quizzes q
SET question_pool_size = 5
WHERE q.id IN (SELECT a.reference_id
               FROM activities a
               WHERE a.activity_type = 'QUIZ'
                 AND a.path_step = 'TEST'
                 AND a.reference_id IS NOT NULL)
  AND (SELECT count(*) FROM questions qu WHERE qu.quiz_id = q.id) >= 5;
