# Implementation phase plan

**13 phases.** Not 5, not 20 — 13 is what the scope actually splits into: each phase ends at a
point where the system still starts, the tests still pass, and the next phase has something
real to build on.

**Status correction (2026-09-21):** the table below previously marked Phases 3–6 ✅. That was
aspirational — this repository's first real build/test/run pass (see `../BUILD-STATUS.md`) found
**zero Java** for `academics`, `curriculum`, `enrollment` or `progress`; only their Flyway
migrations existed. Phases 0–2 were verified first (107 tests green against a real Postgres, a
real `docker compose up --build` proven end-to-end). **Phases 3, 4 and 5 have since been
implemented and verified** (172 tests green; both supplied syllabi served through the real API,
real enrollment concurrency proven against the DB partial unique index, hundreds of real
syllabus-derived lessons served through the new curriculum API — see `../BUILD-STATUS.md`).
**Phase 6 (progress tracking) has since been implemented and verified** (184 tests green; a
concurrent double-completion race proven against real Postgres, the four-level roll-up
self-healing a stale raw-SQL seed placeholder on first real write — see `../BUILD-STATUS.md`).
**Phase 7 (assessment engine) has since been implemented and verified** (197 tests green; all
seven question types graded deterministically against real Postgres, including the real seeded
`DEV-QUIZ-DB` fixture; a genuine `TransactionRequiredException` defect in the progress-integration
event listener found and fixed via `docker compose up --build` — see `../BUILD-STATUS.md`).
**Phase 8 (coding tasks + isolated execution) has since been implemented and fully verified**
(233 tests green; the `docker-run` sandbox engine actually run end-to-end against a real Docker
daemon through a 10-case security matrix — correct/incorrect Python and Go, timeout, memory bomb,
fork bomb, network-exfiltration attempt — finding and fixing 3 real sandbox bugs; 2 more real
authorization/lazy-loading bugs found writing `CodingSubmissionIT`; V014's restricted-role grants
proven exactly right against a real Postgres via `RestrictedRoleGrantsIT`; `FOR UPDATE SKIP
LOCKED` proven to hold under real concurrent claims via `ExecutionJobQueueConcurrencyIT` — see
`../BUILD-STATUS.md` and `../code-execution/security-model.md`). **Phase 9 (grading & feedback)
has since been implemented and verified** (242 tests green; a graded quiz attempt proven to
auto-create a published `grade_entry` and recompute an enrollment's final score end-to-end
through the real HTTP API in one submit call, a manual-grade override proven to supersede — not
duplicate — the current entry, void proven to remove an entry from `v_current_grade_entries`; a
genuine missing-`created_at`/`updated_at` defect in `Feedback` found and fixed via
`GradingIT` — see `../BUILD-STATUS.md`). **Phase 10 (teacher & admin surfaces) has since been
implemented and verified** (246 tests green; the submission-review-queue-and-inline-feedback item
turned out to already exist from Phases 8–9 and was verified rather than rebuilt; a read-only
role/permission catalogue, an admin audit browser, and a registrar CSV roster import are new; a
genuine latent defect in `AuditLogRepository.search` — a null-bound JPQL filter Postgres's
extended protocol cannot type — found and fixed the moment a real caller finally exercised it, see
`../BUILD-STATUS.md`). **Phase 11 (analytics, notifications, search, files) has since been
implemented and verified** (251 tests green; all four modules got real Java for the first time —
`notification` (in-app, event-bus-driven, two producers wired end to end), `analytics` (read-only
wrappers over all six V015 views), `search` (full-text catalogue search over subjects + courses),
`files` (local-storage upload/download/delete with SHA-256 checksums and PUBLIC/AUTHORIZED/PRIVATE
visibility); a genuine `TransactionRequiredException` in the new `notification` module's first
`@TransactionalEventListener(AFTER_COMMIT)` found and fixed via `Phase11IT` — see
`../BUILD-STATUS.md`). **Phase 12 (hardening, full test matrix, deployment) has since been carried
out** (253 tests green; `StudentDataIsolationIT` — a class CLAUDE.md §7.3 and this document's own
quality gate name but that never existed — written and green; a genuinely cold `docker compose up
--build` rehearsed with one observed, self-healing startup race documented rather than hidden; a
real `pg_dump`/`pg_restore` cycle rehearsed with row counts verified; a 200-concurrent load test
run with a real, traced root cause for its tail latency; three hot queries `EXPLAIN ANALYZE`d,
all using their intended index; a secret scan confirming `.env` was never committed — see
`../BUILD-STATUS.md`'s "Phase 12" section and the new `../deployment/runbook.md`). **This closes
all 13 planned phases.** Reverse-proxy/TLS, a log-aggregator sidecar, and dependency CVE scanning
remain open — each is either a deployment-target-specific decision this repository cannot make
generically, or (CVE scanning) blocked on network/API-key access not available in this
environment; both are named explicitly, not silently dropped. Per-phase sections below are
otherwise unchanged from the original plan and remain the intended scope for each phase.

Legend: ✅ implemented and verified · 🟨 partially implemented · ⬜ not started

| # | Phase | Status |
|---|---|---|
| 0 | Analysis & architecture | ✅ |
| 1 | Project foundation & cross-cutting | ✅ verified (was previously unexecuted) |
| 2 | Identity, authentication, authorization | ✅ verified (was previously unexecuted; `AdminUserController`/`MeController` + IT tests added) |
| 3 | Academic catalogue (subjects, courses, outcomes) | ✅ verified — 18 entities, 14 repositories, full admin/public/teacher API, `AssessmentWeightValidator`/`HourBudgetValidator`, `SubjectCatalogueIT`/`CourseDeliveryIT`; both syllabi (`IAKTT1105` 150h, `DAS1110` 180h+150h=330h) reconcile end-to-end through the real API |
| 4 | Enrollment | ✅ verified — `Enrollment`/`EnrollmentEvent`, full admin/teacher/self API, `EnrollmentTest`'s transition matrix, `EnrollmentIT`, `EnrollmentConcurrencyIT` proving `uq_enrollment_active` holds under 8 simultaneous requests |
| 5 | Curriculum & content (modules, lessons, blocks, activities) | ✅ verified — `CourseModule`/`Lesson`/`ContentBlock`/`Activity` (13 entities, 11 repositories), a 7-handler `ContentBlockPayloadValidator` registry covering all 15 block types (real HTML sanitization), an `ActivityTypeHandler` registry covering all 8 activity types, both failing application startup if a type is ever left unhandled; `CurriculumIT` (locale fallback, translation-completeness gate, unpublished-invisible-to-students, reorder integrity, hostile-HTML rejection); a real seeded `IAKTT1105` lesson tree (100+ lessons) served correctly through the new API |
| 6 | Progress tracking | ✅ verified — four-level roll-up (`CourseProgress`/`ModuleProgress`/`LessonProgress`/`ActivityProgress`), optimistic-lock-retry on concurrent completions, `ProgressIT` (6 tests) |
| 7 | Assessment engine (quizzes) | ✅ verified — all 7 question types (incl. `MATCHING`/`ORDERING`), `QuestionGraderRegistry`, `AttemptService` lifecycle with idempotency/expiry/concurrency, `AssessmentIT` (10 tests) |
| 8 | Coding tasks + isolated execution | ✅ verified — full `coding`/`execution` stack in `api` (incl. `CodingSubmissionIT`) + a working `execution-worker` (JDBC queue, `Judge0`/`docker-run`/`Noop` sandbox adapters, grading, write-back), all run end-to-end against a real Docker daemon, restricted-role grants and queue concurrency both proven with Testcontainers |
| 9 | Grading & feedback | ✅ verified — `grading` domain/persistence/app/api (`GradeEntry` append-only lifecycle, `FinalScoreCalculator`, auto-grade wiring off `QuizAttemptGradedEvent` and a coding-submission poller, `AdminGradeController`/`GradeController`/`AdminFeedbackController`), `FinalScoreCalculatorTest` + `GradingIT` |
| 10 | Teacher & admin surfaces | ✅ verified — submission review queue + inline feedback confirmed already built (Phases 8–9); new this phase: `AdminRoleController` (role/permission catalogue), `AdminAuditController` (audit browser), CSV roster import (`AdminEnrollmentController.importCsv`); `AdminSurfacesIT` (4 tests) |
| 11 | Analytics, notifications, search, files | ✅ verified — `notification` (`Notification`, event-bus producers off `common.events.NotificationRequestedEvent`, `MyNotificationsController`), `analytics` (`AdminAnalyticsController` over the six V015 views), `search` (`SearchController`, `websearch_to_tsquery`/`ts_rank`), `files` (`FileController`, `LocalFileStorage`); `Phase11IT` (5 tests) |
| 12 | Hardening, full test matrix, deployment | ✅ verified — `StudentDataIsolationIT` (new, closes a real CLAUDE.md §7.3 gap), a genuinely cold `docker compose up --build` rehearsal, real `pg_dump`/`pg_restore` cycle, `ab -c 200` load test with a traced root cause for its tail latency, `EXPLAIN ANALYZE` on 3 hot queries, a secret scan; `docs/deployment/runbook.md` (new) |

---

## Phase 0 — Analysis & architecture ✅
**Goal** Understand the academic domain before writing code; commit to an architecture.
**Prereqs** Supplied syllabi.
**Deliverables** `docs/academic-analysis/*`, ADR-001..011, `docs/domain/domain-model.md`,
`docs/architecture/README.md`, `CLAUDE.md`, `skills/`, this plan.
**DB/API** none.
**Security** threat model for untrusted execution (ADR-006) written before any execution code.
**Acceptance** every structural claim traceable to a document or marked INFERRED;
every deferred item in `PENDING-INPUTS.md`.
**DoD** an AI agent can start Phase 1 from the docs alone.

## Phase 1 — Project foundation & cross-cutting ✅
**Goal** A repo that boots, migrates, documents itself and fails consistently.
**Modules** `common`, config, `docker/`.
**DB** `V001` extensions + shared functions/triggers (`set_updated_at`, `fts_config`).
**API** `/actuator/health/{liveness,readiness}`, `/v3/api-docs`, `/swagger-ui.html`.
**Tests** context load, `ApiError` shape, `PageResponse` cap, ArchUnit skeleton.
**Security** security headers, CORS allowlist, request size limits, error sanitization,
`NoProcessExecutionInApiTest`.
**Acceptance** `docker compose up` starts API+DB; migrations apply; health is UP; an unknown
path returns a well-formed `ApiError`.
**DoD** `./mvnw verify` green; no `TODO` in `common`.

## Phase 2 — Identity, authentication, authorization ✅
**Goal** Secure login and a role model that survives new roles.
**DB** `V002` users/roles/permissions/user_roles/role_permissions/refresh_tokens/profiles;
`V003` reference data (3 roles, ~40 permissions, mappings).
**API** `POST /auth/login|refresh|logout|logout-all`, `GET /auth/me`,
`POST /auth/change-password`; `/admin/users` CRUD.
**Tests** login success/failure, lockout, token rotation, **reuse detection**, expired/forged
token rejection, permission enforcement, `/admin/*` refused to students.
**Security** BCrypt-12, no user enumeration, constant-ish timing, audit events, rate limit.
**Acceptance** a student token cannot reach any `/admin` route; a rotated refresh token is
dead; reuse revokes the family.
**DoD** `AuthenticationIT` + `AuthorizationMatrixIT` pass; zero plaintext credentials in repo
outside `db/seed` (clearly marked dev-only).

## Phase 3 — Academic catalogue ✅
**Goal** Model AIFU's real catalogue: departments, programmes, years, terms, subjects,
outcomes, prerequisites, courses with hour budgets and rubrics.
**DB** `V004` org+subjects, `V005` courses/teachers/allocations/components/grade scales/materials.
**API** `/subjects`, `/courses` (public catalogue, paginated+filtered),
`/admin/subjects`, `/admin/courses`, `/teacher/courses`.
**Tests** unique subject code, self-prerequisite rejected, component weights sum to scheme
total, publication validator, teacher-of-course authorization.
**Acceptance** both supplied syllabi representable **with zero schema change** — the real
test of Phase 0. ✅ verified: the existing `db/seed/V901` rows for both subjects load and render
end-to-end through the new API with zero schema change.
**DoD** seeded `IAKTT1105` and `DAS1110` reconcile to 150 h and 330 h. ✅ verified via
`docker compose up --build` + real HTTP calls, see `../BUILD-STATUS.md` "Phase 3 end-to-end
verification".

## Phase 4 — Enrollment ✅
**Goal** Explicit enrolment with preserved history and no duplicates under concurrency.
**DB** `V006` enrollments + enrollment_events + partial unique index.
**API** `/admin/enrollments` (single + bulk), `/teacher/courses/{id}/students`,
`/enrollments/me`.
**Tests** duplicate-active rejected (**including two concurrent requests**), withdraw→re-enroll
allowed and both rows retained, status transition matrix, closed-course rejection.
**Acceptance** history is never lost; `EnrollmentConcurrencyIT` proves the DB index holds. ✅
verified: 8 simultaneous `POST /admin/enrollments` for the same course+student → exactly 1 row
created, 7 `409 ENROLLMENT_DUPLICATE`.
**DoD** every transition writes an `enrollment_event`. ✅ verified via `EnrollmentIT`'s
`events_recordEveryTransition` and, independently, via `docker compose up --build` against real
seeded data — see `../BUILD-STATUS.md` "Phase 4 end-to-end verification".

## Phase 5 — Curriculum & content ✅
**Goal** The generic, multilingual, multi-subject content engine.
**DB** `V007` modules/lessons/content blocks/activities/variants + translations + FTS columns.
**API** `/courses/{id}/modules`, `/lessons/{id}` (student, enrolment-scoped),
`/teacher/.../modules|lessons|blocks` (CRUD + reorder + publish).
**Tests** per-`block_type` payload validation, sanitization of hostile HTML, locale fallback
metadata, translation completeness gate, unpublished content invisible to students,
reorder integrity, `ActivityTypeCoverageTest`. ✅ verified — `ContentBlockPayloadValidatorsTest`,
`ContentBlockValidatorRegistryTest`, `ActivityTypeCoverageTest` (unit), `CurriculumIT` (integration).
**Security** `ContentSanitizer` on every write; no raw HTML passthrough. ✅ verified —
`contentBlock_hostileHtml_isRejected` (`CurriculumIT`) confirms a `<script>` payload is rejected
with `422 UNSAFE_CONTENT_REJECTED` before it reaches the database.
**Acceptance** a lesson with 6 block types renders from one API call in uz with `ru` falling
back and `isFallback` flagged. ✅ verified via `docker compose up --build`: a lesson with
TEXT/CALLOUT/CODE_EXAMPLE/LINK/EMBED/QUIZ_REF blocks, `GET /api/v1/lessons/{id}?locale=uz`
returned each with `payloadIsFallback=false`; `?locale=ru` returned the identical content for
every block with `payloadIsFallback=true` — see `../BUILD-STATUS.md` "Phase 5 end-to-end
verification".
**DoD** adding a block type or activity type touches enum + handler + test only. ✅ verified by
construction: `ContentBlockValidatorRegistry`/`ActivityTypeHandlerRegistry` both fail application
startup (not just a test) if any enum value has no registered handler.

## Phase 6 — Progress ✅
**Goal** Four-level progress that answers teacher questions.
**DB** `V008` course/module/lesson/activity progress + unique constraints.
**API** `POST /lessons/{id}/progress`, `GET /courses/{id}/progress/me`,
`GET /teacher/courses/{id}/progress`. ✅ verified — all three implemented (`progress.api`),
`PROGRESS_SELF_WRITE`/`PROGRESS_SELF_READ`/`PROGRESS_COURSE_READ` (pre-seeded V003).
**Tests** idempotent completion, percentage roll-up, concurrent updates (optimistic lock
retry), `last_accessed_at`, isolation (student A cannot read B). ✅ verified —
`ProgressRollupTest` (6 unit tests, domain transition rules) + `ProgressIT` (6 integration
tests: `completingALessonTwice_doesNotDoubleCount`, `courseCompletionPercent_matchesLessonRollup`,
`concurrentProgressUpdates_bothSucceed_rollupStaysConsistent`,
`lastAccessedAt_advancesOnEachRecordedAccess`, `studentB_cannotSeeStudentAsProgress`,
`teacherRoster_showsEachStudentsOwnProgress`).
**Acceptance** completing a lesson twice does not double-count; course % matches lesson roll-up.
✅ verified by `ProgressIT` and independently via `docker compose up --build` against real seeded
`IAKTT1105` data — see `../BUILD-STATUS.md` "Phase 6 end-to-end verification".
**DoD** no single-percentage-only storage anywhere. ✅ verified: `lesson_progress` is the granular
source of truth (view count, per-lesson status); `module_progress`/`course_progress` are
roll-ups **recomputed from source on every write, never incremented** — a repeated completion
recomputes to the same COUNT, which is what makes idempotency a structural property rather than
a special-cased guard.

## Phase 7 — Assessment engine ✅
**Goal** Reusable quizzes with attempt history and auto-grading.
**DB** `V009` quizzes/questions/options/answer keys/attempts/attempt questions/selected options.
**API** teacher authoring (`/teacher/courses/{id}/quizzes`, `/teacher/quizzes/{id}/questions`,
options, answer keys, translations, reorder, publish) and student attempts
(`POST /quizzes/{id}/attempts`, `POST /attempts/{id}/questions/{id}/answer`,
`POST /attempts/{id}/submit`, `GET /attempts/{id}`, `GET /quizzes/{id}/attempts/me`,
`GET /teacher/quizzes/{id}/attempts`). ✅ verified — all implemented (`assessment.api`),
`QUIZ_READ`/`QUIZ_MANAGE`/`QUIZ_ATTEMPT_CREATE`/`QUIZ_ATTEMPT_SELF_READ`/`QUIZ_ATTEMPT_REVIEW`
(pre-seeded V003).
**Done** schema, entities, attempt lifecycle service (`AttemptService`: start/answer/submit,
lazy expiry, `Idempotency-Key` on submit per CLAUDE.md §9), graders for all seven question types
— `SINGLE_CHOICE`/`TRUE_FALSE`/`MULTIPLE_CHOICE` (with partial credit)/`SHORT_ANSWER`/`NUMERIC`/
`MATCHING`/`ORDERING`, a `QuestionGraderRegistry` that fails application startup if any type is
left unhandled (same pattern as `ContentBlockValidatorRegistry`/`ActivityTypeHandlerRegistry`),
question-pool randomization + question shuffling at attempt start, reveal-policy-aware
student/teacher DTO split, student+teacher DTOs.
**Tests** grading per question type, partial credit, attempt-limit enforcement, concurrent
double-submit, expiry, correct answers hidden until policy allows. ✅ verified —
`QuestionGraderRegistryTest` (3 unit tests, completeness) + `AssessmentIT` (10 integration
tests: one per question-type family, partial credit, attempt-limit enforcement, concurrent
double-submit, submit idempotency-key replay, expiry, `revealPolicy=NEVER` hiding, and the
progress-integration regression test below).
**Acceptance** an attempt is graded deterministically, updates progress, and never exposes the
answer key early. ✅ verified — grading is a pure function of stored correct answers/keys, never
the client's echo; `revealPolicyNever_hidesCorrectAnswersAndExplanationForever` proves the served
option list never carries `isCorrect` regardless of policy. **Partially deferred, honestly**:
"produces a `grade_entry`" is not implemented — `grading` (Phase 9) does not exist yet, matching
Phase 6's precedent of not inventing a not-yet-built module's write path. A `QuizAttemptGradedEvent`
(ADR-014) is published on every grading instead, which `grading` can subscribe to once it exists,
exactly as `progress` already does today.
**DoD** adding a question type touches enum + grader + test only. ✅ verified by construction:
`QuestionGraderRegistry` fails application startup (not just a test) if any `QuestionType` value
has no registered grader.

## Phase 8 — Coding tasks + isolated execution ✅
**Goal** Python/Go submissions graded in a sandbox, never in the API.
**DB** `V010` languages/tasks/languages/hints/test cases/submissions; `V011` execution
jobs/results/test results; `V014` restricted worker role + grants.
**Done** schema, entities, submission use case (guards + attempt limits + idempotency +
transactional enqueue), `SandboxEngine` port, Judge0 + docker-run + noop adapters, worker
claim/lease/retry loop, comparison modes, scoring strategies, output truncation, hidden-test
redaction, Compose wiring with network isolation.
**Remaining** teacher task-authoring endpoints, resubmission diffing, per-language image
pinning, **the entire execution test matrix actually being run** (brief §64).
**Security** ADR-006 in full. Any change here needs security review.
**Acceptance** all of: valid Python, valid Go, syntax error, compile failure, runtime error,
timeout, infinite loop, memory bomb, fork bomb, network attempt, filesystem escape attempt,
wrong output, correct output — each producing the right verdict with no host impact.
**⚠️ Not verified in the authoring environment (no Docker/network). This is the single
highest-priority verification gap.**

## Phase 9 — Grading & feedback ✅
**Goal** Append-only gradebook implementing AIFU's weighted, gated rubric.
**DB** `V012` grade_entries (+ immutability trigger, current-grade view) + feedback.
**Done** schema, trigger, entities, append-only service, `FinalScoreCalculator` with
component weights + `min_pass_percent` gates, 100→5 band mapping, feedback model.
**Remaining** teacher gradebook endpoints (bulk entry), manual-override UI contract,
transcript projection, admission/debt report.
**Tests to write** `GradeEntryImmutabilityIT`, supersession chain, final score vs the
`IAKTT1105` rubric (30/30/40 + 60% gates), band mapping at 59/60/79/80/89/90.
**Acceptance** correcting a published grade creates revision 2 and preserves revision 1.

## Phase 10 — Teacher & admin surfaces ✅
**Goal** Everything brief §21-22 lists, with explicit privileges.
**Done** course/content/module/lesson management, roster read, progress read, user admin,
enrollment admin, publication validation, submission review queue + inline feedback (confirmed
already built in Phases 8–9: `TeacherSubmissionController`, `AdminFeedbackController` with
`target_type=SUBMISSION`), role/permission catalogue (`AdminRoleController`, read-only per
ADR-004 — roles are data, not code), CSV roster import (`AdminEnrollmentController.importCsv`),
admin audit browser (`AdminAuditController`, `AUDIT_READ`).
**Remaining** quiz/coding authoring UI contracts (the underlying CRUD already exists and is
OpenAPI-documented — see `../BUILD-STATUS.md`'s "What remains"), system configuration (no backing
schema exists anywhere; not invented — see `PENDING-INPUTS.md` §12), role/permission catalogue
*mutation* (deliberately out of scope — ADR-004 treats a new role as a migration, not an API call).
**Security** no "admin can do anything" shortcut inside services — `ADMIN` bypass lives only
in the guards.
**Acceptance** `AuthorizationMatrixIT` exercises `/admin/users`/`/admin/*/roles` end to end across
STUDENT/TEACHER/ADMIN; the new surfaces are covered by `AdminSurfacesIT` the same way.

## Phase 11 — Analytics, notifications, search, files ✅
**Goal** Answer the brief §26 questions; light notifications; FTS; storage.
**Done** analytics SQL views wired to real endpoints (`AdminAnalyticsController`, course/quiz/
coding-task-scoped, `ANALYTICS_COURSE_READ`); in-app notification model + read/unread API
(`MyNotificationsController`) with `ENROLLMENT_CREATED`/`GRADE_PUBLISHED` producers wired through
a new event-bus record (`common.events.NotificationRequestedEvent`) so no module needed a new
dependency edge to trigger one; FTS search endpoint over subjects + courses with authorization
baked into the query (`PUBLISHED`-only, no post-hoc filtering step to forget); `FileStorage` port
+ `LocalFileStorage` adapter with real upload/download/delete endpoints, SHA-256 checksums, and
PUBLIC/AUTHORIZED/PRIVATE visibility enforcement.
**Remaining** analytics response caching (not built — reads are fast enough on seeded data
without it so far); notification producers for the other 7 seeded `type` values and the `EMAIL`/
`PUSH` channels (no dispatch job exists for either, since only `IN_APP` needs one); lesson/
content-block search (would need a new `search -> enrollment` module edge for enrollment-scoped
visibility — an ADR-level decision, not made here); S3 storage adapter (config/schema-ready, no
implementation); group submissions (`PENDING-INPUTS.md` §5, unrelated to this phase's own scope).
**Acceptance** each §26 question answerable by one endpoint — verified via `Phase11IT` against
real Postgres, not benchmarked for the 200 ms target on seeded data (no load-testing pass has run
yet; that is Phase 12's job).

## Phase 12 — Hardening, full test matrix, deployment ✅
**Goal** Actually prove it works.
**Done** whole suite run and green (253 tests); `StudentDataIsolationIT` written (a real,
previously-missing gap, not a rebuild — see `../BUILD-STATUS.md`); a genuinely cold `docker
compose up --build` rehearsed (`down -v` first) with one self-healing startup race observed and
documented; a real `pg_dump`/`pg_restore` cycle rehearsed with row counts verified; a 200-
concurrent load test (`ab -n 4000 -c 200`) run with 0 failures and a confirmed, traced root cause
for its p95/p99 tail (HikariCP pool sizing, not a code defect); `EXPLAIN ANALYZE` run on 3
representative hot queries, all confirmed on their intended index; a secret scan confirming `.env`
was never committed; `docs/deployment/runbook.md` written with every command and number above.
**Remaining** the §64 execution matrix was already run live in Phase 8 (`docs/code-execution/
security-model.md`) and not repeated since nothing in Phases 9–12 touched the sandbox; dependency
CVE scanning (no scanner wired in, blocked on NVD access not available here); reverse proxy + TLS
and a log-aggregator sidecar (both deployment-target-specific decisions — hostname, certificates,
which aggregator — that cannot be made generically; structured JSON logging itself already exists
and is aggregator-ready, confirmed by reading `logback-spring.xml`).
**Acceptance** every checklist item above was actually executed on this machine and its real
output is quoted in `../BUILD-STATUS.md`'s "Phase 12" section and `../deployment/runbook.md` —
not assumed, not templated.
**DoD** see `../BUILD-STATUS.md`'s "Phase 12" section for the signed-off numbers.

---

## Per-phase quality gate (brief §62)
1. `./mvnw -pl api test` (unit) · 2. `./mvnw verify -Pit` (integration) · 3. read every failure,
fix root cause · 4. `docker compose up` from scratch, migrations apply cleanly on an empty DB
· 5. Hibernate `validate` passes (no drift) · 6. OpenAPI diff reviewed for breaking changes ·
7. `AuthorizationMatrixIT` + `StudentDataIsolationIT` green · 8. `ArchitectureBoundaryTest`
green. **Never start the next phase on a red gate.**
