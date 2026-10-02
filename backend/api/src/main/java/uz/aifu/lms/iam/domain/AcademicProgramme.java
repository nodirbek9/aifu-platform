package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import uz.aifu.lms.common.domain.BaseEntity;

/**
 * AIFU "ta'lim yo'nalishi" (degree programme), e.g. {@code 60610600}. Created alongside {@link
 * Department} in the same migration (V002) because both are org-structure reference data owned by
 * identity, not by the subject catalogue — {@code academics} only reads them.
 */
@Entity
@Table(name = "academic_programmes")
public class AcademicProgramme extends BaseEntity {

    public enum DegreeLevel {
        BACHELOR,
        MASTER,
        PHD
    }

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "knowledge_area_code")
    private String knowledgeAreaCode;

    @Column(name = "education_area_code")
    private String educationAreaCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_level", nullable = false)
    private DegreeLevel degreeLevel = DegreeLevel.BACHELOR;

    protected AcademicProgramme() {}

    public AcademicProgramme(String code, String name, DegreeLevel degreeLevel) {
        this.code = code;
        this.name = name;
        this.degreeLevel = degreeLevel == null ? DegreeLevel.BACHELOR : degreeLevel;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getKnowledgeAreaCode() {
        return knowledgeAreaCode;
    }

    public String getEducationAreaCode() {
        return educationAreaCode;
    }

    public DegreeLevel getDegreeLevel() {
        return degreeLevel;
    }
}
