package uz.aifu.lms.iam.app.org;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.domain.AcademicProgramme;
import uz.aifu.lms.iam.domain.Department;
import uz.aifu.lms.iam.persistence.AcademicProgrammeRepository;
import uz.aifu.lms.iam.persistence.DepartmentRepository;

/**
 * Read-only lookup for org-structure reference data ({@link Department}, {@link AcademicProgramme})
 * owned by {@code iam}. Other modules (e.g. {@code academics}) must resolve these through here
 * rather than injecting {@code iam.persistence} repositories directly (skills/architecture/SKILL.md
 * rule 6).
 */
@Service
public class OrgReferenceDataService {

    private final DepartmentRepository departments;
    private final AcademicProgrammeRepository programmes;

    public OrgReferenceDataService(
            DepartmentRepository departments, AcademicProgrammeRepository programmes) {
        this.departments = departments;
        this.programmes = programmes;
    }

    @Transactional(readOnly = true)
    public Department requireDepartment(UUID id) {
        return departments.findById(id).orElseThrow(() -> Exceptions.notFound("Department", id));
    }

    @Transactional(readOnly = true)
    public AcademicProgramme requireAcademicProgramme(UUID id) {
        return programmes
                .findById(id)
                .orElseThrow(() -> Exceptions.notFound("AcademicProgramme", id));
    }
}
