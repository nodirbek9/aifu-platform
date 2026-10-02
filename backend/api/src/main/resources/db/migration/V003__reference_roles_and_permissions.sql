-- ============================================================================
-- V003 — Reference data the CODE depends on: roles, permissions, mappings.
-- This is NOT seed data: the application's authorization model requires these rows.
-- (Academic/demo content lives in db/seed and loads only under the dev profile.)
-- ============================================================================

INSERT INTO roles (code, name, description, is_system) VALUES
 ('ADMIN',   'Administrator', 'Full platform administration', true),
 ('TEACHER', 'Teacher',       'Teaches assigned courses; authors content', true),
 ('STUDENT', 'Student',       'Enrolled learner', true);

INSERT INTO permissions (code, description, domain) VALUES
 ('USER_READ','View user accounts','iam'),
 ('USER_MANAGE','Create, update, deactivate users','iam'),
 ('ROLE_MANAGE','Assign roles and permissions','iam'),
 ('SUBJECT_READ','View subject catalogue','academics'),
 ('SUBJECT_MANAGE','Create and update subjects','academics'),
 ('COURSE_READ','View courses','academics'),
 ('COURSE_MANAGE','Create, update, publish courses','academics'),
 ('COURSE_TEACHER_MANAGE','Assign teachers to courses','academics'),
 ('COURSE_CONTENT_READ','Read course content as an authorized viewer','curriculum'),
 ('COURSE_CONTENT_WRITE','Create and edit modules, lessons, content blocks','curriculum'),
 ('LESSON_READ','Read lessons of an enrolled or taught course','curriculum'),
 ('ACTIVITY_MANAGE','Create and configure activities','curriculum'),
 ('ENROLLMENT_READ','View enrollments','enrollment'),
 ('ENROLLMENT_MANAGE','Enroll, withdraw, suspend students','enrollment'),
 ('ENROLLMENT_SELF_READ','View own enrollments','enrollment'),
 ('PROGRESS_SELF_READ','View own progress','progress'),
 ('PROGRESS_SELF_WRITE','Record own lesson progress','progress'),
 ('PROGRESS_COURSE_READ','View progress of students in a taught course','progress'),
 ('QUIZ_READ','View quizzes available to the caller','assessment'),
 ('QUIZ_MANAGE','Create and edit quizzes and questions','assessment'),
 ('QUIZ_ATTEMPT_CREATE','Start and submit own quiz attempts','assessment'),
 ('QUIZ_ATTEMPT_SELF_READ','View own quiz attempts','assessment'),
 ('QUIZ_ATTEMPT_REVIEW','Review student attempts in a taught course','assessment'),
 ('CODING_TASK_READ','View coding tasks','coding'),
 ('CODING_TASK_MANAGE','Create and edit coding tasks and test cases','coding'),
 ('SUBMISSION_CREATE','Submit own solutions','coding'),
 ('SUBMISSION_SELF_READ','View own submissions','coding'),
 ('SUBMISSION_REVIEW','Review student submissions in a taught course','coding'),
 ('GRADE_SELF_READ','View own grades','grading'),
 ('GRADE_READ','View grades in a taught course','grading'),
 ('GRADE_WRITE','Record and revise grades','grading'),
 ('FEEDBACK_WRITE','Provide feedback to students','grading'),
 ('FILE_UPLOAD','Upload files','files'),
 ('FILE_MANAGE','Manage and delete files','files'),
 ('ANALYTICS_COURSE_READ','View analytics for a taught course','analytics'),
 ('ANALYTICS_PLATFORM_READ','View platform-wide analytics','analytics'),
 ('AUDIT_READ','Read the audit log','audit'),
 ('NOTIFICATION_SELF_READ','Read own notifications','notification'),
 ('ANNOUNCEMENT_WRITE','Publish announcements','notification'),
 ('SYSTEM_CONFIG_MANAGE','Change system configuration','admin');

-- ADMIN: everything (explicit, not a wildcard — ADR-004/brief §22)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'SUBJECT_READ','COURSE_READ','COURSE_MANAGE','COURSE_CONTENT_READ','COURSE_CONTENT_WRITE',
  'LESSON_READ','ACTIVITY_MANAGE','ENROLLMENT_READ','PROGRESS_COURSE_READ','QUIZ_READ',
  'QUIZ_MANAGE','QUIZ_ATTEMPT_REVIEW','CODING_TASK_READ','CODING_TASK_MANAGE',
  'SUBMISSION_REVIEW','GRADE_READ','GRADE_WRITE','FEEDBACK_WRITE','FILE_UPLOAD',
  'ANALYTICS_COURSE_READ','NOTIFICATION_SELF_READ','ANNOUNCEMENT_WRITE'
) WHERE r.code = 'TEACHER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'SUBJECT_READ','COURSE_READ','COURSE_CONTENT_READ','LESSON_READ','ENROLLMENT_SELF_READ',
  'PROGRESS_SELF_READ','PROGRESS_SELF_WRITE','QUIZ_READ','QUIZ_ATTEMPT_CREATE',
  'QUIZ_ATTEMPT_SELF_READ','CODING_TASK_READ','SUBMISSION_CREATE','SUBMISSION_SELF_READ',
  'GRADE_SELF_READ','NOTIFICATION_SELF_READ'
) WHERE r.code = 'STUDENT';

-- NOTE for future roles (brief §7): CURATOR / MODERATOR / ASSISTANT_TEACHER / SUPER_ADMIN
-- are added here as rows + mappings. No Java change is required. See ADR-004.
