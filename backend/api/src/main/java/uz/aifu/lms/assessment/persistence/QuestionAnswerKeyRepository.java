package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;

public interface QuestionAnswerKeyRepository extends JpaRepository<QuestionAnswerKey, UUID> {

    List<QuestionAnswerKey> findByQuestion_Id(UUID questionId);

    void deleteByQuestion_Id(UUID questionId);
}
