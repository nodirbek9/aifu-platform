-- ============================================================================
-- V901 (DEV ONLY) — The two subjects we actually have syllabi for.
-- Titles, aims, outcomes, hour budgets, rubrics, topic lists, literature and the
-- QS/THE benchmarking links are TRANSCRIBED VERBATIM from the supplied documents.
-- Module grouping is marked PLATFORM_INFERRED: both documents give flat topic lists.
-- No other subject is seeded. See docs/academic-analysis/PENDING-INPUTS.md §1.
-- ============================================================================

-- ======================= SUBJECT 1: IAKTT1105 =======================
INSERT INTO subjects (code, department_id, subject_kind, default_instruction_language, status, published_at)
SELECT 'IAKTT1105', id, 'MANDATORY', 'uz', 'PUBLISHED', now() FROM departments WHERE code='ANIQ_FANLAR';

INSERT INTO subject_translations (subject_id, locale, title, short_title, aim, objectives)
SELECT id, 'uz',
 'Iqtisodiyotda axborot kommunikatsion texnologiyalar va tizimlar',
 'IAKT va tizimlar',
 'Fanni o''qitishdan maqsad – talabalarda mazkur fanning nazariy va amaliy asoslarini o''zlashtirish, iqtisodiyotning turli soha va tarmoqlari statistik ma''lumotlarini qayta ishlash va tashkil qilish tendensiyalari haqida nazariy-metodologik va ilmiy-amaliy bilim hosil qilishdan iborat.',
 'Fanning vazifasi – turli iqtisodiyot tarmoqlarining axborot tizimlari tasnifi va ularda qo''llanilayotgan axborot texnologiyalari, raqamli va axborot texnologiyalarining rivojlanish tendensiyalari, rivojlanish yo''nalishlari va ularning tavsifi haqida tasavvurga ega bo''lishi kerak.'
FROM subjects WHERE code='IAKTT1105';

INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'en', 'Information and Communication Technologies and Systems in Economics'
FROM subjects WHERE code='IAKTT1105';

INSERT INTO subject_programmes (subject_id, programme_id, recommended_year, recommended_term)
SELECT s.id, p.id, 1, 1 FROM subjects s, academic_programmes p
WHERE s.code='IAKTT1105' AND p.code='60410100';

-- Prose-only prerequisites: kept verbatim, no coded subject to point at.
INSERT INTO subject_prerequisites (subject_id, prerequisite_subject_id, requirement_type, free_text_note)
SELECT id, NULL::uuid, 'REQUIRED', 'Ushbu fanni o''rganishda boshlang''ich matematik bilimlarga ega bo''lishi zarur.' FROM subjects WHERE code='IAKTT1105'
UNION ALL
SELECT id, NULL::uuid, 'REQUIRED', 'Kompyuter qurilmalari bilan ishlash ko''nikmasi bo''lishi zarur.' FROM subjects WHERE code='IAKTT1105';

INSERT INTO learning_outcomes (subject_id, code, category, display_order)
SELECT s.id, v.c, v.cat, v.ord FROM subjects s,
 (VALUES ('TN1','KNOWLEDGE',1),('TN2','KNOWLEDGE',2),('TN3','SKILL',3)) AS v(c,cat,ord)
WHERE s.code='IAKTT1105';

INSERT INTO learning_outcome_translations (learning_outcome_id, locale, statement)
SELECT lo.id, 'uz', v.st FROM learning_outcomes lo
JOIN subjects s ON s.id = lo.subject_id AND s.code='IAKTT1105'
JOIN (VALUES
 ('TN1','Turli iqtisodiyot tarmoqlarining axborot tizimlari tasnifi va ularda qo''llanilayotgan axborot texnologiyalari, raqamli va axborot texnologiyalarining rivojlanish tendensiyalari, rivojlanish yo''nalishlari va ularning tavsifi haqida tasavvurga ega bo''lishi.'),
 ('TN2','Lokal hisoblash tarmoqlari va internet tarmog''i funksional imkoniyatlaridan foydalana olishi.'),
 ('TN3','Zamonaviy amaliy dasturiy ta''minotini bilishi va ulardan foydalana olishi kerak. Iqtisodiyotning turli soha va tarmoqlari statistik ma''lumotlarini qayta ishlash va tashkil qilish bo''yicha tasavvurga ega bo''lishi.')
) AS v(c,st) ON v.c = lo.code;

-- Course = the 2025/2026 semester-1 delivery. 5 credits, 150 hours (verbatim).
INSERT INTO courses (subject_id, term_id, programme_id, cohort_code, credits, total_hours,
                     instruction_language, required_locales, delivery_mode, assessment_form,
                     grade_scale_id, status, published_at)
SELECT s.id, t.id, p.id, 'IQT-25-01', 5, 150, 'uz', ARRAY['uz'], 'FULL_TIME', 'EXAM',
       (SELECT id FROM grade_scales WHERE code='AIFU_100_5'), 'PUBLISHED', now()
FROM subjects s, terms t, academic_years ay, academic_programmes p
WHERE s.code='IAKTT1105' AND ay.code='2025/2026' AND t.academic_year_id=ay.id AND t.ordinal=1
  AND p.code='60410100';

INSERT INTO course_translations (course_id, locale, title)
SELECT c.id, 'uz', 'Iqtisodiyotda axborot kommunikatsion texnologiyalar va tizimlar (2025/2026, 1-semestr)'
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='IAKTT1105';

INSERT INTO course_teachers (course_id, teacher_user_id, teacher_role)
SELECT c.id, u.id, 'LEAD' FROM courses c JOIN subjects s ON s.id=c.subject_id, users u
WHERE s.code='IAKTT1105' AND u.username='b.akbarov';

-- Verbatim hour budget: 30 lecture + 30 seminar + 90 independent = 150.
INSERT INTO course_session_allocations (course_id, session_type, allocated_hours)
SELECT c.id, v.st, v.h FROM courses c JOIN subjects s ON s.id=c.subject_id,
 (VALUES ('LECTURE',30),('SEMINAR',30),('INDEPENDENT_STUDY',90)) AS v(st,h)
