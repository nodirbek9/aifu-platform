package uz.aifu.lms.search.app;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.search.domain.SearchResult;
import uz.aifu.lms.search.persistence.SearchQueryRepository;

/**
 * Search over what the caller can open: their courses and the lessons in them; the subject
 * catalogue as well for an administrator (spec S-09). Who may see what is decided inside the query
 * — see {@link SearchQueryRepository}.
 */
@Service
public class SearchService {

    private static final int MAX_RESULTS = 100;
    private static final int MAX_WORDS = 8;

    private final SearchQueryRepository queries;

    public SearchService(SearchQueryRepository queries) {
        this.queries = queries;
    }

    public List<SearchResult> search(
            String queryText, String localeParam, Integer limit, UUID userId, boolean seesAll) {
        String query = toPrefixQuery(queryText);
        if (query.isEmpty()) {
            return List.of();
        }
        LocaleCode locale = LocaleCode.find(localeParam).orElse(LocaleCode.DEFAULT);
        int effectiveLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), MAX_RESULTS);
        return queries.search(locale.code(), query, userId, seesAll, effectiveLimit);
    }

    /**
     * The words typed, each as a prefix, all required: {@code "dastur til"} becomes {@code dastur:*
     * & til:*}. People search as they type, and a search that finds "dasturlash" only once the
     * whole word is written finds nothing for most of the typing. Only letters and digits survive,
     * so nothing typed can be read as query syntax.
     */
    static String toPrefixQuery(String text) {
        if (text == null) {
            return "";
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(word -> !word.isEmpty())
                .limit(MAX_WORDS)
                .map(word -> word + ":*")
                .collect(Collectors.joining(" & "));
    }
}
