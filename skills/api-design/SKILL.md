---
name: api-design
description: REST conventions, DTOs, pagination, errors and OpenAPI for this repo.
---
# API rules

Base `/api/v1`. Additive-only within v1.

## Route shape
```
/api/v1/auth/*                     public-ish
/api/v1/subjects, /courses         catalogue (auth required, published only)
/api/v1/enrollments/me             the caller's own
/api/v1/courses/{id}/modules       nested, enrolment-scoped
/api/v1/lessons/{id}
/api/v1/quiz-attempts/*            the caller's own attempts
/api/v1/submissions/*              the caller's own submissions
/api/v1/teacher/**                 requires teacher permissions + CourseAccessGuard
/api/v1/admin/**                   requires admin permissions
```
Role is **never** a path parameter, and never trusted from the client. `/me` means "derived
from the token".

## DTOs
- Records only, in `<module>/api/dto`. Never a JPA entity in a signature.
- Requests: Bean Validation annotations on every field (`@NotBlank`, `@Size`, `@Min`,
  `@Valid` for nested). Requests never carry `id`, `createdAt`, `status` or `score` unless
  that is genuinely the operation.
- Responses: no internal ids beyond what the client needs, no `password_hash`, no
  `solution_code`, no hidden-test expectations, no other student's identity.
- Localized responses include `resolvedLocale`, `availableLocales`, `isFallback`.

## Lists
Always paginated: `?page=0&size=20&sort=position,asc`. `size` capped at 100 by
`PageableSupport.sanitize`. Envelope:
```json
{"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0,"hasNext":false}
```

## Status codes
`200` read/update · `201` + `Location` create · `202` accepted async (submissions) ·
`204` delete · `400` validation (`VALIDATION_FAILED`) · `401` missing/bad token ·
`403` authenticated but not allowed · `404` not found **or deliberately hidden** ·
`409` conflict/duplicate/state · `422` domain rule violation · `429` rate limited ·
`500` unexpected (never with detail).

## Errors — never improvise
Throw a `DomainException` subclass with an `ErrorCode`. `GlobalExceptionHandler` renders:
```json
{"code":"ENROLLMENT_DUPLICATE","message":"Student already enrolled in this course",
 "details":{"courseId":"..."},"requestId":"...","timestamp":"...","path":"/api/v1/..."}
```
New failure mode ⇒ add an `ErrorCode` constant + document it in `docs/api/error-model.md`.
Messages are human-readable and safe: no SQL, no class names, no stack traces, no
"user not found" (use the generic auth error).

## OpenAPI
Every endpoint: `@Operation(summary, description)`, `@ApiResponse` per documented status,
`@Schema` with `example` on DTO fields, security requirement declared. An endpoint without
an example payload is not done — the future frontend must never have to guess.

## Idempotency
`POST /submissions` and `POST /quiz-attempts/{id}/submit` honour `Idempotency-Key`. Same key +
same payload ⇒ the original response replayed. Same key + different payload ⇒ `409`.
