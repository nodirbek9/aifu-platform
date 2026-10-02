package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CourseTranslationId implements Serializable {

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "locale")
    private String locale;

    protected CourseTranslationId() {}

    public CourseTranslationId(UUID courseId, String locale) {
        this.courseId = courseId;
        this.locale = locale;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseTranslationId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, locale);
    }
}
