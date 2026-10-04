package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {

    List<AnswerOption> findByQuestion_IdOrderByPosition(UUID questionId);

    long countByQuestion_Id(UUID questionId);

    /**
     * The position after the last item: positions may have gaps (a deleted item, a reorder), so
     * counting the items would hand out a position already taken.
     */
    @Query(
            "select coalesce(max(x.position), -1) + 1 from AnswerOption x where x.question.id = :parentId")
    int nextPosition(@Param("parentId") UUID parentId);
}
