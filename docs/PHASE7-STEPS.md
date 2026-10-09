# Phase 7 — Admin powers: implementation steps

Planning baseline: commit `1536927`. Scope: `docs/PLAN.md` §§0, 1, 5, 7. Verification used source code and committed OpenAPI, as requested. PLAN decisions remain unchanged.

**Path notation**

The following prefixes identify exact repository paths:

- `J/` = `backend/api/src/main/java/uz/aifu/lms/`
- `T/` = `backend/api/src/test/java/uz/aifu/lms/`
- `M/` = `backend/api/src/main/resources/db/migration/`
- `A/` = `frontend/src/features/admin/`
- `B/` = `frontend/src/features/builder/`
- `L` = all three files: `frontend/src/shared/i18n/uz.json`, `ru.json`, `en.json`.

Each numbered chunk touches at most five files. Estimates are implementation effort, excluding waiting for CI or backend availability. Schema, implementation, tests and generated contracts belonging to one backend change must land together; chunks are work divisions, not permission to deploy incomplete changes.

**7.0 — Verified gap inventory**

| Requirement | Verdict | Source evidence and implementation consequence |
|---|---|---|
| 7.1 Display the student’s group | **ALREADY EXISTS** | `A/user-page.tsx`, `DetailsCard`, renders `studentProfile.groupCode`. `J/iam/api/dto/UserDtos.java` already exposes it. Preserve it. |
| 7.1 Change group from the student card | **CONFIRMED** | `A/user-page.tsx` has no membership editor. The mutation already exists in `A/reference-queries.ts`, `useAddGroupStudents`, and `J/scheduling/api/AdminGroupController.java`. |
| 7.1 Show a student’s enrollments across courses | **CONFIRMED** | `A/user-page.tsx` has no enrollment section. `J/enrollment/api/AdminEnrollmentController.java` and OpenAPI require `courseId`; neither accepts `userId`. |
| 7.1 Underlying student enrollment lookup | **ALREADY EXISTS** | `J/enrollment/app/EnrollmentService.java`, `listByStudent`; `J/enrollment/persistence/EnrollmentRepository.java`, `findByStudent_Id`. Extend filtering, not enrollment storage. |
| 7.1 Change enrollment status | **ALREADY EXISTS** backend; **CONFIRMED** card UI gap | `AdminEnrollmentController.changeStatus` already accepts `status` and optional `reason` as **query parameters**. |
| 7.2 Teacher-only user list | **ALREADY EXISTS** | `A/users-page.tsx` reads the `role` search parameter; `A/users-queries.ts` forwards it. `A/admin-dashboard-page.tsx` already links to `?role=TEACHER`. |
| 7.2 Integrated teacher tab and teacher courses | **CONFIRMED** | No teacher work section exists in `A/user-page.tsx`. Course assignments exist in `CourseDetail.teachers`, defined in `J/academics/api/dto/CourseDtos.java`. |
| 7.2 Teacher load API | **ALREADY EXISTS** | `B/queries.ts`, `useLoadQuery`; `AdminTimetableController.loadReport` accepts `teacherId`. |
| 7.2 Weekly load semantics | **CONFIRMED** integration gap | `TimetableQueryService.loadRows` counts the **whole term**. Use existing `/week` for weekly hours; label `/load` figures as term totals. No new backend endpoint. |
| 7.3 Deactivate account | **ALREADY EXISTS** | `A/user-page.tsx`, `StatusCard`; `J/iam/app/user/UserService.java`, `changeStatus`, revokes refresh tokens when leaving ACTIVE. Target status is `DISABLED`, not an invented `INACTIVE`. |
| 7.3 Withdraw enrollment | **ALREADY EXISTS** | `J/enrollment/domain/Enrollment.java`, `withdraw`, permits every non-terminal status. `EnrollmentService` appends transition history. |
| 7.3 One confirmation covering both actions | **CONFIRMED** | No combined operation exists in `A/user-page.tsx` or `A/users-queries.ts`. Compose existing endpoints with explicit partial-success handling. |
| 7.4 Students without a group | **CONFIRMED** |
| 7.4 Groups without published timetable entries | **CONFIRMED** |
| 7.4 Courses without a teacher | **CONFIRMED** |
| 7.4 Active enrollments of inactive users | **CONFIRMED** |
| 7.4 Locked accounts | **CONFIRMED** |
| 7.4 Exhausted midterm attempts without passing | **CONFIRMED** |
| Evidence for all six 7.4 rows | — | `J/analytics/api/AdminAnalyticsController.java` exposes course analytics only; `J/analytics/app/AnalyticsService.java` has no administrative problems query. `frontend/src/app/router.tsx` has no problems route. `A/admin-dashboard-page.tsx` only reports unscheduled load and overridden conflicts. OpenAPI and `ENDPOINTS.md` contain no `/admin/problems`. |
| 7.5 Recurring class creation and existing exceptions | **ALREADY EXISTS** | `B/entry-panel.tsx`, `B/queries.ts`, `J/scheduling/app/TimetableService.java`. Preserve these. |
| 7.5 Single-date `ADD` exception | **CONFIRMED** | `J/scheduling/domain/ExceptionKind.java`, `M/V019__groups_and_scheduling.sql`, OpenAPI `ExceptionRequest.kind`, and `B/entry-panel.tsx`’s `KINDS` contain only `CANCELLED`, `MOVED`, `REPLACED`. |
| 7.5 Add action from concrete week preview | **CONFIRMED** | `B/week-preview.tsx` only displays `WeekView`; no mutation action exists. |

