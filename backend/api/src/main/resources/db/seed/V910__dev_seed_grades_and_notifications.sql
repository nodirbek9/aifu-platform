-- ============================================================================
-- V910 (DEV ONLY) — Grades and notifications for the demo student.
--
-- The grades page (S-08) and the notification list (S-09) had nothing to show: the Dasturlash
-- course of the demo semester had one component (the final, 100 points) and no grade, and no
-- demo account had a notification.
--
--   * The course gets the university's scheme, as IAKTT1105 states it: JN 30 (attendance 10,
--     practical activity 10, independent work 10) + ON 30 + YaN 40; JN and ON are gates to the
--     final at 60 %.
--   * student3 gets grades in JN and ON — one of them corrected by the teacher, so the history of
--     changes has something in it — and none in YaN yet: the semester is still running.
--   * student3 gets the notifications these events would have produced, written in the payload
--     shape the application itself writes.
-- ============================================================================

DO $seed$
DECLARE
    v_course     uuid;
    v_enrollment uuid;
    v_student    uuid;
    v_teacher    uuid;
    v_jn         uuid;
    v_att        uuid;
    v_prac       uuid;
    v_ind        uuid;
    v_on         uuid;
    v_first      uuid;
    v_group      uuid;
    r            record;
BEGIN
    SELECT c.id INTO v_course
    FROM courses c JOIN subjects s ON s.id = c.subject_id
    WHERE s.code = 'DAS1110' AND c.cohort_code = 'DI-26-01-S1';
    SELECT id INTO v_student FROM users WHERE username = 'student3';
    SELECT id INTO v_teacher FROM users WHERE username = 'n.ayitbayev';
    IF v_course IS NULL OR v_student IS NULL THEN
        RETURN;
    END IF;
    SELECT id INTO v_enrollment FROM enrollments
    WHERE course_id = v_course AND student_user_id = v_student AND status = 'ACTIVE';

    -- ------------------------------------------------------------ the assessment scheme
    IF NOT EXISTS (SELECT 1 FROM assessment_components WHERE course_id = v_course AND code = 'JN') THEN
        UPDATE assessment_components
        SET max_points = 40, display_order = 3, assessment_form = 'WRITTEN'
        WHERE course_id = v_course AND code = 'YaN';

        INSERT INTO assessment_components (course_id, code, kind, max_points, min_pass_percent,
                                           blocks_final, assessment_form, display_order)
        VALUES (v_course, 'JN', 'CONTINUOUS', 30, 60, true, 'MIXED', 1)
        RETURNING id INTO v_jn;
        INSERT INTO assessment_components (course_id, parent_component_id, code, kind, max_points, display_order)
        VALUES (v_course, v_jn, 'JN_ATTENDANCE', 'ATTENDANCE', 10, 1) RETURNING id INTO v_att;
        INSERT INTO assessment_components (course_id, parent_component_id, code, kind, max_points, display_order)
        VALUES (v_course, v_jn, 'JN_PRACTICAL', 'PRACTICAL_ACTIVITY', 10, 2) RETURNING id INTO v_prac;
        INSERT INTO assessment_components (course_id, parent_component_id, code, kind, max_points, display_order)
        VALUES (v_course, v_jn, 'JN_INDEPENDENT', 'INDEPENDENT_WORK', 10, 3) RETURNING id INTO v_ind;
        INSERT INTO assessment_components (course_id, code, kind, max_points, min_pass_percent,
                                           blocks_final, assessment_form, display_order)
        VALUES (v_course, 'ON', 'MIDTERM', 30, 60, true, 'WRITTEN', 2)
        RETURNING id INTO v_on;
    ELSE
        RETURN;
    END IF;

    IF v_enrollment IS NULL THEN
        RETURN;
    END IF;

    -- ------------------------------------------------------------ grades
    INSERT INTO grade_entries (enrollment_id, assessment_component_id, source, source_ref_type, score,
                               max_score, status, revision, evaluator_user_id, evaluated_at, published_at)
    VALUES (v_enrollment, v_att, 'ATTENDANCE', 'MANUAL', 9, 10, 'PUBLISHED', 1, v_teacher,
            now() - interval '6 days', now() - interval '6 days');

    -- the practical mark was first 6, then corrected to 8: the first row stays, as REVISED
    INSERT INTO grade_entries (enrollment_id, assessment_component_id, source, source_ref_type, score,
                               max_score, status, revision, evaluator_user_id, evaluated_at, published_at)
    VALUES (v_enrollment, v_prac, 'MANUAL', 'MANUAL', 6, 10, 'REVISED', 1, v_teacher,
            now() - interval '9 days', now() - interval '9 days')
    RETURNING id INTO v_first;
    INSERT INTO grade_entries (enrollment_id, assessment_component_id, source, source_ref_type, score,
                               max_score, status, revision, supersedes_id, evaluator_user_id,
                               evaluated_at, published_at)
    VALUES (v_enrollment, v_prac, 'MANUAL', 'MANUAL', 8, 10, 'PUBLISHED', 2, v_first, v_teacher,
            now() - interval '5 days', now() - interval '5 days');

    INSERT INTO grade_entries (enrollment_id, assessment_component_id, source, source_ref_type, score,
                               max_score, status, revision, evaluator_user_id, evaluated_at, published_at)
    VALUES (v_enrollment, v_ind, 'MANUAL', 'MANUAL', 7, 10, 'PUBLISHED', 1, v_teacher,
            now() - interval '4 days', now() - interval '4 days');

    INSERT INTO grade_entries (enrollment_id, assessment_component_id, source, source_ref_type, score,
                               max_score, status, revision, evaluator_user_id, evaluated_at, published_at)
    VALUES (v_enrollment, v_on, 'MANUAL', 'MANUAL', 24, 30, 'PUBLISHED', 1, v_teacher,
            now() - interval '2 days', now() - interval '2 days');

    -- what the application derives from those entries: 9 + 8 + 7 + 24 = 48 of 100 so far; JN 80 %
    -- and ON 80 % pass their gates; the final has not been taken, so its gate is not passed yet
    UPDATE enrollments
    SET final_score = 48, final_grade_symbol = '2', admitted_to_final = true, academic_debt = true,
        final_grade_at = now() - interval '2 days'
    WHERE id = v_enrollment;

    -- ------------------------------------------------------------ notifications
    INSERT INTO notifications (recipient_user_id, type, status, payload, dedupe_key, created_at, sent_at, read_at)
    VALUES (v_student, 'ENROLLMENT_CREATED', 'READ',
            jsonb_build_object('courseId', v_course, 'enrollmentId', v_enrollment),
            'enrollment-created:' || v_enrollment,
            now() - interval '10 days', now() - interval '10 days', now() - interval '9 days');

    FOR r IN
        SELECT a.id, a.title, a.course_id, a.group_id, a.created_at
        FROM announcements a
        WHERE a.course_id = v_course
           OR a.group_id IN (SELECT g.id FROM student_groups g
                             JOIN student_profiles p ON p.group_code = g.code
                             WHERE p.user_id = v_student)
    LOOP
        INSERT INTO notifications (recipient_user_id, type, status, payload, dedupe_key, created_at, sent_at, read_at)
        VALUES (v_student, 'ANNOUNCEMENT',
                CASE WHEN r.course_id IS NULL THEN 'SENT' ELSE 'READ' END,
                CASE WHEN r.course_id IS NULL
                     THEN jsonb_build_object('announcementId', r.id, 'title', r.title, 'groupId', r.group_id)
                     ELSE jsonb_build_object('announcementId', r.id, 'title', r.title, 'courseId', r.course_id)
                END,
                'announcement:' || r.id, r.created_at, r.created_at,
                CASE WHEN r.course_id IS NULL THEN NULL ELSE r.created_at + interval '1 hour' END);
    END LOOP;

    FOR r IN
        SELECT x.id, x.kind, x.on_date, x.new_date, x.reason, x.created_at, e.id AS entry_id,
               e.course_id, s.pair_no, ns.pair_no AS new_pair_no,
               nr.building || '-' || nr.number AS new_room
        FROM timetable_exceptions x
        JOIN timetable_entries e ON e.id = x.entry_id
        JOIN pair_slots s ON s.id = e.pair_slot_id
        LEFT JOIN pair_slots ns ON ns.id = x.new_pair_slot_id
        LEFT JOIN rooms nr ON nr.id = x.new_room_id
        WHERE e.course_id = v_course
    LOOP
        INSERT INTO notifications (recipient_user_id, type, status, payload, dedupe_key, created_at, sent_at)
        VALUES (v_student, 'TIMETABLE_CHANGED', 'SENT',
                jsonb_strip_nulls(jsonb_build_object(
                    'entryId', r.entry_id, 'courseId', r.course_id, 'kind', r.kind,
                    'date', r.on_date, 'pairNo', r.pair_no, 'newDate', r.new_date,
                    'newPairNo', r.new_pair_no, 'newRoom', r.new_room, 'reason', r.reason)),
                'timetable-change:' || r.id, now() - interval '1 day', now() - interval '1 day');
    END LOOP;

    INSERT INTO notifications (recipient_user_id, type, status, payload, created_at, sent_at)
    VALUES (v_student, 'GRADE_PUBLISHED', 'SENT',
            jsonb_build_object('enrollmentId', v_enrollment, 'courseId', v_course,
                               'finalScore', '48.00', 'finalGradeSymbol', '2'),
            now() - interval '2 days', now() - interval '2 days');
END;
$seed$;
