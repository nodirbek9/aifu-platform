package uz.aifu.lms.academics.app;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseTranslation;
import uz.aifu.lms.academics.persistence.CourseRepository;
import uz.aifu.lms.academics.persistence.CourseTeacherRepository;
import uz.aifu.lms.academics.persistence.CourseTranslationRepository;
import uz.aifu.lms.common.i18n.LocaleCode;

/**
 * Small read model of courses for modules that only need to name a course or know who teaches it
 * (timetable cells, load reports, attendance journals) without loading the whole aggregate.
 */
@Service
public class CourseDirectory {

    /**
     * How a course is shown in a list: its subject code and a title in the best available locale.
     */
    public record CourseLabel(UUID courseId, String subjectCode, String title, UUID termId) {}

    private final CourseRepository courses;
    private final CourseTranslationRepository translations;
    private final CourseTeacherRepository courseTeachers;

    public CourseDirectory(
            CourseRepository courses,
            CourseTranslationRepository translations,
            CourseTeacherRepository courseTeachers) {
        this.courses = courses;
        this.translations = translations;
        this.courseTeachers = courseTeachers;
    }

    /** Labels for the given courses; ids that do not exist are simply absent from the result. */
    @Transactional(readOnly = true)
    public Map<UUID, CourseLabel> labels(Collection<UUID> courseIds, LocaleCode locale) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<CourseTranslation>> byCourse =
                translations.findAllByCourseIds(courseIds).stream()
                        .collect(Collectors.groupingBy(t -> t.getCourse().getId()));
        Map<UUID, CourseLabel> labels = new LinkedHashMap<>();
        for (Course course : courses.findByIdIn(courseIds)) {
            labels.put(
                    course.getId(),
                    label(course, byCourse.getOrDefault(course.getId(), List.of()), locale));
        }
        return labels;
    }

    @Transactional(readOnly = true)
    public List<CourseLabel> coursesOfTerm(UUID termId, LocaleCode locale) {
        List<Course> found = courses.findByTerm_IdAndDeletedAtIsNull(termId);
        if (found.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<CourseTranslation>> byCourse =
                translations.findAllByCourseIds(found.stream().map(Course::getId).toList()).stream()
                        .collect(Collectors.groupingBy(t -> t.getCourse().getId()));
        return found.stream()
                .map(c -> label(c, byCourse.getOrDefault(c.getId(), List.of()), locale))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID courseId) {
        return courses.existsByIdAndDeletedAtIsNull(courseId);
    }

    @Transactional(readOnly = true)
    public boolean isTeacherOfCourse(UUID courseId, UUID teacherUserId) {
        return courseTeachers.existsByCourse_IdAndTeacher_Id(courseId, teacherUserId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> courseIdsOfTeacher(UUID teacherUserId) {
        return courseTeachers.findCourseIdsByTeacherId(teacherUserId);
    }

    /**
     * Requested locale, then the course's own language, then Uzbek, then anything; else the code.
     */
    private static CourseLabel label(
            Course course, List<CourseTranslation> all, LocaleCode locale) {
        Map<String, String> titles =
                all.stream()
                        .collect(
                                Collectors.toMap(
                                        CourseTranslation::getLocale,
                                        CourseTranslation::getTitle,
                                        (a, b) -> a));
        String title = titles.get(locale.code());
        if (title == null && course.getInstructionLanguage() != null) {
            title = titles.get(course.getInstructionLanguage().code());
        }
        if (title == null) {
            title = titles.get(LocaleCode.DEFAULT.code());
        }
        if (title == null) {
            title = titles.values().stream().findFirst().orElse(course.getSubject().getCode());
        }
        return new CourseLabel(
                course.getId(), course.getSubject().getCode(), title, course.getTerm().getId());
    }
}
