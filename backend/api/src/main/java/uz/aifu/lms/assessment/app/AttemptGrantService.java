package uz.aifu.lms.assessment.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.domain.QuizAttemptGrant;
import uz.aifu.lms.assessment.persistence.QuizAttemptGrantRepository;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.iam.domain.User;

@Service
public class AttemptGrantService {

    private final QuizAttemptGrantRepository grants;
    private final QuizService quizzes;
    private final AttemptService attemptService;
    private final EnrollmentService enrollments;
    private final EntityManager entityManager;
    private final AuditService auditService;

    public AttemptGrantService(
            QuizAttemptGrantRepository grants,
            QuizService quizzes,
            AttemptService attemptService,
            EnrollmentService enrollments,
            EntityManager entityManager,
            AuditService auditService) {
        this.grants = grants;
        this.quizzes = quizzes;
        this.attemptService = attemptService;
        this.enrollments = enrollments;
        this.entityManager = entityManager;
        this.auditService = auditService;
    }

    @Transactional
    public QuizAttemptGrant grant(UUID quizId, UUID enrollmentId, String reason, UUID adminUserId) {
        if (reason == null || reason.trim().length() < 5) {
            throw Exceptions.validation("Reason must be at least 5 characters long");
        }
        String cleanReason = reason.trim();

        Quiz quiz = quizzes.require(quizId);
        if (quiz.getMaxAttempts() == null) {
            throw Exceptions.validation("Cannot grant attempts: quiz has no attempt limit");
        }

        Enrollment enrollment = enrollments.require(enrollmentId);
        if (!quiz.getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw Exceptions.validation("Enrollment and quiz belong to different courses");
        }

        Query lockQuery = entityManager.createNativeQuery("SELECT quiz_attempt_lock(:quiz, :enr)");
        lockQuery.setParameter("quiz", quizId);
        lockQuery.setParameter("enr", enrollmentId);
        lockQuery.getSingleResult();

        int used = attemptService.maxAttemptNumber(quizId, enrollmentId);
        long existingGrants = grants.countByQuiz_IdAndEnrollment_Id(quizId, enrollmentId);

        // A new grant is possible only once the whole allowance (earlier grants included) is
        // used, so at most one grant is ever unused.
        long allowance = quiz.getMaxAttempts() + existingGrants;

        if (used < allowance) {
            throw Exceptions.validation("Student still has attempts remaining");
        }

        User admin = entityManager.getReference(User.class, adminUserId);
        QuizAttemptGrant grant = new QuizAttemptGrant(quiz, enrollment, admin, cleanReason);
        grant = grants.save(grant);

        auditService.record(
                AuditAction.QUIZ_ATTEMPT_GRANTED,
                adminUserId,
                "Quiz",
                quizId,
                null,
                null,
                Map.of("enrollmentId", enrollmentId, "reason", cleanReason));

        return grant;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<QuizAttemptGrant> list(
            UUID courseId, org.springframework.data.domain.Pageable pageable) {
        return courseId == null
                ? grants.findAll(pageable)
                : grants.findByQuiz_Course_Id(courseId, pageable);
    }
}