Additional implementation facts:

- `CourseSummary` has no teacher assignments; `CourseDetail.teachers` does.
- `UserDetail.studentProfile` exposes `groupCode`, not `groupId`. Resolve the existing group by code using `/groups`.
- `LearningPathService.midtermView()` currently returns `activity.attemptLimit`, not quiz allowance including grants. The problems query must use the authoritative quiz/attempt/grant tables.
- `Timetable.requireEditable()` allows DRAFT and PUBLISHED; it rejects ARCHIVED. Preserve that behavior for exceptions.
- Existing exception mutations do not invalidate all affected preview/load caches.
- `docs/security/authorization-matrix.md`, referenced by the authorization skill, is absent. Create it as documentation; do not claim it was updated.

**Implementation contracts applying to every chunk**

Use the typed `openapi-fetch` client and generated schemas. Keep server data in TanStack Query. New forms use React Hook Form and Zod. Local state may hold form input, selection and operation progress.

Every new section needs loading, actionable empty, error with retry/requestId, and content states. At 390 px use stacked cards; at 768 px use compact sections; at 1440 px retain the existing two-column user-page layout. Verify both themes, keyboard access and reduced motion.

Backend changes are additive under `/api/v1`. Use DTO records, Bean Validation, read-only service transactions for reads, and existing error codes. Document every new or changed operation with `@Operation`, response annotations, security requirements and example payloads.

Admin-only permissions:

| Operation | Permission |
|---|---|
| Cross-user enrollment reads | `ENROLLMENT_READ` |
| Enrollment transitions | `ENROLLMENT_MANAGE` |
| Account changes | `USER_MANAGE` |
| Group membership changes | Existing `GROUP_MANAGE` authorization |
| Problems endpoint | `ANALYTICS_PLATFORM_READ` |
| Timetable exceptions | `TIMETABLE_MANAGE` |

Test ordinary teachers, including assigned course teachers, receive 403 on these administrative operations. Existing staff-readable group and teacher APIs retain their current authorization.

**7.1 — Student card**

**7.1-B1 — Add cross-course enrollment filtering. Four files; medium, 3–5 hours.**

Change:

1. `J/enrollment/api/AdminEnrollmentController.java`
2. `J/enrollment/app/EnrollmentService.java`
3. `J/enrollment/persistence/EnrollmentRepository.java`
4. Create `T/AdminPeopleIT.java`

Contract:

- Extend `GET /api/v1/admin/enrollments`.
- Make `courseId` optional; add optional UUID `userId`.
- Require at least one of `courseId` or `userId`.
- When both are present, apply their intersection in SQL.
- Preserve optional `status`, pagination and existing course-only behavior.
- Preserve `EnrollmentSummary`: `id`, `courseId`, `subjectCode`, `cohortCode`, `termName`, `studentUserId`, `studentFullName`, `status`, `attemptNo`, `enrolledAt`.
- No new request or response record is necessary.
- Preserve `requireCourseEditor(courseId)` whenever a course is supplied.
- Keep the cross-user list behind the existing admin-only permission.
- Unknown user UUID produces an empty page; invalid UUID or missing both filters produces 400.
- Fetch student, subject and term through the existing entity-graph pattern.
- Default order: `enrolledAt DESC, id ASC`; preserve supported explicit sorting.

Tests:

- Student A across two courses; student B in one shared course.
- `userId=A` excludes B; combined course/user filters exclude A’s other course.
- Status filter, empty page, multiple pages and size cap.
- Existing course-only request remains valid.
- Teacher and student receive 403; anonymous caller receives 401.
- Response contains no credentials or unrelated student identifiers.

