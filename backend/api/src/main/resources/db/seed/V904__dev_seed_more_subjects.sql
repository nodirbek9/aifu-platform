-- ============================================================================
-- V904 (DEV ONLY) — Three more subjects from the supplied syllabi.
--
-- Titles, aims, tasks, outcomes, hour budgets, rubrics, topic lists and literature are
-- TRANSCRIBED from academic-materials/_extracted/*.extracted.txt:
--   MTA1204  Ma'lumotlar tuzilmasi va algoritmlar  (1.13 Ma'lumotlar tuzilmasi 142_bo'yicha_DI.doc)
--   KIA1406  Kiberxavfsizlik asoslari               (1.15 Kiberxavfsizlik Asoslari.docx)
--   KOT1306  Kompyuter tarmoqlari                   (1.16 Kompyuter tarmoqlari_sillabus.docx)
-- Module grouping is PLATFORM_INFERRED: every document gives flat topic lists.
--
-- Known discrepancies, kept visible rather than resolved:
--   * KIA1406 and KOT1306 name the programme "60610400 – Dasturiy injiniring", while
--     DAS1110 and MTA1204 use 60610600 for the same programme name. The courses are placed
--     under the existing 60610600 programme; the document code is kept in syllabus_revisions.
--   * MTA1204 gives grade bands but no JN/ON/YaN split, so (as for DAS1110 in V901) only the
--     final component is created. KIA1406 and KOT1306 state 30/30/40 verbatim.
--   * Cohort codes are platform identifiers (the documents name none).
-- ============================================================================

-- ======================= SUBJECT: MTA1204 =======================
INSERT INTO subjects (code, department_id, subject_kind, default_instruction_language, status, published_at)
SELECT 'MTA1204', id, 'MANDATORY', 'uz', 'PUBLISHED', now() FROM departments WHERE code='ANIQ_FANLAR';

INSERT INTO subject_translations (subject_id, locale, title, short_title, aim, objectives)
SELECT id, 'uz', 'Ma''lumotlar tuzilmasi va algoritmlar', 'Ma''lumotlar tuzilmasi',
 'Fanni o''qitishdan maqsad – talabalarni ma''lumotlar tuzilmasi va algoritmlar bilan tanishtirish, amaliy masalalarni yechishda, dasturiy maxsulotlar yaratishda ularni qo''llagan holda optimal yechimni topishni o''rgatish. Ularda mantiqiy va algoritmik mushohada qilish qobiliyatini shakllantirish.',
 'Fanning vazifasi – masalalarni yechishda zarur bo''ladigan mustaqil fikrlash qobiliyatini shakllantirish, yechimni dasturlashtirishda foydalanish mumkin bo''lgan asosiy ma''lumotlar tuzilmalari va algoritmlardan foydalanishni o''rgatishdan iborat.'
FROM subjects WHERE code='MTA1204';

INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'en', 'Data Structures and Algorithms' FROM subjects WHERE code='MTA1204';
INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'ru', 'Структуры данных и алгоритмы' FROM subjects WHERE code='MTA1204';

INSERT INTO subject_programmes (subject_id, programme_id, curriculum_ordinal, recommended_year, recommended_term)
SELECT s.id, p.id, '1.13', 1, 2 FROM subjects s, academic_programmes p
WHERE s.code='MTA1204' AND p.code='60610600';

INSERT INTO subject_prerequisites (subject_id, prerequisite_subject_id, requirement_type, free_text_note)
SELECT s.id, p.id, 'REQUIRED', '(ALG112318) Algebra' FROM subjects s, subjects p
WHERE s.code='MTA1204' AND p.code='ALG112318'
UNION ALL
SELECT id, NULL::uuid, 'REQUIRED', '(MAN1123424) Matematik analiz' FROM subjects WHERE code='MTA1204';

INSERT INTO learning_outcomes (subject_id, code, category, display_order)
SELECT s.id, v.c, v.cat, v.ord FROM subjects s,
 (VALUES ('TN1','COMPETENCY',1),('TN2','COMPETENCY',2),('TN3','SKILL',3),('TN4','SKILL',4),
         ('TN5','SKILL',5),('TN6','SKILL',6),('TN7','SKILL',7)) AS v(c,cat,ord)
WHERE s.code='MTA1204';

INSERT INTO learning_outcome_translations (learning_outcome_id, locale, statement)
SELECT lo.id, 'uz', v.st FROM learning_outcomes lo
JOIN subjects s ON s.id=lo.subject_id AND s.code='MTA1204'
JOIN (VALUES
 ('TN1','Algoritm tushinchasi, stek, dek, ro''yxat, massiv, navbat kabi chiziqli va daraxt, graf kabi nochiziqli berilganlar tuzilmasi, berilganlarning kompyuter xotirasida saqlanishi, algoritmlarni tahlil qilishi va murakkabligini baholashi kerak.'),
 ('TN2','Saralash, qidiruv, satr, daraxt algoritmlari, graflar, xeshlash usullari haqida tasavvurga ega bo''lishi kerak.'),
 ('TN3','Ilmiy faoliyatda kurs bo''yicha olingan bilimlarini qo''llay olish imkoniga ega bo''ladi.'),
 ('TN4','Berilganlarning abstrakt turini (BAT) yarata olishi kerak.'),
 ('TN5','Ular ustida bajariladigan turli xil amallarni tanlangan dasturlash tilida amalga oshira olishi kerak.'),
 ('TN6','Amaliy masalalarni BAT larini qo''llagan holda ular ustida algoritmlarni yaratish yoki mavjudlari ichidan eng optimalini tanlashni bilishi va ulardan foydalana olishi kerak.'),
 ('TN7','Ilmiy-tadqiqot ishlarini bajarishda faol ishtirok etadi.')
) AS v(c,st) ON v.c=lo.code;

-- 2026/2027 semester 2. 3 credits, 90 h (14 lecture + 16 practical + 60 independent), 2 h/week.
INSERT INTO courses (subject_id, term_id, programme_id, cohort_code, credits, total_hours,
                     weekly_contact_hours, instruction_language, required_locales, delivery_mode,
                     grade_scale_id, status, published_at)
SELECT s.id, t.id, p.id, 'DI-26-01-S2', 3, 90, 2, 'uz', ARRAY['uz'], 'FULL_TIME',
       (SELECT id FROM grade_scales WHERE code='AIFU_100_5'), 'PUBLISHED', now()
FROM subjects s, terms t, academic_years ay, academic_programmes p
WHERE s.code='MTA1204' AND ay.code='2026/2027' AND t.academic_year_id=ay.id AND t.ordinal=2
  AND p.code='60610600';

-- ======================= SUBJECT: KIA1406 =======================
INSERT INTO subjects (code, department_id, subject_kind, default_instruction_language, status, published_at)
SELECT 'KIA1406', id, 'MANDATORY', 'uz', 'PUBLISHED', now() FROM departments WHERE code='ANIQ_FANLAR';

INSERT INTO subject_translations (subject_id, locale, title, short_title, aim, objectives)
SELECT id, 'uz', 'Kiberxavfsizlik asoslari', 'Kiberxavfsizlik',
 'Fanni o''qitishdan maqsad — talabalar kasbiy faoliyatida axborot tizimlari va axborot resurslarining kiberxavfsizligini ta''minlash bo''yicha masalalarni yechishda bilim, ko''nikma va malaka shakllantirishdan iborat.',
 'Fanning vazifasi — talabalarni kiberxavfsizlikning asosiy tushunchalari bilan tanishtirish, kriptografiya asoslari, foydalanishni nazoratlash, tarmoq, bulutli hisoblash, internet va kompyuter xavfsizligini ta''minlashning hamda axborot xavfsizligiga tahdidlar va ularga qarshi kurashishni samarali usul va vositalarini o''rgatishdan iborat.'
FROM subjects WHERE code='KIA1406';

INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'en', 'Fundamentals of Cybersecurity' FROM subjects WHERE code='KIA1406';
INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'ru', 'Основы кибербезопасности' FROM subjects WHERE code='KIA1406';

INSERT INTO subject_programmes (subject_id, programme_id, recommended_year, recommended_term)
SELECT s.id, p.id, 2, 3 FROM subjects s, academic_programmes p
WHERE s.code='KIA1406' AND p.code='60610600';

INSERT INTO subject_prerequisites (subject_id, prerequisite_subject_id, requirement_type, free_text_note)
SELECT id, NULL::uuid, 'REQUIRED', 'Informatika va OT asoslari: kompyuter savodxonligi; Windows/Linux muhitida asosiy sozlamalar, fayl tizimi, foydalanuvchi va guruhlar bilan ishlash.' FROM subjects WHERE code='KIA1406'
UNION ALL
SELECT id, NULL::uuid, 'REQUIRED', 'Tarmoq asoslari: IP manzillash, LAN/WLAN, marshrutlash va oddiy tarmoq uskunalari haqida boshlang''ich tushuncha.' FROM subjects WHERE code='KIA1406';

INSERT INTO learning_outcomes (subject_id, code, category, display_order)
SELECT s.id, v.c, v.cat, v.ord FROM subjects s,
 (VALUES ('TN1','KNOWLEDGE',1),('TN2','SKILL',2)) AS v(c,cat,ord)
WHERE s.code='KIA1406';

INSERT INTO learning_outcome_translations (learning_outcome_id, locale, statement)
SELECT lo.id, 'uz', v.st FROM learning_outcomes lo
JOIN subjects s ON s.id=lo.subject_id AND s.code='KIA1406'
JOIN (VALUES
 ('TN1','Kiberxavfsizlik konsepsiyalari: konfidensiallik, yaxlitlik, foydalanish mumkinligi (CIA), tahdid, zaiflik, risk, xavfsizlik nazoratlari. Risklarni boshqarish: risk darajasi va chastotasi, risk matritsasi, jarayon bosqichlari. Kriptografiya asoslari: simmetrik va ochiq kalitli shifrlash, xesh va MAC, elektron raqamli imzo, kriptografik protokollar. Identifikatsiya va autentifikatsiya: parol tizimlari, ko''p omilli autentifikatsiya, biometrik yondashuvlar; foydalanishni boshqarish modellari. Fizik xavfsizlik: tabiiy va sun''iy tahdidlar, kirishni nazorat qilish, axborotni xavfsiz yo''q qilish usullari.'),
 ('TN2','Risk tahlili: aktivlarni aniqlash, tahdid va zaifliklarni xaritalash, risk matritsasini tuzish va ustuvorliklarni belgilash. Kriptovositalar bilan ishlash: fayl va diskni shifrlash, kalitlar yaratish va saqlash, xabarni imzolash va verifikatsiya, sertifikat zanjirini tekshirish. Tarmoq xavfsizligi amaliyoti: trafikni kuzatish va tahlil qilish, firewall qoidalarini yozish, VPN ulash. Zaxira va tiklash: zaxira strategiyasini tanlash, tiklash jarayonini sinovdan o''tkazish; loglarni to''plash, saqlash va auditi uchun minimal tartib yaratish.')
) AS v(c,st) ON v.c=lo.code;

-- 2025/2026, 3rd semester (autumn term of year 2). 6 credits, 180 h (30 + 60 + 90).
INSERT INTO courses (subject_id, term_id, programme_id, cohort_code, credits, total_hours,
                     instruction_language, required_locales, delivery_mode, assessment_form,
                     grade_scale_id, status, published_at)
SELECT s.id, t.id, p.id, 'DI-24-01-S3', 6, 180, 'uz', ARRAY['uz'], 'FULL_TIME', 'EXAM',
       (SELECT id FROM grade_scales WHERE code='AIFU_100_5'), 'PUBLISHED', now()
FROM subjects s, terms t, academic_years ay, academic_programmes p
WHERE s.code='KIA1406' AND ay.code='2025/2026' AND t.academic_year_id=ay.id AND t.ordinal=1
  AND p.code='60610600';

-- ======================= SUBJECT: KOT1306 =======================
INSERT INTO subjects (code, department_id, subject_kind, default_instruction_language, status, published_at)
SELECT 'KOT1306', id, 'MANDATORY', 'uz', 'PUBLISHED', now() FROM departments WHERE code='ANIQ_FANLAR';

INSERT INTO subject_translations (subject_id, locale, title, short_title, aim, objectives)
SELECT id, 'uz', 'Kompyuter tarmoqlari', 'Kompyuter tarmoqlari',
 'Fanni o''qitishdan maqsad – tarmoqlar va axborotni uzatish tizimlarining arxitekturasi va elementlarini; tarmoqlarni va axborot uzatish tizimlarini tashkillanish tamoyillari va ishlashini; lokal va global kompyuter tarmoqlari va axborot uzatish tizimlarining texnik va dasturiy komponentalari strukturasi va bajaradigan vazifasini; axborot uzatish tizimlarida berilganlarni uzatish texnologiyasini; kompyuter tarmoqlari protokollarini; axborot xavfsizligi talablariga muvofiq tarmoqlardan va axborot tizimlaridan foydalanishning xususiyatlarini o''rganish bo''yicha bilim va ko''nikmasini hosil qilishdan iborat.',
 'Fanning vazifasi – talabalarda zamonaviy axborot muhitida tarmoqlarning va axborotni uzatish tizimlarining ahamiyati va imkoniyatini tushunishini shakllantirish, o''z kasbiy sohasida zamonaviy tarmoqlardan va axborot uzatish tizimlaridan foydalanishni o''rgatishdan iborat.'
FROM subjects WHERE code='KOT1306';

INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'en', 'Computer Networks' FROM subjects WHERE code='KOT1306';
INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'ru', 'Компьютерные сети' FROM subjects WHERE code='KOT1306';

INSERT INTO subject_programmes (subject_id, programme_id, recommended_year, recommended_term)
SELECT s.id, p.id, 2, 3 FROM subjects s, academic_programmes p
WHERE s.code='KOT1306' AND p.code='60610600';

INSERT INTO subject_prerequisites (subject_id, prerequisite_subject_id, requirement_type, free_text_note)
SELECT id, NULL::uuid, 'REQUIRED', 'Ushbu fanni o''rganishda kompyuter tarmoqlari bo''yicha ma''lum bilimlarga ega bo''lishi zarur.' FROM subjects WHERE code='KOT1306'
UNION ALL
SELECT id, NULL::uuid, 'REQUIRED', 'Tarmoq texnologiyalari bilan ishlay olish ko''nikmalariga ega bo''lish.' FROM subjects WHERE code='KOT1306';

INSERT INTO learning_outcomes (subject_id, code, category, display_order)
SELECT s.id, v.c, v.cat, v.ord FROM subjects s,
 (VALUES ('TN1','KNOWLEDGE',1),('TN2','SKILL',2)) AS v(c,cat,ord)
WHERE s.code='KOT1306';

INSERT INTO learning_outcome_translations (learning_outcome_id, locale, statement)
SELECT lo.id, 'uz', v.st FROM learning_outcomes lo
JOIN subjects s ON s.id=lo.subject_id AND s.code='KOT1306'
JOIN (VALUES
 ('TN1','Kompyuter tarmoqlarini tashkil qilish va uning faoliyati tamoyillari, lokal tarmoqlarda qurilmalarni o''zaro bog''lanishini tashkil qilish, lokal tarmoqlarning tayanch topologiyalari, zamonaviy, keng tarqalgan protokollar, tarmoq manzili va marshrutlash asoslari, marshrutlash algoritmlari haqida bilim va tasavvurga ega bo''lishi.'),
 ('TN2','Lokal tarmoq yaratish, kerakli qurilmalar tanlash, tarmoqni sozlash, tarmoqda ishlash, unda umumiy resurslarni yaratish va ulardan foydalanish ko''nikmalariga ega bo''lishi; OSI etalon modeli va uning pog''onalarga bo''linishi, tarmoqlarning lokal, mintaqaviy va global turlari, TCP/IP lokal tarmoq ishini diagnostika qilish vositalari, kamchiliklarni bartaraf qilish usullari bo''yicha malakalarga ega bo''lishi kerak.')
) AS v(c,st) ON v.c=lo.code;

-- 2025/2026, 3rd semester. 6 credits, 180 h (30 + 30 + 120).
INSERT INTO courses (subject_id, term_id, programme_id, cohort_code, credits, total_hours,
                     instruction_language, required_locales, delivery_mode, assessment_form,
                     grade_scale_id, status, published_at)
SELECT s.id, t.id, p.id, 'DI-24-01-S3', 6, 180, 'uz', ARRAY['uz'], 'FULL_TIME', 'EXAM',
       (SELECT id FROM grade_scales WHERE code='AIFU_100_5'), 'PUBLISHED', now()
FROM subjects s, terms t, academic_years ay, academic_programmes p
WHERE s.code='KOT1306' AND ay.code='2025/2026' AND t.academic_year_id=ay.id AND t.ordinal=1
  AND p.code='60610600';

-- ======================= Course titles, teachers, hours, rubric =======================
INSERT INTO course_translations (course_id, locale, title)
SELECT c.id, 'uz', v.t FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES ('MTA1204','Ma''lumotlar tuzilmasi va algoritmlar (2026/2027, 2-semestr)'),
             ('KIA1406','Kiberxavfsizlik asoslari (2025/2026, 3-semestr)'),
             ('KOT1306','Kompyuter tarmoqlari (2025/2026, 3-semestr)')) AS v(code,t) ON v.code=s.code;

-- Authors per document: MTA1204 Akbarov + Zoxidov, KIA1406 Zoxidov, KOT1306 Akbarov.
INSERT INTO course_teachers (course_id, teacher_user_id, teacher_role)
SELECT c.id, u.id, v.role FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES ('MTA1204','b.akbarov','LEAD'),('MTA1204','a.zoxidov','ASSISTANT'),
             ('KIA1406','a.zoxidov','LEAD'),('KOT1306','b.akbarov','LEAD')) AS v(code,uname,role) ON v.code=s.code
JOIN users u ON u.username=v.uname;

INSERT INTO course_session_allocations (course_id, session_type, allocated_hours)
SELECT c.id, v.st, v.h FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES ('MTA1204','LECTURE',14),('MTA1204','PRACTICAL',16),('MTA1204','INDEPENDENT_STUDY',60),
             ('KIA1406','LECTURE',30),('KIA1406','PRACTICAL',60),('KIA1406','INDEPENDENT_STUDY',90),
             ('KOT1306','LECTURE',30),('KOT1306','PRACTICAL',30),('KOT1306','INDEPENDENT_STUDY',120)
     ) AS v(code,st,h) ON v.code=s.code;

-- MTA1204: grade bands only, no JN/ON/YaN split in the document (see header).
INSERT INTO assessment_components (course_id, code, kind, max_points, min_pass_percent, blocks_final, display_order)
SELECT c.id, 'YaN', 'FINAL', 100, 60, false, 1
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='MTA1204';

-- KIA1406, KOT1306 verbatim: JN 30 (10 attendance + 10 practical activity + 10 independent
-- work), ON 30 written, YaN 40 written; 60% gates on JN and ON, 60% of YaN or academic debt.
INSERT INTO assessment_components
 (course_id, parent_component_id, code, kind, max_points, min_pass_percent, blocks_final, assessment_form, display_order)
SELECT c.id, NULL::uuid, v.code, v.kind, v.mx, 60, v.bf, v.form, v.ord
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code IN ('KIA1406','KOT1306'),
 (VALUES ('JN','CONTINUOUS',30,true,'MIXED',1),
         ('ON','MIDTERM',30,true,'WRITTEN',2),
         ('YaN','FINAL',40,false,'WRITTEN',3)) AS v(code,kind,mx,bf,form,ord);

INSERT INTO assessment_components
 (course_id, parent_component_id, code, kind, max_points, display_order)
SELECT c.id, jn.id, v.code, v.kind, 10, v.ord
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code IN ('KIA1406','KOT1306')
JOIN assessment_components jn ON jn.course_id=c.id AND jn.code='JN',
 (VALUES ('JN_ATTENDANCE','ATTENDANCE',1),
         ('JN_PRACTICAL','PRACTICAL_ACTIVITY',2),
         ('JN_INDEPENDENT','INDEPENDENT_WORK',3)) AS v(code,kind,ord);

-- ======================= Modules (PLATFORM_INFERRED) =======================
INSERT INTO course_modules (course_id, position, code, status, grouping_source, published_at)
SELECT c.id, v.p, v.m, 'PUBLISHED', 'PLATFORM_INFERRED', now()
FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES
 ('MTA1204',0,'M-ALGORITHMS'),('MTA1204',1,'M-SEARCH-SORT'),('MTA1204',2,'M-STRUCTURES'),('MTA1204',3,'M-INDEPENDENT'),
 ('KIA1406',0,'M-FOUNDATION'),('KIA1406',1,'M-CRYPTO'),('KIA1406',2,'M-ACCESS'),('KIA1406',3,'M-NETWORK'),('KIA1406',4,'M-INDEPENDENT'),
 ('KOT1306',0,'M-FOUNDATION'),('KOT1306',1,'M-LAN'),('KOT1306',2,'M-TCPIP'),('KOT1306',3,'M-SERVICES'),('KOT1306',4,'M-OPERATIONS'),('KOT1306',5,'M-INDEPENDENT')
) AS v(code,p,m) ON v.code=s.code;

INSERT INTO course_module_translations (module_id, locale, title)
SELECT m.id, 'uz', v.t FROM course_modules m
JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES
 ('MTA1204','M-ALGORITHMS','Algoritmlar va ularning murakkabligi'),
 ('MTA1204','M-SEARCH-SORT','Izlash va saralash algoritmlari'),
 ('MTA1204','M-STRUCTURES','Dinamik tuzilmalar, graflar va daraxtlar'),
 ('MTA1204','M-INDEPENDENT','Mustaqil ta''lim topshiriqlari'),
 ('KIA1406','M-FOUNDATION','Kiberxavfsizlik asoslari, risklar va siyosat'),
 ('KIA1406','M-CRYPTO','Kriptografiya'),
 ('KIA1406','M-ACCESS','Autentifikatsiya, foydalanishni boshqarish va fizik xavfsizlik'),
 ('KIA1406','M-NETWORK','Tarmoq, bulut va IoT xavfsizligi'),
 ('KIA1406','M-INDEPENDENT','Mustaqil ta''lim topshiriqlari'),
 ('KOT1306','M-FOUNDATION','Tarmoq asoslari va arxitekturasi'),
 ('KOT1306','M-LAN','Lokal tarmoq texnologiyalari'),
 ('KOT1306','M-TCPIP','TCP/IP, manzillash va marshrutlash'),
 ('KOT1306','M-SERVICES','Amaliy xizmatlar va server tizimlari'),
 ('KOT1306','M-OPERATIONS','Tarmoq xavfsizligi va nosozliklarni bartaraf etish'),
 ('KOT1306','M-INDEPENDENT','Mustaqil ta''lim topshiriqlari')
) AS v(code,m,t) ON v.code=s.code AND v.m=m.code;

-- ======================= Lessons =======================
-- (subject, module, position, lesson code, session type, hours, verbatim title)
CREATE TEMP TABLE v904_lessons (subj text, mod text, pos int, code text, st text, h numeric, title text);
INSERT INTO v904_lessons VALUES
 -- MTA1204 lectures (14 h) and practicals (16 h)
 ('MTA1204','M-ALGORITHMS',0,'M1','LECTURE',2,'Algoritm va uning ta''riflari. Algoritmning intuitiv, formal va kibernetik ta''riflari, xossalari hamda ularning turlari. Tyuring mashinasi va tezisi.'),
 ('MTA1204','M-ALGORITHMS',1,'M2','LECTURE',2,'Algoritmlarning murakkablik tushunchasi. Algoritmlarning murakkabligini hisoblash va murakkablik sinflari. Rekursiv hisoblash algoritmlari. Izlash algoritmlari. Binar va interpolyatsion izlash algoritmlari hamda ularning murakkabligi.'),
 ('MTA1204','M-SEARCH-SORT',0,'M3','LECTURE',2,'Fibonachchi izlash algoritmi va uning murakkabligi. Xeshlash orqali izlash. Saralash algoritmlari. Ichki va tashqi saralash algoritmlari hamda ularning murakkabliklari.'),
 ('MTA1204','M-SEARCH-SORT',1,'M4','LECTURE',2,'Piramidal va Shell saralash algoritmlari va ularning murakkabliklari. Pufaksimon va Xoar saralash algoritmlari hamda ularning murakkabliklari.'),
 ('MTA1204','M-SEARCH-SORT',2,'M5','LECTURE',2,'Bouz-Nelson va parallel saralash algoritmlari tasnifi. Juft-toq saralash algoritmi. Betcher algoritmlari. Berilganlarning dinamik strukturalari. Navbat, stek, dek va ularni massivlar hamda ko''rsatgichlar vositasida yaratish.'),
 ('MTA1204','M-STRUCTURES',0,'M6','LECTURE',2,'Ro''yxatlar. Bir bog''lamli, ikki bog''lamli va chiziqsiz bog''langan ro''yxatlar. Graflar. Graf elementlari, turlari va uning EHMda tasvirlanishi. Deykstra va Floyd algoritmlari.'),
 ('MTA1204','M-STRUCTURES',1,'M7','LECTURE',2,'Daraxtlar – berilganlarning strukturasi sifatida. Yo''naltirilgan, tartiblangan va binar daraxtlar. Muvozanatlashtirilgan daraxtlar. Daraxt va binar daraxt yordamida saralash.'),
 ('MTA1204','M-ALGORITHMS',100,'A1','PRACTICAL',2,'Algoritm va uning ta''riflari. Algoritmning intuitiv, formal va kibernetik ta''riflari, xossalari hamda ularning turlari. Tyuring mashinasi va tezisi.'),
 ('MTA1204','M-ALGORITHMS',101,'A2','PRACTICAL',2,'Algoritmlarning murakkablik tushunchasi. Algoritmlarning murakkabligini hisoblash va murakkablik sinflari. Rekursiv hisoblash algoritmlari.'),
 ('MTA1204','M-SEARCH-SORT',100,'A3','PRACTICAL',2,'Izlash algoritmlari. Binar va interpolyatsion izlash algoritmlari hamda ularning murakkabligi. Fibonachchi izlash algoritmi va uning murakkabligi. Xeshlash orqali izlash.'),
 ('MTA1204','M-SEARCH-SORT',101,'A4','PRACTICAL',2,'Saralash algoritmlari. Ichki va tashqi saralash algoritmlari hamda ularning murakkabliklari. Piramidal va Shell saralash algoritmlari va ularning murakkabliklari.'),
 ('MTA1204','M-SEARCH-SORT',102,'A5','PRACTICAL',2,'Pufaksimon va Xoar saralash algoritmlari hamda ularning murakkabliklari. Bouz-Nelson va parallel saralash algoritmlari tasnifi. Juft-toq saralash algoritmi. Betcher algoritmlari.'),
 ('MTA1204','M-STRUCTURES',100,'A6','PRACTICAL',2,'Berilganlarning dinamik strukturalari. Navbat, stek, dek va ularni massivlar hamda ko''rsatgichlar vositasida yaratish. Ro''yxatlar. Bir bog''lamli, ikki bog''lamli va chiziqsiz bog''langan ro''yxatlar.'),
 ('MTA1204','M-STRUCTURES',101,'A7','PRACTICAL',2,'Graflarda optimallashtirish masalalari. Deykstra va Floyd algoritmlari.'),
 ('MTA1204','M-STRUCTURES',102,'A8','PRACTICAL',2,'Daraxtlar – berilganlarning strukturasi sifatida. Yo''naltirilgan, tartiblangan va binar daraxtlar. Fenvik daraxti. Kesmalar daraxti. Muvozanatlashtirilgan daraxtlar. Daraxt va binar daraxt yordamida saralash.'),
 ('MTA1204','M-INDEPENDENT',200,'MT1','INDEPENDENT_STUDY',30,'Ma''ruza va amaliy mashg''ulotlar mavzularini o''zlashtirish.'),
 ('MTA1204','M-INDEPENDENT',201,'MT2','INDEPENDENT_STUDY',30,'Mustaqil ish va uyga vazifalarni bajarish.'),
 -- KIA1406 lectures (15 x 2 h)
 ('KIA1406','M-FOUNDATION',0,'M1','LECTURE',2,'Kiberxavfsizlik tushunchalari va asosiy tamoyillari. Axborot xavfsizligi, konfidensiallik, yaxlitlik, foydalanish mumkinligi. Tahdid, zaiflik, risk va boshqaruv vositalari.'),
 ('KIA1406','M-FOUNDATION',1,'M2','LECTURE',2,'Risklarni boshqarish. Risk darajasi va chastotasi, risk matritsasi, Risk Management Information Systems (RMIS).'),
 ('KIA1406','M-FOUNDATION',2,'M3','LECTURE',2,'Kiberjinoyatchilik va huquqiy asoslar. Ichki va tashqi kiberjinoyatlar, xalqaro va milliy qonunlar, kiberetika, raqamli kriminalistika.'),
 ('KIA1406','M-FOUNDATION',3,'M4','LECTURE',2,'Kiberxavfsizlik arxitekturasi va siyosati. Xavfsizlik strategiyasi, siyosati, iyerarxiyasi va turlari.'),
 ('KIA1406','M-CRYPTO',0,'M5','LECTURE',2,'Kriptografiyaga kirish. Asosiy tushunchalar, kriptotizimlar, Kerkxofs prinsipi, tarixiy usullar.'),
 ('KIA1406','M-CRYPTO',1,'M6','LECTURE',2,'Simmetrik kriptografik algoritmlar. Oqimli va blokli shifrlash, A5/1 va AES misolida.'),
 ('KIA1406','M-CRYPTO',2,'M7','LECTURE',2,'Ochiq kalitli kriptotizimlar. RSA, modul arifmetikasi, ochiq kalitlar, afzallik va muammolar.'),
 ('KIA1406','M-CRYPTO',3,'M8','LECTURE',2,'Xesh funksiyalar va elektron raqamli imzo. Hash, MAC, ERI, PKI, X.509 sertifikatlari.'),
 ('KIA1406','M-CRYPTO',4,'M9','LECTURE',2,'Kriptografik protokollar va ilovalar. SSL/TLS, IPSec, SSH, blockchain, e-hukumat va elektron to''lovlar.'),
 ('KIA1406','M-ACCESS',0,'M10','LECTURE',2,'Identifikatsiya va autentifikatsiya. Parollar, biometrik tizimlar, ko''p omilli autentifikatsiya.'),
 ('KIA1406','M-ACCESS',1,'M11','LECTURE',2,'Foydalanishni boshqarish modellari. DAC, MAC, RBAC, ABAC, ACL va matritsalar.'),
 ('KIA1406','M-ACCESS',2,'M12','LECTURE',2,'Fizik xavfsizlik. Tabiiy va sun''iy tahdidlar, nazorat choralari, axborotni yo''q qilish usullari.'),
 ('KIA1406','M-NETWORK',0,'M13','LECTURE',2,'Tarmoq xavfsizligi. OSI va TCP/IP modellari, zaiflik va tahdidlar, DoS hujumlari.'),
 ('KIA1406','M-NETWORK',1,'M14','LECTURE',2,'Tarmoq xavfsizligini ta''minlovchi vositalar. Firewall, VPN, IDS, DLP, honeypot.'),
 ('KIA1406','M-NETWORK',2,'M15','LECTURE',2,'Bulutli hisoblash va IoT xavfsizligi. Bulutli tizimlar, maxfiylik masalalari, IoT arxitekturasi va tahdidlar.'),
 -- KIA1406 practicals (15 x 4 h)
 ('KIA1406','M-FOUNDATION',100,'A1','PRACTICAL',4,'Axborot xavfsizligi tushunchalari bo''yicha test va keys-stadi.'),
 ('KIA1406','M-FOUNDATION',101,'A2','PRACTICAL',4,'Risk matritsasini qurish va risklarni baholash (amaliy misol).'),
 ('KIA1406','M-FOUNDATION',102,'A3','PRACTICAL',4,'Kiberjinoyatlar bo''yicha real voqealarni tahlil qilish (case study).'),
 ('KIA1406','M-FOUNDATION',103,'A4','PRACTICAL',4,'Xavfsizlik siyosati loyihasini ishlab chiqish (oddiy tashkilot misolida).'),
 ('KIA1406','M-CRYPTO',100,'A5','PRACTICAL',4,'Klassik shifrlash usullari (Caesar, Vigenere)ni qo''llash.'),
 ('KIA1406','M-CRYPTO',101,'A6','PRACTICAL',4,'Simmetrik algoritmlar (AES) bilan shifrlash dasturida mashq.'),
 ('KIA1406','M-CRYPTO',102,'A7','PRACTICAL',4,'RSA algoritmini oddiy misol orqali qo''llash.'),
 ('KIA1406','M-CRYPTO',103,'A8','PRACTICAL',4,'Hash funksiyalar va ERI yaratish (onlayn vositalarda).'),
 ('KIA1406','M-CRYPTO',104,'A9','PRACTICAL',4,'SSL/TLS sertifikatini tekshirish (brauzer misolida).'),
 ('KIA1406','M-ACCESS',100,'A10','PRACTICAL',4,'Windows OTda foydalanuvchi autentifikatsiyasi va parol siyosatini sozlash.'),
 ('KIA1406','M-ACCESS',101,'A11','PRACTICAL',4,'Linux OTda fayl va katalog huquqlarini boshqarish.'),
 ('KIA1406','M-ACCESS',102,'A12','PRACTICAL',4,'Fizik xavfsizlik bo''yicha real misollarni tahlil qilish (keys).'),
 ('KIA1406','M-NETWORK',100,'A13','PRACTICAL',4,'Tarmoq trafikini monitoring qilish (Wireshark yoki shunga o''xshash vosita).'),
 ('KIA1406','M-NETWORK',101,'A14','PRACTICAL',4,'Virtual VPN ulash va trafikni himoyalash.'),
 ('KIA1406','M-NETWORK',102,'A15','PRACTICAL',4,'IoT qurilmalari xavfsizlik zaifliklarini o''rganish (keys yoki taqdimot).'),
 -- KIA1406 independent study (90 h), verbatim
 ('KIA1406','M-INDEPENDENT',200,'MT1','INDEPENDENT_STUDY',6,'Ma''ruza va amaliy mashg''ulotlar mavzularini o''zlashtirish.'),
 ('KIA1406','M-INDEPENDENT',201,'MT2','INDEPENDENT_STUDY',6,'Mustaqil ish va uyga vazifalarni bajarish.'),
 ('KIA1406','M-INDEPENDENT',202,'MT3','INDEPENDENT_STUDY',6,'Axborotni himoyalashning kriptografik usullari. Enigma shifrlash mashinasi va uning bardoshliligi.'),
 ('KIA1406','M-INDEPENDENT',203,'MT4','INDEPENDENT_STUDY',6,'Axborotni foydalanuvchanligini ta''minlashda antivirus, IDS, IPS vositalarining o''rni. Zararli dasturiy vositalarning klassifikatsiyasi va himoya usullari.'),
 ('KIA1406','M-INDEPENDENT',204,'MT5','INDEPENDENT_STUDY',6,'Axborot-kommunikatsiya texnologiyalari xavfsizligiga bo''ladigan tahdidlar. Zararkunanda dasturlarning turlari.'),
 ('KIA1406','M-INDEPENDENT',205,'MT6','INDEPENDENT_STUDY',6,'Kompyuter viruslari va virusdan himoyalanish usullari. Xorijiy davlatlarning elektron raqamli imzo algoritmlari tahlili. Identifikatsiya va autentifikatsiya tushunchasi va vazifalari.'),
 ('KIA1406','M-INDEPENDENT',206,'MT7','INDEPENDENT_STUDY',6,'Axborot tizimlari va resurslaridan ruxsatsiz foydalanishlarni aniqlash uslubiyati. Simsiz aloqa tizimlarida axborot resurslarini himoyalash.'),
 ('KIA1406','M-INDEPENDENT',207,'MT8','INDEPENDENT_STUDY',6,'Axborotni ruxsatsiz foydalanishlardan himoyalash. Ijtimoiy injeneriyada hujumlar tahlili.'),
 ('KIA1406','M-INDEPENDENT',208,'MT9','INDEPENDENT_STUDY',6,'Zamonaviy antivirus dasturiy vositalari va ularning imkoniyatlari. Kiberjinoyat va kiberhuquq.'),
 ('KIA1406','M-INDEPENDENT',209,'MT10','INDEPENDENT_STUDY',6,'Tarmoqda uzatiluvchi axborotni himoyalashda kriptografiyaning o''rni. Zararkunanda dasturlar va ulardan himoyalanish (masalan, Malwarebytes misolida).'),
 ('KIA1406','M-INDEPENDENT',210,'MT11','INDEPENDENT_STUDY',6,'Kiberjinoyatchilikni keng tarqalishining sabablari. Yuz tasviriga asoslangan autentifikatsiya usuli va uning xususiyatlari.'),
 ('KIA1406','M-INDEPENDENT',211,'MT12','INDEPENDENT_STUDY',6,'Barmoq iziga asoslangan autentifikatsiya usuli. Elektron raqamli imzo.'),
 ('KIA1406','M-INDEPENDENT',212,'MT13','INDEPENDENT_STUDY',6,'Kiberxavfsizlik sohasida karyera yo''llarini o''rganish. Axborot xavfsizligi sohasida ish boshlash.'),
 ('KIA1406','M-INDEPENDENT',213,'MT14','INDEPENDENT_STUDY',4,'Kiberxavfsizlik sohasida mashhur sertifikatlar. Parollarni boshqarish tizimlari (masalan, LastPass) va ulardan foydalanish.'),
 ('KIA1406','M-INDEPENDENT',214,'MT15','INDEPENDENT_STUDY',4,'Virtual himoyalangan tarmoq va undan amaliyotda foydalanish (masalan, CyberGhost yoki ExpressVPN misolida).'),
 ('KIA1406','M-INDEPENDENT',215,'MT16','INDEPENDENT_STUDY',2,'Sotsial injineriya. ESET NOD32 antivirus vositasidan foydalanish.'),
 ('KIA1406','M-INDEPENDENT',216,'MT17','INDEPENDENT_STUDY',2,'Windows OTda zaxira nusxalashni (Backup and Restore) amalga oshirish. Windows OTda imtiyozlar turlari, fayl va kataloglar uchun foydalanishni boshqarish tartibi.'),
 -- KOT1306 lectures (15 x 2 h)
 ('KOT1306','M-FOUNDATION',0,'M1','LECTURE',2,'Kirish. Hisoblash tarmoqlarining rivojlanish tarixi. Kompyuter tarmoqlari klassifikatsiyasi.'),
 ('KOT1306','M-FOUNDATION',1,'M2','LECTURE',2,'Tarmoq qurishning asosiy tamoyillari. Tarmoq topologiyalari. Tarmoq arxitekturasi. Tarmoq qurilmalari.'),
 ('KOT1306','M-FOUNDATION',2,'M3','LECTURE',2,'ISO/OSI modeli. Ochiq tizimlarning o''zaro muloqoti. Tarmoq pog''onalari. Protokol, interfeys, standartlar tushunchalari.'),
 ('KOT1306','M-FOUNDATION',3,'M4','LECTURE',2,'Berilganlarni uzatish usullari. Berilganlarni uzatishning fizik muhitlari. Berilganlarni kodlash va uzatish usullari.'),
 ('KOT1306','M-LAN',0,'M5','LECTURE',2,'Ethernet texnologiyalari. Tarmoq tugunlarini manzillash. MAC manzil tushunchasi.'),
 ('KOT1306','M-LAN',1,'M6','LECTURE',2,'Token Ring va boshqa texnologiyalar. Token Ring, 100VG-AnyLAN, FDDI, Wi-Fi texnologiyalari.'),
 ('KOT1306','M-TCPIP',0,'M7','LECTURE',2,'TCP/IP protokollar steki. Tarmoqlararo berilganlarni almashish tamoyillari. IP protokollari haqida tushuncha.'),
 ('KOT1306','M-TCPIP',1,'M8','LECTURE',2,'IPv4 protokoli asosida tarmoq tugunlarini manzillash. IPv4 protokoli va uning asosida tarmoq tugunlarini manzillash usullari.'),
 ('KOT1306','M-TCPIP',2,'M9','LECTURE',2,'Tarmoq protokollari va marshrutlash protokollari. Tarmoq protokollari va xizmatlari (ARP, ICMP, DHCP). Marshrutlash protokollari va ularning ishlash tamoyillari.'),
 ('KOT1306','M-TCPIP',3,'M10','LECTURE',2,'Transport pog''onasining vazifalari. TCP va UDP protokollari. Soketlar tushunchasi.'),
 ('KOT1306','M-SERVICES',0,'M11','LECTURE',2,'Amaliy pog''ona protokollari va xizmatlari. Veb server xizmati. HTTP, FTP, elektron pochta protokollari.'),
 ('KOT1306','M-SERVICES',1,'M12','LECTURE',2,'Internet tarmog''i va DNS xizmati. DNS xizmati va uning ishlash tamoyili.'),
 ('KOT1306','M-SERVICES',2,'M13','LECTURE',2,'Server operatsion tizimlar va ishchi guruhlar. Server operatsion tizimlar va ularning vazifalari. Ishchi guruhlar va domenli tizimlar.'),
 ('KOT1306','M-OPERATIONS',0,'M14','LECTURE',2,'Tarmoqlarda xavfsizlik muammolari. Xavfsizlikni ta''minlash usullari (Firewall, VPN, IDS).'),
 ('KOT1306','M-OPERATIONS',1,'M15','LECTURE',2,'Tarmoq muammolarini bartaraf qilish. Tarmoqning ishlashi jarayonida yuzaga keladigan muammolarni aniqlash va ularni bartaraf qilish.'),
 -- KOT1306 practicals (15 x 2 h)
 ('KOT1306','M-FOUNDATION',100,'A1','PRACTICAL',2,'Tarmoq qurilmalari va ularning vazifalari. Tarmoq adapterlari, takrorlagich, ko''prik, konsentrator.'),
 ('KOT1306','M-FOUNDATION',101,'A2','PRACTICAL',2,'Tarmoq topologiyalarini o''rganish.'),
 ('KOT1306','M-FOUNDATION',102,'A3','PRACTICAL',2,'Tarmoq emulyatorlari va ulardan foydalanish.'),
 ('KOT1306','M-FOUNDATION',103,'A4','PRACTICAL',2,'Berilganlarni kodlash usullari.'),
 ('KOT1306','M-LAN',100,'A5','PRACTICAL',2,'Lokal tarmoq texnologiyalari: Ethernet, Fast Ethernet. Tarmoq snifferlari yordamida paketlarni tahlil qilish.'),
 ('KOT1306','M-LAN',101,'A6','PRACTICAL',2,'Simsiz axborot uzatish texnologiyalari. Wi-Fi tarmog''ini sozlash.'),
 ('KOT1306','M-TCPIP',100,'A7','PRACTICAL',2,'IP-manzillar. Sinfli manzillar. Maskalar yordamida manzillash. IP manzillardan tejamkor foydalanish.'),
 ('KOT1306','M-TCPIP',101,'A8','PRACTICAL',2,'Marshrutizatorlarni boshqarish.'),
 ('KOT1306','M-TCPIP',102,'A9','PRACTICAL',2,'Soketlar yordamida tarmoq dasturini yaratish.'),
 ('KOT1306','M-SERVICES',100,'A10','PRACTICAL',2,'Windows Server operatsion tizimini o''rnatish va boshlang''ich sozlash.'),
 ('KOT1306','M-SERVICES',101,'A11','PRACTICAL',2,'Windows Server operatsion tizimida tarmoq xizmatlarini o''rnatish va sozlash (DHCP, DNS).'),
 ('KOT1306','M-SERVICES',102,'A12','PRACTICAL',2,'Windows operatsion tizimida foydalanuvchilar va ularning vakolatlarini boshqarish.'),
 ('KOT1306','M-SERVICES',103,'A13','PRACTICAL',2,'Tarmoq umumiy resurslarni yaratish va ulardan foydalanish.'),
 ('KOT1306','M-OPERATIONS',100,'A14','PRACTICAL',2,'Windows operatsion tizimida brandmauerni sozlash. VPN xizmatidan foydalanish.'),
 ('KOT1306','M-OPERATIONS',101,'A15','PRACTICAL',2,'Tarmoq nosozliklarini aniqlash, operatsion tizim utilitalaridan foydalanish.'),
 ('KOT1306','M-INDEPENDENT',200,'MT1','INDEPENDENT_STUDY',60,'Ma''ruza va amaliy mashg''ulotlar mavzularini o''zlashtirish.'),
 ('KOT1306','M-INDEPENDENT',201,'MT2','INDEPENDENT_STUDY',60,'Mustaqil ish va uyga vazifalarni bajarish.');

INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, v.pos, v.code, v.st, v.h, true, 'PUBLISHED', now()
FROM v904_lessons v
JOIN subjects s ON s.code=v.subj
JOIN courses c ON c.subject_id=s.id
JOIN course_modules m ON m.course_id=c.id AND m.code=v.mod;

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.title
FROM v904_lessons v
JOIN subjects s ON s.code=v.subj
JOIN courses c ON c.subject_id=s.id
JOIN course_modules m ON m.course_id=c.id AND m.code=v.mod
JOIN lessons l ON l.module_id=m.id AND l.code=v.code AND l.session_type=v.st;

DROP TABLE v904_lessons;

-- ======================= Literature (verbatim) =======================
INSERT INTO course_materials (course_id, material_type, position, citation, url)
SELECT c.id, v.mt, v.p, v.cit, v.url
FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES
 ('MTA1204','PRIMARY_LITERATURE',1,'Gaynazarov S.M., Saidov D.Y. Algoritmlar va berilganlar tuzilmasi. O''quv qo''llanma. Toshkent, 2022, 250 bet.',NULL),
 ('MTA1204','PRIMARY_LITERATURE',2,'M.O''. Ashurov, Sh.A. Sattorova, Sh.U. Usmonqulov. Algoritmlar. Toshkent, 2018.',NULL),
 ('MTA1204','SUPPLEMENTARY_LITERATURE',3,'M. A. Weiss. Data Structures & Algorithm Analysis in C++, 4th edition, 2014.',NULL),
 ('MTA1204','SUPPLEMENTARY_LITERATURE',4,'Aho A.V., Hopcroft J.E., Ullman J.D. Data Structures and Algorithms, 2003.',NULL),
 ('MTA1204','SUPPLEMENTARY_LITERATURE',5,'Bucknall J. The Tomes of Delphi: Algorithms and Data Structures, 2003.',NULL),
 ('MTA1204','WEB_RESOURCE',6,'Aniq va ijtimoiy fanlar universiteti rasmiy web-sayti','https://www.aifu.uz'),
 ('MTA1204','WEB_RESOURCE',7,'Yagona elektron kutubxona tizimi','https://www.unilibrary.uz'),
 ('KIA1406','PRIMARY_LITERATURE',1,'Robin Sharp. Introduction to Cybersecurity: A Multidisciplinary Challenge. Springer Cham, 2023. 452 p.',NULL),
 ('KIA1406','PRIMARY_LITERATURE',2,'S.K. Ganiyev, A.A. Ganiyev, Z.T. Xudoykulov. Kiberxavfsizlik asoslari: o''quv qo''llanma. T.: "Nihol print", 2021. 224 b.',NULL),
 ('KIA1406','PRIMARY_LITERATURE',3,'Tairakawa Kousho. Securing the Digital Frontier: An Introduction to Cybersecurity for Beginners. Independently published. 139 p.',NULL),
 ('KIA1406','SUPPLEMENTARY_LITERATURE',4,'Ian Neil. CompTIA Security+ Certification Guide. Birmingham: Packt Publishing, 2018.',NULL),
 ('KIA1406','SUPPLEMENTARY_LITERATURE',5,'Wu, C. IoT Network Layer Security. In: Internet of Things Security. Springer Singapore, 2021, pp. 107-123.',NULL),
 ('KIA1406','WEB_RESOURCE',6,'Aniq va ijtimoiy fanlar universiteti rasmiy web-sayti','https://www.aifu.uz'),
 ('KIA1406','WEB_RESOURCE',7,'Cisco Networking Academy','https://www.cisco.com/site/us/en/learn/training-certifications/training/netacad/index.html'),
 ('KOT1306','PRIMARY_LITERATURE',1,'Tanenbaum E., Feamster N., Wetherall D. Kompyuternye seti. 6-e izd. SPb.: Piter, 2023. 992 s.',NULL),
 ('KOT1306','PRIMARY_LITERATURE',2,'Kurose J., Ross K. Kompyuternye seti: Nishodyashchiy podhod. 6-e izd. M.: "E", 2016. 912 s.',NULL),
 ('KOT1306','PRIMARY_LITERATURE',3,'Olifer V., Olifer N. Kompyuternye seti. Principy, tehnologii, protokoly. 5-e izd. 992 s.',NULL),
 ('KOT1306','PRIMARY_LITERATURE',4,'Pulatov S.I. Kompyuter tarmoqlari. Darslik. T.: Ma''rifat, 2024, 268 b.',NULL),
 ('KOT1306','SUPPLEMENTARY_LITERATURE',5,'Dordal P. L. An Introduction to Computer Networks. Release 2.0.6. Loyola University Chicago, 2021.',NULL),
 ('KOT1306','SUPPLEMENTARY_LITERATURE',6,'Miryusupov Z.Z., Djumanov J.X. Kompyuter tarmoqlari. O''quv qo''llanma. T.: Aloqachi, 2020, 144 b.',NULL),
 ('KOT1306','WEB_RESOURCE',7,'Aniq va ijtimoiy fanlar universiteti rasmiy web-sayti','https://www.aifu.uz'),
 ('KOT1306','WEB_RESOURCE',8,'Cisco Networking Academy','https://www.cisco.com/site/us/en/learn/training-certifications/training/netacad/index.html')
) AS v(code,mt,p,cit,url) ON v.code=s.code;

INSERT INTO syllabus_revisions (course_id, revision, status, authors, reviewers, source_document_ref, notes)
SELECT c.id, 1, 'APPROVED', v.authors::jsonb, v.reviewers::jsonb, v.doc, v.note
FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES
 ('MTA1204',
  '[{"name":"B. Akbarov","title":"dotsent v.b.","organisation":"AIFU, Aniq fanlar kafedrasi"},{"name":"A. Zoxidov","title":"katta o''qituvchi","organisation":"AIFU, Aniq fanlar kafedrasi"}]',
  '[{"name":"N.A. Ignatev","title":"O''zMU professori, f-m.f.d."},{"name":"Sh.F. Madraximov","title":"O''zMU professori, t.f.d."}]',
  'academic-materials/syllabi/1.13 Ma''lumotlar tuzilmasi 142_bo''yicha_DI.doc',
  'Hour table in the source is partly garbled; 14 lecture + 16 practical + 60 independent = 90 h follows its topic lists.'),
 ('KIA1406',
  '[{"name":"Zoxidov Abdulla","title":"katta o''qituvchi","organisation":"AIFU, Aniq fanlar kafedrasi"}]',
  '[{"name":"Sh. Pulatov","title":"TATU, Mobil aloqa texnologiyalari kafedrasi dotsenti, t.f.n."},{"name":"B. Akbarov","title":"AIFU, katta o''qituvchi"}]',
  'academic-materials/syllabi/1.15 Kiberxavfsizlik Asoslari.docx',
  'Document names programme code 60610400; placed under 60610600 (same programme name).'),
 ('KOT1306',
  '[{"name":"Akbarov Baxriddin","title":"katta o''qituvchi","organisation":"AIFU, Aniq fanlar kafedrasi"}]',
  '[{"name":"B. Hayitqulov","title":"O''zMU, Dasturiy injiniring va sun''iy intellekt kafedrasi dotsenti, PhD"},{"name":"Sh. Sapayev","title":"AIFU, katta o''qituvchi"}]',
  'academic-materials/syllabi/1.16 Kompyuter tarmoqlari_sillabus.docx',
  'Document names programme code 60610400; placed under 60610600 (same programme name).')
) AS v(code,authors,reviewers,doc,note) ON v.code=s.code;

-- ======================= Dev enrolments =======================
-- student3..student5 are the software-engineering dev students (V900 programme 60610600).
INSERT INTO enrollments (course_id, student_user_id, status, source, enrolled_at)
SELECT c.id, u.id, 'ACTIVE', 'ADMIN', now() - interval '5 days'
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code IN ('MTA1204','KIA1406','KOT1306'),
     users u WHERE u.username IN ('student3','student4','student5');

INSERT INTO enrollment_events (enrollment_id, event_type, to_status, occurred_at)
SELECT e.id, 'CREATED', e.status, e.enrolled_at FROM enrollments e
JOIN courses c ON c.id=e.course_id JOIN subjects s ON s.id=c.subject_id AND s.code IN ('MTA1204','KIA1406','KOT1306');

INSERT INTO course_progress (enrollment_id, status, completion_percent, lessons_total, lessons_completed)
SELECT e.id, 'NOT_STARTED', 0,
       (SELECT count(*) FROM lessons l JOIN course_modules m ON m.id=l.module_id
         WHERE m.course_id = e.course_id AND l.status='PUBLISHED'),
       0
FROM enrollments e JOIN courses c ON c.id=e.course_id
JOIN subjects s ON s.id=c.subject_id AND s.code IN ('MTA1204','KIA1406','KOT1306');
