package uz.aifu.lms.assessment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.AnswerKeyView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.AnswerOptionView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerKeyRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerOptionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.QuestionDetail;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.ReorderAnswerOptionsRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.ReorderQuestionsRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpdateAnswerOptionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpdateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpsertAnswerOptionTranslationRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpsertQuestionTranslationRequest;
import uz.aifu.lms.assessment.app.QuestionService;
import uz.aifu.lms.assessment.app.QuizService;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.AnswerOptionTranslation;
import uz.aifu.lms.assessment.domain.Question;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;

/** Question (+ answer option + answer key) administration. {@code QUIZ_MANAGE}, TEACHER (V003). */
@RestController
@Tag(name = "Teacher: questions", description = "Question administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminQuestionController {

    private final QuestionService questions;
    private final QuizService quizzes;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;
    private final ObjectMapper objectMapper;

    public AdminQuestionController(
            QuestionService questions,
            QuizService quizzes,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser,
            ObjectMapper objectMapper) {
        this.questions = questions;
        this.quizzes = quizzes;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/v1/teacher/quizzes/{quizId}/questions")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "List a quiz's questions")
    public PageResponse<QuestionDetail> list(@PathVariable UUID quizId, Pageable pageable) {
        var quiz = quizzes.require(quizId);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        Page<Question> page = questions.listByQuiz(quizId, pageable);
        return PageResponse.of(page, this::toDetail);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/quizzes/{quizId}/questions")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Add a question to a quiz")
    public ResponseEntity<QuestionDetail> create(
            @PathVariable UUID quizId,
            @Valid @RequestBody CreateQuestionRequest request,
            HttpServletRequest http) {
        var quiz = quizzes.require(quizId);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        Question created =
                questions.create(quizId, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/teacher/questions/" + created.getId()))
                .body(toDetail(created));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/api/v1/teacher/quizzes/{quizId}/questions/reorder")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Reorder a quiz's questions")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID quizId,
            @Valid @RequestBody ReorderQuestionsRequest request,
            HttpServletRequest http) {
        var quiz = quizzes.require(quizId);
        accessGuard.requireCourseEditor(quiz.getCourse().getId());
        questions.reorder(
                quizId,
                request.orderedQuestionIds(),
                currentUser.require().id(),
                http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/teacher/questions/{id}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Get one question, including correct answers")
    public QuestionDetail get(@PathVariable UUID id) {
        Question question = requireEditable(id);
        return toDetail(question);
    }

    @PatchMapping("/api/v1/teacher/questions/{id}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Update a question's points/difficulty/settings")
    public QuestionDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuestionRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        Question updated =
                questions.updateDetails(
                        id, request, currentUser.require().id(), http.getRemoteAddr());
        return toDetail(updated);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/questions/{id}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Delete a question (blocked if any attempt already answered it)")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest http) {
        requireEditable(id);
        questions.delete(id, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/teacher/questions/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Upsert one locale's question translation")
    public QuestionDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertQuestionTranslationRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        questions.upsertTranslation(
                id, locale, request, currentUser.require().id(), http.getRemoteAddr());
        return toDetail(questions.require(id));
    }

    // ---------------------------------------------------------------- answer options

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/questions/{id}/options")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Add an answer option")
    public ResponseEntity<AnswerOptionView> addOption(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAnswerOptionRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        AnswerOption option =
                questions.addOption(id, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/teacher/options/" + option.getId()))
                .body(toOptionView(option, List.of()));
    }

    @PatchMapping("/api/v1/teacher/options/{optionId}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Update an answer option's correctness/matchKey")
    public AnswerOptionView updateOption(
            @PathVariable UUID optionId,
            @Valid @RequestBody UpdateAnswerOptionRequest request,
            HttpServletRequest http) {
        // Authorize first: checking after the write both let a non-editor's change commit and
        // failed with LazyInitializationException (500) on the detached question proxy.
        accessGuard.requireCourseEditor(questions.courseIdOfOption(optionId));
        AnswerOption option =
                questions.updateOption(
                        optionId, request, currentUser.require().id(), http.getRemoteAddr());
        return toOptionView(option, List.of());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/api/v1/teacher/questions/{id}/options/reorder")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Reorder a question's answer options")
    public ResponseEntity<Void> reorderOptions(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderAnswerOptionsRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        questions.reorderOptions(
                id, request.orderedOptionIds(), currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/options/{optionId}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Remove an answer option")
    public ResponseEntity<Void> removeOption(@PathVariable UUID optionId, HttpServletRequest http) {
        // Course-editor check was missing: any QUIZ_MANAGE holder could delete in any course.
        accessGuard.requireCourseEditor(questions.courseIdOfOption(optionId));
        questions.removeOption(optionId, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/teacher/options/{optionId}/translations/{locale}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Upsert one locale's answer option translation")
    public AnswerOptionView upsertOptionTranslation(
            @PathVariable UUID optionId,
            @PathVariable String locale,
            @Valid @RequestBody UpsertAnswerOptionTranslationRequest request,
            HttpServletRequest http) {
        // Same as updateOption: authorize before the write, inside a transaction.
        accessGuard.requireCourseEditor(questions.courseIdOfOption(optionId));
        AnswerOptionTranslation translation =
                questions.upsertOptionTranslation(
                        optionId,
                        locale,
                        request,
                        currentUser.require().id(),
                        http.getRemoteAddr());
        return toOptionView(translation.getOption(), List.of(translation));
    }

    // ---------------------------------------------------------------- answer keys

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/questions/{id}/answer-keys")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Add an answer key (SHORT_ANSWER/NUMERIC)")
    public ResponseEntity<AnswerKeyView> addAnswerKey(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAnswerKeyRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        var key =
                questions.addAnswerKey(
                        id, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new AnswerKeyView(
                                key.getId(),
                                key.getMatchMode(),
                                key.getExpectedValue(),
                                key.getTolerance(),
                                key.getLocale()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/answer-keys/{keyId}")
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Operation(summary = "Remove an answer key")
    public ResponseEntity<Void> removeAnswerKey(@PathVariable UUID keyId, HttpServletRequest http) {
        accessGuard.requireCourseEditor(questions.courseIdOfAnswerKey(keyId));
        questions.removeAnswerKey(keyId, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- helpers

    private Question requireEditable(UUID questionId) {
        Question question = questions.require(questionId);
        accessGuard.requireCourseEditor(question.getQuiz().getCourse().getId());
        return question;
    }

    private QuestionDetail toDetail(Question question) {
        List<AnswerOption> options = questions.listOptions(question.getId());
        Map<UUID, List<AnswerOptionTranslation>> optionTranslations =
                questions.listOptionTranslationsForOptions(
                        options.stream().map(AnswerOption::getId).toList());
        return QuestionMapper.toDetail(
                question,
                questions.listTranslations(question.getId()),
                "uz",
                "uz",
                options,
                optionTranslations,
                questions.listAnswerKeys(question.getId()),
                objectMapper);
    }

    private AnswerOptionView toOptionView(
            AnswerOption option, List<AnswerOptionTranslation> translations) {
        var match =
                translations.stream()
                        .findFirst()
                        .map(t -> new String[] {t.getText(), t.getFeedback()})
                        .orElse(new String[] {null, null});
        return new AnswerOptionView(
                option.getId(),
                option.getPosition(),
                option.isCorrect(),
                option.getMatchKey(),
                match[0],
                match[1]);
    }
}
