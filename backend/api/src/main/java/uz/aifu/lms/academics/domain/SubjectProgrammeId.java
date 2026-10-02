package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubjectProgrammeId implements Serializable {

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "programme_id")
    private UUID programmeId;

    protected SubjectProgrammeId() {}

    public SubjectProgrammeId(UUID subjectId, UUID programmeId) {
        this.subjectId = subjectId;
        this.programmeId = programmeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubjectProgrammeId that)) {
            return false;
        }
        return Objects.equals(subjectId, that.subjectId)
                && Objects.equals(programmeId, that.programmeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId, programmeId);
    }
}
