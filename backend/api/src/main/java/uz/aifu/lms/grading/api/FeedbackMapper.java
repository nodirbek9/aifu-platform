package uz.aifu.lms.grading.api;

import uz.aifu.lms.grading.api.dto.FeedbackDtos.FeedbackView;
import uz.aifu.lms.grading.domain.Feedback;

final class FeedbackMapper {

    private FeedbackMapper() {}

    static FeedbackView toView(Feedback feedback) {
        return new FeedbackView(
                feedback.getId(),
                feedback.getTargetType(),
                feedback.getTargetId(),
                feedback.getAuthorUserId(),
                feedback.getVisibility(),
                feedback.getBodyMarkdown(),
                feedback.getBodySanitizedHtml(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt());
    }
}
