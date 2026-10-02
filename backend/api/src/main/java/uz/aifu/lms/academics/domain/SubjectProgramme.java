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
import uz.aifu.lms.iam.domain.AcademicProgramme;

/**
 * Where a subject sits in a degree programme's curriculum, e.g. {@code curriculumOrdinal="1.09"}.
 */
@Entity
@Table(name = "subject_programmes")
public class SubjectProgramme {

    @EmbeddedId private SubjectProgrammeId id;

    @MapsId("subjectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @MapsId("programmeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id")
    private AcademicProgramme programme;

    @Column(name = "curriculum_ordinal")
    private String curriculumOrdinal;

    @Column(name = "recommended_year")
    private Integer recommendedYear;

    @Column(name = "recommended_term")
    private Integer recommendedTerm;

    protected SubjectProgramme() {}

    public SubjectProgramme(
            Subject subject,
            AcademicProgramme programme,
            String curriculumOrdinal,
            Integer recommendedYear,
            Integer recommendedTerm) {
        this.subject = Guards.required(subject, "subject");
        this.programme = Guards.required(programme, "programme");
        this.id = new SubjectProgrammeId(subject.getId(), programme.getId());
        this.curriculumOrdinal = curriculumOrdinal;
        this.recommendedYear = recommendedYear;
        this.recommendedTerm = recommendedTerm;
    }

    public Subject getSubject() {
        return subject;
    }

    public AcademicProgramme getProgramme() {
        return programme;
    }

    public String getCurriculumOrdinal() {
        return curriculumOrdinal;
    }

    public Integer getRecommendedYear() {
        return recommendedYear;
    }

    public Integer getRecommendedTerm() {
        return recommendedTerm;
    }
}
