---
name: java-backend
description: Java 21 coding conventions for this repo. Load before writing any Java here.
---
# Java conventions

Java 21. **No Lombok** (ADR-002) — records for DTOs, explicit constructors for entities.

## Do
- DTOs are `record`s in `<module>/api/dto`, named `XxxRequest` / `XxxResponse` / `XxxSummary`.
- Constructor injection, fields `private final`. No `@Autowired` on fields. No setter injection.
- Entities: `protected` no-arg constructor for JPA + a meaningful public constructor +
  intention-revealing mutators (`publish()`, `withdraw(reason)`), not bulk setters.
- Extend `common.domain.BaseEntity` (UUID id, createdAt, updatedAt, id-based equals/hashCode)
  or `AuditableEntity` (adds createdBy/updatedBy) or `VersionedEntity` (adds `@Version`).
- Enums for every state; persist with `@Enumerated(EnumType.STRING)`. Never ordinals.
- `Optional` as a return type; never as a field or parameter.
- Fail fast in constructors: `Objects.requireNonNull`, `common.util.Guards.require(...)`.
- Domain rule violations throw a `common.error.DomainException` subclass carrying an
  `ErrorCode`. Never throw bare `RuntimeException`, never `IllegalStateException` for a
  business rule.
- Sealed interfaces + pattern matching for closed hierarchies (content block payloads,
  sandbox results).
- `BigDecimal` for scores/credits/points. **Never `double` for anything graded.**
- `Instant` for timestamps (`timestamptz`), `LocalDate` for academic dates. Never `java.util.Date`.

## Don't
- No business logic in controllers. No `if (user.getRole() == ADMIN)` outside the guards.
- No service class over ~300 lines — split by use case.
- No static mutable state. No `static` helper holding a `@Component`.
- No `CascadeType.ALL`. No `FetchType.EAGER`. Ever.
- No JPA entity in a controller signature, and no entity in a Jackson-serialized position.
- No `e.printStackTrace()`, no `System.out`. Use SLF4J with placeholders.
- No checked exceptions in application-service signatures.
- No inheritance for code reuse; compose.

## Naming
`XxxService` (application service) · `XxxQueryService` (read-only) · `XxxRepository` (Spring
Data) · `XxxGuard` (authorization) · `XxxValidator` (invariants) · `XxxMapper` (MapStruct) ·
`XxxHandler` (strategy per enum value) · `XxxProperties` (config record).

## Logging
`LOG.info("Submission {} queued for task {}", submissionId, taskId)` — ids, never content.
**Never log:** passwords, tokens (even partially), student source code, quiz answer keys,
sanitized-or-not content bodies. `SensitiveDataFilter` is a backstop, not permission.
