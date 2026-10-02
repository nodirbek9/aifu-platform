package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * Append-only-in-practice: prerequisites are created and deleted, never edited in place, so the
 * table has no {@code updated_at} — {@link uz.aifu.lms.common.domain.BaseEntity} does not fit.
 *
 * <p>Some syllabi state a prerequisite as prose only ({@code IAKTT1105}: "boshlang'ich matematik
 * bilimlar"), with no coded subject to point at — {@link #prerequisiteSubject} is then null and
 * {@link #freeTextNote} carries the verbatim text (DB CHECK {@code ck_prereq_has_target}).
 */
@Entity
@Table(name = "subject_prerequisites")
public class SubjectPrerequisite {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false, updatable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_subject_id", updatable = false)
    private Subject prerequisiteSubject;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, updatable = false)
    private RequirementType requirementType = RequirementType.REQUIRED;

    @Column(name = "free_text_note", updatable = false)
    private String freeTextNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SubjectPrerequisite() {}

    public SubjectPrerequisite(
            Subject subject,
            Subject prerequisiteSubject,
            RequirementType requirementType,
            String freeTextNote) {
        this.subject = Guards.required(subject, "subject");
        if (prerequisiteSubject != null && prerequisiteSubject.equals(subject)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "A subject cannot be its own prerequisite");
        }
        if (prerequisiteSubject == null && (freeTextNote == null || freeTextNote.isBlank())) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "A prerequisite needs either a coded subject or a free-text note");
        }
        this.prerequisiteSubject = prerequisiteSubject;
        this.requirementType = requirementType == null ? RequirementType.REQUIRED : requirementType;
        this.freeTextNote = freeTextNote;
    }

    public UUID getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public Subject getPrerequisiteSubject() {
        return prerequisiteSubject;
    }

    public RequirementType getRequirementType() {
        return requirementType;
    }

    public String getFreeTextNote() {
        return freeTextNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
