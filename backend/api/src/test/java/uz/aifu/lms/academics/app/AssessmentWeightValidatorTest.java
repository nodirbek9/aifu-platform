package uz.aifu.lms.academics.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.domain.Department;

class AssessmentWeightValidatorTest {

    private final AssessmentWeightValidator validator = new AssessmentWeightValidator();

    @Test
    void validateTopLevelWeights_sumsToScheme_passes() {
        Course course = course();
        List<AssessmentComponent> components =
                List.of(component(course, null, "JN", 60), component(course, null, "ON", 40));

        validator.validateTopLevelWeights(components);
    }

    @Test
    void validateTopLevelWeights_childComponentsExistButAreNotPassedIn_onlyTopLevelCounted() {
        Course course = course();
        AssessmentComponent jn = component(course, null, "JN", 60);
        // Children are constructed (proving the tree is valid) but, matching the real caller
        // (CourseService.publish uses findByCourse_IdAndParentIsNull), only top-level components
        // are handed to the validator - children must never be double-counted.
        component(course, jn, "JN_ATTENDANCE", 20);
        component(course, jn, "JN_PRACTICAL", 40);
        List<AssessmentComponent> topLevelOnly = List.of(jn, component(course, null, "ON", 40));

        validator.validateTopLevelWeights(topLevelOnly);
    }

    @Test
    void validateTopLevelWeights_doesNotSumTo100_throws() {
        Course course = course();
        List<AssessmentComponent> components =
                List.of(component(course, null, "JN", 50), component(course, null, "ON", 40));

        assertThatThrownBy(() -> validator.validateTopLevelWeights(components))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.ASSESSMENT_WEIGHTS_INVALID);
    }

    @Test
    void validateTopLevelWeights_empty_throws() {
        assertThatThrownBy(() -> validator.validateTopLevelWeights(List.of()))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.ASSESSMENT_WEIGHTS_INVALID);
    }

    @Test
    void validateTopLevelWeights_exposesSumInDetails() {
        Course course = course();
        List<AssessmentComponent> components = List.of(component(course, null, "JN", 70));

        assertThatThrownBy(() -> validator.validateTopLevelWeights(components))
                .isInstanceOfSatisfying(
                        DomainException.class,
                        e -> assertThat(e.details()).containsEntry("sum", "70"));
    }

    private static AssessmentComponent component(
            Course course, AssessmentComponent parent, String code, int points) {
        return new AssessmentComponent(
                course,
                parent,
                code,
                AssessmentComponentKind.CONTINUOUS,
                BigDecimal.valueOf(points),
                null,
                false,
                null,
                0);
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
}