Dependency: none. May run alongside 7.2 and 7.5 backend work.

**7.1-B2 — Publish contract and authorization coverage. Five files; small, 1–2 hours.**

Change:

- `docs/api/openapi.json`
- `docs/api/ENDPOINTS.md`
- `frontend/src/shared/api/schema.d.ts`
- `T/AuthorizationMatrixIT.java`
- `T/StudentDataIsolationIT.java`

Regenerate the first three files using repository commands. Add explicit denial tests for cross-user filtering and retain existing `/enrollments/me` ownership tests.

Dependency: B1. Land with B1.

**7.1-F1 — Typed enrollment hooks and transitions. Four files; medium, 3–4 hours.**

Create:

- `A/student-enrollment-queries.ts`
- `A/student-enrollment-model.ts`
- `A/student-enrollment-queries.test.ts`

Change:

- `A/reference-queries.ts`

Implement `useStudentEnrollmentsQuery`, `useChangeEnrollmentStatus` and a generated-type-based transition helper.

Use `PUT /api/v1/admin/enrollments/{id}/status` with query parameters `{status, reason}` and no JSON body.

Expose only valid transitions:

- PENDING → ACTIVE, WITHDRAWN
- ACTIVE → SUSPENDED, COMPLETED, FAILED, WITHDRAWN
- SUSPENDED → ACTIVE, WITHDRAWN
- WITHDRAWN or FAILED → ACTIVE
- COMPLETED → none

Invalidate user enrollment pages, affected course enrollment queries and problems queries after transitions. Group moves must also invalidate user details and both old/new group rosters.

Vitest: request serialization, filter/query-key isolation, transition choices and cache invalidation.

Dependency: B2.

**7.1-F2 — Group editor. Five files; small, 2–3 hours.**

Create `A/student-group-card.tsx`; change `A/user-page.tsx` and `L`.

Reuse `GET /groups` and `POST /admin/groups/{id}/students` with `{userIds:[userId]}`. Do not remove the old membership first: the existing endpoint moves it.

Resolve current group by `groupCode`; exclude archived targets. Show an honest unassigned state or unresolved-code state. Refetch the user after success.

Keys: `admin.studentCard.groupTitle`, `changeGroup`, `noGroup`, `unknownGroup`, `groupChanged`.

Dependency: F1. Course-card rendering can proceed independently.

**7.1-F3 — Enrollment list and status dialog. Five files; medium, 4–6 hours.**

Create:

- `A/student-enrollments-card.tsx`
- `A/enrollment-status-dialog.tsx`

Change `L`.

Display every server page, current status and course navigation. Resolve localized course titles through existing course-detail reads for visible rows; keep these requests cached and bounded by the visible page. Do not invent a `courseTitle` field on `EnrollmentSummary`.

Require a nonblank reason in the UI. Show transition failures with requestId and preserve form input. Terminal rows remain visible.

Keys: `admin.studentCard.coursesTitle`, `noEnrollments`, `openCourses`, `changeEnrollmentStatus`, `statusChanged`, `reasonRequired`.

Dependency: F1.

**7.1-F4 — Compose and verify. Three files; medium, 2–4 hours.**

Change `A/user-page.tsx`; create `A/student-card.test.tsx` and `frontend/e2e/admin-people.spec.ts`.

Vitest: absent profile, missing group, empty enrollment page, failed mutation, completed enrollment without actions.

Playwright:

1. Create isolated student/group/enrollment records through the real API.
2. Open the student card and verify the existing group.
3. Move the student; reload and verify old/new group rosters.
4. Verify two course enrollments and pagination.
5. Suspend, resume and withdraw one enrollment with a reason.
6. Verify persisted status/history and that the second student is unchanged.

Dependency: F2–F3.

**7.2 — Teacher tab, courses and weekly load**

**7.2-B1 — Verify reusable backend contracts. Two files; small, 1–2 hours.**

Change `T/AdminPeopleIT.java` and `T/SchedulingIT.java`. No production endpoint or DTO changes.

Cover:

- `/admin/users?role=TEACHER` excludes student-only accounts.
- Course details contain unscheduled teacher assignments.
- `/admin/timetables/{id}/load?teacherId=` returns term load.
- `/admin/timetables/{id}/week?teacherId=&date=` includes the appropriate week and exceptions.
- Teacher cannot call these admin endpoints.

Dependency: none.

**7.2-F1 — Teacher work queries. Two files; medium, 4–6 hours.**

Create `A/teacher-work-queries.ts` and `A/teacher-work-queries.test.ts`.

Use existing endpoints only:

