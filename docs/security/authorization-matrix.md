# Authorization Matrix & Security Specification

This document defines the authorization model, permission boundaries, and error behaviors for
administrative and protected operations in the AIFU Learning Platform, with particular focus on
Phase 7 ("Admin powers") operations and timetable exception handling.

---

## 1. Authorization Architecture

Authorization in the platform follows three complementary layers:

1. **Coarse Role & Authority Checks (`@PreAuthorize`)**:
   Endpoints specify required authorities (`hasAuthority('PERMISSION_NAME')`) or roles (`hasRole('ROLE_NAME')`).
   Permissions are granular capabilities seeded in `db/migration/V003__roles_and_permissions.sql`
   (e.g., `TIMETABLE_MANAGE`, `ENROLLMENT_READ`, `ENROLLMENT_MANAGE`, `USER_MANAGE`, `GROUP_MANAGE`, `ANALYTICS_PLATFORM_READ`).
2. **Resource-Level & Ownership Checks (`AccessGuard`)**:
   Even when an authority is held, operations scoped to a specific entity verify access ownership
   (e.g., `requireCourseEditor(courseId)`, student self-ownership filter in SQL queries).
3. **Defense-in-Depth & Data Isolation**:
   Student-scoped reads strictly filter by the caller's student user/enrollment ID directly in SQL
   (verified by `StudentDataIsolationIT`).

### Standard Authentication & Authorization HTTP Responses

All responses adhere to the uniform `ApiError` envelope:
```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable explanation",
  "details": {},
  "requestId": "uuid",
  "timestamp": "2026-10-15T08:00:00Z",
  "path": "/api/v1/..."
}
```

| HTTP Status | Error Code | Trigger Condition |
|---|---|---|
| **401 Unauthorized** | `AUTH_TOKEN_MISSING` | Missing `Authorization: Bearer <token>` header |
| **401 Unauthorized** | `AUTH_TOKEN_INVALID` | Malformed, forged, or unparseable JWT |
| **401 Unauthorized** | `AUTH_TOKEN_EXPIRED` | Expired access token |
| **403 Forbidden** | `AUTH_FORBIDDEN` | Caller lacks the required role or authority |
| **403 Forbidden** | `NOT_COURSE_TEACHER` | Teacher attempting to modify a course they do not instruct |
| **403 Forbidden** | `NOT_ENROLLED` | Student attempting to access course content without active enrollment |

---

## 2. Phase 7 Administrative Operations Matrix

The following matrix documents the Phase 7 administrative capabilities, required permissions, and expected outcomes across all user roles:

| Operation | Endpoint | Method | Required Authority | ADMIN | TEACHER (Assigned) | TEACHER (Other) | STUDENT | Anonymous |
|---|---|---|---|---|---|---|---|---|
| **Cross-User Enrollments** | `/api/v1/admin/enrollments?userId=...` | GET | `ENROLLMENT_READ` | 200 OK | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Enrollment Status Change** | `/api/v1/admin/enrollments/{id}/status` | PUT | `ENROLLMENT_MANAGE` | 200 OK | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Group Student Move** | `/api/v1/admin/groups/{id}/students` | POST | `GROUP_MANAGE` | 200 OK | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Administrative Problems** | `/api/v1/admin/problems` | GET | `ANALYTICS_PLATFORM_READ` | 200 OK | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Account Lifecycle** | `/api/v1/admin/users/{id}/status` | PUT | `USER_MANAGE` | 200 OK | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Clear Account Lockout** | `/api/v1/admin/users/{id}/unlock` | POST | `USER_MANAGE` | 204 No Content | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Add Timetable Exception** | `/api/v1/admin/timetable-entries/{entryId}/exceptions` | POST | `TIMETABLE_MANAGE` | 201 Created | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |
| **Remove Timetable Exception** | `/api/v1/admin/timetable-exceptions/{id}` | DELETE | `TIMETABLE_MANAGE` | 204 No Content | 403 Forbidden | 403 Forbidden | 403 Forbidden | 401 Unauthorized |

> **Note on Course Teachers**: Teachers assigned to a course have teaching permissions on that course (grades, lesson plans, class journals), but have **zero access** to administrative cross-user roster management, account states, or builder timetable mutations. Attempting any administrative call returns `403 AUTH_FORBIDDEN`.

---

## 3. Timetable Exceptions: ADD Contract & Semantics

