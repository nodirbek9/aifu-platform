package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LessonLearningOutcomeId implements Serializable {

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "learning_outcome_id")
    private UUID learningOutcomeId;

    protected LessonLearningOutcomeId() {}

    public LessonLearningOutcomeId(UUID lessonId, UUID learningOutcomeId) {
        this.lessonId = lessonId;
        this.learningOutcomeId = learningOutcomeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LessonLearningOutcomeId that)) {
            return false;
        }
        return Objects.equals(lessonId, that.lessonId)
                && Objects.equals(learningOutcomeId, that.learningOutcomeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonId, learningOutcomeId);
    }
}