- `/admin/users?role=TEACHER`
- `/admin/courses?page=&size=&termId=&locale=`
- `/admin/courses/{id}`
- `/admin/timetables`
- `/admin/timetables/{id}/load?teacherId=&locale=`
- `/admin/timetables/{id}/week?teacherId=&date=&locale=`

For course assignments, page through the selected term’s catalogue and inspect cached course details. Match `teachers[].userId`, never names. Bound detail-request concurrency to four; support cancellation. Do not stop after the first 100 courses or infer assignments from scheduled classes.

Weekly hours:

- Use concrete classes from `/week`.
- Count only `SCHEDULED`, `MOVED_IN`, `REPLACED`.
- Require `ClassView.teacherUserId` to match the selected teacher: the server can also include a replaced original teacher’s class for context.
- Count each occurrence once; one pair equals two academic hours.
- Do not recompute parity, holidays or week boundaries.

Vitest: assignment beyond the first page, duplicate names, assigned-but-unscheduled course, replacement teacher, cancelled class and failed catalogue page.

Dependency: none; no contract change.

**7.2-F2 — Teacher work card. Four files; medium, 3–5 hours.**

Create `A/teacher-work-card.tsx`; change `L`.

Show term/timetable selection, assigned courses, a date selector, weekly hours, and separately labelled term planned/scheduled/remaining hours.

Keys: `admin.teachers.tab`, `courses`, `noCourses`, `weeklyHours`, `termLoad`, `chooseTimetable`, `openTimetable`, `loadingCourses`.

Dependency: F1.

**7.2-F3 — Wire the existing users page. Four files; medium, 2–4 hours.**

Change:

- `A/users-page.tsx`
- `A/user-page.tsx`
- Create `A/teacher-work-card.test.tsx`
- `frontend/e2e/admin-people.spec.ts`

Add a teacher tab/preset to the existing page; preserve query/status filters and reset pagination when changing role. Add the teacher work card for users with TEACHER role. Multi-role users may see both student and teacher sections.

Playwright: teacher tab, selected teacher, unscheduled assigned course, persisted filter after reload, weekly versus term figures, and a replacement/cancellation case.

Dependency: F2.

**7.3 — Deactivate and withdraw in one confirmation**

**7.3-B1 — Lock down existing lifecycle behavior. Two files; small, 1–2 hours.**

Change `T/AdminPeopleIT.java` and `T/EnrollmentIT.java`.

No new backend endpoint or DTO.

Verify:

- `PUT /admin/users/{id}/status`, body `{status:"DISABLED", reason}`.
- `PUT /admin/enrollments/{id}/status?status=WITHDRAWN&reason=...`.
- Withdrawal from ACTIVE, PENDING and SUSPENDED.
- COMPLETED remains terminal.
- Deactivation revokes access; admin cannot deactivate themselves.
- Enrollment events/history remain intact.
- Teacher/student denial and unrelated-account isolation.

Dependency: 7.1-B1 test fixtures.

**7.3-F1 — Recoverable orchestration. Two files; medium, 3–5 hours.**

Create `A/remove-student-queries.ts` and `A/remove-student-queries.test.ts`.

Implement one TanStack mutation workflow:

1. Refetch the target user and selected enrollment states.
2. Disable the account if necessary.
3. Withdraw explicitly selected non-terminal enrollments sequentially.
4. Record confirmed successes and per-operation failures.
5. Invalidate user, enrollment, course and problems queries even after partial failure.

On retry, refetch and skip already satisfied targets. After a lost response, verify state before resending. Never reactivate automatically as compensation.

The workflow is not atomic. Preserve that fact in the UI. Do not add a bulk-delete endpoint.

Vitest: cancellation sends nothing; disable failure prevents withdrawals; failure after one withdrawal preserves success; retry skips completed work; terminal-state race; already-disabled account.

Dependency: 7.1-F1.

**7.3-F2 — Confirmation dialog. Five files; medium, 3–4 hours.**

Create `A/remove-student-dialog.tsx`; change `A/user-page.tsx` and `L`.

Show account identity, consequences, selected courses and reason before confirmation. Load all relevant enrollment pages before offering an “all eligible courses” selection. Never silently act on only the first page.

Disable repeated submission. Keep partial results visible with requestIds and a retry action. For an account with no eligible enrollments, clearly identify the operation as deactivation only.

Keys: `admin.removeStudent.title`, `description`, `selectCourses`, `confirm`, `deactivateOnly`, `partial`, `retryRemaining`, `complete`.

Dependency: F1.

**7.3-F3 — UI and end-to-end verification. Two files; medium, 2–3 hours.**

