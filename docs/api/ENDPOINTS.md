# API endpoints

GENERATED from `docs/api/openapi.json` by `tools/api-docs/endpoints.mjs` — do not edit by hand.
Every path is under `/api/v1`. Types are schema names of the OpenAPI file; `PageResponseX` is a
page of `X` (`content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`).
A `?` marks an optional query parameter. How to use the API (auth, errors, locale, polling) is
in `docs/03-API-GUIDE.md`.

## Admin: academic calendar

Academic years and terms

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/academic-years` | List academic years, most recent first |  |  | 200 AcademicYearView[] |
| POST | `/admin/academic-years` | Create an academic year |  | CreateAcademicYearRequest | 201 AcademicYearView |
| PUT | `/admin/academic-years/{id}/current` | Mark this the current academic year (unmarks any previous one) |  |  | 200 AcademicYearView |
| GET | `/admin/academic-years/{id}/terms` | List terms in an academic year, in ordinal order |  |  | 200 TermView[] |
| POST | `/admin/academic-years/{id}/terms` | Create a term within an academic year |  | CreateTermRequest | 201 TermView |

## Admin: attempt grants

Grant additional quiz attempts

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/attempt-grants` | List attempt grants | courseId?, page?, size?, sort? |  | 200 PageResponseAttemptGrantView |
| POST | `/admin/quizzes/{quizId}/enrollments/{enrollmentId}/attempt-grants` | Grant an additional attempt for a quiz |  | AttemptGrantRequest | 200 AttemptGrantView |

## Admin: audit log

Browse the append-only audit trail

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/audit-log` | Search the audit trail | actorUserId?, action?, entityType?, entityId?, from?, to?, page?, size?, sort? |  | 200 PageResponseAuditLogView |

## Admin: courses

Course delivery administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/courses` | Search the full course catalogue, any status. Admin only. | query?, status?, subjectId?, termId?, locale?, page?, size?, sort? |  | 200 PageResponseCourseSummary |
| POST | `/admin/courses` | Create one delivery of a subject in a term |  | CreateCourseRequest | 201 CourseDetail |
| GET | `/admin/courses/{id}` | Get one course, any status. Caller must be admin or an assigned teacher. |  |  | 200 CourseDetail |
| PATCH | `/admin/courses/{id}` | Update delivery details (cohort, contact hours, mode, form, programme) |  | UpdateCourseDeliveryRequest | 200 CourseDetail |
| DELETE | `/admin/courses/{id}` | Soft-delete a course. Admin only. |  |  | 204 |
| POST | `/admin/courses/{id}/assessment-components` | Add a node to the assessment rubric tree (e.g. JN, then JN_ATTENDANCE under it) |  | CreateAssessmentComponentRequest | 200 CourseDetail |
| PUT | `/admin/courses/{id}/enrollment-window` | Set the enrollment window and capacity |  | UpdateEnrollmentWindowRequest | 200 CourseDetail |
| PUT | `/admin/courses/{id}/required-locales` | Set the locales required before this course may publish (ADR-005) |  | UpdateRequiredLocalesRequest | 200 CourseDetail |
| PUT | `/admin/courses/{id}/session-allocations` | Set the declared hour budget for one session type |  | SetSessionAllocationRequest | 200 CourseDetail |
| PUT | `/admin/courses/{id}/status` | Publish, close, archive or return a course to draft | status |  | 200 CourseDetail |
| POST | `/admin/courses/{id}/teachers` | Assign a teacher to the course. Admin only. |  | AssignTeacherRequest | 200 CourseDetail |
| DELETE | `/admin/courses/{id}/teachers/{teacherUserId}` | Remove a teacher from the course. Admin only. |  |  | 204 |
| PUT | `/admin/courses/{id}/translations/{locale}` | Upsert one locale's course copy |  | UpsertCourseTranslationRequest | 200 CourseDetail |

## Admin: enrollments

Enrollment administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/enrollments` | List enrollments by course or student, optionally filtered by status | courseId?, userId?, status?, page?, size?, sort? |  | 200 |
| POST | `/admin/enrollments` | Enroll one student in a course |  | CreateEnrollmentRequest | 201 EnrollmentDetail |
| GET | `/admin/enrollments/{id}` | Get one enrollment with its full transition history |  |  | 200 EnrollmentDetail |
| PUT | `/admin/enrollments/{id}/status` | Transition an enrollment's status | status, reason? |  | 200 EnrollmentDetail |
| POST | `/admin/enrollments/bulk` | Enroll a batch of students; each student's outcome is reported independently |  | BulkEnrollRequest | 200 BulkEnrollResultView[] |
| POST | `/admin/enrollments/by-group` | Enroll every student of a group into a course |  | GroupEnrollRequest | 200 BulkEnrollResultView[] |
| POST | `/admin/enrollments/import` | Bulk-enroll from a registrar CSV export | courseId, source? | object | 200 CsvImportRowView[] |

## Admin: groups

Student groups ("guruh") and their rosters

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| POST | `/admin/groups` | Create a group |  | GroupRequest | 201 GroupView |
| PUT | `/admin/groups/{id}` | Update a group; renaming keeps its students |  | GroupRequest | 200 GroupView |
| DELETE | `/admin/groups/{id}` | Delete an empty, unused group |  |  | 204 |
| PUT | `/admin/groups/{id}/archived` | Archive or re-activate a group | value |  | 200 GroupView |
| POST | `/admin/groups/{id}/students` | Put students into this group (moves them out of their previous group) |  | GroupStudentsRequest | 200 GroupStudentView[] |
| DELETE | `/admin/groups/{id}/students/{userId}` | Remove a student from this group |  |  | 204 |

## Admin: learning path

Course learning path configuration and gate management

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/courses/{courseId}/learning-path` | Get course learning path configuration |  |  | 200 LearningPathConfigView |
| PUT | `/admin/courses/{courseId}/learning-path` | Configure or enable/disable course learning path |  | LearningPathConfigRequest | 200 LearningPathConfigView |