WHERE s.code='IAKTT1105';

-- Verbatim rubric: JN 30 (10 attendance + 10 practical activity + 10 independent work),
-- ON 30 written, YaN 40 written. 60% gates on JN, ON and YaN.
INSERT INTO assessment_components
 (course_id, parent_component_id, code, kind, max_points, min_pass_percent, blocks_final, assessment_form, display_order)
SELECT c.id, NULL::uuid, 'JN',  'CONTINUOUS', 30, 60, true,  'MIXED',   1 FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='IAKTT1105'
UNION ALL SELECT c.id, NULL::uuid, 'ON',  'MIDTERM',    30, 60, true,  'WRITTEN', 2 FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='IAKTT1105'
UNION ALL SELECT c.id, NULL::uuid, 'YaN', 'FINAL',      40, 60, false, 'WRITTEN', 3 FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='IAKTT1105';

INSERT INTO assessment_components
 (course_id, parent_component_id, code, kind, max_points, display_order)
SELECT c.id, jn.id, v.code, v.kind, 10, v.ord
FROM courses c JOIN subjects s ON s.id=c.subject_id
JOIN assessment_components jn ON jn.course_id=c.id AND jn.code='JN',
 (VALUES ('JN_ATTENDANCE','ATTENDANCE',1),
         ('JN_PRACTICAL','PRACTICAL_ACTIVITY',2),
         ('JN_INDEPENDENT','INDEPENDENT_WORK',3)) AS v(code,kind,ord)
WHERE s.code='IAKTT1105';

-- Modules: PLATFORM_INFERRED. Both documents are flat topic lists (PENDING-INPUTS.md §3).
INSERT INTO course_modules (course_id, position, code, status, grouping_source, published_at)
SELECT c.id, v.p, v.code, 'PUBLISHED', 'PLATFORM_INFERRED', now()
FROM courses c JOIN subjects s ON s.id=c.subject_id,
 (VALUES (0,'M-FOUNDATION'),(1,'M-TOOLS'),(2,'M-DATA-NETWORKS'),
         (3,'M-SYSTEMS'),(4,'M-EMERGING'),(5,'M-SECURITY'),(6,'M-INDEPENDENT')) AS v(p,code)
WHERE s.code='IAKTT1105';

INSERT INTO course_module_translations (module_id, locale, title)
SELECT m.id, 'uz', v.t FROM course_modules m
JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105'
JOIN (VALUES ('M-FOUNDATION','Kirish va axborot texnologiyalari infratuzilmasi'),
             ('M-TOOLS','Iqtisodiyotda dasturiy vositalar'),
             ('M-DATA-NETWORKS','Tarmoq texnologiyalari va ma''lumotlar bazalari'),
             ('M-SYSTEMS','Axborot tizimlari va elektron hukumat'),
             ('M-EMERGING','Bulutli texnologiyalar, blokcheyn va elektron biznes'),
             ('M-SECURITY','Axborot xavfsizligi va kasbiy axborot komplekslari'),
             ('M-INDEPENDENT','Mustaqil ta''lim topshiriqlari')) AS v(c,t) ON v.c=m.code;

-- Lectures (ma'ruza): 15 topics x 2 h = 30 h. Verbatim titles.
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, v.p, v.code, 'LECTURE', 2, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES
 ('M-FOUNDATION',0,'M1'),('M-FOUNDATION',1,'M2'),
 ('M-TOOLS',0,'M3'),('M-TOOLS',1,'M4'),('M-TOOLS',2,'M5'),
 ('M-DATA-NETWORKS',0,'M6'),('M-DATA-NETWORKS',1,'M7'),('M-DATA-NETWORKS',2,'M8'),
 ('M-SYSTEMS',0,'M9'),('M-SYSTEMS',1,'M10'),
 ('M-EMERGING',0,'M11'),('M-EMERGING',1,'M12'),('M-EMERGING',2,'M13'),
 ('M-SECURITY',0,'M14'),('M-SECURITY',1,'M15')
) AS v(mod,p,code) ON v.mod=m.code
WHERE s.code='IAKTT1105';

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id JOIN courses c ON c.id=m.course_id
JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105'
JOIN (VALUES
 ('M1','"Iqtisodiyotda axborot-kommunikatsiya texnologiyalari va tizimlari" faniga kirish'),
 ('M2','Axborot texnologiyalarining infratuzilmasi'),
 ('M3','Iqtisodiyotda foydalaniladigan dasturiy mahsulotlar'),
 ('M4','Jadval redaktorlardan iqtisodiy masalalarni echish jarayonida foydalanish'),
 ('M5','Taqdimotlarni ishlab chiqishning instrumental vositalari'),
 ('M6','Tarmoq texnologiyalari va internet xizmatlari'),
 ('M7','Ma''lumotlar bazalari va katta hajmdagi ma''lumotlar bilan ishlash texnologiyalari'),
 ('M8','Algoritmlashtirish va dasturlash asoslari'),
 ('M9','Axborot tizimlarning tushunchasi'),
 ('M10','Elektron hukumat'),
 ('M11','Bulutli texnologiyalar va ular bilan ishlash'),
 ('M12','Blokcheyn texnologiyasi va undan foydalanish yo''llari'),
 ('M13','Elektron biznes va mobil tijorat'),
 ('M14','Axborot xavfsizligi ta''minlash usullari'),
 ('M15','Iqtisodiyotning kasbiy faoliyatidagi axborot komplekslari va texnologiyalari')
) AS v(c,t) ON v.c=l.code AND l.session_type='LECTURE';

