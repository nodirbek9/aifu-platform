package uz.aifu.lms.analytics.api;

import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.CodingTaskDifficultySummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.CourseCompletionSummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.LessonCompletionSummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.QuestionDifficultySummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.QuizPerformanceSummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.StudentAtRiskSummary;
import uz.aifu.lms.analytics.domain.CodingTaskDifficultyView;
import uz.aifu.lms.analytics.domain.CourseCompletionView;
import uz.aifu.lms.analytics.domain.LessonCompletionView;
import uz.aifu.lms.analytics.domain.QuestionDifficultyView;
import uz.aifu.lms.analytics.domain.QuizPerformanceView;
import uz.aifu.lms.analytics.domain.StudentAtRiskView;

final class AnalyticsMapper {

    private AnalyticsMapper() {}

    static CourseCompletionSummary toSummary(CourseCompletionView v) {
        return new CourseCompletionSummary(
                v.getCourseId(),
                v.getEnrolledTotal(),
                v.getCompletedTotal(),
                v.getInProgressTotal(),
                v.getNotStartedTotal(),
                v.getAvgCompletionPercent(),
                v.getAvgFinalScore());
    }

    static LessonCompletionSummary toSummary(LessonCompletionView v) {
        return new LessonCompletionSummary(
                v.getLessonId(),
                v.getModuleId(),
                v.getTrackedStudents(),
                v.getCompletedStudents(),
                v.getAvgTimeSpentSeconds());
    }

    static QuizPerformanceSummary toSummary(QuizPerformanceView v) {
        return new QuizPerformanceSummary(
                v.getQuizId(),
                v.getAttemptsTotal(),
                v.getStudentsAttempted(),
                v.getAttemptsPassed(),
                v.getAttemptsFailed(),
                v.getAvgPercent(),
                v.getMinPercent(),
                v.getMaxPercent());
    }

    static QuestionDifficultySummary toSummary(QuestionDifficultyView v) {
        return new QuestionDifficultySummary(
                v.getQuestionId(),
                v.getResponses(),
                v.getCorrectResponses(),
                v.getCorrectPercent());
    }

    static CodingTaskDifficultySummary toSummary(CodingTaskDifficultyView v) {
        return new CodingTaskDifficultySummary(
                v.getCodingTaskId(),
                v.getSubmissionsTotal(),
                v.getStudentsAttempted(),
                v.getAcceptedTotal(),
                v.getAcceptancePercent(),
                v.getAvgScore(),
                v.getAvgAttemptsUsed());
    }

    static StudentAtRiskSummary toSummary(StudentAtRiskView v) {
        return new StudentAtRiskSummary(
                v.getEnrollmentId(),
                v.getStudentUserId(),
                v.getCompletionPercent(),
                v.getLastAccessedAt(),
                v.getFinalScore(),
                v.isAcademicDebt(),
                v.getFailedQuizAttempts(),
                v.getUnsuccessfulSubmissions());
    }
}