## Admin: problems

Platform-wide administrative problems

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/problems` | List administrative problems, optionally filtered by type | type?, page?, size?, sort? |  | 200 |

## Admin: roles

Role/permission catalogue (data, not code — ADR-004)

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/permissions` | List the full permission catalogue |  |  | 200 PermissionView[] |
| GET | `/admin/roles` | List every role with the permission codes it grants |  |  | 200 RoleView[] |

## Admin: subjects

Subject catalogue administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/subjects` | Search the full subject catalogue, any status | query?, status?, departmentId?, locale?, page?, size?, sort? |  | 200 PageResponseSubjectSummary |
| POST | `/admin/subjects` | Create a subject |  | CreateSubjectRequest | 201 SubjectDetail |
| GET | `/admin/subjects/{id}` | Get one subject, any status |  |  | 200 SubjectDetail |
| PATCH | `/admin/subjects/{id}` | Update catalogue fields |  | UpdateSubjectRequest | 200 SubjectDetail |
| DELETE | `/admin/subjects/{id}` | Soft-delete a subject |  |  | 204 |
| POST | `/admin/subjects/{id}/learning-outcomes` | Add a learning outcome (TN1, TN2, ...) |  | AddLearningOutcomeRequest | 200 SubjectDetail |
| POST | `/admin/subjects/{id}/prerequisites` | Add a prerequisite (coded subject or free-text note) |  | AddPrerequisiteRequest | 200 SubjectDetail |
| DELETE | `/admin/subjects/{id}/prerequisites/{prerequisiteId}` | Remove a prerequisite |  |  | 204 |
| PUT | `/admin/subjects/{id}/status` | Change publication status |  | ChangeSubjectStatusRequest | 200 SubjectDetail |
| PUT | `/admin/subjects/{id}/translations/{locale}` | Upsert one locale's subject copy |  | UpsertSubjectTranslationRequest | 200 SubjectDetail |
| PUT | `/admin/subjects/learning-outcomes/{outcomeId}/translations/{locale}` | Upsert one locale's statement for a learning outcome |  | UpsertLearningOutcomeTranslationRequest | 204 |

## Admin: timetable

Build, check and publish the timetable; teaching load

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/teaching-load` | The planned teaching load of a term | termId, locale? |  | 200 LoadRow[] |
| PUT | `/admin/teaching-load` | Create or update load lines (one per course, group and session type) | termId | LoadBulkRequest | 200 map |
| DELETE | `/admin/teaching-load/{itemId}` | Delete a load line |  |  | 204 |
| PUT | `/admin/timetable-entries/{entryId}` | Edit or move a class (same body as create; same conflict rules) | locale? | EntryRequest | 200 EntryView |
| DELETE | `/admin/timetable-entries/{entryId}` | Remove a class from the timetable |  |  | 204 |
| GET | `/admin/timetable-entries/{entryId}/exceptions` | One-off changes of a class, by date |  |  | 200 ExceptionView[] |
| POST | `/admin/timetable-entries/{entryId}/exceptions` | Add, cancel, move or replace one occurrence of a class | locale? | ExceptionRequest | 201 ExceptionView |
| DELETE | `/admin/timetable-exceptions/{exceptionId}` | Undo a one-off change |  |  | 204 |
| GET | `/admin/timetables` | Timetables, newest first; optionally of one term | termId? |  | 200 TimetableView[] |
| POST | `/admin/timetables` | Create a draft timetable for a term, optionally as a copy of another one |  | CreateTimetableRequest | 201 TimetableView |
| GET | `/admin/timetables/{id}` | One timetable |  |  | 200 TimetableView |
| DELETE | `/admin/timetables/{id}` | Delete a draft timetable |  |  | 204 |
| POST | `/admin/timetables/{id}/archive` | Archive a timetable (read-only from then on) |  |  | 200 TimetableView |
| GET | `/admin/timetables/{id}/entries` | The builder grid: every recurring class, or those of one group / teacher / room | groupId?, teacherId?, roomId?, locale? |  | 200 EntryView[] |
| POST | `/admin/timetables/{id}/entries` | Add a class | locale? | EntryRequest | 201 EntryView |
| POST | `/admin/timetables/{id}/entries/check` | Dry run: would this class clash, and what warnings does it have? | editedEntryId?, locale? | EntryRequest | 200 CheckResult |
| GET | `/admin/timetables/{id}/load` | Planned hours (yuklama) against the hours this timetable schedules over the term | groupId?, teacherId?, locale? |  | 200 LoadRow[] |
| PUT | `/admin/timetables/{id}/name` | Rename a timetable |  | map | 200 TimetableView |
| POST | `/admin/timetables/{id}/publish` | Publish: students and teachers now see this timetable |  |  | 200 TimetableView |
| GET | `/admin/timetables/{id}/week` | A concrete week of any timetable (preview a draft before publishing) | date?, groupId?, teacherId?, roomId?, locale? |  | 200 WeekView |