-- Seminars (S1..S15): 15 x 2 h = 30 h. Verbatim titles.
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, 100 + v.p, v.code, 'SEMINAR', 2, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id
JOIN (VALUES
 ('M-FOUNDATION',0,'S1'),('M-FOUNDATION',1,'S2'),('M-FOUNDATION',2,'S3'),
 ('M-TOOLS',0,'S4'),('M-TOOLS',1,'S5'),
 ('M-DATA-NETWORKS',0,'S6'),('M-DATA-NETWORKS',1,'S7'),('M-DATA-NETWORKS',2,'S8'),
 ('M-SYSTEMS',0,'S9'),('M-SYSTEMS',1,'S10'),
 ('M-EMERGING',0,'S11'),('M-EMERGING',1,'S12'),('M-EMERGING',2,'S13'),
 ('M-SECURITY',0,'S14'),('M-SECURITY',1,'S15')
) AS v(mod,p,code) ON v.mod=m.code
WHERE s.code='IAKTT1105';

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id JOIN courses c ON c.id=m.course_id
JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105'
JOIN (VALUES
 ('S1','Iqtisodiyotda axborot-kommunikatsion texnologiyalar va tizimlar fanining ahamiyati, maqsadi va vazifalari.'),
 ('S2','Iqtisodiy masalalarni echish jarayonida foydalaniladigan axborotni kiritish, chiqarish va saqlash uchun zamonaviy qurilmalarning turlari.'),
 ('S3','Microsoft Windows, Apple Macintosh va Linux operatsion tizimlarining umumiy tavsiflari.'),
 ('S4','Microsoft Excel dasturida ma''lumotlarni ko''rish, qayta ishlash va umumlashtirish vositalari.'),
 ('S5','Taqdimot muharririda amallarni bajarish.'),
 ('S6','Tarmoq texnologiyalaridan milliy iqtisodiyot tarmoqlari va sohalarida foydalanish.'),
 ('S7','Katta ma''lumotlarni (Big Data) qayta ishlash texnologiyalari.'),
 ('S8','Hisoblash jarayonlarining grafik tasviri.'),
 ('S9','Axborot tizimlarining asosiy tarkibiy qismlari.'),
 ('S10','Interaktiv xizmatlar va ular bilan ishlash.'),
 ('S11','Bulutli texnologiyalarni turli sohalarda amalga oshirish tamoyillari.'),
 ('S12','Blokcheyn texnologiyasidan turli tarmoq sohalarida foydalanish xususiyatlari.'),
 ('S13','Internet-banking, Internet-savdo, Internet-sug''urta, Internet-marketing.'),
 ('S14','Axborotni himoya qilish uchun kriptografik usullardan foydalanish.'),
 ('S15','Soliq hisobotlari va bank kredit, omonat turlari.')
) AS v(c,t) ON v.c=l.code AND l.session_type='SEMINAR';

-- Independent study (MT1..MT31): 90 h. MT1 is 30 h, the rest 2 h each. Verbatim.
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, 200 + v.p, v.code, 'INDEPENDENT_STUDY', v.h, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id,
 (VALUES (1,'MT1',30),(2,'MT2',2),(3,'MT3',2),(4,'MT4',2),(5,'MT5',2),(6,'MT6',2),(7,'MT7',2),
         (8,'MT8',2),(9,'MT9',2),(10,'MT10',2),(11,'MT11',2),(12,'MT12',2),(13,'MT13',2),
         (14,'MT14',2),(15,'MT15',2),(16,'MT16',2),(17,'MT17',2),(18,'MT18',2),(19,'MT19',2),
         (20,'MT20',2),(21,'MT21',2),(22,'MT22',2),(23,'MT23',2),(24,'MT24',2),(25,'MT25',2),
         (26,'MT26',2),(27,'MT27',2),(28,'MT28',2),(29,'MT29',2),(30,'MT30',2),(31,'MT31',2)
 ) AS v(p,code,h)
WHERE s.code='IAKTT1105' AND m.code='M-INDEPENDENT';

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id JOIN courses c ON c.id=m.course_id
JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105'
JOIN (VALUES
 ('MT1','Ma''ruza va amaliy mashg''ulotlar mavzularini o''zlashtirish'),
 ('MT2','Ma''lumotlarni statistik tahlil qilish vositalari.'),
 ('MT3','Taqdimot muharrirlarining asosiy tushunchasi va tamoyillari.'),
 ('MT4','Zamonaviy taqdimot muharrirlari tahlili.'),
 ('MT5','Kompyuter tarmoqlari tushunchasi va turlari.'),
 ('MT6','Tarmoq texnologiyalaridan milliy iqtisodiyot tarmoqlari va sohalarida foydalanish.'),
 ('MT7','Internet xizmatlari bilan ishlash.'),
 ('MT8','Ma''lumotlar bazalari va ularni ishlab chiqish modellari.'),
 ('MT9','Katta ma''lumotlarni (Big Data) qayta ishlash texnologiyalari.'),
 ('MT10','Algoritm tushunchasi va uning xususiyatlari.'),
 ('MT11','Hisoblash jarayonlarining grafik tasviri.'),
 ('MT12','Dasturlash tillari haqida umumiy ma''lumot.'),
 ('MT13','Tarmoqlanuvchi hisoblash jarayonini dasturlash.'),
 ('MT14','Axborot tizimlarining tashkilotdagi o''rni va vazifalari.'),
 ('MT15','Axborot tizimlarining asosiy tarkibiy qismlari.'),
 ('MT16','Axborot tizimlarining tasnifi.'),
 ('MT17','Elektron hukumat tushunchasi va vazifalari.'),
 ('MT18','Elektron hukumatni joriy etish bo''yicha jahon tajribasi.'),
 ('MT19','Interaktiv xizmatlar va ular bilan ishlash.'),
 ('MT20','Bulutli texnologiyalarning asosiy turlari.'),
 ('MT21','Bulutli texnologiyalarda axborot xavfsizligini ta''minlash.'),
 ('MT22','Blokcheyn texnologiyasi va uning qo''llanilishi.'),
 ('MT23','Blokcheyn ishlash tamoyili.'),
 ('MT24','Blokcheyn texnologiyasidan turli tarmoq sohalarida foydalanish xususiyatlari.'),
 ('MT25','Elektron bozor va elektron tovarlar tushunchasi.'),
 ('MT26','Elektron biznes va elektron tijorat modellar.'),
 ('MT27','Internet-banking, Internet-savdo, Internet-sug''urta, Internet-marketing.'),
 ('MT28','Mobil tijorat.'),
 ('MT29','Axborot xavfsizligi tushunchasi va zarurati.'),
 ('MT30','Axborotni himoya qilish uchun kriptografik usullardan foydalanish.'),
 ('MT31','Soliq hisobotlarini yuborish.')
) AS v(c,t) ON v.c=l.code AND l.session_type='INDEPENDENT_STUDY';

