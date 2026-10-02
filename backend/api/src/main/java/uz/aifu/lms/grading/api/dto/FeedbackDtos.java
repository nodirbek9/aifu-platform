package uz.aifu.lms.grading.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.grading.domain.FeedbackTargetType;
import uz.aifu.lms.grading.domain.FeedbackVisibility;

public final class FeedbackDtos {

    private FeedbackDtos() {}

    public record CreateFeedbackRequest(
            @NotNull FeedbackTargetType targetType,
            @NotNull UUID targetId,
            FeedbackVisibility visibility,
            @NotBlank String bodyMarkdown) {}

    public record UpdateFeedbackRequest(
            FeedbackVisibility visibility, @NotBlank String bodyMarkdown) {}

    public record FeedbackView(
            UUID id,
            FeedbackTargetType targetType,
            UUID targetId,
            UUID authorUserId,
            FeedbackVisibility visibility,
            String bodyMarkdown,
            String bodySanitizedHtml,
            Instant createdAt,
            Instant updatedAt) {}
}
