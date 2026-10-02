package uz.aifu.lms.scheduling.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CheckResult;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CreateTimetableRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadBulkRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadRow;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WeekView;
import uz.aifu.lms.scheduling.app.TeachingLoadService;
import uz.aifu.lms.scheduling.app.TimetableQueryService;
import uz.aifu.lms.scheduling.app.TimetableQueryService.Filter;
import uz.aifu.lms.scheduling.app.TimetableService;

/**
 * The timetable builder (spec A-02, A-04). Students and teachers never call these: they read the
 * published timetable through {@link TimetableController}.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(
        name = "Admin: timetable",
        description = "Build, check and publish the timetable; teaching load")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TIMETABLE_MANAGE')")
public class AdminTimetableController {

    private final TimetableService timetables;
    private final TimetableQueryService queries;
    private final TeachingLoadService load;
    private final CurrentUserProvider currentUser;
    private final SchedulingWeb web;

    public AdminTimetableController(
            TimetableService timetables,
            TimetableQueryService queries,
            TeachingLoadService load,
            CurrentUserProvider currentUser,
            SchedulingWeb web) {
        this.timetables = timetables;
        this.queries = queries;
        this.load = load;
        this.currentUser = currentUser;
        this.web = web;
    }

    // ------------------------------------------------------------------ timetables

    @GetMapping("/timetables")
    @Operation(summary = "Timetables, newest first; optionally of one term")
    public List<TimetableView> list(@RequestParam(required = false) UUID termId) {
        return queries.listTimetables(termId);
    }

    @GetMapping("/timetables/{id}")
    @Operation(summary = "One timetable")
    public TimetableView get(@PathVariable UUID id) {
        return queries.toTimetableView(queries.requireTimetable(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/timetables")
    @Operation(summary = "Create a draft timetable for a term, optionally as a copy of another one")
    public ResponseEntity<TimetableView> create(
            @Valid @RequestBody CreateTimetableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queries.toTimetableView(timetables.create(request)));
    }

    @PutMapping("/timetables/{id}/name")
    @Operation(summary = "Rename a timetable")
    public TimetableView rename(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return queries.toTimetableView(timetables.rename(id, body.get("name")));
    }

    @PostMapping("/timetables/{id}/publish")
    @Operation(
            summary = "Publish: students and teachers now see this timetable",
            description = "The timetable previously published for the same term is archived.")
    public TimetableView publish(@PathVariable UUID id, HttpServletRequest http) {
        return queries.toTimetableView(
                timetables.publish(id, currentUser.require().id(), SchedulingWeb.clientIp(http)));
    }

    @PostMapping("/timetables/{id}/archive")
    @Operation(summary = "Archive a timetable (read-only from then on)")
    public TimetableView archive(@PathVariable UUID id) {
        return queries.toTimetableView(timetables.archive(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/timetables/{id}")
    @Operation(summary = "Delete a draft timetable")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        timetables.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ classes

    @GetMapping("/timetables/{id}/entries")
    @Operation(
            summary =
                    "The builder grid: every recurring class, or those of one group / teacher / room")
    public List<EntryView> entries(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return queries.listEntries(
                id, new Filter(groupId, teacherId, roomId), web.locale(locale, http));
    }

    @PostMapping("/timetables/{id}/entries/check")
    @Operation(
            summary = "Dry run: would this class clash, and what warnings does it have?",
            description =
                    "Pass editedEntryId when checking a move of an existing class, so it does not"
                            + " clash with itself. Nothing is saved.")
    public CheckResult check(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID editedEntryId,
            @Valid @RequestBody EntryRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return timetables.check(id, request, editedEntryId, web.locale(locale, http));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/timetables/{id}/entries")
    @Operation(
            summary = "Add a class",
            description =
                    "409 TIMETABLE_CONFLICT with details.conflicts when the teacher, room or a group is"
                            + " busy; with details.warnings when only warnings exist and force is not set.")
    public ResponseEntity<EntryView> createEntry(
            @PathVariable UUID id,
            @Valid @RequestBody EntryRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        timetables.createEntry(
                                id,
                                request,
                                currentUser.require().id(),
                                SchedulingWeb.clientIp(http),
                                web.locale(locale, http)));
    }

    @PutMapping("/timetable-entries/{entryId}")
    @Operation(summary = "Edit or move a class (same body as create; same conflict rules)")
    public EntryView updateEntry(
            @PathVariable UUID entryId,
            @Valid @RequestBody EntryRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return timetables.updateEntry(
                entryId,
                request,
                currentUser.require().id(),
                SchedulingWeb.clientIp(http),
                web.locale(locale, http));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/timetable-entries/{entryId}")
    @Operation(summary = "Remove a class from the timetable")
    public ResponseEntity<Void> deleteEntry(@PathVariable UUID entryId, HttpServletRequest http) {
        timetables.deleteEntry(entryId, currentUser.require().id(), SchedulingWeb.clientIp(http));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ one-off changes

    @GetMapping("/timetable-entries/{entryId}/exceptions")
    @Operation(summary = "One-off changes of a class, by date")
    public List<ExceptionView> exceptions(@PathVariable UUID entryId) {
        timetables.requireEntry(entryId);
        return queries.listExceptions(entryId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/timetable-entries/{entryId}/exceptions")
    @Operation(
            summary = "Cancel, move or replace one occurrence of a class",
            description = "The teacher(s) and the students of the groups get a notification.")
    public ResponseEntity<ExceptionView> addException(
            @PathVariable UUID entryId,
            @Valid @RequestBody ExceptionRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        timetables.addException(
                                entryId,
                                request,
                                currentUser.require().id(),
                                SchedulingWeb.clientIp(http),
                                web.locale(locale, http)));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/timetable-exceptions/{exceptionId}")
    @Operation(summary = "Undo a one-off change")
    public ResponseEntity<Void> removeException(@PathVariable UUID exceptionId) {
        timetables.removeException(exceptionId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ preview and load

    @GetMapping("/timetables/{id}/week")
    @Operation(summary = "A concrete week of any timetable (preview a draft before publishing)")
    public WeekView week(
            @PathVariable UUID id,
            @Parameter(description = "Any date of the week; default today")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return queries.weekOf(
                id,
                web.dateOrToday(date),
                new Filter(groupId, teacherId, roomId),
                web.locale(locale, http));
    }

    @GetMapping("/timetables/{id}/load")
    @Operation(
            summary =
                    "Planned hours (yuklama) against the hours this timetable schedules over the term",
            description =
                    "A pair counts as 2 academic hours. Rows without a plan have plannedHours = null.")
    public List<LoadRow> loadReport(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return queries.load(id, new Filter(groupId, teacherId, null), web.locale(locale, http));
    }

    @GetMapping("/teaching-load")
    @Operation(summary = "The planned teaching load of a term")
    public List<LoadRow> plannedLoad(
            @RequestParam UUID termId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return queries.plannedLoad(termId, web.locale(locale, http));
    }

    @PutMapping("/teaching-load")
    @Operation(
            summary = "Create or update load lines (one per course, group and session type)",
            description = "The client parses the department's Excel sheet and sends its rows here.")
    public Map<String, Object> upsertLoad(
            @RequestParam UUID termId, @Valid @RequestBody LoadBulkRequest request) {
        return Map.of("written", load.upsert(termId, request.items()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/teaching-load/{itemId}")
    @Operation(summary = "Delete a load line")
    public ResponseEntity<Void> deleteLoad(@PathVariable UUID itemId) {
        load.delete(itemId);
        return ResponseEntity.noContent().build();
    }
}
