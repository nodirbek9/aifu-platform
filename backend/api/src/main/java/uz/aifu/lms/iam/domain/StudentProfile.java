package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** Student-specific attributes. Shares the primary key with {@link User}. */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "student_number", nullable = false, unique = true)
    private String studentNumber;

    @Column(name = "programme_id")
    private UUID programmeId;

    @Column(name = "group_code")
    private String groupCode;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "study_form", nullable = false)
    private String studyForm = "FULL_TIME";

    protected StudentProfile() {}

    public StudentProfile(
            User user, String studentNumber, UUID programmeId, String groupCode, Integer year) {
        this.user = user;
        this.studentNumber = studentNumber;
        this.programmeId = programmeId;
        this.groupCode = groupCode;
        this.enrollmentYear = year;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public UUID getProgrammeId() {
        return programmeId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }

    public String getStudyForm() {
        return studyForm;
    }

    public User getUser() {
        return user;
    }

    /** Moves the student to another group ({@code null} = no group). The code must exist. */
    public void assignGroup(String groupCode) {
        this.groupCode = (groupCode == null || groupCode.isBlank()) ? null : groupCode.trim();
    }
}