-- Literature + the QS/THE Top-300 benchmarking table (verbatim).
INSERT INTO course_materials (course_id, material_type, position, citation, url, source_institution, ranking_note, mapped_topic)
SELECT c.id, v.mt, v.p, v.cit, v.url, v.inst, v.rank, v.topic
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code='IAKTT1105',
 (VALUES
  ('PRIMARY_LITERATURE',1,'M.Aripov, B.Begalov, U.Begimqulov, M.Mamarajabov. Axborot Texnologiyalari. Noshir, Toshkent, 2009, 178 b.',NULL,NULL,NULL,NULL),
  ('PRIMARY_LITERATURE',2,'Shoaxmedov N.X., Abdullayeva I.M. Iqtisodiyotda axborot-kommunikatsiya texnologiyalari va tizimlari. Innovatsion rivojlanish nashriyot-matbaa uyi, 2021, 248 b.',NULL,NULL,NULL,NULL),
  ('PRIMARY_LITERATURE',3,'F.E.Jamonqulova, I.E.Shadmanov. Iqtisodiyotda axborot-kommunikatsion texnologiyalar va tizimlar. Iqtisod-moliya, 2022.',NULL,NULL,NULL,NULL),
  ('SUPPLEMENTARY_LITERATURE',4,'Informatsionnye sistemy v ekonomike: uchebnik / pod red. G.A. Titorenko. M.: YuNITI-DANA, 2009.',NULL,NULL,NULL,NULL),
  ('SUPPLEMENTARY_LITERATURE',5,'Florian Heiss. Using R for Introductory Econometrics, 2nd Edition, 2020.',NULL,NULL,NULL,NULL),
  ('SUPPLEMENTARY_LITERATURE',6,'Kodolova I.L. Informatsionnye tekhnologii resheniya ekonomicheskikh zadach. Kazan: Kazanskiy universitet, 2018.',NULL,NULL,NULL,NULL),
  ('LEGAL_DOCUMENT',7,'O''zbekiston Respublikasi Prezidentining 2017 yil 7 fevral PF-4947-son farmoni (Harakatlar strategiyasi).',NULL,NULL,NULL,NULL),
  ('WEB_RESOURCE',8,'Aniq va ijtimoiy fanlar universiteti rasmiy web-sayti','https://www.aifu.uz',NULL,NULL,NULL),
  ('WEB_RESOURCE',9,'Axborot ta''lim portali','https://www.ziyonet.uz',NULL,NULL,NULL),
  ('WEB_RESOURCE',10,'Yagona elektron kutubxona tizimi','https://www.unilibrary.uz',NULL,NULL,NULL),
  ('WEB_RESOURCE',11,'W3Schools','https://www.w3schools.com',NULL,NULL,NULL),
  ('EXTERNAL_COURSE',12,'Introduction to Computational Thinking and Data Science (6.0002)','https://ocw.mit.edu/courses/6-0002-introduction-to-computational-thinking-and-data-science-fall-2016/','Massachusetts Institute of Technology','QS 14 / THE 2','Katta ma''lumotlar va ma''lumotlarni tahlil qilish asoslari'),
  ('EXTERNAL_COURSE',13,'Database Systems (6.5830)','https://ocw.mit.edu/courses/6-5830-database-systems-fall-2023/','Massachusetts Institute of Technology','QS 14 / THE 2','Ma''lumotlar bazalari: munosabatli model, so''rovlarni optimallashtirish, tranzaksiyalar'),
  ('EXTERNAL_COURSE',14,'Computer Security (CS 161)','https://www2.eecs.berkeley.edu/Courses/CS161/','University of California, Berkeley','QS 12 / THE 11','Axborot xavfsizligi va kriptografiya asoslari'),
  ('EXTERNAL_COURSE',15,'Blockchain and CryptoEconomics (CS 294-151)','https://berkeley-blockchain.github.io/cs294-151-f18','University of California, Berkeley','QS 12 / THE 11','Blokcheyn va kriptoiqtisodiyot'),
  ('EXTERNAL_COURSE',16,'Introduction to Computer Networks (CS2105)','https://nusmods.com/courses/CS2105/introduction-to-computer-networks','National University of Singapore','QS 8 / THE 17','Tarmoq texnologiyalari va internet protokollari (HTTP, DNS, TCP/UDP, IP)'),
  ('EXTERNAL_COURSE',17,'E-business (MG209)','https://www.lse.ac.uk/resources/calendar2023-2024/courseGuides/MG/2023_MG209.htm','London School of Economics','QS 56 / THE 46','Elektron biznes, internet-savdo va raqamli strategiya')
 ) AS v(mt,p,cit,url,inst,rank,topic);

INSERT INTO syllabus_revisions (course_id, revision, status, authors, reviewers, source_document_ref, notes)
SELECT c.id, 1, 'APPROVED',
 '[{"name":"Akbarov Baxriddin","title":"katta o''qituvchi","organisation":"AIFU, Aniq fanlar kafedrasi"}]'::jsonb,
 '[{"name":"J.O''. Muxammadiev","title":"O''zMU birinchi prorektori, f-m.f.d. prof. v.b."},
   {"name":"R.A. Djabbarov","title":"O''zMU, Ekonometrika va iqtisodiy modellashtirish kafedrasi katta o''qituvchisi"}]'::jsonb,
 'academic-materials/syllabi/IAKT_iqtisod_1k_2025_sillabus.docx',
 'Protocol numbers/dates are blank in the supplied document (template placeholders "___-son", "___-dagi").'
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='IAKTT1105';

-- ======================= SUBJECT 2: DAS1110 =======================
-- Proof that Subject != Course: ONE subject, TWO courses (semester 1 = 6 credits/180 h,
-- semester 2 = 5 credits/150 h), each with its own topic list.
INSERT INTO subjects (code, department_id, subject_kind, default_instruction_language, status, published_at)
SELECT 'DAS1110', id, 'MANDATORY', 'uz', 'PUBLISHED', now() FROM departments WHERE code='ANIQ_FANLAR';

