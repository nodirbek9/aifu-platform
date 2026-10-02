package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.SubjectPrerequisite;

public interface SubjectPrerequisiteRepository extends JpaRepository<SubjectPrerequisite, UUID> {

    @EntityGraph(attributePaths = "prerequisiteSubject")
    List<SubjectPrerequisite> findBySubject_Id(UUID subjectId);

    boolean existsBySubject_IdAndPrerequisiteSubject_Id(UUID subjectId, UUID prerequisiteSubjectId);
}
