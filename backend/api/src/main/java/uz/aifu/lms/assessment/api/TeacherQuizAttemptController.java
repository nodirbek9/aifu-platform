package uz.aifu.lms.assessment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StudentAttemptSummary;
import uz.aifu.lms.assessment.app.AttemptService;
import uz.aifu.lms.assessment.app.QuizService;
import uz.aifu.lms.assessment.domain.QuizAttempt;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A teacher's own quiz's attempt roster. {@code QUIZ_ATTEMPT_REVIEW} is granted to TEACHER (V003);
 * {@link AccessGuard#requireCourseEditor} is what actually restricts a teacher to a course they are
 * assigned to (skills/authorization/SKILL.md L2).
 */
@RestController
@RequestMapping("/api/v1/teacher/quizzes/{quizId}/attempts")
@Tag(name = "Teacher: quiz attempts", description = "A teacher's own quiz's attempt roster")
@SecurityRequirement(name = "bearerAuth")
public class TeacherQuizAttemptController {

    private final AttemptService attempts;
    private final QuizService quizzes;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public TeacherQuizAttemptController(
            AttemptService attempts,
            QuizService quizzes,
            AccessGuard accessGuard,
            PageableSupport pageableSupport) {
        this.attempts = attempts;
        this.quizzes = quizzes;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT_REVIEW')")
    @Operation(
            summary = "List every attempt at a quiz the caller teaches (or any quiz, for an admin)")
    public PageResponse<StudentAttemptSummary> list(@PathVariable UUID quizId, Pageable pageable) {
        var quiz = quizzes.require(quizId);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        Page<QuizAttempt> page = attempts.listForQuiz(quizId, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, AttemptMapper::toStudentSummary);
    }
}
