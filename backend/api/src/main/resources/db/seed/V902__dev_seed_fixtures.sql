-- ============================================================================
-- V902 (DEV ONLY) — Illustrative fixtures.
--
-- ⚠️  UNLIKE V901, NOTHING IN THIS FILE COMES FROM AN AIFU DOCUMENT.
-- These are development fixtures that exercise the engine (content blocks, a quiz,
-- a coding task, enrollments, progress). They are NOT academic requirements and must
-- NOT be treated as AIFU content. Real content arrives via the teacher/admin API.
-- ============================================================================

-- ---------- Content blocks on one lecture, demonstrating six block types ----------
INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
SELECT l.id, v.p, v.bt, true, v.st::jsonb
FROM lessons l
JOIN lesson_translations lt ON lt.lesson_id = l.id AND lt.locale = 'uz'
JOIN course_modules m ON m.id = l.module_id
JOIN courses c ON c.id = m.course_id
JOIN subjects s ON s.id = c.subject_id AND s.code = 'IAKTT1105',
 (VALUES
  (0,'TEXT','{}'),
  (1,'CALLOUT','{"variant":"INFO"}'),
  (2,'DIAGRAM','{"format":"mermaid"}'),
  (3,'CODE_EXAMPLE','{"language":"python","highlightLines":[2]}'),
  (4,'LINK','{"openInNewTab":true}'),
  (5,'QUIZ_REF','{}')
 ) AS v(p,bt,st)
WHERE l.code = 'M7' AND l.session_type = 'LECTURE';

-- Payloads are JSONB, shape validated per block_type by ContentBlockPayloadValidator.
INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
SELECT b.id, 'uz', v.payload::jsonb, v.plain
FROM content_blocks b
JOIN lessons l ON l.id = b.lesson_id AND l.code = 'M7' AND l.session_type='LECTURE'
JOIN course_modules m ON m.id=l.module_id JOIN courses c ON c.id=m.course_id
JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105'
JOIN (VALUES
 (0,'{"format":"markdown","body":"Ma''lumotlar bazasi - bu tartiblangan, o''zaro bog''langan ma''lumotlar to''plami. Katta hajmdagi ma''lumotlar (Big Data) uchta asosiy xususiyat bilan tavsiflanadi: hajm (volume), tezlik (velocity) va xilma-xillik (variety)."}',
     'Ma''lumotlar bazasi tartiblangan ma''lumotlar to''plami. Big Data: hajm, tezlik, xilma-xillik.'),
 (1,'{"title":"Eslatma","body":"Relatsion va NoSQL bazalar turli masalalar uchun mos keladi."}',
     'Eslatma: Relatsion va NoSQL bazalar turli masalalar uchun mos keladi.'),
 (2,'{"caption":"Ma''lumotlar bazasi arxitekturasi","source":"graph LR; A[Ilova] --> B[MBBT]; B --> C[(Saqlash)]"}',
     'Ma''lumotlar bazasi arxitekturasi diagrammasi.'),
 (3,'{"language":"python","caption":"SQL so''rovini Python orqali bajarish","code":"import sqlite3\nconn = sqlite3.connect(\":memory:\")\nconn.execute(\"CREATE TABLE mahsulot (id INTEGER, narx REAL)\")\nprint(\"jadval yaratildi\")"}',
     'Python sqlite3 misoli: jadval yaratish.'),
 (4,'{"url":"https://ocw.mit.edu/courses/6-5830-database-systems-fall-2023/","label":"MIT 6.5830 Database Systems","note":"QS 14 / THE 2 - sillabusdagi benchmarking havolasi"}',
     'MIT 6.5830 Database Systems havolasi.'),
 (5,'{"note":"Mini-test: ma''lumotlar bazalari asoslari"}',
     'Mini-test havolasi.')
) AS v(p,payload,plain) ON v.p = b.position;

-- ---------- Demo quiz (FIXTURE) ----------
INSERT INTO quizzes (course_id, code, status, max_points, passing_percent, time_limit_seconds,
                     max_attempts, shuffle_questions, shuffle_options, reveal_policy, published_at)
SELECT c.id, 'DEV-QUIZ-DB', 'PUBLISHED', 3, 60, 600, 3, true, true, 'AFTER_LAST_ATTEMPT', now()
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='IAKTT1105';

INSERT INTO quiz_translations (quiz_id, locale, title, description, instructions)
SELECT id, 'uz', 'Ma''lumotlar bazalari asoslari (demo test)',
       'Rivojlanish uchun namuna test. Akademik talab emas.',
       'Har bir savolga bitta yoki bir nechta to''g''ri javobni tanlang.'
FROM quizzes WHERE code='DEV-QUIZ-DB';

INSERT INTO questions (quiz_id, position, question_type, points, difficulty, settings)
SELECT q.id, v.p, v.qt, 1, v.d, v.st::jsonb FROM quizzes q,
 (VALUES (0,'SINGLE_CHOICE','EASY','{}'),
         (1,'MULTIPLE_CHOICE','MEDIUM','{"partialCredit":true}'),
         (2,'NUMERIC','EASY','{}')) AS v(p,qt,d,st)