INSERT INTO subject_translations (subject_id, locale, title, short_title, aim, objectives)
SELECT id, 'uz', 'Dasturlash', 'Dasturlash',
 'Fanni o''qitishdan maqsad – talabalarga dasturlash tillarining fundamental tushunchalari va algoritmlash usullarini, turli sohaga tegishli amaliy masalalarni yechishni, mantiqiy fikrlashni o''rgatish, turli xil dasturlash muhitlarida ilovalar yaratish va ularni amaliyotga tatbiq etish ko''nikmalarini hosil qilishdan iborat. Shuningdek, obyektga yo''naltirilgan dasturlash tamoyillari va dasturlash tillarining takomillashgan imkoniyatlari bilan tanishtirish.',
 'Fanning vazifasi – dasturlashni o''rganayotgan talabalar uchun texnologik innovatsiyalarni qabul qilish, nazariy bilimlar va amaliy ko''nikmalarni shakllantirish, texnik bilimlarga oid masalalarni zamonaviy dasturlash tillari yordamida hal etish va ularning usullarini kasbiy faoliyatga qo''llay olishga erishishdan iborat.'
FROM subjects WHERE code='DAS1110';

INSERT INTO subject_translations (subject_id, locale, title)
SELECT id, 'en', 'Programming' FROM subjects WHERE code='DAS1110';

INSERT INTO subject_programmes (subject_id, programme_id, curriculum_ordinal, recommended_year, recommended_term)
SELECT s.id, p.id, '1.09', 1, 1 FROM subjects s, academic_programmes p
WHERE s.code='DAS1110' AND p.code='60610600'
UNION ALL
SELECT s.id, p.id, NULL, 1, 1 FROM subjects s, academic_programmes p
WHERE s.code='DAS1110' AND p.code='70540101';

-- The only CODED prerequisite in either document: "(ALG112318) Algebra".
-- The Algebra subject itself has no syllabus yet, so it is a code-only placeholder.
INSERT INTO subjects (code, department_id, subject_kind, status)
SELECT 'ALG112318', id, 'MANDATORY', 'DRAFT' FROM departments WHERE code='ANIQ_FANLAR';
INSERT INTO subject_translations (subject_id, locale, title, summary)
SELECT id, 'uz', 'Algebra',
 'Placeholder: referenced as a prerequisite by DAS1110. No syllabus supplied yet (PENDING-INPUTS.md §1).'
FROM subjects WHERE code='ALG112318';

INSERT INTO subject_prerequisites (subject_id, prerequisite_subject_id, requirement_type, free_text_note)
SELECT s.id, p.id, 'REQUIRED', '(ALG112318) Algebra'
FROM subjects s, subjects p WHERE s.code='DAS1110' AND p.code='ALG112318';

INSERT INTO learning_outcomes (subject_id, code, category, display_order)
SELECT s.id, v.c, v.cat, v.ord FROM subjects s,
 (VALUES ('TN1','COMPETENCY',1),('TN2','COMPETENCY',2),('TN3','COMPETENCY',3),
         ('TN4','COMPETENCY',4),('TN5','COMPETENCY',5),
         ('TN6','SKILL',6),('TN7','SKILL',7),('TN8','SKILL',8),('TN9','SKILL',9)) AS v(c,cat,ord)
WHERE s.code='DAS1110';

INSERT INTO learning_outcome_translations (learning_outcome_id, locale, statement)
SELECT lo.id, 'uz', v.st FROM learning_outcomes lo
JOIN subjects s ON s.id=lo.subject_id AND s.code='DAS1110'
JOIN (VALUES
 ('TN1','Axborot, uni saqlash usullari, qayta ishlash va uzatish, hisoblash tizimlarining matematik va dasturiy ta''minoti, ularni fan sohalarida, ishlab chiqarish va ta''limda qo''llash xususiyatlari bo''lishi kerak.'),
 ('TN2','Kompyuterni dasturiy ta''minoti, dastur turlari va xususiyatlari haqida tasavvurga ega bo''lishi kerak.'),
 ('TN3','Yuqori darajadagi dasturlash tillarini, dasturiy ta''minotni, dasturlash texnologiyalaridan foydalanish ko''nikmalariga ega bo''lishi kerak.'),
 ('TN4','Tatbiqiy va hisoblash matematikasi masalalarini yechish algoritmlarini tuzish bo''yicha nazariy bilimlarga ega bo''lishi kerak.'),
 ('TN5','Vektor analiz, maydon nazariyasi va kompleks o''zgaruvchili funksiyalar nazariyasining asosiy tushunchalari, ta''rif va teoremalari haqida bilimga ega bo''lishi kerak.'),
 ('TN6','Fanni o''rganishda talabalar tegishli jarayonlar haqida tasavvurga ega bo''lishlari, ayni paytda ularni mantiqiy fikrlash va to''g''ri xulosalar chiqara olishi kerak.'),
 ('TN7','Obyektga yo''naltirilgan dasturlash konsepsiyalari, inkapsulyatsiya, vorislik, polimorfizm qoidalarini o''rganish va amaliy masalalarni ular yordamida yechish ko''nikmalariga ega bo''lishi kerak.'),
 ('TN8','Tatbiqiy masalalarni yechish algoritmini tuzish, matematik (kompyuter) modelini qurish va uning dasturiy ta''minotini yaratish, chiziqli ro''yxatlar hosil qilish, strukturada berilganlarni jamlash va ko''rsatkichlar bilan ishlash ko''nikmasiga ega bo''lishi kerak.'),
 ('TN9','Olingan bilim va ko''nikmalarni o''z mutaxassisligi doirasidagi muammolarni tahlil qilish va ularga yechim topishda mustaqil va ijodiy qo''llay olishi kerak.')
) AS v(c,st) ON v.c=lo.code;

