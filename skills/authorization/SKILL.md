---
name: authorization
description: Permissions, guards and student data isolation. Load before adding any endpoint that returns or mutates user data.
---
# Authorization rules (ADR-004)

Four layers. An endpoint needs **at least** layers 1 and 3.

## L1 — permission, not role
```java
@PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
```
Never `hasRole('TEACHER')` in a controller. Roles map to permissions in the DB, so a new role
(`ASSISTANT_TEACHER`, `CURATOR`) is data. Permission naming: `<DOMAIN>_<ACTION>` —
`COURSE_READ`, `COURSE_CONTENT_WRITE`, `ENROLLMENT_MANAGE`, `SUBMISSION_REVIEW`,
`GRADE_WRITE`, `USER_MANAGE`, `ANALYTICS_READ`, `AUDIT_READ`.

## L2 — ownership guard
`iam/app/access/`: `CourseAccessGuard`, `EnrollmentAccessGuard`, `AttemptAccessGuard`,
`SubmissionAccessGuard`, `ContentAccessGuard`.
```java
courseAccessGuard.requireTeacherOf(courseId);      // throws ForbiddenException
var enrollment = enrollmentAccessGuard.requireActiveEnrollment(courseId, caller.id());
attemptAccessGuard.requireOwnerOf(attemptId, caller.id());
```
First statement of the use case. **`ADMIN` bypass lives inside the guard** — never write
`if (isAdmin)` in a service.

## L3 — SQL scoping (the one that actually saves you)
Student-scoped repository methods take the caller id as a predicate:
```java
Optional<QuizAttempt> findByIdAndEnrollment_StudentUserId(UUID id, UUID studentUserId);
```
Never `findById(id)` then compare owners in Java. `StudentDataIsolationIT` asserts, per
endpoint, that student B gets `404` for student A's resource.

## L4 — schema
Student-owned rows hang off `enrollment_id`. There is no path for "student A's progress in
student B's enrollment" to exist. Keep it that way: do not add a nullable `user_id` shortcut
to a progress or attempt table.

## Deliberate behaviours
- Unauthorized read of someone else's object ⇒ **404**, not 403. Do not leak existence.
- 403 is only for "you are authenticated, this action exists, you may not perform it".
- Teachers see only their assigned courses (`course_teachers`), including in analytics.
- Teachers may see hidden test cases and `solution_code`; **students never can** — this is
  enforced by using a different DTO/mapper, not by a boolean flag on one DTO.

## Adding an endpoint — checklist
1. `@PreAuthorize` with a permission. 2. Guard call for ownership. 3. Repository method that
filters by caller in SQL. 4. A row in `docs/security/authorization-matrix.md`.
5. A case in `AuthorizationMatrixIT` and, if it returns student data, in
`StudentDataIsolationIT`. Skipping 4-5 means the endpoint is not done.
