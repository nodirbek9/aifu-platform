package uz.aifu.lms.enrollment.app;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.NotificationRequestedEvent;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentEvent;
import uz.aifu.lms.enrollment.domain.EnrollmentEventType;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;
import uz.aifu.lms.enrollment.persistence.EnrollmentEventRepository;
import uz.aifu.lms.enrollment.persistence.EnrollmentRepository;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;

/**
 * Enrollment lifecycle (docs/domain/domain-model.md §4, Phase 4). "Explicit enrolment with
 * preserved history and no duplicates under concurrency" — the app-level pre-check below gives a
 * fast, friendly error in the common case; {@code uq_enrollment_active} (V006) is the real
 * guarantee two concurrent requests cannot both win (see {@link #enroll}).
 */
@Service
public class EnrollmentService {

    private static final Set<EnrollmentStatus> NON_TERMINAL =
            EnumSet.of(
                    EnrollmentStatus.PENDING, EnrollmentStatus.ACTIVE, EnrollmentStatus.SUSPENDED);

    private final EnrollmentRepository enrollments;
    private final EnrollmentEventRepository events;
    private final CourseService courses;
    private final UserService users;
    private final AuditService audit;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final EnrollmentService self;

    public EnrollmentService(
            EnrollmentRepository enrollments,
            EnrollmentEventRepository events,
            CourseService courses,
            UserService users,
            AuditService audit,
            ApplicationEventPublisher eventPublisher,
            Clock clock,
            @Lazy EnrollmentService self) {
        this.enrollments = enrollments;
        this.events = events;
        this.courses = courses;
        this.users = users;
        this.audit = audit;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.self = self;
    }

    @Transactional(readOnly = true)
    public Enrollment require(UUID id) {
        return enrollments
                .findWithDetailsById(id)
                .orElseThrow(() -> Exceptions.notFound("Enrollment", id));
    }

