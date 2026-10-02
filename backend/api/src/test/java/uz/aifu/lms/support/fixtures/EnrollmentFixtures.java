package uz.aifu.lms.support.fixtures;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateAssessmentComponentRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateCourseRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.SetSessionAllocationRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpsertCourseTranslationRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.CreateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpsertSubjectTranslationRequest;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.app.SubjectService;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectKind;

/**
 * Builds a real, PUBLISHED, enrollable {@link Course} — through the actual {@code academics} app
 * services (not raw inserts), so it genuinely satisfies {@code CourseService.publish}'s
 * translation-completeness/weight-sum/hour-budget validators (Phase 3) rather than silently
 * drifting from them. Enrollment tests need this repeatedly; {@code SubjectCatalogueIT}/{@code
 * CourseDeliveryIT} only needed it once each, which is why it was inlined there instead.
 */
@Component
public class EnrollmentFixtures {

    private final AcademicFixtures academicFixtures;
    private final SubjectService subjects;
    private final CourseService courses;

    public EnrollmentFixtures(
            AcademicFixtures academicFixtures, SubjectService subjects, CourseService courses) {
        this.academicFixtures = academicFixtures;
        this.subjects = subjects;
        this.courses = courses;
    }

    /**
     * A PUBLISHED course with no enrollment-window/capacity restriction and {@code totalHours}
     * fully reconciled (one LECTURE allocation, one top-level JN=100 assessment component).
     */
    @Transactional
    public Course publishedCourse(int totalHours) {
        UUID actor = UUID.randomUUID();
        var department = academicFixtures.department();
        var term = academicFixtures.term();
        String code =
                "SUB"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 10)
                                .toUpperCase();

        Subject subject =
                subjects.create(
                        new CreateSubjectRequest(
                                code, department.getId(), SubjectKind.MANDATORY, "uz"),
                        actor,
                        null);
        subjects.upsertTranslation(
                subject.getId(),
                "uz",
                new UpsertSubjectTranslationRequest(
                        "Fixture subject", null, null, null, null, null),
                actor,
                null);
        subjects.publish(subject.getId(), actor, null);

        Course course =
                courses.create(
                        new CreateCourseRequest(
                                subject.getId(),
                                term.getId(),
                                null,
                                BigDecimal.valueOf(6),
                                totalHours,
                                "uz",
                                null),
                        actor,
                        null);
        courses.upsertTranslation(
                course.getId(),
                "uz",
                new UpsertCourseTranslationRequest("Fixture course", null),
                actor,
                null);
        courses.addAssessmentComponent(
                course.getId(),
                new CreateAssessmentComponentRequest(
                        null,
                        "JN",
                        AssessmentComponentKind.CONTINUOUS,
                        BigDecimal.valueOf(100),
                        null,
                        false,
                        null,
                        0),
                actor,
                null);
        courses.setSessionAllocation(
                course.getId(),
                new SetSessionAllocationRequest(SessionType.LECTURE, totalHours),
                actor,
                null);
        return courses.publish(course.getId(), actor, null);
    }
}
