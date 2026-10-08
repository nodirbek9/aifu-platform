package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.iam.domain.User;

/**
 * Append-only allowance of extra quiz attempts granted by admins (V032, brief Phase 5 B1-B2). Not a
 * BaseEntity because table has no updated_at column.
 */
@Entity
@Table(name = "quiz_attempt_grants")
public class QuizAttemptGrant {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false, updatable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "granted_by", nullable = false, updatable = false)
    private User grantedBy;

    @Column(name = "reason", nullable = false, updatable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected QuizAttemptGrant() {
        // JPA
    }

    public QuizAttemptGrant(Quiz quiz, Enrollment enrollment, User grantedBy, String reason) {
        this.quiz = Guards.required(quiz, "quiz");
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.grantedBy = Guards.required(grantedBy, "grantedBy");
        this.reason = Guards.requiredText(reason, "reason");
    }

    public UUID getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public User getGrantedBy() {
        return grantedBy;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        Class<?> thisType = effectiveClass(this);
        Class<?> otherType = effectiveClass(other);
        if (!thisType.equals(otherType)) {
            return false;
        }
        UUID otherId = ((QuizAttemptGrant) other).getId();
        return id != null && id.equals(otherId);
    }

    @Override
    public final int hashCode() {
        return effectiveClass(this).hashCode();
    }

    private static Class<?> effectiveClass(Object candidate) {
        return candidate instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : candidate.getClass();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id="
                + id
                + ", quiz="
                + (quiz != null ? quiz.getId() : null)
                + ", enrollment="
                + (enrollment != null ? enrollment.getId() : null)
                + "}";
    }
}
