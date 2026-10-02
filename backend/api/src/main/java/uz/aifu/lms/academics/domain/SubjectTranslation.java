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

/**
 * Per-locale subject copy (ADR-005). {@code search_vector} is a Postgres {@code GENERATED ALWAYS}
 * column and is deliberately not mapped here — it is DB-computed and read-only.
 */
@Entity
@Table(name = "subject_translations")
public class SubjectTranslation {

    @EmbeddedId private SubjectTranslationId id;

    @MapsId("subjectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "short_title")
    private String shortTitle;

    @Column(name = "summary")
    private String summary;

    @Column(name = "description")
    private String description;

    /** "Fan maqsadi". */
    @Column(name = "aim")
    private String aim;

    /** "Fanning vazifasi". */
    @Column(name = "objectives")
    private String objectives;

    protected SubjectTranslation() {}

    public SubjectTranslation(Subject subject, String locale, String title) {
        this.subject = Guards.required(subject, "subject");
        this.id = new SubjectTranslationId(subject.getId(), Guards.requiredText(locale, "locale"));
        this.title = Guards.requiredText(title, "title");
    }

    public void update(
            String title,
            String shortTitle,
            String summary,
            String description,
            String aim,
            String objectives) {
        this.title = Guards.requiredText(title, "title");
        this.shortTitle = shortTitle;
        this.summary = summary;
        this.description = description;
        this.aim = aim;
        this.objectives = objectives;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public Subject getSubject() {
        return subject;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getAim() {
        return aim;
    }

    public String getObjectives() {
        return objectives;
    }
}
