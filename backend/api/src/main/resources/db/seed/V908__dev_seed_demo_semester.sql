-- ============================================================================
-- V908 (DEV ONLY) — A demo database that looks like a real semester.
--
-- V900–V907 give subjects, lessons, tasks and a handful of accounts, but nothing of what the v3
-- screens are about: no groups with students, no rooms, no timetable, no attendance, no work
-- bound to lessons. This script fills the CURRENT term (2026/2027, 1-semestr) so that every
-- screen of the product spec has something true to show, and the acceptance demo
-- (docs/01-PRODUCT-SPEC.md §9) can be walked through on a fresh database.
--
-- Everything here is invented, except: the subjects (from the syllabi) and the owner's own
-- teaching load (academic-materials/syllabi/Yuklama - Ayitbayev.xlsx), which names the groups
-- and the practical classes he teaches this semester.
--
-- Accounts (password of every dev account: Dev!Pass123):
--   n.ayitbayev   teacher + admin   (the owner: practical classes of all four subjects)
--   b.akbarov, a.zoxidov  teachers  (lectures)        admin  administrator
--   student3      student of BK-DI26/1                student1  student of B-IQTR26/1
--   xolmatov.u    student of BK-DI25/1
-- ============================================================================

-- ---------------------------------------------------------------- the owner's account
INSERT INTO users (email, username, password_hash, full_name, status, preferred_locale) VALUES
 ('n.ayitbayev@dev.aifu.local','n.ayitbayev',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u',
  'Ayitbayev Nodirbek Quvondiq o''g''li','ACTIVE','uz');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code IN ('TEACHER','ADMIN')
WHERE u.username = 'n.ayitbayev';

INSERT INTO teacher_profiles (user_id, employee_number, department_id, academic_title)
SELECT u.id, 'DEV-T-003', d.id, 'stajyor o''qituvchi'
FROM users u, departments d WHERE u.username = 'n.ayitbayev' AND d.code = 'ANIQ_FANLAR';

-- ---------------------------------------------------------------- this semester's courses
-- Dasturlash (DAS1110, DI-26-01-S1) is already in the current term. The other three autumn
-- subjects were seeded into last year's term (all calendar dates are placeholders anyway);
-- bring them into the current one so the timetable has four subjects with real content.
UPDATE courses c SET term_id = t.id
FROM subjects s, terms t JOIN academic_years ay ON ay.id = t.academic_year_id
WHERE s.id = c.subject_id AND s.code IN ('IAKTT1105','KIA1406','KOT1306')
  AND ay.code = '2026/2027' AND t.ordinal = 1;

UPDATE course_translations ct SET title = v.title
FROM courses c JOIN subjects s ON s.id = c.subject_id
JOIN (VALUES ('IAKTT1105','Iqtisodiyotda axborot kommunikatsion texnologiyalar va tizimlar'),
             ('KIA1406','Kiberxavfsizlik asoslari'),
             ('KOT1306','Kompyuter tarmoqlari')) AS v(code, title) ON v.code = s.code
WHERE ct.course_id = c.id AND ct.locale = 'uz';

-- Titles in Russian and English, so the language switch has something to switch.
INSERT INTO subject_translations (subject_id, locale, title, short_title)
SELECT s.id, v.locale, v.title, v.short_title FROM subjects s
JOIN (VALUES
 ('DAS1110','ru','Программирование','Программирование'),
 ('DAS1110','en','Programming','Programming'),
 ('MTA1204','ru','Структуры данных и алгоритмы','Структуры данных'),
 ('MTA1204','en','Data Structures and Algorithms','Data Structures'),
 ('KIA1406','ru','Основы кибербезопасности','Кибербезопасность'),
 ('KIA1406','en','Fundamentals of Cybersecurity','Cybersecurity'),
 ('KOT1306','ru','Компьютерные сети','Компьютерные сети'),
 ('KOT1306','en','Computer Networks','Computer Networks'),
 ('IAKTT1105','ru','Информационно-коммуникационные технологии и системы в экономике','ИКТ в экономике'),
 ('IAKTT1105','en','Information and Communication Technologies and Systems in Economics','ICT in Economics')
) AS v(code, locale, title, short_title) ON v.code = s.code
ON CONFLICT DO NOTHING;

