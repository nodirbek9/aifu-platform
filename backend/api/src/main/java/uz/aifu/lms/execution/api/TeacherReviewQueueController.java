package uz.aifu.lms.execution.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.i18n.LocaleResolver;
import uz.aifu.lms.execution.app.ReviewQueueService;
import uz.aifu.lms.execution.domain.ReviewQueueFilter;
import uz.aifu.lms.iam.app.access.AccessGuard;

@RestController
@Tag(name = "Teacher: submissions", description = "A teacher's own coding task's submission roster")
@SecurityRequirement(name = "bearerAuth")
public class TeacherReviewQueueController {

    private static final int MAX_PAGE_SIZE = 100;

    @Schema(description = "One submission waiting in (or already through) a teacher's review")
    public record ReviewQueueItem(
            UUID submissionId,
            UUID codingTaskId,
            @Schema(description = "In the requested language, else Uzbek, else the task code")
                    String taskTitle,
            UUID studentUserId,
            String studentFullName,
            String groupCode,
            int attemptNumber,
            String languageCode,
            String status,
            String verdict,
            BigDecimal score,
            BigDecimal maxScore,
            Instant submittedAt,
            boolean hasFeedback) {}

    private final ReviewQueueService queue;
    private final AccessGuard accessGuard;
    private final LocaleResolver localeResolver;

    public TeacherReviewQueueController(
            ReviewQueueService queue, AccessGuard accessGuard, LocaleResolver localeResolver) {
        this.queue = queue;
        this.accessGuard = accessGuard;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/submissions")
    @PreAuthorize("hasAuthority('SUBMISSION_REVIEW')")
    @Operation(
            summary =
                    "The review queue of a course: submissions to every coding task, newest first",
            description =
                    "Filters: one task, one verdict, one group, only those without feedback, only"
                            + " each student's latest attempt per task.")
    public PageResponse<ReviewQueueItem> list(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) String verdict,
            @RequestParam(required = false) String groupCode,
            @RequestParam(defaultValue = "false") boolean needsFeedback,
            @RequestParam(defaultValue = "false") boolean latestOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        ReviewQueueFilter filter =
                new ReviewQueueFilter(
                        taskId,
                        blankToNull(verdict),
                        blankToNull(groupCode),
                        needsFeedback,
                        latestOnly);
        ReviewQueueService.Page found =
                queue.find(
                        courseId,
                        filter,
                        localeResolver.preferred(locale, http, null, null).code(),
                        safeSize,
                        safePage);
        long total = found.total();
        List<ReviewQueueItem> content =
                found.content().stream()
                        .map(
                                i ->
                                        new ReviewQueueItem(
                                                i.submissionId(),
                                                i.codingTaskId(),
                                                i.taskTitle(),
                                                i.studentUserId(),
                                                i.studentFullName(),
                                                i.groupCode(),
                                                i.attemptNumber(),
                                                i.languageCode(),
                                                i.status(),
                                                i.verdict(),
                                                i.score(),
                                                i.maxScore(),
                                                i.submittedAt(),
                                                i.hasFeedback()))
                        .toList();
        int totalPages = (int) ((total + safeSize - 1) / safeSize);
        return new PageResponse<>(
                content,
                safePage,
                safeSize,
                total,
                totalPages,
                safePage + 1 < totalPages,
                safePage > 0);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