Create `A/remove-student-dialog.test.tsx`; change `frontend/e2e/admin-people.spec.ts`.

Playwright: cancel first, verify no changes, then confirm deactivation plus selected withdrawals; reload and check status/history. Verify unselected and completed enrollments remain unchanged. Exercise a real invalid-transition response; use Vitest for deterministic transport-failure permutations.

Dependency: F2.

**7.4 — Problems page**

**7.4-B1 — Analytics-owned read model. Five files; medium/high, 5–8 hours.**

Create:

1. `M/V035__admin_problems_view.sql`
2. `J/analytics/domain/AdminProblemView.java`
3. `J/analytics/persistence/AdminProblemViewRepository.java`
4. `J/analytics/api/dto/AdminProblemDtos.java`
5. `T/AdminProblemsIT.java`

Leave V034 reserved for Phase 5 fixes. Recheck migration numbering before implementation; rename only unapplied new migrations if necessary.

Follow V015’s existing analytics-view pattern: one analytics-owned SQL view, immutable entity and repository. No imports of other modules’ persistence packages and no writes through foreign entities.

Use `UNION ALL` with stable `problemKey = type + ":" + primary-resource-id`. One user may legitimately appear in different categories.

Implement these predicates explicitly:

| Type | Predicate |
|---|---|
| `STUDENT_WITHOUT_GROUP` | ACTIVE user with STUDENT role and no student profile, blank group code, or group code not matching a group. Include missing profiles without an inner join. |
| `GROUP_WITHOUT_PUBLISHED_ENTRIES` | ACTIVE group with no linked entry in any PUBLISHED timetable. This is a published-entry check, not a client-side calculation of this week’s classes. |
| `COURSE_WITHOUT_TEACHER` | Non-deleted DRAFT/PUBLISHED course with no `course_teachers` assignment. |
| `ACTIVE_ENROLLMENT_INACTIVE_USER` | ACTIVE enrollment whose student’s user status is not ACTIVE. |
| `LOCKED_ACCOUNT` | `locked_until` is later than database current time. |
| `MIDTERM_ATTEMPTS_EXHAUSTED` | ACTIVE enrollment in an enabled learning path with a configured published MIDTERM quiz; finite allowance exhausted; midterm activity not COMPLETED. Exclude a still-usable IN_PROGRESS attempt and SUBMITTED attempts awaiting grading. |

For the last category:

- Join the configured activity to its referenced quiz and verify course consistency.
- `attemptsUsed = max(quiz_attempts.attempt_number)` for that exact quiz/enrollment.
- `attemptsAllowed = quizzes.max_attempts + count(quiz_attempt_grants)` for that exact pair.
- An elapsed IN_PROGRESS attempt may count as exhausted using its expiry time, without mutating it from this read endpoint.
- Exclude unlimited quizzes and completed midterms.
- Return grant availability separately; an already-used grant must not produce a misleading “grant another attempt” action.

DTO `AdminProblemView` fields:

`problemKey`, `type`, `userId`, `fullName`, `groupId`, `groupCode`, `courseId`, `subjectCode`, `cohortCode`, `termName`, `enrollmentId`, `quizId`, `attemptsUsed`, `attemptsAllowed`, `grantAvailable`, `lockedUntil`.

Use nullable/absent fields where inapplicable. Names and codes come from stored data. No credentials, quiz answers or frontend URLs in the response.

Repository filtering and pagination happen in SQL. Default sort is `type ASC, problemKey ASC`; whitelist supported sort fields.

IT fixtures must include positive and negative examples for all six types, expired locks, missing profiles, archived records, unlimited attempts, grants, successful midterms and pending grading.

Dependency: Phase 5 schema/behavior and 7.0 findings.

**7.4-B2 — Expose the endpoint. Five files; medium, 3–5 hours.**

Create:

- `J/analytics/app/AdminProblemsService.java`
- `J/analytics/api/AdminProblemsController.java`

Change:

- `T/AdminProblemsIT.java`
- `T/AuthorizationMatrixIT.java`
- `T/StudentDataIsolationIT.java`

Contract:

`GET /api/v1/admin/problems?type=&page=&size=&sort=`

Return `PageResponse<AdminProblemView>`. `type` is the six-value enum above; omitted means all types. Invalid type returns 400. Apply `PageableSupport.sanitize` and cap size at 100.

Use `ANALYTICS_PLATFORM_READ`, already granted to ADMIN. The service is `@Transactional(readOnly = true)`.

ITs:

