package uz.aifu.lms.common.content;

import java.util.regex.Pattern;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * Teacher-authored content is UNTRUSTED (brief §40). Everything is sanitized on WRITE, so the
 * database only ever holds safe content and a later rendering bug cannot resurrect an XSS payload.
 *
 * <p>Policy summary — see {@code docs/security/content-security.md}:
 *
 * <ul>
 *   <li>Markdown is the authoring format. A small HTML allowlist is permitted inside it.
 *   <li>{@code <script>}, {@code <style>}, {@code <iframe>}, {@code <object>}, {@code <embed>},
 *       {@code <form>} and every {@code on*} event attribute are stripped.
 *   <li>Only {@code http}, {@code https} and {@code mailto} URLs survive; {@code javascript:} and
 *       {@code data:} are removed.
 *   <li>Links get {@code rel="nofollow noopener noreferrer"}.
 *   <li>Embeds are NOT free-form HTML: an {@code EMBED} block stores a provider + id and the
 *       frontend builds the iframe from an allowlist. That is why {@code <iframe>} can be banned
 *       here.
 * </ul>
 */
@Component
public class ContentSanitizer {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSanitizer.class);

    /** Hard rejection triggers: no legitimate lesson needs these. */
    private static final Pattern HOSTILE =
            Pattern.compile(
                    "(?i)(<\\s*script|javascript\\s*:|vbscript\\s*:|data\\s*:\\s*text/html|<\\s*iframe|"
                            + "on(error|load|click|mouseover|focus|submit)\\s*=)");

    private static final PolicyFactory POLICY =
            new HtmlPolicyBuilder()
                    .allowElements(
                            "p",
                            "br",
                            "strong",
                            "em",
                            "b",
                            "i",
                            "u",
                            "s",
                            "code",
                            "pre",
                            "blockquote",
                            "ul",
                            "ol",
                            "li",
                            "h1",
                            "h2",
                            "h3",
                            "h4",
                            "h5",
                            "h6",
                            "table",
                            "thead",
                            "tbody",
                            "tr",
                            "th",
                            "td",
                            "hr",
                            "span",
                            "sub",
                            "sup",
                            // a self-check question whose answer opens on demand (spec S-04)
                            "details",
                            "summary")
                    .allowUrlProtocols("http", "https", "mailto")
                    .allowElements("a")
                    .allowAttributes("href", "title")
                    .onElements("a")
                    .requireRelNofollowOnLinks()
                    .allowElements("img")
                    .allowAttributes("src", "alt", "title", "width", "height")
                    .onElements("img")
                    .allowAttributes("class")
                    .matching(Pattern.compile("^[a-zA-Z0-9 _-]{0,120}$"))
                    .onElements("span", "code", "pre", "td", "th")
                    .toFactory();

    /**
     * Sanitizes rich text. Silently strips unsafe markup; hard-rejects blatant attack payloads so
     * the author gets a real error instead of mysteriously losing content.
     */
    public String sanitizeRichText(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        if (HOSTILE.matcher(input).find()) {
            LOG.warn(
                    "Rejected content containing an active-content pattern (length={})",
                    input.length());
            throw Exceptions.rule(
                    ErrorCode.UNSAFE_CONTENT_REJECTED,
                    "Content contains scripts, event handlers or unsafe URLs, which are not allowed");
        }
        return POLICY.sanitize(input);
    }

    /** For fields that must be plain text (titles, captions, option labels). Strips all markup. */
    public String sanitizePlainText(String input) {
        if (input == null) {
            return null;
        }
        String stripped = input.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        return stripped;
    }

    /**
     * Code samples are NOT sanitized as markup — escaping would corrupt them. They are stored raw
     * and MUST be rendered as text (never {@code innerHTML}) by the frontend, which is the
     * documented contract for {@code CODE_EXAMPLE} blocks.
     */
    public String acceptCodeSample(String code, int maxBytes) {
        if (code == null) {
            return null;
        }
        if (code.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > maxBytes) {
            throw Exceptions.rule(ErrorCode.SOURCE_TOO_LARGE, "Code sample exceeds the size limit");
        }
        return code;
    }

    /** Flattens sanitized rich text for the full-text-search column. Never returned to clients. */
    public String toSearchText(String sanitizedHtmlOrMarkdown) {
        if (sanitizedHtmlOrMarkdown == null) {
            return null;
        }
        return sanitizedHtmlOrMarkdown
                .replaceAll("<[^>]*>", " ")
                .replaceAll("[#*_`>\\[\\]()]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
