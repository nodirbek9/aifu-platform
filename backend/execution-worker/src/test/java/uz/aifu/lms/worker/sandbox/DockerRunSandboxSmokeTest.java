package uz.aifu.lms.worker.sandbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import uz.aifu.lms.worker.config.SandboxProperties;

/**
 * Runs real code in real containers, one program per teaching language (Python, Java, C++): the
 * images, the compile commands and the sandbox flags of {@link DockerLanguageSpec} actually work
 * together. Needs Docker and the three images, so it runs only on demand:
 *
 * <pre>./mvnw -pl execution-worker test -Dtest=DockerRunSandboxSmokeTest -Dsandbox.smoke=true</pre>
 */
@EnabledIfSystemProperty(named = "sandbox.smoke", matches = "true")
class DockerRunSandboxSmokeTest {

    private final DockerRunSandboxEngine engine =
            new DockerRunSandboxEngine(
                    new SandboxProperties(
                            "docker-run", null, null, 5000, 20000, 262144, 64, 65536, false));

    @Test
    void python_readsStdin_andPrints() {
        SandboxRunResult result =
                run("python", "main.py", false, "a, b = map(int, input().split())\nprint(a + b)\n");
        assertSum(result);
    }

    @Test
    void java_compilesAndRuns() {
        SandboxRunResult result =
                run(
                        "java",
                        "Main.java",
                        true,
                        """
                        import java.util.Scanner;

                        public class Main {
                            public static void main(String[] args) {
                                Scanner in = new Scanner(System.in);
                                System.out.println(in.nextInt() + in.nextInt());
                            }
                        }
                        """);
        assertSum(result);
    }

    @Test
    void cpp_compilesAndRuns() {
        SandboxRunResult result =
                run(
                        "cpp",
                        "main.cpp",
                        true,
                        """
                        #include <iostream>
                        #include <vector>
                        #include <numeric>

                        int main() {
                            std::vector<long long> v(2);
                            for (auto &x : v) std::cin >> x;
                            std::cout << std::accumulate(v.begin(), v.end(), 0LL) << "\\n";
                            return 0;
                        }
                        """);
        assertSum(result);
    }

    @Test
    void cpp_compileError_isReportedAsSuch() {
        SandboxRunResult result = run("cpp", "main.cpp", true, "int main() { return x; }\n");
        assertThat(result.compileSuccess()).isFalse();
        assertThat(result.compileOutput()).contains("error");
    }

    @Test
    void cpp_infiniteLoop_isStopped() {
        SandboxRunRequest loop =
                new SandboxRunRequest(
                        "int main() { volatile unsigned long long i = 0; while (true) { i++; } }\n",
                        "cpp",
                        "judge0:54",
                        true,
                        "main.cpp",
                        "",
                        1000,
                        20000,
                        262144,
                        65536);
        SandboxRunResult result = engine.run(loop);
        assertThat(result.internalError()).isNull();
        assertThat(result.compileSuccess()).isTrue();
        assertThat(result.exitCode()).isNotEqualTo(0);
    }

    /**
     * The platform accepts sources up to 128 KB. One this large cannot be handed to {@code docker
     * run} as an argument or an environment variable (the OS caps each at 128 KB, base64 included),
     * so it travels on stdin ahead of the program's input. The comment is not ASCII on purpose: the
     * split between source and input is counted in bytes, not characters.
     */
    @Test
    void python_sourceAtTheSizeLimit_runs_andStillGetsItsInput() {
        String program = "a, b = map(int, input().split())\nprint(a + b)\n";
        String filler = "# oʻzgaruvchi — переменная — " + "x".repeat(60) + "\n";
        StringBuilder source = new StringBuilder(program);
        while (bytes(source) + bytes(filler) <= 128 * 1024) {
            source.append(filler);
        }
        assertThat(bytes(source)).isGreaterThan(127 * 1024);

        assertSum(run("python", "main.py", false, source.toString()));
    }

    /** Input is fed from its own thread, so a program that ignores it cannot stall the worker. */
    @Test
    void python_thatNeverReadsALargeInput_isStoppedByTheWallClock() {
        SandboxRunRequest deaf =
                new SandboxRunRequest(
                        "while True:\n    pass\n",
                        "python",
                        "unused",
                        false,
                        "main.py",
                        "7\n".repeat(512 * 1024),
                        1000,
                        3000,
                        262144,
                        65536);

        SandboxRunResult result =
                assertTimeoutPreemptively(Duration.ofSeconds(60), () -> engine.run(deaf));

        assertThat(result.internalError()).isNull();
        assertThat(result.timedOut() || (result.exitCode() != null && result.exitCode() != 0))
                .as("exit code %s, timed out %s", result.exitCode(), result.timedOut())
                .isTrue();
    }

    private static int bytes(CharSequence text) {
        return text.toString().getBytes(StandardCharsets.UTF_8).length;
    }

    private SandboxRunResult run(String language, String filename, boolean compile, String source) {
        return engine.run(
                new SandboxRunRequest(
                        source, language, "unused", compile, filename, "19 23\n", 5000, 20000,
                        262144, 65536));
    }

    private static void assertSum(SandboxRunResult result) {
        assertThat(result.internalError()).as("internal error").isNull();
        assertThat(result.compileSuccess())
                .as("compile output: %s", result.compileOutput())
                .isTrue();
        assertThat(result.exitCode()).as("stderr: %s", result.stderr()).isEqualTo(0);
        assertThat(result.stdout().trim()).isEqualTo("42");
    }
}
