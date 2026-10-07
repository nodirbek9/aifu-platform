# PLAN — phases 4–8 (2026-10-07)

Master plan for phases 4–8 of `docs/planning/phases-2026-10-07.md`. Backend rules: `CLAUDE.md`
(binding). Frontend rules: `AGENTS.md`. Major decisions: ADR-015..018 in `docs/decisions/`.
Workers implement one sub-step per job, run the listed test, and do not change a decision here;
if one looks wrong, report it.

## 0. Facts established from the code (what is true today)

- **Lock** is display-only: `progress.app.StudentWorkService.plan` sets `PlanLesson.locked` from
  `lesson_prerequisites` (not populated by content). `GET /api/v1/lessons/{id}`
  (`curriculum.api.LessonController`) only checks course membership. `frontend/src/features/lesson/lesson-page.tsx`
  fetches the content and then hides it if the plan says locked.
- **Progress** (V008): `lesson_progress` (status, view_count, time_spent_seconds, completed_at),
  `activity_progress` (status COMPLETED when an attempt passed, attempts_used, best_score),
  module/course roll-ups recomputed from source. "Done" today = student pressed *Mark done*.
  No reading-time measurement, no heartbeat endpoint; `additionalTimeSpentSeconds` is client-supplied.
- **Attempts** (V009, `assessment.app.AttemptService`): append-only `quiz_attempts`, contiguous
  `attempt_number` (unique index + service check vs `quizzes.max_attempts`), one open attempt
  (partial unique index), `Idempotency-Key` on submit, lazy expiry. Random subset already exists
  (`question_pool_size`), stored per attempt in `quiz_attempt_questions` (reload cannot reroll).
  No "avoid previously served questions" rule. Seeds/generator set `max_attempts = 3`.
- **Events:** `QuizAttemptGradedEvent` → `ProgressService` (activity_progress) and
  `GradeEntryService` (grade_entry, latest attempt supersedes). Coding: worker writes submissions,
  `ProgressService.syncCodingSubmission` polls every 10 s (verdict ACCEPTED ⇒ passed).
- **Grading components:** `JN` CONTINUOUS, `ON` MIDTERM, `YaN` FINAL per course
  (`assessment_components`, V005). A graded activity bound to ON already produces ON grades.
- **Week:** no lesson→week mapping; only `terms.starts_on`, `terms.first_week_parity`.
- **MT lessons:** `session_type = 'INDEPENDENT_STUDY'`, codes `MT*` (65 of 207 lessons).
- **Cross-module ports** live in `iam.app.access` (`CurriculumLocator`, …) and are implemented by
  the owning module — the gate follows this pattern. `ErrorCode.LESSON_PREREQUISITE_NOT_MET` and
  `ACTIVITY_NOT_AVAILABLE` exist (422) and are translated in the frontend.
- **Content:** lessons in `content/<COURSE>/<LESSON>/{uz,ru,en}.md` (import.mjs). Quizzes/tasks so
  far come from the dev seed generator `tools/content-seed` (V906/V907) and only their texts move
  through `tools/content/practice.mjs`; there is no production path that *creates* quizzes/tasks.
- **Admin backend** is broad (users, roles, status, import, groups CRUD + members, timetables,
  entries, exceptions, check, load, enrollments incl. by-group/CSV/status). Gaps are mostly UI.
- Migrations: V001–V027 applied; dev seeds V900–V911. **Phase 5 reserves V028–V030, phase 8 V031**;
  if numbers collide at merge time, take the next free number and keep the order.

## 1. Decisions (one-line reasons)