-- --- Course A: 2026/2027 semester 1. 6 credits, 180 h (30 lecture + 30 practical + 120 independent).
INSERT INTO courses (subject_id, term_id, programme_id, cohort_code, credits, total_hours,
                     weekly_contact_hours, instruction_language, required_locales, delivery_mode,
                     grade_scale_id, status, published_at)
SELECT s.id, t.id, p.id, 'DI-26-01-S1', 6, 180, 4, 'uz', ARRAY['uz'], 'FULL_TIME',
       (SELECT id FROM grade_scales WHERE code='AIFU_100_5'), 'PUBLISHED', now()
FROM subjects s, terms t, academic_years ay, academic_programmes p
WHERE s.code='DAS1110' AND ay.code='2026/2027' AND t.academic_year_id=ay.id AND t.ordinal=1
  AND p.code='60610600';

-- --- Course B: 2026/2027 semester 2. 5 credits, 150 h (30 + 30 + 90).
INSERT INTO courses (subject_id, term_id, programme_id, cohort_code, credits, total_hours,
                     weekly_contact_hours, instruction_language, required_locales, delivery_mode,
                     grade_scale_id, status, published_at)
SELECT s.id, t.id, p.id, 'DI-26-01-S2', 5, 150, 4, 'uz', ARRAY['uz'], 'FULL_TIME',
       (SELECT id FROM grade_scales WHERE code='AIFU_100_5'), 'PUBLISHED', now()
FROM subjects s, terms t, academic_years ay, academic_programmes p
WHERE s.code='DAS1110' AND ay.code='2026/2027' AND t.academic_year_id=ay.id AND t.ordinal=2
  AND p.code='60610600';

INSERT INTO course_translations (course_id, locale, title)
SELECT c.id, 'uz', 'Dasturlash (2026/2027, ' ||
       CASE WHEN c.cohort_code LIKE '%S1' THEN '1-semestr)' ELSE '2-semestr)' END
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='DAS1110';

INSERT INTO course_teachers (course_id, teacher_user_id, teacher_role)
SELECT c.id, u.id, 'LEAD' FROM courses c JOIN subjects s ON s.id=c.subject_id, users u
WHERE s.code='DAS1110' AND u.username='b.akbarov'
UNION ALL
SELECT c.id, u.id, 'ASSISTANT' FROM courses c JOIN subjects s ON s.id=c.subject_id, users u
WHERE s.code='DAS1110' AND u.username='a.zoxidov';

INSERT INTO course_session_allocations (course_id, session_type, allocated_hours)
SELECT c.id, v.st, v.h FROM courses c JOIN subjects s ON s.id=c.subject_id,
 (VALUES ('LECTURE',30),('PRACTICAL',30),('INDEPENDENT_STUDY',120)) AS v(st,h)
WHERE s.code='DAS1110' AND c.cohort_code='DI-26-01-S1'
UNION ALL
SELECT c.id, v.st, v.h FROM courses c JOIN subjects s ON s.id=c.subject_id,
 (VALUES ('LECTURE',30),('PRACTICAL',30),('INDEPENDENT_STUDY',90)) AS v(st,h)
WHERE s.code='DAS1110' AND c.cohort_code='DI-26-01-S2';

-- DAS1110 states grade BANDS but no JN/ON/YaN split. Reusing the institutional 30/30/40
-- structure would be an INVENTION, so only the FINAL component is created and the rest is
-- left for AIFU to confirm (PENDING-INPUTS.md §7).
INSERT INTO assessment_components (course_id, code, kind, max_points, min_pass_percent, blocks_final, display_order)
SELECT c.id, 'YaN', 'FINAL', 100, 60, false, 1
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='DAS1110';

INSERT INTO course_modules (course_id, position, code, status, grouping_source, published_at)
SELECT c.id, v.p, v.code, 'PUBLISHED', 'PLATFORM_INFERRED', now()
FROM courses c JOIN subjects s ON s.id=c.subject_id,
 (VALUES (0,'M-BASICS'),(1,'M-CONTROL-FLOW'),(2,'M-ARRAYS'),(3,'M-FUNCTIONS'),
         (4,'M-MEMORY-STRINGS'),(5,'M-INDEPENDENT')) AS v(p,code)
WHERE s.code='DAS1110' AND c.cohort_code='DI-26-01-S1'
UNION ALL
SELECT c.id, v.p, v.code, 'PUBLISHED', 'PLATFORM_INFERRED', now()
FROM courses c JOIN subjects s ON s.id=c.subject_id,
 (VALUES (0,'M-SORTING'),(1,'M-OOP'),(2,'M-TEMPLATES'),(3,'M-TOOLING'),(4,'M-INDEPENDENT'))
 AS v(p,code)
WHERE s.code='DAS1110' AND c.cohort_code='DI-26-01-S2';

INSERT INTO course_module_translations (module_id, locale, title)
SELECT m.id, 'uz', v.t FROM course_modules m
JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110'
JOIN (VALUES ('M-BASICS','Dasturlash asoslari'),
             ('M-CONTROL-FLOW','Tarmoqlanuvchi va takrorlanuvchi jarayonlar'),
             ('M-ARRAYS','Massivlar'),
             ('M-FUNCTIONS','Funksiyalar va rekursiya'),
             ('M-MEMORY-STRINGS','Dinamik xotira, satrlar va fayllar'),
             ('M-SORTING','Saralash algoritmlari'),
             ('M-OOP','Obyektga yo''naltirilgan dasturlash'),
             ('M-TEMPLATES','Shablonlar va konteynerlar'),
             ('M-TOOLING','Dasturlash muhiti va komponentalar'),
             ('M-INDEPENDENT','Mustaqil ta''lim topshiriqlari')) AS v(c,t) ON v.c=m.code;

