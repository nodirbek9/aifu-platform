package uz.aifu.lms.progress.app;

import uz.aifu.lms.enrollment.domain.Enrollment;

/** One roster row: a student's enrollment paired with their course-level progress snapshot. */
public record EnrollmentProgress(Enrollment enrollment, CourseProgressSnapshot progress) {}
