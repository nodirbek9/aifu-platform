package uz.aifu.lms.enrollment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary;
import uz.aifu.lms.enrollment.app.EnrollmentService;

/**
 * The caller's own enrollment history, any status — a student's own record is never hidden behind
 * course-visibility rules the way a stranger's would be.
 */
@RestController
@RequestMapping("/api/v1/enrollments/me")
@Tag(name = "My enrollments", description = "The caller's own enrollment history")
@SecurityRequirement(name = "bearerAuth")
public class MyEnrollmentsController {

    private final EnrollmentService enrollments;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public MyEnrollmentsController(
            EnrollmentService enrollments,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.enrollments = enrollments;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_SELF_READ')")
    @Operation(summary = "List the caller's own enrollments, any status, any term")
    public PageResponse<EnrollmentSummary> myEnrollments(Pageable pageable) {
        var page =
                enrollments.listByStudent(
                        currentUser.require().id(), pageableSupport.sanitize(pageable));
        return PageResponse.of(page, EnrollmentMapper::toSummary);
    }
}
