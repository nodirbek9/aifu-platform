---
name: coding-execution-security
description: SECURITY CRITICAL. Load before touching submissions, the execution worker, the sandbox, or Judge0/Docker limits.
---
# Untrusted code execution — security rules

Student code is hostile. ADR-006 + `docs/code-execution/security-model.md`. **Changes here
require security review. If unsure, stop and ask.**

## Absolute prohibitions (build fails on these)
1. No `Runtime.exec`, `ProcessBuilder`, `ScriptEngine`, `Compiler` API, GraalVM polyglot, or
   reflection-based eval **anywhere in `backend/api`**. `NoProcessExecutionInApiTest` enforces it.
2. The API must never open a connection to the sandbox or to Judge0.
3. Never run the sandbox `--privileged`, never mount the Docker socket into it, never give it
   network access, never run it as root.
4. Never pass a platform secret, DB URL, or token into the sandbox environment.
5. Never echo a hidden test case's `expected_output`, stdout or stderr to a student. Never
   serialize `coding_task_languages.solution_code` into a student DTO.
6. Never `eval` or string-interpolate student code into a shell command. Source goes to the
   sandbox as a **file/base64 payload**, never as part of a command line.

## The only legal flow
```
POST /submissions  →  guards + limits + validation
                   →  ONE transaction: insert submission(QUEUED) + insert execution_job(PENDING)
                   →  202 {submissionId}
worker             →  claim (FOR UPDATE SKIP LOCKED, lease) → SandboxEngine.run per test case
                   →  truncate + sanitize output → verdict + score
                   →  execution_result + submission update + grade_entry + progress
```

## Limits — defaults, hard-capped by config, never removable
CPU 5 s · wall 10 s · memory 256 MB · pids 64 · stack 64 MB · output 64 KB (truncate + flag) ·
source 128 KB · `--network=none` · read-only rootfs · `tmpfs /tmp` 16 MB `noexec,nosuid` ·
non-root uid · `--cap-drop=ALL` · `--security-opt=no-new-privileges`.
A task may **tighten** a limit; it may never exceed the configured ceiling
(`ExecutionProperties` clamps it — keep the clamp).

## Threats and the control that answers each
infinite loop → CPU + wall timeout · fork bomb → pids limit · memory bomb → memory cgroup ·
disk fill → read-only rootfs + sized tmpfs · data exfiltration → no network · host escape →
unprivileged container, dropped caps, no socket · secret theft → no secrets in env +
restricted DB role · output flood → truncation · queue DoS → per-student rate limit + max
concurrent + bounded worker concurrency · poison job → `attempt_count`/`max_attempts` → `DEAD`.

## Worker DB role
`aifu_execution_worker` (migration `V014`): SELECT on submissions/tasks/test cases/languages;
INSERT/UPDATE on `execution_*` and a narrow update of submission status/verdict/score.
**No access to `users`, `refresh_tokens`, `audit_log`, `grade_entries`.** Do not widen these
grants for convenience — if the worker "needs" more, the design is wrong.

## Adding a language
1. Row in `programming_languages` (`code`, `executor_key`, `compile_required`, `is_enabled`).
2. Confirm the sandbox image exists and is pinned by digest.
3. Set compile + run limits (compiled languages need a separate compile budget).
4. Add the full §64 test matrix for it: valid, syntax error, compile failure, runtime error,
   timeout, infinite loop, memory bomb, wrong output, correct output.
5. Enable only after those tests pass. **Do not enable a language you have not tested.**

## Verification status (be honest)
The limits and topology above are written from the threat model. In the authoring environment
they were **not executed** (no Docker, no network). Phase 12 must run the §64 matrix and an
isolation test before this is called verified. Do not report it as verified until then.
