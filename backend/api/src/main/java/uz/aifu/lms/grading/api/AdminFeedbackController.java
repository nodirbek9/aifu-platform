package uz.aifu.lms.grading.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.app.AttemptService;
import uz.aifu.lms.assessment.domain.QuizAttempt;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.CreateFeedbackRequest;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.FeedbackView;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.UpdateFeedbackRequest;
import uz.aifu.lms.grading.app.FeedbackService;
import uz.aifu.lms.grading.app.GradeEntryService;
import uz.aifu.lms.grading.domain.Feedback;
import uz.aifu.lms.grading.domain.FeedbackTargetType;
import uz.aifu.lms.grading.domain.GradeEntry;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Teacher-authored feedback (V012). {@code FEEDBACK_WRITE} (TEACHER, V003) covers both read and
 * write here — there is no separate {@code FEEDBACK_READ} permission seeded, matching how {@code
 * CODING_TASK_MANAGE} covers both in {@code coding.api.AdminCodingTaskController} (Phase 8). {@code
 * targetType} is polymorphic (V012's own design), so ownership is resolved per type before every
 * mutation or read — {@link AccessGuard#requireCourseEditor} is what actually restricts a teacher
 * to a course they are assigned to (skills/authorization/SKILL.md L2); skipping this would let any
 * teacher touch any other teacher's course feedback, the exact class of bug found and fixed in
 * {@code coding.api.AdminCodingTaskContentController} (Phase 8, defect #7).
 *
 * <p><b>No student-facing read endpoint exists yet</b> — V003 seeds no student-facing feedback
 * permission at all (only {@code FEEDBACK_WRITE}, teacher-only), unlike every other
 * *_SELF_READ-shaped resource in this codebase. Rather than invent a permission the schema design
 * doesn't provide, the honest read is that student-visible feedback is meant to surface embedded in
 * the target's own response (a submission's or attempt's detail view), not as a standalone endpoint
 * — see {@code docs/BUILD-STATUS.md}'s "What remains" for Phase 9.
 */
@RestController
@Tag(
        name = "Teacher: feedback",
        description = "Feedback on a submission, attempt, grade, activity, or enrollment")
@SecurityRequirement(name = "bearerAuth")
public class AdminFeedbackController {

    private final FeedbackService feedback;
    private final GradeEntryService grades;
    private final EnrollmentService enrollments;
    private final SubmissionService submissions;
    private final AttemptService attempts;
    private final ActivityService activities;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;

    public AdminFeedbackController(
            FeedbackService feedback,
            GradeEntryService grades,
            EnrollmentService enrollments,
            SubmissionService submissions,
            AttemptService attempts,
            ActivityService activities,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser) {
        this.feedback = feedback;
        this.grades = grades;
        this.enrollments = enrollments;
        this.submissions = submissions;
        this.attempts = attempts;
        this.activities = activities;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/teacher/feedback")
    @PreAuthorize("hasAuthority('FEEDBACK_WRITE')")
    @Operation(summary = "List every feedback entry for one target, including INTERNAL notes")
    public List<FeedbackView> listForTarget(
            @RequestParam FeedbackTargetType targetType, @RequestParam UUID targetId) {
        requireCourseEditorOfTarget(targetType, targetId);
        return feedback.listForTarget(targetType, targetId).stream()
                .map(FeedbackMapper::toView)
                .toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/feedback")
    @PreAuthorize("hasAuthority('FEEDBACK_WRITE')")
    @Operation(summary = "Write feedback on a submission, attempt, grade, activity, or enrollment")
    public ResponseEntity<FeedbackView> create(
            @Valid @RequestBody CreateFeedbackRequest request, HttpServletRequest http) {
        requireCourseEditorOfTarget(request.targetType(), request.targetId());
        UUID actorId = currentUser.require().id();
        Recipient recipient = recipientOf(request.targetType(), request.targetId());
        Feedback saved =
                feedback.create(
                        request.targetType(),
                        request.targetId(),
                        actorId,
                        request.visibility(),
                        request.bodyMarkdown(),
                        actorId,
                        http.getRemoteAddr(),
                        recipient.studentUserId(),
                        recipient.linkContext());
        return ResponseEntity.created(URI.create("/api/v1/teacher/feedback/" + saved.getId()))
                .body(FeedbackMapper.toView(saved));
    }

    @PatchMapping("/api/v1/teacher/feedback/{id}")
    @PreAuthorize("hasAuthority('FEEDBACK_WRITE')")
    @Operation(summary = "Edit previously written feedback")
    public FeedbackView update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeedbackRequest request,
            HttpServletRequest http) {
        Feedback existing = feedback.require(id);
        requireCourseEditorOfTarget(existing.getTargetType(), existing.getTargetId());
        Feedback updated =
                feedback.update(
                        id,
                        request.visibility(),
                        request.bodyMarkdown(),
                        currentUser.require().id(),
                        http.getRemoteAddr());
        return FeedbackMapper.toView(updated);
    }

    /**
     * Every branch resolves a courseId from an already-eagerly-fetched association (never a bare
     * lazy proxy touched after its owning read-only transaction closed — {@code
     * spring.jpa.open-in-view: false}, see the class doc).
     */
    private void requireCourseEditorOfTarget(FeedbackTargetType targetType, UUID targetId) {
        UUID courseId =
                switch (targetType) {
                    case SUBMISSION -> {
                        Submission submission = submissions.require(targetId);
                        yield submission.getCodingTask().getCourse().getId();
                    }
                    case QUIZ_ATTEMPT -> {
                        QuizAttempt attempt = attempts.requireForReview(targetId);
                        yield attempt.getQuiz().getCourse().getId();
                    }
                    case GRADE_ENTRY -> {
                        GradeEntry entry = grades.require(targetId);
                        yield enrollments
                                .require(entry.getEnrollment().getId())
                                .getCourse()
                                .getId();
                    }
                    case ACTIVITY -> activities.require(targetId).getCourse().getId();
                    case ENROLLMENT -> enrollments.require(targetId).getCourse().getId();
                };
        accessGuard.requireCourseEditor(courseId);
    }

    /** Whom to tell about new feedback, with the ids a client needs to open the work. */
    private record Recipient(UUID studentUserId, Map<String, Object> linkContext) {}

    private Recipient recipientOf(FeedbackTargetType targetType, UUID targetId) {
        return switch (targetType) {
            case SUBMISSION -> {
                Submission submission = submissions.require(targetId);
                yield new Recipient(
                        submission.getEnrollment().getStudent().getId(),
                        Map.of(
                                "courseId",
                                submission.getCodingTask().getCourse().getId().toString(),
                                "codingTaskId",
                                submission.getCodingTask().getId().toString()));
            }
            case QUIZ_ATTEMPT -> {
                QuizAttempt attempt = attempts.requireForReview(targetId);
                yield new Recipient(
                        attempt.getEnrollment().getStudent().getId(),
                        Map.of(
                                "courseId",
                                attempt.getQuiz().getCourse().getId().toString(),
                                "quizId",
                                attempt.getQuiz().getId().toString()));
            }
            default -> new Recipient(null, Map.of());
        };
    }
}
