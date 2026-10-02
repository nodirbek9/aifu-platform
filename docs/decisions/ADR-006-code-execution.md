# ADR-006 — Code execution: Judge0 behind a `SandboxEngine` port

**Status:** Accepted · **Security-critical: changes require review**

## Context
Students submit Python and Go source code (brief §17). It is **hostile by default**:
infinite loops, fork bombs, memory exhaustion, filesystem probing, outbound network calls,
attempts to read other students' data or the platform's secrets. Brief §18 forbids running it
in the API process.

## Options
1. **In-process** (`ProcessBuilder`, `ScriptEngine`, GraalVM polyglot). Forbidden. Shares the
   JVM's filesystem, network, env vars and DB credentials with attacker-controlled code.
2. **Same host, different process, no container.** Requires hand-rolling seccomp/cgroups/
   namespaces/rlimits correctly. We will get it wrong. Rejected.
3. **Judge0** — mature, purpose-built OSS judge (isolate/cgroups, per-language images,
   CPU/wall/memory/pids/stack limits, network off). Battle-tested by competitive-programming
   platforms.
4. **Own container-per-submission runner** (`docker run --rm --network=none ...`).
   Full control, but we own the hardening, image lifecycle and DoS surface.
5. **Commercial API** (Sphere, Piston cloud). Sends student code off-premise; a university
   data-residency question we must not answer unilaterally. Rejected for v1.

## Decision
**A `SandboxEngine` port with Judge0 as the default adapter.**

```java
public interface SandboxEngine {
    SandboxRunResult run(SandboxRunRequest request);   // one program, one stdin, hard limits
    boolean supports(String languageCode);
}
```
Adapters: `Judge0SandboxEngine` (default), `DockerRunSandboxEngine` (reference/air-gapped
fallback, `--network=none --read-only --pids-limit --memory --cpus`, non-root, dropped caps,
`tmpfs /tmp:noexec`), `NoopSandboxEngine` (tests). Selected by `EXECUTION_ENGINE`.

The port matters more than the engine: it keeps the choice reversible and makes the entire
untrusted-code surface **one interface with three implementations**, all outside the API.

**Topology — the non-negotiable part.**
```
 API (backend/api)              Worker (backend/execution-worker)        Sandbox
 ─────────────────              ────────────────────────────────         ───────
 POST /submissions              claim job (FOR UPDATE SKIP LOCKED)       judge0 / docker
 persist submission  ──►  DB ◄──── poll ──►  build run request  ──────►  isolated, no network
 enqueue execution_job          receive result, grade, write back        killed on limits
 return 202 + submissionId      update submission + progress + grade
```
- The API **never** talks to the sandbox and never sees untrusted output before it is
  truncated and sanitized by the worker.
- The worker holds a **restricted DB role** (`aifu_execution_worker`): SELECT on
  `submissions`, `coding_tasks`, `coding_task_test_cases`, `programming_languages`;
  INSERT/UPDATE on `execution_jobs`, `execution_results`, `execution_test_results`,
  `submissions.status/verdict/score`. **No access to `users`, `refresh_tokens`, `audit_log`.**
  A fully compromised worker still cannot read a password hash.
- The worker exposes **no HTTP port**. It is a puller, so nothing can reach it inbound.
- Judge0 runs on an internal Compose network with `internet: false`.

**Per-run limits** (task-overridable, hard-capped by config):
CPU time 5 s · wall time 10 s · memory 256 MB · processes/threads 64 · stack 64 MB ·
output 64 KB (truncated, flagged) · source 128 KB · no network · read-only rootfs ·
writable `tmpfs /tmp` (16 MB, `noexec`, `nosuid`) · non-root uid · all capabilities dropped ·
never `--privileged`, never a mounted Docker socket in the sandbox.

**Anti-DoS:** per-student rate limit and max concurrent running submissions; bounded worker
concurrency; queue depth metric; per-job `attempt_count` with `max_attempts` then `DEAD`
(no infinite retry storms); a `RUNNING` job whose lease expires is reclaimed, not duplicated.

**Result hygiene:** hidden test cases never return `expected_output`, and stdout/stderr of a
hidden test is withheld from students (teachers may see it). `solution_code` is never
serialized into a student DTO. Output is truncated before it is stored.

## Consequences
Untrusted execution is physically outside the API, behind one auditable interface, with a
blast radius bounded by a restricted DB role and a network-less sandbox. Costs an extra
deployable and Judge0's own Postgres+Redis (its internal requirement, not ours). Async means
the frontend polls or subscribes for a result — an explicit API contract, see ADR-007.

**Not yet verified in this environment.** The sandbox limits above are configuration written
from the threat model; they have not been executed or penetration-tested here. See
`docs/code-execution/security-model.md` §"Verification status" and the required test matrix
in `docs/testing/README.md` §6.

## Revisit when
C++ is enabled for `DAS1110` (see `PENDING-INPUTS.md` §2 — needs a compile-stage image), or
submission volume exceeds a single worker (scale horizontally; the queue already supports it).