INSERT INTO course_translations (course_id, locale, title)
SELECT c.id, st.locale,
       st.title || CASE WHEN c.cohort_code LIKE '%-S1' THEN CASE st.locale WHEN 'ru' THEN ' (1-й семестр)' ELSE ' (semester 1)' END
                        WHEN c.cohort_code LIKE '%-S2' THEN CASE st.locale WHEN 'ru' THEN ' (2-й семестр)' ELSE ' (semester 2)' END
                        ELSE '' END
FROM courses c JOIN subject_translations st ON st.subject_id = c.subject_id AND st.locale IN ('ru','en')
ON CONFLICT DO NOTHING;

-- The owner teaches the practical classes.
INSERT INTO course_teachers (course_id, teacher_user_id, teacher_role)
SELECT c.id, u.id, 'ASSISTANT'
FROM courses c JOIN subjects s ON s.id = c.subject_id, users u
WHERE u.username = 'n.ayitbayev'
  AND ((s.code = 'DAS1110' AND c.cohort_code = 'DI-26-01-S1') OR s.code IN ('IAKTT1105','KIA1406','KOT1306'))
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- groups and students
-- Real group codes instead of the two placeholder ones. Moving the students creates the new
-- groups (V019 trigger); the emptied placeholders are then removed.
UPDATE student_profiles SET group_code = 'BK-DI26/1'  WHERE group_code = 'DI-26-01';
UPDATE student_profiles SET group_code = 'B-IQTR26/1' WHERE group_code = 'IQT-25-01';
DELETE FROM student_groups WHERE code IN ('DI-26-01','IQT-25-01');

CREATE TEMP TABLE demo_students (
    username text, full_name text, group_code text, programme text, year int, number text
) ON COMMIT DROP;
INSERT INTO demo_students VALUES
 ('abdullayev.s','Abdullayev Sardor','BK-DI26/1','60610600',2026,'DI26-006'),
 ('karimova.m','Karimova Malika','BK-DI26/1','60610600',2026,'DI26-007'),
 ('tursunov.j','Tursunov Jasur','BK-DI26/1','60610600',2026,'DI26-008'),
 ('rahimova.d','Rahimova Dilnoza','BK-DI26/1','60610600',2026,'DI26-009'),
 ('yusupov.b','Yusupov Bekzod','BK-DI26/1','60610600',2026,'DI26-010'),
 ('nazarova.sh','Nazarova Shahzoda','BK-DI26/1','60610600',2026,'DI26-011'),
 ('ergashev.o','Ergashev Otabek','BK-DI26/1','60610600',2026,'DI26-012'),
 ('saidova.m','Saidova Madina','BK-DI26/1','60610600',2026,'DI26-013'),
 ('qodirov.a','Qodirov Azizbek','BK-DI26/1','60610600',2026,'DI26-014'),
 ('ismoilova.g','Ismoilova Gulnoza','BK-DI26/1','60610600',2026,'DI26-015'),
 ('xolmatov.u','Xolmatov Ulug''bek','BK-DI25/1','60610600',2025,'DI25-001'),
 ('aliyeva.n','Aliyeva Nigora','BK-DI25/1','60610600',2025,'DI25-002'),
 ('normatov.sh','Normatov Sherzod','BK-DI25/1','60610600',2025,'DI25-003'),
 ('usmonova.z','Usmonova Zarina','BK-DI25/1','60610600',2025,'DI25-004'),
 ('hamidov.f','Hamidov Farrux','BK-DI25/1','60610600',2025,'DI25-005'),
 ('jorayeva.s','Jo''rayeva Sevara','BK-DI25/1','60610600',2025,'DI25-006'),
 ('mirzayev.d','Mirzayev Doston','BK-DI25/1','60610600',2025,'DI25-007'),
 ('sobirova.k','Sobirova Kamola','BK-DI25/1','60610600',2025,'DI25-008'),
 ('toshpolatov.i','Toshpo''latov Islom','BK-DI25/1','60610600',2025,'DI25-009'),
 ('rustamova.l','Rustamova Laylo','BK-DI25/1','60610600',2025,'DI25-010'),
 ('olimov.j','Olimov Javohir','B-IQTR26/1','60410100',2026,'IQT26-003'),
 ('hasanova.f','Hasanova Feruza','B-IQTR26/1','60410100',2026,'IQT26-004'),
 ('sultonov.a','Sultonov Akmal','B-IQTR26/1','60410100',2026,'IQT26-005'),
 ('bozorova.m','Bozorova Mohira','B-IQTR26/1','60410100',2026,'IQT26-006'),
 ('raxmonov.s','Raxmonov Sanjar','B-IQTR26/1','60410100',2026,'IQT26-007'),
 ('yoldosheva.d','Yo''ldosheva Dildora','B-IQTR26/1','60410100',2026,'IQT26-008'),
 ('komilov.a','Komilov Abror','B-IQTR26/1','60410100',2026,'IQT26-009'),
 ('ahmedova.n','Ahmedova Nilufar','B-IQTR26/1','60410100',2026,'IQT26-010');

