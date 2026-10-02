# ADR-014 — Cross-module notification via Spring events, not a dependency edge

**Status:** Accepted

## Context
Phase 7's stated Acceptance (`docs/planning/phase-plan.md`): "an attempt is graded
deterministically, produces a `grade_entry`, **updates progress**, and never exposes the answer
key early." `activity_progress` (Phase 6) already exists specifically for this — its own class
javadoc said "`assessment`/`coding` (Phase 7/8) are what will call attempts... and record the
outcome here."

But the module map runs the wrong way for a direct call: `pr --> as` (`progress` depends on
`assessment`, so it may call into it), never `as --> pr`. `assessment` importing `progress.app`
to write an `ActivityProgress` row directly would be a reverse dependency — exactly the kind of
edge the DAG requirement (`ArchitectureBoundaryTest`) exists to reject, and for a real reason:
`progress` already aggregates across `curriculum`/`enrollment`/`assessment`/`coding` (its roll-up
needs data from all of them); if any of those also depended back on `progress`, the graph would
cycle and "which module owns the roll-up" would stop having one answer.

## Options
1. Have `progress` poll `assessment` for newly-graded attempts (e.g. a `findGradedSince(cursor)`
   query, invoked lazily whenever a course-progress read happens). Rejected — turns a clear,
   single "this just happened" moment into an open-ended freshness question every read path has to
   reason about, and still doesn't decouple the *write* path's timing.
2. Break the DAG: let `assessment -> progress`. Rejected outright — recreates the exact cycle the
   architecture forbids (`progress` already depends on `assessment`).
3. **A domain event.** `assessment.app.AttemptService` publishes a plain record
   (`common.events.QuizAttemptGradedEvent`) via Spring's `ApplicationEventPublisher` after grading;
   `progress.app.ProgressService` listens with `@TransactionalEventListener(phase = AFTER_COMMIT)`.

## Decision
**Option 3.** The event type lives in `common.events` — both modules may already depend on
`common` (cross-cutting, allowed for everyone) — so publishing and listening cost neither module a
new dependency on the other. `AttemptService` doesn't know or care whether anything listens;
`ProgressService` doesn't know or care who published. This is the same shape as the codebase's
existing extension-point registries (`ContentBlockValidatorRegistry`, `QuestionGraderRegistry`) in
spirit: a producer and consumers connected through a shared, neutral contract type, not through a
direct reference to each other's internals.

`AFTER_COMMIT` specifically: `AttemptService.doSubmit` grades and commits inside its own
transaction. If the listener ran on plain `@EventListener` (synchronous, same transaction), a
failure inside `ProgressService`'s roll-up write would roll back the *attempt's own* grading
transaction too — a bug in progress bookkeeping would silently un-grade a quiz attempt. Listening
after commit means the attempt is grading-of-record the instant `submit` returns 200, regardless
of what happens to the progress side afterward; a progress-side failure is logged and retried
(`ProgressService`'s existing optimistic-lock/`DataIntegrityViolationException` retry loop, reused
unchanged) without ever touching the attempt.

`event.activityId()` is nullable — an attempt started without an `activityId` (no `Activity`
resolvable at start time) publishes an event the listener no-ops on immediately. Not an error: an
attempt not tied to any course activity has nothing in `curriculum`/`progress` to update.

**A real pitfall found by `docker compose up --build` smoke testing, not by any automated
test**: the listener's own database write must run with `@Transactional(propagation =
REQUIRES_NEW)`, not plain `@Transactional`. `TransactionalApplicationListenerSynchronization`
invokes `AFTER_COMMIT` listeners from inside `AbstractPlatformTransactionManager`'s
`triggerAfterCompletion`, which runs after the committing transaction's resources are already
unbound from the thread but before its `TransactionInterceptor` call has technically returned. A
plain `@Transactional` listener method tries to join that already-unbound context instead of
opening a fresh one, and fails with `TransactionRequiredException: no transaction is in progress`
the moment it flushes. No unit or integration test caught this — every test that exercises
`submit` uses `activityId = null`, which short-circuits the listener before it touches the
database; only a real quiz-linked-to-an-activity attempt (the seeded `DEV-QUIZ-DB` fixture,
started with its real `activityId`) exercises the path. `AssessmentIT`'s
`gradedAttemptLinkedToActivity_updatesActivityAndCourseProgress` now covers it.

## Consequences
No new module dependency edge. `coding` (Phase 8) should publish an analogous event
(`CodingSubmissionGradedEvent` or similar) for the identical reason once it exists, rather than
being tempted to import `progress` directly. The tradeoff: eventual (not synchronous-in-the-same-
transaction) consistency between "attempt graded" and "progress updated" — acceptable here because
nothing in this codebase currently requires reading updated progress in the same request that
submitted the attempt; the roll-up recompute-from-source design (ADR from Phase 6) means a
temporarily stale `course_progress` row self-heals on the next write regardless.
