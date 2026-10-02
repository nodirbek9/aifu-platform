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

/** Per-locale module copy. {@code search_vector} is DB-computed and not mapped here. */
@Entity
@Table(name = "course_module_translations")
public class CourseModuleTranslation {

    @EmbeddedId private CourseModuleTranslationId id;

    @MapsId("moduleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private CourseModule module;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    protected CourseModuleTranslation() {}

    public CourseModuleTranslation(
            CourseModule module, String locale, String title, String description) {
        this.module = Guards.required(module, "module");
        this.id =
                new CourseModuleTranslationId(
                        module.getId(), Guards.requiredText(locale, "locale"));
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

    public CourseModule getModule() {
        return module;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