INSERT INTO users (email, username, password_hash, full_name, status, preferred_locale)
SELECT username || '@dev.aifu.local', username,
       '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u', full_name, 'ACTIVE', 'uz'
FROM demo_students;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN demo_students d ON d.username = u.username
JOIN roles r ON r.code = 'STUDENT';

INSERT INTO student_profiles (user_id, student_number, programme_id, group_code, enrollment_year, study_form)
SELECT u.id, d.number, p.id, d.group_code, d.year, 'FULL_TIME'
FROM users u JOIN demo_students d ON d.username = u.username
JOIN academic_programmes p ON p.code = d.programme;

UPDATE student_groups g SET programme_id = p.id, course_year = v.course_year,
       admission_year = v.admission_year, planned_size = v.planned_size,
       curator_user_id = (SELECT id FROM users WHERE username = v.curator)
FROM (VALUES ('BK-DI26/1','60610600',1,2026,25,'n.ayitbayev'),
             ('BK-DI25/1','60610600',2,2025,26,'a.zoxidov'),
             ('B-IQTR26/1','60410100',1,2026,30,'b.akbarov')) AS v(code, programme, course_year, admission_year, planned_size, curator)
JOIN academic_programmes p ON p.code = v.programme
WHERE g.code = v.code;

-- Each group studies its subjects.
INSERT INTO enrollments (course_id, student_user_id, status, source, enrolled_at)
SELECT c.id, p.user_id, 'ACTIVE', 'ADMIN', now() - interval '25 days'
FROM student_profiles p
JOIN (VALUES ('BK-DI26/1','DAS1110','DI-26-01-S1'),
             ('B-IQTR26/1','IAKTT1105','IQT-25-01'),
             ('BK-DI25/1','KIA1406','DI-24-01-S3'),
             ('BK-DI25/1','KOT1306','DI-24-01-S3')) AS v(grp, subject, cohort) ON v.grp = p.group_code
JOIN subjects s ON s.code = v.subject
JOIN courses c ON c.subject_id = s.id AND c.cohort_code = v.cohort
WHERE NOT EXISTS (SELECT 1 FROM enrollments e WHERE e.course_id = c.id AND e.student_user_id = p.user_id);

INSERT INTO enrollment_events (enrollment_id, event_type, to_status, occurred_at)
SELECT e.id, 'CREATED', e.status, e.enrolled_at FROM enrollments e
WHERE NOT EXISTS (SELECT 1 FROM enrollment_events x WHERE x.enrollment_id = e.id);

-- ---------------------------------------------------------------- rooms and holidays
INSERT INTO rooms (building, number, name, capacity, room_type) VALUES
 ('A','101','Katta ma''ruza zali',120,'LECTURE_HALL'),
 ('A','204',NULL,30,'CLASSROOM'),
 ('A','206',NULL,30,'CLASSROOM'),
 ('B','301','Kompyuter sinfi 1',25,'COMPUTER_LAB'),
 ('B','302','Kompyuter sinfi 2',26,'COMPUTER_LAB'),
 ('B','305','Tarmoq laboratoriyasi',15,'COMPUTER_LAB');

