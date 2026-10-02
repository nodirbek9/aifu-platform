-- ============================================================================
-- V900 (DEV ONLY) — Institution, calendar and fake users.
-- Codes/names are verbatim from the supplied syllabi; people are invented except
-- where the syllabus names the actual teacher (public document information).
-- ============================================================================

INSERT INTO departments (code, name, name_en) VALUES
  ('ANIQ_FANLAR', 'Aniq fanlar kafedrasi', 'Department of Exact Sciences');

-- Verbatim programme/area codes from both documents.
INSERT INTO academic_programmes (code, name, knowledge_area_code, education_area_code, degree_level) VALUES
  ('60410100', 'Iqtisodiyot',        '400000', '410000', 'BACHELOR'),
  ('60610600', 'Dasturiy injiniring','600000', '610000', 'BACHELOR'),
  ('70540101', 'Matematika',         '500000', '540000', 'MASTER');

-- Dates are PLACEHOLDERS: no academic calendar was supplied (PENDING-INPUTS.md §4).
INSERT INTO academic_years (code, starts_on, ends_on, is_current, is_placeholder) VALUES
  ('2025/2026', '2025-09-01', '2026-06-30', false, true),
  ('2026/2027', '2026-09-01', '2027-06-30', true,  true);

INSERT INTO terms (academic_year_id, ordinal, name, starts_on, ends_on, is_placeholder)
SELECT id, 1, '1-semestr', starts_on, starts_on + interval '18 weeks', true FROM academic_years WHERE code='2025/2026'
UNION ALL
SELECT id, 2, '2-semestr', starts_on + interval '20 weeks', ends_on, true FROM academic_years WHERE code='2025/2026'
UNION ALL
SELECT id, 1, '1-semestr', starts_on, starts_on + interval '18 weeks', true FROM academic_years WHERE code='2026/2027'
UNION ALL
SELECT id, 2, '2-semestr', starts_on + interval '20 weeks', ends_on, true FROM academic_years WHERE code='2026/2027';

-- ---------------------------------------------------------------------------
-- DEVELOPMENT-ONLY ACCOUNTS. Password for every account below: Dev!Pass123
-- Stored as a BCrypt cost-12 hash (never plaintext). Obviously-fake .local domain.
-- ---------------------------------------------------------------------------
INSERT INTO users (email, username, password_hash, full_name, status, preferred_locale) VALUES
 ('admin@dev.aifu.local','admin',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Dev Administrator','ACTIVE','uz'),
 ('teacher.akbarov@dev.aifu.local','b.akbarov',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Akbarov Baxriddin Xusniddin o''g''li','ACTIVE','uz'),
 ('teacher.zoxidov@dev.aifu.local','a.zoxidov',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Zoxidov Abdulla','ACTIVE','uz'),
 ('student1@dev.aifu.local','student1',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Dev Student One','ACTIVE','uz'),
 ('student2@dev.aifu.local','student2',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Dev Student Two','ACTIVE','ru'),
 ('student3@dev.aifu.local','student3',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Dev Student Three','ACTIVE','en'),
 ('student4@dev.aifu.local','student4',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Dev Student Four','ACTIVE','uz'),
 ('student5@dev.aifu.local','student5',
  '$2b$12$HFgUMOu.0ivWZg8YwXxe9Op/QZ0ZLjCPo3LHdrUkDmdf67gJIbn1u','Dev Student Five','SUSPENDED','uz');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code =
  CASE WHEN u.username = 'admin' THEN 'ADMIN'
       WHEN u.username IN ('b.akbarov','a.zoxidov') THEN 'TEACHER'
       ELSE 'STUDENT' END
WHERE u.email LIKE '%@dev.aifu.local';

-- Office hours verbatim from the IAKTT1105 syllabus ("Qabul kunlari").
INSERT INTO teacher_profiles (user_id, employee_number, department_id, academic_title, office_hours)
SELECT u.id, 'DEV-T-001', d.id, 'katta o''qituvchi / dotsent v.b.', 'Seshanba, Payshanba 14.00-16.00'
FROM users u, departments d WHERE u.username='b.akbarov' AND d.code='ANIQ_FANLAR';
INSERT INTO teacher_profiles (user_id, employee_number, department_id, academic_title)
SELECT u.id, 'DEV-T-002', d.id, 'katta o''qituvchi'
FROM users u, departments d WHERE u.username='a.zoxidov' AND d.code='ANIQ_FANLAR';

INSERT INTO student_profiles (user_id, student_number, programme_id, group_code, enrollment_year, study_form)
SELECT u.id, 'DEV-S-' || lpad(right(u.username, 1), 3, '0'),
       (SELECT id FROM academic_programmes WHERE code = CASE WHEN u.username IN ('student1','student2')
            THEN '60410100' ELSE '60610600' END),
       CASE WHEN u.username IN ('student1','student2') THEN 'IQT-25-01' ELSE 'DI-26-01' END,
       CASE WHEN u.username IN ('student1','student2') THEN 2025 ELSE 2026 END,
       'FULL_TIME'
FROM users u WHERE u.username LIKE 'student%';
