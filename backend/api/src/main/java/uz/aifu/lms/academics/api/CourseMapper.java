package uz.aifu.lms.academics.api;

import java.util.List;
import java.util.Set;
import uz.aifu.lms.academics.api.dto.CourseDtos.AssessmentComponentView;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseDetail;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseSummary;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseTranslationView;
import uz.aifu.lms.academics.api.dto.CourseDtos.SessionAllocationView;
import uz.aifu.lms.academics.api.dto.CourseDtos.TeacherView;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseTranslation;

/**
 * Shared entity-to-DTO mapping for {@link CourseController}, {@link AdminCourseController} and
 * {@link TeacherCourseController}.
 */
final class CourseMapper {

    private CourseMapper() {}

    record TitleResolution(String text, boolean fallback) {}

    static TitleResolution resolveTitle(
            List<CourseTranslation> translations, String requestedLocale, String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(requestedLocale))
                .findFirst()
                .map(t -> new TitleResolution(t.getTitle(), false))
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst()
                                        .map(t -> new TitleResolution(t.getTitle(), true)))
                .or(
                        () ->
                                translations.stream()
                                        .findFirst()
                                        .map(t -> new TitleResolution(t.getTitle(), true)))
                .orElse(new TitleResolution(null, true));
    }

    static CourseSummary toSummary(
            Course course, List<CourseTranslation> translations, String resolvedLocale) {
        TitleResolution title =
                resolveTitle(translations, resolvedLocale, course.getInstructionLanguage().code());
        return new CourseSummary(
                course.getId(),
                course.getSubject().getCode(),
                title.text(),
                course.getTerm().getName(),
                course.getCohortCode(),
                course.getCredits(),
                course.getTotalHours(),
                course.getStatus());
    }

    static CourseDetail toDetail(Course course, CourseService courses) {
        List<CourseTranslationView> translationViews =
                courses.listTranslations(course.getId()).stream()
                        .map(
                                t ->
                                        new CourseTranslationView(
                                                t.getLocale(), t.getTitle(), t.getDescription()))
                        .toList();
        List<TeacherView> teacherViews =
                courses.listTeachers(course.getId()).stream()
                        .map(
                                ct ->
                                        new TeacherView(
                                                ct.getTeacher().getId(),
                                                ct.getTeacher().getFullName(),
                                                ct.getTeacherRole()))
                        .toList();
        List<SessionAllocationView> allocationViews =
                courses.listSessionAllocations(course.getId()).stream()
                        .map(
                                a ->
                                        new SessionAllocationView(
                                                a.getSessionType(), a.getAllocatedHours()))
                        .toList();
        List<AssessmentComponentView> componentViews =
                courses.listAssessmentComponents(course.getId()).stream()
                        .map(CourseMapper::toComponentView)
                        .toList();
        return new CourseDetail(
                course.getId(),
                course.getSubject().getId(),
                course.getSubject().getCode(),
                course.getTerm().getId(),
                course.getTerm().getName(),
                course.getCohortCode(),
                course.getCredits(),
                course.getTotalHours(),
                course.getWeeklyContactHours(),
                course.getInstructionLanguage().code(),
                Set.of(course.getRequiredLocales()),
                course.getDeliveryMode(),
                course.getAssessmentForm(),
                course.getStatus(),
                course.getEnrollmentOpensAt(),
                course.getEnrollmentClosesAt(),
                course.getMaxEnrollment(),
                course.getPublishedAt(),
                translationViews,
                teacherViews,
                allocationViews,
                componentViews);
    }

    private static AssessmentComponentView toComponentView(AssessmentComponent c) {
        return new AssessmentComponentView(
                c.getId(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getCode(),
                c.getKind(),
                c.getMaxPoints(),
                c.getMinPassPercent(),
                c.isBlocksFinal(),
                c.getAssessmentForm(),
                c.getDisplayOrder());
    }
}
