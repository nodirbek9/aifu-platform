package uz.aifu.lms.search.app;

import java.util.List;
import org.springframework.stereotype.Service;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.search.domain.SearchResult;
import uz.aifu.lms.search.persistence.SearchQueryRepository;

/**
 * Catalogue search — subjects + courses only (brief §26). {@code search -> curriculum} is an
 * allowed module edge (module map) but deliberately unused so far: lesson/content-block search
 * needs enrollment-scoped visibility, which would need a new {@code search -> enrollment} edge —
 * out of scope for this pass, see {@code docs/BUILD-STATUS.md}'s "What remains".
 */
@Service
public class SearchService {

    private static final int MAX_RESULTS = 100;

    private final SearchQueryRepository queries;

    public SearchService(SearchQueryRepository queries) {
        this.queries = queries;
    }

    public List<SearchResult> search(String queryText, String localeParam, Integer limit) {
        if (queryText == null || queryText.isBlank()) {
            return List.of();
        }
        LocaleCode locale = LocaleCode.find(localeParam).orElse(LocaleCode.DEFAULT);
        int effectiveLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), MAX_RESULTS);
        return queries.search(locale.code(), queryText.trim(), effectiveLimit, 0);
    }
}
