package uz.aifu.lms.enrollment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.BulkEnrollRequest;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.BulkEnrollResultView;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.CreateEnrollmentRequest;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.CsvImportRowView;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentDetail;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.GroupEnrollRequest;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.user.StudentDirectory;

/**
 * Enrollment administration. {@code ENROLLMENT_MANAGE}/{@code ENROLLMENT_READ} are ADMIN-only today
 * (V003), but every course-scoped read/write here ALSO calls {@link
 * AccessGuard#requireCourseEditor} — defense in depth for the day a permission like this is
 * broadened to TEACHER, exactly the lesson from {@code AdminCourseController}
 * (skills/authorization/SKILL.md L2).
 */
@RestController
@RequestMapping("/api/v1/admin/enrollments")
@Tag(name = "Admin: enrollments", description = "Enrollment administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminEnrollmentController {

    private final EnrollmentService enrollments;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;
    private final StudentDirectory students;

    public AdminEnrollmentController(
            EnrollmentService enrollments,
            AccessGuard accessGuard,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser,
            StudentDirectory students) {
        this.enrollments = enrollments;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
        this.students = students;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    @Operation(summary = "List a course's enrollments, optionally filtered by status")
    public PageResponse<EnrollmentSummary> listByCourse(
            @RequestParam UUID courseId,
            @RequestParam(required = false) EnrollmentStatus status,
            Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        var statuses = status == null ? null : EnumSet.of(status);
        var page = enrollments.listByCourse(courseId, statuses, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, EnrollmentMapper::toSummary);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    @Operation(summary = "Get one enrollment with its full transition history")
    public EnrollmentDetail get(@PathVariable UUID id) {
        Enrollment enrollment = enrollments.require(id);
        accessGuard.requireCourseEditor(enrollment.getCourse().getId());
        return EnrollmentMapper.toDetail(enrollment, enrollments);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_MANAGE')")
    @Operation(summary = "Enroll one student in a course")
    public ResponseEntity<EnrollmentDetail> create(
            @Valid @RequestBody CreateEnrollmentRequest request, HttpServletRequest http) {
        accessGuard.requireCourseEditor(request.courseId());
        Enrollment created =
                enrollments.enroll(
                        request.courseId(),
                        request.studentUserId(),
                        request.source(),
                        currentUser.require().id(),
                        clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/admin/enrollments/" + created.getId()))
                .body(EnrollmentMapper.toDetail(created, enrollments));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('ENROLLMENT_MANAGE')")
    @Operation(
            summary =
                    "Enroll a batch of students; each student's outcome is reported independently")
    public List<BulkEnrollResultView> bulkCreate(
            @Valid @RequestBody BulkEnrollRequest request, HttpServletRequest http) {
        accessGuard.requireCourseEditor(request.courseId());
        return enrollments
                .bulkEnroll(
                        request.courseId(),
                        request.studentUserIds(),
                        request.source(),
                        currentUser.require().id(),
                        clientIp(http))
                .stream()
                .map(
                        o ->
                                new BulkEnrollResultView(
                                        o.studentUserId(),
                                        o.success(),
                                        o.enrollmentId(),
                                        o.errorCode(),
                                        o.errorMessage()))
                .toList();
    }

    @PostMapping("/by-group")
    @PreAuthorize("hasAuthority('ENROLLMENT_MANAGE')")
    @Operation(
            summary = "Enroll every student of a group into a course",
            description =
                    "Each student's outcome is reported independently, as in /bulk; a student who"
                            + " is already enrolled is a failed row with ENROLLMENT_DUPLICATE, not a"
                            + " request-level error. An empty group answers an empty list.")
    public List<BulkEnrollResultView> enrollGroup(
            @Valid @RequestBody GroupEnrollRequest request, HttpServletRequest http) {
        accessGuard.requireCourseEditor(request.courseId());
        List<UUID> studentIds =
                students.studentsOfGroup(request.groupCode().trim()).stream()
                        .map(StudentDirectory.StudentRef::userId)
                        .toList();
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return enrollments
                .bulkEnroll(
                        request.courseId(),
                        studentIds,
                        request.source(),
                        currentUser.require().id(),
                        clientIp(http))
                .stream()
                .map(
                        o ->
                                new BulkEnrollResultView(
                                        o.studentUserId(),
                                        o.success(),
                                        o.enrollmentId(),
                                        o.errorCode(),
                                        o.errorMessage()))
                .toList();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ENROLLMENT_MANAGE')")
    @Operation(
            summary = "Bulk-enroll from a registrar CSV export",
            description =
                    "One student number per line (an optional 'student_number' header row is "
                            + "skipped). Each row's outcome is reported independently, mirroring "
                            + "/bulk; an unresolvable student number is reported as a failed row, "
                            + "never as a request-level error.")
    public List<CsvImportRowView> importCsv(
            @RequestParam UUID courseId,
            @RequestParam(required = false) EnrollmentSource source,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        List<String> studentNumbers = readCsvLines(file);
        return enrollments
                .importByStudentNumbers(
                        courseId,
                        studentNumbers,
                        source,
                        currentUser.require().id(),
                        clientIp(http))
                .stream()
                .map(
                        o ->
                                new CsvImportRowView(
                                        o.identifier(),
                                        o.success(),
                                        o.studentUserId(),
                                        o.enrollmentId(),
                                        o.errorCode(),
                                        o.errorMessage()))
                .toList();
    }

    /**
     * One student number per non-blank line; a literal {@code student_number} header is skipped.
     */
    private static List<String> readCsvLines(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "CSV file is empty");
        }
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                String cell = line.split(",", 2)[0].trim();
                if (first) {
                    first = false;
                    if (cell.equalsIgnoreCase("student_number")) {
                        continue;
                    }
                }
                if (!cell.isEmpty()) {
                    lines.add(cell);
                }
            }
            return lines;
        } catch (IOException e) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "Could not read the uploaded CSV file");
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ENROLLMENT_MANAGE')")
    @Operation(
            summary = "Transition an enrollment's status",
            description =
                    "PENDING->ACTIVE, ACTIVE<->SUSPENDED, ACTIVE->COMPLETED|FAILED|WITHDRAWN, "
                            + "and WITHDRAWN|FAILED->ACTIVE (REINSTATED, an admin correction, not a new attempt).")
    public EnrollmentDetail changeStatus(
            @PathVariable UUID id,
            @RequestParam EnrollmentStatus status,
            @RequestParam(required = false) String reason,
            HttpServletRequest http) {
        Enrollment existing = enrollments.require(id);
        accessGuard.requireCourseEditor(existing.getCourse().getId());
        UUID actorId = currentUser.require().id();
        String ip = clientIp(http);
        Enrollment updated =
                switch (status) {
                    case ACTIVE -> activateResumeOrReinstate(existing, id, actorId, ip, reason);
                    case SUSPENDED -> enrollments.suspend(id, actorId, ip, reason);
                    case COMPLETED -> enrollments.complete(id, actorId, ip, reason);
                    case FAILED -> enrollments.fail(id, actorId, ip, reason);
                    case WITHDRAWN -> enrollments.withdraw(id, actorId, ip, reason);
                    case PENDING ->
                            throw Exceptions.rule(
                                    ErrorCode.ENROLLMENT_INVALID_TRANSITION,
                                    "An enrollment cannot be moved back to PENDING");
                };
        return EnrollmentMapper.toDetail(updated, enrollments);
    }

    /**
     * Target ACTIVE is reachable from three different source states, each a different service call
     * — pick the one matching the enrollment's current state; the domain layer ({@link
     * Enrollment#activate()}/{@code resume}/{@code reinstate}) still validates it regardless.
     */
    private Enrollment activateResumeOrReinstate(
            Enrollment existing, UUID id, UUID actorId, String ip, String reason) {
        if (existing.getStatus() == EnrollmentStatus.SUSPENDED) {
            return enrollments.resume(id, actorId, ip, reason);
        }
        if (existing.getStatus().isTerminal()) {
            return enrollments.reinstate(id, actorId, ip, reason);
        }
        return enrollments.activate(id, actorId, ip, reason);
    }

    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return http.getRemoteAddr();
    }
}
