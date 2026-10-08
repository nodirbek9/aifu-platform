package uz.aifu.lms.progress.app;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.progress.domain.ActivityProgressStatus;
import uz.aifu.lms.progress.domain.CourseProgress;
import uz.aifu.lms.progress.domain.ModuleProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.progress.persistence.ActivityProgressRepository;
import uz.aifu.lms.progress.persistence.CourseProgressRepository;
import uz.aifu.lms.progress.persistence.LessonProgressRepository;
import uz.aifu.lms.progress.persistence.ModuleProgressRepository;

/**
 * Recomputes module- and course-level progress roll-ups from underlying lesson and activity
 * records.
 *
 * <p>Shared between {@link ProgressService} and {@link LearningPathService} (Phase 5, B7).
 */
@Component
class RollupWriter {

    private final CourseProgressRepository courseProgress;
    private final ModuleProgressRepository moduleProgress;
    private final LessonProgressRepository lessonProgress;
    private final ActivityProgressRepository activityProgress;
    private final LessonService lessons;
    private final ActivityService activities;

    RollupWriter(
            CourseProgressRepository courseProgress,
            ModuleProgressRepository moduleProgress,
            LessonProgressRepository lessonProgress,
            ActivityProgressRepository activityProgress,
            LessonService lessons,
            ActivityService activities) {
        this.courseProgress = courseProgress;
        this.moduleProgress = moduleProgress;
        this.lessonProgress = lessonProgress;
        this.activityProgress = activityProgress;
        this.lessons = lessons;
        this.activities = activities;
    }

    void recomputeModuleProgress(Enrollment enrollment, CourseModule module, Instant now) {
        ModuleProgress mp =
                moduleProgress
                        .findByEnrollment_IdAndModule_Id(enrollment.getId(), module.getId())
                        .orElseGet(() -> new ModuleProgress(enrollment, module));
        long total = lessons.countRequiredPublished(module.getId());
        long completed =
                lessonProgress.countCompletedRequiredInModule(
                        enrollment.getId(),
                        module.getId(),
                        PublicationStatus.PUBLISHED,
                        ProgressStatus.COMPLETED);
        mp.recordAccess(now);
        mp.applyRollup((int) total, (int) completed, now);
        moduleProgress.saveAndFlush(mp);
    }

    void recomputeCourseProgress(Enrollment enrollment, UUID courseId, Instant now) {
        CourseProgress cp =
                courseProgress
                        .findById(enrollment.getId())
                        .orElseGet(() -> new CourseProgress(enrollment));
        long lessonsTotal = lessons.countRequiredPublishedInCourse(courseId);
        long lessonsCompleted =
                lessonProgress.countCompletedRequiredInCourse(
                        enrollment.getId(),
                        courseId,
                        PublicationStatus.PUBLISHED,
                        ProgressStatus.COMPLETED);
        long activitiesTotal = activities.countPublished(courseId);
        long activitiesCompleted =
                activityProgress.countCompletedInCourse(
                        enrollment.getId(), courseId, ActivityProgressStatus.COMPLETED);
        cp.recordAccess(now);
        cp.applyRollup(
                (int) lessonsTotal,
                (int) lessonsCompleted,
                (int) activitiesTotal,
                (int) activitiesCompleted,
                now);
        courseProgress.saveAndFlush(cp);
    }
}
