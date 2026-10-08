# ADR-018 — Rolling the learning path out without losing data or locking anyone out

**Status:** Accepted (2026-10-07, phase 5)

## Context
Students already have `lesson_progress` rows (COMPLETED via the old button), `activity_progress`
rows and quiz attempts made under `max_attempts = 3`. Only ~26 of 207 lessons have a test and ~51
a task; the rest arrive during phase 6. Switching the gate on globally the day it is deployed would
(a) lock students out of lessons they are already in, and (b) gate on steps that have no content.

## Decision
1. **Per-course switch.** `course_learning_paths.enabled` (default false, no row = disabled).
   An admin enables a course with `PUT /api/v1/admin/courses/{id}/learning-path` once its phase-6
   content batch has passed verification. Disabled course = today's behaviour exactly.
2. **Waivers at enable time, in the same transaction:** for every ACTIVE enrollment of the course,
   the *frontier* is the furthest chain lesson (chain order) that has a `lesson_progress` row or a
   graded attempt/submission on one of its activities. Every chain lesson up to and including the
   frontier gets `lesson_progress.path_waived_at = now()` (row created if missing). A waived lesson
   is open and counts as done for unlocking; its circles still show the real step state, so the
   student can still finish it. Re-enabling is idempotent (only NULLs are filled).
3. **Legacy completions:** a `lesson_progress` row that is COMPLETED before the migration gets
   `read_completed_at = completed_at` (V032), so its lecture circle is green.
4. **Attempts:** nothing is updated or deleted in `quiz_attempts`; V033 lowers `max_attempts` to 2
   only on TEST/MIDTERM quizzes. Students over the new limit keep their history and, thanks to (2),
   their lessons.
5. **Missing steps are satisfied:** a lesson without a published TEST or PRACTICE activity does not
   wait for it. When content is added later to an enabled course, lessons a student has already
   completed stay COMPLETED (status is never downgraded).
6. **Audit:** enable/disable and the count of waivers written go to `audit_log`
   (`LEARNING_PATH_ENABLED` / `LEARNING_PATH_DISABLED`).

## Consequences
- No data migration rewrites history; every new column is nullable or defaulted.
- Enabling is a short, single-course transaction (~200 enrollments × ≤ 50 lessons ≈ 10k rows max,
  one `INSERT … ON CONFLICT` + one `UPDATE`).
- A student who opened lesson 10 before enabling but skipped lesson 4's test is not forced back;
  that is the price of not locking anyone out, and the owner accepted "nobody loses access".

## Revisit when
A new term starts: courses of the new term can be enabled before any student opens a lesson,
so no waivers are written and the full rule applies from day one.
