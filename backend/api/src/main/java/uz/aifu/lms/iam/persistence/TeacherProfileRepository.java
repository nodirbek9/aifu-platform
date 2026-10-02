package uz.aifu.lms.iam.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.iam.domain.TeacherProfile;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, UUID> {
    Optional<TeacherProfile> findByEmployeeNumber(String employeeNumber);

    /**
     * {@code department} is LAZY; callers that read {@code getDepartment()} outside the transaction
     * (e.g. to build a DTO) must fetch it eagerly here rather than via plain {@code findById}.
     */
    @EntityGraph(attributePaths = "department")
    Optional<TeacherProfile> findWithDepartmentByUserId(UUID userId);
}
