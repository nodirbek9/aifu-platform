# ADR-013 — Split `CurriculumLocator` into one port per owning module

**Status:** Accepted

## Context
`iam.app.access.CurriculumLocator`'s own javadoc, written in Phase 5, already flagged this:
"implemented in `curriculum`, `assessment` and `coding` adapters." `AccessGuard` injects it as a
single `Optional<CurriculumLocator> curriculumLocator` field. Spring's `Optional<T>` constructor
injection resolves at most **one** matching bean — if a second `@Component implements
CurriculumLocator` is ever registered (exactly what building `assessment` requires, per that same
javadoc), the application context fails to start with `NoUniqueBeanDefinitionException`. Phase 5's
`CurriculumLocatorAdapter` javadoc anticipated this precisely: "whoever builds those modules next
must revisit this single-adapter design (most likely splitting `CurriculumLocator` into per-module
ports) — not a problem to solve speculatively now." Phase 7 (`assessment`) is that next module.

The deeper reason a single adapter never worked: `courseIdOfQuiz`/`courseIdOfCodingTask` ask "what
course owns this quiz/coding task", but `curriculum` (the module that would own the adapter bean)
has no `quizzes`/`coding_tasks` tables to query — only `assessment`/`coding` do, and neither may
depend backward on `curriculum` owning their lookup (that would be `assessment -> curriculum ->
assessment`-shaped, forbidden by the module map's DAG requirement in the first place — `assessment
-> curriculum` is the real, and only, allowed direction).

## Decision
Split the one interface into three, one per owning module, each with a single implementer:

- `CurriculumLocator` — `courseIdOfModule`, `courseIdOfLesson`, `courseIdOfActivity`. Implemented
  only by `curriculum.app.CurriculumLocatorAdapter` (its `courseIdOfQuiz`/`courseIdOfCodingTask`
  stubs and their now-inapplicable javadoc are removed).
- `AssessmentLocator` — `courseIdOfQuiz`. Implemented by `assessment.app.AssessmentLocatorAdapter`
  (Phase 7), backed by `quizzes.course_id` — a direct FK, no indirection through `curriculum`
  needed at all.
- `CodingLocator` — `courseIdOfCodingTask`. Registered now (per the standing convention of
  registering ports before their owning module exists, so the very first class in `coding` is
  already governed), implemented once Phase 8 builds `coding.app.CodingLocatorAdapter`.

`AccessGuard` gains `Optional<AssessmentLocator>`/`Optional<CodingLocator>` constructor
parameters alongside the existing three ports; `requireQuizViewer`/`requireQuizEditor` now resolve
through `assessment()` instead of `curriculum()`, and `requireCodingTaskViewer`/
`requireCodingTaskEditor` through `coding()`.

## Consequences
`assessment -> iam` becomes a real edge in the module map — for the **same** reason `academics`/
`curriculum`/`enrollment` point at `iam` (implementing one of `AccessGuard`'s ownership ports), not
the "consumes `AccessGuard`" reason ADR-012 introduced for `progress`. Both reasons happen to
apply to `assessment` (it also has its own teacher-facing quiz-authoring endpoints that call
`AccessGuard.requireCourseEditor` directly), but one edge covers both. `coding` will need the
identical `-> iam` edge in Phase 8, for the identical reason. `docs/architecture/README.md`'s
diagram and `ArchitectureBoundaryTest`'s `ALLOWED` map are updated to add `as --> iam` now, and
`co --> iam` was already anticipated as it would need to happen in Phase 8 (the `AccessGuard` port
list itself is not touched further — `CodingLocator` already exists as of this ADR, just
unimplemented, the same pattern `EnrollmentLookup`/`CurriculumLocator` used while `enrollment`/
`curriculum` didn't exist yet).

This is a breaking change to an internal interface only (`CurriculumLocator`), not to any public
API — no HTTP contract moves. No test previously exercised `requireQuizViewer`/
`requireQuizEditor`/`requireCodingTaskViewer`/`requireCodingTaskEditor` (nothing could, since
`assessment`/`coding` did not exist), so nothing regresses.
