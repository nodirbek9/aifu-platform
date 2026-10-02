package uz.aifu.lms.curriculum.domain;

/**
 * Provenance of a {@link CourseModule}'s grouping. Both supplied syllabi give flat topic lists with
 * no chapters, so any module grouping is a platform decision until AIFU confirms otherwise (V007
 * migration comment).
 */
public enum GroupingSource {
    SYLLABUS_EXPLICIT,
    PLATFORM_INFERRED,
    TEACHER_DEFINED
}
