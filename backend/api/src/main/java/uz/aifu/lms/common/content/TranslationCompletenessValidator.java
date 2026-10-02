package uz.aifu.lms.common.content;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * "Locale set on required translations is complete before publish" — shared across every
 * publish-gated, translated resource (subject, course, and eventually curriculum content).
 * Deliberately generic (plain locale-code sets, no module type): a genuine cross-cutting content
 * concern, the same category as {@link ContentSanitizer}.
 */
@Component
public class TranslationCompletenessValidator {

    public void requireLocales(
            Set<String> requiredLocales, Set<String> availableLocales, String resourceDescription) {
        Set<String> missing = new LinkedHashSet<>(requiredLocales);
        missing.removeAll(availableLocales);
        if (!missing.isEmpty()) {
            throw Exceptions.rule(
                            ErrorCode.TRANSLATION_INCOMPLETE,
                            resourceDescription
                                    + " is missing required translations for: "
                                    + missing)
                    .with("missingLocales", missing);
        }
    }
}