INSERT INTO holidays (on_date, name) VALUES
 ('2026-10-01','O''qituvchi va murabbiylar kuni'),
 ('2026-12-08','Konstitutsiya kuni'),
 ('2027-01-01','Yangi yil');

-- ---------------------------------------------------------------- the published timetable
INSERT INTO timetables (term_id, name, status, published_at, published_by)
SELECT t.id, '2026/2027, 1-semestr', 'PUBLISHED', now() - interval '30 days',
       (SELECT id FROM users WHERE username = 'admin')
FROM terms t JOIN academic_years ay ON ay.id = t.academic_year_id
WHERE ay.code = '2026/2027' AND t.ordinal = 1;

-- One row per recurring class. No teacher, room or group is double-booked; the computer-network
-- practical splits its group in two halves that meet on alternate (odd / even) weeks.
CREATE TEMP TABLE demo_entries (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    n int, subject text, cohort text, session_type text, teacher text,
    building text, room text, dow int, pair_no int, parity text, grp text, subgroup text
) ON COMMIT DROP;
INSERT INTO demo_entries (n, subject, cohort, session_type, teacher, building, room, dow, pair_no, parity, grp, subgroup) VALUES
 (1, 'DAS1110',  'DI-26-01-S1','LECTURE',  'b.akbarov',  'A','101',1,1,'EVERY','BK-DI26/1', NULL),
 (2, 'DAS1110',  'DI-26-01-S1','PRACTICAL','n.ayitbayev','B','301',5,1,'EVERY','BK-DI26/1', NULL),
 (3, 'IAKTT1105','IQT-25-01',  'LECTURE',  'b.akbarov',  'A','101',2,1,'EVERY','B-IQTR26/1',NULL),
 (4, 'IAKTT1105','IQT-25-01',  'SEMINAR',  'n.ayitbayev','A','204',4,2,'EVERY','B-IQTR26/1',NULL),
 (5, 'KIA1406',  'DI-24-01-S3','LECTURE',  'a.zoxidov',  'A','101',1,2,'EVERY','BK-DI25/1', NULL),
 (6, 'KIA1406',  'DI-24-01-S3','PRACTICAL','n.ayitbayev','B','302',2,3,'EVERY','BK-DI25/1', NULL),
 (7, 'KOT1306',  'DI-24-01-S3','LECTURE',  'b.akbarov',  'A','206',3,1,'EVERY','BK-DI25/1', NULL),
 (8, 'KOT1306',  'DI-24-01-S3','PRACTICAL','n.ayitbayev','B','305',4,3,'ODD',  'BK-DI25/1', 'A'),
 (9, 'KOT1306',  'DI-24-01-S3','PRACTICAL','n.ayitbayev','B','305',4,3,'EVEN', 'BK-DI25/1', 'B'),
 (10,'KIA1406',  'DI-24-01-S3','PRACTICAL','n.ayitbayev','B','302',5,2,'EVERY','BK-DI25/1', NULL);

INSERT INTO timetable_entries (id, timetable_id, course_id, session_type, teacher_user_id, room_id,
                               day_of_week, pair_slot_id, week_parity, subgroup)
SELECT d.id, tt.id, c.id, d.session_type, u.id, r.id, d.dow, ps.id, d.parity, d.subgroup
FROM demo_entries d
JOIN subjects s ON s.code = d.subject
JOIN courses c ON c.subject_id = s.id AND c.cohort_code = d.cohort
JOIN timetables tt ON tt.term_id = c.term_id AND tt.status = 'PUBLISHED'
JOIN users u ON u.username = d.teacher
JOIN rooms r ON r.building = d.building AND r.number = d.room
JOIN pair_slots ps ON ps.pair_no = d.pair_no;

INSERT INTO timetable_entry_groups (entry_id, group_id)
SELECT d.id, g.id FROM demo_entries d JOIN student_groups g ON g.code = d.grp;

-- Two one-off changes: a cancelled practical, and a lecture moved to the next day.
INSERT INTO timetable_exceptions (entry_id, on_date, kind, reason, created_by)
SELECT d.id, date '2026-10-16', 'CANCELLED', 'O''qituvchi malaka oshirish kursida',
       (SELECT id FROM users WHERE username = 'admin')
