package uz.aifu.lms.analytics.app;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.analytics.api.dto.AdminProblemDtos.AdminProblemView;
import uz.aifu.lms.analytics.domain.AdminProblemView.AdminProblemType;
import uz.aifu.lms.analytics.persistence.AdminProblemViewRepository;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.error.Exceptions;

/** Read-only, SQL-paginated access to the analytics-owned administrative problems view. */
@Service
public class AdminProblemsService {

    private static final Sort DEFAULT_SORT = Sort.by("type", "problemKey");
    private static final Set<String> SORT_FIELDS = Set.of("type", "problemKey");

    private final AdminProblemViewRepository problems;
    private final PageableSupport pageableSupport;

    public AdminProblemsService(
            AdminProblemViewRepository problems, PageableSupport pageableSupport) {
        this.problems = problems;
        this.pageableSupport = pageableSupport;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProblemView> list(AdminProblemType type, Pageable requested) {
        Pageable sanitized = pageableSupport.sanitize(requested, DEFAULT_SORT);
        Sort sort = sanitized.getSort();
        for (Sort.Order order : sort) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw Exceptions.validation("Supported sort fields are type and problemKey");
            }
        }
        for (Sort.Order order : DEFAULT_SORT) {
            if (sort.getOrderFor(order.getProperty()) == null) {
                sort = sort.and(Sort.by(order));
            }
        }
        Pageable pageable =
                PageRequest.of(
                        sanitized.getPageNumber(), Math.min(sanitized.getPageSize(), 100), sort);
        var page = type == null ? problems.findAll(pageable) : problems.findByType(type, pageable);
        return PageResponse.of(page, AdminProblemView::from);
    }
}
