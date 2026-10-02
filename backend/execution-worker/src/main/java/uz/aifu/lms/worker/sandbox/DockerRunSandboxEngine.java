package uz.aifu.lms.worker.sandbox;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.aifu.lms.worker.config.SandboxProperties;

/**
 * Reference/fallback sandbox engine (ADR-006): invokes the host's {@code docker} CLI directly via
 * {@link ProcessBuilder}. This is the ONE place in the entire codebase a process is spawned to run
 * untrusted code — legitimate only here, in {@code execution-worker}, never in {@code backend/api}
 * (skills/coding-execution-security/SKILL.md, {@code NoProcessExecutionInApiTest}).
 *
 * <p>Every invocation: {@code --network=none}, {@code --read-only} root filesystem, a numeric
 * non-root {@code --user}, {@code --cap-drop=ALL}, {@code --security-opt=no-new-privileges}, {@code
 * --pids-limit}, {@code --memory}, {@code --cpus}, never {@code --privileged}, never a
 * docker-socket mount INTO the sandbox container (the worker's own socket access, needed to talk to
 * the daemon at all, is a separate, already-accepted trade-off of this "docker-outside-of- docker"
 * design — see {@code docker/execution-worker/Dockerfile}).
 *
 * <p><b>Source is never bind-mounted.</b> This process itself runs inside a container talking to
 * the HOST's docker daemon over a mounted socket ("docker-outside-of-docker") — a path written to
 * THIS container's filesystem (e.g. a Java temp file) is invisible to the daemon launching the
 * sandbox as a sibling container, since bind-mount sources are resolved by the daemon, not by this
 * process. Source is instead base64-encoded and passed as an environment variable, decoded by a
 * fixed, adapter-authored shell script inside the sandbox — never interpolated as a shell string,
 * and never a code path the student's own content can influence syntactically.
 */
public class DockerRunSandboxEngine implements SandboxEngine {

    private static final Logger log = LoggerFactory.getLogger(DockerRunSandboxEngine.class);
    private static final double CPU_QUOTA = 1.0;
    private static final String COMPILE_FAILURE_MARKER = "###COMPILE_FAILED###\n";

    private final SandboxProperties properties;

    public DockerRunSandboxEngine(SandboxProperties properties) {
        if (properties.enableNetwork()) {
            throw new IllegalStateException(
                    "aifu.sandbox.enable-network must be false — ADR-006 forbids network access from the sandbox");
        }
        this.properties = properties;
    }

    @Override
    public boolean supports(String engineKey) {
        return "docker-run".equalsIgnoreCase(engineKey);
    }