    @Transactional(readOnly = true)
    public Page<Enrollment> listByCourse(
            UUID courseId, Collection<EnrollmentStatus> statuses, Pageable pageable) {
        return statuses == null || statuses.isEmpty()
                ? enrollments.findByCourse_Id(courseId, pageable)
                : enrollments.findByCourse_IdAndStatusIn(courseId, statuses, pageable);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listActiveByCourse(UUID courseId) {
        return enrollments.findByCourse_IdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Page<Enrollment> listByStudent(UUID studentUserId, Pageable pageable) {
        return enrollments.findByStudent_Id(studentUserId, pageable);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentEvent> listEvents(UUID enrollmentId) {
        return events.findByEnrollment_IdOrderByOccurredAtAsc(enrollmentId);
    }

    /**
     * The caller's own ACTIVE enrollment in a course — the ownership check {@code progress} (Phase
     * 6) uses to accept a lesson-progress write, without needing {@code AccessGuard} (ADR-012):
     * scoped entirely by the caller's own id, never load-then-check.
     */
    @Transactional(readOnly = true)
    public Enrollment requireActiveEnrollment(UUID courseId, UUID studentUserId) {
        return enrollments
                .findFirstByCourse_IdAndStudent_IdAndStatusIn(
                        courseId, studentUserId, EnumSet.of(EnrollmentStatus.ACTIVE))
                .orElseThrow(() -> Exceptions.notEnrolled(courseId));
    }

    /**
     * The caller's most recent enrollment (any status) in a course — backs {@code GET
     * /courses/{id}/progress/me}, matching {@code AccessGuard}'s own "completed/withdrawn students
     * keep read access to their own history" rule, without depending on {@code iam}.
     */
    @Transactional(readOnly = true)
    public Enrollment requireLatestEnrollment(UUID courseId, UUID studentUserId) {
        return enrollments
                .findFirstByCourse_IdAndStudent_IdOrderByAttemptNoDesc(courseId, studentUserId)
                .orElseThrow(() -> Exceptions.notEnrolled(courseId));
    }

    /**
     * A plain {@code UUID}, never the {@code academics.domain.Course} entity itself: {@code
     * progress} may depend on {@code enrollment} but not on {@code academics}
     * (ArchitectureBoundaryTest), so this is how it learns which course an enrollment belongs to.
     */
    @Transactional(readOnly = true)
    public UUID courseIdOf(UUID enrollmentId) {
        return enrollments
                .findCourseIdById(enrollmentId)
                .orElseThrow(() -> Exceptions.notFound("Enrollment", enrollmentId));
    }

    /**
     * The write path for {@code grading.app.GradeEntryService}'s recomputed snapshot (ADR-011) —
     * {@code grading} may depend on {@code enrollment}, but a cross-module write must go through
     * the owning module's own application service, never a stray mutation on an entity obtained
     * from a read call (CLAUDE.md §2).
     */
    @Transactional
    public void recordFinalGrade(
            UUID enrollmentId,
            BigDecimal finalScore,
            String finalGradeSymbol,
            boolean admittedToFinal,
            boolean academicDebt) {
        Enrollment enrollment = require(enrollmentId);
        enrollment.recordFinalGrade(
                finalScore, finalGradeSymbol, admittedToFinal, academicDebt, clock.instant());
    }

    @Transactional
    public Enrollment enroll(
            UUID courseId, UUID studentUserId, EnrollmentSource source, UUID actorId, String ip) {
        Course course = courses.require(courseId);
        requireCourseAcceptsEnrollment(course);
        User student = users.require(studentUserId);
        requireStudentRole(student);

        if (enrollments.existsByCourse_IdAndStudent_IdAndStatusIn(
                courseId, studentUserId, NON_TERMINAL)) {
            throw duplicateEnrollment();
        }
        requireCapacity(course);

        int attemptNo = enrollments.findMaxAttemptNo(courseId, studentUserId) + 1;
        Instant now = clock.instant();
        Enrollment enrollment = new Enrollment(course, student, attemptNo, source, now);
        Enrollment saved;
        try {
            // saveAndFlush, not save: forces the INSERT now so a real concurrent-request race
            // (against uq_enrollment_active, or uq_enrollment_attempt when both racers computed
            // the same attemptNo) surfaces here as a catchable exception, not later at an
            // uncontrolled point during the transaction's eventual commit-time flush.
            saved = enrollments.saveAndFlush(enrollment);
        } catch (DataIntegrityViolationException e) {
            throw duplicateEnrollment();
        }
        recordEvent(
                saved,
                EnrollmentEventType.CREATED,
                null,
                EnrollmentStatus.ACTIVE,
                actorId,
                null,
                now);
        audit.record(
                AuditAction.ENROLLMENT_CREATED,
                actorId,
                "enrollment",
                saved.getId(),
                ip,
                null,
                Map.of(
                        "courseId",
                        courseId.toString(),
                        "studentUserId",
                        studentUserId.toString(),
                        "attemptNo",
                        attemptNo));
        eventPublisher.publishEvent(
                new NotificationRequestedEvent(
                        studentUserId,
                        "ENROLLMENT_CREATED",
                        Map.of(
                                "courseId",
                                courseId.toString(),
                                "enrollmentId",
                                saved.getId().toString()),
                        "enrollment-created:" + saved.getId(),
                        now));
        return saved;
    }

    /**
     * Each student is its own transaction (via {@code self}, the proxied bean — a direct {@code
     * this.enroll(...)} call would bypass Spring's transactional proxy entirely) so one student
     * already being enrolled does not void the rest of the batch.
     */
    public List<BulkEnrollOutcome> bulkEnroll(
            UUID courseId,
            List<UUID> studentUserIds,
            EnrollmentSource source,
            UUID actorId,
            String ip) {
        List<BulkEnrollOutcome> outcomes = new ArrayList<>();
        for (UUID studentUserId : studentUserIds) {
            try {
                Enrollment saved = self.enroll(courseId, studentUserId, source, actorId, ip);
                outcomes.add(BulkEnrollOutcome.success(studentUserId, saved.getId()));
            } catch (DomainException e) {
                outcomes.add(
                        BulkEnrollOutcome.failure(
                                studentUserId, e.errorCode().name(), e.getMessage()));
            }
        }
        return outcomes;
    }

    /**
     * Registrar CSV roster import (Phase 10) — one student number per row. Same per-row isolation
     * as {@link #bulkEnroll} (via {@code self}, no ambient {@code @Transactional} here) plus one
     * extra failure mode: a student number that resolves to nothing is reported, never thrown,
     * exactly like every other per-row outcome.
     */
    public List<CsvEnrollOutcome> importByStudentNumbers(
            UUID courseId,
            List<String> studentNumbers,
            EnrollmentSource source,
            UUID actorId,
            String ip) {
        List<CsvEnrollOutcome> outcomes = new ArrayList<>();
        for (String raw : studentNumbers) {
            String identifier = raw == null ? "" : raw.trim();
            if (identifier.isEmpty()) {
                continue;
            }
            Optional<UUID> studentUserId = users.findUserIdByStudentNumber(identifier);
            if (studentUserId.isEmpty()) {
                outcomes.add(
                        CsvEnrollOutcome.failure(
                                identifier,
                                null,
                                ErrorCode.RESOURCE_NOT_FOUND.name(),
                                "No student with this student number"));
                continue;
            }
            try {
                Enrollment saved = self.enroll(courseId, studentUserId.get(), source, actorId, ip);
                outcomes.add(
                        CsvEnrollOutcome.success(identifier, studentUserId.get(), saved.getId()));
            } catch (DomainException e) {
                outcomes.add(
                        CsvEnrollOutcome.failure(
                                identifier,
                                studentUserId.get(),
                                e.errorCode().name(),
                                e.getMessage()));
            }
        }
        return outcomes;
    }

    @Transactional
    public Enrollment activate(UUID id, UUID actorId, String ip, String reason) {
        return transition(
                id, Enrollment::activate, EnrollmentEventType.ACTIVATED, actorId, ip, reason);
    }

    @Transactional
    public Enrollment suspend(UUID id, UUID actorId, String ip, String reason) {
        return transition(
                id, Enrollment::suspend, EnrollmentEventType.SUSPENDED, actorId, ip, reason);
    }

    @Transactional
    public Enrollment resume(UUID id, UUID actorId, String ip, String reason) {
        return transition(id, Enrollment::resume, EnrollmentEventType.RESUMED, actorId, ip, reason);
    }

    @Transactional
    public Enrollment complete(UUID id, UUID actorId, String ip, String reason) {
        return transition(
                id,
                e -> e.complete(clock.instant()),
                EnrollmentEventType.COMPLETED,
                actorId,
                ip,
                reason);
    }

    @Transactional
    public Enrollment fail(UUID id, UUID actorId, String ip, String reason) {
        return transition(id, Enrollment::fail, EnrollmentEventType.FAILED, actorId, ip, reason);
    }

    @Transactional
    public Enrollment withdraw(UUID id, UUID actorId, String ip, String reason) {
        return transition(
                id,
                e -> e.withdraw(clock.instant()),
                EnrollmentEventType.WITHDRAWN,
                actorId,
                ip,
                reason);
    }

    /**
     * Admin correction of a mistaken WITHDRAWN/FAILED transition. See {@link
     * Enrollment#reinstate()}.
     */
    @Transactional
    public Enrollment reinstate(UUID id, UUID actorId, String ip, String reason) {
        return transition(
                id, Enrollment::reinstate, EnrollmentEventType.REINSTATED, actorId, ip, reason);
    }

    private Enrollment transition(
            UUID id,
            Consumer<Enrollment> mutator,
            EnrollmentEventType eventType,
            UUID actorId,
            String ip,
            String reason) {
        Enrollment enrollment = require(id);
        EnrollmentStatus from = enrollment.getStatus();
        mutator.accept(enrollment);
        EnrollmentStatus to = enrollment.getStatus();
        recordEvent(enrollment, eventType, from, to, actorId, reason, clock.instant());
        AuditAction action =
                eventType == EnrollmentEventType.WITHDRAWN
                        ? AuditAction.ENROLLMENT_WITHDRAWN
                        : AuditAction.ENROLLMENT_STATUS_CHANGED;
        audit.record(
                action,
                actorId,
                "enrollment",
                id,
                ip,
                null,
                Map.of("from", from.name(), "to", to.name()));
        return enrollment;
    }

    private void recordEvent(
            Enrollment enrollment,
            EnrollmentEventType type,
            EnrollmentStatus from,
            EnrollmentStatus to,
            UUID actorId,
            String reason,
            Instant now) {
        events.save(new EnrollmentEvent(enrollment, type, from, to, actorId, reason, now));
    }

    private void requireCourseAcceptsEnrollment(Course course) {
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw Exceptions.conflict(
                    ErrorCode.ENROLLMENT_CLOSED,
                    "Course is not open for enrollment (status=" + course.getStatus() + ")");
        }
        Instant now = clock.instant();
        if (course.getEnrollmentOpensAt() != null && now.isBefore(course.getEnrollmentOpensAt())) {
            throw Exceptions.conflict(ErrorCode.ENROLLMENT_CLOSED, "Enrollment has not opened yet");
        }
        if (course.getEnrollmentClosesAt() != null && now.isAfter(course.getEnrollmentClosesAt())) {
            throw Exceptions.conflict(ErrorCode.ENROLLMENT_CLOSED, "Enrollment window has closed");
        }
    }

    private void requireCapacity(Course course) {
        Integer max = course.getMaxEnrollment();
        if (max == null) {
            return;
        }
        long active = enrollments.countByCourse_IdAndStatusIn(course.getId(), NON_TERMINAL);
        if (active >= max) {
            throw Exceptions.conflict(
                    ErrorCode.ENROLLMENT_CLOSED, "Course has reached its enrollment capacity");
        }
    }

    private void requireStudentRole(User student) {
        boolean isStudent =
                student.getRoles().stream().anyMatch(r -> RoleCode.STUDENT.equals(r.getCode()));
        if (!isStudent) {
            throw Exceptions.validation(
                    "User " + student.getId() + " does not have the STUDENT role");
        }
    }

    private static DomainException duplicateEnrollment() {
        return Exceptions.conflict(
                ErrorCode.ENROLLMENT_DUPLICATE,
                "Student already has a non-terminal enrollment in this course");
    }
}
