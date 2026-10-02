-- ============================================================================
-- V909 (DEV ONLY) — One quiz with every question type, in three languages.
--
-- The quizzes of V906 use four of the seven question types and are written in Uzbek only, and
-- the demo student's main course (Dasturlash) has no quiz at all. This adds one to the lecture
-- "Dasturlash tillarining tuzilmasi": single choice, multiple choice, true / false, numeric,
-- short answer, ordering and matching — so the quiz screen (S-06) can be seen and tested with
-- each of them, and the language switch has something to show.
-- ============================================================================

CREATE FUNCTION pg_temp.seed_question(
    p_quiz uuid, p_position int, p_type text, p_points numeric,
    p_uz text, p_ru text, p_en text,
    p_why_uz text, p_why_ru text, p_why_en text) RETURNS uuid AS $fn$
DECLARE
    v_id uuid;
BEGIN
    INSERT INTO questions (quiz_id, position, question_type, points, difficulty)
    VALUES (p_quiz, p_position, p_type, p_points, 'EASY')
    RETURNING id INTO v_id;
    INSERT INTO question_translations (question_id, locale, prompt, explanation) VALUES
        (v_id, 'uz', p_uz, p_why_uz),
        (v_id, 'ru', p_ru, p_why_ru),
        (v_id, 'en', p_en, p_why_en);
    RETURN v_id;
END;
$fn$ LANGUAGE plpgsql;

CREATE FUNCTION pg_temp.seed_option(
    p_question uuid, p_position int, p_correct boolean, p_match_key text,
    p_uz text, p_ru text, p_en text) RETURNS void AS $fn$
DECLARE
    v_id uuid;
BEGIN
    INSERT INTO answer_options (question_id, position, is_correct, match_key)
    VALUES (p_question, p_position, p_correct, p_match_key)
    RETURNING id INTO v_id;
    INSERT INTO answer_option_translations (option_id, locale, text) VALUES
        (v_id, 'uz', p_uz), (v_id, 'ru', p_ru), (v_id, 'en', p_en);
END;
$fn$ LANGUAGE plpgsql;

DO $seed$
DECLARE
    v_course   uuid;
    v_module   uuid;
    v_lesson   uuid;
    v_quiz     uuid;
    v_q        uuid;
    v_activity uuid;
