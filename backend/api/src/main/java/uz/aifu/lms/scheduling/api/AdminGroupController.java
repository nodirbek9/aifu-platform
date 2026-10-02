package uz.aifu.lms.scheduling.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupStudentView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupStudentsRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.scheduling.app.SchedulingReferenceService;

@RestController
@RequestMapping("/api/v1/admin/groups")
@Tag(name = "Admin: groups", description = "Student groups (\"guruh\") and their rosters")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('GROUP_MANAGE')")
public class AdminGroupController {

    private final SchedulingReferenceService reference;
    private final CurrentUserProvider currentUser;

    public AdminGroupController(
            SchedulingReferenceService reference, CurrentUserProvider currentUser) {
        this.reference = reference;
        this.currentUser = currentUser;
    }

    @PostMapping
    @Operation(summary = "Create a group")
    public ResponseEntity<GroupView> create(@Valid @RequestBody GroupRequest request) {
        GroupView created = reference.createGroup(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/groups/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a group; renaming keeps its students")
    public GroupView update(@PathVariable UUID id, @Valid @RequestBody GroupRequest request) {
        return reference.updateGroup(id, request);
    }

    @PutMapping("/{id}/archived")
    @Operation(summary = "Archive or re-activate a group")
    public GroupView setArchived(@PathVariable UUID id, @RequestParam boolean value) {
        return reference.setGroupArchived(id, value);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an empty, unused group")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reference.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/students")
    @Operation(summary = "Put students into this group (moves them out of their previous group)")
    public List<GroupStudentView> addStudents(
            @PathVariable UUID id,
            @Valid @RequestBody GroupStudentsRequest request,
            HttpServletRequest http) {
        return reference.addStudents(
                id, request.userIds(), currentUser.require().id(), SchedulingWeb.clientIp(http));
    }

    @DeleteMapping("/{id}/students/{userId}")
    @Operation(summary = "Remove a student from this group")
    public ResponseEntity<Void> removeStudent(
            @PathVariable UUID id, @PathVariable UUID userId, HttpServletRequest http) {
        reference.removeStudent(
                id, userId, currentUser.require().id(), SchedulingWeb.clientIp(http));
        return ResponseEntity.noContent().build();
    }
}
