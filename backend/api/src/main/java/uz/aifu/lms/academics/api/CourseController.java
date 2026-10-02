package uz.aifu.lms.academics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseDetail;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseSummary;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.academics.domain.CourseTranslation;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;

/**
 * Public course catalogue (one delivery of a subject in one term). Always PUBLISHED/CLOSED-only
 * ({@code Course.getStatus().isVisibleToStudents()}) — a caller wanting drafts uses {@link
 * AdminCourseController} or, for their own assignments, {@link TeacherCourseController}.
 */
@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Public course catalogue")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courses;
    private final PageableSupport pageableSupport;

    public CourseController(CourseService courses, PageableSupport pageableSupport) {
        this.courses = courses;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_READ')")
    @Operation(
            summary = "Browse the published course catalogue",
            description = "Paginated, filtered by subject/term/free-text.")
    public PageResponse<CourseSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) UUID termId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Pageable pageable) {
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        // Public catalogue shows both "still visible to students" states; status filtering beyond
        // that is an admin/teacher concern (AdminCourseController, TeacherCourseController).
        var publishedPage =
                courses.search(
                        query,
                        java.util.EnumSet.of(CourseStatus.PUBLISHED, CourseStatus.CLOSED),
                        subjectId,
                        termId,
                        resolvedLocale,
                        pageableSupport.sanitize(pageable));
        Map<UUID, List<CourseTranslation>> byId =
                courses.listTranslationsForCourses(
                        publishedPage.getContent().stream().map(Course::getId).toList());
        return PageResponse.of(
                publishedPage,
                c ->
                        CourseMapper.toSummary(
                                c, byId.getOrDefault(c.getId(), List.of()), resolvedLocale));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    @Operation(summary = "Get one published course with its rubric and session allocations")
    public CourseDetail get(@PathVariable UUID id) {
        Course course = courses.require(id);
        if (!course.getStatus().isVisibleToStudents()) {
            throw Exceptions.notFound("Course", id);
        }
        return CourseMapper.toDetail(course, courses);
    }

    private static String resolveLocale(String explicit, String acceptLanguage) {
        return LocaleCode.find(explicit)
                .or(() -> LocaleCode.find(firstAcceptLanguageTag(acceptLanguage)))
                .orElse(LocaleCode.DEFAULT)
                .code();
    }

    private static String firstAcceptLanguageTag(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return null;
        }
        return acceptLanguage.split(",")[0].split(";")[0].trim();
    }
}