BEGIN
    SELECT c.id INTO v_course
    FROM courses c JOIN subjects s ON s.id = c.subject_id
    WHERE s.code = 'DAS1110' AND c.cohort_code = 'DI-26-01-S1';
    IF v_course IS NULL OR EXISTS (SELECT 1 FROM quizzes WHERE code = 'DAS-Q-M2') THEN
        RETURN;
    END IF;
    SELECT l.id, l.module_id INTO v_lesson, v_module
    FROM lessons l JOIN course_modules m ON m.id = l.module_id
    WHERE m.course_id = v_course AND l.code = 'M2' AND l.deleted_at IS NULL;
    IF v_lesson IS NULL THEN
        RETURN;
    END IF;

    INSERT INTO quizzes (course_id, code, status, max_points, passing_percent, time_limit_seconds,
                         max_attempts, shuffle_questions, shuffle_options, reveal_policy, published_at)
    VALUES (v_course, 'DAS-Q-M2', 'PUBLISHED', 10, 60, 600, 3, false, true, 'AFTER_ATTEMPT', now())
    RETURNING id INTO v_quiz;

    INSERT INTO quiz_translations (quiz_id, locale, title, description, instructions) VALUES
        (v_quiz, 'uz', 'Dasturlash asoslari: takrorlash testi',
         'Dastlabki ikki maʼruza boʻyicha qisqa test: algoritm, oʻzgaruvchilar, maʼlumot turlari va amallar.',
         'Testda 7 ta savol bor, ularning turi har xil: variant tanlash, son yoki soʻz kiritish, tartiblash va juftlash. Har bir savolni diqqat bilan oʻqing.'),
        (v_quiz, 'ru', 'Основы программирования: тест для повторения',
         'Короткий тест по первым двум лекциям: алгоритм, переменные, типы данных и операции.',
         'В тесте 7 вопросов разных типов: выбор варианта, ввод числа или слова, упорядочивание и сопоставление. Внимательно читайте каждый вопрос.'),
        (v_quiz, 'en', 'Programming basics: a revision quiz',
         'A short quiz on the first two lectures: algorithms, variables, data types and operators.',
         'The quiz has 7 questions of different kinds: choosing an option, typing a number or a word, ordering and matching. Read each question carefully.');

    -- 1. single choice
    v_q := pg_temp.seed_question(v_quiz, 0, 'SINGLE_CHOICE', 1,
        'Algoritm nima?', 'Что такое алгоритм?', 'What is an algorithm?',
        'Algoritm — masalani yechishga olib boradigan aniq va chekli qadamlar ketma-ketligi. U biror dasturlash tiliga bogʻliq emas.',
        'Алгоритм — это точная и конечная последовательность шагов, ведущая к решению задачи. Он не зависит от языка программирования.',
        'An algorithm is an exact, finite sequence of steps that leads to the solution of a problem. It does not depend on a programming language.');
    PERFORM pg_temp.seed_option(v_q, 0, true, NULL,
        'Masalani yechish uchun aniq qadamlar ketma-ketligi',
        'Точная последовательность шагов для решения задачи',
        'An exact sequence of steps that solves a problem');
    PERFORM pg_temp.seed_option(v_q, 1, false, NULL,
        'Dasturlash tilining bir turi', 'Разновидность языка программирования', 'A kind of programming language');
    PERFORM pg_temp.seed_option(v_q, 2, false, NULL,
        'Kompyuterning hisoblash qurilmasi', 'Вычислительное устройство компьютера', 'The computing unit of a computer');
    PERFORM pg_temp.seed_option(v_q, 3, false, NULL,
        'Dastur kodi saqlanadigan fayl', 'Файл, в котором хранится код программы', 'A file that stores program code');

    -- 2. multiple choice
    v_q := pg_temp.seed_question(v_quiz, 1, 'MULTIPLE_CHOICE', 2,
        'Quyidagi qiymatlarning qaysilari butun son turiga (int) mansub?',
        'Какие из этих значений относятся к целому типу (int)?',
        'Which of these values are of the integer type (int)?',
        '42 va −7 — butun sonlar. 3.14 — haqiqiy son (float), qoʻshtirnoq ichidagi "25" esa satr (str).',
        '42 и −7 — целые числа. 3.14 — вещественное число (float), а "25" в кавычках — строка (str).',
        '42 and −7 are integers. 3.14 is a floating-point number, and "25" in quotes is a string.');
    PERFORM pg_temp.seed_option(v_q, 0, true, NULL, '42', '42', '42');
    PERFORM pg_temp.seed_option(v_q, 1, true, NULL, '−7', '−7', '−7');
    PERFORM pg_temp.seed_option(v_q, 2, false, NULL, '3.14', '3.14', '3.14');
    PERFORM pg_temp.seed_option(v_q, 3, false, NULL, '"25"', '"25"', '"25"');

    -- 3. true / false
    v_q := pg_temp.seed_question(v_quiz, 2, 'TRUE_FALSE', 1,
        'Oʻzgaruvchi nomi raqam bilan boshlanishi mumkin.',
        'Имя переменной может начинаться с цифры.',
        'A variable name may start with a digit.',
        'Python, Java va C++ tillarida oʻzgaruvchi nomi harf yoki ostki chiziq bilan boshlanadi; raqam faqat keyingi oʻrinlarda keladi.',
        'В Python, Java и C++ имя переменной начинается с буквы или подчёркивания; цифра может стоять только дальше.',
        'In Python, Java and C++ a variable name starts with a letter or an underscore; a digit may only come later.');
    PERFORM pg_temp.seed_option(v_q, 0, false, NULL, 'Toʻgʻri', 'Верно', 'True');
    PERFORM pg_temp.seed_option(v_q, 1, true, NULL, 'Notoʻgʻri', 'Неверно', 'False');

    -- 4. numeric
    v_q := pg_temp.seed_question(v_quiz, 3, 'NUMERIC', 1,
        '17 % 5 ifodasining qiymati nechaga teng? (% — boʻlishdagi qoldiq amali)',
        'Чему равно значение выражения 17 % 5? (% — остаток от деления)',
        'What is the value of 17 % 5? (% is the remainder of a division)',
        '17 = 5 · 3 + 2, demak qoldiq 2 ga teng.',
        '17 = 5 · 3 + 2, значит остаток равен 2.',
        '17 = 5 · 3 + 2, so the remainder is 2.');
    INSERT INTO question_answer_keys (question_id, match_mode, expected_value, tolerance)
    VALUES (v_q, 'NUMERIC_TOLERANCE', '2', 0);

    -- 5. short answer
    v_q := pg_temp.seed_question(v_quiz, 4, 'SHORT_ANSWER', 1,
        'Python tilida ekranga matn chiqaradigan funksiyaning nomini yozing.',
        'Напишите имя функции, которая выводит текст на экран в Python.',
        'Type the name of the Python function that writes text to the screen.',
        'print() funksiyasi oʻziga berilgan qiymatlarni ekranga chiqaradi.',
        'Функция print() выводит на экран переданные ей значения.',
        'The print() function writes the values it is given to the screen.');
    INSERT INTO question_answer_keys (question_id, match_mode, expected_value) VALUES
        (v_q, 'CASE_INSENSITIVE', 'print'),
        (v_q, 'CASE_INSENSITIVE', 'print()');

    -- 6. ordering: the stored positions are the right order
    v_q := pg_temp.seed_question(v_quiz, 5, 'ORDERING', 2,
        'Dastur yaratish bosqichlarini toʻgʻri tartibda joylashtiring.',
        'Расположите этапы создания программы в правильном порядке.',
        'Put the stages of writing a program in the right order.',
        'Avval masala tahlil qilinadi, soʻng algoritm tuziladi, kod yoziladi va oxirida dastur testlanadi.',
        'Сначала задачу анализируют, затем составляют алгоритм, пишут код и в конце тестируют программу.',
        'First the problem is analysed, then the algorithm is designed, the code is written, and finally the program is tested.');
    PERFORM pg_temp.seed_option(v_q, 0, false, NULL,
        'Masalani tahlil qilish', 'Анализ задачи', 'Analysing the problem');
    PERFORM pg_temp.seed_option(v_q, 1, false, NULL,
        'Algoritm tuzish', 'Составление алгоритма', 'Designing the algorithm');
    PERFORM pg_temp.seed_option(v_q, 2, false, NULL,
        'Kodni yozish', 'Написание кода', 'Writing the code');
    PERFORM pg_temp.seed_option(v_q, 3, false, NULL,
        'Testlash va xatolarni tuzatish', 'Тестирование и исправление ошибок', 'Testing and fixing errors');

    -- 7. matching: options with the same key belong together
    v_q := pg_temp.seed_question(v_quiz, 6, 'MATCHING', 2,
        'Har bir qiymatni uning maʼlumot turi bilan juftlang.',
        'Сопоставьте каждое значение с его типом данных.',
        'Match each value with its data type.',
        '42 — butun son, 3.14 — haqiqiy son, "salom" — satr, True — mantiqiy qiymat.',
        '42 — целое число, 3.14 — вещественное, "salom" — строка, True — логическое значение.',
        '42 is an integer, 3.14 a floating-point number, "salom" a string, and True a boolean.');
    PERFORM pg_temp.seed_option(v_q, 0, false, 'int', '42', '42', '42');
    PERFORM pg_temp.seed_option(v_q, 1, false, 'int', 'butun son (int)', 'целое число (int)', 'integer (int)');
    PERFORM pg_temp.seed_option(v_q, 2, false, 'float', '3.14', '3.14', '3.14');
    PERFORM pg_temp.seed_option(v_q, 3, false, 'float', 'haqiqiy son (float)', 'вещественное число (float)', 'floating-point number (float)');
    PERFORM pg_temp.seed_option(v_q, 4, false, 'str', '"salom"', '"salom"', '"salom"');
    PERFORM pg_temp.seed_option(v_q, 5, false, 'str', 'satr (str)', 'строка (str)', 'string (str)');
    PERFORM pg_temp.seed_option(v_q, 6, false, 'bool', 'True', 'True', 'True');
    PERFORM pg_temp.seed_option(v_q, 7, false, 'bool', 'mantiqiy qiymat (bool)', 'логическое значение (bool)', 'boolean (bool)');

    -- the quiz is work of the lecture: it shows in the lesson, in the plan and in "my work"
    INSERT INTO activities (course_id, module_id, lesson_id, position, activity_type, reference_id,
                            is_graded, attempt_limit, due_at, status)
    VALUES (v_course, v_module, v_lesson,
            (SELECT coalesce(max(a.position) + 1, 0) FROM activities a WHERE a.lesson_id = v_lesson),
            'QUIZ', v_quiz, false, 3, date_trunc('day', now()) + interval '9 days 18 hours', 'PUBLISHED')
    RETURNING id INTO v_activity;
    INSERT INTO activity_translations (activity_id, locale, title) VALUES
        (v_activity, 'uz', 'Dasturlash asoslari: takrorlash testi'),
        (v_activity, 'ru', 'Основы программирования: тест для повторения'),
        (v_activity, 'en', 'Programming basics: a revision quiz');
END;
$seed$;
