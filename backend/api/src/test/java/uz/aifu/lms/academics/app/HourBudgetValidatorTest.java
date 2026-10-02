package uz.aifu.lms.academics.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseSessionAllocation;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.domain.Department;

class HourBudgetValidatorTest {

    private final HourBudgetValidator validator = new HourBudgetValidator();

    @Test
    void checkReconciliation_allocationsMatchTotal_empty() {
        Course course = course(180);
        List<CourseSessionAllocation> allocations =
                List.of(
                        allocation(course, SessionType.LECTURE, 60),
                        allocation(course, SessionType.SEMINAR, 30),
                        allocation(course, SessionType.PRACTICAL, 90));

        Optional<String> mismatch = validator.checkReconciliation(course, allocations);

        assertThat(mismatch).isEmpty();
    }

    @Test
    void checkReconciliation_allocationsUnderTotal_describesMismatch() {
        Course course = course(180);
        List<CourseSessionAllocation> allocations =
                List.of(allocation(course, SessionType.LECTURE, 60));

        Optional<String> mismatch = validator.checkReconciliation(course, allocations);

        assertThat(mismatch)
                .isPresent()
                .get(org.assertj.core.api.InstanceOfAssertFactories.STRING)
                .contains("60h")
                .contains("180h");
    }

    @Test
    void checkReconciliation_noAllocations_describesMismatch() {
        Course course = course(150);

        Optional<String> mismatch = validator.checkReconciliation(course, List.of());

        assertThat(mismatch)
                .isPresent()
                .get(org.assertj.core.api.InstanceOfAssertFactories.STRING)
                .contains("0h")
                .contains("150h");
    }

    @Test
    void validateForPublish_reconciles_doesNotThrow() {
        Course course = course(90);
        List<CourseSessionAllocation> allocations =
                List.of(allocation(course, SessionType.LECTURE, 90));

        validator.validateForPublish(course, allocations);
    }

    @Test
    void validateForPublish_mismatch_throws() {
        Course course = course(90);
        List<CourseSessionAllocation> allocations =
                List.of(allocation(course, SessionType.LECTURE, 30));

        assertThatThrownBy(() -> validator.validateForPublish(course, allocations))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.PUBLICATION_VALIDATION_FAILED);
    }

    private static CourseSessionAllocation allocation(Course course, SessionType type, int hours) {
        return new CourseSessionAllocation(course, type, hours);
    }

    private static Course course(int totalHours) {
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
                subject, term, "DEFAULT", BigDecimal.valueOf(6), totalHours, LocaleCode.UZ, null);
    }
}