WHERE q.code='DEV-QUIZ-DB';

INSERT INTO question_translations (question_id, locale, prompt, explanation)
SELECT qu.id, 'uz', v.pr, v.ex FROM questions qu JOIN quizzes q ON q.id=qu.quiz_id AND q.code='DEV-QUIZ-DB'
JOIN (VALUES
 (0,'SQL tilida jadvaldan ma''lumot olish uchun qaysi operator ishlatiladi?','SELECT operatori tanlash uchun ishlatiladi.'),
 (1,'Big Data ning "3V" xususiyatlari qaysilar?','Volume, Velocity, Variety.'),
 (2,'Relatsion modelda jadvalning satri nima deb ataladi? Nechta asosiy kalit (primary key) bir jadvalda bo''lishi mumkin?','Bir jadvalda faqat bitta asosiy kalit bo''ladi.')
) AS v(p,pr,ex) ON v.p = qu.position;

INSERT INTO answer_options (question_id, position, is_correct)
SELECT qu.id, v.p, v.c FROM questions qu JOIN quizzes q ON q.id=qu.quiz_id AND q.code='DEV-QUIZ-DB'
JOIN (VALUES (0,0,true),(0,1,false),(0,2,false),(0,3,false),
             (1,0,true),(1,1,true),(1,2,true),(1,3,false)) AS v(qp,p,c) ON v.qp = qu.position;

INSERT INTO answer_option_translations (option_id, locale, text)
SELECT o.id, 'uz', v.t FROM answer_options o
JOIN questions qu ON qu.id=o.question_id JOIN quizzes q ON q.id=qu.quiz_id AND q.code='DEV-QUIZ-DB'
JOIN (VALUES (0,0,'SELECT'),(0,1,'UPDATE'),(0,2,'DELETE'),(0,3,'CREATE'),
             (1,0,'Volume (hajm)'),(1,1,'Velocity (tezlik)'),(1,2,'Variety (xilma-xillik)'),
             (1,3,'Validity (haqiqiylik)')) AS v(qp,op,t)
  ON v.qp = qu.position AND v.op = o.position;

INSERT INTO question_answer_keys (question_id, match_mode, expected_value, tolerance)
SELECT qu.id, 'NUMERIC_TOLERANCE', '1', 0
FROM questions qu JOIN quizzes q ON q.id=qu.quiz_id AND q.code='DEV-QUIZ-DB'
WHERE qu.position = 2;

-- ---------- Demo coding task (FIXTURE) ----------
-- Python + Go per the product requirement. C++ is intentionally NOT attached
-- because the language is disabled pending the PENDING-INPUTS.md §2 decision.
-- memory_limit_kb=262144 (256MB), not the tighter 128MB originally here: real docker-run
-- verification (Phase 8) found `go build` from a cold cache (no cross-run cache reuse, by
-- design — see DockerRunSandboxEngine) needs meaningfully more than 128MB even for a
-- two-import "sum the numbers" program; 128MB reliably OOM-killed the compiler. 256MB matches
-- aifu.sandbox.default-memory-limit-kb, so this fixture no longer tightens the platform ceiling.
INSERT INTO coding_tasks (course_id, code, difficulty, status, max_points, scoring_strategy,
                          max_attempts, cpu_time_limit_ms, wall_time_limit_ms, memory_limit_kb, published_at)
SELECT c.id, 'DEV-TASK-SUM', 'EASY', 'PUBLISHED', 100, 'PER_TEST_WEIGHTED', 10,
       2000, 5000, 262144, now()
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110'
WHERE c.cohort_code='DI-26-01-S1';

INSERT INTO coding_task_translations (coding_task_id, locale, title, description, instructions, constraints_text)
SELECT id, 'uz', 'Massiv elementlari yig''indisi',
 'Standart kirishdan n ta butun son o''qib, ularning yig''indisini chiqaruvchi dastur yozing.',
 'Birinchi satrda n soni, ikkinchi satrda bo''sh joy bilan ajratilgan n ta butun son beriladi. Yig''indini bitta satrda chiqaring.',
 '1 <= n <= 1000, har bir son -10^6 dan 10^6 gacha.'
FROM coding_tasks WHERE code='DEV-TASK-SUM';

INSERT INTO coding_task_languages (coding_task_id, language_code, starter_code, solution_code)
SELECT id, 'python',
E'n = int(input())\nnums = list(map(int, input().split()))\n# TODO: yig\'indini hisoblang va chiqaring\n',
E'n = int(input())\nprint(sum(map(int, input().split())))\n'
FROM coding_tasks WHERE code='DEV-TASK-SUM';

