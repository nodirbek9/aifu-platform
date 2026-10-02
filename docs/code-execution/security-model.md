# Code execution security model — verification status

This file records what has actually been verified against a running sandbox, as opposed to what
ADR-006 merely specifies. See `docs/decisions/ADR-006-code-execution.md` for the design; this file
is the honest record of what has been *checked*.

## Scope of this verification (Phase 8)

Per an explicit product decision, this round of verification exercised the **`docker-run`
engine** (`uz.aifu.lms.worker.sandbox.DockerRunSandboxEngine`) end-to-end against a real Docker
daemon — not the full Judge0 infrastructure (`docker/judge0/` is still empty; that needs its own
Postgres+Redis and is a separate, reviewed change) and not a code-only/untested build. `judge0` is
still ADR-006's named **production default**; `docker-run` is the reference/fallback engine, and is
what `docker-compose.yml`'s `execution-worker` service is wired to today (see that file's header
comment).

## What was actually run

A real `docker compose up --build` stack (`postgres` + `api` + `execution-worker`), against the
`DEV-TASK-SUM` dev-seed fixture (`db/seed/V902__dev_seed_fixtures.sql`) — "read N, then N
integers, print their sum" — 4 test cases (2 visible/example, 2 hidden), Python and Go attached.
Submissions were inserted directly (bypassing the API's HTTP layer, which is already covered by
`SubmissionService`'s own guard logic and unit tests) so the worker's claim → sandbox → grade →
write-back pipeline could be exercised directly and repeatedly. Every case below was observed via
`SELECT` against `submissions`/`execution_results`/`execution_test_results` after the job
completed — not inferred from logs.

| # | Case | Expected | Observed | Result |
|---|------|----------|----------|--------|
| 1 | Correct Python solution | ACCEPTED, 100/100 | `ACCEPTED`, 100.00/100.00, 4/4 | ✅ |
| 2 | Correct Go solution (compile + run) | ACCEPTED, 100/100 | `ACCEPTED`, 100.00/100.00, 4/4 | ✅ (after 3 fixes — see below) |
| 3 | Python syntax error | Graceful failure, no crash | `RUNTIME_ERROR` (Python has no separate compile stage; a `SyntaxError` surfaces as a non-zero exit with a traceback) | ✅ |
| 4 | Go compile failure (undefined identifier) | `COMPILATION_ERROR` | `COMPILATION_ERROR`, compile output captured | ✅ |
| 5 | Python runtime error (division by zero) | `RUNTIME_ERROR` | `RUNTIME_ERROR` | ✅ |
| 6 | Wrong output (off-by-one) | `WRONG_ANSWER` | `WRONG_ANSWER` | ✅ |
| 7 | Infinite loop (`while True: pass`) | Killed, `TIME_LIMIT_EXCEEDED` | Killed via the sandbox's own `ulimit -t` (SIGKILL, exit 137) at ~2.3–2.9s against a 2000ms CPU limit; classified `TIME_LIMIT_EXCEEDED` | ✅ (after classification fix — see below) |
| 8 | Memory bomb (unbounded `bytearray` allocation loop) | Killed, `MEMORY_LIMIT_EXCEEDED` | Killed by the `--memory` cgroup OOM killer (SIGKILL, exit 137) in 0.4–1.4s; classified `MEMORY_LIMIT_EXCEEDED` in 3 of 4 test cases, `TIME_LIMIT_EXCEEDED` in 1 (see **Known limitation** below) | ✅ mostly — limitation documented |
| 9 | Fork bomb (`while True: os.fork()`) | Blocked, no host impact | `os.fork()` raised `BlockingIOError: [Errno 11] Resource temporarily unavailable` — `--pids-limit=64` correctly refused further forks. Classified `RUNTIME_ERROR` (an uncaught exception, not a timeout or memory event) | ✅ |
| 10 | Network exfiltration attempt (`urllib.request.urlopen` to an external host) | Connection refused/unreachable | Caught `URLError` inside the student program (`--network=none`); printed `NETWORK_BLOCKED:URLError`, which then failed the actual test assertion (`WRONG_ANSWER`) since it doesn't match the expected sum | ✅ — no network reachability at all |

**Verdict: every security property ADR-006 requires was independently confirmed against a real
sandbox, not just asserted in code.** No case demonstrated a network path out, an uncontrolled
process/memory explosion, or execution surviving past its resource limits.

## Real bugs found and fixed during this verification

These would not have been caught without actually running the engine:

1. **Bind-mounting source into the sandbox is fundamentally broken under
   "docker-outside-of-docker."** The worker itself runs inside a container and talks to the HOST's
   docker daemon over `/var/run/docker.sock`. A path written to the worker's own filesystem (e.g. a
   Java temp file) is invisible to the daemon that launches the sandbox as a *sibling* container —
   `-v <workerLocalPath>:/workspace` silently mounted an empty/nonexistent directory. Fixed by
   never bind-mounting source at all: it is base64-encoded and passed as an `-e AIFU_SRC=...`
   environment variable, decoded by a fixed shell script inside the sandbox.