## Admin: timetable reference data

Rooms, the bell schedule (pair slots), holidays, odd/even week setting

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/holidays` | Non-teaching days |  |  | 200 HolidayView[] |
| POST | `/admin/holidays` | Mark a date as a holiday: classes on it are shown as not held |  | HolidayRequest | 201 HolidayView |
| DELETE | `/admin/holidays/{id}` | Remove a holiday |  |  | 204 |
| GET | `/admin/pair-slots` | The bell schedule, in pair order |  |  | 200 PairSlotView[] |
| POST | `/admin/pair-slots` | Add a pair to the bell schedule |  | PairSlotRequest | 201 PairSlotView |
| PUT | `/admin/pair-slots/{id}` | Change a pair's number or times |  | PairSlotRequest | 200 PairSlotView |
| DELETE | `/admin/pair-slots/{id}` | Delete a pair no class uses |  |  | 204 |
| GET | `/admin/rooms` | All rooms |  |  | 200 RoomView[] |
| POST | `/admin/rooms` | Create a room |  | RoomRequest | 201 RoomView |
| PUT | `/admin/rooms/{id}` | Update a room |  | RoomRequest | 200 RoomView |
| DELETE | `/admin/rooms/{id}` | Delete a room no class uses |  |  | 204 |
| PUT | `/admin/terms/{termId}/first-week-parity` | Whether the term's first week is odd (toq) or even (juft) |  | TermParityRequest | 200 map |

## Admin: users

Account provisioning and lifecycle administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/admin/users` | Search accounts | query?, status?, role?, page?, size?, sort? |  | 200 PageResponseUserSummary |
| POST | `/admin/users` | Create an account |  | CreateUserRequest | 201 UserDetail |
| GET | `/admin/users/{id}` | Get one account |  |  | 200 UserDetail |
| PATCH | `/admin/users/{id}` | Update profile fields |  | UpdateUserRequest | 200 UserDetail |
| POST | `/admin/users/{id}/reset-password` | Administrative password reset |  | ResetPasswordRequest | 204 |
| PUT | `/admin/users/{id}/roles` | Replace the role set |  | SetRolesRequest | 200 UserDetail |
| PUT | `/admin/users/{id}/status` | Change lifecycle status |  | ChangeStatusRequest | 200 UserDetail |
| POST | `/admin/users/{id}/unlock` | Clear a brute-force lockout |  |  | 204 |
| POST | `/admin/users/import` | Import students with their groups |  | ImportStudentsRequest | 200 ImportStudentResult[] |

## Analytics

Teacher/admin analytics over V015's read models

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses/{courseId}/analytics/at-risk` | Students who are behind in a course (brief §26's at-risk list) | page?, size?, sort? |  | 200 PageResponseStudentAtRiskSummary |
| GET | `/teacher/courses/{courseId}/analytics/coding-tasks` | Per-coding-task difficulty summary for a course |  |  | 200 CodingTaskDifficultySummary[] |
| GET | `/teacher/courses/{courseId}/analytics/completion` | Course-level completion summary |  |  | 200 CourseCompletionSummary |
| GET | `/teacher/courses/{courseId}/analytics/lessons` | Per-lesson completion summary for a course |  |  | 200 LessonCompletionSummary[] |
| GET | `/teacher/courses/{courseId}/analytics/quizzes` | Per-quiz performance summary for a course |  |  | 200 QuizPerformanceSummary[] |
| GET | `/teacher/quizzes/{quizId}/analytics/questions` | Item analysis for every question in one quiz |  |  | 200 QuestionDifficultySummary[] |

## Announcements

Messages to the students of a course or a group

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/announcements/me` | The announcements meant for me, newest first | locale?, page?, size?, sort? |  | 200 PageResponseAnnouncementView |
| GET | `/courses/{courseId}/announcements` | The announcements of one course, newest first | locale?, page?, size?, sort? |  | 200 PageResponseAnnouncementView |
| POST | `/teacher/announcements` | Post an announcement to a course or a group | locale? | CreateAnnouncementRequest | 201 AnnouncementView |
| DELETE | `/teacher/announcements/{id}` | Delete an announcement (its author, or the study office) |  |  | 204 |

## Attendance