    @Override
    public SandboxRunResult run(SandboxRunRequest request) {
        DockerLanguageSpec spec;
        try {
            spec = DockerLanguageSpec.forLanguage(request.languageCode());
        } catch (IllegalArgumentException e) {
            return SandboxRunResult.internalFailure(e.getMessage());
        }

        String containerName = "aifu-sandbox-" + UUID.randomUUID();
        try {
            List<String> command = buildDockerCommand(containerName, spec, request);
            ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(false);
            Process process = pb.start();

            writeStdinThenClose(process, request.stdin());

            long start = System.nanoTime();
            boolean finished =
                    process.waitFor(request.wallTimeLimitMs() + 5_000L, TimeUnit.MILLISECONDS);
            boolean timedOut = !finished;
            if (!finished) {
                killContainer(containerName);
                process.waitFor(5, TimeUnit.SECONDS);
            }
            int timeMs = (int) ((System.nanoTime() - start) / 1_000_000);

            String stdout = readTruncated(process.getInputStream(), request.maxOutputBytes());
            String stderr = readTruncated(process.getErrorStream(), request.maxOutputBytes());
            int exitCode = finished ? process.exitValue() : -1;

            if (!timedOut && exitCode == 97 && stderr.startsWith(COMPILE_FAILURE_MARKER)) {
                return SandboxRunResult.compileFailure(
                        stderr.substring(COMPILE_FAILURE_MARKER.length()));
            }
            return new SandboxRunResult(
                    true,
                    null,
                    timedOut ? null : exitCode,
                    stdout,
                    stderr,
                    timeMs,
                    null,
                    timedOut,
                    false,
                    null);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("docker-run sandbox failed", e);
            return SandboxRunResult.internalFailure(
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private List<String> buildDockerCommand(
            String containerName, DockerLanguageSpec spec, SandboxRunRequest request) {
        long memoryBytes = request.memoryLimitKb() * 1024L;
        int cpuSeconds = Math.max(1, (request.cpuTimeLimitMs() + 999) / 1000);
        String sourceB64 =
                Base64.getEncoder()
                        .encodeToString(request.sourceCode().getBytes(StandardCharsets.UTF_8));

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-i");
        cmd.add("--name");
        cmd.add(containerName);
        cmd.add("--network=none");
        cmd.add("--read-only");
        cmd.add("--cap-drop=ALL");
        cmd.add("--security-opt=no-new-privileges");
        cmd.add("--user=1000:1000");
        cmd.add("--pids-limit=" + properties.maxProcesses());
        cmd.add("--memory=" + memoryBytes);
        cmd.add("--memory-swap=" + memoryBytes);
        cmd.add("--cpus=" + CPU_QUOTA);
        cmd.add("--tmpfs=/tmp:rw,noexec,nosuid,size=16m");
        if (spec.compileCommand() != null) {
            cmd.add("--tmpfs=/build:rw,exec,nosuid,size=128m");
            // A compiler's own scratch/cache dir (e.g. go build's $GOCACHE) and its temp working
            // dir ($WORK, controlled by $TMPDIR) both default under $HOME/os.TempDir() —
            // /tmp is a separate, much smaller tmpfs (16m) that quickly runs out of space for
            // multi-package builds, and the true root is --read-only anyway. All compiler scratch
            // space must live on the one writable, exec-permitted mount.
            cmd.add("-e");
            cmd.add("GOCACHE=/build/.gocache");
            cmd.add("-e");
            cmd.add("TMPDIR=/build");
            cmd.add("-e");
            cmd.add("HOME=/build");
            // Go's compiler parallelizes across GOMAXPROCS (host CPU count by default — Go 1.21
            // does not read the cgroup --cpus quota), so each concurrent package compile adds to
            // peak RSS independent of the CPU quota actually granted. Pinning it to 1 matches
            // --cpus=1.0 and was necessary in practice: unpinned, `go build` reliably exceeded a
            // 256MB memory ceiling even for a two-import program (real docker-run verification,
            // Phase 8).
            cmd.add("-e");
            cmd.add("GOMAXPROCS=1");
        }
        cmd.add("-e");
        cmd.add("AIFU_SRC=" + sourceB64);
        cmd.add(spec.image());
        cmd.add("sh");
        cmd.add("-c");
        cmd.add(shellScript(spec, request, cpuSeconds));
        return cmd;
    }

    private String shellScript(DockerLanguageSpec spec, SandboxRunRequest request, int cpuSeconds) {
        String sourcePath = "/tmp/work/" + request.sourceFilename();
        String runCmd = spec.runCommand().formatted(request.sourceFilename());
        StringBuilder script = new StringBuilder();
        script.append("mkdir -p /tmp/work && echo \"$AIFU_SRC\" | base64 -d > ")
                .append(sourcePath)
                .append(" && cd /tmp/work; ");
        // `ulimit -t <cpuSeconds>` bounds the SUBMITTED PROGRAM's own CPU time
        // (coding_tasks.cpu_time_limit_ms) — applied right before `exec`, never around the
        // compile step. A cold-cache `go build` legitimately needs more CPU time than a task's
        // per-run limit allows (real docker-run verification, Phase 8: a 2-second run limit
        // reliably SIGKILLed the compiler well before it produced any output).
        if (spec.compileCommand() != null) {
            String compileCmd = spec.compileCommand().formatted(request.sourceFilename());
            script.append(compileCmd)
                    .append(" 2>/tmp/compile.err; ec=$?; ")
                    .append("if [ $ec -ne 0 ]; then echo '")
                    .append(COMPILE_FAILURE_MARKER.strip())
                    .append("' 1>&2; cat /tmp/compile.err 1>&2; exit 97; fi; ");
        }
        script.append("ulimit -t ").append(cpuSeconds).append(" 2>/dev/null; ");
        script.append("exec ").append(runCmd);
        return script.toString();
    }

    private static void writeStdinThenClose(Process process, String stdin) {
        try (OutputStream out = process.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                out.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // The process may have already exited (e.g. syntax error before reading stdin) — not
            // a sandbox failure, just an unread pipe.
            log.debug("Could not write stdin to sandboxed process", e);
        }
    }

    private static void killContainer(String containerName) {
        try {
            new ProcessBuilder("docker", "kill", containerName)
                    .start()
                    .waitFor(5, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to force-kill sandbox container {}", containerName, e);
        }
    }

    private static String readTruncated(java.io.InputStream in, int maxBytes) throws IOException {
        byte[] all = in.readAllBytes();
        int len = Math.min(all.length, maxBytes);
        return new String(all, 0, len, StandardCharsets.UTF_8);
    }
}
