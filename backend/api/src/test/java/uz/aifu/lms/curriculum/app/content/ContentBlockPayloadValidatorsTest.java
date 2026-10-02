package uz.aifu.lms.curriculum.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.error.DomainException;

/**
 * Per-block-type payload validation (phase-plan.md Tests list), including hostile-HTML rejection —
 * {@link ContentSanitizer} is exercised for real, not mocked.
 */
class ContentBlockPayloadValidatorsTest {

    private final ContentSanitizer sanitizer = new ContentSanitizer();
    private final ObjectMapper mapper = new ObjectMapper();

    // ---------------------------------------------------------------- RichTextBlockValidator

    @Test
    void richText_validPayload_sanitizesAndKeepsSafeMarkup() throws Exception {
        RichTextBlockValidator validator = new RichTextBlockValidator(sanitizer);
        JsonNode payload = json("{\"html\":\"<p>Hello <strong>world</strong></p>\"}");

        JsonNode result = validator.validatePayload(payload);

        assertThat(result.get("html").asText()).contains("<strong>world</strong>");
        assertThat(validator.plainText(result)).contains("Hello").contains("world");
    }

    @Test
    void richText_hostileScript_isRejected() throws Exception {
        RichTextBlockValidator validator = new RichTextBlockValidator(sanitizer);
        JsonNode payload = json("{\"html\":\"<p>Hi</p><script>alert(1)</script>\"}");

        assertThatThrownBy(() -> validator.validatePayload(payload))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(uz.aifu.lms.common.error.ErrorCode.UNSAFE_CONTENT_REJECTED);
    }

    @Test
    void richText_missingHtml_isRejected() throws Exception {
        RichTextBlockValidator validator = new RichTextBlockValidator(sanitizer);

        assertThatThrownBy(() -> validator.validatePayload(json("{}")))
                .isInstanceOf(DomainException.class);
    }

    // ---------------------------------------------------------------- CalloutBlockValidator

    @Test
    void callout_requiresAKnownVariant() throws Exception {
        CalloutBlockValidator validator = new CalloutBlockValidator(sanitizer);

        JsonNode ok = validator.validateSettings(json("{\"variant\":\"warning\"}"));
        assertThat(ok.get("variant").asText()).isEqualTo("WARNING");

        assertThatThrownBy(() -> validator.validateSettings(json("{\"variant\":\"SPOOKY\"}")))
                .isInstanceOf(DomainException.class);
    }

    // ---------------------------------------------------------------- CodeExampleBlockValidator

    @Test
    void codeExample_requiresLanguageAndAcceptsRawCode() throws Exception {
        CodeExampleBlockValidator validator = new CodeExampleBlockValidator(sanitizer);

        JsonNode settings = validator.validateSettings(json("{\"language\":\"Python\"}"));
        assertThat(settings.get("language").asText()).isEqualTo("python");

        JsonNode payload =
                validator.validatePayload(json("{\"code\":\"print('<b>not html</b>')\"}"));
        assertThat(payload.get("code").asText()).isEqualTo("print('<b>not html</b>')");

        assertThatThrownBy(() -> validator.validateSettings(json("{}")))
                .isInstanceOf(DomainException.class);
    }

    // ---------------------------------------------------------------- FileBackedBlockValidator

    @Test
    void fileBacked_requiresFile() {
        FileBackedBlockValidator validator = new FileBackedBlockValidator(sanitizer);

        assertThat(validator.requiresFile()).isTrue();
        assertThat(validator.supports())
                .containsExactlyInAnyOrder(
                        uz.aifu.lms.curriculum.domain.ContentBlockType.IMAGE,
                        uz.aifu.lms.curriculum.domain.ContentBlockType.VIDEO,
                        uz.aifu.lms.curriculum.domain.ContentBlockType.AUDIO,
                        uz.aifu.lms.curriculum.domain.ContentBlockType.DIAGRAM,
                        uz.aifu.lms.curriculum.domain.ContentBlockType.FILE);
    }

    // ---------------------------------------------------------------- LinkBlockValidator

    @Test
    void link_rejectsJavascriptProtocol() throws Exception {
        LinkBlockValidator validator = new LinkBlockValidator(sanitizer);

        assertThatThrownBy(
                        () -> validator.validateSettings(json("{\"url\":\"javascript:alert(1)\"}")))
                .isInstanceOf(DomainException.class);

        JsonNode ok = validator.validateSettings(json("{\"url\":\"https://example.com/path\"}"));
        assertThat(ok.get("url").asText()).isEqualTo("https://example.com/path");
    }

    // ---------------------------------------------------------------- EmbedBlockValidator

    @Test
    void embed_restrictsExternalIdCharset() throws Exception {
        EmbedBlockValidator validator = new EmbedBlockValidator(sanitizer);

        JsonNode ok =
                validator.validateSettings(
                        json("{\"provider\":\"youtube\",\"externalId\":\"dQw4w9WgXcQ\"}"));
        assertThat(ok.get("provider").asText()).isEqualTo("YOUTUBE");

        assertThatThrownBy(
                        () ->
                                validator.validateSettings(
                                        json(
                                                "{\"provider\":\"youtube\",\"externalId\":\"<script>\"}")))
                .isInstanceOf(DomainException.class);
    }

    // ---------------------------------------------------------------- ReferenceBlockValidator

    @Test
    void reference_requiresAValidUuid() throws Exception {
        ReferenceBlockValidator validator = new ReferenceBlockValidator();

        JsonNode ok =
                validator.validateSettings(
                        json("{\"refId\":\"" + java.util.UUID.randomUUID() + "\"}"));
        assertThat(ok.get("refId").asText()).isNotBlank();

        assertThatThrownBy(() -> validator.validateSettings(json("{\"refId\":\"not-a-uuid\"}")))
                .isInstanceOf(DomainException.class);
    }

    private JsonNode json(String raw) throws Exception {
        return mapper.readTree(raw);
    }
}