My attendance; the topic of a class

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/attendance/me` | My attendance, one block per course | courseId?, locale? |  | 200 MyCourseAttendance[] |
| GET | `/class-sessions/{entryId}/{date}/topic` | The topic of a class: the lesson its teacher linked, if any | locale? |  | 200 ClassTopicView |

## Authentication

Login, token refresh, logout, password change

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| POST | `/auth/login` | Log in |  | LoginRequest | 200 TokenResponse |
| POST | `/auth/logout` | Log out of this session |  | LogoutRequest | 204 |
| POST | `/auth/logout-all` | Log out everywhere |  |  | 204 |
| GET | `/auth/me` | Get the current account |  |  | 200 CurrentUserResponse |
| PATCH | `/auth/me` | Change my own interface language or phone |  | UpdateMyProfileRequest | 200 CurrentUserResponse |
| PUT | `/auth/password` | Change own password |  | ChangePasswordRequest | 204 |
| POST | `/auth/refresh` | Refresh the token pair |  | RefreshRequest | 200 TokenResponse |

## Code runs

Run code without grading: custom input or the examples

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| POST | `/runs` | Queue a run |  | RunRequest | 202 RunView |
| GET | `/runs/{id}` | The caller's own run and, once finished, its output or test results |  |  | 200 RunView |

## Coding tasks

A course's published coding task list

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/coding-tasks/{codingTaskId}/hints` | The task's hints as the caller has opened them | locale? |  | 200 StudentHintView[] |
| POST | `/coding-tasks/{codingTaskId}/hints/{hintId}/reveal` | Open the next hint | locale? |  | 200 StudentHintView[] |
| GET | `/courses/{courseId}/coding-tasks` | List a course's published coding tasks | locale?, page?, size?, sort? |  | 200 PageResponseCodingTaskSummary |
| GET | `/courses/{courseId}/coding-tasks/{id}` | Get one published coding task, student-safe (no solution code, hidden tests redacted) | locale? |  | 200 CodingTaskStudentDetail |
| GET | `/programming-languages` | The enabled programming languages (Python, Java, C++) |  |  | 200 ProgrammingLanguageView[] |

## Courses

Public course catalogue

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/courses` | Browse the published course catalogue | query?, subjectId?, termId?, locale?, page?, size?, sort? |  | 200 PageResponseCourseSummary |
| GET | `/courses/{id}` | Get one published course with its rubric and session allocations |  |  | 200 CourseDetail |

## Files

Upload, download, delete file metadata

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| POST | `/files` | Upload a file | visibility? | multipart | 201 FileDetail |
| GET | `/files/{id}` | Get file metadata |  |  | 200 FileDetail |
| DELETE | `/files/{id}` | Soft-delete a file's metadata (the underlying object is left in place) |  |  | 204 |
| GET | `/files/{id}/content` | Download the file's raw content |  |  | 200 string |

## Groups

Student groups and rosters (staff)

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/groups` | All groups with their student counts |  |  | 200 GroupView[] |
| GET | `/groups/{id}` | One group |  |  | 200 GroupView |
| GET | `/groups/{id}/students` | The group's roster, by name |  |  | 200 GroupStudentView[] |

## Lesson path

Learning path state of a lesson

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/lessons/{lessonId}/path` | The learning path state of a lesson for the caller |  |  | 200 LessonPathView |

## Lesson reading

Reading heartbeat tracking for the learning path

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| POST | `/lessons/{lessonId}/reading` | Record a reading heartbeat on a lesson |  | ReadingBeatRequest | 200 LessonPathView |

## Lessons

A module's published lessons, and one lesson's full content

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/lessons/{lessonId}` | Get one published lesson with all its content blocks, resolved for one locale | locale? |  | 200 LessonDetail |
| GET | `/modules/{moduleId}/lessons` | List a module's published lessons, in order | locale?, page?, size?, sort? |  | 200 PageResponseLessonSummary |

## Modules

A course's published module list

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/courses/{courseId}/modules` | List a course's published modules, in order | locale?, page?, size?, sort? |  | 200 PageResponseModuleSummary |

## My enrollments

The caller's own enrollment history

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/enrollments/me` | List the caller's own enrollments, any status, any term | page?, size?, sort? |  | 200 PageResponseEnrollmentSummary |

## My feedback

Teacher feedback on the caller's own work

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/feedback/me` | Feedback on one of my submissions or quiz attempts | targetType, targetId |  | 200 MyFeedbackView[] |

## My grades

The caller's own enrollment grade summary

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/enrollments/{id}/grades` | My grades in one course: the assessment scheme with earned points, the entries that count, the history, and the derived final score | locale? |  | 200 EnrollmentGradeSummary |

## My progress

