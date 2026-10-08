package uz.aifu.lms.assessment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.api.dto.AttemptGrantDtos.AttemptGrantRequest;
import uz.aifu.lms.assessment.api.dto.AttemptGrantDtos.AttemptGrantView;
import uz.aifu.lms.assessment.app.AttemptGrantService;
import uz.aifu.lms.assessment.domain.QuizAttemptGrant;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;

@RestController
@Tag(name = "Admin: attempt grants", description = "Grant additional quiz attempts")
@SecurityRequirement(name = "bearerAuth")
public class AdminAttemptGrantController {

    private final AttemptGrantService attemptGrantService;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public AdminAttemptGrantController(
            AttemptGrantService attemptGrantService,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.attemptGrantService = attemptGrantService;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @PostMapping("/api/v1/admin/quizzes/{quizId}/enrollments/{enrollmentId}/attempt-grants")
    @PreAuthorize("hasAuthority(T(uz.aifu.lms.iam.app.access.Perm).QUIZ_ATTEMPT_GRANT)")
    @Operation(summary = "Grant an additional attempt for a quiz")
    public ResponseEntity<AttemptGrantView> grantAttempt(
            @PathVariable UUID quizId,
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody AttemptGrantRequest request) {
        UUID adminUserId = currentUser.require().id();
        QuizAttemptGrant grant =
                attemptGrantService.grant(quizId, enrollmentId, request.reason(), adminUserId);

        AttemptGrantView view = toView(grant);
        return ResponseEntity.created(
                        URI.create(
                                "/api/v1/admin/quizzes/"
                                        + quizId
                                        + "/enrollments/"
                                        + enrollmentId
                                        + "/attempt-grants"))
                .body(view);
    }

    @GetMapping("/api/v1/admin/attempt-grants")
    @PreAuthorize("hasAuthority(T(uz.aifu.lms.iam.app.access.Perm).QUIZ_ATTEMPT_GRANT)")
    @Operation(summary = "List attempt grants")
    public PageResponse<AttemptGrantView> listGrants(
            @RequestParam(required = false) UUID courseId, Pageable pageable) {
        Pageable safePageable = pageableSupport.sanitize(pageable);
        Page<QuizAttemptGrant> page = attemptGrantService.list(courseId, safePageable);
        return PageResponse.of(page, AdminAttemptGrantController::toView);
    }

    private static AttemptGrantView toView(QuizAttemptGrant grant) {
        return new AttemptGrantView(
                grant.getId(),
                grant.getQuiz().getId(),
                grant.getEnrollment().getId(),
                grant.getGrantedBy().getId(),
                grant.getReason(),
                grant.getCreatedAt());
    }
}
