package uz.aifu.lms.worker.sandbox;

/**
 * The sandbox port (ADR-006). Exactly one implementation is selected at startup by {@code
 * aifu.sandbox.engine} ({@code judge0} | {@code docker-run} | {@code noop|}) — see {@link
 * uz.aifu.lms.worker.config.SandboxEngineConfig}. Every implementation must enforce: no network,
 * read-only root filesystem, non-root user, all capabilities dropped, never privileged, source
 * passed as a file/stdin payload and never shell-interpolated (skills/coding-execution-security).
 */
public interface SandboxEngine {

    /** The {@code aifu.sandbox.engine} value this adapter answers to. */
    boolean supports(String engineKey);

    /** Runs one compile-then-execute cycle against one stdin, inside a fresh isolated sandbox. */
    SandboxRunResult run(SandboxRunRequest request);
}
