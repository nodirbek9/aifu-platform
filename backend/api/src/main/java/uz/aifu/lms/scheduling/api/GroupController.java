package uz.aifu.lms.scheduling.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupStudentView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.scheduling.app.SchedulingReferenceService;

/** Read access to groups and rosters for staff (teachers and admins). */
@RestController
@RequestMapping("/api/v1/groups")
@Tag(name = "Groups", description = "Student groups and rosters (staff)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('GROUP_READ')")
public class GroupController {

    private final SchedulingReferenceService reference;

    public GroupController(SchedulingReferenceService reference) {
        this.reference = reference;
    }

    @GetMapping
    @Operation(summary = "All groups with their student counts")
    public List<GroupView> list() {
        return reference.listGroups();
    }

    @GetMapping("/{id}")
    @Operation(summary = "One group")
    public GroupView get(@PathVariable UUID id) {
        return reference.getGroup(id);
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "The group's roster, by name")
    public List<GroupStudentView> students(@PathVariable UUID id) {
        return reference.listStudents(id);
    }
}
