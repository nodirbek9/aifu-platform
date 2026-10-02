# Build status — honest inventory

Last updated 2026-09-23, after Phase 12 (hardening, full test matrix, deployment) was carried out
on an actual machine (Java 21, Maven, Docker all available). **Read this before trusting any claim
of completeness.**

## Verification status: VERIFIED — foundation through hardening

Unlike the original hand-off (Java 17, no Maven, no Docker, no network — nothing executed), every
pass since has actually run everything and fixed forward. Stated plainly:

- ✅ `./mvnw clean verify -Pit` (whole reactor: `api` + `execution-worker`) is green — 253 tests
  (223 in `api` — Phase 11's 221 plus 2 new this pass: `StudentDataIsolationIT`; 30 in
  `execution-worker`, unchanged).
- ✅ All 17 schema migrations + 3 dev-seed migrations apply cleanly to a real PostgreSQL 16.
- ✅ 253 tests pass: unit (incl. every prior phase's list), `ArchitectureBoundaryTest`
  (75 module-boundary rules), `NoProcessExecutionInApiTest`, and Testcontainers-backed integration
  tests (`ApplicationContextLoadIT`, `AuthenticationIT`, `AuthorizationMatrixIT`,
  `SubjectCatalogueIT`, `CourseDeliveryIT`, `EnrollmentIT`, `EnrollmentConcurrencyIT`,
  `CurriculumIT`, `ProgressIT`, `AssessmentIT`, `CodingSubmissionIT`, `RestrictedRoleGrantsIT`,
  `ExecutionJobQueueConcurrencyIT`, `GradingIT`, `AdminSurfacesIT`, `Phase11IT`,
  `StudentDataIsolationIT` — the last one new this pass) exercising real HTTP calls and real
  Postgres, no mocks.
- ✅ **`StudentDataIsolationIT` closes a real, explicit gap**: CLAUDE.md §7.3 and the per-phase
  quality gate (`phase-plan.md`) both name this class as required, but it never existed as its own
  file before this pass — cross-student isolation was previously proven only piecemeal, inside
  individual module ITs (e.g. `CodingSubmissionIT`'s submission-ownership test). Two new tests:
  a quiz attempt owned by another student returns `404` (not `403` — ADR-004, existence is not
  leaked) while the owner's own read succeeds; a published grade entry owned by another student is
  equally invisible while the owner's own `GET /enrollments/{id}/grades` succeeds.
- ✅ **A genuine, real `docker compose up --build` cold-start rehearsal** — `docker compose down -v`
  first (a truly empty volume, not a warm cache), then a full rebuild: Postgres healthy, all 20
  migrations (17 schema + 3 dev seed) applied, `api`'s readiness probe `UP` within ~15 s. One
  observed, self-healing startup race documented in `docs/deployment/runbook.md` (the worker polls
  `execution_jobs` for ~2 s before `api`'s own migration creates it, then recovers on its own next
  poll — not a defect, `execution-worker`'s scheduled retry is exactly what a "physically separate
  worker with no shared startup ordering" design should do).
- ✅ **A real backup/restore rehearsal**: `pg_dump -Fc` from the running container, restored into a
  scratch database, row counts verified identical (`SELECT count(*) FROM users` — 8 in both).
- ✅ **A real load-test smoke run** (`ab -n 4000 -c 200` against an authenticated, indexed read
  endpoint): 0 failed requests, median 172 ms, but p95/p99 tail (592 ms / 886 ms, worst 2116 ms)
  traced to a real, confirmed cause — `HikariCP`'s default `maximum-pool-size=15` queuing ~185 of
  200 simultaneous DB-touching requests. See `docs/deployment/runbook.md`'s "Load / capacity
  observations" for the full numbers and the reasoning for not changing the default blind.
- ✅ **`EXPLAIN ANALYZE` on three representative hot queries** (active-enrollment lookup, course
  lesson listing, audit-log browse) — all sub-millisecond on seeded data, all confirmed using their
  intended index (`uq_enrollment_active`, `uq_module_position`, `idx_audit_log_occurred`), none
  falling back to a sequential scan.
- ✅ **A real secret scan**: `.env` confirmed git-ignored and never committed; no AWS-style access
  key pattern or PEM private-key block exists in any tracked file.
- ⚠️ **Dependency CVE scanning was not run** — no scanner is wired into the build, and an ad-hoc
  OWASP dependency-check run needs either an NVD API key or a long first-run database download,
  neither available in this environment; see `docs/deployment/runbook.md` for what a real run
  would need. Not silently skipped — recorded honestly as a gap.
- ⚠️ **Reverse proxy + TLS and log-aggregator wiring were not built** — both are deployment-target
  specifics (hostname, certificates, which aggregator) that cannot be safely invented generically.
  Structured JSON logging (Logstash encoder, `requestId`/`userId` from MDC) already exists under
  the `prod` Spring profile and is aggregator-ready — confirmed by reading `logback-spring.xml`,
  not assumed.
- ✅ **Phase 11's four new modules, all with real Java for the first time**: `notification`
  (in-app only, V013 — `Notification`/`NotificationService`, a new `common.events.
  NotificationRequestedEvent` any module can publish without a new module-map edge, two real
  producers wired — `EnrollmentService.enroll` → `ENROLLMENT_CREATED`, `GradeEntryService.
  recomputeFinalScore` → `GRADE_PUBLISHED`); `analytics` (read-only `@Immutable`-entity wrappers
  over all six V015 views, `AdminAnalyticsController` gated by the already-seeded
  `ANALYTICS_COURSE_READ`); `search` (full-text catalogue search over subjects + courses via
  `websearch_to_tsquery`/`ts_rank` against V004/V005's generated `search_vector` columns,
  authorization baked into the query itself — only `PUBLISHED` rows are ever selectable);
  `files` (`FileStorage` port + `LocalFileStorage` adapter, SHA-256 checksummed uploads,
  PUBLIC/AUTHORIZED/PRIVATE visibility enforced in `FileService.canRead`, soft delete).
- ✅ **A real, previously-undetectable defect found and fixed while wiring the first-ever
  `@TransactionalEventListener(AFTER_COMMIT)` in `notification`**: `NotificationService.notify`
  needed `Propagation.REQUIRES_NEW`, not plain `@Transactional` — see "Defects found and fixed —
  Phase 11" below.
- ✅ **Phase 10's closing pieces**: a read-only role/permission catalogue (`AdminRoleController`,
  `GET /admin/roles` + `/admin/permissions`, gated by `ROLE_MANAGE` since V003 seeds no narrower
  `ROLE_READ`), an admin audit browser (`AdminAuditController`, `GET /admin/audit-log`, gated by
  the already-seeded `AUDIT_READ`), and a registrar CSV roster import
  (`POST /admin/enrollments/import`, one student number per line, per-row outcomes exactly like
  the existing JSON `/bulk` endpoint). The submission review queue + inline feedback item on
  Phase 10's remaining list turned out to already exist (`TeacherSubmissionController` from Phase
  8, `AdminFeedbackController` with `target_type=SUBMISSION` from Phase 9) — verified, not
  rebuilt.
- ✅ **Wiring the audit browser to a real caller found and fixed a real, previously-latent
  defect** in `AuditLogRepository.search` — see "Defects found and fixed — Phase 10" below (a
  static JPQL "`:param is null or ...`" filter query that Postgres's extended protocol rejects the
  instant any filter is actually left null, replaced with a `Specification` that only adds a
  predicate for filters that are set).
- ✅ **A graded quiz attempt now auto-creates a published `grade_entry` and recomputes the
  enrollment's derived final-score/band/admission/debt snapshot end-to-end, through the real HTTP
  submit endpoint** (`GradingIT.gradedQuizAttempt_autoCreatesPublishedGradeEntry_andRecomputesFinalScore`)
  — not asserted against a mock, against a real Postgres row. The append-only supersede/void flow
  (a manual override creates revision 2, revision 1 disappears from the current view, voiding
  drops the entry from the final score) is verified the same way.
- ✅ **Writing `GradingIT` found and fixed a real defect** — see "Defects found and fixed —
  Phase 9" below (a missing `created_at`/`updated_at` assignment in `Feedback`'s constructor,
  caught by a real `NOT NULL` violation, not a mock).
