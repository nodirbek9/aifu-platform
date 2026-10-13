-- ============================================================================
-- V037 — Admin problems: an exhausted midterm can always get another attempt.
--
-- ADR-016 amendment (2026-10-12): an admin may grant another attempt each time the
-- whole allowance is used up, not only once. A MIDTERM_ATTEMPTS_EXHAUSTED row already
-- means "allowance used, not passed, no usable attempt", so grant_available is true.
-- The rest of the view is unchanged from V035.
-- ============================================================================

CREATE OR REPLACE VIEW v_admin_problems AS

-- 1. Students without group
SELECT
    ('STUDENT_WITHOUT_GROUP:' || u.id::text) AS problem_key,
    'STUDENT_WITHOUT_GROUP'                  AS type,
    u.id                                     AS user_id,
    u.full_name                              AS full_name,
    sg.id                                    AS group_id,
    sp.group_code                            AS group_code,
    NULL::uuid                               AS course_id,
    NULL::text                               AS subject_code,
    NULL::text                               AS cohort_code,
    NULL::text                               AS term_name,
    NULL::uuid                               AS enrollment_id,
    NULL::uuid                               AS quiz_id,
    NULL::int                                AS attempts_used,
    NULL::int                                AS attempts_allowed,
    NULL::boolean                            AS grant_available,
    NULL::timestamptz                        AS locked_until
FROM users u
LEFT JOIN student_profiles sp ON sp.user_id = u.id
LEFT JOIN student_groups sg ON sg.code = sp.group_code
WHERE u.status = 'ACTIVE'
  AND EXISTS (
      SELECT 1 FROM user_roles ur
      JOIN roles r ON r.id = ur.role_id
      WHERE ur.user_id = u.id AND r.code = 'STUDENT'
  )
  AND (
      sp.user_id IS NULL
      OR sp.group_code IS NULL
      OR btrim(sp.group_code) = ''
      OR sg.id IS NULL
      OR sg.status <> 'ACTIVE'
  )

UNION ALL

-- 2. Groups without published entries
SELECT
    ('GROUP_WITHOUT_PUBLISHED_ENTRIES:' || g.id::text) AS problem_key,
    'GROUP_WITHOUT_PUBLISHED_ENTRIES'                 AS type,
    NULL::uuid                                        AS user_id,
    NULL::text                                        AS full_name,
    g.id                                              AS group_id,
    g.code                                            AS group_code,
    NULL::uuid                                        AS course_id,
    NULL::text                                        AS subject_code,
    NULL::text                                        AS cohort_code,
    NULL::text                                        AS term_name,
    NULL::uuid                                        AS enrollment_id,
    NULL::uuid                                        AS quiz_id,
    NULL::int                                         AS attempts_used,
    NULL::int                                         AS attempts_allowed,
    NULL::boolean                                     AS grant_available,
    NULL::timestamptz                                 AS locked_until
FROM student_groups g
WHERE g.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM timetable_entry_groups teg
      JOIN timetable_entries te ON te.id = teg.entry_id
      JOIN timetables t ON t.id = te.timetable_id
      WHERE teg.group_id = g.id
        AND t.status = 'PUBLISHED'
  )

UNION ALL

-- 3. Courses without teacher
SELECT
    ('COURSE_WITHOUT_TEACHER:' || c.id::text) AS problem_key,
    'COURSE_WITHOUT_TEACHER'                  AS type,
    NULL::uuid                                AS user_id,
    NULL::text                                AS full_name,
    NULL::uuid                                AS group_id,
    NULL::text                                AS group_code,
    c.id                                      AS course_id,
    s.code                                    AS subject_code,
    c.cohort_code                             AS cohort_code,
    t.name                                    AS term_name,
    NULL::uuid                                AS enrollment_id,
    NULL::uuid                                AS quiz_id,
    NULL::int                                 AS attempts_used,
    NULL::int                                 AS attempts_allowed,
    NULL::boolean                             AS grant_available,
    NULL::timestamptz                         AS locked_until
FROM courses c
JOIN subjects s ON s.id = c.subject_id
JOIN terms t ON t.id = c.term_id
WHERE c.deleted_at IS NULL
  AND c.status IN ('DRAFT', 'PUBLISHED')
  AND NOT EXISTS (
      SELECT 1
      FROM course_teachers ct
      WHERE ct.course_id = c.id
  )

UNION ALL

-- 4. Active enrollment with inactive user
SELECT
    ('ACTIVE_ENROLLMENT_INACTIVE_USER:' || e.id::text) AS problem_key,
    'ACTIVE_ENROLLMENT_INACTIVE_USER'                 AS type,
    u.id                                              AS user_id,
    u.full_name                                       AS full_name,
    sg.id                                             AS group_id,
    sp.group_code                                     AS group_code,
    c.id                                              AS course_id,
    s.code                                            AS subject_code,
    c.cohort_code                                     AS cohort_code,
    t.name                                            AS term_name,
    e.id                                              AS enrollment_id,
    NULL::uuid                                        AS quiz_id,
    NULL::int                                         AS attempts_used,
    NULL::int                                         AS attempts_allowed,
    NULL::boolean                                     AS grant_available,
    u.locked_until                                    AS locked_until
