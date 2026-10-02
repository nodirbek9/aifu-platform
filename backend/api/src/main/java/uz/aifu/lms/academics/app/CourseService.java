package uz.aifu.lms.academics.app;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.api.dto.CourseDtos.AssignTeacherRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateAssessmentComponentRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateCourseRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.SetSessionAllocationRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpdateCourseDeliveryRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpdateEnrollmentWindowRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpsertCourseTranslationRequest;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseSessionAllocation;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.academics.domain.CourseTeacher;
import uz.aifu.lms.academics.domain.CourseTranslation;
import uz.aifu.lms.academics.domain.GradeScale;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.academics.persistence.AssessmentComponentRepository;
import uz.aifu.lms.academics.persistence.CourseRepository;
import uz.aifu.lms.academics.persistence.CourseSessionAllocationRepository;
import uz.aifu.lms.academics.persistence.CourseTeacherRepository;
import uz.aifu.lms.academics.persistence.CourseTranslationRepository;
import uz.aifu.lms.academics.persistence.GradeScaleRepository;
import uz.aifu.lms.academics.persistence.SubjectRepository;
import uz.aifu.lms.academics.persistence.TermRepository;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.app.org.OrgReferenceDataService;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.AcademicProgramme;
import uz.aifu.lms.iam.domain.User;

/** Course delivery administration (docs/domain/domain-model.md §1, Phase 3). */
@Service
public class CourseService {

    private final CourseRepository courses;
    private final CourseTranslationRepository translations;
    private final CourseTeacherRepository courseTeachers;
    private final CourseSessionAllocationRepository sessionAllocations;
    private final AssessmentComponentRepository assessmentComponents;
    private final SubjectRepository subjects;
    private final TermRepository terms;
    private final GradeScaleRepository gradeScales;
    private final OrgReferenceDataService orgReferenceData;
    private final UserService users;
    private final ContentSanitizer sanitizer;
    private final TranslationCompletenessValidator translationValidator;
    private final AssessmentWeightValidator weightValidator;
    private final HourBudgetValidator hourBudgetValidator;
    private final AuditService audit;
    private final Clock clock;

    public CourseService(
            CourseRepository courses,
            CourseTranslationRepository translations,
            CourseTeacherRepository courseTeachers,
            CourseSessionAllocationRepository sessionAllocations,
            AssessmentComponentRepository assessmentComponents,
            SubjectRepository subjects,
            TermRepository terms,
            GradeScaleRepository gradeScales,
            OrgReferenceDataService orgReferenceData,
            UserService users,
            ContentSanitizer sanitizer,
            TranslationCompletenessValidator translationValidator,
            AssessmentWeightValidator weightValidator,
            HourBudgetValidator hourBudgetValidator,
            AuditService audit,
            Clock clock) {
        this.courses = courses;
        this.translations = translations;
        this.courseTeachers = courseTeachers;
        this.sessionAllocations = sessionAllocations;
        this.assessmentComponents = assessmentComponents;
        this.subjects = subjects;
        this.terms = terms;
        this.gradeScales = gradeScales;
        this.orgReferenceData = orgReferenceData;
        this.users = users;
        this.sanitizer = sanitizer;
        this.translationValidator = translationValidator;
        this.weightValidator = weightValidator;
        this.hourBudgetValidator = hourBudgetValidator;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<Course> search(
            String query,
            java.util.Collection<CourseStatus> statuses,
            UUID subjectId,
            UUID termId,
            String locale,
            Pageable pageable) {
        String pattern = normalizePattern(query);
        LocaleCode resolvedLocale = LocaleCode.parseOrDefault(locale);
        return courses.search(
                statuses, subjectId, termId, resolvedLocale.code(), pattern, pageable);
    }

    @Transactional(readOnly = true)
    public Course require(UUID id) {
        return courses.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> Exceptions.notFound("Course", id));
    }

    /** Courses the given teacher is assigned to, any status — backs {@code /teacher/courses}. */
    @Transactional(readOnly = true)
    public Page<Course> findForTeacher(UUID teacherUserId, Pageable pageable) {
        Set<UUID> courseIds = courseTeachers.findCourseIdsByTeacherId(teacherUserId);
        if (courseIds.isEmpty()) {
            return org.springframework.data.domain.Page.empty(pageable);
        }
        return courses.findByIdInAndDeletedAtIsNull(courseIds, pageable);
    }

