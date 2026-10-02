package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.util.Guards;

/** Per-locale lesson copy. {@code search_vector} is DB-computed and not mapped here. */
@Entity
@Table(name = "lesson_translations")
public class LessonTranslation {

    @EmbeddedId private LessonTranslationId id;

    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary")
    private String summary;

    protected LessonTranslation() {}

    public LessonTranslation(Lesson lesson, String locale, String title, String summary) {
        this.lesson = Guards.required(lesson, "lesson");
        this.id = new LessonTranslationId(lesson.getId(), Guards.requiredText(locale, "locale"));
        this.title = Guards.requiredText(title, "title");
        this.summary = summary;
    }

    public void update(String title, String summary) {
        this.title = Guards.requiredText(title, "title");
        this.summary = summary;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }
}
