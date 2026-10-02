package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.assessment.domain.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {

    List<AnswerOption> findByQuestion_IdOrderByPosition(UUID questionId);

    long countByQuestion_Id(UUID questionId);
}
