package uz.aifu.lms.academics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseDetail;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseSummary;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleResolver;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A teacher's own course list. Self-scoped by the caller's own id (L3 SQL scoping,
 * skills/authorization/SKILL.md) — no separate ownership guard needed for the LIST, since there is
 * no other teacher's id a caller could substitute in. {@code GET /{id}} still goes through {@link
 * AccessGuard#requireCourseEditor} because a courseId in the path IS something another teacher's id
 * could be substituted for.
 */
@RestController
@RequestMapping("/api/v1/teacher/courses")
@Tag(name = "Teacher: courses", description = "The caller's own assigned courses")
@SecurityRequirement(name = "bearerAuth")
public class TeacherCourseController {

    private final CourseService courses;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;
    private final LocaleResolver localeResolver;

    public TeacherCourseController(
            CourseService courses,
            AccessGuard accessGuard,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser,
            LocaleResolver localeResolver) {
        this.courses = courses;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
        this.localeResolver = localeResolver;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_READ')")
    @Operation(summary = "List courses the caller teaches, any status")
    public PageResponse<CourseSummary> myCourses(
            @RequestParam(required = false) String locale,
            Pageable pageable,
            HttpServletRequest http) {
        String resolvedLocale = localeResolver.preferred(locale, http, null, null).code();
        var page =
                courses.findForTeacher(
                        currentUser.require().id(), pageableSupport.sanitize(pageable));
        return PageResponse.of(
                page,
                c ->
                        CourseMapper.toSummary(
                                c, courses.listTranslations(c.getId()), resolvedLocale));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    @Operation(summary = "Get one of the caller's courses in full detail")
    public CourseDetail get(@PathVariable UUID id) {
        accessGuard.requireCourseEditor(id);
        return CourseMapper.toDetail(courses.require(id), courses);
    }
}
