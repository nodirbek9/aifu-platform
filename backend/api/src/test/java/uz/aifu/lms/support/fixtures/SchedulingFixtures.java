package uz.aifu.lms.support.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.api.dto.CourseDtos.AssignTeacherRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateCourseRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpsertCourseTranslationRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.CreateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpsertSubjectTranslationRequest;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.app.SubjectService;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.academics.domain.TeacherRole;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.academics.persistence.AcademicYearRepository;
import uz.aifu.lms.academics.persistence.TermRepository;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.persistence.StudentProfileRepository;

/**
 * Timetable test set-up. Every call to {@link #term()} gets its own far-future year, so the "which
 * term is this date in" lookup of one test never sees another test's term in the shared container
 * (the generic {@link AcademicFixtures#term()} terms all cover the same dates).
 */
@Component
public class SchedulingFixtures {

    private static final AtomicInteger NEXT_YEAR = new AtomicInteger(2040);

    private final AcademicYearRepository academicYears;
    private final TermRepository terms;
    private final AcademicFixtures academicFixtures;
    private final UserFixtures userFixtures;
    private final SubjectService subjects;
    private final CourseService courses;
    private final StudentProfileRepository studentProfiles;

    public SchedulingFixtures(
            AcademicYearRepository academicYears,
            TermRepository terms,
            AcademicFixtures academicFixtures,
            UserFixtures userFixtures,
            SubjectService subjects,
            CourseService courses,
            StudentProfileRepository studentProfiles) {
        this.academicYears = academicYears;
        this.terms = terms;
        this.academicFixtures = academicFixtures;
        this.userFixtures = userFixtures;
        this.subjects = subjects;
        this.courses = courses;
        this.studentProfiles = studentProfiles;
    }

    /** A term from 1 September to 15 January of a year no other test uses; first week is ODD. */
    @Transactional
    public Term term() {
        int year = NEXT_YEAR.getAndIncrement();
        String suffix = unique(8);
        AcademicYear academicYear =
                academicYears.save(
                        new AcademicYear(
                                "AY-" + suffix,
                                LocalDate.of(year, 9, 1),
                                LocalDate.of(year + 1, 6, 30),
                                true));
        return terms.save(
                new Term(
                        academicYear,
                        1,
                        "Term " + suffix,
                        LocalDate.of(year, 9, 1),
                        LocalDate.of(year + 1, 1, 15),
                        true));
    }

    /** A course of the given term with an Uzbek title; optionally with its teacher assigned. */
    @Transactional
    public Course course(Term term, User teacherOrNull) {
        UUID actor = UUID.randomUUID();
        String code = "SCH" + unique(10).toUpperCase();
        Subject subject =
                subjects.create(
                        new CreateSubjectRequest(
                                code,
                                academicFixtures.department().getId(),
                                SubjectKind.MANDATORY,
                                "uz"),
                        actor,
                        null);
        subjects.upsertTranslation(
                subject.getId(),
                "uz",
                new UpsertSubjectTranslationRequest(
                        "Fixture subject", null, null, null, null, null),
                actor,
                null);
        Course course =
                courses.create(
                        new CreateCourseRequest(
                                subject.getId(),
                                term.getId(),
                                null,
                                BigDecimal.valueOf(6),
                                150,
                                "uz",
                                null),
                        actor,
                        null);
        courses.upsertTranslation(
                course.getId(),
                "uz",
                new UpsertCourseTranslationRequest("Dasturlash " + code, null),
                actor,
                null);
        if (teacherOrNull != null) {
            courses.assignTeacher(
                    course.getId(),
                    new AssignTeacherRequest(teacherOrNull.getId(), TeacherRole.LEAD),
                    teacherOrNull.getId(), // assigned_by must be a real user
                    null);
        }
        return course;
    }

    /** An ACTIVE student with a profile; {@code groupCode} may be null (no group yet). */
    @Transactional
    public User student(String groupCode) {
        User user = userFixtures.activeUser(RoleCode.STUDENT);
        studentProfiles.save(new StudentProfile(user, "S" + unique(10), null, groupCode, null));
        return user;
    }

    public static String unique(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
