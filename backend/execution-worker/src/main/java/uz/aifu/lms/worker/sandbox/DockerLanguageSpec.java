package uz.aifu.lms.worker.sandbox;

import java.util.Map;

/**
 * Fixed, hardcoded docker-image + command templates per language code — deliberately NOT driven by
 * {@code programming_languages.executor_key} (that column holds Judge0 language ids, e.g. {@code
 * judge0:71}, meaningless to this adapter). {@link DockerRunSandboxEngine} is the
 * reference/fallback engine (ADR-006), not the production default, so a small hardcoded map is
 * acceptable; Judge0 remains the source of truth for supported languages.
 */
record DockerLanguageSpec(String image, String compileCommand, String runCommand) {

    private static final Map<String, DockerLanguageSpec> SPECS =
            Map.of(
                    "python",
                            new DockerLanguageSpec(
                                    "python:3.11-slim", null, "python3 /tmp/work/%s"),
                    "go",
                            new DockerLanguageSpec(
                                    "golang:1.21-alpine",
                                    "go build -o /build/main /tmp/work/%s",
                                    "/build/main"),
                    // JVM flags keep both javac and the program inside the sandbox limits: one
                    // active CPU and the serial collector keep the thread count well under
                    // --pids-limit; -XX:-UsePerfData avoids writing hsperfdata into the small
                    // noexec /tmp; the heap caps leave headroom under the 256MB memory limit.
                    // Classes go to /build, the only writable, exec-permitted mount.
                    "java",
                            new DockerLanguageSpec(
                                    "eclipse-temurin:21-jdk",
                                    "javac -J-XX:+UseSerialGC -J-XX:ActiveProcessorCount=1"
                                            + " -J-XX:TieredStopAtLevel=1 -J-XX:-UsePerfData"
                                            + " -J-Xmx160m -encoding UTF-8 -d /build /tmp/work/%s",
                                    "java -XX:+UseSerialGC -XX:ActiveProcessorCount=1"
                                            + " -XX:TieredStopAtLevel=1 -XX:-UsePerfData -Xmx128m"
                                            + " -Xss16m -Dfile.encoding=UTF-8 -cp /build Main"),
                    // The binary goes to /build like Go's: /tmp is noexec. -pipe keeps g++ from
                    // writing temporaries into the small /tmp.
                    "cpp",
                            new DockerLanguageSpec(
                                    "gcc:13",
                                    "g++ -std=c++17 -O2 -pipe -o /build/main /tmp/work/%s",
                                    "/build/main"));

    static DockerLanguageSpec forLanguage(String code) {
        DockerLanguageSpec spec = SPECS.get(code);
        if (spec == null) {
            throw new IllegalArgumentException("No docker-run spec for language: " + code);
        }
        return spec;
    }
}