An `ADD` exception (`ExceptionKind.ADD`) creates a single-date additional occurrence of an existing recurring timetable entry.
It inherits the course, session type, student groups, and subgroup from the parent timetable entry. It does not alter recurring schedule patterns or term templates.

### 3.1 Endpoint

`POST /api/v1/admin/timetable-entries/{entryId}/exceptions`

- **Authorization**: `TIMETABLE_MANAGE` (Admin only)
- **Path Parameter**: `entryId` (UUID of the parent recurring timetable entry)
- **Query Parameter**: `locale` (optional)

### 3.2 Request Schema (`ExceptionRequest`)

| Field | Type | Constraint | Description |
|---|---|---|---|
| `kind` | `String` | `@NotNull`, `"ADD"` | Exception kind enum |
| `onDate` | `LocalDate` | `@NotNull` | Date on which the added class is held |
| `newPairSlotId` | `UUID` | `@NotNull` for ADD | Bell schedule pair slot for the added occurrence |
| `newDate` | `LocalDate` | Must be `null` for ADD | Used only for `MOVED` exceptions |
| `newRoomId` | `UUID` | Optional | Room override; defaults to entry's room if omitted |
| `newTeacherUserId` | `UUID` | Optional | Teacher override; defaults to entry's teacher if omitted |
| `reason` | `String` | `@NotNull`, trimmed length 1–300 | Administrative explanation for the added class |

### 3.3 Payload & Response Examples

#### Success Request (201 Created)
```http
POST /api/v1/admin/timetable-entries/b1234567-0000-0000-0000-000000000001/exceptions
Authorization: Bearer <admin-jwt>
Content-Type: application/json

{
  "onDate": "2026-10-15",
  "kind": "ADD",
  "newPairSlotId": "80000000-0000-0000-0000-000000000002",
  "newRoomId": "90000000-0000-0000-0000-000000000005",
  "newTeacherUserId": "a0000000-0000-0000-0000-000000000003",
  "reason": "Makeup lecture for missed holiday class"
}
```

#### Success Response
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "e0000000-0000-0000-0000-000000000001",
  "entryId": "b1234567-0000-0000-0000-000000000001",
  "onDate": "2026-10-15",
  "kind": "ADD",
  "newDate": null,
  "newPairNo": 2,
  "newRoomId": "90000000-0000-0000-0000-000000000005",
  "newRoomLabel": "Room 204",
  "newTeacherUserId": "a0000000-0000-0000-0000-000000000003",
  "newTeacherName": "Dr. Rustam Karimov",
  "reason": "Makeup lecture for missed holiday class"
}
```

---

## 4. Error Responses & Failure Scenarios

### 4.1 Validation Failure (400 Bad Request)

Returned when required fields are missing, invalid, or violate ADD invariants (e.g. `newDate` provided, `newPairSlotId` missing, or blank `reason`):

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "code": "VALIDATION_FAILED",
  "message": "Validation failed for object='exceptionRequest'. Error count: 1",
  "details": {
    "fields": {
      "addValid": "An added class requires newPairSlotId, no newDate, and a reason between 1 and 300 characters"
    }
  },
  "requestId": "c1f7a0b2-3344-4899-bb01-9a7c6b543210",
  "timestamp": "2026-10-15T09:15:00Z",
  "path": "/api/v1/admin/timetable-entries/b1234567-0000-0000-0000-000000000001/exceptions"
}
```

### 4.2 Timetable Conflict (409 Conflict)

Returned when the teacher, room, or student group is already booked for another class or exception in that pair on that date:

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "code": "TIMETABLE_CONFLICT",
  "message": "Class conflicts with existing scheduled class",
  "details": {
    "conflicts": [
      {
        "type": "TEACHER",
        "entryId": "c9876543-0000-0000-0000-000000000002",
        "subjectCode": "DAS1110",
        "courseTitle": "Dasturlash asoslari",
        "teacherName": "Dr. Rustam Karimov",
        "roomLabel": "Auditorium 1",
        "groupCodes": ["BK-DI26/1"],
        "dayOfWeek": 4,
        "pairNo": 2,
        "weekParity": "EVERY"
      }
    ],
    "warnings": []
  },
  "requestId": "e4a2d81f-1122-4455-8899-aa1122334455",
  "timestamp": "2026-10-15T09:16:00Z",
  "path": "/api/v1/admin/timetable-entries/b1234567-0000-0000-0000-000000000001/exceptions"
}
```

### 4.3 Forbidden (403 Forbidden)

Returned when caller is not an administrator with `TIMETABLE_MANAGE` (e.g. students or teachers):

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "code": "AUTH_FORBIDDEN",
  "message": "Access is denied",
  "details": null,
  "requestId": "f891a27e-3461-419b-a01e-7b7891fa1201",
  "timestamp": "2026-10-15T09:17:00Z",
  "path": "/api/v1/admin/timetable-entries/b1234567-0000-0000-0000-000000000001/exceptions"
}
```

