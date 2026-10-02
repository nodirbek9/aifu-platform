package uz.aifu.lms.analytics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.CodingTaskDifficultySummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.CourseCompletionSummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.LessonCompletionSummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.QuestionDifficultySummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.QuizPerformanceSummary;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.StudentAtRiskSummary;
import uz.aifu.lms.analytics.app.AnalyticsService;
import uz.aifu.lms.analytics.domain.QuizPerformanceView;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Teacher/admin analytics (brief §26) — a thin read-only wrapper over V015's views, gated by {@code
 * ANALYTICS_COURSE_READ}. Course-scoped routes guard on the path's {@code courseId} before loading;
 * quiz-/coding-task-scoped routes load the view row first (it already carries the owning {@code
 * course_id}) and guard on that, the same load-then-guard shape {@code AdminGradeController}/{@code
 * AdminFeedbackController} use — see {@code docs/architecture/README.md}'s {@code an --> iam} note.
 */
@RestController
@Tag(name = "Analytics", description = "Teacher/admin analytics over V015's read models")
@SecurityRequirement(name = "bearerAuth")
public class AdminAnalyticsController {

    private final AnalyticsService analytics;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public AdminAnalyticsController(
            AnalyticsService analytics, AccessGuard accessGuard, PageableSupport pageableSupport) {
        this.analytics = analytics;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/analytics/completion")
    @PreAuthorize("hasAuthority('ANALYTICS_COURSE_READ')")
    @Operation(summary = "Course-level completion summary")
    public CourseCompletionSummary courseCompletion(@PathVariable UUID courseId) {
        accessGuard.requireCourseEditor(courseId);
        return analytics
                .courseCompletion(courseId)
                .map(AnalyticsMapper::toSummary)
                .orElseThrow(() -> Exceptions.notFound("CourseCompletion", courseId));
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/analytics/lessons")
    @PreAuthorize("hasAuthority('ANALYTICS_COURSE_READ')")
    @Operation(summary = "Per-lesson completion summary for a course")
    public List<LessonCompletionSummary> lessonCompletion(@PathVariable UUID courseId) {
        accessGuard.requireCourseEditor(courseId);
        return analytics.lessonCompletion(courseId).stream()
                .map(AnalyticsMapper::toSummary)
                .toList();
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/analytics/quizzes")
    @PreAuthorize("hasAuthority('ANALYTICS_COURSE_READ')")
    @Operation(summary = "Per-quiz performance summary for a course")
    public List<QuizPerformanceSummary> quizPerformanceForCourse(@PathVariable UUID courseId) {
        accessGuard.requireCourseEditor(courseId);
        return analytics.quizPerformanceForCourse(courseId).stream()
                .map(AnalyticsMapper::toSummary)
                .toList();
    }

    @GetMapping("/api/v1/teacher/quizzes/{quizId}/analytics/questions")
    @PreAuthorize("hasAuthority('ANALYTICS_COURSE_READ')")
    @Operation(summary = "Item analysis for every question in one quiz")
    public List<QuestionDifficultySummary> questionDifficulty(@PathVariable UUID quizId) {
        QuizPerformanceView quiz =
                analytics
                        .quizPerformance(quizId)
                        .orElseThrow(() -> Exceptions.notFound("Quiz", quizId));
        accessGuard.requireCourseEditor(quiz.getCourseId());
        return analytics.questionDifficulty(quizId).stream()
                .map(AnalyticsMapper::toSummary)
                .toList();
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/analytics/coding-tasks")
    @PreAuthorize("hasAuthority('ANALYTICS_COURSE_READ')")
    @Operation(summary = "Per-coding-task difficulty summary for a course")
    public List<CodingTaskDifficultySummary> codingTaskDifficulty(@PathVariable UUID courseId) {
        accessGuard.requireCourseEditor(courseId);
        return analytics.codingTaskDifficultyForCourse(courseId).stream()
                .map(AnalyticsMapper::toSummary)
                .toList();
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/analytics/at-risk")
    @PreAuthorize("hasAuthority('ANALYTICS_COURSE_READ')")
    @Operation(summary = "Students who are behind in a course (brief §26's at-risk list)")
    public PageResponse<StudentAtRiskSummary> studentsAtRisk(
            @PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        var page = analytics.studentsAtRisk(courseId, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, AnalyticsMapper::toSummary);
    }
}
