package uz.aifu.lms.progress.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.i18n.LocaleResolver;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CoursePlanView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.MyActivityView;
import uz.aifu.lms.progress.app.StudentWorkService;

/**
 * The student's course page and work list (product spec S-01.2, S-01.3, S-03.2, S-07): the course
 * plan with a status on every lesson, and all quizzes and tasks across the student's courses.
 */
@RestController
@Tag(name = "My progress", description = "Course plan with lesson statuses; my quizzes and tasks")
@SecurityRequirement(name = "bearerAuth")
public class StudentWorkController {

    private final StudentWorkService work;
    private final CurrentUserProvider currentUser;
    private final LocaleResolver localeResolver;

    public StudentWorkController(
            StudentWorkService work,
            CurrentUserProvider currentUser,
            LocaleResolver localeResolver) {
        this.work = work;
        this.currentUser = currentUser;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/api/v1/courses/{courseId}/plan/me")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_READ')")
    @Operation(
            summary = "The course's modules and lessons with my status on each, in one call",
            description =
                    "Published content only. A lesson is locked while a lesson it requires is not"
                            + " completed. When the course learning path is enabled the lock, its lockReason and"
                            + " the lecture/test/practice steps come from the server gate; midterm shows the"
                            + " midterm standing. A teacher or admin gets the same tree without statuses.")
    public CoursePlanView plan(
            @PathVariable UUID courseId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return work.plan(courseId, locale(locale, http));
    }

    @GetMapping("/api/v1/activities/me")
    @PreAuthorize("hasAuthority('PROGRESS_SELF_READ')")
    @Operation(
            summary = "My quizzes, coding tasks and other activities across my courses",
            description = "Soonest deadline first; activities without a deadline come last.")
    public List<MyActivityView> myWork(
            @RequestParam(required = false) UUID courseId,
            @Parameter(description = "UPCOMING, TODO, IN_PROGRESS, COMPLETED or OVERDUE")
                    @RequestParam(required = false)
                    String state,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return work.myWork(currentUser.require().id(), courseId, state, locale(locale, http));
    }

    private LocaleCode locale(String explicit, HttpServletRequest http) {
        return localeResolver.preferred(explicit, http, null, null);
    }
}
