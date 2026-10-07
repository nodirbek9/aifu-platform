# ADR-017 — The midterm is a course quiz bound to ON, opened in term week 8, gating later lessons

**Status:** Accepted (2026-10-07, phase 5)

## Context
Owner: an oraliq nazorat (midterm) in week 8 — 30 questions from the topics covered so far,
40 minutes, 60 %, two attempts; until it is passed, later lessons stay locked.
Facts in the code: grading components are per course in `assessment_components`
(`JN` = CONTINUOUS, `ON` = MIDTERM, `YaN` = FINAL; V005, seeds V901/V904). A graded QUIZ activity
bound to a component already becomes a `grade_entry` through `GradeEntryService.onQuizAttemptGraded`.
Lessons carry no week number; the only calendar anchor is `terms.starts_on` (and
`terms.first_week_parity`, V019) via `courses.term_id`. `activities.available_from` exists.

## Decision
- **Model:** one QUIZ (`code = <COURSE>-ON`) + one activity with `path_step = 'MIDTERM'`,
  `is_graded = true`, `assessment_component_id` = the course's `MIDTERM`-kind component, placed at
  module level (`module_id` of the anchor lesson, `lesson_id` NULL). Its grade reaches ON through
  the existing path; nothing new in `grading`.
- **Where it sits in the chain:** `course_learning_paths.midterm_after_lesson_id` (the anchor) —
  the last chain lesson the midterm covers. Chain lessons after the anchor need the midterm passed.
  Explicit, because content order is not tied to calendar weeks and syllabi differ per course.
- **Week 8:** week 1 is the Monday-based week containing `terms.starts_on`; the midterm opens at
  Monday of week 8, 00:00 Asia/Tashkent, i.e. monday(starts_on) + 49 days.
  Stored as `course_learning_paths.midterm_opens_at`, filled with that default when the admin
  configures the path and editable (holidays, a late term start). Also copied into the
  activity's `available_from` so existing "not yet available" UI works.
- **Coverage ("topics so far"):** the midterm bank is written from the chain lessons up to the
  anchor (phase 6 content rule; each question carries its source lesson code in the content file
  for review, not in the DB).
- **Availability:** the midterm does not require the student to have finished the lessons before
  it — it is a scheduled exam; it requires only `now ≥ midterm_opens_at`. No closing date.
- **Attempts/grants:** as ADR-016 (max 2, admin grant +1, audited).

## Consequences
- A fast student who reaches the anchor before week 8 waits for the midterm; the plan view says
  when it opens.
- A student who fails twice stays locked after the anchor until an admin grants an attempt —
  intended by the owner; the admin dashboard lists such students (phase 7).
- One midterm per course. A second checkpoint (e.g. week 15) would need a child table
  `course_path_checkpoints`; not built now (YAGNI).

## Revisit when
The university fixes midterm dates centrally (then read them from the academic calendar), or a
course needs two checkpoints.
