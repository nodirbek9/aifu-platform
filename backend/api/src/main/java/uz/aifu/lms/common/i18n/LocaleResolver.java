package uz.aifu.lms.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolution order (ADR-005): explicit {@code ?locale=} → {@code Accept-Language} → the caller's
 * {@code preferred_locale} → the course's {@code instruction_language} → {@code uz}.
 *
 * <p>{@link #resolveFrom} picks the best AVAILABLE locale for a specific piece of content and
 * reports whether it had to fall back, so the response can say so honestly.
 *
 * <p>Named explicitly to avoid the default bean name {@code localeResolver}: Spring MVC's {@code
 * DispatcherServlet} looks up that exact name expecting its own {@code
 * org.springframework.web.servlet.LocaleResolver} strategy interface, and finding this unrelated
 * type there instead breaks every request with a 500 at servlet init.
 */
@Component("aifuLocaleResolver")
public class LocaleResolver {

    /** Preference order for the request, most specific first. Nulls are ignored. */
    public LocaleCode preferred(
            String explicit,
            HttpServletRequest request,
            LocaleCode userPreference,
            LocaleCode courseLanguage) {
        return LocaleCode.find(explicit)
                .or(() -> fromAcceptLanguage(request))
                .or(() -> Optional.ofNullable(userPreference))
                .or(() -> Optional.ofNullable(courseLanguage))
                .orElse(LocaleCode.DEFAULT);
    }

    /**
     * Chooses among the locales a piece of content actually has.
     *
     * @param requested what the caller wants
     * @param available locales present for this content
     * @param contentDefault the content's own default (usually the course instruction language)
     */
    public LocaleResolution resolveFrom(
            LocaleCode requested, Collection<String> available, LocaleCode contentDefault) {
        List<String> sorted = available.stream().sorted().toList();
        if (available.contains(requested.code())) {
            return LocaleResolution.exact(requested, sorted);
        }
        LocaleCode resolved =
                Optional.ofNullable(contentDefault)
                        .filter(candidate -> available.contains(candidate.code()))
                        .or(
                                () ->
                                        available.contains(LocaleCode.DEFAULT.code())
                                                ? Optional.of(LocaleCode.DEFAULT)
                                                : sorted.stream()
                                                        .findFirst()
                                                        .flatMap(LocaleCode::find))
                        .orElse(LocaleCode.DEFAULT);
        return LocaleResolution.fallback(requested, resolved, sorted);
    }

    private Optional<LocaleCode> fromAcceptLanguage(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        String header = request.getHeader("Accept-Language");
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        // "ru-RU,ru;q=0.9,en;q=0.8" -> first supported tag wins.
        for (String part : header.split(",")) {
            String tag = part.split(";")[0].trim();
            Optional<LocaleCode> match = LocaleCode.find(tag);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }
}
