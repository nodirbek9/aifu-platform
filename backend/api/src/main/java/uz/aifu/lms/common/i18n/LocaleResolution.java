package uz.aifu.lms.common.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Locale metadata returned with every localized response so a frontend can badge untranslated
 * content instead of silently presenting a fallback as if it were the requested language.
 */
@Schema(name = "LocaleResolution")
public record LocaleResolution(
        @Schema(description = "Locale actually used for the returned text", example = "uz")
                String resolvedLocale,
        @Schema(description = "Locale the caller asked for", example = "ru") String requestedLocale,
        @Schema(description = "Locales in which this content exists", example = "[\"uz\"]")
                List<String> availableLocales,
        @Schema(
                        description =
                                "True when the requested locale was unavailable and a fallback was used")
                boolean fallback) {

    public static LocaleResolution exact(LocaleCode locale, List<String> available) {
        return new LocaleResolution(locale.code(), locale.code(), available, false);
    }

    public static LocaleResolution fallback(
            LocaleCode requested, LocaleCode resolved, List<String> available) {
        return new LocaleResolution(resolved.code(), requested.code(), available, true);
    }
}
