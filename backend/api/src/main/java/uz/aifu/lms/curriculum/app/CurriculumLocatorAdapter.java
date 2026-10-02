package uz.aifu.lms.curriculum.app;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.curriculum.persistence.ActivityRepository;
import uz.aifu.lms.curriculum.persistence.CourseModuleRepository;
import uz.aifu.lms.curriculum.persistence.LessonRepository;
import uz.aifu.lms.iam.app.access.CurriculumLocator;

/**
 * Real implementation of {@link CurriculumLocator} — until now an {@code Optional.empty()} in
 * {@code AccessGuard} because {@code curriculum} did not exist (see {@link
 * uz.aifu.lms.academics.app.CourseTeacherLookupAdapter} for the analogous {@code academics} port,
 * {@link uz.aifu.lms.enrollment.app.EnrollmentLookupAdapter} for {@code enrollment}'s).
 *
 * <p>Quiz/coding-task lookups moved to their own single-implementer ports ({@code
 * AssessmentLocator}, {@code CodingLocator}) in Phase 7 — see ADR-013.
 */
@Component
public class CurriculumLocatorAdapter implements CurriculumLocator {

    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final ActivityRepository activities;

    public CurriculumLocatorAdapter(
            CourseModuleRepository modules,
            LessonRepository lessons,
            ActivityRepository activities) {
        this.modules = modules;
        this.lessons = lessons;
        this.activities = activities;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> courseIdOfModule(UUID moduleId) {
        return modules.findCourseIdById(moduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> courseIdOfLesson(UUID lessonId) {
        return lessons.findCourseIdById(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> courseIdOfActivity(UUID activityId) {
        return activities.findCourseIdById(activityId);
    }
}
