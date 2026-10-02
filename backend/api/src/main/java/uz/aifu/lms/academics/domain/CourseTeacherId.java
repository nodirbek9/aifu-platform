package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CourseTeacherId implements Serializable {

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "teacher_user_id")
    private UUID teacherUserId;

    protected CourseTeacherId() {}

    public CourseTeacherId(UUID courseId, UUID teacherUserId) {
        this.courseId = courseId;
        this.teacherUserId = teacherUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseTeacherId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId)
                && Objects.equals(teacherUserId, that.teacherUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, teacherUserId);
    }
}
