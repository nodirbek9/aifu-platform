# ADR-012 — Progress consumes `AccessGuard` directly (a second reason to depend on `iam`)

**Status:** Accepted

## Context
Phase 6 adds `GET /teacher/courses/{id}/progress`: a teacher reads the progress roster of a
course. Per ADR-004 Layer 2, "is this caller a teacher *of this course*" is an ownership
question and must go through the one centralized guard, `iam.app.access.AccessGuard` — never a
bare `@PreAuthorize` permission check (a teacher would then read any course's roster, not just
their own) and never a hand-rolled `course_teachers` lookup duplicated inside `progress`.

`docs/architecture/README.md`'s module map deliberately does **not** draw `pr --> iam`. Its
prose gives one specific, narrow reason `ac`/`cu`/`en` point at `iam`: each *implements* one of
`AccessGuard`'s ownership ports (`CourseTeacherLookup`, `CurriculumLocator`, `EnrollmentLookup`)
so `AccessGuard` can resolve ownership without depending downward on them — and it says not to
add a general `iam` dependency for anything else. `progress` implements no such port; it just
needs to *call* the guard.

## Options
1. Skip `AccessGuard`; gate the endpoint with `@PreAuthorize("hasAuthority('PROGRESS_COURSE_READ')")`
   alone. Rejected — violates CLAUDE.md §7.2 (authorization is server-side, centralized, plus an
   ownership guard; permission alone is coarse-only and would let any teacher read any course's
   roster).
2. Duplicate a minimal "is this user a teacher of this course" query inside `progress`. Rejected
   — a second, unowned copy of a check `academics`/`iam` already own; the two would drift the
   moment `CourseTeacherLookup` changes shape.
3. **Add `progress -> iam` and call `AccessGuard.requireCourseEditor` directly**, as a plain
   consumer, not a port implementer.

## Decision
**Option 3.** `progress` (its `api`/`app` layers) may import `iam.app.access.AccessGuard` and
its `CourseAccess` result type. This is a *second, distinct* category of `-> iam` edge alongside
the existing "implements an ownership port" category:

- **Implements a port** (`academics`, `curriculum`, `enrollment`): `AccessGuard` depends on
  them through an `Optional<Port>`, not the reverse dependency direction shown by the arrow.
- **Consumes the guard** (`progress`, and expect `assessment`/`coding`/`grading` to need the
  identical arrow in their own phases, for their own teacher-facing, course-scoped endpoints):
  a straightforward `AccessGuard` → forward dependency, same as any other bean.

Both are legitimate reasons to draw `-> iam`. What stays disallowed is a *third* kind — reaching
into `iam` for anything other than these two — which is exactly what the module map's existing
prose was guarding against and continues to guard against.

The student-facing endpoints in this phase (`POST /lessons/{id}/progress`,
`GET /courses/{id}/progress/me`) do **not** need `AccessGuard`: ownership there is "the caller's
own active enrollment for this course", answered entirely inside `progress`'s already-allowed
`enrollment` + `curriculum` dependencies (ADR-004 Layers 3/4 — the query is scoped by the
caller's own `enrollment_id`, never load-then-check). Only the teacher roster endpoint needs the
cross-role ownership resolution `AccessGuard` provides.

## Consequences
`docs/architecture/README.md`'s diagram gains `pr --> iam` and its prose gains the second-category
explanation above. `ArchitectureBoundaryTest`'s `ALLOWED` map gains `"iam"` in `progress`'s set.
No cycle is introduced: `iam` still does not depend on `progress`. The next module that needs a
teacher-facing, course-scoped endpoint (`assessment`, `coding`, or `grading`) should follow this
same precedent rather than reopening the question.