- All/type-filtered pagination, stable ordering and correct totals.
- No duplicate rows within a type from role/attempt joins.
- Changing the underlying group/teacher/status/grant removes the relevant problem.
- Teacher, assigned teacher and student receive 403; anonymous caller receives 401.
- Neither error bodies nor denied requests expose problem rows.
- ArchitectureBoundaryTest passes without widening the module graph.

Dependency: B1.

**7.4-B3 — Contract and security documentation. Four files; small, 1–2 hours.**

Regenerate:

- `docs/api/openapi.json`
- `docs/api/ENDPOINTS.md`
- `frontend/src/shared/api/schema.d.ts`

Create `docs/security/authorization-matrix.md`, covering the changed Phase 7 administrative reads/writes and expected 401/403 behavior.

Dependency: B2. Land with B1–B2.

**7.4-F1 — Problems query and action mapping. Three files; medium, 2–3 hours.**

Create:

- `A/problems-queries.ts`
- `A/problems-model.ts`
- `A/problems-model.test.ts`

Implement `useAdminProblemsQuery({type,page,size})`. Preserve filters in search parameters. Narrow generated DTOs at the boundary.

Map known types to internal destinations:

- Missing group → student card’s group section.
- Missing timetable entries → builder with `view=group&id={groupId}`.
- Missing teacher → course administration.
- Inactive user enrollment → student card’s enrollment section.
- Locked account → user status/unlock section.
- Exhausted midterm → learning-path page with exact course, enrollment and quiz context.

Missing required identifiers disable the action and show an honest unavailable state; never build malformed destinations.

Dependency: B3.

**7.4-F2 — Problems page. Four files; medium, 3–5 hours.**

Create `A/problems-page.tsx`; change `L`.

Build type filter, total count, pagination, named rows and fix actions. Reuse existing table/card/empty/error primitives. Do not derive a global problem count from the current page length.

Keys: `nav.adminProblems`; `admin.problems.title`, `allTypes`, `empty`, `emptyBody`, `reviewUsers`, `fix`, `actionUnavailable`, `reviewAttempt`; plus all six `admin.problems.type.<TYPE>` keys.

Dependency: F1.

**7.4-F3 — Navigation and coverage. Five files; medium, 2–4 hours.**

Change:

- `frontend/src/app/router.tsx`
- `frontend/src/app/shell/nav.ts`
- `A/admin-dashboard-page.tsx`
- Create `A/problems-page.test.tsx`
- `frontend/e2e/admin-people.spec.ts`

Add the lazy `/admin/problems` route under the existing ADMIN guard and a dashboard link.

Vitest: all six action mappings, invalid type, loading/error/empty states, page reset and missing fields.

Playwright: create representative real records, filter each problem type, follow each fix destination, perform a fix, return and verify the row disappears. Run the new page on desktop and phone.

Dependency: F2.

**7.4-F4 — Preserve exact grant context. Five files; medium, 3–5 hours.**

Change:

- `A/learning-path-page.tsx`
- `A/learning-path-queries.ts`
- `A/structure-queries.ts`
- `A/learning-path-queries.test.ts`
- `frontend/e2e/admin-people.spec.ts`

Add support for course/enrollment/quiz search parameters and `initialQuizId` in `GiveAttemptDialog`.

Resolve targeted course/enrollment records directly, even when absent from the first list page. Validate that the enrollment and quiz belong to the selected course. Never silently substitute the first quiz or student.

Use the existing grant endpoint and reason validation. Invalidate problems after success. A row with `grantAvailable=false` links to review without offering another grant.

Tests: deep link beyond the first enrollment page, wrong-course identifiers, exact quiz selection, server rejection and successful problem removal.

Dependency: 7.4-F1 and existing Phase 5 grant UI.

**7.5 — Add one dated class from the week preview**

An `ADD` exception creates an additional occurrence of a selected existing timetable entry. It inherits course, session type, groups and subgroup. It does not create a recurring entry. The existing recurring-class editor remains the way to define a new class template.

**7.5-B1 — Additive exception contract and constraints. Five files; medium, 3–5 hours.**

Change:

1. Create `M/V036__timetable_add_exception.sql`
2. `J/scheduling/domain/ExceptionKind.java`
3. `J/scheduling/domain/TimetableException.java`
4. `J/scheduling/api/dto/SchedulingDtos.java`
5. Create `T/scheduling/domain/TimetableExceptionTest.java`

Add enum value `ADD`; preserve all existing values and payloads.

Reuse:

`POST /api/v1/admin/timetable-entries/{entryId}/exceptions`

For ADD:

- `kind = ADD`
- `onDate` = the actual added date
- `newPairSlotId` required
- `newRoomId`, `newTeacherUserId` optional overrides
- `reason` required, trimmed, 1–300 characters
- `newDate` absent