FROM demo_entries d WHERE d.n = 2;

INSERT INTO timetable_exceptions (entry_id, on_date, kind, new_date, new_pair_slot_id, new_room_id, reason, created_by)
SELECT d.id, date '2026-10-19', 'MOVED', date '2026-10-20',
       (SELECT id FROM pair_slots WHERE pair_no = 3),
       (SELECT id FROM rooms WHERE building = 'A' AND number = '204'),
       'Ma''ruza zalida tadbir o''tkaziladi',
       (SELECT id FROM users WHERE username = 'admin')
FROM demo_entries d WHERE d.n = 1;

-- The planned load ("yuklama") the timetable is checked against: 30 hours per class type.
INSERT INTO teaching_load_items (term_id, course_id, group_id, session_type, teacher_user_id, planned_hours)
SELECT DISTINCT c.term_id, c.id, g.id, d.session_type, u.id, 30
FROM demo_entries d
JOIN subjects s ON s.code = d.subject
JOIN courses c ON c.subject_id = s.id AND c.cohort_code = d.cohort
JOIN student_groups g ON g.code = d.grp
JOIN users u ON u.username = d.teacher;

-- ---------------------------------------------------------------- classes already held
-- The Dasturlash practical (Fridays, 1st pair) of the first four weeks, each with its topic and
-- attendance, so the class page, the journal and "my attendance" have history.
INSERT INTO class_sessions (course_id, on_date, pair_no, session_type, groups_key, subgroup, entry_id,
                            teacher_user_id, lesson_id, notes, attendance_marked_at, attendance_marked_by)
SELECT e.course_id, v.on_date, 1, 'PRACTICAL', g.id::text, '', e.id, e.teacher_user_id,
       (SELECT l.id FROM lessons l JOIN course_modules m ON m.id = l.module_id
         WHERE m.course_id = e.course_id AND l.code = v.lesson_code AND l.session_type = 'PRACTICAL'),
       v.notes, v.on_date + time '09:55', e.teacher_user_id
FROM demo_entries d
JOIN timetable_entries e ON e.id = d.id
JOIN student_groups g ON g.code = d.grp
JOIN (VALUES (date '2026-09-04','A1','Kompilyator o''rnatilganini tekshirish; birinchi dastur.'),
             (date '2026-09-11','A2',NULL),
             (date '2026-09-18','A3','Kabisa yili masalasi uyga vazifa qilib berildi.'),
             (date '2026-09-25','A4',NULL)) AS v(on_date, lesson_code, notes) ON true
WHERE d.n = 2;

INSERT INTO class_session_groups (session_id, group_id)
SELECT cs.id, g.id FROM class_sessions cs JOIN student_groups g ON g.id::text = cs.groups_key;

INSERT INTO attendance_records (session_id, student_user_id, status, note, marked_by, marked_at)
SELECT cs.id, p.user_id,
       CASE abs(hashtext(u.username || cs.on_date::text)) % 12
            WHEN 0 THEN 'ABSENT' WHEN 1 THEN 'ABSENT' WHEN 2 THEN 'LATE' WHEN 3 THEN 'EXCUSED'
            ELSE 'PRESENT' END,
       CASE WHEN abs(hashtext(u.username || cs.on_date::text)) % 12 = 3 THEN 'Ma''lumotnoma bor' END,
       cs.attendance_marked_by, cs.attendance_marked_at
FROM class_sessions cs
JOIN student_groups g ON g.id::text = cs.groups_key
JOIN student_profiles p ON p.group_code = g.code
JOIN users u ON u.id = p.user_id AND u.status = 'ACTIVE';

-- ---------------------------------------------------------------- practice bound to lessons
-- Every quiz and coding task a lesson refers to becomes an activity of that lesson, so it shows
-- in "my work", counts in the lesson's practice status, and a submission made from the lesson
-- page is recorded for it. One activity per quiz or task (the first lesson that refers to it).
INSERT INTO activities (course_id, module_id, lesson_id, position, activity_type, reference_id,
                        is_graded, status)
SELECT x.course_id, x.module_id, x.lesson_id,
       x.base_position + row_number() OVER (PARTITION BY x.lesson_id ORDER BY x.block_position) - 1,
       x.activity_type, x.reference_id, false, 'PUBLISHED'
