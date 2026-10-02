package uz.aifu.lms.analytics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    @Schema(description = "Course-level completion summary")
    public record CourseCompletionSummary(
            UUID courseId,
            long enrolledTotal,
            long completedTotal,
            long inProgressTotal,
            long notStartedTotal,
            BigDecimal avgCompletionPercent,
            BigDecimal avgFinalScore) {}

    @Schema(description = "Lesson-level completion summary")
    public record LessonCompletionSummary(
            UUID lessonId,
            UUID moduleId,
            long trackedStudents,
            long completedStudents,
            BigDecimal avgTimeSpentSeconds) {}

    @Schema(description = "Quiz-level performance summary")
    public record QuizPerformanceSummary(
            UUID quizId,
            long attemptsTotal,
            long studentsAttempted,
            long attemptsPassed,
            long attemptsFailed,
            BigDecimal avgPercent,
            BigDecimal minPercent,
            BigDecimal maxPercent) {}

    @Schema(description = "Item analysis for one question")
    public record QuestionDifficultySummary(
            UUID questionId, long responses, long correctResponses, BigDecimal correctPercent) {}

    @Schema(description = "Coding-task difficulty summary")
    public record CodingTaskDifficultySummary(
            UUID codingTaskId,
            long submissionsTotal,
            long studentsAttempted,
            long acceptedTotal,
            BigDecimal acceptancePercent,
            BigDecimal avgScore,
            BigDecimal avgAttemptsUsed) {}

    @Schema(description = "One student flagged as at-risk in a course")
    public record StudentAtRiskSummary(
            UUID enrollmentId,
            UUID studentUserId,
            BigDecimal completionPercent,
            Instant lastAccessedAt,
            BigDecimal finalScore,
            boolean academicDebt,
            long failedQuizAttempts,
            long unsuccessfulSubmissions) {}
}