FROM enrollments e
JOIN users u ON u.id = e.student_user_id
JOIN courses c ON c.id = e.course_id
JOIN subjects s ON s.id = c.subject_id
JOIN terms t ON t.id = c.term_id
LEFT JOIN student_profiles sp ON sp.user_id = u.id
LEFT JOIN student_groups sg ON sg.code = sp.group_code
WHERE e.status = 'ACTIVE'
  AND u.status <> 'ACTIVE'

UNION ALL

-- 5. Locked accounts
SELECT
    ('LOCKED_ACCOUNT:' || u.id::text) AS problem_key,
    'LOCKED_ACCOUNT'                  AS type,
    u.id                              AS user_id,
    u.full_name                       AS full_name,
    sg.id                             AS group_id,
    sp.group_code                     AS group_code,
    NULL::uuid                        AS course_id,
    NULL::text                        AS subject_code,
    NULL::text                        AS cohort_code,
    NULL::text                        AS term_name,
    NULL::uuid                        AS enrollment_id,
    NULL::uuid                        AS quiz_id,
    NULL::int                         AS attempts_used,
    NULL::int                         AS attempts_allowed,
    NULL::boolean                     AS grant_available,
    u.locked_until                    AS locked_until
FROM users u
LEFT JOIN student_profiles sp ON sp.user_id = u.id
LEFT JOIN student_groups sg ON sg.code = sp.group_code
WHERE u.locked_until IS NOT NULL
  AND u.locked_until > now()

UNION ALL

-- 6. Midterm attempts exhausted
SELECT
    ('MIDTERM_ATTEMPTS_EXHAUSTED:' || e.id::text) AS problem_key,
    'MIDTERM_ATTEMPTS_EXHAUSTED'                  AS type,
    u.id                                          AS user_id,
    u.full_name                                   AS full_name,
    sg.id                                         AS group_id,
    sp.group_code                                 AS group_code,
    c.id                                          AS course_id,
    s.code                                        AS subject_code,
    c.cohort_code                                 AS cohort_code,
    t.name                                        AS term_name,
    e.id                                          AS enrollment_id,
    q.id                                          AS quiz_id,
    stats.attempts_used                           AS attempts_used,
    (q.max_attempts + stats.grants_count)::int    AS attempts_allowed,
    true                                          AS grant_available,
    u.locked_until                                AS locked_until
FROM enrollments e
JOIN course_learning_paths clp ON clp.course_id = e.course_id AND clp.enabled = true
JOIN activities a ON a.id = clp.midterm_activity_id
    AND a.course_id = e.course_id
    AND a.activity_type = 'QUIZ'
    AND a.path_step = 'MIDTERM'
    AND a.status = 'PUBLISHED'
JOIN quizzes q ON q.id = a.reference_id
    AND q.course_id = e.course_id
    AND q.status = 'PUBLISHED'
    AND q.max_attempts IS NOT NULL
JOIN users u ON u.id = e.student_user_id
JOIN courses c ON c.id = e.course_id
JOIN subjects s ON s.id = c.subject_id
JOIN terms t ON t.id = c.term_id
LEFT JOIN student_profiles sp ON sp.user_id = u.id
LEFT JOIN student_groups sg ON sg.code = sp.group_code
CROSS JOIN LATERAL (
    SELECT
        coalesce(max(qa.attempt_number), 0)::int AS attempts_used,
        (SELECT count(*) FROM quiz_attempt_grants qag WHERE qag.quiz_id = q.id AND qag.enrollment_id = e.id)::int AS grants_count
    FROM quiz_attempts qa
    WHERE qa.quiz_id = q.id AND qa.enrollment_id = e.id
) stats
WHERE e.status = 'ACTIVE'
  AND c.deleted_at IS NULL
  AND stats.attempts_used >= (q.max_attempts + stats.grants_count)
  AND NOT EXISTS (
      SELECT 1
      FROM activity_progress ap
      WHERE ap.enrollment_id = e.id
        AND ap.activity_id = a.id
        AND ap.status = 'COMPLETED'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM quiz_attempts qa
      WHERE qa.quiz_id = q.id
        AND qa.enrollment_id = e.id
        AND (
            (qa.status = 'GRADED' AND qa.passed = true)
            OR qa.status = 'SUBMITTED'
            OR (qa.status = 'IN_PROGRESS' AND (qa.expires_at IS NULL OR qa.expires_at > now()))
        )
  );

COMMENT ON VIEW v_admin_problems IS
 'Answers admin problems query (Phase 7.4). Read-only UNION ALL view identifying operational issues.';
