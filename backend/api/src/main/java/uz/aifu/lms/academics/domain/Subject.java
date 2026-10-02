package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.iam.domain.Department;

/**
 * "Fan" — the stable catalogue entity. NEVER holds lessons or credits directly: a lesson hangs off
 * a {@link Course} (one delivery of a subject in one term), and credits/hours live on the course
 * too. {@code DAS1110} (two semesters, different credits each) is the proof this split is required
 * (docs/domain/domain-model.md §1).
 */
@Entity
@Table(name = "subjects")
public class Subject extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_kind", nullable = false)
    private SubjectKind subjectKind = SubjectKind.MANDATORY;

    @Column(name = "default_instruction_language", nullable = false)
    private String defaultInstructionLanguage = LocaleCode.DEFAULT.code();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Subject() {}

    public Subject(
            String code,
            Department department,
            SubjectKind subjectKind,
            LocaleCode defaultInstructionLanguage) {
        this.code = Guards.requiredText(code, "code").toUpperCase();
        this.department = Guards.required(department, "department");
        this.subjectKind = subjectKind == null ? SubjectKind.MANDATORY : subjectKind;
        this.defaultInstructionLanguage =
                (defaultInstructionLanguage == null
                                ? LocaleCode.DEFAULT
                                : defaultInstructionLanguage)
                        .code();
    }

    public void updateCatalogueFields(
            Department department, SubjectKind subjectKind, LocaleCode defaultInstructionLanguage) {
        this.department = Guards.required(department, "department");
        this.subjectKind = subjectKind == null ? SubjectKind.MANDATORY : subjectKind;
        if (defaultInstructionLanguage != null) {
            this.defaultInstructionLanguage = defaultInstructionLanguage.code();
        }
    }

    public void publish(Instant now) {
        if (!status.canTransitionTo(PublicationStatus.PUBLISHED)) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Cannot publish a subject from status " + status);
        }
        this.status = PublicationStatus.PUBLISHED;
        this.publishedAt = now;
    }

    public void archive() {
        if (!status.canTransitionTo(PublicationStatus.ARCHIVED)) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Cannot archive a subject from status " + status);
        }
        this.status = PublicationStatus.ARCHIVED;
    }

    public void returnToDraft() {
        if (!status.canTransitionTo(PublicationStatus.DRAFT)) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Cannot move a subject from status " + status + " back to draft");
        }
        this.status = PublicationStatus.DRAFT;
    }

    public void softDelete(Instant now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String getCode() {
        return code;
    }

    public Department getDepartment() {
        return department;
    }

    public SubjectKind getSubjectKind() {
        return subjectKind;
    }

    public LocaleCode getDefaultInstructionLanguage() {
        return LocaleCode.find(defaultInstructionLanguage).orElse(LocaleCode.DEFAULT);
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
