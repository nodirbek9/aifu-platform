-- ============================================================================
-- V911 (DEV ONLY) — The demo lesson plan in Russian and English.
--
-- V908 wrote the teacher's plan of DAS1110 A1 in Uzbek only, so a teacher who works in Russian
-- or English saw it in Uzbek on the class page while the lesson itself was in their language.
-- Rows that already exist (written through the API) are left as they are.
-- ============================================================================

INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
SELECT b.id, v.locale, v.payload::jsonb, ''
FROM content_blocks b
JOIN lessons l ON l.id = b.lesson_id
JOIN course_modules m ON m.id = l.module_id
JOIN courses c ON c.id = m.course_id
JOIN subjects s ON s.id = c.subject_id
CROSS JOIN (VALUES
 ('ru', '{
   "objectives": "<ul><li>Студент может объяснить разницу между компилятором и интерпретатором.</li><li>Самостоятельно пишет и запускает первую программу.</li></ul>",
   "materials": "<p>Компьютерный класс, проектор. У каждого студента должна быть учётная запись на платформе.</p>",
   "timeline": [
     {"title": "Организационная часть и посещаемость", "minutes": 10},
     {"title": "Повторение лекции", "minutes": 15, "notes": "<p>Что такое идентификатор? Какие имена неверны?</p>"},
     {"title": "Вместе: первая программа", "minutes": 35, "notes": "<p>Показать на платформе задачу «Приветствие» на трёх языках.</p>"},
     {"title": "Самостоятельная работа", "minutes": 15},
     {"title": "Итоги и домашнее задание", "minutes": 5}],
   "questions": "<ol><li>Чем компилятор отличается от интерпретатора?</li><li>Почему имя переменной не начинается с цифры?</li></ol>",
   "homework": "<p>Решить задачу «Арифметические операции» хотя бы на двух языках.</p>"}'),
 ('en', '{
   "objectives": "<ul><li>The student can explain the difference between a compiler and an interpreter.</li><li>Writes and runs a first program on their own.</li></ul>",
   "materials": "<p>A computer lab and a projector. Every student needs an account on the platform.</p>",
   "timeline": [
     {"title": "Organisation and attendance", "minutes": 10},
     {"title": "Lecture review", "minutes": 15, "notes": "<p>What is an identifier? Which names are invalid?</p>"},
     {"title": "Together: the first program", "minutes": 35, "notes": "<p>Show the \"Greeting\" task on the platform in three languages.</p>"},
     {"title": "Individual work", "minutes": 15},
     {"title": "Summary and homework", "minutes": 5}],
   "questions": "<ol><li>How does a compiler differ from an interpreter?</li><li>Why can a variable name not start with a digit?</li></ol>",
   "homework": "<p>Solve the \"Arithmetic operations\" task in at least two languages.</p>"}')
) AS v(locale, payload)
WHERE b.block_type = 'LESSON_PLAN'
  AND s.code = 'DAS1110' AND c.cohort_code = 'DI-26-01-S1' AND l.code = 'A1'
  AND NOT EXISTS (
      SELECT 1 FROM content_block_translations t WHERE t.block_id = b.id AND t.locale = v.locale);
