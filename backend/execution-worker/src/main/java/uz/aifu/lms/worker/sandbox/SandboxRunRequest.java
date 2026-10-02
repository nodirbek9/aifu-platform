package uz.aifu.lms.worker.sandbox;

/**
 * One sandboxed run request (ADR-006). {@code sourceCode} must never be shell-interpolated by an
 * adapter — pass it as a file/stdin payload only (skills/coding-execution-security/SKILL.md).
 */
public record SandboxRunRequest(
        String sourceCode,
        String languageCode,
        String languageExecutorKey,
        boolean compileRequired,
        String sourceFilename,
        String stdin,
        int cpuTimeLimitMs,
        int wallTimeLimitMs,
        int memoryLimitKb,
        int maxOutputBytes) {}
