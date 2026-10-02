package uz.aifu.lms.assessment.app.grading;

import java.util.Set;
import uz.aifu.lms.assessment.domain.QuestionType;

/**
 * One grader per {@link QuestionType} (or a family of closely related ones) — "adding a question
 * type is a new enum value plus a grader, never a rewrite of the attempt lifecycle" (mirrors {@code
 * curriculum.app.content.ContentBlockPayloadValidator}'s extension-point shape).
 *
 * <p>Grading is always deterministic and always server-side: a grader never receives anything the
 * student could have tampered with beyond their own recorded response — the correct answers ({@link
 * uz.aifu.lms.assessment.domain.AnswerOption#isCorrect()}, {@link
 * uz.aifu.lms.assessment.domain.QuestionAnswerKey}) are read straight from the database, never
 * echoed back from the client.
 */
public interface QuestionGrader {

    Set<QuestionType> supports();

    GradeResult grade(GradingContext context);
}
