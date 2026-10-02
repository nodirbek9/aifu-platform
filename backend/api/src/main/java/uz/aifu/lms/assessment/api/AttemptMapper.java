package uz.aifu.lms.assessment.api;

import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptSummary;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StudentAttemptSummary;
import uz.aifu.lms.assessment.domain.QuizAttempt;

/**
 * Shared entity-to-DTO mapping for the plain attempt summaries. The full, reveal-policy-aware
 * per-question detail view is assembled in {@code QuizAttemptController} directly — it needs
 * question/option/translation data resolved together with the reveal decision, which does not
 * factor cleanly into a static one-entity-in-one-DTO-out mapper the way a summary does.
 */
final class AttemptMapper {

    private AttemptMapper() {}

    static AttemptSummary toSummary(QuizAttempt attempt) {
        return new AttemptSummary(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getAttemptNumber(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getMaxScore(),
                attempt.getPercent(),
                attempt.getPassed(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt());
    }

    static StudentAttemptSummary toStudentSummary(QuizAttempt attempt) {
        return new StudentAttemptSummary(
                attempt.getId(),
                attempt.getEnrollment().getStudent().getId(),
                attempt.getEnrollment().getStudent().getFullName(),
                attempt.getAttemptNumber(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getMaxScore(),
                attempt.getPercent(),
                attempt.getPassed(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt());
    }
}
