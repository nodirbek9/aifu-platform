package uz.aifu.lms.worker.sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.aifu.lms.worker.config.SandboxProperties;

/**
 * The default sandbox engine (ADR-006): a network-isolated Judge0 instance the worker calls over
 * the internal Docker network. {@code languageExecutorKey} is the {@code judge0:<id>} value seeded
 * in {@code programming_languages.executor_key} (V010) — everything after the colon is Judge0's
 * numeric language id. All payloads are base64 — never string-concatenated into a shell command;
 * Judge0 itself owns the actual sandboxing (its own isolated, network-disabled containers).
 */
public class Judge0SandboxEngine implements SandboxEngine {

    private static final Logger log = LoggerFactory.getLogger(Judge0SandboxEngine.class);

    private final SandboxProperties properties;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public Judge0SandboxEngine(SandboxProperties properties) {
        this.properties = properties;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public boolean supports(String engineKey) {
        return "judge0".equalsIgnoreCase(engineKey);
    }

    @Override
    public SandboxRunResult run(SandboxRunRequest request) {
        try {
            int languageId = parseLanguageId(request.languageExecutorKey());
            Map<String, Object> body =
                    Map.of(
                            "source_code",
                            encode(request.sourceCode()),
                            "language_id",
                            languageId,
                            "stdin",
                            encode(request.stdin()),
                            "cpu_time_limit",
                            request.cpuTimeLimitMs() / 1000.0,
                            "wall_time_limit",
                            request.wallTimeLimitMs() / 1000.0,
                            "memory_limit",
                            request.memoryLimitKb(),
                            "enable_network",
                            false);
            String json = mapper.writeValueAsString(body);

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            properties.judge0Url()
                                                    + "/submissions?base64_encoded=true&wait=true"))
                            .timeout(Duration.ofMillis(request.wallTimeLimitMs() + 15_000L))
                            .header("Content-Type", "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            json, StandardCharsets.UTF_8));
            if (properties.judge0Token() != null && !properties.judge0Token().isBlank()) {
                builder.header("X-Auth-Token", properties.judge0Token());
            }

            HttpResponse<String> response =
                    http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                return SandboxRunResult.internalFailure(
                        "Judge0 returned HTTP " + response.statusCode());
            }
            return parseResult(response.body(), request.maxOutputBytes());
        } catch (Exception e) {
            log.warn("Judge0 run failed", e);
            return SandboxRunResult.internalFailure(
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private SandboxRunResult parseResult(String body, int maxOutputBytes) throws Exception {
        JsonNode node = mapper.readTree(body);
        String stdout = decode(node.path("stdout").asText(null));
        String stderr = decode(node.path("stderr").asText(null));
        String compileOutput = decode(node.path("compile_output").asText(null));
        int statusId = node.path("status").path("id").asInt(0);
        // Judge0 status ids: 3=Accepted, 6=Compilation Error, 5=Time Limit Exceeded, ... see
        // https://ce.judge0.com/statuses — the worker maps this to a TestVerdict at grading time,
        // not here; this adapter only reports raw facts.
        boolean compileSuccess = statusId != 6;
        boolean timedOut = statusId == 5;
        Integer exitCode = node.path("exit_code").isNull() ? null : node.path("exit_code").asInt();
        Double timeSeconds = node.path("time").isNull() ? null : node.path("time").asDouble();
        Integer memoryKb = node.path("memory").isNull() ? null : node.path("memory").asInt();

        boolean truncated = false;
        if (stdout != null && stdout.getBytes(StandardCharsets.UTF_8).length > maxOutputBytes) {
            stdout = truncateToBytes(stdout, maxOutputBytes);
            truncated = true;
        }
        if (stderr != null && stderr.getBytes(StandardCharsets.UTF_8).length > maxOutputBytes) {
            stderr = truncateToBytes(stderr, maxOutputBytes);
            truncated = true;
        }

        return new SandboxRunResult(
                compileSuccess,
                compileOutput,
                exitCode,
                stdout == null ? "" : stdout,
                stderr == null ? "" : stderr,
                timeSeconds == null ? 0 : (int) Math.round(timeSeconds * 1000),
                memoryKb,
                timedOut,
                truncated,
                null);
    }

    private static int parseLanguageId(String executorKey) {
        if (executorKey == null || !executorKey.startsWith("judge0:")) {
            throw new IllegalArgumentException("Not a judge0 executor key: " + executorKey);
        }
        return Integer.parseInt(executorKey.substring("judge0:".length()));
    }

    private static String encode(String value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return base64;
        }
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    private static String truncateToBytes(String value, int maxBytes) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, 0, Math.min(maxBytes, bytes.length), StandardCharsets.UTF_8);
    }
}