Course plan with lesson statuses; my quizzes and tasks

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/activities/me` | My quizzes, coding tasks and other activities across my courses | courseId?, state?, locale? |  | 200 MyActivityView[] |
| GET | `/courses/{courseId}/plan/me` | The course's modules and lessons with my status on each, in one call | locale? |  | 200 CoursePlanView |
| GET | `/courses/{courseId}/progress/me` | The caller's own progress in a course, with a per-module breakdown |  |  | 200 CourseProgressView |
| POST | `/lessons/{lessonId}/progress` | Record a lesson view or completion for the caller's own active enrollment |  | RecordLessonProgressRequest | 200 LessonProgressView |

## My quiz attempts

Starting, answering, and submitting the caller's own attempts

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/attempts/{attemptId}` | Get the caller's own attempt, with served questions and responses | locale? |  | 200 AttemptDetail |
| POST | `/attempts/{attemptId}/questions/{questionId}/answer` | Record (or overwrite) the caller's response to one served question |  | RecordAnswerRequest | 200 ServedQuestionView |
| POST | `/attempts/{attemptId}/submit` | Submit an attempt for grading. Idempotency-Key honoured (CLAUDE.md §9). |  |  | 200 AttemptDetail |
| POST | `/quizzes/{quizId}/attempts` | Start a new attempt for a published quiz the caller is enrolled to take |  | StartAttemptRequest | 201 AttemptDetail |
| GET | `/quizzes/{quizId}/attempts/me` | List the caller's own attempts at one quiz, most recent first | page?, size?, sort? |  | 200 PageResponseAttemptSummary |
| GET | `/quizzes/{quizId}/attempts/me/allowance` | How many attempts the caller has used and may still use at one quiz |  |  | 200 AttemptAllowanceView |

## My submissions

Creating and reading the caller's own coding submissions

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/coding-tasks/{codingTaskId}/submissions/me` | List the caller's own submissions to one coding task, most recent attempt first | page?, size?, sort? |  | 200 PageResponseSubmissionSummary |
| POST | `/submissions` | Submit a solution for grading. Idempotency-Key honoured (CLAUDE.md §9). |  | CreateSubmissionRequest | 201 SubmissionDetail |
| GET | `/submissions/{id}` | Get the caller's own submission, with per-test results (hidden tests redacted) |  |  | 200 SubmissionDetail |

## Notifications

The caller's own in-app notifications

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| POST | `/notifications/{id}/read` | Mark one of the caller's own notifications read |  |  | 200 NotificationView |
| GET | `/notifications/me` | List the caller's own notifications, newest first | unreadOnly?, page?, size?, sort? |  | 200 PageResponseNotificationView |
| POST | `/notifications/me/read-all` | Mark every unread notification of mine as read |  |  | 200 UnreadCountView |
| GET | `/notifications/me/unread-count` | Unread notification count for the caller, for a badge/counter |  |  | 200 UnreadCountView |

## Quizzes

A course's published quiz list

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/courses/{courseId}/quizzes` | List a course's published quizzes | locale?, page?, size?, sort? |  | 200 PageResponseQuizSummary |
| GET | `/courses/{courseId}/quizzes/{id}` | Get one published quiz | locale? |  | 200 QuizDetail |

## Reference data

Departments and academic programmes

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/reference/departments` | Departments (kafedralar), by name |  |  | 200 DepartmentView[] |
| GET | `/reference/programmes` | Academic programmes (yo'nalishlar), by code |  |  | 200 ProgrammeView[] |

## Search

Search over my courses and their lessons

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/search` | Search my courses and their lessons | q, locale?, limit? |  | 200 SearchResultView[] |

## Subjects

Public subject catalogue

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/subjects` | Browse the published subject catalogue | query?, departmentId?, locale?, page?, size?, sort? |  | 200 PageResponseSubjectSummary |
| GET | `/subjects/{id}` | Get one published subject with outcomes and prerequisites |  |  | 200 SubjectDetail |

## Teacher: activities

Activity administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/activities/{id}` | Get one activity, any status |  |  | 200 ActivityDetail |
| PATCH | `/teacher/activities/{id}` | Update activity fields |  | UpdateActivityRequest | 200 ActivityDetail |
| PUT | `/teacher/activities/{id}/status` | Publish, archive or return an activity to draft |  | ChangeActivityStatusRequest | 200 ActivityDetail |
| PUT | `/teacher/activities/{id}/translations/{locale}` | Upsert one locale's activity copy |  | UpsertActivityTranslationRequest | 200 ActivityDetail |
| PUT | `/teacher/activities/{id}/variants/{variantNo}` | Upsert one variant (DAS1110: "mustaqil ish topshiriqlari variativ tavsifga ega bo'lishi lozim") |  | UpsertActivityVariantRequest | 200 ActivityDetail |
| GET | `/teacher/courses/{courseId}/activities` | List a course's activities, any status | page?, size?, sort? |  | 200 PageResponseActivityDetail |
| POST | `/teacher/courses/{courseId}/activities` | Create an activity, placed under a module or a lesson |  | CreateActivityRequest | 201 ActivityDetail |

## Teacher: class sessions and attendance