-- Semester 1 lectures (verbatim titles and hours; note M6, M9, M10 are 4 h).
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, v.p, v.code, 'LECTURE', v.h, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S1'
JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110'
JOIN (VALUES
 ('M-BASICS',0,'M1',2),('M-BASICS',1,'M2',2),
 ('M-CONTROL-FLOW',0,'M3',2),('M-CONTROL-FLOW',1,'M4',2),
 ('M-ARRAYS',0,'M5',2),('M-ARRAYS',1,'M6',4),
 ('M-FUNCTIONS',0,'M7',2),('M-FUNCTIONS',1,'M8',2),
 ('M-MEMORY-STRINGS',0,'M9',4),('M-MEMORY-STRINGS',1,'M10',4),
 ('M-MEMORY-STRINGS',2,'M11',2),('M-MEMORY-STRINGS',3,'M12',2)
) AS v(mod,p,code,h) ON v.mod=m.code;

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id
JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S1'
JOIN (VALUES
 ('M1','Dasturlashning asosiy tushunchalari.'),
 ('M2','Dasturlash tillarining tuzilmasi.'),
 ('M3','Tarmoqlanuvchi jarayonlarni tashkil etish.'),
 ('M4','Takrorlanuvchi jarayonlarni tashkil etish.'),
 ('M5','Bir o''lchovli massivlar.'),
 ('M6','Ko''p o''lchovli massivlar.'),
 ('M7','Funksiyalar bilan ishlash.'),
 ('M8','Rekursiv funksiyalar bilan ishlash.'),
 ('M9','Dinamik xotira bilan ishlash.'),
 ('M10','Satrlar va kengaytirilgan belgilar.'),
 ('M11','String toifasidagi satrlar.'),
 ('M12','Fayllar bilan ishlash.')
) AS v(c,t) ON v.c=l.code AND l.session_type='LECTURE';

-- Semester 1 practicals (amaliy): verbatim, 30 h.
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, 100 + v.p, v.code, 'PRACTICAL', v.h, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S1'
JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110'
JOIN (VALUES
 ('M-BASICS',0,'A1',4),('M-BASICS',1,'A2',2),('M-BASICS',2,'A3',4),
 ('M-CONTROL-FLOW',0,'A4',2),('M-CONTROL-FLOW',1,'A5',4),('M-CONTROL-FLOW',2,'A6',4),
 ('M-CONTROL-FLOW',3,'A7',2),('M-CONTROL-FLOW',4,'A8',4),
 ('M-ARRAYS',0,'A9',4)
) AS v(mod,p,code,h) ON v.mod=m.code;

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id
JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S1'
JOIN (VALUES
 ('A1','Kompilyator turlari. Identifikator va ularning turlari.'),
 ('A2','Ma''lumot turlari. Kiritish va chiqarish funksiyalari. Operatorlar.'),
 ('A3','Mantiqiy amallar va matematik funksiyalar bilan ishlash.'),
 ('A4','Tarmoqlanuvchi jarayonlarni tashkil etish. Shartli o''tish operatori.'),
 ('A5','Tarmoqlanuvchi jarayonlarni tashkil etish. Tanlash operatori. Ternar operator.'),
 ('A6','Takrorlanuvchi jarayonlarni tashkil etish. Parametrli takrorlash operatori. Old shartli takrorlash operatori.'),
 ('A7','Takrorlanuvchi jarayonlarni tashkil etish. Parametrli takrorlash operatori (davomi).'),
 ('A8','Takrorlanuvchi jarayonlarni tashkil etish. So''ng shartli takrorlanuvchi operatorlar. Ichma-ich takrorlanish jarayonlari.'),
 ('A9','Bir o''lchamli massivlar. Statik massivlar. Elementlarni saralash va qidirish usullari.')
) AS v(c,t) ON v.c=l.code AND l.session_type='PRACTICAL';

-- Semester 1 independent study: 6 topics x 20 h = 120 h. Verbatim.
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, 200 + v.p, v.code, 'INDEPENDENT_STUDY', 20, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S1'
JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110',
 (VALUES (1,'MT1'),(2,'MT2'),(3,'MT3'),(4,'MT4'),(5,'MT5'),(6,'MT6')) AS v(p,code)
WHERE m.code='M-INDEPENDENT';

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id AND m.code='M-INDEPENDENT'
JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S1'
JOIN (VALUES
 ('MT1','Tarmoqlanuvchi jarayonlarni tashkil etish (algoritm va dastur).'),
 ('MT2','Takrorlanuvchi jarayonlarni tashkil etish (algoritm va dastur).'),
 ('MT3','Massivlarni tashkil etish.'),
 ('MT4','Matritsalarni tashkil etish.'),
 ('MT5','Vektorlarni tashkil etish.'),
 ('MT6','Ichma-ich joylashgan siklik jarayonlarni tashkil etish.')
) AS v(c,t) ON v.c=l.code;

-- Semester 2 lectures (verbatim, 30 h; M5 is 6 h).
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, v.p, v.code, 'LECTURE', v.h, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S2'
JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110'
JOIN (VALUES
 ('M-SORTING',0,'M1',2),
 ('M-OOP',0,'M2',4),('M-OOP',1,'M3',4),('M-OOP',2,'M4',4),
 ('M-TEMPLATES',0,'M5',6),('M-TEMPLATES',1,'M6',4),
 ('M-TOOLING',0,'M7',2),('M-TOOLING',1,'M8',4)
) AS v(mod,p,code,h) ON v.mod=m.code;

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id
JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S2'
JOIN (VALUES
 ('M1','Saralash algoritmlari.'),
 ('M2','Obyektga yo''naltirilgan dasturlash asoslari.'),
 ('M3','Obyektlar massivi.'),
 ('M4','Inkapsulyatsiya va merosxo''rlik. Polimorfizm.'),
 ('M5','Shablonlar bilan ishlash.'),
 ('M6','Konteynerlar (kolleksiyalar).'),
 ('M7','Visual Studio muhitida dasturlash.'),
 ('M8','Komponentalar bilan ishlash.')
) AS v(c,t) ON v.c=l.code AND l.session_type='LECTURE';

