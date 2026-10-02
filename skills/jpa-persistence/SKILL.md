---
name: jpa-persistence
description: JPA/Hibernate rules for this repo — fetching, N+1, cascades, locking.
---
# JPA rules

## Mapping
- `@Entity` in `<module>/domain`. Table name explicit: `@Table(name = "lesson_progress")`.
- Every association is `LAZY`, including `@ManyToOne` (the default is EAGER — override it).
- `@ManyToOne` uses `@JoinColumn(name = "course_id", nullable = false)` + a matching
  `optional = false`.
- Collections: `@OneToMany(mappedBy = ..., cascade = {PERSIST, MERGE}, orphanRemoval = true)`
  **only** for composition. Never `CascadeType.ALL`, never `REMOVE` on a non-composition.
- No bidirectional association unless both sides are genuinely needed. Prefer a repository query.
- `equals`/`hashCode` from `BaseEntity` (id-based). **Never** include associations in
  `toString()` — that is how lazy-init exceptions and log bombs happen.
- Enums: `@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "text")`.
- JSONB: `@JdbcTypeCode(SqlTypes.JSON)` on a `Map<String,Object>` or a typed record.
- `@Version private long version;` on concurrently-edited entities (progress, enrollment,
  course, submission, attempt).

## Reading
- Default to projections. If a controller needs 3 fields, write an interface/record projection
  query — do not load the aggregate.
- N+1 kills: use `@EntityGraph(attributePaths = {...})` on the repository method, or an
  explicit `join fetch` JPQL query. **Never** fix it by making an association EAGER, and never
  enable `hibernate.enable_lazy_load_no_trans`.
- Pagination + `join fetch` on a collection = in-memory pagination warning. Use a two-step
  query (ids page, then fetch by ids) — `LessonRepository.findPageOfIds` is the pattern.
- `@Transactional(readOnly = true)` on read services: Hibernate skips dirty checking.

## Writing
- Student-scoped queries take the caller's id **as a parameter in the WHERE clause**:
  `findByIdAndEnrollment_StudentUserId(id, callerId)`. Load-then-check is a bug here.
- Concurrency: rely on DB constraints first (partial unique indexes), optimistic locking
  second. Catch `OptimisticLockingFailureException` in the service and translate to
  `ErrorCode.CONCURRENT_MODIFICATION` (or retry once for progress updates).
- Bulk updates: `@Modifying(clearAutomatically = true, flushAutomatically = true)`.
- Never `saveAll` a 10k list without batching (`hibernate.jdbc.batch_size=50` is configured).

## Hibernate config already set (don't fight it)
`ddl-auto=validate` · `batch_size=50` · `order_inserts/order_updates=true` ·
`in_clause_parameter_padding=true` · statistics off in prod ·
`fail_on_pagination_over_collection_fetch=true` (so the mistake above becomes an error).
