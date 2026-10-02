package uz.aifu.lms.search.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.search.api.dto.SearchDtos.SearchResultView;
import uz.aifu.lms.search.app.SearchService;

/**
 * Catalogue search (subjects + courses, brief §26). Gated on {@code SUBJECT_READ} — the closest
 * existing permission to "may browse the catalogue," already granted to every role (V003); no
 * dedicated {@code SEARCH_*} permission exists and none is invented here (CLAUDE.md §14.5).
 * Authorization is enforced inside the query itself (only {@code PUBLISHED} rows), not by this
 * permission check — see {@code search.persistence.SearchQueryRepository}'s class doc.
 */
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Full-text catalogue search (subjects + courses)")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final SearchService search;

    public SearchController(SearchService search) {
        this.search = search;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBJECT_READ')")
    @Operation(
            summary = "Search subjects and courses",
            description = "Ranked by relevance; only PUBLISHED subjects/courses can ever appear.")
    public List<SearchResultView> search(
            @RequestParam("q") String queryText,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) Integer limit) {
        return search.search(queryText, locale, limit).stream()
                .map(
                        r ->
                                new SearchResultView(
                                        r.resultType(),
                                        r.entityId(),
                                        r.title(),
                                        r.snippet(),
                                        r.rank()))
                .toList();
    }
}
