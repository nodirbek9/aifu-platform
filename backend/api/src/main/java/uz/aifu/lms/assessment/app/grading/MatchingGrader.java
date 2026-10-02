package uz.aifu.lms.assessment.app.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;

/**
 * {@code MATCHING}: {@code answer_options.match_key} (V009 column comment: "pairing key") groups
 * this question's options into correct pairs — every two options sharing the same {@code match_key}
 * are meant to be matched together (a teacher-authoring invariant, not a DB constraint). The schema
 * has no dedicated "paired with" column on the submission side, so the student's pairing is read
 * from {@link QuizAttemptSelectedOption#getOrdinal()}: two selected options submitted with the same
 * ordinal are the pair the student proposed. Each proposed pair that also shares a real {@code
 * match_key} earns {@code points / totalPairs}.
 */
@Component
public class MatchingGrader implements QuestionGrader {

    @Override
    public Set<QuestionType> supports() {
        return Set.of(QuestionType.MATCHING);
    }

    @Override
    public GradeResult grade(GradingContext context) {
        BigDecimal points = context.question().getPoints();
        Map<String, List<UUID>> correctGroups =
                context.options().stream()
                        .filter(o -> o.getMatchKey() != null)
                        .collect(
                                Collectors.groupingBy(
                                        AnswerOption::getMatchKey,
                                        Collectors.mapping(
                                                AnswerOption::getId, Collectors.toList())));
        long totalPairs = correctGroups.values().stream().filter(g -> g.size() == 2).count();
        if (totalPairs == 0) {
            return new GradeResult(BigDecimal.ZERO, false);
        }

        Map<Integer, List<UUID>> submittedGroups =
                context.selectedOptions().stream()
                        .filter(s -> s.getOrdinal() != null)
                        .collect(
                                Collectors.groupingBy(
                                        QuizAttemptSelectedOption::getOrdinal,
                                        Collectors.mapping(
                                                s -> s.getOption().getId(), Collectors.toList())));

        long correctPairs = 0;
        for (List<UUID> submittedPair : submittedGroups.values()) {
            if (submittedPair.size() != 2) {
                continue;
            }
            boolean isCorrectPair =
                    correctGroups.values().stream()
                            .anyMatch(g -> g.size() == 2 && g.containsAll(submittedPair));
            if (isCorrectPair) {
                correctPairs++;
            }
        }

        BigDecimal fraction =
                BigDecimal.valueOf(correctPairs)
                        .divide(BigDecimal.valueOf(totalPairs), 6, RoundingMode.HALF_UP);
        BigDecimal awarded = points.multiply(fraction).setScale(2, RoundingMode.HALF_UP);
        return new GradeResult(awarded, correctPairs == totalPairs);
    }
}