    @Transactional
    public Course create(CreateCourseRequest request, UUID actorId, String ip) {
        Subject subject =
                subjects.findByIdAndDeletedAtIsNull(request.subjectId())
                        .orElseThrow(() -> Exceptions.notFound("Subject", request.subjectId()));
        Term term =
                terms.findById(request.termId())
                        .orElseThrow(() -> Exceptions.notFound("Term", request.termId()));
        String cohortCode =
                request.cohortCode() == null || request.cohortCode().isBlank()
                        ? "DEFAULT"
                        : request.cohortCode().trim();
        if (courses.existsBySubject_IdAndTerm_IdAndCohortCodeAndDeletedAtIsNull(
                subject.getId(), term.getId(), cohortCode)) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "A course for this subject/term/cohort already exists");
        }
        GradeScale gradeScale =
                request.gradeScaleCode() == null
                        ? defaultGradeScale()
                        : requireGradeScale(request.gradeScaleCode());
        Course course =
                new Course(
                        subject,
                        term,
                        cohortCode,
                        request.credits(),
                        request.totalHours(),
                        LocaleCode.parseOrDefault(request.instructionLanguage()),
                        gradeScale);
        Course saved = courses.save(course);
        audit.record(
                AuditAction.COURSE_CREATED,
                actorId,
                "course",
                saved.getId(),
                ip,
                null,
                Map.of("subjectCode", subject.getCode(), "term", term.getName()));
        return saved;
    }

    @Transactional
    public Course updateDelivery(
            UUID id, UpdateCourseDeliveryRequest request, UUID actorId, String ip) {
        Course course = require(id);
        AcademicProgramme programme =
                request.programmeId() == null ? null : requireProgramme(request.programmeId());
        course.updateDeliveryDetails(
                request.cohortCode(),
                request.weeklyContactHours(),
                request.deliveryMode(),
                request.assessmentForm(),
                programme);
        audit.record(AuditAction.COURSE_UPDATED, actorId, "course", id, ip, null);
        return course;
    }

    @Transactional
    public Course updateRequiredLocales(UUID id, Set<String> locales, UUID actorId, String ip) {
        Course course = require(id);
        String[] normalized =
                locales.stream()
                        .map(l -> LocaleCode.require(l).code())
                        .distinct()
                        .toArray(String[]::new);
        course.updateRequiredLocales(normalized);
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                id,
                ip,
                null,
                Map.of("transition", "required-locales"));
        return course;
    }

    @Transactional
    public Course updateEnrollmentWindow(
            UUID id, UpdateEnrollmentWindowRequest request, UUID actorId, String ip) {
        Course course = require(id);
        course.updateEnrollmentWindow(
                request.enrollmentOpensAt(), request.enrollmentClosesAt(), request.maxEnrollment());
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                id,
                ip,
                null,
                Map.of("transition", "enrollment-window"));
        return course;
    }

    @Transactional
    public Course publish(UUID id, UUID actorId, String ip) {
        Course course = require(id);
        translationValidator.requireLocales(
                Set.of(course.getRequiredLocales()),
                availableLocales(id),
                "Course " + course.getSubject().getCode());
        List<AssessmentComponent> topLevel =
                assessmentComponents.findByCourse_IdAndParentIsNull(id);
        weightValidator.validateTopLevelWeights(topLevel);
        hourBudgetValidator.validateForPublish(course, sessionAllocations.findByCourse_Id(id));
        course.publish(clock.instant());
        audit.record(AuditAction.COURSE_PUBLISHED, actorId, "course", id, ip, null);
        return course;
    }

    @Transactional
    public Course close(UUID id, UUID actorId, String ip) {
        Course course = require(id);
        course.close();
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                id,
                ip,
                null,
                Map.of("transition", "closed"));
        return course;
    }

    @Transactional
    public Course archive(UUID id, UUID actorId, String ip) {
        Course course = require(id);
        course.archive();
        audit.record(AuditAction.COURSE_ARCHIVED, actorId, "course", id, ip, null);
        return course;
    }

    @Transactional
    public Course returnToDraft(UUID id, UUID actorId, String ip) {
        Course course = require(id);
        course.returnToDraft();
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                id,
                ip,
                null,
                Map.of("transition", "to-draft"));
        return course;
    }

    @Transactional
    public void softDelete(UUID id, UUID actorId, String ip) {
        Course course = require(id);
        course.softDelete(clock.instant());
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                id,
                ip,
                null,
                Map.of("transition", "deleted"));
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<CourseTranslation> listTranslations(UUID courseId) {
        return translations.findAllByCourseId(courseId);
    }

    /**
     * Batched for catalogue lists — controllers must not call the repository directly
     * (skills/architecture/SKILL.md rule 6).
     */
    @Transactional(readOnly = true)
    public Map<UUID, List<CourseTranslation>> listTranslationsForCourses(
            java.util.Collection<UUID> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByCourseIds(courseIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getCourse().getId()));
    }

    @Transactional
    public CourseTranslation upsertTranslation(
            UUID courseId,
            String locale,
            UpsertCourseTranslationRequest request,
            UUID actorId,
            String ip) {
        Course course = require(courseId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        String title = sanitizer.sanitizePlainText(request.title());
        String description = sanitizer.sanitizeRichText(request.description());
        CourseTranslation translation =
                translations
                        .findByCourseIdAndLocale(courseId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new CourseTranslation(
                                                course, resolvedLocale.code(), title, description));
        translation.update(title, description);
        CourseTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "course",
                courseId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- teachers

    @Transactional(readOnly = true)
    public List<CourseTeacher> listTeachers(UUID courseId) {
        return courseTeachers.findByCourse_Id(courseId);
    }

    @Transactional
    public CourseTeacher assignTeacher(
            UUID courseId, AssignTeacherRequest request, UUID actorId, String ip) {
        Course course = require(courseId);
        User teacher = users.require(request.teacherUserId());
        if (courseTeachers.existsByCourse_IdAndTeacher_Id(courseId, teacher.getId())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "This teacher is already assigned to the course");
        }
        CourseTeacher assignment =
                new CourseTeacher(course, teacher, request.teacherRole(), actorId);
        CourseTeacher saved = courseTeachers.save(assignment);
        audit.record(
                AuditAction.COURSE_TEACHER_ASSIGNED,
                actorId,
                "course",
                courseId,
                ip,
                null,
                Map.of("teacherUserId", teacher.getId().toString()));
        return saved;
    }

    @Transactional
    public void removeTeacher(UUID courseId, UUID teacherUserId, UUID actorId, String ip) {
        if (!courseTeachers.existsByCourse_IdAndTeacher_Id(courseId, teacherUserId)) {
            throw Exceptions.notFound("CourseTeacher", teacherUserId);
        }
        courseTeachers.deleteByCourse_IdAndTeacher_Id(courseId, teacherUserId);
        audit.record(
                AuditAction.COURSE_TEACHER_REMOVED,
                actorId,
                "course",
                courseId,
                ip,
                null,
                Map.of("teacherUserId", teacherUserId.toString()));
    }

    // ---------------------------------------------------------------- session allocations

    @Transactional(readOnly = true)
    public List<CourseSessionAllocation> listSessionAllocations(UUID courseId) {
        return sessionAllocations.findByCourse_Id(courseId);
    }

    @Transactional
    public CourseSessionAllocation setSessionAllocation(
            UUID courseId, SetSessionAllocationRequest request, UUID actorId, String ip) {
        Course course = require(courseId);
        CourseSessionAllocation allocation =
                sessionAllocations.findByCourse_Id(courseId).stream()
                        .filter(a -> a.getSessionType() == request.sessionType())
                        .findFirst()
                        .orElseGet(
                                () ->
                                        new CourseSessionAllocation(
                                                course,
                                                request.sessionType(),
                                                request.allocatedHours()));
        allocation.setAllocatedHours(request.allocatedHours());
        CourseSessionAllocation saved = sessionAllocations.save(allocation);
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                courseId,
                ip,
                null,
                Map.of("transition", "session-allocation"));
        return saved;
    }

    // ---------------------------------------------------------------- assessment components

    @Transactional(readOnly = true)
    public List<AssessmentComponent> listAssessmentComponents(UUID courseId) {
        return assessmentComponents.findByCourse_IdOrderByDisplayOrder(courseId);
    }

    /**
     * Read-only lookup for other modules (e.g. {@code curriculum} grading an activity against a
     * component) — never inject {@code AssessmentComponentRepository} directly (CLAUDE.md §2).
     */
    @Transactional(readOnly = true)
    public AssessmentComponent requireAssessmentComponent(UUID id) {
        return assessmentComponents
                .findById(id)
                .orElseThrow(() -> Exceptions.notFound("AssessmentComponent", id));
    }

    @Transactional
    public AssessmentComponent addAssessmentComponent(
            UUID courseId, CreateAssessmentComponentRequest request, UUID actorId, String ip) {
        Course course = require(courseId);
        if (assessmentComponents.existsByCourse_IdAndCodeIgnoreCase(courseId, request.code())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "A component with this code already exists on the course");
        }
        AssessmentComponent parent =
                request.parentComponentId() == null
                        ? null
                        : assessmentComponents
                                .findById(request.parentComponentId())
                                .orElseThrow(
                                        () ->
                                                Exceptions.notFound(
                                                        "AssessmentComponent",
                                                        request.parentComponentId()));
        AssessmentComponent component =
                new AssessmentComponent(
                        course,
                        parent,
                        request.code(),
                        request.kind(),
                        request.maxPoints(),
                        request.minPassPercent(),
                        request.blocksFinal(),
                        request.assessmentForm(),
                        request.displayOrder());
        AssessmentComponent saved = assessmentComponents.save(component);
        audit.record(
                AuditAction.COURSE_UPDATED,
                actorId,
                "course",
                courseId,
                ip,
                null,
                Map.of("transition", "assessment-component-added"));
        return saved;
    }

    // ---------------------------------------------------------------- helpers

    private Set<String> availableLocales(UUID courseId) {
        return translations.findAllByCourseId(courseId).stream()
                .map(CourseTranslation::getLocale)
                .collect(java.util.stream.Collectors.toSet());
    }

    private GradeScale requireGradeScale(String code) {
        return gradeScales
                .findByCode(code)
                .orElseThrow(() -> Exceptions.notFound("GradeScale", code));
    }

    private GradeScale defaultGradeScale() {
        return gradeScales
                .findByCode("AIFU_100_5")
                .orElseThrow(() -> Exceptions.notFound("GradeScale", "AIFU_100_5"));
    }

    private AcademicProgramme requireProgramme(UUID id) {
        return orgReferenceData.requireAcademicProgramme(id);
    }

    private static String normalizePattern(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return "%" + query.trim().toLowerCase() + "%";
    }
}