| # | Decision | Reason |
|---|---|---|
| D1 | One `progress.app.LearningPathService` owns steps, chain and unlock rule; exposed as port `iam.app.access.LessonGate` (ADR-015) | progress already depends on every source module; one rule, no drift, no new ArchUnit edge |
| D2 | Gate enforced at lesson GET, reading heartbeat, quiz start, coding submit and code run; only for enrolled callers of path-enabled courses | these are all the ways to consume a locked lesson; teachers preview freely |
| D3 | Refusal = existing `LESSON_PREREQUISITE_NOT_MET` / `ACTIVITY_NOT_AVAILABLE` with `details.reason` | ErrorCode is a frozen contract; additive details are free |
| D4 | Chain = published required non-INDEPENDENT_STUDY lessons by (module.position, position); MT outside the chain, always open | MT at the course end must never block; generic rule, no subject names |
| D5 | Steps: lecture = server-measured reading; test = `path_step TEST` activity COMPLETED; practice = `path_step PRACTICE` activity COMPLETED; missing step = satisfied; no order inside a lesson | reuses activity_progress; content gaps must not lock students |
| D6 | `activities.path_step` (TEST/PRACTICE/MIDTERM) instead of guessing by type | a lesson may hold several quizzes; theory practice is a quiz too |
| D7 | Reading: 15 s heartbeats, server clock credits ≤ 30 s, ≥ 10 s apart, one lesson per enrollment at a time; green = end reached + ≥ 50 % of estimate (ADR-015) | client can fake beats but not wall-clock time |
| D8 | Estimate = `lessons.estimated_minutes` else server word count / 200 wpm (min 2); snapshot on first beat | server must not trust the client's estimate; goalposts don't move mid-read |
| D9 | Bank = quiz's own questions; draw prefers unseen questions; stored per attempt (ADR-016) | everything but the "unseen" rule exists |
| D10 | Allowance = `max_attempts` + rows in append-only `quiz_attempt_grants`; DB trigger + advisory lock | invariant at DTO, service and DB as CLAUDE.md §8 requires |
| D11 | Midterm = course quiz `<COURSE>-ON`, activity `path_step MIDTERM` bound to ON; anchor lesson + `midterm_opens_at` in `course_learning_paths` (ADR-017) | grades flow through existing ON path; explicit anchor beats guessing weeks |
| D12 | Week 1 = Monday-week containing `terms.starts_on`; midterm opens Monday of week 8 00:00 Asia/Tashkent; admin may edit | only calendar anchor that exists |
| D13 | Per-course enable switch + waivers up to each student's frontier at enable time (ADR-018) | nobody loses access; old courses keep old behaviour until content is ready |
| D14 | API additive under /api/v1, DTO records, springdoc example on every new endpoint, regenerate `openapi.json` + `ENDPOINTS.md` + frontend `schema.d.ts` in the same sub-step | CLAUDE.md §9, OpenApiContractIT |
| D15 | Content as repo files `content/<COURSE>/<LESSON>/assess.json` (+ `content/<COURSE>/midterm.json`), imported through the admin API by stable codes | same review/import model as lessons; works on prod and dev |
| D16 | Uzbek apostrophe fix = content-file fix + one idempotent data migration on uz translations | prod data was imported from those files; seeds stay untouched |
| D17 | Starter code carries no natural-language comments | starter code is not localized; adding a per-locale starter is not worth a schema change |

## 2. Phase 4 — Lesson mode design (frontend only) — risk: low

**Goal:** lesson page follows light/dark theme, readable sans body text, soft colours, distinct
code blocks, nothing hidden behind the bottom bar, on desktop and phone.
**Files:** `frontend/src/shared/ui/tokens.css`, `index.css`, `shared/ui/rich-text.tsx`,
`shared/ui/code-block.tsx`, `features/lesson/lesson-page.tsx`, `lesson-blocks.tsx`, `lesson-toc.tsx`,
`app/theme.tsx`, `features/lesson/present*` must keep projector mode unchanged.

- **4.1 Find the navy source.** Open a lesson in light theme (Chrome), inspect computed background;
  grep for `.dark` forced on lesson/present routes, `brand-navy`, `bg-[`. Write the cause in the
  PR description. Fix so the lesson root uses `bg-surface`/`text-text` tokens only.
- **4.2 Reading typography tokens.** In `tokens.css` change `--font-reading` to the sans stack
  (`'Onest Variable', system-ui, sans-serif`); add `--text-reading` 1.0625rem/1.75 line-height;
  `max-w-lesson` stays 42.5rem. Headings `font-display` at most `text-display-md` on phone,
  `display-lg` on desktop (h1 currently `display-xl`).
