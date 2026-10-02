package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CourseSessionAllocationId implements Serializable {

    @Column(name = "course_id")
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type")
    private SessionType sessionType;

    protected CourseSessionAllocationId() {}

    public CourseSessionAllocationId(UUID courseId, SessionType sessionType) {
        this.courseId = courseId;
        this.sessionType = sessionType;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseSessionAllocationId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId) && sessionType == that.sessionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, sessionType);
    }
}
