package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOptionId;

public interface QuizAttemptSelectedOptionRepository
        extends JpaRepository<QuizAttemptSelectedOption, QuizAttemptSelectedOptionId> {

    @EntityGraph(attributePaths = "option")
    List<QuizAttemptSelectedOption> findByAttemptQuestion_IdOrderByOrdinal(UUID attemptQuestionId);

    void deleteByAttemptQuestion_Id(UUID attemptQuestionId);
}