- **4.3 Soft palette.** Add light/dark tokens `--color-reading-surface`, `--color-reading-text`
  (≈ #1f2937 light / #e5e7eb dark, contrast ≥ 7:1) and callout tints at ~8 % alpha; use them in
  `rich-text.tsx` and callout blocks. No hex in components (AGENTS.md).
- **4.4 Code look.** `code-block.tsx` and the example tabs in `lesson-blocks.tsx`: own token
  `--color-code-surface` (light: slate-50, dark: #0d1117-like), 1 px border, radius-lg, mono 0.875rem,
  line numbers optional, horizontal scroll, copy button; inline `code` gets a subtle pill. Same
  look in both themes, no patchy per-line backgrounds (remove any `bg-*` on `<span>` lines).
- **4.5 Spacing and bottom bar.** Article bottom padding `pb-[calc(theme(spacing.24)+env(safe-area-inset-bottom))]`
  when a fixed bottom nav/bar is present (phone); `scroll-margin-top` for headings under the
  sticky header; paragraph spacing 1em; lists indented.
- **4.6 Tests.** Vitest snapshot-free test that `RichText` renders with `font-reading`; Playwright
  `e2e/lesson-theme.spec.ts`: light and dark screenshot of one lesson (desktop + phone), axe has no
  colour-contrast violations, last paragraph's bounding box ends above the bottom bar.
**Acceptance:** lint, typecheck, vitest, the new e2e pass; manual check in 3 locales.

## 3. Phase 5 — Learning path — risk: high

### Backend (each B-step = migration/entity/DTO/test together, `./mvnw -pl api test`, ITs with `-Pit`)

- **B1 Migration V028 `learning_path`** (schema only + safe backfill):
  - `course_learning_paths(course_id PK→courses, enabled bool default false, enabled_at, enabled_by→users,
    midterm_activity_id→activities, midterm_after_lesson_id→lessons, midterm_opens_at timestamptz,
    min_reading_fraction numeric(3,2) default 0.50 CHECK 0..1, created_at, updated_at, version)`,
    CHECK midterm_activity_id and midterm_after_lesson_id both null or both set; index every FK; updated_at trigger.
  - `activities.path_step text CHECK IN ('TEST','PRACTICE','MIDTERM')`; CHECK TEST/PRACTICE ⇒ lesson_id NOT NULL;
    partial unique index `(lesson_id, path_step) WHERE path_step IN ('TEST','PRACTICE') AND status <> 'ARCHIVED'`.
    Backfill: first QUIZ by position per lesson → TEST; first CODING_TASK per lesson → PRACTICE.
  - `lesson_progress` add `active_reading_seconds int NOT NULL DEFAULT 0 CHECK ≥0`, `last_heartbeat_at`,
    `max_block_position int`, `reached_end_at`, `required_reading_seconds int`, `read_completed_at`,
    `path_waived_at`; index `(enrollment_id, last_heartbeat_at)`. Backfill `read_completed_at = completed_at`
    where status = 'COMPLETED'.
  - `quiz_attempt_grants(id, quiz_id→quizzes RESTRICT, enrollment_id→enrollments RESTRICT, granted_by→users,
    reason text NOT NULL CHECK length(btrim) ≥ 5, created_at)`; indexes; trigger forbidding UPDATE/DELETE.
  - Function `quiz_attempt_lock(quiz uuid, enr uuid)` = `pg_advisory_xact_lock(hashtextextended(quiz::text||enr::text,0))`;
    `BEFORE INSERT ON quiz_attempts` trigger: lock, require `attempt_number = coalesce(max,0)+1` and
    `≤ max_attempts + grants` when max_attempts not null, else raise `check_violation`.
  - Permission `QUIZ_ATTEMPT_GRANT` and `LEARNING_PATH_MANAGE` → ADMIN (pattern of V022).
  - Test: Flyway runs in `AbstractIntegrationTest`; `LearningPathSchemaIT` (trigger rejects 3rd attempt
    when max 2, accepts after a grant; grant row cannot be updated).
- **B2 Entities + repositories.** `progress.domain.CourseLearningPath`, new fields on `LessonProgress`
  (methods `creditReading(now, maxCredit, minGap)`, `markReachedEnd`, `waive`, `markReadCompleted`),
  `curriculum.domain.PathStep` enum + `Activity.pathStep`, `assessment.domain.QuizAttemptGrant`.
  Unit tests for `LessonProgress.creditReading` (gap < 10 s → 0, cap 30 s, first beat → 0).
- **B3 `curriculum.app.ReadingTimeEstimator`** (`estimatedSeconds(lessonId, locale)`; words of student-visible
  blocks' `plain_text` / 200 wpm, min 120 s; `estimated_minutes` wins) + `LessonService.lastVisibleBlockPosition`.
  Unit test with fixed text.
- **B4 `progress.app.LearningPathService` — read model.** `chainOf(courseId)`, `pathState(enrollmentId)`
  → per lesson: steps {LECTURE, TEST, PRACTICE} each `DONE|TODO|NONE`, `done`, `open`, `lockReason`,
  `blockingLessonId`; midterm {activityId, opensAt, passed, attemptsUsed, attemptsAllowed}. Max 4 queries
  per call (outline, lesson_progress by enrollment, activity_progress by enrollment, path config).
  Unit tests (pure function `PathRules.evaluate(...)` over in-memory inputs): MT never locked and never
  blocking; missing step satisfied; waiver opens; midterm anchor; explicit prerequisites.
- **B5 Gate port + enforcement.** `iam.app.access.LessonGate { void requireLessonOpen(UUID lessonId, UUID enrollmentId);
  void requireActivityOpen(UUID activityId, UUID enrollmentId); }`, adapter in progress;
  `AccessGuard.requireOpenLesson(lessonId)` (viewer check + gate when enrollment present).
  Call sites: `LessonController.get`, `AttemptService.doStart` (resolved activity; midterm → `opensAt` check),
  `SubmissionService.create`, the code-run create service (`coding`/`execution`, by task's activity).
  Register nothing new in `ArchitectureBoundaryTest` (verify it passes). IT `LearningPathGateIT`:
  deep link to lesson 2 → 422 `LESSON_PREREQUISITE_NOT_MET` with `details.reason`; quiz start / submission /
  run on lesson 2 activities → 422; teacher → 200; disabled course → 200; MT lesson → 200.
- **B6 Reading heartbeat.** `POST /api/v1/lessons/{lessonId}/reading` body `ReadingBeatRequest(@Min(0) int maxBlockPosition, boolean atEnd)`
  → `LessonPathView`. Service: gate, load-or-create lesson_progress, snapshot required seconds on first beat,
  credit (server `Clock`), cross-lesson rule (any other lesson of this enrollment with `last_heartbeat_at > now-10s`
  ⇒ no credit), accept atEnd only at last visible block, then `reevaluateLesson`. Optimistic retry as in
  `ProgressService.retrying`. Old `POST /lessons/{id}/progress` with COMPLETED on an enabled course → `VALIDATION_FAILED`.
  IT with a mutable test `Clock`: beats every 15 s reach green only after required time AND atEnd;
  beats every 1 s credit nothing extra; two lessons in parallel share time.
- **B7 Lesson completion + roll-ups.** `LearningPathService.reevaluateLesson(enrollment, lesson)`: if all present
  steps DONE and status ≠ COMPLETED ⇒ `recordAccess(now, true, 0)` + module/course recompute (reuse
  `ProgressService` private methods by moving them to a package-private `RollupWriter`). Called from heartbeat,
  `doRecordActivityAttempt`, `doSyncCodingSubmission` (when the activity has a lesson). Never downgrades.
  IT: pass test + ACCEPTED submission + reading ⇒ lesson COMPLETED and next lesson opens.
- **B8 Question draw.** `AttemptService.serveQuestions`: unseen-first draw with `SecureRandom`, fill by
  least-recently served (`QuizAttemptQuestionRepository.servedHistory(quizId, enrollmentId)`).
  Unit test: bank 10, pool 5 ⇒ attempt 2 shares 0 questions with attempt 1; bank 7 ⇒ overlap 3, least recent.
- **B9 Attempt allowance + grants.** `AttemptService.doStart` uses `allowance = max + grants` (catch the trigger's
  `check_violation` → `ATTEMPT_LIMIT_REACHED`). `assessment.app.AttemptGrantService.grant(quizId, enrollmentId, reason, adminId)`
  under the advisory lock (native query `SELECT quiz_attempt_lock(...)`), rules of ADR-016, `AuditService.record(QUIZ_ATTEMPT_GRANTED, …)`.
  Endpoints: `POST /api/v1/admin/quizzes/{quizId}/enrollments/{enrollmentId}/attempt-grants` (201 `AttemptGrantView`),
  `GET /api/v1/admin/attempt-grants?courseId=&page=&size=` (PageResponse). Reveal-policy check uses allowance.
  Student quiz DTO + `attempts/me` gain `attemptsAllowed`, `attemptsUsed`. IT: double grant → 409
  `INVALID_STATE_TRANSITION`; grant when attempts left → 422; grant then start → attempt 3 ok, attempt 4 → 422.
- **B10 Path config + enable with waivers.** `GET|PUT /api/v1/admin/courses/{courseId}/learning-path`
  (`LearningPathConfigRequest(boolean enabled, UUID midtermActivityId, UUID midtermAfterLessonId, Instant midtermOpensAt)`);
  validates midterm activity is a QUIZ with path_step MIDTERM in this course and anchor is a chain lesson;
  default `midtermOpensAt` = monday(term.starts_on)+49 d 00:00 Asia/Tashkent; copies it to the activity's
  `available_from`; on enable writes waivers per ADR-018 with one `INSERT … ON CONFLICT DO NOTHING` +
  one `UPDATE … SET path_waived_at = now() WHERE path_waived_at IS NULL`; audit. IT: student who opened
  lesson 5 keeps lessons 1–5 open after enable; lesson 6 locked until 5 done.
- **B11 Read API for the UI.** Extend `PlanLesson` additively: `steps` (`LessonStepsView{lecture,test,practice}`),
  `lockReason`, `waived`; `CoursePlanView.midterm` (`MidtermView{activityId, quizId, opensAt, afterLessonId,
  passed, attemptsUsed, attemptsAllowed}`); `locked` now comes from the gate when enabled.
  `GET /api/v1/lessons/{lessonId}/path` → `LessonPathView{steps, activeReadingSeconds, requiredReadingSeconds,
  reachedEnd, testActivityId, practiceActivityId, open, lockReason}`. springdoc examples on all.
- **B12 Data migration V029 `learning_path_quiz_settings`.** For quizzes behind a TEST activity:
  `max_attempts = 2`, `question_pool_size = LEAST(5, count)` only where count ≥ 5, `passing_percent = 60`,
  `reveal_policy = 'AFTER_LAST_ATTEMPT'`; behind PRACTICE quiz: `max_attempts = NULL`. Update the seed
  generator (`tools/content-seed/generate.mjs`: 3 → 2) and regenerate V906 only if V906 is not yet applied anywhere
  (it is — so instead add dev seed `V912__dev_seed_path_steps.sql` setting path_step/limits on dev content).
- **B13 Contract + docs.** Regenerate `docs/api/openapi.json` (OpenApiContractIT -Dopenapi.write=true),
  `ENDPOINTS.md`, `frontend/src/shared/api/schema.d.ts`; `docs/03-API-GUIDE.md` §2 add the `details.reason`
  values; CLAUDE.md §8 row "≤ max_attempts + grants"; `docs/decisions/README.md` rows 012–018;
  `StudentDataIsolationIT` case for `/lessons/{id}/path` and heartbeat. (V030 is left free for fixes found here.)

### Frontend (`frontend/`, AGENTS.md; run `npm run lint && npm run typecheck && npm test`)

- **F1 Model.** `features/courses/model.ts`: `PlanLesson.steps`, `lockReason`, `waived`; `CoursePlan.midterm`;
  `features/lesson/model.ts`: `LessonPath`. Unit tests for narrowing (absent fields ⇒ `NONE`).
- **F2 `StepDots` component** (`shared/ui/step-dots.tsx`): three circles (lecture/test/practice), DONE = success
  fill, TODO = outline, NONE = dashed muted, each with an aria-label and tooltip in uz/ru/en. Used in
  `course-plan-list.tsx` rows and the lesson header.
- **F3 Server lock.** Lesson page: on 422 `LESSON_PREREQUISITE_NOT_MET` render `LockedLesson` from
  `details` (no content fetched/shown); locked rows in the plan are not links; midterm card in the plan
  (opens at / attempts / start button). Remove reliance on client-only `locked`.
- **F4 Reading tracker** `features/lesson/use-reading-heartbeat.ts`: every 15 s while
  `document.visibilityState === 'visible'` and last input/scroll < 120 s; IntersectionObserver on a sentinel
  after the last block sets `atEnd`; tracks `maxBlockPosition` via `data-block-position`; stops when lecture is
  DONE; updates the lecture ring ("12 / 18 min"). Replace the *Mark done* button with the steps panel.
  Vitest with fake timers.
- **F5 Quiz.** `quiz-start.tsx`: "attempt 1 of 2", no-attempts state ("ask the dean's office" text),
  midterm variant (30 questions, 40 min). After submit, invalidate plan + lesson path and refetch once after 2 s.
- **F6 Practice section** ("Mavzuni mustahkamlash uchun topshiriqlar"): shows PRACTICE task (code or
  checked-answer quiz) with its step state; coding page refetches the path after a verdict (poll 5 s × 3).
- **F7 Admin grant + path config.** Course admin page: "Learning path" card (enable switch with confirm
  dialog showing waiver count, midterm quiz + anchor lesson pickers, opens-at date); student row and quiz
  attempts view: "Give one more attempt" dialog (reason required) → POST grant; grants list.
- **F8 i18n + e2e.** All strings in uz/ru/en (`npm run i18n:check` if present). Playwright
  `e2e/learning-path.spec.ts`: deep link to locked lesson shows lock and no text; read (test clock via
  short estimated_minutes fixture), pass test, solve practice ⇒ next lesson opens; admin grant flow.

**Acceptance (phase 5):** `./mvnw clean verify -Pit` green incl. ArchUnit and OpenApiContractIT; frontend
lint/typecheck/vitest/e2e green; manual: student cannot open lesson N+1 by URL or by calling the API
with curl; MT lessons stay open; enabling a course mid-term locks no lesson a student already opened.

## 4. Phase 6 — Content: test banks and practice — risk: medium (volume)

**Goal:** every one of the 207 lessons has a TEST bank (8–10 questions, uz/ru/en) and one PRACTICE task;
a midterm bank per course; all imported and verified. Order: DAS1110-S1 (27), DAS1110-S2 (23), MTA1204 (17),
KOT1306 (32), KIA1406 (47), IAKTT1105 (61).

- **C0 Format + tooling (one job, Gemini Flash):**
  - `content/<COURSE>/<LESSON>/assess.json`: `{ test: { questions: [ {type: SINGLE|MULTI|TF|SHORT|NUM,
    difficulty, points, prompt:{uz,ru,en}, options:[{uz,ru,en}], correct:[i], answers:[…], tolerance,
    explanation:{uz,ru,en}} ] }, practice: { kind: CODE|ANSWER, title, statement:{uz,ru,en},
    code?: {starter:{python,java,cpp}, solution:{python,java,cpp}, tests:[{input, expected, hidden}]},
    answer?: { questions:[…same shape…] } } }`. `content/<COURSE>/midterm.json`: `{afterLesson, questions:[…, lesson]}`.
  - `tools/content/assess.mjs check <COURSE> [--lesson X]` (no network): schema, 8–10 questions, ≥ 2 per
    difficulty band, every locale non-empty, correct indices valid, ≥ 3 tests with ≥ 2 hidden, starter has no
    natural-language comments, uz text has no ASCII `'` after o/g/O/G (must be ʻ U+02BB) and uses ʼ U+02BC elsewhere.
  - `tools/content/assess.mjs verify <COURSE>`: runs solutions in the sandbox images (reuse
    `tools/content-seed/verify-solutions.mjs` runner) — all 3 solutions pass every test; the starter fails at least one.
  - `tools/content/assess.mjs import <COURSE> [--dry-run]`: upsert by codes `<COURSE>-<LESSON>-T`,
    `<COURSE>-<LESSON>-P`, `<COURSE>-ON` through admin APIs, activity `path_step`, settings per ADR-016;
    TEST/PRACTICE activities are ungraded unless the course scheme says otherwise (open question Q4).
  - Update `docs/06-CONTENT-GUIDE.md` §7 with the format and rules.
- **C1..Cn Batches (one job each, Qwen Flash writes, Gemini Flash fixes failures):** ≤ 10 lessons per batch,
  ~23 batches. Input: the lesson's `uz.md` (+ ru/en) and `examples.json`. Rule: lesson has runnable code
  examples and the course is programming-oriented ⇒ `kind: CODE`; otherwise `kind: ANSWER` (numeric,
  short answer, or a case with single-choice questions). Questions only on what the lesson text says.
- **Per batch verification (Claude runs, mechanical):** `assess.mjs check` → `assess.mjs verify` →
  `import --dry-run` against dev → `import` → `GET` counts. A batch is done only when all four are green.
  Then a Gemini Pro reviewer samples 2 lessons per batch for factual/translation errors.
- **Midterm (one job per course):** 60 questions from chain lessons up to the anchor (default: the last
  chain lesson whose syllabus week ≤ 7, i.e. roughly the first 7 lectures + their practicals — the batch job
  proposes the anchor, Claude confirms), check + import, then `PUT …/learning-path` with the anchor.
- **Enable:** after a course's batches and midterm are imported, enable its path (B10) in dev, run e2e smoke.
**Acceptance:** `assess.mjs check` and `verify` green for all six courses; counts: 207 TEST quizzes with
≥ 8 questions, 207 PRACTICE activities, 6 midterm quizzes with ≥ 60 questions; spot review logged.

## 5. Phase 7 — Admin powers — risk: low/medium

**Already present (do not rebuild):** user search/create/edit/roles/status/reset/unlock/import
(`features/admin/users-page`, `user-page`, `import-page`); groups CRUD, archive, add/remove students
(`reference-page`); timetable builder with add/edit/move/delete class, exceptions, conflict check, load
(`features/builder`); course teachers, components, enrollments bulk/by-group/CSV (`course-admin-page`);
dashboard counts + needs-attention (`admin-dashboard-page`); audit log.
**Real gaps (verify each in the browser first — step 7.0 — and drop any that turn out to exist):**
- **7.1 Student card:** `user-page.tsx` shows no group and no courses. Add group (change group via
  `POST /admin/groups/{id}/students`), enrollments list (`GET /admin/enrollments?userId=` — backend: add the
  optional `userId` filter, additive) with status change (`PUT /admin/enrollments/{id}/status`, reason).
- **7.2 Teachers view:** users page with role=TEACHER preset + per-teacher courses and weekly load
  (`/admin/timetables/{id}/load?teacherId=`) — a tab, not a new backend.
- **7.3 Remove student:** "Deactivate" (status) + "Withdraw from course" (enrollment status) in one
  confirm flow; hard delete stays forbidden (append-only history).
- **7.4 Problems page** `/admin/problems`: students without group, groups without published timetable
  entries, courses without teacher, active enrollments of inactive users, locked accounts, students with
  attempts exhausted and midterm not passed (from phase 5) — each row with a fix action link.
  Backend: `GET /api/v1/admin/problems?type=&page=&size=` (read-only queries, paginated, in `analytics`
  or each owning module via app services; no cross-module persistence).
- **7.5 Single class add from the week view:** builder already adds; confirm a one-off class
  (exception type ADD) exists for a single date; if not, add the option to the exceptions dialog.
**Acceptance:** each gap has an e2e step in `e2e/admin-builder.spec.ts` or a new `admin-people.spec.ts`;
backend ITs for new filters and the problems endpoint (admin-only, 403 for teacher).

## 6. Phase 8 — Small defects — risk: low

- **8.1 Uzbek apostrophes:** script `tools/content/fix-apostrophes.mjs` rewrites `content/**/uz.md`,
  `practice.uz.json`, `assess.json` uz fields: `([OoGg])['‘’`]` → `$1ʻ`, other letter-`'`-letter → `ʼ`; run, review
  diff. Migration `V031__uz_apostrophes.sql`: same regexp on uz rows of `lesson_translations`,
  `course_module_translations`, `course_translations`, `subject_translations`, `quiz_translations`,
  `coding_task_translations` (idempotent). `import.mjs` check rejects ASCII apostrophes in uz.
- **8.2 Truncated course names on timetable cards:** `features/timetable/class-row.tsx`, `week-view.tsx`,
  builder cards: show course short title (subject short title) or wrap to two lines (`line-clamp-2`) with full
  title in a tooltip; never cut the term suffix into "1-...".
- **8.3 Latest grades shows code:** `features/dashboard/work-blocks.tsx` `RecentGradesBlock` /
  `courses/queries.ts useRecentGrades`: show the activity/component title (from the grade DTO's activity title;
  if the DTO lacks it, add `activityTitle` additively in `grading.api.dto.GradeDtos`) with course title as subtitle.
- **8.4 Starter codes:** list the 29 starters (`coding_task_languages.starter_code` via teacher API export);
  remove natural-language comments (keep signatures and `// TODO`-free skeletons); update
  `tools/content-seed/content/*.mjs` + `_cpp.mjs` and dev seed via a new `V913__dev_seed_neutral_starters.sql`;
  production via `assess.mjs import`. Verify C++ starters still compile (`verify-solutions.mjs`).
**Acceptance:** grep finds no `o'`/`g'` in uz content; e2e asserts full course name visible on a timetable card
(desktop) and activity title in latest grades; verify-solutions green.

## 7. Order and dependencies

1. **Phase 4** and **Phase 8** (independent, low risk) — can run in parallel with B1–B4.
2. **Phase 5 backend** B1 → B2 → B3 → B4 → B5 → B6 → B7 → B8 → B9 → B10 → B11 → B12 → B13 (strict).
3. **Phase 5 frontend** F1 after B13 (needs schema.d.ts); F2–F8 in order (F2 and F5 can run in parallel).
4. **Phase 6** C0 can start after B1 (needs `path_step` only for import); batches run during phase 5
   frontend work; import into dev after B12. Enabling paths per course only after B10 + its batches.
5. **Phase 7** after phase 5 (problems page uses grants/midterm data); 7.1–7.3 may start earlier.
6. Phase 9 (final check) after all.

## 8. Risks

| Risk | Level | Mitigation |
|---|---|---|
| Gate locks real students on rollout | high | per-course switch, waivers to frontier (ADR-018), IT for mid-term enable |
| Reading time too strict (slow readers fine, but estimates off for code-heavy lessons) | medium | 50 % fraction is a config column; estimator unit-tested; teacher can set `estimated_minutes` |
| Async step updates feel laggy (coding sync 10 s) | medium | UI refetch/poll after submit; consider lowering sync delay to 5 s if measured |
| Heartbeat load (200 × 4/min) | low | one row update per beat, no roll-up; index on (enrollment_id, last_heartbeat_at) |
| Content volume/quality (≈ 2 000 questions × 3 languages) | medium | mechanical check/verify gates per batch, sampled review, missing step = satisfied |
| Trigger vs service disagreement on attempt limit | low | same allowance query; trigger error mapped to `ATTEMPT_LIMIT_REACHED`; IT covers both |
| Search/file endpoints leak locked lesson text | low | out of scope now; listed for phase 9 review |
| Latest-attempt-supersedes lowers ON on a worse 2nd try | medium | owner decision Q2 |

## 9. Open questions for the owner (default applies if no answer)

- **Q1** Must the test wait until the lecture is green inside the same lesson? *Default: no order inside a lesson.*
- **Q2** Midterm and tests with two attempts: which score counts — best or last? *Default: best
  (change `GradeEntryService.recordOrSupersede` to supersede only with a higher score for QUIZ_ATTEMPT sources).*
- **Q3** Does the midterm close at some date (end of week 8 / 9)? *Default: no closing date.*
- **Q4** Do lesson tests/practice give JN points, or only unlock? *Default: unlock only (ungraded); existing
  graded activities keep their grading.*
- **Q5** Who may grant an extra attempt — admin only, or also the course teacher? *Default: admin only.*
- **Q6** A student who reaches the midterm anchor before week 8 waits. OK? *Default: yes.*

## Plan review (Gemini 3.1 Pro, 2026-10-07) — what was accepted

Accepted and to be built into phase 5:
- **Final heartbeat can be lost (review 8, 10):** "reached the end" is sticky on the server — the first heartbeat that says `atEnd=true` sets it, and any later heartbeat keeps it. The client also sends a last beat with `navigator.sendBeacon` on `pagehide`, so a short visit is not lost.
- **Refetch race after a quiz submit (7):** the plan screen refetches until the server's step state changes (bounded retries with backoff), not once after a fixed 2 seconds. The graded event is handled in the same transaction wherever the gate depends on it.
- **Lessons inserted after roll-out (4) and re-enabling a course (6):** the waiver is computed once per (enrollment, lesson) and stored; turning the gate off and on again never waives more, and a lesson added later is waived for students who are already past its position.
- **Midterm dates (5):** the midterm date stays admin-editable after the term dates change; a change is audited.
- **Late enrolment (9):** covered by the roll-out waiver rule; a student enrolled late starts with the lessons before the furthest reached one waived.

Rejected:
- Half the reading time is the owner's rule; it stays (1). The idle timeout of 120 s stays; a lesson with an embedded video counts as active while the player plays (2).
- Review 3 reads "MT" as the midterm; here MT means the self-study lessons. The midterm (ON) does lock the lessons after it, as the owner asked.

## Owner questions — defaults taken (the owner said "decide yourself")
Q1 no order between steps of one lesson; Q2 the best score counts (needs the change to `recordOrSupersede`); Q3 no closing date; Q4 lesson tests and practice only unlock (no JN points); Q5 admin only grants an extra attempt; Q6 yes, a fast student waits for week 8.
