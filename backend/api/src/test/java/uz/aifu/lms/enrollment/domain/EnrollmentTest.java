package uz.aifu.lms.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.domain.Department;
import uz.aifu.lms.iam.domain.User;

/**
 * The state transition matrix (docs/domain/domain-model.md §4): valid moves succeed, everything
 * else raises {@link ErrorCode#ENROLLMENT_INVALID_TRANSITION}.
 */
class EnrollmentTest {

    private static final Instant NOW = Instant.parse("2026-01-15T00:00:00Z");

    @Test
    void newEnrollment_startsActive() {
        Enrollment enrollment = enrollment();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getAttemptNo()).isEqualTo(1);
        assertThat(enrollment.getEnrolledAt()).isEqualTo(NOW);
    }

    @Test
    void activate_fromActive_isRejected() {
        // No Phase 4 path ever constructs a PENDING row (see EnrollmentStatus.PENDING); the only
        // reachable state to exercise activate()'s guard against is the constructor's ACTIVE.
        Enrollment enrollment = enrollment();

        assertInvalidTransition(enrollment::activate);
    }

    @Test
    void suspend_thenResume_returnsToActive() {
        Enrollment enrollment = enrollment();

        enrollment.suspend();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.SUSPENDED);

        enrollment.resume();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void suspend_fromNonActive_isRejected() {
        Enrollment enrollment = enrollment();
        enrollment.withdraw(NOW);

        assertInvalidTransition(enrollment::suspend);
    }

    @Test
    void resume_fromNonSuspended_isRejected() {
        Enrollment enrollment = enrollment();

        assertInvalidTransition(enrollment::resume);
    }

    @Test
    void complete_fromActive_setsCompletedAt() {
        Enrollment enrollment = enrollment();

        enrollment.complete(NOW);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getCompletedAt()).isEqualTo(NOW);
    }

    @Test
    void complete_fromSuspended_isRejected() {
        Enrollment enrollment = enrollment();
        enrollment.suspend();

        assertInvalidTransition(() -> enrollment.complete(NOW));
    }

    @Test
    void fail_fromActive_succeeds() {
        Enrollment enrollment = enrollment();

        enrollment.fail();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.FAILED);
    }

    @Test
    void withdraw_fromActiveOrSuspended_succeeds() {
        Enrollment active = enrollment();
        active.withdraw(NOW);
        assertThat(active.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
        assertThat(active.getWithdrawnAt()).isEqualTo(NOW);

        Enrollment suspended = enrollment();
        suspended.suspend();
        suspended.withdraw(NOW);
        assertThat(suspended.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
    }

    @Test
    void withdraw_fromTerminal_isRejected() {
        Enrollment enrollment = enrollment();
        enrollment.complete(NOW);

        assertInvalidTransition(() -> enrollment.withdraw(NOW));
    }

    @Test
    void reinstate_fromWithdrawn_returnsToActiveAndClearsWithdrawnAt() {
        Enrollment enrollment = enrollment();
        enrollment.withdraw(NOW);

        enrollment.reinstate();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getWithdrawnAt()).isNull();
    }

    @Test
    void reinstate_fromFailed_returnsToActive() {
        Enrollment enrollment = enrollment();
        enrollment.fail();

        enrollment.reinstate();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void reinstate_fromCompleted_isRejected() {
        // ADR-011: COMPLETED implies grade finalization and stays genuinely terminal.
        Enrollment enrollment = enrollment();
        enrollment.complete(NOW);

        assertInvalidTransition(enrollment::reinstate);
    }

    @Test
    void reinstate_fromActive_isRejected() {
        Enrollment enrollment = enrollment();

        assertInvalidTransition(enrollment::reinstate);
    }

    private static void assertInvalidTransition(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.ENROLLMENT_INVALID_TRANSITION);
    }

    private static Enrollment enrollment() {
        return new Enrollment(course(), student(), 1, EnrollmentSource.ADMIN, NOW);
    }

    private static Course course() {
        Department department = new Department("AF", "Aniq fanlar", "Exact Sciences");
        Subject subject = new Subject("DAS1110", department, SubjectKind.MANDATORY, LocaleCode.UZ);
        AcademicYear academicYear =
                new AcademicYear(
                        "2025/2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30), true);
        Term term =
                new Term(
                        academicYear,
                        1,
                        "Semester 1",
                        LocalDate.of(2025, 9, 1),
                        LocalDate.of(2026, 1, 15),
                        true);
        return new Course(
                subject, term, "DEFAULT", BigDecimal.valueOf(6), 180, LocaleCode.UZ, null);
    }

    private static User student() {
        return new User(
                "student@test.aifu.local", "student1", "hash", "Test Student", LocaleCode.UZ);
    }
}
