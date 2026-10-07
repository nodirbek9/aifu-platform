package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.util.Guards;

/** An academic group ("guruh"), e.g. {@code BK-DI26/1}. Students belong to it by its code. */
@Entity
@Table(name = "student_groups")
public class StudentGroup extends VersionedEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "programme_id")
    private UUID programmeId;

    @Column(name = "course_year")
    private Integer courseYear;

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "study_form", nullable = false)
    private String studyForm = "FULL_TIME";

    @Column(name = "instruction_language", nullable = false)
    private String instructionLanguage = "uz";

    @Column(name = "planned_size")
    private Integer plannedSize;

    @Column(name = "curator_user_id")
    private UUID curatorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false)
    private Shift shift = Shift.MORNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupStatus status = GroupStatus.ACTIVE;

    protected StudentGroup() {}

    public StudentGroup(String code) {
        this.code = Guards.requiredText(code, "code").trim();
    }

    public void update(
            String code,
            UUID programmeId,
            Integer courseYear,
            Integer admissionYear,
            String studyForm,
            String instructionLanguage,
            Integer plannedSize,
            UUID curatorUserId,
            Shift shift) {
        this.code = Guards.requiredText(code, "code").trim();
        this.programmeId = programmeId;
        this.courseYear = courseYear;
        this.admissionYear = admissionYear;
        if (studyForm != null) {
            this.studyForm = studyForm;
        }
        if (instructionLanguage != null) {
            this.instructionLanguage = instructionLanguage;
        }
        this.plannedSize = plannedSize;
        this.curatorUserId = curatorUserId;
        if (shift != null) {
            this.shift = shift;
        }
    }

    public void archive() {
        this.status = GroupStatus.ARCHIVED;
    }

    public void activate() {
        this.status = GroupStatus.ACTIVE;
    }

    public String getCode() {
        return code;
    }

    public UUID getProgrammeId() {
        return programmeId;
    }

    public Integer getCourseYear() {
        return courseYear;
    }

    public Integer getAdmissionYear() {
        return admissionYear;
    }

    public String getStudyForm() {
        return studyForm;
    }

    public String getInstructionLanguage() {
        return instructionLanguage;
    }

    public Integer getPlannedSize() {
        return plannedSize;
    }

    public UUID getCuratorUserId() {
        return curatorUserId;
    }

    public Shift getShift() {
        return shift;
    }

    public GroupStatus getStatus() {
        return status;
    }
}
