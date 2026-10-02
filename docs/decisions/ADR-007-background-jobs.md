# ADR-007 — Background jobs: PostgreSQL `SKIP LOCKED` queue, no broker

**Status:** Accepted

## Context
Needed async work: code execution (must be async — it takes seconds), notification fan-out,
progress/analytics recomputation, file post-processing, token cleanup. Brief §36-37: add
Redis/RabbitMQ only when justified.

## Options
1. **Synchronous execution inside the HTTP request.** A 10-second timeout burns a request
   thread and gives the student a hung page. Rejected for code execution.
2. **RabbitMQ / Kafka.** Real queue semantics; adds a broker to run, monitor, secure and
   back up, plus the classic dual-write problem (DB commit + publish are not atomic ⇒ needs
   an outbox anyway). Unjustified at ~10s of submissions per hour.
3. **Spring `@Async` + in-JVM executor.** Zero infrastructure, but the job dies with the JVM
   and would run untrusted work inside the API. Rejected.
4. **PostgreSQL table + `SELECT ... FOR UPDATE SKIP LOCKED`.** Transactional enqueue (no
   dual write), durable, observable with plain SQL, multi-consumer safe, retryable.

## Decision
**Option 4.** `execution_jobs` is the queue; the pattern generalizes via a small
`JobQueue<T>` abstraction.

```sql
UPDATE execution_jobs SET status='CLAIMED', claimed_by=:worker,
       claimed_at=now(), lease_expires_at=now() + interval '2 minutes',
       attempt_count = attempt_count + 1
WHERE id IN (
  SELECT id FROM execution_jobs
  WHERE status='PENDING' AND available_at <= now()
  ORDER BY priority DESC, available_at
  FOR UPDATE SKIP LOCKED LIMIT :batch)
RETURNING *;
```

Semantics: at-least-once delivery; **idempotent handlers** (a re-run overwrites the same
`execution_result` keyed by submission, so a duplicate run is harmless); exponential backoff
via `available_at`; `attempt_count >= max_attempts` ⇒ `DEAD` + audit + teacher-visible
`INTERNAL_ERROR` verdict; expired leases reclaimed by a sweeper so a crashed worker's job
does not sit `CLAIMED` forever.

The enqueue happens **in the same transaction** as the `submissions` insert. No submission can
exist without its job, and no job without its submission. This is the single most valuable
property and the reason a broker would have needed an outbox table regardless.

In-process scheduled work (`@Scheduled`, in the API) is limited to cheap maintenance:
expired-token purge, lease sweeping, progress recompute, notification dispatch.
All are guarded by an advisory-lock so they stay safe if a second instance appears.

**Async contract for the frontend:** `POST /submissions` → `202 Accepted` with
`{submissionId, status: "QUEUED"}`; the client polls
`GET /api/v1/submissions/{id}` (documented poll interval + `Retry-After`) until `COMPLETED`.
SSE/WebSocket push is a later, additive improvement; the poll contract stays valid.

## Consequences
No broker to operate; jobs are inspectable with `SELECT`; enqueue is transactional; backups
cover the queue. Costs polling latency (~1 s, configurable — irrelevant when runs take
seconds) and some DB write load (trivial here). Not suitable for very high throughput fan-out.

## Revisit when
Sustained >50 jobs/minute, or a genuine pub/sub fan-out appears (multiple independent
consumers of the same event).
