package uz.aifu.lms.common.i18n;

import java.util.Arrays;
import java.util.Optional;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * Supported content locales (ADR-005). Mirrors the {@code locales} reference table.
 *
 * <p>Adding a language is a DB insert plus a value here; it is never a schema change. Note that
 * PostgreSQL has no Uzbek text-search dictionary, so {@code uz} search is unstemmed (ADR-010).
 */
public enum LocaleCode {
    UZ("uz"),
    RU("ru"),
    EN("en");

    public static final LocaleCode DEFAULT = UZ;

    private final String code;

    LocaleCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Optional<LocaleCode> find(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toLowerCase();
        // Accept "uz-UZ", "ru_RU", "en-GB" and similar.
        String language = normalized.split("[-_]")[0];
        return Arrays.stream(values()).filter(value -> value.code.equals(language)).findFirst();
    }

    /** Lenient parse used for user preferences: an unknown value falls back to {@link #DEFAULT}. */
    public static LocaleCode parseOrDefault(String raw) {
        return find(raw).orElse(DEFAULT);
    }

    public static LocaleCode require(String raw) {
        return find(raw)
                .orElseThrow(
                        () ->
                                Exceptions.rule(
                                                ErrorCode.UNSUPPORTED_LOCALE,
                                                "Unsupported locale: " + raw)
                                        .with(
                                                "supported",
                                                Arrays.stream(values())
                                                        .map(LocaleCode::code)
                                                        .toList()));
    }
}
