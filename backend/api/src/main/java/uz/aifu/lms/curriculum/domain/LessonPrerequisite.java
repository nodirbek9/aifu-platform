package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * A lesson that must be completed before this one (self-referential, DB CHECK {@code
 * ck_lesson_prereq_not_self} backs the same invariant enforced here in Java).
 */
@Entity
@Table(name = "lesson_prerequisites")
public class LessonPrerequisite {

    @EmbeddedId private LessonPrerequisiteId id;

    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @MapsId("requiredLessonId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "required_lesson_id")
    private Lesson requiredLesson;

    protected LessonPrerequisite() {}

    public LessonPrerequisite(Lesson lesson, Lesson requiredLesson) {
        this.lesson = Guards.required(lesson, "lesson");
        this.requiredLesson = Guards.required(requiredLesson, "requiredLesson");
        if (requiredLesson.equals(lesson)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "A lesson cannot be its own prerequisite");
        }
        this.id = new LessonPrerequisiteId(lesson.getId(), requiredLesson.getId());
    }

    public Lesson getLesson() {
        return lesson;
    }

    public Lesson getRequiredLesson() {
        return requiredLesson;
    }
}
