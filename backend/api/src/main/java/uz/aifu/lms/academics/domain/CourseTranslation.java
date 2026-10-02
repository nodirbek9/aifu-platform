package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.util.Guards;

@Entity
@Table(name = "course_translations")
public class CourseTranslation {

    @EmbeddedId private CourseTranslationId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    protected CourseTranslation() {}

    public CourseTranslation(Course course, String locale, String title, String description) {
        this.course = Guards.required(course, "course");
        this.id = new CourseTranslationId(course.getId(), Guards.requiredText(locale, "locale"));
        this.title = Guards.requiredText(title, "title");
        this.description = description;
    }

    public void update(String title, String description) {
        this.title = Guards.requiredText(title, "title");
        this.description = description;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public Course getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
