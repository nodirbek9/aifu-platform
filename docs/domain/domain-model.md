# Domain model

Derived from the two supplied syllabi (`../academic-analysis/syllabus-analysis.md`).
Locale-free structure + `*_translations` siblings (ADR-005); translation tables are omitted
from the diagrams for readability.

## 1. Institution & catalogue

```mermaid
erDiagram
  DEPARTMENT ||--o{ SUBJECT : owns
  DEPARTMENT ||--o{ TEACHER_PROFILE : employs
  ACADEMIC_PROGRAMME ||--o{ SUBJECT_PROGRAMME : includes
  SUBJECT ||--o{ SUBJECT_PROGRAMME : "offered to"
  SUBJECT ||--o{ SUBJECT_PREREQUISITE : requires
  SUBJECT ||--o{ LEARNING_OUTCOME : declares
  ACADEMIC_YEAR ||--o{ TERM : contains
  SUBJECT ||--o{ COURSE : "delivered as"
  TERM ||--o{ COURSE : schedules
  COURSE ||--o{ COURSE_TEACHER : "taught by"
  COURSE ||--o{ COURSE_SESSION_ALLOCATION : "budgets hours"
  COURSE ||--o{ ASSESSMENT_COMPONENT : "graded by"
  COURSE ||--o{ COURSE_MATERIAL : references
  COURSE ||--o{ SYLLABUS_REVISION : "approved via"
```

**`Subject` vs `Course` is the load-bearing distinction.** `Subject` = the stable catalogue
entry (`DAS1110`), owned by a department, attached to programmes, carrying learning outcomes
and prerequisites. `Course` = **one delivery in one term** with its own credits, hour budget,
language, teachers and rubric. Proof it is necessary: `DAS1110` runs across two semesters with
6 and 5 credits and two different topic lists.

## 2. Content tree

```mermaid
erDiagram
  COURSE ||--o{ COURSE_MODULE : contains
  COURSE_MODULE ||--o{ LESSON : contains
  LESSON ||--o{ CONTENT_BLOCK : "composed of"
  LESSON ||--o{ LESSON_LEARNING_OUTCOME : "maps to"
  LESSON ||--o{ LESSON_PREREQUISITE : "unlocked by"
  LESSON ||--o{ ACTIVITY : "asks student to"
  COURSE_MODULE ||--o{ ACTIVITY : "module-level"
  ACTIVITY }o--|| ASSESSMENT_COMPONENT : "scores into"
  ACTIVITY ||--o{ ACTIVITY_VARIANT : "varies by"
  ACTIVITY }o--o| QUIZ : references
  ACTIVITY }o--o| CODING_TASK : references
  CONTENT_BLOCK }o--o| FILE : embeds
```

`lessons.session_type` ∈ `LECTURE` | `SEMINAR` | `PRACTICAL` | `LABORATORY` |
`INDEPENDENT_STUDY` — straight from the institutional form. The same topic legitimately
appears in two tracks (e.g. `IAKTT1105` S7 and MT9 are both Big Data), so lessons are not
deduplicated by title.

`content_blocks.block_type` ∈ `TEXT` `IMAGE` `VIDEO` `CODE_EXAMPLE` `CALLOUT` `DIAGRAM`
`TABLE` `CASE_STUDY` `FILE` `LINK` `EMBED` `QUIZ_REF` `CODING_TASK_REF` `ACTIVITY_REF`.
Payload is validated per type (`ContentBlockPayloadValidator`).

`activities.activity_type` ∈ `QUIZ` `CODING_TASK` `MANUAL_TASK` `CASE_STUDY` `PRESENTATION`
`GROUP_PROJECT` `FILE_SUBMISSION`. **This is the multi-subject extension point.** Cybersecurity
case studies, Networks configuration exercises and AI practicals are new types + handlers, not
new schemas. No code anywhere branches on a subject code.

## 3. Enrolment, progress, assessment, grading

