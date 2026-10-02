package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LessonTranslationId implements Serializable {

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "locale")
    private String locale;

    protected LessonTranslationId() {}

    public LessonTranslationId(UUID lessonId, String locale) {
        this.lessonId = lessonId;
        this.locale = locale;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LessonTranslationId that)) {
            return false;
        }
        return Objects.equals(lessonId, that.lessonId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonId, locale);
    }
}
