package uz.aifu.lms.common.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.config.ApiProperties;

/**
 * Clamps client-supplied paging so no caller can request an unbounded result set.
 *
 * <p>Every controller that accepts a {@link Pageable} must pass it through {@link #sanitize} — an
 * unbounded query is a denial-of-service vector and a data-exfiltration convenience.
 */
@Component
public class PageableSupport {

    private final ApiProperties properties;

    public PageableSupport(ApiProperties properties) {
        this.properties = properties;
    }

    public Pageable sanitize(Pageable requested) {
        return sanitize(requested, Sort.unsorted());
    }

    public Pageable sanitize(Pageable requested, Sort fallbackSort) {
        if (requested == null || requested.isUnpaged()) {
            return PageRequest.of(0, properties.defaultPageSize(), fallbackSort);
        }
        int size = Math.min(Math.max(requested.getPageSize(), 1), properties.maxPageSize());
        int page = Math.max(requested.getPageNumber(), 0);
        Sort sort = requested.getSort().isSorted() ? requested.getSort() : fallbackSort;
        return PageRequest.of(page, size, sort);
    }
}
