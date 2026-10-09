package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/**
 * Read-only mapping of {@code v_admin_problems} (V035). Discovers six administrative problems
 * across platform resources (docs/PLAN.md §5, Phase 7.4). Never written from Java; the view IS the
 * query.
 */
@Entity
@Immutable
@Table(name = "v_admin_problems")
public class AdminProblemView {

    public enum AdminProblemType {
        STUDENT_WITHOUT_GROUP,
        GROUP_WITHOUT_PUBLISHED_ENTRIES,
        COURSE_WITHOUT_TEACHER,
        ACTIVE_ENROLLMENT_INACTIVE_USER,
        LOCKED_ACCOUNT,
        MIDTERM_ATTEMPTS_EXHAUSTED
    }

    @Id
    @Column(name = "problem_key")
    private String problemKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AdminProblemType type;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "group_code")
    private String groupCode;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "cohort_code")
    private String cohortCode;

    @Column(name = "term_name")
    private String termName;

    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Column(name = "quiz_id")
    private UUID quizId;

    @Column(name = "attempts_used")
    private Integer attemptsUsed;

    @Column(name = "attempts_allowed")
    private Integer attemptsAllowed;

    @Column(name = "grant_available")
    private Boolean grantAvailable;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    protected AdminProblemView() {}

    public String getProblemKey() {
        return problemKey;
    }

    public AdminProblemType getType() {
        return type;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getCohortCode() {
        return cohortCode;
    }

    public String getTermName() {
        return termName;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public Integer getAttemptsUsed() {
        return attemptsUsed;
    }

    public Integer getAttemptsAllowed() {
        return attemptsAllowed;
    }

    public Boolean getGrantAvailable() {
        return grantAvailable;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdminProblemView that)) {
            return false;
        }
        return Objects.equals(problemKey, that.problemKey);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