Class page, lesson of the class, attendance, journal, thematic plan

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/class-sessions/{entryId}/{date}` | The class page: class details, linked lesson, notes, roster with marks | locale? |  | 200 ClassSessionView |
| PUT | `/teacher/class-sessions/{entryId}/{date}` | Link the lesson taught, set a free-text topic and private notes | locale? | SessionDetailsRequest | 200 ClassSessionView |
| PUT | `/teacher/class-sessions/{entryId}/{date}/attendance` | Mark attendance | locale? | AttendanceRequest | 200 ClassSessionView |
| GET | `/teacher/courses/{courseId}/attendance` | Attendance journal: the group's students by surname x every class held in the period, with the marks where attendance was taken | groupId?, from?, to?, locale? |  | 200 JournalView |
| GET | `/teacher/courses/{courseId}/class-plan` | Calendar-thematic plan: every class of the term with its topic | groupId?, locale? |  | 200 PlannedClassView[] |
| GET | `/teacher/journal/groups` | The groups I teach, each with my courses that meet it | locale? |  | 200 JournalGroupChoice[] |

## Teacher: coding task content

Languages, hints, and test cases

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| DELETE | `/teacher/coding-task-hints/{hintId}` | Remove a hint |  |  | 204 |
| PUT | `/teacher/coding-task-hints/{hintId}/translations/{locale}` | Upsert one locale's hint translation |  | UpsertHintTranslationRequest | 200 HintView |
| PATCH | `/teacher/coding-task-languages/{taskLanguageId}` | Update a language's starter/solution code |  | UpsertLanguageRequest | 200 LanguageTeacherView |
| DELETE | `/teacher/coding-task-languages/{taskLanguageId}` | Detach a language from a coding task |  |  | 204 |
| PATCH | `/teacher/coding-task-test-cases/{testCaseId}` | Update a test case |  | UpdateTestCaseRequest | 200 TestCaseView |
| DELETE | `/teacher/coding-task-test-cases/{testCaseId}` | Remove a test case |  |  | 204 |
| GET | `/teacher/coding-tasks/{id}/hints` | List a coding task's hints |  |  | 200 HintView[] |
| POST | `/teacher/coding-tasks/{id}/hints` | Add a hint to a coding task |  | CreateHintRequest | 201 HintView |
| GET | `/teacher/coding-tasks/{id}/languages` | List a coding task's languages, with solution code |  |  | 200 LanguageTeacherView[] |
| POST | `/teacher/coding-tasks/{id}/languages/{languageCode}` | Attach an enabled language to a coding task |  | UpsertLanguageRequest | 201 LanguageTeacherView |
| GET | `/teacher/coding-tasks/{id}/test-cases` | List a coding task's test cases, including hidden ones and expected output |  |  | 200 TestCaseView[] |
| POST | `/teacher/coding-tasks/{id}/test-cases` | Add a test case to a coding task |  | CreateTestCaseRequest | 201 TestCaseView |

## Teacher: coding tasks

Coding task administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/coding-tasks/{id}` | Get one coding task, any status, with solution code and hidden tests |  |  | 200 CodingTaskDetail |
| PATCH | `/teacher/coding-tasks/{id}` | Update a coding task's settings |  | UpdateCodingTaskRequest | 200 CodingTaskDetail |
| DELETE | `/teacher/coding-tasks/{id}` | Archive a coding task (real delete is never exposed — submission history references it) |  |  | 204 |
| PUT | `/teacher/coding-tasks/{id}/status` | Publish, archive, or return a coding task to draft |  | ChangeCodingTaskStatusRequest | 200 CodingTaskDetail |
| PUT | `/teacher/coding-tasks/{id}/translations/{locale}` | Upsert one locale's coding task translation |  | UpsertCodingTaskTranslationRequest | 200 CodingTaskDetail |
| GET | `/teacher/courses/{courseId}/coding-tasks` | List all of a course's coding tasks, any status | page?, size?, sort? |  | 200 PageResponseCodingTaskDetail |
| POST | `/teacher/courses/{courseId}/coding-tasks` | Create a coding task |  | CreateCodingTaskRequest | 201 CodingTaskDetail |

## Teacher: content blocks

Content block administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| PATCH | `/teacher/blocks/{id}` | Update a content block's settings/file/visibility |  | UpdateContentBlockRequest | 200 ContentBlockView |
| DELETE | `/teacher/blocks/{id}` | Soft-delete a content block |  |  | 204 |
| PUT | `/teacher/blocks/{id}/translations/{locale}` | Upsert one locale's block content. Payload shape depends on the block's type. |  | UpsertContentBlockTranslationRequest | 200 ContentBlockView |
| GET | `/teacher/lessons/{lessonId}/blocks` | List a lesson's content blocks | page?, size?, sort? |  | 200 PageResponseContentBlockView |
| POST | `/teacher/lessons/{lessonId}/blocks` | Create a content block within a lesson |  | CreateContentBlockRequest | 201 ContentBlockView |
| PUT | `/teacher/lessons/{lessonId}/blocks/reorder` | Reorder a lesson's content blocks |  | ReorderContentBlocksRequest | 204 |

## Teacher: course progress

A teacher's own course progress roster

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses/{courseId}/progress` | List course-level progress for every student in a course the caller teaches (or any course, for an admin) | page?, size?, sort? |  | 200 PageResponseStudentCourseProgressSummary |

## Teacher: course roster

A teacher's own course enrollments

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses/{courseId}/students` | List the roster of a course the caller teaches (or any course, for an admin) | status?, page?, size?, sort? |  | 200 PageResponseEnrollmentSummary |

