package uz.aifu.lms.announcement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.announcement.api.dto.AnnouncementDtos.AnnouncementView;
import uz.aifu.lms.announcement.api.dto.AnnouncementDtos.CreateAnnouncementRequest;
import uz.aifu.lms.announcement.app.AnnouncementService;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.i18n.LocaleResolver;

/** Announcements to a course or a group (product spec T-11). */
@RestController
@Tag(name = "Announcements", description = "Messages to the students of a course or a group")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {

    private final AnnouncementService announcements;
    private final PageableSupport pageableSupport;
    private final LocaleResolver localeResolver;

    public AnnouncementController(
            AnnouncementService announcements,
            PageableSupport pageableSupport,
            LocaleResolver localeResolver) {
        this.announcements = announcements;
        this.pageableSupport = pageableSupport;
        this.localeResolver = localeResolver;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/announcements")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "Post an announcement to a course or a group",
            description =
                    "A course: its teachers and admins. A group: the study office or the group's"
                            + " curator. Every recipient gets an ANNOUNCEMENT notification.")
    public ResponseEntity<AnnouncementView> post(
            @Valid @RequestBody CreateAnnouncementRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(announcements.post(request, locale(locale, http)));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/announcements/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_WRITE')")
    @Operation(summary = "Delete an announcement (its author, or the study office)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        announcements.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/announcements/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "The announcements meant for me, newest first",
            description =
                    "A student: their current courses and their group. A teacher: the courses they"
                            + " teach and what they wrote. The study office: everything.")
    public PageResponse<AnnouncementView> mine(
            Pageable pageable,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return PageResponse.of(
                announcements.mine(pageableSupport.sanitize(pageable), locale(locale, http)));
    }

    @GetMapping("/api/v1/courses/{courseId}/announcements")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    @Operation(summary = "The announcements of one course, newest first")
    public PageResponse<AnnouncementView> ofCourse(
            @PathVariable UUID courseId,
            Pageable pageable,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return PageResponse.of(
                announcements.ofCourse(
                        courseId, pageableSupport.sanitize(pageable), locale(locale, http)));
    }

    private LocaleCode locale(String explicit, HttpServletRequest http) {
        return localeResolver.preferred(explicit, http, null, null);
    }
}
