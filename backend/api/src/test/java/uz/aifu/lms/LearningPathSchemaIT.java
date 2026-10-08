package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Integration tests verifying the DB triggers, constraints and partial unique indexes introduced in
 * V032 (Phase 5 Learning Path).
 */
@Tag("it")
class LearningPathSchemaIT extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;

    @Test
    void quizAttemptTrigger_rejectsThirdAttemptWhenMaxTwo_andAcceptsAfterGrantRow() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        Course course = enrollmentFixtures.publishedCourse(150);
        Enrollment enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);

        UUID quizId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO quizzes (
                    id, course_id, code, max_attempts, max_points, passing_percent,
                    status, shuffle_questions, shuffle_options, reveal_policy,
                    created_at, updated_at, version
                ) VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)
                """,
                quizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));

        // Attempt 1: succeeds
        jdbc.update(
                """
                INSERT INTO quiz_attempts (
                    id, quiz_id, enrollment_id, attempt_number, status, started_at, submitted_at, version
                ) VALUES (?, ?, ?, 1, 'GRADED', now(), now(), 0)
                """,
                UUID.randomUUID(),
                quizId,
                enrollment.getId());

        // Attempt 2: succeeds
        jdbc.update(
                """
                INSERT INTO quiz_attempts (
                    id, quiz_id, enrollment_id, attempt_number, status, started_at, submitted_at, version
                ) VALUES (?, ?, ?, 2, 'GRADED', now(), now(), 0)
                """,
                UUID.randomUUID(),
                quizId,
                enrollment.getId());

        // Attempt 3: rejected by trigger because max_attempts = 2 and grants = 0
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        """
                INSERT INTO quiz_attempts (
                    id, quiz_id, enrollment_id, attempt_number, status, started_at, submitted_at, version
                ) VALUES (?, ?, ?, 3, 'GRADED', now(), now(), 0)
                """,
                                        UUID.randomUUID(),
                                        quizId,
                                        enrollment.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Admin inserts grant row
        UUID grantId = UUID.randomUUID();
        int grantInserted =
                jdbc.update(
                        """
                INSERT INTO quiz_attempt_grants (
                    id, quiz_id, enrollment_id, granted_by, reason, created_at
                ) VALUES (?, ?, ?, ?, 'Approved by academic council', now())
                """,
                        grantId,
                        quizId,
                        enrollment.getId(),
                        admin.getId());
        assertThat(grantInserted).isEqualTo(1);

        // Attempt 3: now succeeds with grant!
        int attempt3Inserted =
                jdbc.update(
                        """
                INSERT INTO quiz_attempts (
                    id, quiz_id, enrollment_id, attempt_number, status, started_at, submitted_at, version
                ) VALUES (?, ?, ?, 3, 'GRADED', now(), now(), 0)
                """,
                        UUID.randomUUID(),
                        quizId,
                        enrollment.getId());
        assertThat(attempt3Inserted).isEqualTo(1);

        // Attempt 4: rejected because max_attempts(2) + grants(1) = 3
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        """
                INSERT INTO quiz_attempts (
                    id, quiz_id, enrollment_id, attempt_number, status, started_at, submitted_at, version
                ) VALUES (?, ?, ?, 4, 'GRADED', now(), now(), 0)
                """,
                                        UUID.randomUUID(),
                                        quizId,
                                        enrollment.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void quizAttemptGrants_cannotBeUpdatedOrDeleted() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        Course course = enrollmentFixtures.publishedCourse(150);
        Enrollment enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);

        UUID quizId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO quizzes (
                    id, course_id, code, max_attempts, max_points, passing_percent,
                    status, shuffle_questions, shuffle_options, reveal_policy,
                    created_at, updated_at, version
                ) VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)
                """,
                quizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));

        UUID grantId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO quiz_attempt_grants (
                    id, quiz_id, enrollment_id, granted_by, reason, created_at
                ) VALUES (?, ?, ?, ?, 'Initial grant reason', now())
                """,
                grantId,
                quizId,
                enrollment.getId(),
                admin.getId());

        // Update rejected by trigger
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "UPDATE quiz_attempt_grants SET reason = 'Modified reason' WHERE id = ?",
                                        grantId))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Delete rejected by trigger
        assertThatThrownBy(
                        () -> jdbc.update("DELETE FROM quiz_attempt_grants WHERE id = ?", grantId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void partialUniqueIndex_rejectsSecondTestActivityOnSameLesson() {
        Course course = enrollmentFixtures.publishedCourse(150);

        UUID moduleId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO course_modules (
                    id, course_id, position, status, created_at, updated_at, version
                ) VALUES (?, ?, 0, 'PUBLISHED', now(), now(), 0)
                """,
                moduleId,
                course.getId());

        UUID lessonId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO lessons (
                    id, module_id, position, session_type, status, is_required,
                    created_at, updated_at, version
                ) VALUES (?, ?, 0, 'LECTURE', 'PUBLISHED', true, now(), now(), 0)
                """,
                lessonId,
                moduleId);

        // First TEST activity on lesson succeeds
        UUID act1 = UUID.randomUUID();
        int firstInserted =
                jdbc.update(
                        """
                INSERT INTO activities (
                    id, course_id, module_id, lesson_id, position, activity_type,
                    status, is_graded, collaboration_mode, variant_strategy, path_step,
                    created_at, updated_at, version
                ) VALUES (?, ?, ?, ?, 0, 'QUIZ', 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'TEST', now(), now(), 0)
                """,
                        act1,
                        course.getId(),
                        moduleId,
                        lessonId);
        assertThat(firstInserted).isEqualTo(1);

        // Second TEST activity on same lesson is rejected by uq_activities_lesson_path_step
        UUID act2 = UUID.randomUUID();
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        """
                INSERT INTO activities (
                    id, course_id, module_id, lesson_id, position, activity_type,
                    status, is_graded, collaboration_mode, variant_strategy, path_step,
                    created_at, updated_at, version
                ) VALUES (?, ?, ?, ?, 1, 'QUIZ', 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'TEST', now(), now(), 0)
                """,
                                        act2,
                                        course.getId(),
                                        moduleId,
                                        lessonId))
                .isInstanceOf(DataIntegrityViolationException.class);

        // PRACTICE activity on same lesson succeeds (different path_step)
        UUID actPractice = UUID.randomUUID();
        int practiceInserted =
                jdbc.update(
                        """
                INSERT INTO activities (
                    id, course_id, module_id, lesson_id, position, activity_type,
                    status, is_graded, collaboration_mode, variant_strategy, path_step,
                    created_at, updated_at, version
                ) VALUES (?, ?, ?, ?, 2, 'CODING_TASK', 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'PRACTICE', now(), now(), 0)
                """,
                        actPractice,
                        course.getId(),
                        moduleId,
                        lessonId);
        assertThat(practiceInserted).isEqualTo(1);
    }

    @Test
    void permissions_grantedToAdminRole() {
        Integer count =
                jdbc.queryForObject(
                        """
                SELECT count(*)
                FROM role_permissions rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.code = 'ADMIN'
                  AND p.code IN ('QUIZ_ATTEMPT_GRANT', 'LEARNING_PATH_MANAGE')
                """,
                        Integer.class);
        assertThat(count).isEqualTo(2);
    }
}