- ✅ `docker compose up --build` (Phase 8's stack — `postgres` + `api` + `execution-worker`)
  still starts cleanly from a cold volume with the new V017 migration applied; not re-run fresh
  this pass since Phase 9 added no new Docker-relevant surface (no new adapter, no new worker
  code) — see "What remains" for what a genuinely thorough Phase 9 close-out would still add.
- ✅ **The `docker-run` sandbox engine was exercised end-to-end against a real Docker daemon**
  (Phase 8) — not just compiled — with a 10-case security verification matrix (correct/incorrect
  Python and Go, timeout, memory bomb, fork bomb, network-exfiltration attempt). **Three real bugs
  were found and fixed only by actually running it** (a fundamentally broken bind-mount design
  under "docker-outside-of-docker," a Go build-cache/tempdir placement bug, and a CPU-ulimit that
  was wrongly wrapping the compile step). See `docs/code-execution/security-model.md` for the full
  matrix and findings — including one still-open, documented best-effort limitation
  (TLE-vs-MLE classification for a bare SIGKILL, since `docker-run` never reports peak memory).
- ⚠️ Judge0 itself was not stood up (`docker/judge0/` is still empty) — an explicit scope decision,
  not an oversight; `judge0` remains ADR-006's named production default.
- ⚠️ **Feedback has no student-facing read endpoint** — V003 seeds no student-facing feedback
  permission at all (only `FEEDBACK_WRITE`, teacher-only). See `grading.api.AdminFeedbackController`'s
  own class doc and "What remains" below for the reasoning; this was a deliberate scope decision,
  not an oversight, given the permission model itself doesn't provide a seeded read grant.

**Phase 1/2's first real run found and fixed 15 concrete defects that had never been exercised**
(see "Defects found and fixed — Phase 1/2" below). **Phase 3 found and fixed 3 more; Phase 4 found
and fixed 1 more; Phase 5 found and fixed 1 more; Phase 6 found and fixed 2 more; Phase 7 found and
fixed 4 more; Phase 8 found and fixed 5 more (3 inside the `docker-run` sandbox engine itself, 2
more — a lazy-loading bug and a missing authorization check — found writing its own test suite
afterward); Phase 9 found and fixed 1 more; Phase 10 found and fixed 1 more; Phase 11 found and
fixed 1 more** (see the respective sections below) — each phase inherits the same
`ArchitectureBoundaryTest` and IT harness built during Phase 1/2, so violations are caught before
merge rather than after. This is the expected cost of a first real run against a database (or,
Phase 8, a real sandbox), not a sign the design was bad. **Phase 12 found zero new code defects**
— it closed one real process gap instead (`StudentDataIsolationIT` existing at last) and produced
operational findings (a capacity-tuning observation, a self-healing startup race) rather than bugs,
which is the expected shape of a hardening pass on code that eleven prior real-execution passes
already exercised.

## What is complete and verified

| Area | Status |
|---|---|
| Academic analysis of both supplied syllabi | ✅ `docs/academic-analysis/syllabus-analysis.md` |
| Pending-input register | ✅ `docs/academic-analysis/PENDING-INPUTS.md` |
| 11 ADRs, architecture + domain docs | ✅ `docs/decisions/`, `docs/architecture/`, `docs/domain/` |
| 13-phase implementation plan | ✅ `docs/planning/phase-plan.md` — **status column corrected this pass, see note below** |
| `CLAUDE.md`, 12 project skills | ✅ (testing skill corrected this pass — see below) |
| Database schema, V001–V015 + dev seed V900–V902 | ✅ **written AND now proven to apply** against real Postgres 16 |
| Maven poms, Spring profiles, `mvnw` wrapper | ✅ wrapper added this pass; reactor build genuinely green |
| `common` module | ✅ code + tests; one real bug fixed (see below) |
| `audit` module | ✅ code; one real bug fixed (the FK deadlock) |
| `iam` domain, persistence, auth services, access guards | ✅ code, **now with an IT-tested auth flow** |
| `iam` API surface | ✅ `AuthController`, `AdminUserController`, `MeController` — the latter two written this pass |
| Foundational tests | ✅ `ArchitectureBoundaryTest`, `NoProcessExecutionInApiTest`, `ApplicationContextLoadIT`, unit tests for `ApiError`/`PageResponse`/`PageableSupport` — all written this pass |
| Identity/auth tests | ✅ `AuthenticationIT`, `AuthorizationMatrixIT` — login/lockout/refresh-rotation/reuse-detection/permission-matrix, all against a real DB |
| `docker/api/Dockerfile`, `docker/postgres/init/`, root `docker-compose.yml` | ✅ written Phase 1/2, **re-proven this pass with `docker compose up --build` including the new academics endpoints** |
| `academics` domain, persistence, app services, validators | ✅ 18 entities, 14 repositories, `SubjectService`/`CourseService`/`AcademicCalendarService`, `AssessmentWeightValidator`, `HourBudgetValidator` — all written and tested this pass |
| `academics` API surface | ✅ `SubjectController`, `CourseController` (public catalogue), `AdminSubjectController`, `AdminCourseController`, `TeacherCourseController` (self-scoped), `AdminAcademicCalendarController` |
| `common.content.TranslationCompletenessValidator` | ✅ new, deliberately generic (shared by `academics` now, `curriculum` later) |
| `iam.app.org.OrgReferenceDataService` | ✅ new — read-only `Department`/`AcademicProgramme` lookup so `academics` never reaches into `iam.persistence` directly (see defect #2 below) |
| Academic-catalogue tests | ✅ `AssessmentWeightValidatorTest`, `HourBudgetValidatorTest` (unit), `SubjectCatalogueIT`, `CourseDeliveryIT` (Testcontainers) — unique-code conflict, self-prerequisite rejection, translation-completeness gate, weight/hour reconciliation, teacher-of-course authorization |
| `enrollment` domain, persistence, app service | ✅ `Enrollment`/`EnrollmentEvent` entities, `EnrollmentRepository`/`EnrollmentEventRepository`, `EnrollmentService` (enroll with closed-course/window/capacity/duplicate checks, per-student-transactional bulk enroll, full status transition matrix), `EnrollmentLookupAdapter` (the real `iam.app.access.EnrollmentLookup` port, replacing the `Optional.empty()` placeholder from Phase 1/2) — all written and tested this pass |
| `enrollment` API surface | ✅ `AdminEnrollmentController` (single + bulk enroll, status transitions, roster), `TeacherCourseStudentsController` (self-scoped roster), `MyEnrollmentsController` (`/enrollments/me`) |
| Enrollment tests | ✅ `EnrollmentTest` (unit — the full PENDING/ACTIVE/SUSPENDED/COMPLETED/FAILED/WITHDRAWN transition matrix), `EnrollmentIT` (duplicate-active rejection, closed-course rejection, withdraw→re-enroll history preservation, status transitions, event history, teacher/self access scoping), `EnrollmentConcurrencyIT` (8 simultaneous enroll requests for the same course+student — proves the DB partial unique index, not just the app-level pre-check, is what actually holds) |
| `curriculum` domain, persistence, app services | ✅ `CourseModule`/`Lesson`/`ContentBlock`/`Activity` (+ translations, `LessonLearningOutcome`, `LessonPrerequisite`, `ActivityVariant`) — 13 entities, 11 repositories; `ModuleService`/`LessonService`/`ContentBlockService`/`ActivityService` with reorder (deferred unique constraints), publish gates, translation batching; `CurriculumLocatorAdapter` (the real `iam.app.access.CurriculumLocator` port) |
| `curriculum` content extension point | ✅ `ContentBlockPayloadValidator` — 7 handlers covering all 15 `ContentBlockType` values (rich text, callout, code example, file-backed media, link, embed, cross-module reference), dispatched by `ContentBlockValidatorRegistry`, which fails application startup if any type is left unhandled. `ActivityTypeHandlerRegistry` does the same for all 8 `ActivityType` values. Both reuse `common.content.ContentSanitizer` for real HTML sanitization, not a stub. |
| `curriculum` API surface | ✅ `ModuleController`/`LessonController` (public, enrollment-scoped, `PUBLISHED`-only), `AdminModuleController`/`AdminLessonController`/`AdminContentBlockController`/`AdminActivityController` (teacher/admin CRUD + reorder + publish + translations + variants) |
| Curriculum tests | ✅ `ContentBlockPayloadValidatorsTest`, `ContentBlockValidatorRegistryTest`, `ActivityTypeCoverageTest` (unit — named to match phase-plan.md's Tests list verbatim), `CurriculumIT` (locale fallback metadata, translation-completeness gate, unpublished content invisible to students, reorder integrity, hostile-HTML rejection, sanitized content rendering, activity reference validation) |
| `progress` domain, persistence, app service | ✅ `CourseProgress` (`@MapsId` shared identity with `Enrollment` — its PK IS `enrollment_id`), `ModuleProgress`/`LessonProgress`/`ActivityProgress` (own generated UUID PK, new `common.domain.UpdatableEntity` base — id+`updated_at`+`version`, no `created_at`, since V008's four tables were deliberately built without one); `ProgressService.recordLessonProgress` recomputes module- and course-level roll-ups **from source on every write, never incrementing** a counter, retried through a `self`-proxy on either an `ObjectOptimisticLockingFailureException` (racing updates to an existing roll-up row) or a `DataIntegrityViolationException` (racing find-then-create of a brand-new roll-up row) |
| `progress` API surface | ✅ `LessonProgressController` (`POST /lessons/{id}/progress`, no `AccessGuard` — ownership is the caller's own active enrollment, resolved entirely through the already-allowed `enrollment`/`curriculum` dependencies), `MyCourseProgressController` (`GET /courses/{id}/progress/me`), `TeacherCourseProgressController` (`GET /teacher/courses/{id}/progress`, the one endpoint in this module that *does* consume `AccessGuard` — see ADR-012) |
| `ADR-012` (`progress` → `iam`, second reason) | ✅ new — documents the second, distinct category of `-> iam` edge (consuming `AccessGuard` from a teacher-facing endpoint, vs. implementing one of its ownership ports); `docs/architecture/README.md`'s diagram and `ArchitectureBoundaryTest`'s `ALLOWED` map both updated to match |
| Progress tests | ✅ `ProgressRollupTest` (unit — `LessonProgress`/`ModuleProgress`/`CourseProgress` transition rules, including the self-sufficiency fix below), `ProgressIT` (idempotent completion, percentage roll-up matching the lesson-level count, concurrent-update retry proven against real Postgres, `last_accessed_at` advancing, cross-student isolation, teacher roster correctness) |
| `assessment` domain, persistence, app services | ✅ `Quiz`/`Question`/`AnswerOption`/`QuestionAnswerKey`/`QuizAttempt`/`QuizAttemptQuestion`/`QuizAttemptSelectedOption` (+ translations) — `Quiz` extends `VersionedEntity` (manual `createdBy`, matches `Enrollment`'s shape); `Question`/`AnswerOption` have no audit columns at all (matches `answer_options`' actual schema — no base class forced onto them); `QuizAttempt` has `version` but no `created_at`/`updated_at` at all, a third distinct shape none of the existing base classes fit, so it implements identity/versioning directly. `QuizService`/`QuestionService` (CRUD, reorder via deferred unique constraints, translations, publish gate incl. "at least one question"); `AttemptService` (start/answer/submit lifecycle, lazy expiry, `Idempotency-Key` on submit, question-pool randomization + shuffle at start) |
| `assessment` grading extension point | ✅ `QuestionGrader` — 6 implementations covering all 7 `QuestionType` values (`SingleSelectionGrader` covers both `SINGLE_CHOICE`/`TRUE_FALSE`), dispatched by `QuestionGraderRegistry`, which fails application startup if any type is left unhandled — same pattern as `ContentBlockValidatorRegistry`/`ActivityTypeHandlerRegistry`. `MultipleChoiceGrader`/`OrderingGrader` support `settings.partialCredit`; `MatchingGrader`/`OrderingGrader` are original design decisions documented in their own javadoc, since V009's schema does not fully specify a submission shape for either question type |
| `assessment` API surface | ✅ `QuizController` (public, enrollment-scoped, `PUBLISHED`-only), `AdminQuizController`/`AdminQuestionController` (teacher/admin CRUD + reorder + publish + translations + options + answer keys), `QuizAttemptController` (student start/answer/submit/list-own/get-own, no `AccessGuard` — ownership is the caller's own active enrollment, ADR-012's reasoning reused), `TeacherQuizAttemptController` (roster) |
| `ADR-013` (split `CurriculumLocator`) | ✅ new — fixes a real defect Phase 5's own `CurriculumLocatorAdapter` javadoc had already flagged: a single `Optional<CurriculumLocator>` bean cannot have two implementers, so `courseIdOfQuiz`/`courseIdOfCodingTask` move to their own single-implementer ports (`AssessmentLocator`, `CodingLocator`). `AccessGuard`, the architecture diagram, and `ArchitectureBoundaryTest` all updated |
| `ADR-014` (`progress`⇄`assessment` via a domain event) | ✅ new — `assessment` cannot depend on `progress` (module map runs the other way), so `AttemptService` publishes `common.events.QuizAttemptGradedEvent` on grading and `ProgressService` listens with `@TransactionalEventListener(AFTER_COMMIT)`, updating `activity_progress`/`course_progress`. Also documents the `REQUIRES_NEW` propagation fix — see defect #4 below |
| Assessment tests | ✅ `QuestionGraderRegistryTest` (unit — grader completeness), `AssessmentIT` (10 integration tests: single/multiple-choice incl. partial credit, short-answer/numeric via answer keys, matching/ordering, attempt-limit enforcement, concurrent double-submit, submit-idempotency-key replay, expiry, `revealPolicy=NEVER` hiding, and the real progress-integration regression test) |
| `coding` domain, persistence, app services | ✅ `CodingTask`/`CodingTaskTranslation`/`ProgrammingLanguage`/`CodingTaskLanguage`/`CodingTaskHint`(+translation)/`CodingTaskTestCase`/`Submission` — `CodingTaskService` (CRUD, publish gate incl. "at least one test case", translations), `CodingTaskContentService` (languages/hints/test cases bundled, matching `QuestionService`'s pattern), `SubmissionService` (idempotency-key replay, per-student rate limiting via `bucket4j`, attempt-limit, concurrent-submissions cap, language-enabled check, source-size ceiling) |
| `execution` domain, persistence, app services (in `api`) | ✅ `ExecutionJob`/`ExecutionResult`/`ExecutionTestResult` (read-only from `api`'s side — written by `execution-worker` via the restricted V014 role), `SubmissionSubmitService` — the ADR-007 orchestrator that creates a `Submission` and enqueues its `ExecutionJob` atomically, living in `execution.app` (not `coding.app`) because the module map only allows `execution -> coding`, never the reverse |
| `coding`/`execution` API surface | ✅ `CodingTaskController` (public, student-safe — no solution code, hidden tests redacted), `AdminCodingTaskController`/`AdminCodingTaskContentController` (teacher CRUD), `SubmissionController` (`POST /submissions` honouring `Idempotency-Key`, get-own, list-own), `TeacherSubmissionController` (roster, unredacted review) |
| `execution-worker` (separate deployable) | ✅ **written, compiled, and run against a real Docker daemon this pass** — plain-JDBC domain records (no JPA, connects with the restricted V014 role), `ExecutionJobQueue` (`FOR UPDATE SKIP LOCKED` claim, lease renewal, backoff retry, expired-lease sweeper), `SubmissionDataRepository`/`ResultWriteBackRepository`, `SandboxEngine` port with three adapters (`Judge0SandboxEngine`, `DockerRunSandboxEngine`, `NoopSandboxEngine`), `Grader`/`Scorer`/`OutputComparator` (all 6 `comparison_mode` values, all 3 `scoring_strategy` values), `ExecutionLoop` tying it together, `ExecutionWorkerApplication` main class. `docker/execution-worker/Dockerfile` + a `docker-compose.yml` service (wired to `docker-run`, with the docker.sock trade-off documented inline) are new this pass too. |
| `ADR-007` bridge to `progress` | ✅ new — `execution-worker` is a separate PROCESS with no shared Spring event bus, so the `QuizAttemptGradedEvent` pattern (ADR-014) doesn't apply; instead `progress.app.ProgressService` gained a `@Scheduled` sweeper (`syncCodingSubmissions`) that polls terminal, unsynced submissions via `coding.app.SubmissionService` (module map already allows `pr -> co`) and folds them into `activity_progress`, marking `submissions.progress_synced_at` (new column, V016 — deliberately outside V014's grant list, so only `api` can ever set it) |
| Coding/execution tests | ✅ `execution-worker`'s pure grading logic (`OutputComparatorTest` — all 6 comparison modes, `GraderTest` — 8 tests incl. the SIGKILL TLE/MLE heuristic, `ScorerTest` — 6 tests, all 3 scoring strategies) plus `api`'s `CodingSubmissionIT` (6 tests — idempotency, attempt-limit, hidden-test/`solutionCode` redaction, ownership isolation) plus `RestrictedRoleGrantsIT`/`ExecutionJobQueueConcurrencyIT` below. |
| Live sandbox verification (docker-run) | ✅ **the real, load-bearing verification for this phase** — a 10-case matrix run against an actual `docker compose up --build` stack with a real Docker daemon (correct/incorrect Python and Go, timeout, memory bomb, fork bomb, network-exfiltration attempt). Found and fixed 3 real bugs (see defects below). Full write-up: `docs/code-execution/security-model.md`. |
| `RestrictedRoleGrantsIT` (`execution-worker`) | ✅ new — proves V014's restricted role grants are EXACTLY right via Postgres's own `has_table_privilege`/`has_column_privilege` catalog functions against a real Testcontainers Postgres migrated with `api`'s own Flyway scripts (`filesystem:` location, since `execution-worker` deliberately does not depend on the `api` artifact). 6 tests: can read the 5 tables needed to run a submission, can fully manage its 3 own execution tables, can update only the 8 narrow outcome columns on `submissions` (never `source_code`/`enrollment_id`/`attempt_number`/`coding_task_id`), and cannot see `users`/`refresh_tokens`/`audit_log`/`grade_entries`/`enrollments`/`quizzes`/etc at all. |
| `ExecutionJobQueueConcurrencyIT` (`execution-worker`) | ✅ new — proves `FOR UPDATE SKIP LOCKED` (V011's own documented claim statement) actually holds: 8 concurrent "workers" race to claim one fixture job, exactly one ever succeeds. Fixture rows inserted with `session_replication_role = replica` to bypass the FK chain a real submission would otherwise need. |
| `grading` domain, persistence, app services | ✅ `GradeEntry` (append-only, DRAFT/PUBLISHED/REVISED/VOIDED — `supersede`/`publish`/`markRevised`/`voidEntry` methods, all a plain in-Java defense-in-depth mirror of `trg_grade_entry_immutable`), `Feedback` (polymorphic `targetType`/`targetId`, markdown sanitized on write). `GradeEntryService` (`recordAutoGrade`/`recordManualGrade`/`voidEntry`, one-current-entry-per-(enrollment,component,source-ref) supersession), `FeedbackService`. `assessmentComponentId`/`activityId`/`evaluatorUserId`/`sourceRefId`/`supersedesId` are all bare `UUID` columns on `GradeEntry`, not `@ManyToOne` — only `enrollment` (same module) is a real association, keeping `academics`/`curriculum`/`iam` entirely out of `grading`'s domain layer |
| `FinalScoreCalculator` | ✅ the `IAKTT1105` rubric made machine-computable (ADR-011) — a pure function over `ComponentSpec`/`EarnedEntry`/`GradeBand` records (not JPA entities, mirroring `execution-worker.grading.Scorer`'s Phase 8 split): sums each top-level component's own earned points (its children's, if any), applies `blocksFinal`/`min_pass_percent` admission gates, flags academic debt off the FINAL-kind component specifically, resolves the `grade_scale_bands` symbol. 5 unit tests against the syllabus's own numbers (30/30/40, 60% gates, 59/60/79/80/89/90 band boundaries) |
| Auto-grade wiring | ✅ QUIZ: `GradeEntryService.onQuizAttemptGraded` listens for `QuizAttemptGradedEvent` (ADR-014's pattern, reused) — same-process, same `@TransactionalEventListener(AFTER_COMMIT)` + `REQUIRES_NEW`-via-self-proxy shape Phase 7 established. CODING: `GradeEntryService.syncCodingSubmissions`, a `@Scheduled` poller mirroring `progress.app.ProgressService`'s identical bridge (ADR-007) — a SEPARATE marker column (`submissions.grade_synced_at`, V017) from progress's `progress_synced_at` (V016), since the two are independent consumers of the same signal |
| `enrollment.recordFinalGrade` | ✅ new write path on `EnrollmentService` (not a stray mutation on an entity obtained from a read call — CLAUDE.md §2) — the derived final-score/band/admission/debt snapshot ADR-011 describes, recomputed by `grading.app.GradeEntryService` whenever published entries change |
| `grading` API surface | ✅ `GradeController` (student: `GET /enrollments/{id}/grades`, ownership via the loaded `Enrollment`'s student, no `AccessGuard`), `AdminGradeController` (teacher: roster, record/override, void — `AccessGuard.requireCourseEditor`), `AdminFeedbackController` (teacher: create/update/list, ownership resolved per polymorphic `targetType` — 5 branches, one per `FeedbackTargetType`, each via an already-eagerly-fetched association to avoid the exact lazy-loading trap found in Phase 8) |
| Grading/feedback tests | ✅ `FinalScoreCalculatorTest` (5 unit), `GradingIT` (4 integration — a graded quiz attempt auto-creating a PUBLISHED entry and recomputing the final score/band/admission end-to-end through real HTTP; manual-grade supersession + void; cross-teacher feedback rejection; same-course feedback create+list) |
| `iam` role/permission catalogue | ✅ `UserService.listRoles`/`listPermissions` (`RoleRepository.findAllWithPermissions` — one `@EntityGraph` query, no N+1 on the `ManyToMany`; new `PermissionRepository`), `AdminRoleController` (`GET /admin/roles`, `GET /admin/permissions`, both `ROLE_MANAGE`) — read-only, since ADR-004 already treats roles as data, not code; assignment stays `PUT /admin/users/{id}/roles` (Phase 2) |
| Admin audit browser | ✅ `AuditService.search` (now a `Specification`, not the latent-broken static `@Query` — see defect below), `AdminAuditController` (`GET /admin/audit-log`, `AUDIT_READ`, every filter optional: actor/action/entityType/entityId/from/to) |
| Registrar CSV roster import | ✅ `EnrollmentService.importByStudentNumbers` (same per-row-isolated-transaction shape as `bulkEnroll`, via `self`; an unresolved student number is a failed row, never a thrown exception), `UserService.findUserIdByStudentNumber`, `AdminEnrollmentController.importCsv` (`POST /admin/enrollments/import`, multipart, `ENROLLMENT_MANAGE`) |
| Teacher & admin surfaces tests | ✅ `AdminSurfacesIT` (4 integration — role/permission catalogue is `ROLE_MANAGE`-gated and lists the seeded roles with their real permissions, audit log is `AUDIT_READ`-gated and surfaces a real recorded action, CSV import enrolls a resolvable student number and reports an unresolvable one as a failed row, never a 5xx) |
| `notification` domain/app/api | ✅ `Notification` (`created_at` only, no `updated_at`/`version` — a third id/timestamp shape, standalone like `AuditLog`/`GradeEntry`), `NotificationService` (`notify`/`listOwn`/`unreadCount`/`markRead`), a `@TransactionalEventListener(AFTER_COMMIT)` on the new `common.events.NotificationRequestedEvent` — any module can trigger a notification via the event bus without a new module-map edge (same reasoning as `QuizAttemptGradedEvent`, ADR-014). `MyNotificationsController` (`GET /notifications/me`, `/me/unread-count`, `POST /{id}/read`, all `NOTIFICATION_SELF_READ`) |
| Notification producers wired | ✅ Two real ones, proving the pipe end to end: `EnrollmentService.enroll` → `ENROLLMENT_CREATED` (dedupe-keyed on the enrollment id), `GradeEntryService.recomputeFinalScore` → `GRADE_PUBLISHED` (fires on every recompute, no dedupe key — each grade change is its own notification). The other 7 seeded `notifications.type` values (`LESSON_COMPLETED`, `QUIZ_GRADED`, `SUBMISSION_GRADED`, `TEACHER_FEEDBACK`, `ANNOUNCEMENT`, `DEADLINE_REMINDER`, `ACCOUNT_STATUS_CHANGED`) have no producer yet — see "What remains" |
| `analytics` domain/app/api | ✅ Six `@Immutable`-mapped read-only entities over every V015 view (`CourseCompletionView`, `LessonCompletionView`, `QuizPerformanceView`, `QuestionDifficultyView`, `CodingTaskDifficultyView`, `StudentAtRiskView`), `AnalyticsService`, `AdminAnalyticsController` (`ANALYTICS_COURSE_READ`, course-scoped routes guard on the path `courseId` before loading, quiz-/coding-task-scoped routes load the view row first — it already carries its own `course_id` — then guard on that, avoiding a new `assessment`/`coding` module dependency) |
| `search` domain/app/persistence/api | ✅ Full-text catalogue search (subjects + courses only — lesson/content-block search would need a new `search -> enrollment` edge for enrollment-scoped visibility, deliberately out of scope, see "What remains"). `SearchQueryRepository` (`NamedParameterJdbcTemplate`, `UNION ALL` ranked by `ts_rank` against V004/V005's generated `search_vector` columns via `websearch_to_tsquery(fts_config(:locale), :queryText)`), `SearchController` (`GET /search?q=&locale=&limit=`, gated on the existing `SUBJECT_READ` — no dedicated `SEARCH_*` permission was invented). Authorization is baked into the query itself: only `status = 'PUBLISHED'` rows are ever selectable |
| `files` domain/app/api | ✅ `StoredFile` (V013, bare `uploadedBy` UUID — `files` may depend on nothing beyond `common`, the module map's one zero-cross-module-edge entry), `FileStorage` port + `LocalFileStorage` adapter (`@ConditionalOnProperty(aifu.storage.provider=local)`), `FileService` (SHA-256 checksum, content-type/size validation against `StorageProperties`, PUBLIC/AUTHORIZED/PRIVATE visibility via `canRead` — compares bare UUIDs and `common.security.AuthenticatedUser` facts, never an `iam` type), `FileController` (`POST /files`, `GET /files/{id}`, `GET /files/{id}/content`, `DELETE /files/{id}`) |
| Phase 11 tests | ✅ `Phase11IT` (5 integration — enrolling publishes and surfaces a real `ENROLLMENT_CREATED` notification; a manual grade record publishes `GRADE_PUBLISHED`; search finds a published course by title; analytics' course-completion endpoint is course-editor-gated; a PRIVATE file is readable by its uploader and an admin but not an unrelated teacher, and disappears after delete) |
| `StudentDataIsolationIT` | ✅ new — the acceptance test CLAUDE.md §7.3 and the quality gate both name but that never existed as its own class until now. Quiz-attempt and grade cross-student isolation, both asserting `404` (existence not leaked, ADR-004), both with a positive control proving the owner's own read still succeeds |
| Cold-start, backup/restore, load, hot-query, secret-scan rehearsals | ✅ all run for real this pass against the actual `docker-compose.yml` stack and a real Postgres — see `docs/deployment/runbook.md` (new) for the full commands and observed numbers, and "Phase 12" below for the narrative |
| `docs/deployment/runbook.md` | ✅ new — cold start, backup/restore, load/capacity findings, hot-query sanity, secret hygiene, logging, and an honest list of what it deliberately does not cover |

## What remains (unstarted or design-only)

| Area | Notes |
|---|---|
| Dependency CVE scanning | No scanner wired into the build; an ad-hoc OWASP dependency-check run needs an NVD API key or a long first-run DB download, neither available here. See `docs/deployment/runbook.md`. |
| Reverse proxy + TLS | No `nginx`/Caddy config exists. Hostname and certificates are deployment-target specifics that cannot be invented generically. |
| Log aggregator wiring | Structured JSON logging already exists (`logback-spring.xml`, `prod` profile) and is aggregator-ready; no sidecar (Loki/ELK/CloudWatch-style) is configured in `docker-compose.yml` — which aggregator is a deployment-target decision. |
| HikariCP pool sizing for high concurrency | Confirmed via a real 200-concurrent load test that the default `maximum-pool-size=15` is the tail-latency bottleneck at that concurrency (not a bug — CLAUDE.md's actual design target is ~200 total students, not 200 simultaneous requests). Raising it is a one-line env-var change (`DB_POOL_MAX`) left to whoever sizes the target Postgres instance's own `max_connections` accordingly. |
| Student-facing feedback read | No endpoint — V003 seeds no student-facing feedback permission at all (only `FEEDBACK_WRITE`, teacher-only), unlike every other `*_SELF_READ`-shaped resource in this codebase. See `grading.api.AdminFeedbackController`'s class doc: the likely intended design is feedback surfacing embedded in a submission's/attempt's own response, not a standalone endpoint — worth confirming with product before inventing a permission. |
| `grading` test-coverage gaps | No test for `ExecutionJobQueue`-style concurrent-supersession races on `GradeEntry` (two teachers overriding the same component at once), no test for the CODING auto-grade poller (`syncCodingSubmissions`) end-to-end — `CodingSubmissionIT`'s simulated-worker-write technique would extend directly to this. |
| `grade_entries` roster/transcript projections | `AdminGradeController.roster` returns one row per enrollment (current final score/band/admission/debt) but not a full per-component breakdown table, and there is no transcript/admission-report export — `phase-plan.md`'s "Remaining" list for Phase 9 (bulk entry UI contract, transcript projection, admission/debt report) is still open. |
| Quiz/coding authoring UI contracts | Phase 10's remaining list names this explicitly; the underlying CRUD (`AdminQuizController`/`AdminQuestionController`/`AdminCodingTaskController`) was already built and OpenAPI-documented in Phases 7–8, so there is no missing endpoint — what would remain is a dedicated author-facing DTO/wizard shape if the frontend needs one, which cannot be scoped without a frontend to consult. |
| Role/permission catalogue mutation | `AdminRoleController` is read-only by design (ADR-004: new roles are a migration, not an API call) — no endpoint exists to create a role or edit a role's permission set at runtime, and none is planned; adding one would need its own ADR since it changes the "roles are data, not code" model. |
| System configuration screen | Named in Phase 10's remaining list but no backing schema/entity exists anywhere in the codebase for runtime-configurable settings — not built, and not invented per CLAUDE.md §14.5 ("if a requirement is missing, add it to PENDING-INPUTS.md and stop"); see `docs/academic-analysis/PENDING-INPUTS.md` §12. |
| Notification producers for 7 of 9 event types | Only `ENROLLMENT_CREATED`/`GRADE_PUBLISHED` are wired. `LESSON_COMPLETED`, `QUIZ_GRADED`, `SUBMISSION_GRADED`, `TEACHER_FEEDBACK`, `ANNOUNCEMENT`, `DEADLINE_REMINDER`, `ACCOUNT_STATUS_CHANGED` all have a `NotificationType` value and a working pipe (`NotificationRequestedEvent`) but no module publishes them yet — each is a small, mechanical addition once prioritized. |
| `EMAIL`/`PUSH` notification channels | `NotificationChannel` has both values, but only `IN_APP` dispatches (a row IS its delivery); the other two need an actual outbound integration and a real async dispatch job, neither of which exists. |
| Lesson/content-block search | `search`'s module-map edge to `curriculum` is allowed but unused — only subjects + courses are searchable. Enrollment-scoped lesson search would need a new `search -> enrollment` edge (ADR-level decision, not made here). |
| S3 file storage adapter | `FileProvider.S3`/`StorageProperties`' S3 fields are schema/config-ready; no `S3FileStorage` implementation exists. `LOCAL` is the only working adapter. |
| `docker/judge0/` | Still empty — deliberate scope decision for Phase 8 (see `docs/code-execution/security-model.md`'s "Scope of this verification"), not an oversight. `judge0` remains ADR-006's named production default; `docker-compose.yml`'s `execution-worker` service is wired to `docker-run` instead. |
| Wider test suite | The mandatory-coverage list in `skills/testing/SKILL.md` is on track for Phases 1–11, module by module. Phase 12 is the dedicated hardening/full-matrix pass. |
| `docs/api`, `docs/database`, `docs/security`, `docs/development`, `docs/testing`, `docs/deployment` | Directories exist; pages not written. `docs/code-execution/security-model.md` **is** written. |
| Java formatting debt (pre-existing, not any recent pass) | `./mvnw spotless:check` fails on ~105 files written in Phase 1/2 that were never run through `spotless:apply` (whitespace/wrapping only, AOSP style). Every file touched in Phases 3–9 is spotless-clean (each pass runs `spotless:apply` before finishing). Not fixed here to keep each pass's diff scoped to the module it actually changed (CLAUDE.md §14.2, "change one module per commit") — a repo-wide `spotless:apply` is a good, low-risk one-off cleanup commit whenever convenient. |

## Phase 3 end-to-end verification

Beyond the automated test suite, Phase 3's `phase-plan.md` acceptance criterion — "both supplied
syllabi representable with zero schema change" and "seeded `IAKTT1105` and `DAS1110` reconcile to
150 h and 330 h" — was checked directly against the real `dev`-seeded data (SQL written in
Phase 1/2, `db/seed/V901`), through the brand-new API, in a real `docker compose up --build`:

- `IAKTT1105`: one course, 150 total hours, session allocations sum to 150, top-level assessment
  weights sum to 100. ✅
- `DAS1110`: two term deliveries — 180 h (semester 1) + 150 h (semester 2) = **330 h**, matching
  `Course.java`'s javadoc ("6 credits/180h in semester 1 and 5 credits/150h in semester 2"). Each
  delivery's own session allocations and assessment weights independently reconcile. ✅
- `GET /api/v1/subjects` and `GET /api/v1/courses` (public, `PUBLISHED`-only) and
  `GET /api/v1/courses/{id}` (full detail: translations, teachers, session allocations, the full
  `JN`/`ON`/`YaN` rubric tree with `JN`'s children) all returned correct data end to end, logged
  in as the seeded dev admin.

## Phase 4 end-to-end verification

`db/seed/V902` (Phase 1/2 SQL, never touched this pass) inserts real enrollment rows directly —
`student1`/`student2` ACTIVE in `IAKTT1105`, `student3`/`student4` ACTIVE in `DAS1110` S1 — a
useful check in its own right: it proves the new `Enrollment` JPA mapping can read rows it never
wrote. Checked directly, in a real `docker compose up --build`:

- `GET /api/v1/admin/enrollments?courseId=<IAKTT1105>` correctly returned both raw-SQL-seeded
  rows (`student1`, `student2`) with correct names, status, attempt number. ✅
- `GET /api/v1/enrollments/me` as `student1` returned exactly that student's one enrollment. ✅
- `POST /api/v1/admin/enrollments` enrolling the previously-unenrolled `student5` in `IAKTT1105`
  returned `201` with a correctly-populated `CREATED` event; repeating the identical request
  immediately after returned `409 ENROLLMENT_DUPLICATE`. ✅

## Phase 5 end-to-end verification

`db/seed/V901` (Phase 1/2 SQL) turns out to already contain a rich, verbatim-from-the-syllabus
module/lesson tree for `IAKTT1105` — dozens of lessons across `LECTURE`/`SEMINAR`/`INDEPENDENT_STUDY`
session types, with real Uzbek titles — that this pass's Java had never touched until now. Checked
directly, in a real `docker compose up --build`:

- `GET /api/v1/modules/{id}/lessons` against a real seeded module returned 100+ real,
  correctly-paginated, correctly-titled lessons the way `db/seed/V901` actually wrote them
  (session types, positions and all) — the strongest evidence yet that the entity mapping is
  correct against realistic data, not just synthetic test fixtures. ✅
- Created a fresh module → translated it → published it → created a lesson under it → translated
  it → created a `TEXT` content block → wrote a payload containing safe HTML (`<strong>`) →
  published the lesson → fetched it as `student1` (real, dev-seed-enrolled in `IAKTT1105`): the
  sanitized HTML round-tripped correctly end to end. ✅
- The same flow with `<script>alert(1)</script>` in the payload was rejected with
  `422 UNSAFE_CONTENT_REJECTED` before it ever reached the database. ✅
- Phase 5's stated **Acceptance** criterion checked directly: one lesson with 6 different block
  types (`TEXT`, `CALLOUT`, `CODE_EXAMPLE`, `LINK`, `EMBED`, `QUIZ_REF`) rendered from a single
  `GET /api/v1/lessons/{id}`. Requested in `uz` (the locale every block was authored in), every
  block came back with `payloadIsFallback: false`. Requested in `ru` (no `ru` translation exists
  for any block), every block came back with the identical `uz` content and
  `payloadIsFallback: true` — locale fallback works uniformly across every block type, not just
  the one (`TEXT`) exercised by `CurriculumIT`. ✅

## Phase 6 end-to-end verification

`db/seed/V902` (Phase 1/2 SQL) already inserts placeholder `course_progress` rows directly for the
Phase 4 enrollment fixtures — `IN_PROGRESS`, `15.00%`, `lessons_total` from a raw `count(*)` over
*all* published lessons (no `is_required` filter), `lessons_completed = 2` with no backing
`lesson_progress` rows at all. This turned out to be a genuinely useful, adversarial starting
state: it proves the new code does not blindly trust a stale/fabricated counter. Checked directly,
in a real `docker compose up --build`:

- `GET /api/v1/courses/{id}/progress/me` as `student1` (real, dev-seed-enrolled in `IAKTT1105`,
  never touched by this pass's Java before now) first returned the raw seeded placeholder exactly
  as `V902` wrote it: `IN_PROGRESS`, `15.00%`, `lessonsTotal: 61`, `lessonsCompleted: 2`. ✅ proves
  the entity mapping reads pre-existing rows it never wrote — same category of check as Phase 4/5's
  "read what the seed already inserted" verification.
- `POST /api/v1/lessons/{id}/progress` with `{"status":"COMPLETED"}` against a real seeded,
  required, published lesson, then re-reading `GET .../progress/me`: the response now shows
  `lessonsTotal: 63` (the *real*, required-only, live count — different from the seed's naive
  unfiltered `61`) and `lessonsCompleted: 1` (the *real* count from the one actual
  `lesson_progress` row that now exists — not the seed's fabricated `2`), with a populated
  `modules` breakdown for the touched module (`20.00%` = 1 of 5 lessons). The stale placeholder is
  silently and correctly self-healed on the first real write, exactly as the "recomputed from
  source, never incremented" design intends. ✅
- `GET /api/v1/teacher/courses/{id}/progress` as the real seeded teacher (`b.akbarov`) returned
  both real enrolled students with their own, independently correct numbers: `student1`'s
  just-updated `1.59%`/`lessonsCompleted: 1`, and `student2`'s still-untouched, still-stale seeded
  `4.00%`/`lessonsCompleted: 2` — including the "deliberately left stale" `last_accessed_at` the
  seed comment says exists specifically to make `v_students_at_risk` return a row in dev. ✅
- `GET /api/v1/courses/{id}/progress/me` as `student2` after `student1`'s write returned
  `student2`'s own unchanged numbers, not `student1`'s — direct proof of Phase 6's isolation
  requirement (ADR-004 Layer 3/4: scoped by the caller's own `enrollment_id`, never load-then-check)
  against a real second student, not just a synthetic fixture. ✅
- `POST /api/v1/lessons/{id}/progress` as `student3` (real, seeded, but enrolled in `DAS1110`, not
  `IAKTT1105`) against an `IAKTT1105` lesson returned `403 NOT_ENROLLED` — the ownership check
  resolves entirely through `enrollment`/`curriculum` without needing `AccessGuard`, and still
  correctly refuses a real cross-course access attempt. ✅

## Phase 7 end-to-end verification

`db/seed/V902` (Phase 1/2 SQL) already contains a complete, hand-authored demo quiz —
`DEV-QUIZ-DB` on `IAKTT1105`, `PUBLISHED`, `shuffle_questions=true`, `shuffle_options=true`,
`reveal_policy=AFTER_LAST_ATTEMPT`, `max_attempts=3`, one `SINGLE_CHOICE`, one `MULTIPLE_CHOICE`
(with `partialCredit=true`), one `NUMERIC` question — that this pass's Java had never touched
until now, and a real `activities` row (`activity_type='QUIZ'`) already pointing at it. Checked
directly, in a real `docker compose up --build`:

- `GET /api/v1/courses/{id}/quizzes` as `student1` (real, dev-seed-enrolled in `IAKTT1105`)
  correctly listed the seeded `DEV-QUIZ-DB` quiz with its real title. ✅
- `POST /api/v1/quizzes/{id}/attempts` → answered all three real seeded questions correctly
  (`SELECT` for the SQL question, all three "3V" options for the multiple-choice question, `1`
  for "how many primary keys per table") → `POST .../submit`: graded `3.00/3.00 = 100.00%`,
  `passed=true`. `served_position` differed from each question's authored `position` — proof
  `shuffle_questions=true` is honoured, not just accepted and ignored. ✅
- The response's served options carried only `id`/`text`, no `isCorrect`/`pointsAwarded`/
  `explanation` on attempt 1 of a `max_attempts=3`, `AFTER_LAST_ATTEMPT` quiz — reveal correctly
  withheld until the student's last attempt, checked against a real multi-attempt policy, not a
  synthetic `NEVER`/`IMMEDIATELY` fixture. ✅
- A second student answered the multiple-choice question with only 1 of 3 correct options
  selected: `score = 2.33/3.00 = 77.67%` — `partialCredit=true` on a **real seeded** question
  (not a fixture built by this pass's own test) scored proportionally, matching
  `MultipleChoiceGrader`'s formula exactly. ✅

## Defects found and fixed — Phase 1/2 (all previously unexecuted code)

Found by actually running the build/tests/containers — not by inspection. Ordered roughly by
severity:

1. **Self-deadlock on nearly every audited write to a user's own row** — `audit_log.actor_user_id`
   had a real FK to `users(id)`. `AuditService` deliberately commits via
   `Propagation.REQUIRES_NEW` (documented, correct reasoning) but the FK-validation lock that
   requires on the `users` row can never be granted while the *caller's* still-open transaction
   holds a lock on that same row from its own prior UPDATE — a guaranteed, Postgres-undetectable
   deadlock. Hit on login success, password change, and would have hit status/role/password-reset
   changes too. Fixed by dropping the FK (V013) — `actor_label` already exists precisely so the
   audit trail stays meaningful without a live reference.
2. **Brute-force lockout never actually persisted.** `AuthenticationService.login()` incremented
   `failed_login_attempts` and then threw `AUTH_INVALID_CREDENTIALS` to reject the request —
   which rolled back the very same transaction that recorded the failure (Spring's default
   rollback-on-unchecked-exception). An account could never actually lock. Fixed with a
   `UserService.recordFailedLogin` method that commits via `REQUIRES_NEW`.
3. **Refresh-token reuse detection never actually revoked the family**, for the identical reason:
   `RefreshTokenRepository.revokeFamily` ran inside the same transaction as the exception that
   reported the reuse. Fixed by marking that repository method `REQUIRES_NEW`.
4. **`common.security.JwtAuthenticationFilter` imported `iam.app.auth.TokenService` directly** —
   a `common → iam` dependency, backwards per the module map and CLAUDE.md §5 ("common depends on
   no module"), and a live cycle since `iam` already depends on `common`. Fixed with a
   `common.security.AccessTokenAuthenticator` port, implemented by `iam`'s `TokenService`.
5. **`LocaleResolver`'s default Spring bean name (`localeResolver`) collided with Spring MVC's
   well-known `DispatcherServlet` strategy bean name**, which expects its own
   `org.springframework.web.servlet.LocaleResolver` type there. This 500'd *every* HTTP request.
   Fixed by naming the bean explicitly (`@Component("aifuLocaleResolver")`).
6. **`RefreshToken.ipAddress` had `columnDefinition = "inet"` but no `@ColumnTransformer`** —
   satisfies Hibernate's schema *validation* but not a real *write*; every login crashed trying to
   insert a refresh token. `AuditLog.ipAddress` had the opposite gap (transformer, no
   `columnDefinition`) and failed validation instead. Both now carry both.
7. **`permissions` table was missing `created_at`/`updated_at`**, breaking Hibernate schema
   validation against the entity (which extends `BaseEntity`) and violating CLAUDE.md §5 ("every
   table"). `refresh_tokens` had the same gap, plus a `revoked_reason` CHECK constraint missing
   two values (`ACCOUNT_DISABLED`, `ROLES_CHANGED`) that the Java enum already defines and
   `UserService` already sends — would have thrown on the first account suspension or role change
   that also had an active session. All fixed in V002.
8. **`users.email`/`username` are `citext` in the DB but mapped as plain `String`** with no
   `columnDefinition` — same validation-vs-write split as #6. Fixed.
9. **`AccessGuard`'s three ports (`CourseTeacherLookup`, `EnrollmentLookup`, `CurriculumLocator`)
   were required constructor dependencies with zero implementations** (their owning modules don't
   exist yet), so the whole application context failed to start. Changed to `Optional<T>` with a
   clear failure message if a guard method needing an unbuilt module's port is ever actually
   called — the app now boots today and needs no further change once those modules land.
10. **`UserRepository.search()` combined `@EntityGraph(roles)` with pagination`** — Hibernate
    hard-errors on collection-fetch + pagination (`fail_on_pagination_over_collection_fetch` is
    intentionally on). Fixed with a separate batched role lookup instead of a fetch join.
11. **The same query's null `:query` parameter, used inside `concat()`, made Hibernate guess the
    wrong JDBC type** (`lower(bytea) does not exist` against Postgres) whenever no search filter
    was supplied. Fixed by building the finished `%pattern%` (or `null`) in Java and binding it
    directly, no `concat()`.
12. **`db/seed/V901` inserted bare, untyped `NULL` literals into `uuid` columns inside `UNION ALL`
    queries** (`subject_prerequisites.prerequisite_subject_id`,
    `assessment_components.parent_component_id`) — the first time this migration was ever run
    against Postgres, since dev seed only loads under the `dev` profile. Fixed with `NULL::uuid`.
13. **`backend/pom.xml` pinned `bucket4j.version` to `8.10.1`, but the `bucket4j_jdk17-core`
    artifact only starts publishing at `8.11.0`** (earlier versions exist only under the plain
    `bucket4j-core` artifact) — the reactor could not even resolve dependencies. Bumped to
    `8.14.0`.
14. **No `mvnw` wrapper existed**, despite every command in CLAUDE.md assuming one. Generated.
15. **`./mvnw verify -Pit` silently ran zero integration tests.** Surefire's default include
    patterns (`**/*Test.java`, `**/*Tests.java`) never match `*IT.java`, and the `it` profile
    never added an include for it — the documented command for running integration tests did
    nothing. Fixed by adding `**/*IT.java` to the `it` profile's includes.

Also worth recording, not a code defect: **Testcontainers 1.20.2 could not talk to this machine's
Docker Desktop at all** (`BadRequestException` on every strategy) — bumped to 1.21.4, which
resolved it cleanly. And the IT test HTTP client needed to be Apache HttpClient5, not Boot's
default (`HttpURLConnection`) or `java.net.http.HttpClient` — both of the latter two have
reproducible, unrelated bugs/hangs against the embedded Tomcat NIO connector for exactly the
request shapes these tests need (a POST returning 401, a PUT returning 204 on a reused keep-alive
connection). See `AbstractIntegrationTest`'s javadoc.

## Defects found and fixed — Phase 3

Found by writing real unit/integration tests against real Postgres, then by the project's own
`ArchitectureBoundaryTest` — not by inspection. Ordered by severity:

1. **`AssessmentComponent`'s "parent must belong to the same course" check threw a raw
   `NullPointerException` instead of the intended `DomainException`** whenever both `course` and
   `parent.course` were the same not-yet-persisted (transient) entity — it compared
   `parent.course.getId().equals(course.getId())` directly, and a transient entity's `getId()` is
   `null` until Hibernate assigns it. Not reachable through the one production call site today
   (`CourseService.addAssessmentComponent` always loads both `course` and `parent` as already-
   persisted entities via `require()`/`findById()`), but it broke on the very first unit test that
   built a component tree in memory before saving — exactly the kind of latent trap a future
   bulk-import or seed-authoring path would hit. Fixed by comparing entity identity
   (`parent.course.equals(course)`, using `BaseEntity`'s reference-equality-first `equals()`)
   instead of comparing raw id values. `SubjectPrerequisite`'s "cannot be its own prerequisite"
   check had the identical pattern (`prerequisiteSubject.getId().equals(subject.getId())`) and got
   the same fix, proactively, before it could bite the same way.
2. **`academics.app.CourseService`/`SubjectService` injected `iam.persistence` repositories
   (`DepartmentRepository`, `AcademicProgrammeRepository`, `UserRepository`) directly** —
   `ArchitectureBoundaryTest`'s `noModuleReachesIntoAnotherModulesPersistencePackage` rule caught
   9 violations. Fixed by adding `iam.app.org.OrgReferenceDataService` (a thin read-only
   `require(id)` lookup for `Department`/`AcademicProgramme`, matching the existing
   `UserService.require()` convention) and routing `CourseService`'s teacher lookup through the
   existing `UserService` instead of `UserRepository` directly.
3. **`AdminSubjectController`, `CourseController`, `SubjectController` and `SubjectMapper`
   injected `academics.persistence` repositories directly** (`LearningOutcomeTranslationRepository`,
   `CourseTranslationRepository`, `SubjectTranslationRepository`) to get batched translation
   lookups for catalogue list rendering — `controllersNeverInjectARepository` caught 12
   violations. Fixed by adding batch methods to the app-layer services
   (`SubjectService.listTranslationsForSubjects`/`listOutcomeTranslations`,
   `CourseService.listTranslationsForCourses`) and routing every call site through them.

All three were caught by the project's own tests before any manual review — the strongest
argument yet for having written `ArchitectureBoundaryTest` in Phase 1.

## Defects found and fixed — Phase 4

Found by `EnrollmentIT` — not by inspection:

1. **`EnrollmentRepository.findByStudent_Id` (backing `/enrollments/me`) omitted `student` from
   its `@EntityGraph`**, fetching only `course.subject`/`course.term`. `EnrollmentMapper.toSummary`
   calls `enrollment.getStudent().getFullName()`, and that mapping runs in the controller, after
   `EnrollmentService`'s read-only transaction (and its Hibernate session) has already closed —
   so it threw `org.hibernate.LazyInitializationException: could not initialize proxy ... no
   Session` on every call, a 500 on the one endpoint a student actually calls to see their own
   enrollments. The sibling queries (`findByCourse_Id`, `findByCourse_IdAndStatusIn`, used by the
   admin/teacher roster views) already had `student` correctly listed — this one query was missed
   when the three were written together. Fixed by adding `student` to the entity graph.

Two more issues surfaced in the same run but were bugs in the **test code**, not the production
code, and are recorded here only because they're an easy mistake to repeat: (a) `course_teachers`
has a real FK on `assigned_by` (unlike `audit_log`'s, dropped in V013) — a test passing
`UUID.randomUUID()` as the actor for `CourseService.assignTeacher` failed with a constraint
violation; fixed by using a real persisted user's id. (b) manually pre-encoding a query-parameter
space as `%20` before handing the URL string to `RestTemplate.exchange` double-encodes it
(`%20` → `%2520` → decoded once server-side back to the literal string `%20`); fixed by building
the request with `UriComponentsBuilder.queryParam(...)`, which encodes exactly once.

## Defects found and fixed — Phase 5

Found by `CurriculumIT` — not by inspection:

1. **`CourseModuleRepository.findByIdAndDeletedAtIsNull` did not eagerly fetch `course`** — the
   single lookup `ModuleService.require` uses everywhere, including inside
   `LessonService.create`, which resolves the owning module this way *before* building a new
   `Lesson`. That lesson's `getModule().getCourse()` is then read back in the controller
   (`LessonMapper.toDetail`, to resolve the default locale), after `LessonService.create`'s own
   transaction — and therefore its Hibernate session — had already closed. Every "create lesson"
   call threw `org.hibernate.LazyInitializationException: could not initialize proxy [Course] ...
   no Session`, a 500 on the most basic teacher-facing write in the whole module. The same missing
   `@EntityGraph` also broke the public module list (`ModuleMapper.toSummary` needs
   `module.getCourse().getInstructionLanguage()` to resolve locale fallback) via
   `findByCourse_IdAndStatusAndDeletedAtIsNull`. This is the third time this exact class of bug
   has appeared (see Phase 4 defect #1) — a query used across module boundaries needs its
   `@EntityGraph` audited against *every* field every mapper reachable from it will touch, not
   just the ones exercised by the first caller written. Fixed by adding
   `@EntityGraph(attributePaths = "course")` to all three `CourseModuleRepository` lookup methods.
2. **`docs/architecture/README.md`'s module-map diagram never drew `curriculum → iam`**, even
   though `iam.app.access.CurriculumLocator`'s own javadoc says "implemented in curriculum,
   assessment and coding adapters" — the edge was simply missing when the diagram was first drawn
   (`academics → iam` and `enrollment → iam`, the identical pattern for the identical reason, were
   both drawn correctly). `ArchitectureBoundaryTest`'s `ALLOWED` map inherited the same gap and
   failed the build the moment `CurriculumLocatorAdapter` was written. Not a "genuine new need"
   requiring an ADR — a pre-existing inconsistency between an already-documented port and an
   already-drawn diagram. Fixed both: added `curriculum → iam` to the diagram (with a note
   explaining the `ac`/`cu`/`en` → `iam` edges are *only* for implementing `AccessGuard`'s three
   ownership ports, not a general license) and to the test's `ALLOWED` map.

## Defects found and fixed — Phase 6

Found by writing `ProgressRollupTest` and `ProgressIT` against real Postgres — not by inspection:

1. **`ModuleProgress.applyRollup`/`CourseProgress.applyRollup` silently depended on
   `recordAccess` having been called first**, to move status out of `NOT_STARTED`. In production
   this is harmless — `ProgressService.recomputeModuleProgress`/`recomputeCourseProgress` always
   call `recordAccess(now)` immediately before `applyRollup(...)` — but it is a fragile, implicit
   contract between two sibling methods, and `ProgressRollupTest` (calling `applyRollup` alone, the
   way a future caller reasonably might) caught it immediately: a partial completion
   (`lessonsCompleted > 0`, not all required lessons done) left `status` at `NOT_STARTED` instead of
   advancing to `IN_PROGRESS`. Fixed by making `applyRollup` self-sufficient: it now reaches
   `IN_PROGRESS` on its own whenever `lessonsCompleted > 0` and not everything is done, while still
   preserving an already-`IN_PROGRESS` status set by a real view-only `recordAccess` call when
   `lessonsCompleted == 0` (a student who has viewed content but completed nothing must not be
   reported back down to `NOT_STARTED`).
2. **A concurrent "first write" race on `module_progress`/`course_progress` surfaced as a
   `DataIntegrityViolationException` (`uq_module_progress`/the `course_progress` primary key), not
   the `ObjectOptimisticLockingFailureException` the retry loop was written to catch** — a classic
   find-then-create race: when two requests for the *same enrollment* both complete a lesson in the
   *same, not-yet-touched* module at once, both find no existing roll-up row and both attempt an
   `INSERT`; only one wins, the other gets a unique-constraint violation rather than a stale-version
   error (optimistic locking only applies to *updating* a row that already exists).
   `ProgressIT.concurrentProgressUpdates_bothSucceed_rollupStaysConsistent` (two real concurrent
   `POST /lessons/{id}/progress` calls against Postgres, not a mock) caught this as a `409` where
   both requests were expected to eventually succeed. Fixed by having
   `ProgressService`'s retry loop catch `DataIntegrityViolationException` alongside
   `ObjectOptimisticLockingFailureException` — the retried attempt runs in a fresh transaction, by
   which point the winning insert has committed and the retry correctly finds-then-updates instead
   of finding-then-inserting again.

## Defects found and fixed — Phase 7

Found while actually implementing `assessment` — not by inspection:

1. **`iam.app.access.CurriculumLocator`'s single-bean shape could never actually support a second
   implementer**, exactly as Phase 5's own `CurriculumLocatorAdapter` javadoc had already warned:
   "whoever builds those modules next must revisit this single-adapter design." Building
   `assessment` was that moment — `Optional<CurriculumLocator>` resolves to at most one Spring
   bean, so registering a second `@Component implements CurriculumLocator` for `assessment` would
   have failed the whole application context at startup with `NoUniqueBeanDefinitionException`.
   Fixed per ADR-013: split into `CurriculumLocator` (curriculum-only), `AssessmentLocator`
   (assessment-only), `CodingLocator` (registered now, unimplemented until Phase 8) — `AccessGuard`
   updated to take one `Optional<T>` per port.
2. **`assessment` needed `academics` for the identical reason `curriculum` already does**
   (`quizzes.course_id` is a direct FK to `courses`, same shape as `activities.course_id`), but
   the module map only drew `as --> cu & en`. Not a new pattern — the same one `cu --> ac` already
   established — so fixed directly (diagram + `ArchitectureBoundaryTest`) rather than a full ADR.
3. **Every translation repository (`QuizTranslationRepository`, `QuestionTranslationRepository`,
   `AnswerOptionTranslationRepository`) was written with bare Spring Data derived method names
   (`findByQuizIdAndLocale`, etc.) instead of explicit `@Query` JPQL** — the exact pattern already
   used everywhere else in this codebase (`LessonTranslationRepository`, `ActivityTranslationRepository`,
   ...), which this pass mistakenly assumed were auto-derived rather than hand-written. An entity
   with both an `@EmbeddedId` (carrying e.g. `quizId`) and a `@ManyToOne @MapsId` association
   (`quiz`, whose *own* id is also just `id`) is genuinely ambiguous to Spring Data's query
   derivation — `QuestionId` can parse as either `id.questionId` (the embedded id) or
   `question.id` (the association) — and it silently picked the wrong one, failing application
   startup with `Could not resolve attribute 'locale' of 'QuestionTranslation'` the first time
   `AssessmentIT` actually loaded the Spring context (no earlier test exercised these beans).
   Fixed by writing explicit `@Query` for all three repositories, matching `curriculum`'s existing
   translation repositories exactly.
4. **`ProgressService.doRecordActivityAttempt` (ADR-014's event listener) threw
   `TransactionRequiredException: no transaction is in progress`** the moment it tried to flush —
   found only by `docker compose up --build` smoke testing against the real seeded `DEV-QUIZ-DB`
   quiz linked to a real `activities` row, **not** by any automated test (every `AssessmentIT`/unit
   test starts an attempt with `activityId = null`, which short-circuits the listener before it
   touches the database). Root cause: `@TransactionalEventListener(AFTER_COMMIT)` invokes the
   listener from inside the *committing* transaction's `triggerAfterCompletion`, after that
   transaction's resources are unbound from the thread but before its `TransactionInterceptor`
   call has technically returned — a plain `@Transactional` listener method tries to join that
   already-unbound context instead of opening a fresh one. Fixed with
   `@Transactional(propagation = REQUIRES_NEW)`. A regression test now exists:
   `AssessmentIT.gradedAttemptLinkedToActivity_updatesActivityAndCourseProgress` builds a real
   curriculum `Activity` referencing a quiz, starts an attempt with that `activityId`, and asserts
   `course_progress.activitiesCompleted` actually increments after grading.

## Phase 8 end-to-end verification

Beyond `./mvnw clean verify -Pit`, Phase 8's real acceptance criterion was "actually run untrusted
code in the sandbox and watch it come back graded" — a 10-case matrix against a real
`docker compose up --build` stack (`postgres` + `api` + `execution-worker`, the `docker-run`
engine), inserting submissions directly and watching the worker's claim → sandbox → grade →
write-back pipeline complete against real `submissions`/`execution_results`/
`execution_test_results` rows. Full table and findings: `docs/code-execution/security-model.md`.
Headline results: a correct Python solution and a correct Go solution (compile + run) both scored
100/100; a syntax error, a compile failure, a runtime error, and a wrong answer were each
classified correctly; an infinite loop was killed and classified `TIME_LIMIT_EXCEEDED`; a memory
bomb was killed by the `--memory` cgroup and classified `MEMORY_LIMIT_EXCEEDED` (mostly — one
sub-case is a documented, known heuristic limitation); a fork bomb was blocked by `--pids-limit`
(`BlockingIOError: [Errno 11] Resource temporarily unavailable`, no host impact); a network
exfiltration attempt was blocked by `--network=none` (caught `URLError` inside the sandboxed
program). Every ADR-006 security property was independently confirmed against a real sandbox, not
just asserted in code.

## Defects found and fixed — Phase 8

Found only by actually running the `docker-run` sandbox engine against a real Docker daemon — none
of these would surface from reading the code, and none did during authoring:

1. **Bind-mounting source into the sandbox is fundamentally broken under
   "docker-outside-of-docker."** `execution-worker` itself runs inside a container and reaches the
   HOST's docker daemon over a mounted `/var/run/docker.sock` — a path written to the worker's own
   filesystem (a Java temp file) is invisible to the daemon launching the sandbox as a *sibling*
   container, since bind-mount sources are resolved by the daemon, not by the process requesting
   them. `docker run -v <workerLocalTempDir>:/workspace` silently mounted an empty directory,
   surfacing as `python3: can't open file '/workspace/main.py'`. Fixed by never bind-mounting
   source at all — it is base64-encoded, passed as an `-e AIFU_SRC=...` environment variable, and
   decoded by a fixed shell script inside the sandbox (`DockerRunSandboxEngine`).
2. **`go build` needs a writable, exec-permitted scratch directory it doesn't get by default.**
   `$GOCACHE`/`$TMPDIR` default under `$HOME`/`os.TempDir()`, resolving to either the read-only
   root or a 16MB `noexec` tmpfs — both fail outright (`no space left on device` /
   `read-only file system`) for a real multi-package build. Fixed with
   `-e GOCACHE=/build/.gocache -e TMPDIR=/build -e HOME=/build`, plus `-e GOMAXPROCS=1` (Go 1.21
   doesn't read the cgroup `--cpus` quota, so unpinned parallel compilation exceeded a 256MB memory
   ceiling even for a two-import program).
3. **The sandbox's `ulimit -t <cpuTimeLimitMs>` was wrapping the *compile* step, not just the
   run step.** A coding task's `cpu_time_limit_ms` is meant to bound the *submitted program's* own
   CPU time; applying the identical ceiling to `go build` itself reliably SIGKILLed the compiler
   (a cold-cache Go build legitimately needs more CPU time than a typical per-run limit allows) —
   every Go submission, correct or not, failed as a false `COMPILATION_ERROR`. Fixed by moving
   `ulimit -t` to apply only immediately before `exec <runCmd>`.
4. (Content, not code) The `DEV-TASK-SUM` dev-seed fixture's `memory_limit_kb` was tightened to
   131072 (128MB), which — even after fix #2 — still reliably OOM-killed `go build`. Raised to
   262144 (256MB) in `db/seed/V902__dev_seed_fixtures.sql`, with a comment recording why; a
   published Go coding task needs meaningfully more memory headroom than the equivalent Python one
   under `docker-run`'s cold-cache-per-run design.

A fifth issue was found and is **documented, not "fixed" in the sense of eliminated**: `docker-run`
never reports a submission's peak memory usage, so a bare SIGKILL with neither the worker's
wall-clock timeout fired nor a `memoryKb` reading over limit is disambiguated between
`TIME_LIMIT_EXCEEDED` and `MEMORY_LIMIT_EXCEEDED` by a timing heuristic in `Grader`
(`SIGKILL_EXIT_CODE` branch) rather than a certainty. See `docs/code-execution/security-model.md`'s
"Known limitation" section for the empirical accuracy of that heuristic against real runs.

Two more real defects were found writing `CodingSubmissionIT` (the `api`-side test suite added
after the sandbox verification above) — both in `AdminCodingTaskContentController`, both never
exercised until an actual HTTP `PATCH` hit them:

6. **`updateLanguage`/`upsertHintTranslation`/`updateTestCase` threw `LazyInitializationException`
   on every call.** Each did its `AccessGuard` ownership check via a lazy chain
   (`updated.getCodingTask().getCourse().getId()`) evaluated AFTER the `@Transactional` service
   method had already returned — and this codebase runs with `spring.jpa.open-in-view: false`
   (CLAUDE.md's own JPA discipline), so the Hibernate session is closed by the time the controller
   touches that proxy. Fixed by resolving the owning `CodingTask`'s id via a new plain scalar query
   (`CodingTaskLanguageRepository`/`CodingTaskHintRepository`/`CodingTaskTestCaseRepository`'s new
   `findCodingTaskIdById`) BEFORE mutating, then calling `accessGuard.requireCodingTaskEditor`
   first — no lazy access after the transaction ever again. `CodingTaskContentService` gained three
   matching `codingTaskIdOf*` helper methods.
7. **`removeLanguage`/`removeHint`/`removeTestCase` had NO ownership check at all** — only the
   coarse `@PreAuthorize('CODING_TASK_MANAGE')`, which every TEACHER holds platform-wide (V003).
   Any teacher could delete another teacher's coding-task language/hint/test-case by guessing or
   enumerating its UUID — a real CLAUDE.md §7.2 violation ("authorization is server-side and
   centralized... never scattered/missing"), not merely a lazy-loading bug. Fixed with the same
   `codingTaskIdOf*` + `requireCodingTaskEditor` pattern as defect #6, applied to all three
   `DELETE` endpoints.

Both are covered by `CodingSubmissionIT` now indirectly (the test suite exercises `updateTestCase`
as part of its fixture setup, which is how #6 was found), though no test specifically targets #7's
cross-teacher deletion scenario yet — a good next addition, mirroring `AssessmentIT`'s own
cross-teacher checks if any exist, or a fresh `StudentDataIsolationIT`-style test.

## Phase 9 end-to-end verification

Beyond `./mvnw clean verify -Pit`, Phase 9's real acceptance criterion (`phase-plan.md`:
"correcting a published grade creates revision 2 and preserves revision 1") was checked against
real Postgres through the actual HTTP endpoints, not asserted in isolation:

- A published quiz, bound to a graded `Activity` whose `assessmentComponentId` points at the
  fixture course's single top-level component, is submitted by a real enrolled student through
  `POST /attempts/{id}/submit`. By the time that call returns, `GradeEntryService`'s
  `QuizAttemptGradedEvent` listener has already run (synchronous `AFTER_COMMIT`, same shape
  Phase 7 proved for progress) — `GET /enrollments/{id}/grades` immediately shows one PUBLISHED
  `GradeEntry` with `source=QUIZ`, and the enrollment's derived `finalScore`/`finalGradeSymbol`/
  `admittedToFinal` already reflect it.
- A manual grade recorded via `POST /teacher/enrollments/{id}/grades`, then overridden with a
  second call to the same endpoint, produces revision 2 with `supersedesId` pointing at revision
  1 — and the student's current-grades view shows exactly one entry (the new one), never both.
  Voiding it removes it from the current view entirely and the final score returns to 0.
- A teacher without `AccessGuard`-verified ownership of the course cannot write feedback on that
  course's enrollment (`403`); the actual course owner can, and reading it back returns exactly
  what was written.

## Defects found and fixed — Phase 9

Found only by actually running `GradingIT` against real Postgres — not by reading the code:

1. **`Feedback`'s constructor never set `created_at`/`updated_at`**, both `NOT NULL` columns
   (V012). Every `POST /teacher/feedback` failed with a real Postgres constraint violation
   (`null value in column "created_at"`) the first time the endpoint was actually exercised — no
   unit test caught it because `FeedbackService`/`Feedback` were never instantiated end-to-end
   before this integration test existed. Fixed by adding an `Instant now` parameter to the
   constructor (matching every other timestamped entity in this codebase) and passing
   `clock.instant()` from `FeedbackService.create`.

## Phase 10 end-to-end verification

Beyond `./mvnw clean verify -Pit`, real HTTP checks against real Postgres via `AdminSurfacesIT`:

- **Role/permission catalogue is read-only and `ROLE_MANAGE`-gated**: a STUDENT token gets `403`
  on `GET /admin/roles`; an ADMIN token gets `200` with the three seeded roles, and `ADMIN`'s own
  role view includes `ROLE_MANAGE`/`AUDIT_READ` among its granted permission codes — proving the
  `@EntityGraph`-fetched `Role.permissions` collection actually round-trips through HTTP, not just
  through a repository unit test. `GET /admin/permissions` returns the full V003 catalogue.
- **Audit browser is `AUDIT_READ`-gated, not just any-admin**: a TEACHER token (which has plenty
  of other admin-adjacent grants) gets `403` on `GET /admin/audit-log`. An ADMIN token gets `200`,
  and filtering by `actorUserId` on the admin's own id surfaces that admin's own login as a real
  row read back from Postgres — not an empty page, proving the `Specification`-based query (see
  the defect below) actually returns rows, not just avoids throwing.
- **CSV roster import enrolls what it can resolve and reports the rest, never a 500**: one CSV
  body with one real student number and one fabricated one produces exactly one successful
  enrollment row and one `RESOURCE_NOT_FOUND` failure row in the same response — the per-row
  isolation `bulkEnroll` already had (Phase 4) carries over to the CSV path unchanged.

## Defects found and fixed — Phase 10

Found only by wiring a real caller to a repository method that had existed, unused, since before
Phase 9 — not by reading the code:

1. **`AuditLogRepository.search`'s static JPQL is rejected by Postgres the instant any filter is
   actually left null.** The query was written defensively (`:param is null or a.field = :param`
   per filter) but never called by any controller until `AdminAuditController` (this phase) — the
   first real call with `action=null` failed with `org.postgresql.util.PSQLException: ERROR: could
   not determine data type of parameter $N`. A bare `? is null` gives the Postgres extended
   protocol no column context to infer that bind parameter's type from; Postgres's own binder
   cannot resolve it, and reordering the OR clauses (typed comparison first) does not help either
   — each named-parameter *occurrence* becomes its own independent `?` placeholder once Hibernate
   rewrites the JPQL, so the untyped occurrence fails regardless of where it sits in the query
   text. Fixed by dropping the static `@Query` entirely: `AuditLogRepository` now extends
   `JpaSpecificationExecutor<AuditLog>`, and `AuditService.search` builds a `Specification` that
   adds a predicate only for filters that are actually non-null — no unqualified null is ever
   bound to the database at all. Verified via `AdminSurfacesIT.auditLog_isAdminOnly_andSurfacesARealRecordedAction`
   passing with a real `actorUserId` filter (the exact case that reproduced the bug) plus every
   other test in the module still green after the rewrite.

## Phase 11 end-to-end verification

Beyond `./mvnw clean verify -Pit`, real HTTP checks against real Postgres via `Phase11IT`:

- **Enrolling a student publishes a real, readable notification**: `POST` an enrollment via
  `EnrollmentService.enroll`, then `GET /api/v1/notifications/me` as that student returns a
  `ENROLLMENT_CREATED` row — not asserted against a mock event bus, against a Postgres row read
  back over real HTTP with a real JWT. `?unreadOnly=true` filters it correctly (status `SENT`).
- **Recording a manual grade publishes `GRADE_PUBLISHED`**: `POST
  /teacher/enrollments/{id}/grades` (a real 201), then the student's own `GET /notifications/me`
  contains a `GRADE_PUBLISHED` row — proving `GradeEntryService.recomputeFinalScore`'s new
  `eventPublisher.publishEvent(...)` call actually reaches `notification` end to end.
- **Search finds a published course by title, ranked**: `GET /search?q=Fixture&locale=uz` returns
  the fixture course's id among its hits — real `websearch_to_tsquery`/`ts_rank` against real
  Postgres, not a stub.
- **Analytics' course-completion endpoint is course-editor-gated, not just role-gated**: an
  unrelated TEACHER gets `403`; an ADMIN gets `200` with the right `courseId` echoed back from a
  real `v_course_completion` row (which only exists once at least one enrollment does — the view's
  `JOIN enrollments` is inner, not left, on that side).
- **A PRIVATE file's visibility is enforced exactly as designed**: the uploader reads it, an admin
  reads it, an unrelated teacher gets `404` (not `403` — existence is not leaked, ADR-004), and
  after `DELETE` even the uploader gets `404`.

## Defects found and fixed — Phase 11

Found only by actually running `Phase11IT` against real Postgres — not by reading the code:

1. **`AuditLogRepository`'s null-parameter lesson from Phase 10 almost repeated itself, caught in
   review before it shipped** — `search.persistence.SearchQueryRepository` was written from the
   start using bound positional parameters inside a single well-typed `UNION ALL` (never a bare
   `? is null` guard), specifically because of the Phase 10 defect's lesson; no incident here, but
   worth recording that the lesson generalized correctly to new code.
2. **`NotificationService.notify` needed `Propagation.REQUIRES_NEW`, not plain `@Transactional`.**
   The first time `EnrollmentService.enroll` actually published a `NotificationRequestedEvent` and
   `notification`'s `@TransactionalEventListener(phase = AFTER_COMMIT)` fired for real, `self.notify(...)`
   threw `jakarta.persistence.TransactionRequiredException: no transaction is in progress` out of
   `notifications.flush()` — even though the call visibly passed through Spring's
   `TransactionInterceptor` in the stack trace. This is exactly the "stale thread-bound-resource"
   trap `grading.app.GradeEntryService.recordAutoGrade`'s own javadoc already documents and
   defends against with `REQUIRES_NEW`: a plain `@Transactional` (`REQUIRED`) invoked from inside
   an `AFTER_COMMIT` callback can attempt to participate in the just-completing transaction's not
   -yet-fully-unbound resources instead of cleanly starting a new one. **Fixed** by changing
   `notify`'s propagation to `REQUIRES_NEW`, matching the established, already-proven pattern
   exactly — this was a case of not re-applying a lesson from the start (unlike the
   `SearchQueryRepository` case above), caught immediately by `Phase11IT` rather than shipped
   silently.

## Phase 12 — hardening, full test matrix, deployment

Phase 12 is fundamentally different from Phases 1–11: there is no new feature module to build, so
"verification" means actually exercising operational concerns end to end rather than writing more
application code. Everything below was run for real on this machine; none of it is a template.

**Full test matrix**: `./mvnw clean verify -Pit` — 253 tests, 0 failures (223 `api`, 30
`execution-worker`). `ArchitectureBoundaryTest` (75 rules) and `NoProcessExecutionInApiTest` both
green. `StudentDataIsolationIT` — the class CLAUDE.md §7.3 names as the acceptance proof for ADR-004
Layer 3 but that never existed before this pass — is now real and green (see "What is complete").

**`docker compose up --build` from a genuinely empty volume** (`docker compose down -v` first, not
a warm-cache rebuild): all 20 migrations applied (17 schema + 3 dev seed), `api`'s readiness probe
reported `UP` within ~15 seconds of container start. One transient, self-healing condition was
observed and is now documented rather than silently ignored: `execution-worker`'s `@Scheduled`
job-queue poller starts before `api`'s own Flyway migration finishes creating `execution_jobs`
(`docker-compose.yml`'s `depends_on` only waits for Postgres to be healthy, not for `api`'s
migrations to finish, since only `api` owns migrations — ADR-001). The worker logged 1–2
`relation "execution_jobs" does not exist` errors in the first ~2 seconds, then recovered cleanly
on its own next poll with no further errors for the rest of the run. This is the correct behavior
of a "physically separate worker with independent startup" design, not a defect — see
`docs/deployment/runbook.md` for the full account and why a migration-gate init container was not
added to "fix" something that already self-heals.

**Backup/restore rehearsal**: `pg_dump -Fc` against the live container, restored into a scratch
database (`aifu_restore_test`), row counts verified identical in every checked table (`users`: 8
in both source and restore). `pg_restore` produced no errors on a clean target.

**Load test at 200 concurrent** (`ab -n 4000 -c 200` against `GET /api/v1/courses`, authenticated
via a real dev-seeded admin JWT): 0 failed requests out of 4000. Median latency 172 ms, but a real
tail — p95 592 ms, p99 886 ms, worst case 2116 ms. Traced to a confirmed, not guessed, root cause:
`spring.datasource.hikari.maximum-pool-size` defaults to 15 (`DB_POOL_MAX`), so at 200 simultaneous
DB-touching requests roughly 185 of them queue waiting for one of 15 connections — Tomcat's own
thread pool (default 200) accepts every connection immediately, so the wait is entirely at the JDBC
pool layer, not the HTTP layer. Not changed here: CLAUDE.md's stated design target is ~200 *total
enrolled students*, not 200 simultaneous in-flight requests, and raising `DB_POOL_MAX` without also
sizing the target deployment's own Postgres `max_connections` accordingly would be guessing at a
number that only a real deployment target can justify. Full numbers in
`docs/deployment/runbook.md`.

**`EXPLAIN ANALYZE` on three representative hot queries** against the seeded dev database: the
active-enrollment partial-unique-index lookup (0.224 ms, `Index Scan` on `uq_enrollment_active`),
a course's lesson listing via the module join (0.315 ms, `Bitmap Index Scan` on
`uq_module_position`), and the audit-log browse (0.059 ms, `Index Scan` on `idx_audit_log_occurred`
with an incremental sort). All three use their intended index; none falls back to a sequential
scan. At the seeded dataset's current row counts this says little about production scale directly,
but it confirms the index design itself is sound for these access patterns.

**Secret scan**: confirmed directly, not assumed — `.gitignore` excludes `.env`/`.env.*`, no `.env`
file has ever been committed (checked via `git log --all --diff-filter=A`), and no AWS-style access
key pattern or PEM private-key block exists in any tracked file (checked via a repo-wide grep).

**Not done, and recorded honestly rather than silently skipped**: dependency CVE scanning (no
scanner wired in; a real OWASP dependency-check run needs an NVD API key or a long first download,
neither available here), reverse proxy + TLS (no target hostname/certs to configure against), and
a log-aggregator sidecar (the logs themselves are already structured JSON under the `prod` profile
— confirmed by reading `logback-spring.xml` — but which aggregator to ship them to is a
deployment-target decision this repository cannot make generically). All three are named explicitly
in `docs/deployment/runbook.md`'s closing section rather than left implicit.

## Correction to `docs/planning/phase-plan.md`

That file's status table previously marked Phases 3–6 (academics, enrollment, curriculum,
progress) as ✅. **That was aspirational, not actual** at the time — this was corrected during
Phase 1/2's verification pass, when it was found that only migrations existed for those modules.
Phases 3–7 (academic catalogue, enrollment, curriculum & content, progress tracking, assessment
engine) have since been implemented and verified; `phase-plan.md`'s table has been updated to
mark all five ✅ accordingly.

## Unresolved academic conflict (unchanged)

`DAS1110 (Dasturlash)` is unambiguously a **C++** course in the supplied document, while the
brief requires **Python and Go** execution. `programming_languages` is a data table: `python`
and `go` seeded enabled, `cpp` seeded **disabled with a DO-NOT-DELETE note**. AIFU must decide.
Tracked as `PENDING-INPUTS.md §2`.
