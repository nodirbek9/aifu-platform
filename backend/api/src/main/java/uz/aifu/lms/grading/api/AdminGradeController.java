package uz.aifu.lms.grading.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.api.dto.GradeDtos.CourseGradebookRow;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.GradeEntryView;
import uz.aifu.lms.grading.api.dto.GradeDtos.RecordManualGradeRequest;
import uz.aifu.lms.grading.api.dto.GradeDtos.StudentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.VoidGradeRequest;
import uz.aifu.lms.grading.app.GradeEntryService;
import uz.aifu.lms.grading.domain.GradeEntry;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.user.StudentDirectory;

/**
 * A teacher's gradebook for one course: manual entry/override, void, and the current roster. {@code
 * GRADE_READ}/{@code GRADE_WRITE} are granted to TEACHER (V003); {@link
 * AccessGuard#requireCourseEditor} is what actually restricts a teacher to a course they are
 * assigned to (skills/authorization/SKILL.md L2).
 */
@RestController
@Tag(name = "Teacher: grades", description = "Manual entry, override, and the course grade roster")
@SecurityRequirement(name = "bearerAuth")
public class AdminGradeController {

    private static final int GRADEBOOK_LIMIT = 500;

    private final GradeEntryService grades;
    private final EnrollmentService enrollments;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;
    private final StudentDirectory studentDirectory;

    public AdminGradeController(
            GradeEntryService grades,
            EnrollmentService enrollments,
            AccessGuard accessGuard,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser,
            StudentDirectory studentDirectory) {
        this.grades = grades;
        this.enrollments = enrollments;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
        this.studentDirectory = studentDirectory;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/grades")
    @PreAuthorize("hasAuthority('GRADE_READ')")
    @Operation(summary = "Current grade summary for every enrolled student in this course")
    public PageResponse<StudentGradeSummary> roster(
            @PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        // Current entries drive derived state on enrollments; the roster reads enrollments
        // directly so a student with zero entries yet still appears (not admitted, no debt).
        Page<Enrollment> page =
                enrollments.listByCourse(courseId, List.of(), pageableSupport.sanitize(pageable));
        return PageResponse.of(page, GradeMapper::toStudentSummary);
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/gradebook")
    @PreAuthorize("hasAuthority('GRADE_READ')")
    @Operation(
            summary = "The gradebook of a course: every student with every component, alphabetical",
            description =
                    "One row per enrolled student (at most "
                            + GRADEBOOK_LIMIT
                            + "), each with the full grade summary the single-student endpoint"
                            + " returns: earned per component, the final score, its grade,"
                            + " admission, debt, current entries and history.")
    public List<CourseGradebookRow> gradebook(
            @PathVariable UUID courseId, @RequestParam(required = false) String locale) {
        accessGuard.requireCourseEditor(courseId);
        String code = LocaleCode.find(locale).orElse(LocaleCode.DEFAULT).code();
        return enrollments
                .listByCourse(
                        courseId,
                        List.of(),
                        PageRequest.of(0, GRADEBOOK_LIMIT, Sort.by("student.fullName")))
                .stream()
                .map(
                        e ->
                                new CourseGradebookRow(
                                        e.getStudent().getId(),
                                        e.getStudent().getFullName(),
                                        studentDirectory
                                                .groupCodeOf(e.getStudent().getId())
                                                .orElse(null),
                                        GradeMapper.toSummary(
                                                grades.gradebookFor(e.getId(), code))))
                .toList();
    }

    @GetMapping("/api/v1/teacher/enrollments/{id}/grades")
    @PreAuthorize("hasAuthority('GRADE_READ')")
    @Operation(summary = "One student's full current grade summary, teacher view")
    public EnrollmentGradeSummary get(@PathVariable UUID id) {
        Enrollment enrollment = enrollments.require(id);
        accessGuard.requireCourseEditor(enrollment.getCourse().getId());
        return GradeMapper.toSummary(grades.gradebookFor(id, LocaleCode.DEFAULT.code()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/enrollments/{id}/grades")
    @PreAuthorize("hasAuthority('GRADE_WRITE')")
    @Operation(summary = "Record or override a manual grade for one assessment component")
    public ResponseEntity<GradeEntryView> record(
            @PathVariable UUID id,
            @Valid @RequestBody RecordManualGradeRequest request,
            HttpServletRequest http) {
        Enrollment enrollment = enrollments.require(id);
        accessGuard.requireCourseEditor(enrollment.getCourse().getId());
        UUID evaluatorId =
                request.evaluatorUserId() == null
                        ? currentUser.require().id()
                        : request.evaluatorUserId();
        GradeEntry entry =
                grades.recordManualGrade(
                        id,
                        request.assessmentComponentId(),
                        request.score(),
                        request.maxScore(),
                        evaluatorId,
                        currentUser.require().id(),
                        http.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/teacher/grades/" + entry.getId()))
                .body(GradeMapper.toView(entry));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/v1/teacher/grades/{id}/void")
    @PreAuthorize("hasAuthority('GRADE_WRITE')")
    @Operation(summary = "Void a published grade entry (append-only — never deleted, ADR-011)")
    public ResponseEntity<Void> voidEntry(
            @PathVariable UUID id,
            @Valid @RequestBody VoidGradeRequest request,
            HttpServletRequest http) {
        GradeEntry entry = grades.require(id);
        // entry.getEnrollment().getId() is safe on a lazy proxy (no fetch needed for an FK-backed
        // id); re-resolving through EnrollmentService avoids touching entry.getEnrollment().
        // getCourse() after this read-only transaction has already closed
        // (spring.jpa.open-in-view: false — see coding.api.AdminCodingTaskContentController's
        // defect #6, Phase 8, for the exact failure mode this sidesteps).
        Enrollment enrollment = enrollments.require(entry.getEnrollment().getId());
        accessGuard.requireCourseEditor(enrollment.getCourse().getId());
        grades.voidEntry(id, request.reason(), currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
