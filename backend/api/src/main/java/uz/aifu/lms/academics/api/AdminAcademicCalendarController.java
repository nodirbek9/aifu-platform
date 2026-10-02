package uz.aifu.lms.academics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.api.dto.AcademicCalendarDtos.AcademicYearView;
import uz.aifu.lms.academics.api.dto.AcademicCalendarDtos.CreateAcademicYearRequest;
import uz.aifu.lms.academics.api.dto.AcademicCalendarDtos.CreateTermRequest;
import uz.aifu.lms.academics.api.dto.AcademicCalendarDtos.TermView;
import uz.aifu.lms.academics.app.AcademicCalendarService;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Term;

/**
 * Minimal academic-calendar administration: {@code AcademicYear}/{@code Term} are prerequisites for
 * creating a course, not a full scheduling system (PENDING-INPUTS.md §4).
 */
@RestController
@RequestMapping("/api/v1/admin/academic-years")
@Tag(name = "Admin: academic calendar", description = "Academic years and terms")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('SYSTEM_CONFIG_MANAGE')")
public class AdminAcademicCalendarController {

    private final AcademicCalendarService calendar;

    public AdminAcademicCalendarController(AcademicCalendarService calendar) {
        this.calendar = calendar;
    }

    @GetMapping
    @Operation(summary = "List academic years, most recent first")
    public List<AcademicYearView> listYears() {
        return calendar.listAcademicYears().stream()
                .map(AdminAcademicCalendarController::toYearView)
                .toList();
    }

    @PostMapping
    @Operation(summary = "Create an academic year")
    public ResponseEntity<AcademicYearView> createYear(
            @Valid @RequestBody CreateAcademicYearRequest request) {
        AcademicYear year = calendar.createAcademicYear(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/academic-years/" + year.getId()))
                .body(toYearView(year));
    }

    @PutMapping("/{id}/current")
    @Operation(summary = "Mark this the current academic year (unmarks any previous one)")
    public AcademicYearView markCurrent(@PathVariable UUID id) {
        return toYearView(calendar.markCurrent(id));
    }

    @GetMapping("/{id}/terms")
    @Operation(summary = "List terms in an academic year, in ordinal order")
    public List<TermView> listTerms(@PathVariable UUID id) {
        return calendar.listTerms(id).stream()
                .map(AdminAcademicCalendarController::toTermView)
                .toList();
    }

    @PostMapping("/{id}/terms")
    @Operation(summary = "Create a term within an academic year")
    public ResponseEntity<TermView> createTerm(
            @PathVariable UUID id, @Valid @RequestBody CreateTermRequest request) {
        Term term = calendar.createTerm(id, request);
        return ResponseEntity.created(
                        URI.create("/api/v1/admin/academic-years/" + id + "/terms/" + term.getId()))
                .body(toTermView(term));
    }

    private static AcademicYearView toYearView(AcademicYear year) {
        return new AcademicYearView(
                year.getId(),
                year.getCode(),
                year.getStartsOn(),
                year.getEndsOn(),
                year.isCurrent(),
                year.isPlaceholder());
    }

    private static TermView toTermView(Term term) {
        return new TermView(
                term.getId(),
                term.getAcademicYear().getId(),
                term.getAcademicYear().getCode(),
                term.getOrdinal(),
                term.getName(),
                term.getStartsOn(),
                term.getEndsOn(),
                term.isPlaceholder());
    }
}
