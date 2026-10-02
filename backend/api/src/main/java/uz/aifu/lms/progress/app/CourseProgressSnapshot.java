package uz.aifu.lms.progress.app;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.progress.domain.ProgressStatus;

/**
 * A course-level progress reading, present whether or not a {@code course_progress} row exists yet.
 * An enrollment the student has never touched has no row (nothing has triggered a roll-up), but is
 * still a legitimate "0 of N, not started" answer rather than a 404 — {@code lessonsTotal}/ {@code
 * activitiesTotal} are computed live from the course's current published content in that case.
 */
public record CourseProgressSnapshot(
        UUID enrollmentId,
        UUID courseId,
        ProgressStatus status,
        BigDecimal completionPercent,
        int lessonsTotal,
        int lessonsCompleted,
        int activitiesTotal,
        int activitiesCompleted,
        long timeSpentSeconds,
        Instant startedAt,
        Instant lastAccessedAt,
        Instant completedAt) {}
