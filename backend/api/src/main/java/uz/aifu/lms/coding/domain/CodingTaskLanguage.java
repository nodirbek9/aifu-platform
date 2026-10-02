package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * Not a {@link uz.aifu.lms.common.domain.BaseEntity}: {@code coding_task_languages} (V010) has no
 * audit columns.
 *
 * <p>{@link #solutionCode} is NEVER serialized into a student-facing DTO (V010 column comment,
 * skills/coding-execution-security/SKILL.md) — enforced by using a distinct teacher-only response
 * mapper, never a per-field visibility flag here.
 */
@Entity
@Table(name = "coding_task_languages")
public class CodingTaskLanguage {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coding_task_id", nullable = false, updatable = false)
    private CodingTask codingTask;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_code", nullable = false, updatable = false)
    private ProgrammingLanguage language;

    @Column(name = "starter_code")
    private String starterCode;

    @Column(name = "solution_code")
    private String solutionCode;

    protected CodingTaskLanguage() {}

    public CodingTaskLanguage(
            CodingTask codingTask,
            ProgrammingLanguage language,
            String starterCode,
            String solutionCode) {
        this.codingTask = Guards.required(codingTask, "codingTask");
        this.language = Guards.required(language, "language");
        this.starterCode = starterCode;
        this.solutionCode = solutionCode;
    }

    public void update(String starterCode, String solutionCode) {
        this.starterCode = starterCode;
        this.solutionCode = solutionCode;
    }

    public UUID getId() {
        return id;
    }

    public CodingTask getCodingTask() {
        return codingTask;
    }

    public ProgrammingLanguage getLanguage() {
        return language;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public String getSolutionCode() {
        return solutionCode;
    }
}
