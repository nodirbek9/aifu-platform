package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.LearningOutcome;

public interface LearningOutcomeRepository extends JpaRepository<LearningOutcome, UUID> {

    @Query(
            "select o from LearningOutcome o where o.subject.id = :subjectId order by o.displayOrder")
    List<LearningOutcome> findAllBySubjectIdOrderByDisplayOrder(@Param("subjectId") UUID subjectId);

    boolean existsBySubject_IdAndCodeIgnoreCase(UUID subjectId, String code);
}
