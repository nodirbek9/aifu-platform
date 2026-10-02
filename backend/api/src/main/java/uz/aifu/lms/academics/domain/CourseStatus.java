package uz.aifu.lms.academics.domain;

/**
 * Course lifecycle. A superset of {@link uz.aifu.lms.common.domain.PublicationStatus}: a course can
 * additionally be {@link #CLOSED} (enrollment window ended) without being archived.
 */
public enum CourseStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED,
    CLOSED;

    public boolean isVisibleToStudents() {
        return this == PUBLISHED || this == CLOSED;
    }
}
