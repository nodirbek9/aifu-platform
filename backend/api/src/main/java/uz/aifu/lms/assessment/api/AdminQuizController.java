package uz.aifu.lms.assessment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.api.dto.QuizDtos.ChangeQuizStatusRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.CreateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizDetail;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpdateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpsertQuizTranslationRequest;
import uz.aifu.lms.assessment.app.QuizService;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Quiz administration. {@code QUIZ_MANAGE} is granted to TEACHER (V003), and {@link
 * AccessGuard#requireCourseEditor}/{@link AccessGuard#requireQuizEditor} are what actually restrict
 * a teacher to a course they are assigned to (skills/authorization/SKILL.md L2).
 */
@RestController
@Tag(name = "Teacher: quizzes", description = "Quiz administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminQuizController {

    private final QuizService quizzes;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public AdminQuizController(
            QuizService quizzes,
            AccessGuard accessGuard,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.quizzes = quizzes;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/quizzes")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "List all of a course's quizzes, any status")
    public PageResponse<QuizDetail> list(@PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        Page<Quiz> page = quizzes.listByCourse(courseId, pageableSupport.sanitize(pageable));
        return PageResponse.of(
                page, q -> QuizMapper.toDetail(q, quizzes.listTranslations(q.getId()), "uz"));
    }

    @PostMapping("/api/v1/teacher/courses/{courseId}/quizzes")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Create a quiz")
    public ResponseEntity<QuizDetail> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateQuizRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        Quiz created =
                quizzes.create(courseId, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/teacher/quizzes/" + created.getId()))
                .body(
                        QuizMapper.toDetail(
                                created, quizzes.listTranslations(created.getId()), "uz"));
    }

    @GetMapping("/api/v1/teacher/quizzes/{id}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Get one quiz, any status")
    public QuizDetail get(@PathVariable UUID id) {
        Quiz quiz = quizzes.require(id);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        return QuizMapper.toDetail(quiz, quizzes.listTranslations(id), "uz");
    }

    @PatchMapping("/api/v1/teacher/quizzes/{id}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Update quiz settings")
    public QuizDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuizRequest request,
            HttpServletRequest http) {
        Quiz quiz = quizzes.require(id);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        Quiz updated =
                quizzes.updateDetails(
                        id, request, currentUser.require().id(), http.getRemoteAddr());
        return QuizMapper.toDetail(updated, quizzes.listTranslations(id), "uz");
    }

    @PutMapping("/api/v1/teacher/quizzes/{id}/status")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Publish, archive, or return a quiz to draft")
    public QuizDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeQuizStatusRequest request,
            HttpServletRequest http) {
        Quiz quiz = quizzes.require(id);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        UUID actorId = currentUser.require().id();
        String ip = http.getRemoteAddr();
        Quiz updated =
                switch (request.status()) {
                    case PUBLISHED -> quizzes.publish(id, actorId, ip);
                    case ARCHIVED -> quizzes.archive(id, actorId, ip);
                    case DRAFT -> quizzes.returnToDraft(id, actorId, ip);
                };
        return QuizMapper.toDetail(updated, quizzes.listTranslations(id), "uz");
    }

    @PutMapping("/api/v1/teacher/quizzes/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Upsert one locale's quiz translation")
    public QuizDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertQuizTranslationRequest request,
            HttpServletRequest http) {
        Quiz quiz = quizzes.require(id);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        quizzes.upsertTranslation(
                id, locale, request, currentUser.require().id(), http.getRemoteAddr());
        return QuizMapper.toDetail(quiz, quizzes.listTranslations(id), locale);
    }

    @DeleteMapping("/api/v1/teacher/quizzes/{id}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(
            summary =
                    "Archive a quiz (real delete is never exposed — attempt history references it)")
    public ResponseEntity<Void> archive(@PathVariable UUID id, HttpServletRequest http) {
        Quiz quiz = quizzes.require(id);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        quizzes.archive(id, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