Apply ADD-specific Bean Validation, domain validation and DB constraints. Preserve the existing `(entry_id,on_date)` uniqueness constraint and all old exception rows. Do not edit V019.

Keep `ExceptionView` fields unchanged; its enum gains ADD and `newPairNo` identifies the chosen pair.

Unit tests: valid ADD, missing pair/reason, invalid extra date and unchanged old-kind validation.

Dependency: none; coordinate migration ordering with 7.4.

**7.5-B2 — Persistence, occurrence rendering and conflict checks. Four files; high, 5–8 hours.**

Change:

- `J/scheduling/app/TimetableService.java`
- `J/scheduling/app/TimetableQueryService.java`
- `J/scheduling/api/AdminTimetableController.java`
- Create `T/TimetableAddExceptionIT.java`

In `addException`:

- Lock the timetable before checking or inserting.
- Preserve ARCHIVED rejection.
- For ADD, bypass the requirement that a scheduled occurrence already exists.
- Validate term/entry date bounds and reject a holiday.
- Reject a second exception or another occurrence of the same entry on that date; preserve unambiguous `entryId + date` lookup.
- Validate slot, room and replacement teacher using existing services.
- Check actual dated occurrences for teacher, room and group conflicts, including moved/replaced/added occurrences.
- Preserve subgroup rules.
- Return existing `TIMETABLE_CONFLICT`/validation/state errors.
- Persist, audit and notify through existing paths.

In `occurrences`:

- Exclude ADD from the map that replaces/cancels recurring occurrences.
- Append exactly one occurrence on `onDate` using its selected pair and overrides.
- Return existing class status `SCHEDULED`, with `changeReason`; do not expand every timetable consumer’s status union unnecessarily.
- Ensure week, today, next, attendance lookup and term load see the added occurrence through the shared occurrence calculation.
- Ensure notification `pairNo` reflects the added pair.

ITs:

- Added occurrence exists once on the chosen date; original recurrence remains.
- It does not appear the following week.
- Conflicts with recurring, moved and added classes.
- Two concurrent conflicting ADD requests cannot both succeed.
- Duplicate source/date rejected without a 500.
- Out-of-term, holiday, invalid teacher/slot and archived timetable rejected.
- Preview, published week, `occurrenceOf` and load include the extra class.
- Removing ADD removes only that occurrence.
- Teacher/student mutation attempts return 403.
- Existing CANCELLED/MOVED/REPLACED behavior remains valid.

Dependency: B1. B1–B2 must land together.

**7.5-B3 — Contract and authorization synchronization. Five files; small, 1–2 hours.**

Change:

- `docs/api/openapi.json`
- `docs/api/ENDPOINTS.md`
- `frontend/src/shared/api/schema.d.ts`
- `T/AuthorizationMatrixIT.java`
- `docs/security/authorization-matrix.md`

Document ADD request/response examples and validation/conflict/forbidden responses. Regenerate contracts.

Dependency: B2 and creation of the authorization document.

**7.5-F1 — Reusable exception form and cache coverage. Four files; medium, 3–4 hours.**

Change:

- `B/entry-panel.tsx`
- Create `B/one-off-changes.tsx`
- `B/queries.ts`
- Create `B/queries.test.ts`

Extract `OneOffChanges` for use by both entry panel and week preview. Preserve old exception behaviors.

After add/delete, invalidate exception list, admin week preview, admin dashboard week, timetable load, teacher work queries and published timetable queries.

Vitest: ADD payload and complete invalidation after success/failure settlement.

Dependency: B3.

**7.5-F2 — ADD form. Five files; medium, 3–4 hours.**

Change `B/one-off-changes.tsx` and `L`; create `B/one-off-changes.test.tsx`.

Expose ADD date, pair, optional teacher/room overrides and reason. Prefill the preview’s selected date. Do not require an original occurrence on that date.

Render server conflicts with retryable input and requestId. Disable duplicate submission.

Keys: `builder.kind.ADD`, `addSingleClass`, `sourceClass`, `singleClassDate`, `singleClassAdded`, `noSourceClass`, `chooseSourceClass`.

Dependency: F1.

**7.5-F3 — Week-preview entry point and acceptance flow. Four files; medium, 3–5 hours.**

Change:

- `B/week-preview.tsx`
- `B/builder-page.tsx`
- Create `B/week-preview.test.tsx`
- `frontend/e2e/admin-builder.spec.ts`

Pass selected timetable status and source-entry choices to the preview. Add an “add single class” action available for DRAFT/PUBLISHED timetables. Show an actionable empty state when no source class exists.