FROM (
    SELECT DISTINCT ON ((cb.settings->>'refId'))
           m.course_id, l.module_id, l.id AS lesson_id, cb.position AS block_position,
           CASE cb.block_type WHEN 'QUIZ_REF' THEN 'QUIZ' ELSE 'CODING_TASK' END AS activity_type,
           (cb.settings->>'refId')::uuid AS reference_id,
           (SELECT coalesce(max(a.position) + 1, 0) FROM activities a WHERE a.lesson_id = l.id) AS base_position
    FROM content_blocks cb
    JOIN lessons l ON l.id = cb.lesson_id
    JOIN course_modules m ON m.id = l.module_id
    WHERE cb.block_type IN ('QUIZ_REF','CODING_TASK_REF') AND cb.deleted_at IS NULL
      AND cb.settings->>'refId' IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM activities a WHERE a.reference_id = (cb.settings->>'refId')::uuid)
    ORDER BY (cb.settings->>'refId'), l.position, cb.position
) x;

INSERT INTO activity_translations (activity_id, locale, title)
SELECT a.id, 'uz', coalesce(tt.title, qt.title)
FROM activities a
LEFT JOIN coding_task_translations tt ON a.activity_type = 'CODING_TASK' AND tt.coding_task_id = a.reference_id AND tt.locale = 'uz'
LEFT JOIN quiz_translations qt ON a.activity_type = 'QUIZ' AND qt.quiz_id = a.reference_id AND qt.locale = 'uz'
WHERE coalesce(tt.title, qt.title) IS NOT NULL
ON CONFLICT DO NOTHING;

-- Deadlines for the first practical tasks of Dasturlash: one already missed, the rest ahead.
UPDATE activities a SET due_at = date_trunc('day', now()) + (v.days || ' days')::interval + interval '18 hours'
FROM coding_tasks t
JOIN (VALUES ('DAS-S1-A1',-3),('DAS-S1-A2',2),('DAS-S1-A3',5),('DAS-S1-A4',9),('DAS-S1-A5',14)) AS v(code, days)
  ON v.code = t.code
WHERE a.activity_type = 'CODING_TASK' AND a.reference_id = t.id;

-- ---------------------------------------------------------------- hints
INSERT INTO coding_task_hints (coding_task_id, position, penalty_percent)
SELECT t.id, v.position, 0 FROM coding_tasks t
JOIN (VALUES ('DAS-S1-A3',0),('DAS-S1-A6',0),('DAS-S1-A6',1),('DAS-S1-A7',0),('DAS-S1-A7',1),('DAS-S1-A7',2)) AS v(code, position)
  ON v.code = t.code;

INSERT INTO coding_task_hint_translations (hint_id, locale, body)
SELECT h.id, 'uz', v.body
FROM coding_task_hints h JOIN coding_tasks t ON t.id = h.coding_task_id
JOIN (VALUES
 ('DAS-S1-A3',0,'100 ga bo''linadigan yil faqat 400 ga ham bo''linsagina kabisa bo''ladi: 1900 — kabisa emas, 2000 — kabisa.'),
 ('DAS-S1-A6',0,'0! = 1. Natijani saqlaydigan o''zgaruvchini 1 dan boshlang.'),
 ('DAS-S1-A6',1,'Sikl 2 dan n gacha yuradi va har qadamda natijani i ga ko''paytiradi. 20! oddiy int ga sig''maydi — 64 bitli tur kerak.'),
 ('DAS-S1-A7',0,'1 tub son emas. Bu holatni alohida tekshiring.'),
 ('DAS-S1-A7',1,'Bo''luvchini n gacha emas, faqat d * d <= n bo''lguncha izlash yetarli.'),
 ('DAS-S1-A7',2,'Birinchi bo''luvchi topilishi bilan siklni to''xtating — qolganini tekshirish shart emas.')
) AS v(code, position, body) ON v.code = t.code AND v.position = h.position;

