package uz.aifu.lms.grading.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.app.AttemptService;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.MyFeedbackView;
import uz.aifu.lms.grading.app.FeedbackService;
import uz.aifu.lms.grading.domain.Feedback;
import uz.aifu.lms.grading.domain.FeedbackTargetType;
import uz.aifu.lms.iam.app.user.StudentDirectory;

/**
 * A student reads the feedback a teacher left on their own work (product spec S-05.8). Internal
 * notes never appear here, and somebody else's work looks as if it did not exist.
 */
@RestController
@Tag(name = "My feedback", description = "Teacher feedback on the caller's own work")
@SecurityRequirement(name = "bearerAuth")
public class MyFeedbackController {

    private final FeedbackService feedback;
    private final SubmissionService submissions;
    private final AttemptService attempts;
    private final StudentDirectory people;
    private final CurrentUserProvider currentUser;

    public MyFeedbackController(
            FeedbackService feedback,
            SubmissionService submissions,
            AttemptService attempts,
            StudentDirectory people,
            CurrentUserProvider currentUser) {
        this.feedback = feedback;
        this.submissions = submissions;
        this.attempts = attempts;
        this.people = people;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/feedback/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Feedback on one of my submissions or quiz attempts",
            description =
                    "targetType is SUBMISSION or QUIZ_ATTEMPT; targetId must be the caller's.")
    public List<MyFeedbackView> mine(
            @RequestParam FeedbackTargetType targetType, @RequestParam UUID targetId) {
        UUID userId = currentUser.require().id();
        switch (targetType) {
            case SUBMISSION -> submissions.require(targetId, userId);
            case QUIZ_ATTEMPT -> attempts.require(targetId, userId);
            default -> throw Exceptions.validation("targetType must be SUBMISSION or QUIZ_ATTEMPT");
        }
        List<Feedback> visible = feedback.listVisibleForTarget(targetType, targetId);
        Map<UUID, String> authors =
                people.fullNames(
                        visible.stream()
                                .map(Feedback::getAuthorUserId)
                                .collect(Collectors.toSet()));
        return visible.stream()
                .map(
                        f ->
                                new MyFeedbackView(
                                        f.getId(),
                                        authors.get(f.getAuthorUserId()),
                                        f.getBodyMarkdown(),
                                        f.getBodySanitizedHtml(),
                                        f.getCreatedAt(),
                                        f.getUpdatedAt()))
                .toList();
    }
}
