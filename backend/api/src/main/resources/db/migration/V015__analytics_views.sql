-- ============================================================================
-- V015 — Analytics read models (brief §26). Views, not a data warehouse.
-- Every view is course-scoped so authorization can filter by course_teachers.
-- ============================================================================

-- "How many students completed the course?" / "Which students are behind?"
CREATE VIEW v_course_completion AS
SELECT c.id                                        AS course_id,
       count(*)                                    AS enrolled_total,
       count(*) FILTER (WHERE cp.status = 'COMPLETED')   AS completed_total,
       count(*) FILTER (WHERE cp.status = 'IN_PROGRESS') AS in_progress_total,
       count(*) FILTER (WHERE cp.status = 'NOT_STARTED') AS not_started_total,
       round(avg(cp.completion_percent), 2)        AS avg_completion_percent,
       round(avg(e.final_score), 2)                AS avg_final_score
FROM courses c
JOIN enrollments e     ON e.course_id = c.id AND e.status IN ('ACTIVE','COMPLETED','FAILED')
LEFT JOIN course_progress cp ON cp.enrollment_id = e.id
GROUP BY c.id;

-- "Which students completed lesson X?"
CREATE VIEW v_lesson_completion AS
SELECT l.id                          AS lesson_id,
       l.module_id,
       m.course_id,
       count(lp.id)                  AS tracked_students,
       count(*) FILTER (WHERE lp.status = 'COMPLETED') AS completed_students,
       round(avg(lp.time_spent_seconds)::numeric, 0)   AS avg_time_spent_seconds
FROM lessons l
JOIN course_modules m ON m.id = l.module_id
LEFT JOIN lesson_progress lp ON lp.lesson_id = l.id
WHERE l.deleted_at IS NULL
GROUP BY l.id, l.module_id, m.course_id;

-- "Which students failed a quiz?" / "What is the average score?"
CREATE VIEW v_quiz_performance AS
SELECT q.id                                     AS quiz_id,
       q.course_id,
       count(a.id)                               AS attempts_total,
       count(DISTINCT a.enrollment_id)           AS students_attempted,
       count(*) FILTER (WHERE a.passed)          AS attempts_passed,
       count(*) FILTER (WHERE a.passed = false)  AS attempts_failed,
       round(avg(a.percent), 2)                  AS avg_percent,
       min(a.percent)                            AS min_percent,
       max(a.percent)                            AS max_percent
FROM quizzes q
LEFT JOIN quiz_attempts a ON a.quiz_id = q.id AND a.status = 'GRADED'
GROUP BY q.id, q.course_id;

-- Item analysis: which questions discriminate badly / are too hard.
CREATE VIEW v_question_difficulty AS
SELECT qq.question_id,
       q.quiz_id,
       count(*)                                          AS responses,
       count(*) FILTER (WHERE qq.is_correct)             AS correct_responses,
       round(100.0 * count(*) FILTER (WHERE qq.is_correct) / nullif(count(*), 0), 2)
                                                          AS correct_percent
FROM quiz_attempt_questions qq
JOIN questions q ON q.id = qq.question_id
JOIN quiz_attempts a ON a.id = qq.attempt_id AND a.status = 'GRADED'
GROUP BY qq.question_id, q.quiz_id;

-- "Which coding tasks are hardest?"
CREATE VIEW v_coding_task_difficulty AS
SELECT t.id                                          AS coding_task_id,
       t.course_id,
       count(s.id)                                   AS submissions_total,
       count(DISTINCT s.enrollment_id)               AS students_attempted,
       count(*) FILTER (WHERE s.verdict = 'ACCEPTED') AS accepted_total,
       round(100.0 * count(*) FILTER (WHERE s.verdict = 'ACCEPTED')
             / nullif(count(s.id), 0), 2)            AS acceptance_percent,
       round(avg(s.score), 2)                        AS avg_score,
       round(avg(s.attempt_number), 2)               AS avg_attempts_used
FROM coding_tasks t
LEFT JOIN submissions s ON s.coding_task_id = t.id AND s.status = 'COMPLETED'
GROUP BY t.id, t.course_id;

-- "Which students are behind?" — the at-risk list a teacher actually wants.
CREATE VIEW v_students_at_risk AS
SELECT e.id                          AS enrollment_id,
       e.course_id,
       e.student_user_id,
       coalesce(cp.completion_percent, 0)            AS completion_percent,
       cp.last_accessed_at,
       e.final_score,
       e.academic_debt,
       (SELECT count(*) FROM quiz_attempts qa
         WHERE qa.enrollment_id = e.id AND qa.status = 'GRADED' AND qa.passed = false)
                                                     AS failed_quiz_attempts,
       (SELECT count(*) FROM submissions s
         WHERE s.enrollment_id = e.id AND s.verdict IS NOT NULL AND s.verdict <> 'ACCEPTED')
                                                     AS unsuccessful_submissions
FROM enrollments e
LEFT JOIN course_progress cp ON cp.enrollment_id = e.id
WHERE e.status = 'ACTIVE'
  AND (coalesce(cp.completion_percent, 0) < 50
       OR cp.last_accessed_at IS NULL
       OR cp.last_accessed_at < now() - interval '14 days');

COMMENT ON VIEW v_students_at_risk IS
 'Answers brief §26 "which students are behind". Authorization: filter by course_teachers for
  a teacher, unrestricted for ADMIN. Never expose across courses to a teacher.';
