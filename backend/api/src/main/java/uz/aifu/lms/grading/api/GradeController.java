package uz.aifu.lms.grading.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.app.GradeEntryService;

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
    @Operation(
            summary =
                    "My grades in one course: the assessment scheme with earned points, the entries"
                            + " that count, the history, and the derived final score")
    public EnrollmentGradeSummary myGrades(
            @PathVariable UUID id,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        requireOwned(id);
        return GradeMapper.toSummary(
                grades.gradebookFor(id, resolveLocale(locale, acceptLanguage)));
    }

    private static String resolveLocale(String explicit, String acceptLanguage) {
        String header =
                acceptLanguage == null || acceptLanguage.isBlank()
                        ? null
                        : acceptLanguage.split("[,;-]", 2)[0].trim();
        return LocaleCode.find(explicit)
                .or(() -> LocaleCode.find(header))
                .orElse(LocaleCode.DEFAULT)
                .code();
    }

    private Enrollment requireOwned(UUID enrollmentId) {
        Enrollment enrollment = enrollments.require(enrollmentId);
        if (!enrollment.getStudent().getId().equals(currentUser.require().id())) {
            throw Exceptions.notFound("Enrollment", enrollmentId);
        }
        return enrollment;
    }
}