-- Semester 2 practicals (verbatim, 30 h).
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, 100 + v.p, v.code, 'PRACTICAL', v.h, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S2'
JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110'
JOIN (VALUES
 ('M-SORTING',0,'A1',4),('M-SORTING',1,'A2',6),
 ('M-OOP',0,'A3',4),('M-OOP',1,'A4',4),
 ('M-TEMPLATES',0,'A5',4),
 ('M-TOOLING',0,'A6',2),('M-TOOLING',1,'A7',2),('M-TOOLING',2,'A8',4)
) AS v(mod,p,code,h) ON v.mod=m.code;

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id
JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S2'
JOIN (VALUES
 ('A1','Ko''p o''lchamli massivlar. Statik massivlar. Elementlarni saralash usullari.'),
 ('A2','Elementlarni qidirish usullari. Elementlar ustida amallarni ko''rsatish.'),
 ('A3','Funksiyalar bilan ishlash. Funksiya tavsifi. Qiymat qaytaruvchi funksiya. Funksiya-protsedura. Funksiyalarni qayta yuklash.'),
 ('A4','Rekursiv funksiya. Foydalanuvchi kutubxonasini tashkil etish.'),
 ('A5','Dinamik xotira bilan ishlash. Ko''rsatkichlar. Dinamik bir va ko''p o''lchamli massivlar.'),
 ('A6','Satrlar va kengaytirilgan belgilar. ASCII kodlari.'),
 ('A7','Satrlar bilan ishlovchi funksiyalar.'),
 ('A8','Matnli fayllar haqida umumiy tushuncha. Fayllar va oqimlar.')
) AS v(c,t) ON v.c=l.code AND l.session_type='PRACTICAL';

-- Semester 2 independent study: 7 topics = 90 h (30+10+10+10+10+10+10). Verbatim.
INSERT INTO lessons (module_id, position, code, session_type, allocated_hours, is_required, status, published_at)
SELECT m.id, 200 + v.p, v.code, 'INDEPENDENT_STUDY', v.h, true, 'PUBLISHED', now()
FROM course_modules m JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S2'
JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110',
 (VALUES (1,'MT1',30),(2,'MT2',10),(3,'MT3',10),(4,'MT4',10),(5,'MT5',10),(6,'MT6',10),(7,'MT7',10))
 AS v(p,code,h)
WHERE m.code='M-INDEPENDENT';

INSERT INTO lesson_translations (lesson_id, locale, title)
SELECT l.id, 'uz', v.t FROM lessons l
JOIN course_modules m ON m.id=l.module_id AND m.code='M-INDEPENDENT'
JOIN courses c ON c.id=m.course_id AND c.cohort_code='DI-26-01-S2'
JOIN (VALUES
 ('MT1','Bir o''lchamli massivlarning funksiya parametrlari sifatida qo''llanilishi.'),
 ('MT2','Matritsalarning funksiya parametrlari sifatida qo''llanilishi.'),
 ('MT3','Dinamik massivlar va ko''rsatkichlar.'),
 ('MT4','Rekursiv jarayonlarni tashkil etish.'),
 ('MT5','Ichma-ich joylashgan rekursiv jarayonlarni tashkil etish.'),
 ('MT6','Satrlar bilan ishlash.'),
 ('MT7','O''zaro bog''langan sinflarni tashkil etish.')
) AS v(c,t) ON v.c=l.code;

INSERT INTO course_materials (course_id, material_type, position, citation, url)
SELECT c.id, v.mt, v.p, v.cit, v.url
FROM courses c JOIN subjects s ON s.id=c.subject_id AND s.code='DAS1110',
 (VALUES
  ('PRIMARY_LITERATURE',1,'Mo''minov B.B. Dasturlash 1. Darslik. T.: "Nihol print", 2021, 280 b.',NULL),
  ('PRIMARY_LITERATURE',2,'Mo''minov B.B. Dasturlash 2. Darslik. T.: "Nihol print", 2021, 604 b.',NULL),
  ('PRIMARY_LITERATURE',3,'Nazirov Sh.A., Qobulov R.V., Bobojonov M.R., Raxmanov Q.S. C va C++ tili. T.: "Voris-nashriyot" MChJ, 2013, 488 b.',NULL),
  ('PRIMARY_LITERATURE',4,'Abdullayeva Z.Sh., Ishniyozov O.O. Dasturlash I va Dasturlash II fanidan o''quv qo''llanma, 2022, 141 b.',NULL),
  ('PRIMARY_LITERATURE',5,'Xaidarova M.O. va boshq. "Programmirovanie na C++" fanidan laboratoriya ishlarini bajarish bo''yicha metodik qo''llanma. TUIT, Toshkent, 2017, 145 b.',NULL),
  ('SUPPLEMENTARY_LITERATURE',6,'Horton I. Beginning Visual C++ 2012. John Wiley & Sons, 2012, 988 p.',NULL),
  ('SUPPLEMENTARY_LITERATURE',7,'Ziborov V.V. MS Visual C++ 2010 v srede .NET. M.: Piter, 2012, 320 s.',NULL),
  ('WEB_RESOURCE',8,'Aniq va ijtimoiy fanlar universiteti rasmiy web-sayti','https://www.aifu.uz'),
  ('WEB_RESOURCE',9,'O''zbekiston Respublikasi oliy ta''lim, fan va innovatsiyalar vazirligi','https://www.edu.uz')
 ) AS v(mt,p,cit,url);

INSERT INTO syllabus_revisions (course_id, revision, status, authors, reviewers, source_document_ref, notes)
SELECT c.id, 1, 'APPROVED',
 '[{"name":"B. Akbarov","title":"dotsent v.b.","organisation":"AIFU, Aniq fanlar kafedrasi"},
   {"name":"A. Zoxidov","title":"katta o''qituvchi","organisation":"AIFU, Aniq fanlar kafedrasi"}]'::jsonb,
 '[{"name":"N.A. Ignatev","title":"O''zMU professori, fizika-matematika fanlari doktori"},
   {"name":"Sh.F. Madraximov","title":"O''zMU professori, texnika fanlari doktori"}]'::jsonb,
 'academic-materials/syllabi/1.09. Dasturlash 142_bo''yicha_DI.doc',
 'IMPORTANT: this syllabus is a C/C++ course (Visual Studio, pointers, templates) while the platform executes Python and Go. Unresolved: PENDING-INPUTS.md §2.'
FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code='DAS1110';