-- ---------------------------------------------------------------- a teacher's lesson plan
-- The first practical class of Dasturlash carries a plan only its teachers can see.
DO $plan$
DECLARE v_lesson uuid; v_block uuid; v_pos int;
BEGIN
    SELECT l.id INTO v_lesson
    FROM lessons l JOIN course_modules m ON m.id = l.module_id
    JOIN courses c ON c.id = m.course_id JOIN subjects s ON s.id = c.subject_id
    WHERE s.code = 'DAS1110' AND c.cohort_code = 'DI-26-01-S1'
      AND l.code = 'A1' AND l.session_type = 'PRACTICAL';
    IF v_lesson IS NULL THEN
        RAISE NOTICE 'V908: practical lesson A1 of DAS1110 not found; no lesson plan seeded';
        RETURN;
    END IF;
    SELECT coalesce(max(position) + 1, 0) INTO v_pos FROM content_blocks WHERE lesson_id = v_lesson;
    INSERT INTO content_blocks (lesson_id, position, block_type, is_required, visibility, settings)
    VALUES (v_lesson, v_pos, 'LESSON_PLAN', false, 'TEACHER_ONLY', '{}'::jsonb)
    RETURNING id INTO v_block;
    INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
    VALUES (v_block, 'uz', jsonb_build_object(
        'objectives', '<ul><li>Talaba kompilyator va interpretator farqini tushuntira oladi.</li><li>Birinchi dasturni mustaqil yozib, ishga tushiradi.</li></ul>',
        'timeline', jsonb_build_array(
            jsonb_build_object('minutes', 10, 'title', 'Tashkiliy qism va davomat'),
            jsonb_build_object('minutes', 15, 'title', 'Ma''ruzani takrorlash', 'notes', '<p>Identifikator nima? Qaysi nomlar noto''g''ri?</p>'),
            jsonb_build_object('minutes', 35, 'title', 'Birgalikda: birinchi dastur', 'notes', '<p>Platformada «Salomlashish» topshirig''ini uch tilda ko''rsatish.</p>'),
            jsonb_build_object('minutes', 15, 'title', 'Mustaqil ish'),
            jsonb_build_object('minutes', 5, 'title', 'Yakun va uyga vazifa')),
        'materials', '<p>Kompyuter sinfi, proyektor. Har bir talabada platforma akkaunti bo''lishi kerak.</p>',
        'questions', '<ol><li>Kompilyator va interpretator qanday farq qiladi?</li><li>Nega o''zgaruvchi nomi raqam bilan boshlanmaydi?</li></ol>',
        'homework', '<p>«Arifmetik amallar» topshirig''ini kamida ikki tilda yechish.</p>'), '');
END
$plan$;

-- ---------------------------------------------------------------- announcements
INSERT INTO announcements (course_id, author_user_id, title, body, created_at)
SELECT c.id, u.id, 'Amaliy mashg''ulotlar tartibi',
       '<p>Amaliy mashg''ulotlar juma kunlari 1-juftlikda <b>B-301</b> kompyuter sinfida o''tadi. Har bir darsdan oldin tegishli mavzuni platformada o''qib keling.</p>',
       now() - interval '20 days'
FROM courses c JOIN subjects s ON s.id = c.subject_id, users u
WHERE s.code = 'DAS1110' AND c.cohort_code = 'DI-26-01-S1' AND u.username = 'n.ayitbayev';

INSERT INTO announcements (group_id, author_user_id, title, body, created_at)
SELECT g.id, u.id, 'Guruh yig''ilishi',
       '<p>Dushanba kuni ma''ruzadan keyin A-101 xonada guruh yig''ilishi bo''ladi. Barcha talabalarning qatnashishi shart.</p>',
       now() - interval '2 days'
FROM student_groups g, users u WHERE g.code = 'BK-DI26/1' AND u.username = 'admin';

-- ---------------------------------------------------------------- three languages, not two
-- V906 was written when tasks accepted Python and Java; its lesson texts still say so.
UPDATE content_block_translations
SET payload = replace(payload::text, 'Python yoki Java tilida', 'Python, Java yoki C++ tilida')::jsonb,
    plain_text = replace(plain_text, 'Python yoki Java tilida', 'Python, Java yoki C++ tilida')
WHERE payload::text LIKE '%Python yoki Java tilida%';
