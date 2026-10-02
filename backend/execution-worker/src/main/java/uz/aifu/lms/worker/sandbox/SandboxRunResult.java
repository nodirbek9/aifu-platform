package uz.aifu.lms.worker.sandbox;

/** Outcome of one sandboxed run — compile stage (if any) plus the single run against one stdin. */
public record SandboxRunResult(
        boolean compileSuccess,
        String compileOutput,
        Integer exitCode,
        String stdout,
        String stderr,
        int timeMs,
        Integer memoryKb,
        boolean timedOut,
        boolean outputTruncated,
        String internalError) {

    public static SandboxRunResult compileFailure(String compileOutput) {
        return new SandboxRunResult(
                false, compileOutput, null, "", "", 0, null, false, false, null);
    }

    public static SandboxRunResult internalFailure(String message) {
        return new SandboxRunResult(true, null, null, "", "", 0, null, false, false, message);
    }
}
