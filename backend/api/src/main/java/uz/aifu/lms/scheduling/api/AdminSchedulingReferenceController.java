package uz.aifu.lms.scheduling.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.app.AcademicCalendarService;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.HolidayRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.HolidayView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TermParityRequest;
import uz.aifu.lms.scheduling.app.SchedulingReferenceService;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(
        name = "Admin: timetable reference data",
        description = "Rooms, the bell schedule (pair slots), holidays, odd/even week setting")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TIMETABLE_MANAGE')")
public class AdminSchedulingReferenceController {

    private final SchedulingReferenceService reference;
    private final AcademicCalendarService calendar;

    public AdminSchedulingReferenceController(
            SchedulingReferenceService reference, AcademicCalendarService calendar) {
        this.reference = reference;
        this.calendar = calendar;
    }

    // ---- rooms
    @GetMapping("/rooms")
    @Operation(summary = "All rooms")
    public List<RoomView> rooms() {
        return reference.listRooms();
    }

    @PostMapping("/rooms")
    @Operation(summary = "Create a room")
    public ResponseEntity<RoomView> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reference.createRoom(request));
    }

    @PutMapping("/rooms/{id}")
    @Operation(summary = "Update a room")
    public RoomView updateRoom(@PathVariable UUID id, @Valid @RequestBody RoomRequest request) {
        return reference.updateRoom(id, request);
    }

    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "Delete a room no class uses")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        reference.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // ---- bell schedule
    @GetMapping("/pair-slots")
    @Operation(summary = "The bell schedule, in pair order")
    public List<PairSlotView> pairSlots() {
        return reference.listPairSlots();
    }

    @PostMapping("/pair-slots")
    @Operation(summary = "Add a pair to the bell schedule")
    public ResponseEntity<PairSlotView> createPairSlot(
            @Valid @RequestBody PairSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reference.createPairSlot(request));
    }

    @PutMapping("/pair-slots/{id}")
    @Operation(summary = "Change a pair's number or times")
    public PairSlotView updatePairSlot(
            @PathVariable UUID id, @Valid @RequestBody PairSlotRequest request) {
        return reference.updatePairSlot(id, request);
    }

    @DeleteMapping("/pair-slots/{id}")
    @Operation(summary = "Delete a pair no class uses")
    public ResponseEntity<Void> deletePairSlot(@PathVariable UUID id) {
        reference.deletePairSlot(id);
        return ResponseEntity.noContent().build();
    }

    // ---- holidays
    @GetMapping("/holidays")
    @Operation(summary = "Non-teaching days")
    public List<HolidayView> holidays() {
        return reference.listHolidays();
    }

    @PostMapping("/holidays")
    @Operation(summary = "Mark a date as a holiday: classes on it are shown as not held")
    public ResponseEntity<HolidayView> createHoliday(@Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reference.createHoliday(request));
    }

    @DeleteMapping("/holidays/{id}")
    @Operation(summary = "Remove a holiday")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        reference.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    // ---- odd / even weeks
    @PutMapping("/terms/{termId}/first-week-parity")
    @Operation(summary = "Whether the term's first week is odd (toq) or even (juft)")
    public Map<String, Object> setFirstWeekParity(
            @PathVariable UUID termId, @Valid @RequestBody TermParityRequest request) {
        Term term = calendar.changeFirstWeekParity(termId, request.firstWeekParity());
        return Map.of("termId", term.getId(), "firstWeekParity", term.getFirstWeekParity());
    }
}
