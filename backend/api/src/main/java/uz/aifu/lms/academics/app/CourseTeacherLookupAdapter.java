package uz.aifu.lms.academics.app;

import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.academics.persistence.CourseRepository;
import uz.aifu.lms.academics.persistence.CourseTeacherRepository;
import uz.aifu.lms.iam.app.access.CourseTeacherLookup;

/**
 * Real implementation of {@link CourseTeacherLookup} — until now an {@code Optional.empty()} in
 * {@code AccessGuard} because {@code academics} did not exist. Every course-scoped ownership
 * decision in the whole platform (teacher-of-course, course-editor, nested content access) is a
 * SQL-backed check through this adapter, never a Java-side comparison
 * (skills/authorization/SKILL.md L3).
 */
@Component
public class CourseTeacherLookupAdapter implements CourseTeacherLookup {

    private final CourseRepository courses;
    private final CourseTeacherRepository courseTeachers;

    public CourseTeacherLookupAdapter(
            CourseRepository courses, CourseTeacherRepository courseTeachers) {
        this.courses = courses;
        this.courseTeachers = courseTeachers;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeacherOfCourse(UUID courseId, UUID userId) {
        return courseTeachers.existsByCourse_IdAndTeacher_Id(courseId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> courseIdsTaughtBy(UUID userId) {
        return courseTeachers.findCourseIdsByTeacherId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseExists(UUID courseId) {
        return courses.existsByIdAndDeletedAtIsNull(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseIsPublished(UUID courseId) {
        return courses.findStatusByIdAndDeletedAtIsNull(courseId)
                .map(CourseStatus::isVisibleToStudents)
                .orElse(false);
    }
}
