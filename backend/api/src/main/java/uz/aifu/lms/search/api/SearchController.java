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
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.search.api.dto.SearchDtos.SearchResultView;
import uz.aifu.lms.search.app.SearchService;

/**
 * Search over what the caller can open (spec S-09). Gated on {@code SUBJECT_READ} — the closest
 * existing permission to "may browse," already granted to every role (V003); no dedicated {@code
 * SEARCH_*} permission exists and none is invented here (CLAUDE.md §14.5). What a caller may see is
 * enforced inside the query itself — see {@code search.persistence.SearchQueryRepository}.
 */
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Search over my courses and their lessons")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final SearchService search;
    private final CurrentUserProvider currentUser;

    public SearchController(SearchService search, CurrentUserProvider currentUser) {
        this.search = search;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBJECT_READ')")
    @Operation(
            summary = "Search my courses and their lessons",
            description =
                    "Each word typed is matched as a prefix, in every language the text exists in;"
                            + " the reader's language ranks first. A student finds the courses"
                            + " they are enrolled in and the published lessons of those courses; a"
                            + " teacher those they teach; an administrator everything, and the"
                            + " subject catalogue as well.")
    public List<SearchResultView> search(
            @RequestParam("q") String queryText,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) Integer limit) {
        AuthenticatedUser caller = currentUser.require();
        return search.search(queryText, locale, limit, caller.id(), caller.isAdmin()).stream()
                .map(
                        r ->
                                new SearchResultView(
                                        r.resultType(),
                                        r.entityId(),
                                        r.courseId(),
                                        r.title(),
                                        r.snippet(),
                                        r.rank()))
                .toList();
    }
}
