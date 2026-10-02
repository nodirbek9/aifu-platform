package uz.aifu.lms.grading.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.app.GradeEntryService;
import uz.aifu.lms.grading.domain.GradeEntry;

/**
 * A student's own grades. Ownership is "the caller's own enrollment" — no {@code AccessGuard},
 * resolved entirely by checking the loaded {@link Enrollment}'s student against the caller,
 * matching {@code coding.app.SubmissionService#require(UUID, UUID)}'s shape (Phase 8, ADR-012's
 * reasoning reused).
 */
@RestController
@Tag(name = "My grades", description = "The caller's own enrollment grade summary")
@SecurityRequirement(name = "bearerAuth")
public class GradeController {

    private final GradeEntryService grades;
    private final EnrollmentService enrollments;
    private final CurrentUserProvider currentUser;

    public GradeController(
            GradeEntryService grades,
            EnrollmentService enrollments,
            CurrentUserProvider currentUser) {
        this.grades = grades;
        this.enrollments = enrollments;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/enrollments/{id}/grades")
    @PreAuthorize("hasAuthority('GRADE_SELF_READ')")
    @Operation(summary = "The caller's own current published grade entries + derived final score")
    public EnrollmentGradeSummary myGrades(@PathVariable UUID id) {
        Enrollment enrollment = requireOwned(id);
        List<GradeEntry> entries = grades.listCurrentForEnrollment(id);
        return GradeMapper.toSummary(enrollment, entries);
    }

    private Enrollment requireOwned(UUID enrollmentId) {
        Enrollment enrollment = enrollments.require(enrollmentId);
        if (!enrollment.getStudent().getId().equals(currentUser.require().id())) {
            throw Exceptions.notFound("Enrollment", enrollmentId);
        }
        return enrollment;
    }
}
