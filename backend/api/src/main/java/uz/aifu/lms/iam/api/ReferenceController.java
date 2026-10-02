package uz.aifu.lms.iam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.iam.app.org.OrgReferenceDataService;

/**
 * The university's departments and academic programmes, for the pickers of the admin forms (a
 * group's programme, a teacher's department, a subject's department). Reference data only: it is
 * maintained by migrations, not through the API.
 */
@RestController
@RequestMapping("/api/v1/reference")
@Tag(name = "Reference data", description = "Departments and academic programmes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class ReferenceController {

    public record DepartmentView(UUID id, String code, String name, String nameEn) {}

    public record ProgrammeView(UUID id, String code, String name, String degreeLevel) {}

    private final OrgReferenceDataService reference;

    public ReferenceController(OrgReferenceDataService reference) {
        this.reference = reference;
    }

    @GetMapping("/departments")
    @Operation(summary = "Departments (kafedralar), by name")
    public List<DepartmentView> departments() {
        return reference.listDepartments().stream()
                .map(d -> new DepartmentView(d.getId(), d.getCode(), d.getName(), d.getNameEn()))
                .toList();
    }

    @GetMapping("/programmes")
    @Operation(summary = "Academic programmes (yo'nalishlar), by code")
    public List<ProgrammeView> programmes() {
        return reference.listProgrammes().stream()
                .map(
                        p ->
                                new ProgrammeView(
                                        p.getId(),
                                        p.getCode(),
                                        p.getName(),
                                        p.getDegreeLevel().name()))
                .toList();
    }
}