```mermaid
erDiagram
  USER ||--o| STUDENT_PROFILE : has
  USER ||--o| TEACHER_PROFILE : has
  USER ||--o{ USER_ROLE : has
  ROLE ||--o{ ROLE_PERMISSION : grants
  USER ||--o{ ENROLLMENT : holds
  COURSE ||--o{ ENROLLMENT : accepts
  ENROLLMENT ||--o{ ENROLLMENT_EVENT : "history of"
  ENROLLMENT ||--|| COURSE_PROGRESS : summarizes
  ENROLLMENT ||--o{ MODULE_PROGRESS : tracks
  ENROLLMENT ||--o{ LESSON_PROGRESS : tracks
  ENROLLMENT ||--o{ ACTIVITY_PROGRESS : tracks
  ENROLLMENT ||--o{ QUIZ_ATTEMPT : makes
  ENROLLMENT ||--o{ SUBMISSION : makes
  QUIZ ||--o{ QUESTION : contains
  QUESTION ||--o{ ANSWER_OPTION : offers
  QUIZ_ATTEMPT ||--o{ QUIZ_ATTEMPT_QUESTION : "snapshot of"
  QUIZ_ATTEMPT_QUESTION ||--o{ QUIZ_ATTEMPT_SELECTED_OPTION : answers
  CODING_TASK ||--o{ CODING_TASK_TEST_CASE : "verified by"
  CODING_TASK ||--o{ CODING_TASK_LANGUAGE : "solvable in"
  SUBMISSION ||--|| EXECUTION_JOB : enqueues
  SUBMISSION ||--o{ EXECUTION_RESULT : produces
  EXECUTION_RESULT ||--o{ EXECUTION_TEST_RESULT : details
  ENROLLMENT ||--o{ GRADE_ENTRY : accumulates
  GRADE_ENTRY }o--|| ASSESSMENT_COMPONENT : "counts toward"
  GRADE_ENTRY ||--o{ FEEDBACK : receives
```

**`Enrollment` is the spine.** Every student-owned row (progress, attempt, submission, grade)
hangs off `enrollment_id`, never a bare `user_id`. That makes "another student's data" not
merely forbidden but *unrepresentable* — the cheapest possible enforcement of brief §9 and §60.

Progress is deliberately **four-level** (course / module / lesson / activity), not a single
percentage, so a teacher can answer "who is behind, and where" (brief §26).

## 4. Key lifecycles

```
Subject / Course / Module / Lesson / Quiz / CodingTask
  DRAFT ──publish──► PUBLISHED ──archive──► ARCHIVED
    ▲                    │
    └──── unpublish ─────┘        (publishing runs CoursePublicationValidator)

Enrollment
  PENDING ─► ACTIVE ─► COMPLETED | FAILED
              ├─► SUSPENDED ─► ACTIVE
              └─► WITHDRAWN            (terminal; history kept in enrollment_events)

QuizAttempt   IN_PROGRESS ─► SUBMITTED ─► GRADED
                          └─► EXPIRED | ABANDONED

Submission    QUEUED ─► RUNNING ─► COMPLETED(verdict) | FAILED
ExecutionJob  PENDING ─► CLAIMED ─► RUNNING ─► SUCCEEDED | FAILED ─► (retry) | DEAD

GradeEntry    DRAFT ─► PUBLISHED ─► REVISED (superseded) | VOIDED     [append-only]
```

## 5. Enumerations owned by the domain

`UserStatus` `RoleCode` `SubjectKind` `SubjectStatus` `DegreeLevel` `SessionType`
`DeliveryMode` `PublicationStatus` `LearningOutcomeCategory` `RequirementType`
`ContentBlockType` `ActivityType` `CollaborationMode` `VariantStrategy` `Difficulty`
`AssessmentComponentKind` `QuestionType` `AnswerMatchMode` `AttemptStatus`
`EnrollmentStatus` `ProgressStatus` `SubmissionStatus` `Verdict` `ExecutionJobStatus`
`ScoringStrategy` `TestCaseVisibility` `ComparisonMode` `GradeSource` `GradeStatus`
`MaterialType` `NotificationType` `AuditAction` `Locale`.

All stored as `text` + CHECK constraint (ADR-009): extending an enum is a one-line migration,
and a bad value is impossible.
