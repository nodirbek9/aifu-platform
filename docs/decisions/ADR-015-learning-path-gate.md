# ADR-015 — One server-side learning-path gate, and reading time measured by the server

**Status:** Accepted (2026-10-07, phase 5 of `docs/planning/phases-2026-10-07.md`)

## Context
The owner wants each lesson to have three steps (lecture, test, practice) and the next lesson to
open only when all three are done. Today the "lock" is computed only for display:
`progress.app.StudentWorkService.plan` sets `PlanLesson.locked` from `lesson_prerequisites`
(which the content does not even populate), and `GET /api/v1/lessons/{id}`
(`curriculum.api.LessonController.get`) serves any published lesson to any enrolled student. A deep
link or an edited client opens a locked lesson. The lecture step is a client button
(`POST /lessons/{id}/progress {status: COMPLETED}`) that anyone can call at once.

## Options
1. Keep the lock in the frontend and hide links. Rejected: the API is the trust boundary (CLAUDE.md §7.2).
2. Each module checks "is the previous lesson done" on its own. Rejected: three copies of one rule drift.
3. **One `LearningPathService` in `progress` that owns the rule, exposed through a port
   `iam.app.access.LessonGate` (same pattern as `CurriculumLocator`), called at every entry point.**

## Decision
- **Owner:** `progress.app.LearningPathService` (progress already depends on curriculum,
  assessment, coding, enrollment — no new edge in `ArchitectureBoundaryTest`). Adapter
  `progress.app.LessonGateAdapter implements iam.app.access.LessonGate`.
- **Enforced at:** lesson content `GET /lessons/{id}` (via new `AccessGuard.requireOpenLesson`),
  reading heartbeat, quiz attempt start (`AttemptService.doStart`, by the activity's lesson),
  coding submission create and code run (by the task's activity lesson). Answer/submit of an
  already-started attempt are not re-gated (the attempt was legal when started).
- **Who is gated:** only a caller who has an enrollment in the course (`CourseAccess.enrollment()`
  present) and only when the course's path is enabled (`course_learning_paths.enabled`).
  Teachers/admins are never gated. A refused call returns the existing
  `LESSON_PREREQUISITE_NOT_MET` (422) with `details.reason` ∈ `PREVIOUS_LESSON_INCOMPLETE`,
  `MIDTERM_REQUIRED`, and `details.blockingLessonId` / `details.midtermActivityId`; a midterm before
  its opening time returns the existing `ACTIVITY_NOT_AVAILABLE`. No new `ErrorCode`.
- **Chain:** the course's published, required lessons with `session_type <> 'INDEPENDENT_STUDY'`,
  ordered by (module.position, lesson.position). Independent-study (MT) lessons have the same
  three steps but are outside the chain: always open, never blocking. Generic rule, no subject code.
- **Open(L):** L is outside the chain, or L is waived (ADR-018), or every earlier chain lesson is
  done, its explicit `lesson_prerequisites` are done, and — when L comes after the midterm anchor —
  the midterm is passed (ADR-017). **Done(L)** = waived, or `lesson_progress.status = COMPLETED`,
  which from now on is written only by the path service when every *present* step is green.
  A step whose activity does not exist yet counts as satisfied (content arrives in phase 6).
- **Steps:** lecture = `lesson_progress.read_completed_at`; test = the lesson's activity with
  `path_step = 'TEST'` has `activity_progress.status = COMPLETED` (quiz passed); practice =
  `path_step = 'PRACTICE'` activity COMPLETED (coding verdict ACCEPTED on all tests incl. hidden,
  or a checked-answer quiz passed). Within a lesson the steps have no order.

## Reading time (anti-cheat)
- Client sends `POST /api/v1/lessons/{id}/reading {maxBlockPosition, atEnd}` every 15 s only
  while the tab is visible and the user was active in the last 120 s.
- The server credits `min(now − last_heartbeat_at, 30 s)` using **its own clock**; a beat less
  than 10 s after the previous one, or while another lesson of the same enrollment received a
  credited beat in the last 10 s, credits nothing. A script can send beats but cannot compress
  wall-clock time, and parallel tabs/lessons share one clock.
- Required time = ⌈estimated seconds × 0.5⌉, snapshotted into `lesson_progress.required_reading_seconds`
  on the first beat. Estimated minutes = `lessons.estimated_minutes`, else a server
  `ReadingTimeEstimator` (words of the locale's `content_block_translations.plain_text` / 200 wpm,
  minimum 2 min).
- "Read to the end": `atEnd = true` is accepted only with `maxBlockPosition` equal to the last
  student-visible block; it sets `reached_end_at` once. Lecture is green when `reached_end_at` is
  set **and** `active_reading_seconds ≥ required_reading_seconds`.
- The old `POST /lessons/{id}/progress {status: COMPLETED}` is rejected with `VALIDATION_FAILED`
  for a path-enabled course; `IN_PROGRESS` keeps working.

## Consequences
- Every gated request costs ~3 small indexed queries (chain outline, lesson_progress and
  activity_progress of one enrollment). Fine at 200 students; cache only if measured.
- Quiz/coding steps turn green asynchronously (AFTER_COMMIT event / 10 s submission sync);
  the UI re-polls the path view after a submit.
- The heartbeat touches only `lesson_progress` (optimistic lock + the existing retry loop);
  roll-ups are recomputed only when a lesson becomes COMPLETED.

## Revisit when
Teachers want to define their own unlock order, or the platform runs more than one API instance
(the 10 s cross-lesson check is a DB read, so it already works across instances).
