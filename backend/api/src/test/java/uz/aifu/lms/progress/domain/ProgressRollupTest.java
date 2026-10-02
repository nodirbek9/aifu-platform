package uz.aifu.lms.progress.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.GroupingSource;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.domain.Department;
import uz.aifu.lms.iam.domain.User;

/**
 * "Completing a lesson twice does not double-count; course % matches lesson roll-up"
 * (docs/planning/phase-plan.md Phase 6 Acceptance). These are pure domain assertions — the
 * COUNT-based idempotency of the roll-up itself is proved against real Postgres in {@code
 * ProgressIT}; this test locks down each entity's own transition rules.
 */
class ProgressRollupTest {

    private static final Instant T0 = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-01-15T10:05:00Z");

    @Test
    void lessonProgress_firstAccess_startsInProgress() {
        LessonProgress lp = new LessonProgress(enrollment(), lesson());

        lp.recordAccess(T0, false, 30);

        assertThat(lp.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);
        assertThat(lp.getViewCount()).isEqualTo(1);
        assertThat(lp.getTimeSpentSeconds()).isEqualTo(30);
        assertThat(lp.getFirstAccessedAt()).isEqualTo(T0);
        assertThat(lp.getLastAccessedAt()).isEqualTo(T0);
        assertThat(lp.getCompletedAt()).isNull();
    }

    @Test
    void lessonProgress_completingTwice_isIdempotent() {
        LessonProgress lp = new LessonProgress(enrollment(), lesson());
        lp.recordAccess(T0, true, 30);
        Instant firstCompletedAt = lp.getCompletedAt();

        lp.recordAccess(T1, true, 20);

        assertThat(lp.getStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(lp.getCompletedAt()).isEqualTo(firstCompletedAt);
        // A repeat view is still a real view/time entry, only completion itself is idempotent.
        assertThat(lp.getViewCount()).isEqualTo(2);
        assertThat(lp.getTimeSpentSeconds()).isEqualTo(50);
        assertThat(lp.getLastAccessedAt()).isEqualTo(T1);
    }

    @Test
    void moduleProgress_applyRollup_computesPercentAndCompletion() {
        ModuleProgress mp = new ModuleProgress(enrollment(), module());

        mp.applyRollup(2, 1, T0);
        assertThat(mp.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);
        assertThat(mp.getCompletionPercent()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(mp.getCompletedAt()).isNull();

        mp.applyRollup(2, 2, T1);
        assertThat(mp.getStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(mp.getCompletionPercent()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(mp.getCompletedAt()).isEqualTo(T1);
    }

    @Test
    void moduleProgress_completedAt_isNotRewrittenOnRepeatedFullRollup() {
        ModuleProgress mp = new ModuleProgress(enrollment(), module());
        mp.applyRollup(1, 1, T0);
        Instant firstCompletedAt = mp.getCompletedAt();

        mp.applyRollup(1, 1, T1);

        assertThat(mp.getCompletedAt()).isEqualTo(firstCompletedAt);
    }

    @Test
    void courseProgress_applyRollup_matchesLessonCounts() {
        CourseProgress cp = new CourseProgress(enrollment());

        cp.applyRollup(4, 2, 3, 0, T0);

        assertThat(cp.getLessonsTotal()).isEqualTo(4);
        assertThat(cp.getLessonsCompleted()).isEqualTo(2);
        assertThat(cp.getCompletionPercent()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(cp.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);

        cp.applyRollup(4, 4, 3, 3, T1);
        assertThat(cp.getStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(cp.getCompletionPercent()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(cp.getCompletedAt()).isEqualTo(T1);
    }

    @Test
    void courseProgress_noRequiredLessons_neverAutoCompletes() {
        CourseProgress cp = new CourseProgress(enrollment());

        cp.applyRollup(0, 0, 0, 0, T0);

        assertThat(cp.getStatus()).isNotEqualTo(ProgressStatus.COMPLETED);
        assertThat(cp.getCompletionPercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static Enrollment enrollment() {
        return new Enrollment(course(), student(), 1, EnrollmentSource.ADMIN, T0);
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

    private static CourseModule module() {
        return new CourseModule(course(), 0, "M1", GroupingSource.PLATFORM_INFERRED);
    }

    private static Lesson lesson() {
        return new Lesson(module(), 0, "L1", uz.aifu.lms.academics.domain.SessionType.LECTURE);
    }
}