## Teacher: courses

The caller's own assigned courses

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses` | List courses the caller teaches, any status | locale?, page?, size?, sort? |  | 200 PageResponseCourseSummary |
| GET | `/teacher/courses/{id}` | Get one of the caller's courses in full detail |  |  | 200 CourseDetail |

## Teacher: feedback

Feedback on a submission, attempt, grade, activity, or enrollment

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/feedback` | List every feedback entry for one target, including INTERNAL notes | targetType, targetId |  | 200 FeedbackView[] |
| POST | `/teacher/feedback` | Write feedback on a submission, attempt, grade, activity, or enrollment |  | CreateFeedbackRequest | 201 FeedbackView |
| PATCH | `/teacher/feedback/{id}` | Edit previously written feedback |  | UpdateFeedbackRequest | 200 FeedbackView |

## Teacher: grades

Manual entry, override, and the course grade roster

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses/{courseId}/gradebook` | The gradebook of a course: every student with every component, alphabetical | locale? |  | 200 CourseGradebookRow[] |
| GET | `/teacher/courses/{courseId}/grades` | Current grade summary for every enrolled student in this course | page?, size?, sort? |  | 200 PageResponseStudentGradeSummary |
| GET | `/teacher/enrollments/{id}/grades` | One student's full current grade summary, teacher view |  |  | 200 EnrollmentGradeSummary |
| POST | `/teacher/enrollments/{id}/grades` | Record or override a manual grade for one assessment component |  | RecordManualGradeRequest | 201 GradeEntryView |
| POST | `/teacher/grades/{id}/void` | Void a published grade entry (append-only — never deleted, ADR-011) |  | VoidGradeRequest | 204 |

## Teacher: lessons

Lesson administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/lessons/{id}` | Get one lesson, any status, with its content blocks; block content in `locale` (default uz) | locale? |  | 200 LessonDetail |
| PATCH | `/teacher/lessons/{id}` | Update lesson fields |  | UpdateLessonRequest | 200 LessonDetail |
| DELETE | `/teacher/lessons/{id}` | Soft-delete a lesson |  |  | 204 |
| POST | `/teacher/lessons/{id}/outcomes/{outcomeId}` | Link a learning outcome this lesson advances |  |  | 204 |
| DELETE | `/teacher/lessons/{id}/outcomes/{outcomeId}` | Unlink a learning outcome |  |  | 204 |
| POST | `/teacher/lessons/{id}/prerequisites/{requiredLessonId}` | Add a lesson prerequisite |  |  | 204 |
| DELETE | `/teacher/lessons/{id}/prerequisites/{requiredLessonId}` | Remove a lesson prerequisite |  |  | 204 |
| PUT | `/teacher/lessons/{id}/status` | Publish, archive or return a lesson to draft |  | ChangeLessonStatusRequest | 200 LessonDetail |
| PUT | `/teacher/lessons/{id}/translations/{locale}` | Upsert one locale's lesson copy |  | UpsertLessonTranslationRequest | 200 LessonDetail |
| GET | `/teacher/modules/{moduleId}/lessons` | List a module's lessons, any status | page?, size?, sort? |  | 200 PageResponseLessonDetail |
| POST | `/teacher/modules/{moduleId}/lessons` | Create a lesson within a module |  | CreateLessonRequest | 201 LessonDetail |
| PUT | `/teacher/modules/{moduleId}/lessons/reorder` | Reorder a module's lessons |  | ReorderLessonsRequest | 204 |

## Teacher: modules

