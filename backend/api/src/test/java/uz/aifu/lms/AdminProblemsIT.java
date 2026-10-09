package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.aifu.lms.academics.api.dto.CourseDtos.AssignTeacherRequest;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.TeacherRole;
import uz.aifu.lms.analytics.api.dto.AdminProblemDtos;
import uz.aifu.lms.analytics.domain.AdminProblemView;
import uz.aifu.lms.analytics.domain.AdminProblemView.AdminProblemType;
import uz.aifu.lms.analytics.persistence.AdminProblemViewRepository;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.Role;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.persistence.RoleRepository;
import uz.aifu.lms.iam.persistence.StudentProfileRepository;
import uz.aifu.lms.iam.persistence.UserRepository;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 7.4-B1/B2: Administrative problems read model and HTTP endpoint against real PostgreSQL.
 * Covers positive and negative cases for all six problem types, expired locks, missing profiles,
 * archived records, unlimited attempts, grants, successful midterms and pending grading.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminProblemsIT extends AbstractIntegrationTest {

    @Autowired private AdminProblemViewRepository problems;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private StudentProfileRepository studentProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;
    @LocalServerPort private int port;

    private User adminUser;
    private User teacherUser;

    @BeforeAll
    void setupBaseUsers() {
        adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        teacherUser = userFixtures.activeUser(RoleCode.TEACHER);
    }

    // -------------------------------------------------------------------------
    // 1. STUDENT_WITHOUT_GROUP
    // -------------------------------------------------------------------------

    @Test
    void studentWithoutGroup_positiveAndNegativeCases() {
        String activeGrpCode = "GRP_ACT_" + randomSuffix();
        String archivedGrpCode = "GRP_ARC_" + randomSuffix();
        createGroup(activeGrpCode, "ACTIVE");
        createGroup(archivedGrpCode, "ARCHIVED");

        // Positive 1: ACTIVE student with profile having null group code
        User studentNullGroup = createStudentUser(true);
        saveStudentProfile(studentNullGroup, null);

        // Positive 2: ACTIVE student with profile having blank group code
        User studentBlankGroup = createStudentUser(true);
        saveStudentProfile(studentBlankGroup, "   ");

        // Positive 3: ACTIVE student with NO profile at all (missing profiles)
        User studentNoProfile = createStudentUser(true);

        // Positive 4: ACTIVE student assigned to an ARCHIVED group (archived records)
        User studentArchivedGroup = createStudentUser(true);
        saveStudentProfile(studentArchivedGroup, archivedGrpCode);

        // Negative 1: ACTIVE student in an ACTIVE group
        User studentActiveGroup = createStudentUser(true);
        saveStudentProfile(studentActiveGroup, activeGrpCode);

        // Negative 2: DISABLED student with null group
        User studentDisabled = createStudentUser(false);
        saveStudentProfile(studentDisabled, null);

        // Negative 3: ACTIVE teacher without group (not STUDENT role)
        User plainTeacher = userFixtures.activeUser(RoleCode.TEACHER);

        List<AdminProblemView> found =
                problems.findByType(AdminProblemType.STUDENT_WITHOUT_GROUP, PageRequest.of(0, 100))
                        .getContent();

        List<UUID> foundUserIds = found.stream().map(AdminProblemView::getUserId).toList();

        // Positives must be included
        assertThat(foundUserIds)
                .contains(
                        studentNullGroup.getId(),
                        studentBlankGroup.getId(),
                        studentNoProfile.getId(),
                        studentArchivedGroup.getId());

        // Negatives must be excluded
        assertThat(foundUserIds)
                .doesNotContain(
                        studentActiveGroup.getId(), studentDisabled.getId(), plainTeacher.getId());

        // Verify problem key format and details on one positive
        AdminProblemView item =
                found.stream()
                        .filter(p -> p.getUserId().equals(studentNullGroup.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(item.getProblemKey())
                .isEqualTo("STUDENT_WITHOUT_GROUP:" + studentNullGroup.getId());
        assertThat(item.getType()).isEqualTo(AdminProblemType.STUDENT_WITHOUT_GROUP);
        assertThat(item.getFullName()).isEqualTo(studentNullGroup.getFullName());
    }

    // -------------------------------------------------------------------------
    // 2. GROUP_WITHOUT_PUBLISHED_ENTRIES
    // -------------------------------------------------------------------------

    @Test
    void groupWithoutPublishedEntries_positiveAndNegativeCases() {
        Course course = enrollmentFixtures.publishedCourse(150);
        UUID termId = getCourseTermId(course.getId());
        UUID pairSlotId = getFirstPairSlotId();

        // Positive 1: ACTIVE group with no entries at all
        String codeNoEntries = "GRP_NO_TT_" + randomSuffix();
        UUID groupNoEntriesId = createGroup(codeNoEntries, "ACTIVE");

        // Positive 2: ACTIVE group with entries only in a DRAFT timetable
        String codeDraftTt = "GRP_DRAFT_TT_" + randomSuffix();
        UUID groupDraftTtId = createGroup(codeDraftTt, "ACTIVE");
        UUID draftTtId = createTimetable(termId, "Draft Timetable " + randomSuffix(), "DRAFT");
        createTimetableEntry(
                draftTtId, course.getId(), teacherUser.getId(), pairSlotId, groupDraftTtId);

        // Negative 1: ACTIVE group with entry in a PUBLISHED timetable
        String codePublishedTt = "GRP_PUB_TT_" + randomSuffix();
        UUID groupPublishedTtId = createGroup(codePublishedTt, "ACTIVE");
        UUID pubTtId =
                createTimetable(termId, "Published Timetable " + randomSuffix(), "PUBLISHED");
        createTimetableEntry(
                pubTtId, course.getId(), teacherUser.getId(), pairSlotId, groupPublishedTtId);

        // Negative 2: ARCHIVED group with no entries (archived records)
        String codeArchived = "GRP_ARC_NO_TT_" + randomSuffix();
        UUID groupArchivedId = createGroup(codeArchived, "ARCHIVED");

        List<AdminProblemView> found =
                problems.findByType(
                                AdminProblemType.GROUP_WITHOUT_PUBLISHED_ENTRIES,
                                PageRequest.of(0, 100))
                        .getContent();

        List<UUID> foundGroupIds = found.stream().map(AdminProblemView::getGroupId).toList();

        assertThat(foundGroupIds).contains(groupNoEntriesId, groupDraftTtId);
        assertThat(foundGroupIds).doesNotContain(groupPublishedTtId, groupArchivedId);

        AdminProblemView item =
                found.stream()
                        .filter(p -> p.getGroupId().equals(groupNoEntriesId))
                        .findFirst()
                        .orElseThrow();
        assertThat(item.getProblemKey())
                .isEqualTo("GROUP_WITHOUT_PUBLISHED_ENTRIES:" + groupNoEntriesId);
        assertThat(item.getGroupCode()).isEqualTo(codeNoEntries);
    }

    // -------------------------------------------------------------------------
    // 3. COURSE_WITHOUT_TEACHER
    // -------------------------------------------------------------------------

    @Test
    void courseWithoutTeacher_positiveAndNegativeCases() {
        // Positive 1: PUBLISHED course without teachers
        Course coursePubNoTeacher = enrollmentFixtures.publishedCourse(150);

        // Positive 2: DRAFT course without teachers
        Course courseDraftNoTeacher = enrollmentFixtures.publishedCourse(150);
        jdbc.update(
                "UPDATE courses SET status = 'DRAFT' WHERE id = ?", courseDraftNoTeacher.getId());

        // Negative 1: PUBLISHED course with teacher assigned
        Course courseWithTeacher = enrollmentFixtures.publishedCourse(150);
        courseService.assignTeacher(
                courseWithTeacher.getId(),
                new AssignTeacherRequest(teacherUser.getId(), TeacherRole.LEAD),
                adminUser.getId(),
                null);

        // Negative 2: ARCHIVED course without teacher (archived records)
        Course courseArchived = enrollmentFixtures.publishedCourse(150);
        jdbc.update("UPDATE courses SET status = 'ARCHIVED' WHERE id = ?", courseArchived.getId());

        // Negative 3: Soft-deleted course without teacher
        Course courseDeleted = enrollmentFixtures.publishedCourse(150);
        jdbc.update("UPDATE courses SET deleted_at = now() WHERE id = ?", courseDeleted.getId());

        List<AdminProblemView> found =
                problems.findByType(AdminProblemType.COURSE_WITHOUT_TEACHER, PageRequest.of(0, 100))
                        .getContent();

        List<UUID> foundCourseIds = found.stream().map(AdminProblemView::getCourseId).toList();

        assertThat(foundCourseIds)
                .contains(coursePubNoTeacher.getId(), courseDraftNoTeacher.getId());
        assertThat(foundCourseIds)
                .doesNotContain(
                        courseWithTeacher.getId(), courseArchived.getId(), courseDeleted.getId());

        AdminProblemView item =
                found.stream()
                        .filter(p -> p.getCourseId().equals(coursePubNoTeacher.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(item.getProblemKey())
                .isEqualTo("COURSE_WITHOUT_TEACHER:" + coursePubNoTeacher.getId());
        assertThat(item.getSubjectCode()).isNotBlank();
        assertThat(item.getTermName()).isNotBlank();
    }

    // -------------------------------------------------------------------------
    // 4. ACTIVE_ENROLLMENT_INACTIVE_USER
    // -------------------------------------------------------------------------

    @Test
    void activeEnrollmentInactiveUser_positiveAndNegativeCases() {
        Course course = enrollmentFixtures.publishedCourse(150);

        // Positive 1: ACTIVE enrollment of a DISABLED user
        User userDisabled = createStudentUser(false);
        Enrollment enrDisabled = enroll(course, userDisabled);

        // Positive 2: ACTIVE enrollment of a SUSPENDED user
        User userSuspended = createStudentUser(true);
        jdbc.update("UPDATE users SET status = 'SUSPENDED' WHERE id = ?", userSuspended.getId());
        Enrollment enrSuspended = enroll(course, userSuspended);

        // Negative 1: ACTIVE enrollment of an ACTIVE user
        User userActive = createStudentUser(true);
        Enrollment enrActive = enroll(course, userActive);

        // Negative 2: WITHDRAWN enrollment of a DISABLED user
        User userWithdrawn = createStudentUser(false);
        Enrollment enrWithdrawn = enroll(course, userWithdrawn);
        jdbc.update(
                "UPDATE enrollments SET status = 'WITHDRAWN' WHERE id = ?", enrWithdrawn.getId());

        // Negative 3: COMPLETED enrollment of a DISABLED user
        User userCompleted = createStudentUser(false);
        Enrollment enrCompleted = enroll(course, userCompleted);
        jdbc.update(
                "UPDATE enrollments SET status = 'COMPLETED' WHERE id = ?", enrCompleted.getId());

        List<AdminProblemView> found =
                problems.findByType(
                                AdminProblemType.ACTIVE_ENROLLMENT_INACTIVE_USER,
                                PageRequest.of(0, 100))
                        .getContent();

        List<UUID> foundEnrIds = found.stream().map(AdminProblemView::getEnrollmentId).toList();

        assertThat(foundEnrIds).contains(enrDisabled.getId(), enrSuspended.getId());
        assertThat(foundEnrIds)
                .doesNotContain(enrActive.getId(), enrWithdrawn.getId(), enrCompleted.getId());

        AdminProblemView item =
                found.stream()
                        .filter(p -> p.getEnrollmentId().equals(enrDisabled.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(item.getProblemKey())
                .isEqualTo("ACTIVE_ENROLLMENT_INACTIVE_USER:" + enrDisabled.getId());
        assertThat(item.getUserId()).isEqualTo(userDisabled.getId());
    }

    // -------------------------------------------------------------------------
    // 5. LOCKED_ACCOUNT
    // -------------------------------------------------------------------------

    @Test
    void lockedAccount_positiveAndNegativeCases() {
        // Positive 1: User locked until future timestamp
        User lockedUser = createStudentUser(true);
        Instant futureLock = Instant.now().plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        jdbc.update(
                "UPDATE users SET locked_until = ? WHERE id = ?",
                Timestamp.from(futureLock),
                lockedUser.getId());

        // Negative 1: User with expired lock (expired locks test)
        User expiredLockUser = createStudentUser(true);
        Instant pastLock = Instant.now().minus(2, ChronoUnit.HOURS);
        jdbc.update(
                "UPDATE users SET locked_until = ? WHERE id = ?",
                Timestamp.from(pastLock),
                expiredLockUser.getId());

        // Negative 2: User with no lock
        User unlockedUser = createStudentUser(true);

        List<AdminProblemView> found =
                problems.findByType(AdminProblemType.LOCKED_ACCOUNT, PageRequest.of(0, 100))
                        .getContent();

        List<UUID> foundUserIds = found.stream().map(AdminProblemView::getUserId).toList();

        assertThat(foundUserIds).contains(lockedUser.getId());
        assertThat(foundUserIds).doesNotContain(expiredLockUser.getId(), unlockedUser.getId());

        AdminProblemView item =
                found.stream()
                        .filter(p -> p.getUserId().equals(lockedUser.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(item.getProblemKey()).isEqualTo("LOCKED_ACCOUNT:" + lockedUser.getId());
        assertThat(item.getLockedUntil()).isNotNull();
        assertThat(item.getLockedUntil()).isAfter(Instant.now());
    }

    // -------------------------------------------------------------------------
    // 6. MIDTERM_ATTEMPTS_EXHAUSTED
    // -------------------------------------------------------------------------

    @Test
    void midtermAttemptsExhausted_positiveAndNegativeCases() {
        Course course = enrollmentFixtures.publishedCourse(150);
        UUID lessonId = createPublishedLesson(course.getId());
        UUID assessmentCompId = getTopLevelAssessmentComponentId(course.getId());

        // Setup midterm quiz and activity
        UUID quizId = createQuiz(course.getId(), 2, "PUBLISHED");
        UUID midtermActivityId =
                createMidtermActivity(course.getId(), quizId, assessmentCompId, "PUBLISHED");
        enableCourseLearningPath(course.getId(), midtermActivityId, lessonId);

        // Positive 1: 2 failed graded attempts, 0 grants -> EXHAUSTED, grantAvailable = true
        User studentExhausted = createStudentUser(true);
        Enrollment enrExhausted = enroll(course, studentExhausted);
        createQuizAttempt(quizId, enrExhausted.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttempt(quizId, enrExhausted.getId(), midtermActivityId, 2, "GRADED", false, 50);

        // Positive 2: 1 grant given, student also failed attempt 3 -> EXHAUSTED, grantAvailable =
        // false
        User studentGrantExhausted = createStudentUser(true);
        Enrollment enrGrantExhausted = enroll(course, studentGrantExhausted);
        createQuizAttempt(
                quizId, enrGrantExhausted.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttempt(
                quizId, enrGrantExhausted.getId(), midtermActivityId, 2, "GRADED", false, 50);
        createQuizGrant(quizId, enrGrantExhausted.getId(), adminUser.getId(), "Extra attempt");
        createQuizAttempt(
                quizId, enrGrantExhausted.getId(), midtermActivityId, 3, "GRADED", false, 45);

        // Positive 3: Attempt 1 failed, Attempt 2 is elapsed IN_PROGRESS -> EXHAUSTED
        User studentElapsedInProgress = createStudentUser(true);
        Enrollment enrElapsedInProgress = enroll(course, studentElapsedInProgress);
        createQuizAttempt(
                quizId, enrElapsedInProgress.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttemptWithExpiry(
                quizId,
                enrElapsedInProgress.getId(),
                midtermActivityId,
                2,
                "IN_PROGRESS",
                Instant.now().minus(10, ChronoUnit.MINUTES));

        // Negative 1: Only 1 attempt used out of 2 (attempts remaining)
        User studentOneAttempt = createStudentUser(true);
        Enrollment enrOneAttempt = enroll(course, studentOneAttempt);
        createQuizAttempt(quizId, enrOneAttempt.getId(), midtermActivityId, 1, "GRADED", false, 40);

        // Negative 2: 0 attempts used
        User studentZeroAttempts = createStudentUser(true);
        Enrollment enrZeroAttempts = enroll(course, studentZeroAttempts);

        // Negative 3: 2 attempts used, but 1 grant given (attemptsAllowed = 3, used = 2)
        User studentWithGrantRemaining = createStudentUser(true);
        Enrollment enrWithGrantRemaining = enroll(course, studentWithGrantRemaining);
        createQuizAttempt(
                quizId, enrWithGrantRemaining.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttempt(
                quizId, enrWithGrantRemaining.getId(), midtermActivityId, 2, "GRADED", false, 50);
        createQuizGrant(quizId, enrWithGrantRemaining.getId(), adminUser.getId(), "Extra attempt");

        // Negative 4: Passed midterm (successful midterms)
        User studentPassed = createStudentUser(true);
        Enrollment enrPassed = enroll(course, studentPassed);
        createQuizAttempt(quizId, enrPassed.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttempt(quizId, enrPassed.getId(), midtermActivityId, 2, "GRADED", true, 85);

        // Negative 5: Attempt 2 is SUBMITTED awaiting grading (pending grading)
        User studentSubmitted = createStudentUser(true);
        Enrollment enrSubmitted = enroll(course, studentSubmitted);
        createQuizAttempt(quizId, enrSubmitted.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttempt(
                quizId, enrSubmitted.getId(), midtermActivityId, 2, "SUBMITTED", false, 0);

        // Negative 6: Attempt 2 is IN_PROGRESS and still usable (not elapsed)
        User studentUsableInProgress = createStudentUser(true);
        Enrollment enrUsableInProgress = enroll(course, studentUsableInProgress);
        createQuizAttempt(
                quizId, enrUsableInProgress.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttemptWithExpiry(
                quizId,
                enrUsableInProgress.getId(),
                midtermActivityId,
                2,
                "IN_PROGRESS",
                Instant.now().plus(30, ChronoUnit.MINUTES));

        // Negative 7: Midterm quiz has max_attempts = null (unlimited attempts)
        Course courseUnlimited = enrollmentFixtures.publishedCourse(150);
        UUID lessonUnlimitedId = createPublishedLesson(courseUnlimited.getId());
        UUID compUnlimitedId = getTopLevelAssessmentComponentId(courseUnlimited.getId());
        UUID quizUnlimitedId = createQuiz(courseUnlimited.getId(), null, "PUBLISHED");
        UUID actUnlimitedId =
                createMidtermActivity(
                        courseUnlimited.getId(), quizUnlimitedId, compUnlimitedId, "PUBLISHED");
        enableCourseLearningPath(courseUnlimited.getId(), actUnlimitedId, lessonUnlimitedId);
        User studentUnlimited = createStudentUser(true);
        Enrollment enrUnlimited = enroll(courseUnlimited, studentUnlimited);
        createQuizAttempt(
                quizUnlimitedId, enrUnlimited.getId(), actUnlimitedId, 1, "GRADED", false, 30);
        createQuizAttempt(
                quizUnlimitedId, enrUnlimited.getId(), actUnlimitedId, 2, "GRADED", false, 30);

        // Negative 8: Learning path is disabled
        Course coursePathDisabled = enrollmentFixtures.publishedCourse(150);
        UUID lessonPathDisabledId = createPublishedLesson(coursePathDisabled.getId());
        UUID compPathDisabledId = getTopLevelAssessmentComponentId(coursePathDisabled.getId());
        UUID quizPathDisabledId = createQuiz(coursePathDisabled.getId(), 2, "PUBLISHED");
        UUID actPathDisabledId =
                createMidtermActivity(
                        coursePathDisabled.getId(),
                        quizPathDisabledId,
                        compPathDisabledId,
                        "PUBLISHED");
        insertCourseLearningPath(
                coursePathDisabled.getId(), false, actPathDisabledId, lessonPathDisabledId);
        User studentPathDisabled = createStudentUser(true);
        Enrollment enrPathDisabled = enroll(coursePathDisabled, studentPathDisabled);
        createQuizAttempt(
                quizPathDisabledId,
                enrPathDisabled.getId(),
                actPathDisabledId,
                1,
                "GRADED",
                false,
                40);
        createQuizAttempt(
                quizPathDisabledId,
                enrPathDisabled.getId(),
                actPathDisabledId,
                2,
                "GRADED",
                false,
                40);

        // Negative 9: Midterm quiz is DRAFT
        Course courseDraftQuiz = enrollmentFixtures.publishedCourse(150);
        UUID lessonDraftQuizId = createPublishedLesson(courseDraftQuiz.getId());
        UUID compDraftQuizId = getTopLevelAssessmentComponentId(courseDraftQuiz.getId());
        UUID quizDraftId = createQuiz(courseDraftQuiz.getId(), 2, "DRAFT");
        UUID actDraftQuizId =
                createMidtermActivity(
                        courseDraftQuiz.getId(), quizDraftId, compDraftQuizId, "PUBLISHED");
        enableCourseLearningPath(courseDraftQuiz.getId(), actDraftQuizId, lessonDraftQuizId);
        User studentDraftQuiz = createStudentUser(true);
        Enrollment enrDraftQuiz = enroll(courseDraftQuiz, studentDraftQuiz);
        createQuizAttempt(
                quizDraftId, enrDraftQuiz.getId(), actDraftQuizId, 1, "GRADED", false, 40);
        createQuizAttempt(
                quizDraftId, enrDraftQuiz.getId(), actDraftQuizId, 2, "GRADED", false, 40);

        // Negative 10: Enrollment is WITHDRAWN
        User studentWithdrawnEnr = createStudentUser(true);
        Enrollment enrWithdrawnStatus = enroll(course, studentWithdrawnEnr);
        createQuizAttempt(
                quizId, enrWithdrawnStatus.getId(), midtermActivityId, 1, "GRADED", false, 40);
        createQuizAttempt(
                quizId, enrWithdrawnStatus.getId(), midtermActivityId, 2, "GRADED", false, 40);
        jdbc.update(
                "UPDATE enrollments SET status = 'WITHDRAWN' WHERE id = ?",
                enrWithdrawnStatus.getId());

        List<AdminProblemView> found =
                problems.findByType(
                                AdminProblemType.MIDTERM_ATTEMPTS_EXHAUSTED, PageRequest.of(0, 100))
                        .getContent();

        List<UUID> foundEnrIds = found.stream().map(AdminProblemView::getEnrollmentId).toList();

        // Check positives
        assertThat(foundEnrIds)
                .contains(
                        enrExhausted.getId(),
                        enrGrantExhausted.getId(),
                        enrElapsedInProgress.getId());

        // Check negatives
        assertThat(foundEnrIds)
                .doesNotContain(
                        enrOneAttempt.getId(),
                        enrZeroAttempts.getId(),
                        enrWithGrantRemaining.getId(),
                        enrPassed.getId(),
                        enrSubmitted.getId(),
                        enrUsableInProgress.getId(),
                        enrUnlimited.getId(),
                        enrPathDisabled.getId(),
                        enrDraftQuiz.getId(),
                        enrWithdrawnStatus.getId());

        // Check grant availability on Positive 1 (no grants used yet)
        AdminProblemView itemPositive1 =
                found.stream()
                        .filter(p -> p.getEnrollmentId().equals(enrExhausted.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(itemPositive1.getAttemptsUsed()).isEqualTo(2);
        assertThat(itemPositive1.getAttemptsAllowed()).isEqualTo(2);
        assertThat(itemPositive1.getGrantAvailable()).isTrue();

        // Check grant availability on Positive 2 (grant already used)
        AdminProblemView itemPositive2 =
                found.stream()
                        .filter(p -> p.getEnrollmentId().equals(enrGrantExhausted.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(itemPositive2.getAttemptsUsed()).isEqualTo(3);
        assertThat(itemPositive2.getAttemptsAllowed()).isEqualTo(3);
        assertThat(itemPositive2.getGrantAvailable()).isFalse();

        // Check Positive 3 with elapsed attempt
        AdminProblemView itemPositive3 =
                found.stream()
                        .filter(p -> p.getEnrollmentId().equals(enrElapsedInProgress.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(itemPositive3.getAttemptsUsed()).isEqualTo(2);
        assertThat(itemPositive3.getAttemptsAllowed()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // 7. Pagination, Sorting & DTO mapping
    // -------------------------------------------------------------------------

    @Test
    void paginationSortingAndDtoMapping() {
        // Default sort: type ASC, problemKey ASC
        Sort defaultSort = Sort.by("type").ascending().and(Sort.by("problemKey").ascending());
        Page<AdminProblemView> page0 = problems.findAll(PageRequest.of(0, 10, defaultSort));

        assertThat(page0.getContent()).isNotEmpty();
        assertThat(page0.getTotalElements()).isGreaterThanOrEqualTo(page0.getContent().size());

        // Verify sorting order in page content
        for (int i = 0; i < page0.getContent().size() - 1; i++) {
            AdminProblemView current = page0.getContent().get(i);
            AdminProblemView next = page0.getContent().get(i + 1);
            int typeCompare = current.getType().name().compareTo(next.getType().name());
            assertThat(typeCompare).isLessThanOrEqualTo(0);
            if (typeCompare == 0) {
                assertThat(current.getProblemKey().compareTo(next.getProblemKey()))
                        .isLessThanOrEqualTo(0);
            }
        }

        // Verify findById
        AdminProblemView firstItem = page0.getContent().getFirst();
        Optional<AdminProblemView> foundById = problems.findById(firstItem.getProblemKey());
        assertThat(foundById).isPresent();
        assertThat(foundById.get()).isEqualTo(firstItem);

        // Verify DTO record mapping
        AdminProblemDtos.AdminProblemView dto = AdminProblemDtos.AdminProblemView.from(firstItem);
        assertThat(dto.problemKey()).isEqualTo(firstItem.getProblemKey());
        assertThat(dto.type()).isEqualTo(firstItem.getType());
        assertThat(dto.userId()).isEqualTo(firstItem.getUserId());
        assertThat(dto.courseId()).isEqualTo(firstItem.getCourseId());
        assertThat(dto.enrollmentId()).isEqualTo(firstItem.getEnrollmentId());

        // Count queries
        long totalCount = problems.count();
        assertThat(totalCount).isGreaterThan(0);
        long studentCount = problems.countByType(AdminProblemType.STUDENT_WITHOUT_GROUP);
        assertThat(studentCount).isGreaterThanOrEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // HTTP endpoint pagination, validation and live problem resolution
    // -------------------------------------------------------------------------

    @Test
    void list_allAndEachType_paginateWithStableOrderingAndCorrectTotals() {
        createStudentUser(true);
        createGroup("PAGE-" + randomSuffix(), "ACTIVE");
        Course course = enrollmentFixtures.publishedCourse(150);
        enroll(course, createStudentUser(false));
        User locked = createStudentUser(true);
        jdbc.update(
                "UPDATE users SET locked_until = now() + interval '1 hour' WHERE id = ?",
                locked.getId());
        UUID quizId = createQuiz(course.getId(), 2, "PUBLISHED");
        UUID activityId =
                createMidtermActivity(
                        course.getId(),
                        quizId,
                        getTopLevelAssessmentComponentId(course.getId()),
                        "PUBLISHED");
        enableCourseLearningPath(course.getId(), activityId, createPublishedLesson(course.getId()));
        Enrollment exhausted = enroll(course, createStudentUser(true));
        createQuizAttempt(quizId, exhausted.getId(), activityId, 1, "GRADED", false, 40);
        createQuizAttempt(quizId, exhausted.getId(), activityId, 2, "GRADED", false, 40);

        HttpHeaders headers = adminHeaders();
        assertPagesMatchSql(null, headers);
        for (AdminProblemType type : AdminProblemType.values()) {
            assertPagesMatchSql(type, headers);
        }
    }

    @Test
    void list_multipleUserRolesAndQuizAttempts_doNotDuplicateProblemsWithinType() {
        User student = createStudentUser(true);
        jdbc.update(
                "INSERT INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE code = ?",
                student.getId(),
                RoleCode.TEACHER);
        Course course = enrollmentFixtures.publishedCourse(150);
        UUID quizId = createQuiz(course.getId(), 2, "PUBLISHED");
        UUID activityId =
                createMidtermActivity(
                        course.getId(),
                        quizId,
                        getTopLevelAssessmentComponentId(course.getId()),
                        "PUBLISHED");
        enableCourseLearningPath(course.getId(), activityId, createPublishedLesson(course.getId()));
        Enrollment enrollment = enroll(course, student);
        createQuizAttempt(quizId, enrollment.getId(), activityId, 1, "GRADED", false, 40);
        createQuizAttempt(quizId, enrollment.getId(), activityId, 2, "GRADED", false, 40);

        List<AdminProblemDtos.AdminProblemView> rows = allProblems(null, adminHeaders());
        assertThat(rows)
                .extracting(AdminProblemDtos.AdminProblemView::problemKey)
                .doesNotHaveDuplicates();
        assertThat(rows.stream().filter(row -> student.getId().equals(row.userId())))
                .extracting(AdminProblemDtos.AdminProblemView::type)
                .containsExactlyInAnyOrder(
                        AdminProblemType.STUDENT_WITHOUT_GROUP,
                        AdminProblemType.MIDTERM_ATTEMPTS_EXHAUSTED);
    }

    @Test
    void list_invalidTypeAndUnsupportedSort_returnSafeBadRequest() {
        HttpHeaders headers = adminHeaders();
        for (String query : List.of("?type=UNKNOWN", "?sort=passwordHash,asc")) {
            ResponseEntity<ApiError> response =
                    rest.exchange(
                            url(query), HttpMethod.GET, new HttpEntity<>(headers), ApiError.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().code())
                    .isEqualTo(
                            query.startsWith("?type=") ? "MALFORMED_REQUEST" : "VALIDATION_FAILED");
            assertThat(response.getBody().requestId()).isNotBlank();
            assertThat(response.getBody().toString())
                    .doesNotContain("problemKey", "fullName", "passwordHash");
        }
    }

    @Test
    void list_sizeCapExplicitSortAndOutOfRangePage_preserveEnvelopeAndTotals() {
        createStudentUser(true);
        HttpHeaders headers = adminHeaders();
        PageResponse<AdminProblemDtos.AdminProblemView> capped = getProblems("?size=1000", headers);
        assertThat(capped.size()).isEqualTo(100);
        assertThat(capped.content()).hasSizeLessThanOrEqualTo(100);
        PageResponse<AdminProblemDtos.AdminProblemView> descending =
                getProblems("?size=5&sort=problemKey,desc", headers);
        List<String> expected =
                jdbc.queryForList(
                        "SELECT problem_key FROM v_admin_problems ORDER BY problem_key DESC LIMIT 5",
                        String.class);
        assertThat(descending.content())
                .extracting(AdminProblemDtos.AdminProblemView::problemKey)
                .containsExactlyElementsOf(expected);
        PageResponse<AdminProblemDtos.AdminProblemView> byType =
                getProblems("?size=5&sort=type,desc", headers);
        List<String> typeExpected =
                jdbc.queryForList(
                        "SELECT problem_key FROM v_admin_problems ORDER BY type DESC, problem_key ASC LIMIT 5",
                        String.class);
        assertThat(byType.content())
                .extracting(AdminProblemDtos.AdminProblemView::problemKey)
                .containsExactlyElementsOf(typeExpected);
        PageResponse<AdminProblemDtos.AdminProblemView> empty =
                getProblems("?page=1000000&size=100", headers);
        assertThat(empty.content()).isEmpty();
        assertThat(empty.totalElements()).isEqualTo(capped.totalElements());
        assertThat(empty.hasNext()).isFalse();
    }

    @Test
    void list_assigningStudentGroup_removesMissingGroupProblem() {
        User student = createStudentUser(true);
        HttpHeaders headers = adminHeaders();
        assertProblemPresent(AdminProblemType.STUDENT_WITHOUT_GROUP, student.getId(), headers);
        String code = "FIX-" + randomSuffix();
        createGroup(code, "ACTIVE");
        saveStudentProfile(student, code);
        assertProblemAbsent(AdminProblemType.STUDENT_WITHOUT_GROUP, student.getId(), headers);
    }

    @Test
    void list_publishingGroupTimetable_removesMissingPublishedEntriesProblem() {
        Course course = enrollmentFixtures.publishedCourse(150);
        UUID groupId = createGroup("FIX-TT-" + randomSuffix(), "ACTIVE");
        UUID timetableId =
                createTimetable(getCourseTermId(course.getId()), "Fix " + randomSuffix(), "DRAFT");
        createTimetableEntry(
                timetableId, course.getId(), teacherUser.getId(), getFirstPairSlotId(), groupId);
        HttpHeaders headers = adminHeaders();
        assertProblemPresent(AdminProblemType.GROUP_WITHOUT_PUBLISHED_ENTRIES, groupId, headers);
        jdbc.update("UPDATE timetables SET status = 'PUBLISHED' WHERE id = ?", timetableId);
        assertProblemAbsent(AdminProblemType.GROUP_WITHOUT_PUBLISHED_ENTRIES, groupId, headers);
    }

    @Test
    void list_unlockingAccount_removesLockedAccountProblem() {
        User student = createStudentUser(true);
        jdbc.update(
                "UPDATE users SET locked_until = now() + interval '1 hour' WHERE id = ?",
                student.getId());
        HttpHeaders headers = adminHeaders();
        assertProblemPresent(AdminProblemType.LOCKED_ACCOUNT, student.getId(), headers);
        jdbc.update("UPDATE users SET locked_until = NULL WHERE id = ?", student.getId());
        assertProblemAbsent(AdminProblemType.LOCKED_ACCOUNT, student.getId(), headers);
    }

    @Test
    void list_assigningTeacher_removesMissingTeacherProblem() {
        Course course = enrollmentFixtures.publishedCourse(150);
        HttpHeaders headers = adminHeaders();
        assertProblemPresent(AdminProblemType.COURSE_WITHOUT_TEACHER, course.getId(), headers);
        courseService.assignTeacher(
                course.getId(),
                new AssignTeacherRequest(teacherUser.getId(), TeacherRole.LEAD),
                adminUser.getId(),
                null);
        assertProblemAbsent(AdminProblemType.COURSE_WITHOUT_TEACHER, course.getId(), headers);
    }

    @Test
    void list_reactivatingUser_removesInactiveUserEnrollmentProblem() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = createStudentUser(false);
        Enrollment enrollment = enroll(course, student);
        HttpHeaders headers = adminHeaders();
        assertProblemPresent(
                AdminProblemType.ACTIVE_ENROLLMENT_INACTIVE_USER, enrollment.getId(), headers);
        jdbc.update("UPDATE users SET status = 'ACTIVE' WHERE id = ?", student.getId());
        assertProblemAbsent(
                AdminProblemType.ACTIVE_ENROLLMENT_INACTIVE_USER, enrollment.getId(), headers);
    }

    @Test
    void list_grantingAttempt_removesExhaustedMidtermProblem() {
        Course course = enrollmentFixtures.publishedCourse(150);
        UUID quizId = createQuiz(course.getId(), 2, "PUBLISHED");
        UUID activityId =
                createMidtermActivity(
                        course.getId(),
                        quizId,
                        getTopLevelAssessmentComponentId(course.getId()),
                        "PUBLISHED");
        enableCourseLearningPath(course.getId(), activityId, createPublishedLesson(course.getId()));
        Enrollment enrollment = enroll(course, createStudentUser(true));
        createQuizAttempt(quizId, enrollment.getId(), activityId, 1, "GRADED", false, 40);
        createQuizAttempt(quizId, enrollment.getId(), activityId, 2, "GRADED", false, 40);
        HttpHeaders headers = adminHeaders();
        assertProblemPresent(
                AdminProblemType.MIDTERM_ATTEMPTS_EXHAUSTED, enrollment.getId(), headers);
        createQuizGrant(quizId, enrollment.getId(), adminUser.getId(), "Additional attempt");
        assertProblemAbsent(
                AdminProblemType.MIDTERM_ATTEMPTS_EXHAUSTED, enrollment.getId(), headers);
    }

    private void assertPagesMatchSql(AdminProblemType type, HttpHeaders headers) {
        List<String> expected =
                type == null
                        ? jdbc.queryForList(
                                "SELECT problem_key FROM v_admin_problems ORDER BY type, problem_key",
                                String.class)
                        : jdbc.queryForList(
                                "SELECT problem_key FROM v_admin_problems WHERE type = ? ORDER BY type, problem_key",
                                String.class,
                                type.name());
        assertThat(expected).isNotEmpty();
        List<AdminProblemDtos.AdminProblemView> rows = new ArrayList<>();
        int size = 3;
        int totalPages = (expected.size() + size - 1) / size;
        for (int page = 0; page < totalPages; page++) {
            String query =
                    "?page=" + page + "&size=" + size + (type == null ? "" : "&type=" + type);
            PageResponse<AdminProblemDtos.AdminProblemView> response = getProblems(query, headers);
            assertThat(response.page()).isEqualTo(page);
            assertThat(response.size()).isEqualTo(size);
            assertThat(response.totalElements()).isEqualTo(expected.size());
            assertThat(response.totalPages()).isEqualTo(totalPages);
            assertThat(response.hasNext()).isEqualTo(page + 1 < totalPages);
            assertThat(response.hasPrevious()).isEqualTo(page > 0);
            if (type != null) {
                assertThat(response.content())
                        .extracting(AdminProblemDtos.AdminProblemView::type)
                        .containsOnly(type);
            }
            rows.addAll(response.content());
            assertThat(getProblems(query, headers)).isEqualTo(response);
        }
        assertThat(rows)
                .extracting(AdminProblemDtos.AdminProblemView::problemKey)
                .containsExactlyElementsOf(expected);
        assertThat(rows)
                .isSortedAccordingTo(
                        Comparator.comparing(
                                        (AdminProblemDtos.AdminProblemView row) ->
                                                row.type().name())
                                .thenComparing(AdminProblemDtos.AdminProblemView::problemKey));
    }

    private List<AdminProblemDtos.AdminProblemView> allProblems(
            AdminProblemType type, HttpHeaders headers) {
        List<AdminProblemDtos.AdminProblemView> rows = new ArrayList<>();
        int page = 0;
        PageResponse<AdminProblemDtos.AdminProblemView> response;
        do {
            response =
                    getProblems(
                            "?size=100&page=" + page++ + (type == null ? "" : "&type=" + type),
                            headers);
            rows.addAll(response.content());
        } while (response.hasNext());
        return rows;
    }

    private void assertProblemPresent(AdminProblemType type, UUID id, HttpHeaders headers) {
        assertThat(allProblems(type, headers))
                .extracting(AdminProblemDtos.AdminProblemView::problemKey)
                .contains(type + ":" + id);
    }

    private void assertProblemAbsent(AdminProblemType type, UUID id, HttpHeaders headers) {
        assertThat(allProblems(type, headers))
                .extracting(AdminProblemDtos.AdminProblemView::problemKey)
                .doesNotContain(type + ":" + id);
    }

    private PageResponse<AdminProblemDtos.AdminProblemView> getProblems(
            String query, HttpHeaders headers) {
        ResponseEntity<PageResponse<AdminProblemDtos.AdminProblemView>> response =
                rest.exchange(
                        url(query),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private HttpHeaders adminHeaders() {
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        "http://localhost:" + port + "/api/v1/auth/login",
                        new LoginRequest(adminUser.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(response.getBody().accessToken());
        return headers;
    }

    private String url(String query) {
        return "http://localhost:" + port + "/api/v1/admin/problems" + query;
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private User createStudentUser(boolean active) {
        String suffix = randomSuffix();
        User user =
                new User(
                        "student-" + suffix + "@test.aifu.local",
                        "student_" + suffix,
                        passwordEncoder.encode(UserFixtures.RAW_PASSWORD),
                        "Test Student " + suffix,
                        LocaleCode.DEFAULT);
        if (active) {
            user.activate();
        } else {
            user.changeStatus(uz.aifu.lms.iam.domain.UserStatus.DISABLED);
        }
        Role studentRole = roleRepository.findByCode(RoleCode.STUDENT).orElseThrow();
        user.assignRole(studentRole);
        return userRepository.save(user);
    }

    private void saveStudentProfile(User user, String groupCode) {
        String studentNumber = "STU-" + randomSuffix();
        StudentProfile profile = new StudentProfile(user, studentNumber, null, groupCode, 2026);
        studentProfileRepository.save(profile);
    }

    private UUID createGroup(String code, String status) {
        UUID groupId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO student_groups (id, code, status, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, now(), now(), 0)",
                groupId,
                code,
                status);
        return groupId;
    }

    private UUID createTimetable(UUID termId, String name, String status) {
        UUID ttId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO timetables (id, term_id, name, status, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, now(), now(), 0)",
                ttId,
                termId,
                name,
                status);
        return ttId;
    }

    private void createTimetableEntry(
            UUID timetableId, UUID courseId, UUID teacherId, UUID pairSlotId, UUID groupId) {
        UUID entryId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO timetable_entries (id, timetable_id, course_id, session_type, teacher_user_id, "
                        + "day_of_week, pair_slot_id, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 'LECTURE', ?, 1, ?, now(), now(), 0)",
                entryId,
                timetableId,
                courseId,
                teacherId,
                pairSlotId);
        jdbc.update(
                "INSERT INTO timetable_entry_groups (entry_id, group_id) VALUES (?, ?)",
                entryId,
                groupId);
    }

    private UUID getCourseTermId(UUID courseId) {
        return jdbc.queryForObject(
                "SELECT term_id FROM courses WHERE id = ?", UUID.class, courseId);
    }

    private UUID getFirstPairSlotId() {
        return jdbc.queryForObject(
                "SELECT id FROM pair_slots ORDER BY pair_no LIMIT 1", UUID.class);
    }

    private UUID getTopLevelAssessmentComponentId(UUID courseId) {
        return jdbc.queryForObject(
                "SELECT id FROM assessment_components WHERE course_id = ? AND parent_component_id IS NULL LIMIT 1",
                UUID.class,
                courseId);
    }

    private Enrollment enroll(Course course, User student) {
        return enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, adminUser.getId(), null);
    }

    private UUID createPublishedLesson(UUID courseId) {
        UUID moduleId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO course_modules (id, course_id, position, status, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'PUBLISHED', now(), now(), 0)",
                moduleId,
                courseId);
        jdbc.update(
                "INSERT INTO course_module_translations (module_id, locale, title) VALUES (?, 'uz', 'Modul')",
                moduleId);
        UUID lessonId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, estimated_minutes, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'LECTURE', 'PUBLISHED', true, 1, now(), now(), 0)",
                lessonId,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Dars')",
                lessonId);
        return lessonId;
    }

    private UUID createQuiz(UUID courseId, Integer maxAttempts, String status) {
        UUID quizId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quizzes (id, course_id, code, status, max_attempts, passing_percent, max_points, shuffle_questions, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, ?, 60, 100, false, now(), now(), 0)",
                quizId,
                courseId,
                "Q-" + randomSuffix(),
                status,
                maxAttempts);
        return quizId;
    }

    private UUID createMidtermActivity(
            UUID courseId, UUID quizId, UUID assessmentComponentId, String status) {
        UUID actId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO activities (id, course_id, lesson_id, position, activity_type, path_step, "
                        + "reference_id, assessment_component_id, is_graded, status, variant_strategy, "
                        + "collaboration_mode, created_at, updated_at, version) "
                        + "VALUES (?, ?, NULL, 0, 'QUIZ', 'MIDTERM', ?, ?, true, ?, 'NONE', 'INDIVIDUAL', now(), now(), 0)",
                actId,
                courseId,
                quizId,
                assessmentComponentId,
                status);
        return actId;
    }

    private void enableCourseLearningPath(
            UUID courseId, UUID midtermActivityId, UUID midtermAfterLessonId) {
        insertCourseLearningPath(courseId, true, midtermActivityId, midtermAfterLessonId);
    }

    private void insertCourseLearningPath(
            UUID courseId, boolean enabled, UUID midtermActivityId, UUID midtermAfterLessonId) {
        jdbc.update(
                "INSERT INTO course_learning_paths (course_id, enabled, midterm_activity_id, midterm_after_lesson_id, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, now(), now(), 0) "
                        + "ON CONFLICT (course_id) DO UPDATE SET enabled = EXCLUDED.enabled, "
                        + "midterm_activity_id = EXCLUDED.midterm_activity_id, "
                        + "midterm_after_lesson_id = EXCLUDED.midterm_after_lesson_id",
                courseId,
                enabled,
                midtermActivityId,
                midtermAfterLessonId);
    }

    private void createQuizAttempt(
            UUID quizId,
            UUID enrollmentId,
            UUID activityId,
            int attemptNumber,
            String status,
            boolean passed,
            int percent) {
        UUID attemptId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quiz_attempts (id, quiz_id, enrollment_id, activity_id, attempt_number, "
                        + "status, passed, percent, started_at, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), 0)",
                attemptId,
                quizId,
                enrollmentId,
                activityId,
                attemptNumber,
                status,
                passed,
                BigDecimal.valueOf(percent));
    }

    private void createQuizAttemptWithExpiry(
            UUID quizId,
            UUID enrollmentId,
            UUID activityId,
            int attemptNumber,
            String status,
            Instant expiresAt) {
        UUID attemptId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quiz_attempts (id, quiz_id, enrollment_id, activity_id, attempt_number, "
                        + "status, passed, percent, started_at, expires_at, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, false, 0, now(), ?, 0)",
                attemptId,
                quizId,
                enrollmentId,
                activityId,
                attemptNumber,
                status,
                Timestamp.from(expiresAt));
    }

    private void createQuizGrant(UUID quizId, UUID enrollmentId, UUID adminId, String reason) {
        UUID grantId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quiz_attempt_grants (id, quiz_id, enrollment_id, granted_by, reason, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, now())",
                grantId,
                quizId,
                enrollmentId,
                adminId,
                reason);
    }
}