INSERT INTO coding_task_languages (coding_task_id, language_code, starter_code, solution_code)
SELECT id, 'go',
E'package main\n\nimport "fmt"\n\nfunc main() {\n\tvar n int\n\tfmt.Scan(&n)\n\t// TODO: yig\'indini hisoblang va chiqaring\n}\n',
E'package main\n\nimport "fmt"\n\nfunc main() {\n\tvar n int\n\tfmt.Scan(&n)\n\tsum := 0\n\tfor i := 0; i < n; i++ {\n\t\tvar x int\n\t\tfmt.Scan(&x)\n\t\tsum += x\n\t}\n\tfmt.Println(sum)\n}\n'
FROM coding_tasks WHERE code='DEV-TASK-SUM';

INSERT INTO coding_task_test_cases
 (coding_task_id, position, name, visibility, is_example, weight, stdin, expected_stdout, comparison_mode)
SELECT t.id, v.p, v.n, v.vis, v.ex, v.w, v.i, v.o, 'TRIMMED'
FROM coding_tasks t,
 (VALUES (0,'Namuna 1','VISIBLE',true, 1,E'3\n1 2 3',        '6'),
         (1,'Namuna 2','VISIBLE',true, 1,E'1\n42',            '42'),
         (2,'Manfiy sonlar','HIDDEN',false,2,E'4\n-5 5 -3 3', '0'),
         (3,'Katta qiymatlar','HIDDEN',false,3,E'5\n1000000 1000000 1000000 1000000 1000000','5000000')
 ) AS v(p,n,vis,ex,w,i,o)
WHERE t.code='DEV-TASK-SUM';

-- ---------- Activities binding the quiz and task into the curriculum ----------
INSERT INTO activities (course_id, module_id, lesson_id, position, activity_type, reference_id,
                        assessment_component_id, is_graded, max_points, attempt_limit, status)
SELECT c.id, l.module_id, l.id, 0, 'QUIZ', q.id, ac.id, true, 3, 3, 'PUBLISHED'
FROM quizzes q
JOIN courses c ON c.id = q.course_id
JOIN subjects s ON s.id = c.subject_id AND s.code='IAKTT1105'
JOIN course_modules m ON m.course_id = c.id
JOIN lessons l ON l.module_id = m.id AND l.code='M7' AND l.session_type='LECTURE'
JOIN assessment_components ac ON ac.course_id = c.id AND ac.code='JN_INDEPENDENT'
WHERE q.code='DEV-QUIZ-DB';

INSERT INTO activity_translations (activity_id, locale, title, instructions)
SELECT a.id, 'uz', 'Mini-test: ma''lumotlar bazalari', 'Testni 10 daqiqada yakunlang.'
FROM activities a WHERE a.activity_type='QUIZ';

INSERT INTO activities (course_id, module_id, lesson_id, position, activity_type, reference_id,
                        assessment_component_id, is_graded, max_points, attempt_limit,
                        collaboration_mode, variant_strategy, status)
SELECT c.id, l.module_id, l.id, 0, 'CODING_TASK', t.id, ac.id, true, 100, 10,
       'INDIVIDUAL', 'NONE', 'PUBLISHED'
FROM coding_tasks t
JOIN courses c ON c.id = t.course_id
JOIN course_modules m ON m.course_id = c.id AND m.code='M-ARRAYS'
JOIN lessons l ON l.module_id = m.id AND l.code='A9' AND l.session_type='PRACTICAL'
JOIN assessment_components ac ON ac.course_id = c.id AND ac.code='YaN'
WHERE t.code='DEV-TASK-SUM';

INSERT INTO activity_translations (activity_id, locale, title, instructions)
SELECT a.id, 'uz', 'Amaliy topshiriq: massiv yig''indisi', 'Yechimni Python yoki Go tilida topshiring.'
FROM activities a WHERE a.activity_type='CODING_TASK';

-- ---------- Enrollments + progress ----------
INSERT INTO enrollments (course_id, student_user_id, status, source, enrolled_at)
SELECT c.id, u.id, 'ACTIVE', 'ADMIN', now() - interval '20 days'
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105',
     users u WHERE u.username IN ('student1','student2')
UNION ALL
SELECT c.id, u.id, 'ACTIVE', 'ADMIN', now() - interval '10 days'
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110',
     users u WHERE u.username IN ('student3','student4') AND c.cohort_code='DI-26-01-S1';

INSERT INTO enrollment_events (enrollment_id, event_type, to_status, occurred_at)
SELECT id, 'CREATED', status, enrolled_at FROM enrollments;

INSERT INTO course_progress (enrollment_id, status, completion_percent, lessons_total,
                             lessons_completed, started_at, last_accessed_at)
SELECT e.id, 'IN_PROGRESS', 15.00,
       (SELECT count(*) FROM lessons l JOIN course_modules m ON m.id=l.module_id
         WHERE m.course_id = e.course_id AND l.status='PUBLISHED'),
       2, now() - interval '18 days', now() - interval '2 days'
FROM enrollments e;

-- One student deliberately left stale so v_students_at_risk returns a row in dev.
UPDATE course_progress SET last_accessed_at = now() - interval '30 days', completion_percent = 4.00
WHERE enrollment_id IN (
    SELECT e.id FROM enrollments e JOIN users u ON u.id = e.student_user_id
    WHERE u.username = 'student2');
