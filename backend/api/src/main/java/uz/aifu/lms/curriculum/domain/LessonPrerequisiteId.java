package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LessonPrerequisiteId implements Serializable {

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "required_lesson_id")
    private UUID requiredLessonId;

    protected LessonPrerequisiteId() {}

    public LessonPrerequisiteId(UUID lessonId, UUID requiredLessonId) {
        this.lessonId = lessonId;
        this.requiredLessonId = requiredLessonId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LessonPrerequisiteId that)) {
            return false;
        }
        return Objects.equals(lessonId, that.lessonId)
                && Objects.equals(requiredLessonId, that.requiredLessonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonId, requiredLessonId);
    }
}
