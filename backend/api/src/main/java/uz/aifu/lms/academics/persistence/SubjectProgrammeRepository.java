package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.SubjectProgramme;
import uz.aifu.lms.academics.domain.SubjectProgrammeId;

public interface SubjectProgrammeRepository
        extends JpaRepository<SubjectProgramme, SubjectProgrammeId> {

    @EntityGraph(attributePaths = "programme")
    List<SubjectProgramme> findBySubject_Id(UUID subjectId);
}
