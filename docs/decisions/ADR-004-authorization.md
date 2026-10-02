# ADR-004 — Authorization: roles → permissions, plus centralized access guards

**Status:** Accepted

## Context
`ADMIN`/`TEACHER`/`STUDENT` today; `CURATOR`, `MODERATOR`, `ASSISTANT_TEACHER`, `SUPER_ADMIN`
later (brief §7) without redesigning security. Two distinct questions must not be conflated:
- **What may this kind of user do?** ("can create a quiz") — coarse, role-shaped.
- **May this user do it to *this* object?** ("is he a teacher *of this course*") — ownership.

## Options
1. Hardcoded role checks (`hasRole('TEACHER')`) everywhere. Every new role edits every
   controller. Rejected.
2. Full ABAC/policy engine (OPA, Spring Security ACL). Powerful, heavy, and a nightmare for
   an AI agent to reason about at this scale. Rejected.
3. **Role→permission mapping in the DB + permission-based `@PreAuthorize` + explicit
   ownership guards.**

## Decision
**Option 3.**

**Layer 1 — permissions as authorities.** `roles` ⇄ `permissions` in the DB. The JWT carries
granted permissions. Endpoints authorize on *permissions*, never role names:
`@PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")`. Adding `ASSISTANT_TEACHER` = one
role row + permission rows. **Zero code change.** That is the whole point.

**Layer 2 — ownership guards.** One guard per aggregate in `iam.app.access`:
`CourseAccessGuard`, `EnrollmentAccessGuard`, `AttemptAccessGuard`, `SubmissionAccessGuard`,
`ContentAccessGuard`. Each exposes intention-revealing methods
(`requireTeacherOf(courseId)`, `requireEnrolledIn(courseId)`, `requireOwnerOf(attemptId)`).
Application services call them at the top of every use case. **`ADMIN` bypass lives inside
the guards** — one auditable place, not `if (isAdmin)` sprinkled through services (brief §22).

**Layer 3 — query-level scoping.** Student-facing repository methods take the caller's
`enrollmentId`/`userId` **as a SQL predicate**. We never load-then-check: a student query
physically cannot return another student's row. `StudentDataIsolationIT` asserts this per
endpoint.

**Layer 4 — database constraints.** FKs make cross-tenant rows unrepresentable: progress
hangs off `enrollment_id`, attempts off `enrollment_id`, so "another student's progress" has
no path to exist.

Denial is default: no permission ⇒ `403 AUTH_FORBIDDEN`. Existence is not leaked — an
unauthorized read of someone else's object returns `404 RESOURCE_NOT_FOUND`, deliberately.

## Consequences
New roles are configuration. Authorization logic is greppable in one package. Costs one
in-memory cached lookup of role→permission, and discipline: **a use case that forgets its
guard is a hole.** Mitigation: `AuthorizationCoverageTest` asserts every non-public
controller method has either a `@PreAuthorize` or a documented guard call.
