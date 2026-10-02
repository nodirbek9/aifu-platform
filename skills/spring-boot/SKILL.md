---
name: spring-boot
description: Spring Boot configuration, transactions, caching, scheduling and profiles in this repo.
---
# Spring Boot rules

## Config
- All settings via `@ConfigurationProperties` **records** in `<module>/app/config` (e.g.
  `JwtProperties`, `ExecutionProperties`, `StorageProperties`). No stray `@Value`.
- Everything environment-specific comes from env vars with a dev default in
  `application-dev.yml`. Property names mirror `.env.example`.
- Profiles: `dev` (seed data, verbose logging, swagger on), `test` (Testcontainers),
  `prod` (swagger off, `json` logs, strict CORS). `spring.jpa.hibernate.ddl-auto=validate`
  in **all** of them.
- Secrets are never in any `application*.yml`. Placeholder + env var only.

## Transactions
- `@Transactional` on application-service methods only. Read paths:
  `@Transactional(readOnly = true)`.
- Keep transactions short. **Never** call the sandbox, an HTTP client, or object storage
  inside a transaction.
- Need work after commit? `TransactionalEventPublisher` +
  `@TransactionalEventListener(phase = AFTER_COMMIT)`. Progress recomputation and notification
  creation use this.
- The submission insert **and** its `execution_job` insert are one transaction (ADR-007).
  Do not split them.
- Self-invocation does not start a transaction. Split the method into another bean.

## Caching (Caffeine, no Redis)
Cache names: `subjects`, `courses`, `course-structure`, `role-permissions`,
`programming-languages`. Never cache anything student-scoped or authorization-dependent —
a per-user cache key is a leak waiting to happen. Evict explicitly on write in the same
service that writes.

## Scheduling
`@Scheduled` lives in `<module>/app/job`. Every job wraps itself in
`AdvisoryLock.tryRun("job-name", ...)` so a second instance is harmless. Jobs log start/finish
with a count. Current jobs: expired-token purge, execution-lease sweeper, notification
dispatch, progress recompute.

## Web
- One `@RestControllerAdvice` (`GlobalExceptionHandler`) in `common.error`. Do not add another.
- Filters order: `RequestIdFilter` → `RateLimitFilter` → `JwtAuthenticationFilter`.
- Jackson: `WRITE_DATES_AS_TIMESTAMPS` off (ISO-8601 UTC), `FAIL_ON_UNKNOWN_PROPERTIES` on
  for requests, non-null inclusion for responses.
- Validate with `@Valid` on the body + `@Validated` on the controller for params.