Course module administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses/{courseId}/modules` | List a course's modules, any status | page?, size?, sort? |  | 200 PageResponseModuleDetail |
| POST | `/teacher/courses/{courseId}/modules` | Create a module within a course |  | CreateModuleRequest | 201 ModuleDetail |
| PUT | `/teacher/courses/{courseId}/modules/reorder` | Reorder a course's modules |  | ReorderModulesRequest | 204 |
| GET | `/teacher/modules/{id}` | Get one module, any status |  |  | 200 ModuleDetail |
| PATCH | `/teacher/modules/{id}` | Update module fields |  | UpdateModuleRequest | 200 ModuleDetail |
| DELETE | `/teacher/modules/{id}` | Soft-delete a module |  |  | 204 |
| PUT | `/teacher/modules/{id}/status` | Publish, archive or return a module to draft |  | ChangeModuleStatusRequest | 200 ModuleDetail |
| PUT | `/teacher/modules/{id}/translations/{locale}` | Upsert one locale's module copy |  | UpsertModuleTranslationRequest | 200 ModuleDetail |

## Teacher: questions

Question administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| DELETE | `/teacher/answer-keys/{keyId}` | Remove an answer key |  |  | 204 |
| PATCH | `/teacher/options/{optionId}` | Update an answer option's correctness/matchKey |  | UpdateAnswerOptionRequest | 200 AnswerOptionView |
| DELETE | `/teacher/options/{optionId}` | Remove an answer option |  |  | 204 |
| PUT | `/teacher/options/{optionId}/translations/{locale}` | Upsert one locale's answer option translation |  | UpsertAnswerOptionTranslationRequest | 200 AnswerOptionView |
| GET | `/teacher/questions/{id}` | Get one question, including correct answers |  |  | 200 QuestionDetail |
| PATCH | `/teacher/questions/{id}` | Update a question's points/difficulty/settings |  | UpdateQuestionRequest | 200 QuestionDetail |
| DELETE | `/teacher/questions/{id}` | Delete a question (blocked if any attempt already answered it) |  |  | 204 |
| POST | `/teacher/questions/{id}/answer-keys` | Add an answer key (SHORT_ANSWER/NUMERIC) |  | CreateAnswerKeyRequest | 201 AnswerKeyView |
| POST | `/teacher/questions/{id}/options` | Add an answer option |  | CreateAnswerOptionRequest | 201 AnswerOptionView |
| PUT | `/teacher/questions/{id}/options/reorder` | Reorder a question's answer options |  | ReorderAnswerOptionsRequest | 204 |
| PUT | `/teacher/questions/{id}/translations/{locale}` | Upsert one locale's question translation |  | UpsertQuestionTranslationRequest | 200 QuestionDetail |
| GET | `/teacher/quizzes/{quizId}/questions` | List a quiz's questions | page?, size?, sort? |  | 200 PageResponseQuestionDetail |
| POST | `/teacher/quizzes/{quizId}/questions` | Add a question to a quiz |  | CreateQuestionRequest | 201 QuestionDetail |
| PUT | `/teacher/quizzes/{quizId}/questions/reorder` | Reorder a quiz's questions |  | ReorderQuestionsRequest | 204 |

## Teacher: quiz attempts

A teacher's own quiz's attempt roster

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/quizzes/{quizId}/attempts` | List every attempt at a quiz the caller teaches (or any quiz, for an admin) | page?, size?, sort? |  | 200 PageResponseStudentAttemptSummary |

## Teacher: quizzes

Quiz administration

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/courses/{courseId}/quizzes` | List all of a course's quizzes, any status | page?, size?, sort? |  | 200 PageResponseQuizDetail |
| POST | `/teacher/courses/{courseId}/quizzes` | Create a quiz |  | CreateQuizRequest | 201 QuizDetail |
| GET | `/teacher/quizzes/{id}` | Get one quiz, any status |  |  | 200 QuizDetail |
| PATCH | `/teacher/quizzes/{id}` | Update quiz settings |  | UpdateQuizRequest | 200 QuizDetail |
| DELETE | `/teacher/quizzes/{id}` | Archive a quiz (real delete is never exposed — attempt history references it) |  |  | 204 |
| PUT | `/teacher/quizzes/{id}/status` | Publish, archive, or return a quiz to draft |  | ChangeQuizStatusRequest | 200 QuizDetail |
| PUT | `/teacher/quizzes/{id}/translations/{locale}` | Upsert one locale's quiz translation |  | UpsertQuizTranslationRequest | 200 QuizDetail |

## Teacher: submissions

A teacher's own coding task's submission roster

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/coding-tasks/{codingTaskId}/submissions` | List every submission to a coding task the caller teaches (or any task, for an admin) | page?, size?, sort? |  | 200 PageResponseStudentSubmissionSummary |
| GET | `/teacher/coding-tasks/{codingTaskId}/submissions/{id}` | Get one submission with full per-test results, including hidden test output |  |  | 200 SubmissionDetail |
| GET | `/teacher/courses/{courseId}/submissions` | The review queue of a course: submissions to every coding task, newest first | taskId?, verdict?, groupCode?, needsFeedback?, latestOnly?, page?, size?, locale? |  | 200 PageResponseReviewQueueItem |

## Teacher: verify coding task solutions

Check the reference solutions against the task's tests

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/teacher/coding-tasks/{codingTaskId}/verification` | The latest verification of each language's solution |  |  | 200 VerificationView |
| POST | `/teacher/coding-tasks/{codingTaskId}/verification` | Run every reference solution against all tests |  |  | 202 VerificationView |

## Timetable

The published timetable: my week, today, by group / teacher / room

| Method | Path | What it does | Query | Body | Returns |
|---|---|---|---|---|---|
| GET | `/timetable/directory` | Groups and teachers that have classes, to look up anybody's week | date? |  | 200 TimetableDirectory |
| GET | `/timetable/me` | My week | date?, as?, locale? |  | 200 WeekView |
| GET | `/timetable/next` | My next class that will actually be held | as?, locale? |  | 200 ClassView |
| GET | `/timetable/pair-slots` | The bell schedule |  |  | 200 PairSlotView[] |
| GET | `/timetable/rooms` | Rooms (to look up a room's week) |  |  | 200 RoomView[] |
| GET | `/timetable/today` | My classes today (campus date, Asia/Tashkent) | as?, locale? |  | 200 DayView |
| GET | `/timetable/week` | A week of one group, teacher or room (exactly one of the three ids) | date?, groupId?, teacherId?, roomId?, locale? |  | 200 WeekView |