### 4.4 Unauthorized (401 Unauthorized)

Returned when unauthenticated or token is missing/expired:

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "code": "AUTH_TOKEN_MISSING",
  "message": "Full authentication is required to access this resource",
  "details": null,
  "requestId": "a0011223-4455-6677-8899-aabbccddeeff",
  "timestamp": "2026-10-15T09:18:00Z",
  "path": "/api/v1/admin/timetable-entries/b1234567-0000-0000-0000-000000000001/exceptions"
}
```

---

## 5. Verification & Test Suite

The contracts and permission matrix specified here are continuously enforced by the test suite:

- `uz.aifu.lms.AuthorizationMatrixIT`: Verifies role denial (403 `AUTH_FORBIDDEN`) and anonymous denial (401) on administrative routes including timetable exceptions and cross-user enrollments.
- `uz.aifu.lms.TimetableAddExceptionIT`: Integration test verifying ADD occurrence generation, conflict detection (409 `TIMETABLE_CONFLICT`), concurrency locks, Bean Validation constraints, date boundaries, and authorization isolation.
- `uz.aifu.lms.AdminPeopleIT`: Tests cross-user enrollment filtering and admin-only access control.

## 6. Phase 7.4: Administrative Problems

Source of truth: [AdminProblemsController](../../backend/api/src/main/java/uz/aifu/lms/analytics/api/AdminProblemsController.java),
[AdminProblemsService](../../backend/api/src/main/java/uz/aifu/lms/analytics/app/AdminProblemsService.java),
and [AdminProblemDtos](../../backend/api/src/main/java/uz/aifu/lms/analytics/api/dto/AdminProblemDtos.java).
The generated [OpenAPI contract](../api/openapi.json) and [endpoint catalogue](../api/ENDPOINTS.md)
must be regenerated from this implementation before a frontend consumes the endpoint.

### Read contract and authorization

`GET /api/v1/admin/problems?type=&page=&size=&sort=` requires a bearer access token and
`ANALYTICS_PLATFORM_READ`. ADMIN holds that permission; ordinary teachers, including teachers
assigned to an affected course, and students do not. Course assignment grants no access to
this platform-wide read model. This is a read-only service transaction over the analytics-owned
SQL view; reading a row never changes an account, enrollment, timetable, or quiz attempt.

| Caller / request | Expected response | Data exposure |
|---|---|---|
| ADMIN with `ANALYTICS_PLATFORM_READ`, valid parameters | 200 `PageResponseAdminProblemView` | Requested page only |
| Ordinary assigned or unassigned TEACHER | 403 `AUTH_FORBIDDEN` | No problem rows or resource identities |
| STUDENT | 403 `AUTH_FORBIDDEN` | No problem rows, including the caller's own problems |
| Anonymous, invalid or expired bearer token | 401 `AUTH_TOKEN_MISSING` | No problem rows |
| Authorized caller with invalid `type` or unsupported sort property | 400 `VALIDATION_FAILED` | No problem rows |

For these protected routes, `JwtAuthenticationFilter` leaves invalid/expired tokens
unauthenticated and `JsonAuthenticationEntryPoint` returns `AUTH_TOKEN_MISSING`; distinct
`AUTH_TOKEN_INVALID` / `AUTH_TOKEN_EXPIRED` responses in the general error table above are not
produced by this filter. Errors include `code`, `message`, `requestId`, and `path`. Optional
`details` and `timestamp` depend on the error handler; do not require them on security-filter
responses. Denied reads must not contain `content`, `problemKey`, `fullName`, `userId`, or
`enrollmentId`, and must not include credentials, answers, or internal exception details.

Omitting `type` includes all six categories. Filtering, counting and pagination happen in SQL;
`totalElements` describes the filtered result, not the current page length. Page indices start
at zero and size is capped at 100. Supported sort properties are `type` and `problemKey` only.
The default is `type ASC, problemKey ASC`; missing default tie-breakers are appended to an
explicit sort. A stable `problemKey` is `type + ":" + primary-resource-id`. The same resource
may appear in different categories, but must not be duplicated within a category.

### Row data and administrative fixes

| Type | Resource identifiers | Administrative fix / required permission |
|---|---|---|
| `STUDENT_WITHOUT_GROUP` | `userId` | `POST /api/v1/admin/groups/{id}/students`, `GROUP_MANAGE` |
| `GROUP_WITHOUT_PUBLISHED_ENTRIES` | `groupId`, `groupCode` | Build/publish a timetable through existing admin endpoints, `TIMETABLE_MANAGE` |
| `COURSE_WITHOUT_TEACHER` | `courseId`, `subjectCode`, `cohortCode`, `termName` | `POST /api/v1/admin/courses/{id}/teachers`, `COURSE_TEACHER_MANAGE` |
| `ACTIVE_ENROLLMENT_INACTIVE_USER` | `userId`, `enrollmentId`, `courseId` | Account status: `USER_MANAGE`; enrollment transition: `ENROLLMENT_MANAGE` and course-editor guard |
| `LOCKED_ACCOUNT` | `userId`, `lockedUntil` | `POST /api/v1/admin/users/{id}/unlock`, `USER_MANAGE` |
| `MIDTERM_ATTEMPTS_EXHAUSTED` | `userId`, `courseId`, `enrollmentId`, `quizId` | `POST /api/v1/admin/quizzes/{quizId}/enrollments/{enrollmentId}/attempt-grants`, `QUIZ_ATTEMPT_GRANT` |

Rows additionally expose stored `fullName`, `groupId`, and `groupCode` where applicable.
Midterm rows include `attemptsUsed`, `attemptsAllowed`, and `grantAvailable`.
Inapplicable null properties are absent from JSON. No frontend URL, password hash, token,
quiz answer, or solution code is part of this DTO. Identifiers in a row provide context;
they do not authorize the caller to perform the proposed fix.

The midterm allowance comes from the exact quiz/enrollment pair: the maximum attempt number
versus `quizzes.max_attempts + count(quiz_attempt_grants)`. Unlimited quizzes, completed/passed
midterms, usable in-progress attempts, and submitted attempts awaiting grading are excluded.
Expired in-progress attempts can count without being mutated by this read. `grantAvailable`
means no grant already exists for that pair; it is not a permission check. Each write
endpoint independently validates permission and current state, so a stale row cannot authorize
another grant or bypass a transition rule.

### Related Phase 7 permission boundaries

The matrix in section 2 states the intended Phase 7 administrative outcomes. Successful
mutations there presume valid resources and transitions; authorization does not guarantee a
write will pass domain validation. Account deactivation and enrollment withdrawal are separate
requests requiring `USER_MANAGE` and `ENROLLMENT_MANAGE` respectively, with partial success
possible. No combined atomic operation or hard-delete capability is implied.

Existing staff reads retain their authorization: `/api/v1/groups` and its roster reads require
`GROUP_READ`, held by ADMIN and TEACHER. Course teacher roster reads continue to require
`ENROLLMENT_READ` plus the course ownership guard.

**Cross-user enrollment enforcement gap (outside 7.4-B3):** V003 also grants `ENROLLMENT_READ`
to TEACHER. The current `AdminEnrollmentController.listByCourse` checks that authority and
only invokes the course-editor guard when `courseId` is supplied; `userId` alone has no
administrative caller check. Consequently the 403 requirement in section 2 is not established
by the current source for that filter. `AdminPeopleIT` and `AuthorizationMatrixIT` require
teacher denial, including assigned teachers, but this documentation change does not repair
that earlier chunk or claim those tests passed.

### Verification

- `AdminProblemsIT`: all six predicates, type filtering, totals, stable paging, size cap,
  allowed/invalid sorts, no duplicates, and removal after underlying state changes.
- `AuthorizationMatrixIT`: ADMIN access; assigned/unassigned teacher and student denial;
  anonymous denial; no problem rows in denied responses, for all/type-filtered reads.
- `StudentDataIsolationIT`: students cannot read another student's administrative problems
  or obtain identifiers from denied requests and errors.
- `ArchitectureBoundaryTest`: analytics keeps its existing module boundaries.
- `OpenApiContractIT`: the committed contract matches the running application. Regeneration
  uses `-Dopenapi.write=true`, followed by the endpoint catalogue and frontend type generators.

These are required checks, not a claim that an integration run completed. A Docker-dependent
test skipped because Docker is inaccessible does not verify this matrix or regenerate OpenAPI.
