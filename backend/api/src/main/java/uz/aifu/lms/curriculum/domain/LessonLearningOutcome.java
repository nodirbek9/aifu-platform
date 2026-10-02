package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.academics.domain.LearningOutcome;
import uz.aifu.lms.common.util.Guards;

/** Pure join: which learning outcomes a lesson advances. */
@Entity
@Table(name = "lesson_learning_outcomes")
public class LessonLearningOutcome {

    @EmbeddedId private LessonLearningOutcomeId id;

    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @MapsId("learningOutcomeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_outcome_id")
    private LearningOutcome learningOutcome;

    protected LessonLearningOutcome() {}

    public LessonLearningOutcome(Lesson lesson, LearningOutcome learningOutcome) {
        this.lesson = Guards.required(lesson, "lesson");
        this.learningOutcome = Guards.required(learningOutcome, "learningOutcome");
        this.id = new LessonLearningOutcomeId(lesson.getId(), learningOutcome.getId());
    }

    public Lesson getLesson() {
        return lesson;
    }

    public LearningOutcome getLearningOutcome() {
        return learningOutcome;
    }
}
