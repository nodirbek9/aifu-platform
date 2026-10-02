package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.iam.domain.AcademicProgramme;

/**
 * One delivery of a {@link Subject} in one {@link Term}. Credits/hours live HERE, not on the
 * subject: {@code DAS1110} is 6 credits/180h in semester 1 and 5 credits/150h in semester 2.
 */
@Entity
@Table(name = "courses")
public class Course extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false, updatable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false, updatable = false)
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id")
    private AcademicProgramme programme;

    @Column(name = "cohort_code", nullable = false)
    private String cohortCode = "DEFAULT";

    @Column(name = "credits", nullable = false)
    private BigDecimal credits;

    @Column(name = "total_hours", nullable = false)
    private int totalHours;

    @Column(name = "weekly_contact_hours")
    private BigDecimal weeklyContactHours;

    @Column(name = "instruction_language", nullable = false)
    private String instructionLanguage = LocaleCode.DEFAULT.code();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "required_locales", nullable = false)
    private String[] requiredLocales = new String[] {LocaleCode.DEFAULT.code()};

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode", nullable = false)
    private DeliveryMode deliveryMode = DeliveryMode.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_form")
    private CourseFinalAssessmentForm assessmentForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_scale_id")
    private GradeScale gradeScale;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "enrollment_opens_at")
    private Instant enrollmentOpensAt;

    @Column(name = "enrollment_closes_at")
    private Instant enrollmentClosesAt;

    @Column(name = "max_enrollment")
    private Integer maxEnrollment;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Course() {}

    public Course(
            Subject subject,
            Term term,
            String cohortCode,
            BigDecimal credits,
            int totalHours,
            LocaleCode instructionLanguage,
            GradeScale gradeScale) {
        this.subject = Guards.required(subject, "subject");
        this.term = Guards.required(term, "term");
        this.cohortCode =
                cohortCode == null || cohortCode.isBlank() ? "DEFAULT" : cohortCode.trim();
        setCreditsAndHours(credits, totalHours);
        this.instructionLanguage =
                (instructionLanguage == null ? LocaleCode.DEFAULT : instructionLanguage).code();
        this.requiredLocales = new String[] {this.instructionLanguage};
        this.gradeScale = gradeScale;
    }

    public void setCreditsAndHours(BigDecimal credits, int totalHours) {
        if (credits == null
                || credits.signum() <= 0
                || credits.compareTo(BigDecimal.valueOf(60)) > 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "credits must be > 0 and <= 60");
        }
        if (totalHours <= 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "totalHours must be > 0");
        }
        this.credits = credits;
        this.totalHours = totalHours;
    }

    public void updateDeliveryDetails(
            String cohortCode,
            BigDecimal weeklyContactHours,
            DeliveryMode deliveryMode,
            CourseFinalAssessmentForm assessmentForm,
            AcademicProgramme programme) {
        if (cohortCode != null && !cohortCode.isBlank()) {
            this.cohortCode = cohortCode.trim();
        }
        this.weeklyContactHours = weeklyContactHours;
        this.deliveryMode = deliveryMode == null ? DeliveryMode.FULL_TIME : deliveryMode;
        this.assessmentForm = assessmentForm;
        this.programme = programme;
    }

    public void updateRequiredLocales(String[] requiredLocales) {
        if (requiredLocales == null || requiredLocales.length == 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "requiredLocales must not be empty");
        }
        this.requiredLocales = requiredLocales.clone();
    }

    public void updateEnrollmentWindow(Instant opensAt, Instant closesAt, Integer maxEnrollment) {
        if (opensAt != null && closesAt != null && !closesAt.isAfter(opensAt)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "enrollmentClosesAt must be after enrollmentOpensAt");
        }
        this.enrollmentOpensAt = opensAt;
        this.enrollmentClosesAt = closesAt;
        this.maxEnrollment = maxEnrollment;
    }

    public void publish(Instant now) {
        requireTransition(CourseStatus.PUBLISHED);
        this.status = CourseStatus.PUBLISHED;
        this.publishedAt = now;
    }

    public void close() {
        requireTransition(CourseStatus.CLOSED);
        this.status = CourseStatus.CLOSED;
    }

    public void archive() {
        requireTransition(CourseStatus.ARCHIVED);
        this.status = CourseStatus.ARCHIVED;
    }

    public void returnToDraft() {
        requireTransition(CourseStatus.DRAFT);
        this.status = CourseStatus.DRAFT;
    }

    private void requireTransition(CourseStatus target) {
        boolean allowed =
                switch (status) {
                    case DRAFT ->
                            target == CourseStatus.PUBLISHED || target == CourseStatus.ARCHIVED;
                    case PUBLISHED ->
                            target == CourseStatus.CLOSED
                                    || target == CourseStatus.ARCHIVED
                                    || target == CourseStatus.DRAFT;
                    case CLOSED -> target == CourseStatus.ARCHIVED;
                    case ARCHIVED -> target == CourseStatus.DRAFT;
                };
        if (!allowed) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Cannot move a course from " + status + " to " + target);
        }
    }

    public void softDelete(Instant now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Subject getSubject() {
        return subject;
    }

    public Term getTerm() {
        return term;
    }

    public AcademicProgramme getProgramme() {
        return programme;
    }

    public String getCohortCode() {
        return cohortCode;
    }

    public BigDecimal getCredits() {
        return credits;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public BigDecimal getWeeklyContactHours() {
        return weeklyContactHours;
    }

    public LocaleCode getInstructionLanguage() {
        return LocaleCode.find(instructionLanguage).orElse(LocaleCode.DEFAULT);
    }

    public String[] getRequiredLocales() {
        return requiredLocales.clone();
    }

    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    public CourseFinalAssessmentForm getAssessmentForm() {
        return assessmentForm;
    }

    public GradeScale getGradeScale() {
        return gradeScale;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public Instant getEnrollmentOpensAt() {
        return enrollmentOpensAt;
    }

    public Instant getEnrollmentClosesAt() {
        return enrollmentClosesAt;
    }

    public Integer getMaxEnrollment() {
        return maxEnrollment;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
