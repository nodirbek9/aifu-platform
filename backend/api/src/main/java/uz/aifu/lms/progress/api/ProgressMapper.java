package uz.aifu.lms.progress.api;

import java.util.List;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.ModuleProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.StudentCourseProgressSummary;
import uz.aifu.lms.progress.app.CourseProgressSnapshot;
import uz.aifu.lms.progress.app.EnrollmentProgress;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ModuleProgress;

/** Shared entity/snapshot-to-DTO mapping for the progress controllers. */
final class ProgressMapper {

    private ProgressMapper() {}

    static LessonProgressView toLessonProgressView(LessonProgress lp) {
        return new LessonProgressView(
                lp.getLesson().getId(),
                lp.getStatus(),
                lp.getViewCount(),
                lp.getTimeSpentSeconds(),
                lp.getFirstAccessedAt(),
                lp.getLastAccessedAt(),
                lp.getCompletedAt());
    }

    static ModuleProgressView toModuleProgressView(ModuleProgress mp) {
        return new ModuleProgressView(
                mp.getModule().getId(),
                mp.getStatus(),
                mp.getCompletionPercent(),
                mp.getStartedAt(),
                mp.getLastAccessedAt(),
                mp.getCompletedAt());
    }

    static CourseProgressView toCourseProgressView(
            CourseProgressSnapshot snapshot, List<ModuleProgress> modules) {
        return new CourseProgressView(
                snapshot.enrollmentId(),
                snapshot.courseId(),
                snapshot.status(),
                snapshot.completionPercent(),
                snapshot.lessonsTotal(),
                snapshot.lessonsCompleted(),
                snapshot.activitiesTotal(),
                snapshot.activitiesCompleted(),
                snapshot.timeSpentSeconds(),
                snapshot.startedAt(),
                snapshot.lastAccessedAt(),
                snapshot.completedAt(),
                modules.stream().map(ProgressMapper::toModuleProgressView).toList());
    }

    static StudentCourseProgressSummary toStudentSummary(EnrollmentProgress ep) {
        CourseProgressSnapshot snapshot = ep.progress();
        return new StudentCourseProgressSummary(
                snapshot.enrollmentId(),
                ep.enrollment().getStudent().getId(),
                ep.enrollment().getStudent().getFullName(),
                ep.enrollment().getAttemptNo(),
                snapshot.status(),
                snapshot.completionPercent(),
                snapshot.lessonsTotal(),
                snapshot.lessonsCompleted(),
                snapshot.lastAccessedAt());
    }
}
