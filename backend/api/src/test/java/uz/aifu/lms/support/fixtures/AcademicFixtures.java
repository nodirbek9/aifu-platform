package uz.aifu.lms.support.fixtures;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.academics.persistence.AcademicYearRepository;
import uz.aifu.lms.academics.persistence.TermRepository;
import uz.aifu.lms.iam.domain.Department;
import uz.aifu.lms.iam.persistence.DepartmentRepository;

/**
 * Builds persisted org-structure/calendar reference rows ({@link Department}, {@link Term}) that
 * V002/V005 do NOT seed (only {@code roles}/{@code permissions}/{@code grade_scales} are baseline
 * migration data; departments and terms are dev-seed-only per CLAUDE.md §5) — so subject/course
 * integration tests can set up their prerequisites in one line without depending on the academic
 * calendar admin flow they are not testing (skills/testing/SKILL.md), same rationale as {@link
 * UserFixtures}.
 */
@Component
public class AcademicFixtures {

    private final DepartmentRepository departments;
    private final AcademicYearRepository academicYears;
    private final TermRepository terms;

    public AcademicFixtures(
            DepartmentRepository departments,
            AcademicYearRepository academicYears,
            TermRepository terms) {
        this.departments = departments;
        this.academicYears = academicYears;
        this.terms = terms;
    }

    @Transactional
    public Department department() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return departments.save(
                new Department(
                        "DEPT-" + suffix,
                        "Fixture Department " + suffix,
                        "Fixture Department " + suffix));
    }

    @Transactional
    public Term term() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        AcademicYear academicYear =
                academicYears.save(
                        new AcademicYear(
                                "AY-" + suffix,
                                LocalDate.of(2025, 9, 1),
                                LocalDate.of(2026, 6, 30),
                                true));
        return terms.save(
                new Term(
                        academicYear,
                        1,
                        "Term " + suffix,
                        LocalDate.of(2025, 9, 1),
                        LocalDate.of(2026, 1, 15),
                        true));
    }
}