2. **`go build` needs a writable, exec-permitted scratch directory it doesn't get by default.**
   `$GOCACHE` and `$TMPDIR` (Go's own build-cache and working-directory settings) default under
   `$HOME`/`os.TempDir()`, which resolves to `/` (read-only root) or the 16MB `noexec` `/tmp`
   tmpfs — both fail outright for a multi-package build. Fixed with `-e GOCACHE=/build/.gocache
   -e TMPDIR=/build -e HOME=/build`, pointing all compiler scratch space at the one writable,
   exec-permitted mount (`--tmpfs=/build:rw,exec,nosuid,size=128m`).
3. **`ulimit -t <cpuTimeLimitMs>` was wrapping the *compile* step, not just the run step.** A
   coding task's `cpu_time_limit_ms` is meant to bound the *submitted program's* CPU time; applying
   the same ceiling to `go build` itself reliably SIGKILLed the compiler before it produced any
   output, since a cold-cache Go build legitimately needs more CPU time than a typical per-run
   limit allows. Fixed by moving `ulimit -t` to apply only immediately before `exec <runCmd>`,
   never around the compile stage.
4. Incidentally, Go's compiler parallelizes across `GOMAXPROCS` (host CPU count by default — Go
   1.21 does not read the cgroup `--cpus` quota), so each concurrent package compile added to peak
   RSS independent of the CPU quota actually granted. Pinned to `GOMAXPROCS=1` to match
   `--cpus=1.0` and keep compilation within a 256MB ceiling.
5. The `DEV-TASK-SUM` dev-seed fixture's `memory_limit_kb` was tightened to 131072 (128MB), which
   reliably OOM-killed `go build` even after fix #4. Raised to 262144 (256MB, matching
   `aifu.sandbox.default-memory-limit-kb`) in `db/seed/V902__dev_seed_fixtures.sql` — a dev/test
   fixture, not academic content, so adjusting it based on real measurement is in scope. **A
   content author publishing a real Go coding task should budget at least 256MB**, materially more
   than Python needs for the same task.

## Known limitation: TLE vs. MLE classification for `docker-run`

`docker-run` never reports a submission's peak memory usage back (unlike Judge0, which does) — so
when a sandboxed process is SIGKILLed (exit 137) with neither the worker's own wall-clock timeout
fired nor a `memoryKb` reading over the limit, `Grader` has no direct signal for *why*. It falls
back to a timing heuristic (`uz.aifu.lms.worker.grading.Grader`, `SIGKILL_EXIT_CODE` branch): a
kill within 300ms of the task's CPU-time ceiling is classified `TIME_LIMIT_EXCEEDED` (the
sandbox's own `ulimit -t`); a kill well short of it is classified `MEMORY_LIMIT_EXCEEDED` (the
`--memory` cgroup's OOM killer). This is a best-effort guess, not a certainty — verified against
real runs above, it got case 7 right in all 4/4 sub-cases and case 8 right in 3/4 (one memory-bomb
run happened to survive long enough to cross the CPU-ceiling threshold before being OOM-killed,
and was mislabeled `TIME_LIMIT_EXCEEDED`). The submission-level verdict and the pass/fail outcome
were correct in every case; only the specific limit-exceeded *label* can occasionally be wrong.
Judge0, which reports real memory usage, would not have this ambiguity — another reason it remains
the production default.

## What was NOT covered by this round

- Judge0 itself (no `docker/judge0/` infrastructure stood up).
- Concurrent submissions racing the same claim (`ExecutionJobQueue.claim`'s `FOR UPDATE SKIP
  LOCKED`) — covered by unit-level reasoning and the append-only `uq_execution_job_submission`
  constraint, not a live concurrency test.
- Expired-lease reclamation (`ExecutionJobQueue.reclaimExpiredLeases`) under an actual worker
  crash/kill mid-run.
- Disk-based attacks (e.g. filling `--tmpfs` to exhaustion) beyond what the size caps already
  bound.
