package uz.aifu.lms.worker.sandbox;

/**
 * A fixed, deterministic non-sandbox for tests ({@code aifu.sandbox.engine=noop}) — never touches a
 * process or network. Always "succeeds" with empty output; it exists to exercise the
 * claim/grade/write-back pipeline without a real sandbox, never to grade real student code.
 */
public class NoopSandboxEngine implements SandboxEngine {

    @Override
    public boolean supports(String engineKey) {
        return "noop".equalsIgnoreCase(engineKey);
    }

    @Override
    public SandboxRunResult run(SandboxRunRequest request) {
        return new SandboxRunResult(true, "", 0, "", "", 1, 0, false, false, null);
    }
}
