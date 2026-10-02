package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import uz.aifu.lms.support.AbstractIntegrationTest;

/**
 * The API contract the frontend is generated from: {@code docs/api/openapi.json} must be exactly
 * what the running application serves. The frontend builds its TypeScript types from that file
 * (ARCHITECTURE D5), so an endpoint or field that changes without the file changing would break the
 * UI at run time instead of at compile time.
 *
 * <p>After changing the API on purpose, regenerate the file and commit it:
 *
 * <pre>./mvnw -pl api verify -Pit -Dtest=OpenApiContractIT -Dopenapi.write=true</pre>
 */
class OpenApiContractIT extends AbstractIntegrationTest {

    /** Maven runs the test from {@code backend/api}. */
    private static final Path CONTRACT = Path.of("..", "..", "docs", "api", "openapi.json");

    private static final ObjectMapper JSON =
            new ObjectMapper()
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                    .configure(SerializationFeature.INDENT_OUTPUT, true);

    @LocalServerPort private int port;

    @Test
    void committedContract_matchesTheRunningApi() throws IOException {
        String served = normalize(rest.getForObject(url("/v3/api-docs"), String.class));

        if (Boolean.getBoolean("openapi.write")) {
            Files.createDirectories(CONTRACT.getParent());
            Files.writeString(CONTRACT, served, StandardCharsets.UTF_8);
            return;
        }
        assertThat(Files.exists(CONTRACT))
                .as("docs/api/openapi.json is missing; generate it with -Dopenapi.write=true")
                .isTrue();
        String committed = Files.readString(CONTRACT, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertThat(committed)
                .as(
                        "The API changed but docs/api/openapi.json did not. Regenerate it with"
                                + " -Dopenapi.write=true and commit the result.")
                .isEqualTo(served);
    }

    /** Sorted keys and no server address: the same API always produces the same bytes. */
    private static String normalize(String apiDocs) throws IOException {
        JsonNode root = JSON.readTree(apiDocs);
        ((ObjectNode) root).remove("servers");
        Object sorted = JSON.treeToValue(root, Object.class);
        return JSON.writeValueAsString(sorted).replace("\r\n", "\n") + "\n";
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