Also add missing preview error/retry handling.

Playwright, desktop and phone:

1. Select a real source class in an isolated timetable.
2. Open week preview and add an occurrence on a free date/pair.
3. Verify it after reload and verify the original class remains.
4. Move to the following week and verify no recurrence.
5. Attempt a teacher/group/room conflict and verify refusal.
6. Delete the added exception and verify load/week refresh.
7. Verify keyboard operation and no horizontal overflow.

Do not place these cases inside the existing desktop-only skipped test.

Dependency: F2.

**Localization requirements**

Add keys with their consuming components; the unused-key checker must remain green. Every listed key must exist in all three `L` files.

Suggested primary labels:

| Key | uz | ru | en |
|---|---|---|---|
| `admin.studentCard.groupTitle` | Talabaning guruhi | Группа студента | Student group |
| `admin.studentCard.changeGroup` | Guruhni almashtirish | Изменить группу | Change group |
| `admin.studentCard.coursesTitle` | Kursga yozilishlar | Зачисления на курсы | Course enrollments |
| `admin.teachers.tab` | Oʻqituvchilar | Преподаватели | Teachers |
| `admin.teachers.weeklyHours` | Haftalik dars soatlari | Часы занятий за неделю | Weekly teaching hours |
| `admin.teachers.termLoad` | Semestr yuklamasi | Нагрузка за семестр | Term teaching load |
| `admin.removeStudent.title` | Hisobni oʻchirish va kurslardan chiqarish | Отключение учётной записи и отчисление с курсов | Deactivate and withdraw |
| `admin.removeStudent.retryRemaining` | Bajarilmagan amallarni qayta urinish | Повторить незавершённые действия | Retry remaining actions |
| `nav.adminProblems` | Muammolar | Проблемы | Problems |
| `builder.kind.ADD` | Qoʻshimcha dars | Дополнительное занятие | Additional class |
| `builder.addSingleClass` | Bir martalik dars qoʻshish | Добавить разовое занятие | Add a single class |

Use U+02BB in Uzbek `oʻ`/`gʻ`. Reuse existing status, reason, retry, cancel and error-code keys. Deactivation descriptions must explicitly state that history is retained.

**Dependency order and parallel work**

1. Start 7.1-B1, 7.2 query/test work and 7.5-B1 independently.
2. Publish 7.1 contract, then build the student card.
3. Build 7.3 after student enrollment hooks exist.
4. Start 7.4 backend only after Phase 5 schema and allowance behavior are available.
5. Build problems UI after its generated contract.
6. Finish ADD backend and generated contract before ADD UI.
7. Run cross-feature acceptance after all streams merge.

Parallel work may use separate files, but serialize changes to:

- `L`
- Generated API files
- `A/user-page.tsx`
- `frontend/e2e/admin-people.spec.ts`
- `AuthorizationMatrixIT.java`
- The authorization document

Apply V035 before V036 unless migration numbers are reassigned before either is applied.

**Validation and acceptance**

Implementers must run backend unit/ArchUnit tests, relevant tagged integration tests and OpenAPI contract validation. Frontend gates are lint, strict typecheck, Vitest, i18n check, production build and Playwright against the running backend.

Browser acceptance remains required during implementation: 390/768/1440 px, light/dark, uz/ru/en, keyboard and reduced motion. This planning review does not satisfy those runtime gates.

Success means:

- Existing capabilities were reused.
- Student enrollment filtering is scoped and paginated.
- Group/status changes persist with history.
- Teacher weekly and term figures are correctly distinguished.
- Combined removal reports partial outcomes honestly.
- All six problems are discoverable with working fix destinations.
- ADD creates one dated occurrence with real conflict checks.
- Teacher authorization and student isolation tests pass.
- No mock application data, hard deletion or new cross-module persistence dependency was introduced.

**SUMMARY:**

- Fayllar o‘zgartirilmadi; read-only muhit yozishni taqiqlaydi.
- Guruh ko‘rsatish va TEACHER filtri allaqachon mavjud.
- `userId` bo‘yicha admin enrollment filtri yetishmaydi.
- Deaktivatsiya va withdrawal API’lari mavjud; yagona tasdiqlash oqimi yo‘q.
- `/load` semestrni hisoblaydi; haftalik hisob uchun `/week` kerak.
- Olti turdagi problems endpointi va sahifasi yo‘q.
- `ADD` UI, OpenAPI, Java enum va DB constraintida yo‘q.
- Yuqorida ≤5 faylli bo‘laklar, kontraktlar, testlar va i18n rejasi berildi.
- Brauzer va testlar ishga tushirilmadi.