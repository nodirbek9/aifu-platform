package uz.aifu.lms.assessment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptDetail;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptSummary;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.RecordAnswerRequest;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.ServedOptionView;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.ServedQuestionView;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StartAttemptRequest;
import uz.aifu.lms.assessment.app.AttemptService;
import uz.aifu.lms.assessment.app.AttemptView;
import uz.aifu.lms.assessment.app.QuestionService;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.AnswerOptionTranslation;
import uz.aifu.lms.assessment.domain.AttemptStatus;
import uz.aifu.lms.assessment.domain.Question;
import uz.aifu.lms.assessment.domain.QuestionTranslation;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.domain.QuizAttempt;
import uz.aifu.lms.assessment.domain.QuizAttemptQuestion;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.idempotency.IdempotencyKeys;
import uz.aifu.lms.common.security.CurrentUserProvider;

/**
 * A student's own quiz attempts. Ownership is "the caller's own enrollment" — no {@code
 * AccessGuard}, resolved entirely inside {@link AttemptService} (ADR-012's reasoning, reused).
 */
@RestController
@Tag(
        name = "My quiz attempts",
        description = "Starting, answering, and submitting the caller's own attempts")
@SecurityRequirement(name = "bearerAuth")
public class QuizAttemptController {

    private final AttemptService attempts;
    private final QuestionService questions;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;
    private final Clock clock;

    public QuizAttemptController(
            AttemptService attempts,
            QuestionService questions,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser,
            Clock clock) {
        this.attempts = attempts;
        this.questions = questions;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @PostMapping("/api/v1/quizzes/{quizId}/attempts")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT_CREATE')")
    @Operation(summary = "Start a new attempt for a published quiz the caller is enrolled to take")
    public ResponseEntity<AttemptDetail> start(
            @PathVariable UUID quizId, @RequestBody(required = false) StartAttemptRequest request) {
        String locale = request == null ? null : request.locale();
        UUID activityId = request == null ? null : request.activityId();
        AttemptView view = attempts.start(quizId, currentUser.require().id(), locale, activityId);
        return ResponseEntity.status(201).body(toDetail(view, locale));
    }

    @PostMapping("/api/v1/attempts/{attemptId}/questions/{questionId}/answer")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT_CREATE')")
    @Operation(summary = "Record (or overwrite) the caller's response to one served question")
    public ServedQuestionView answer(
            @PathVariable UUID attemptId,
            @PathVariable UUID questionId,
            @RequestBody RecordAnswerRequest request) {
        QuizAttemptQuestion attemptQuestion =
                attempts.recordAnswer(attemptId, questionId, currentUser.require().id(), request);
        String defaultLocale =
                attemptQuestion.getAttempt().getQuiz().getCourse().getInstructionLanguage().code();
        return toServedQuestionView(attemptQuestion, false, defaultLocale, defaultLocale);
    }

    @PostMapping("/api/v1/attempts/{attemptId}/submit")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT_CREATE')")
    @Operation(summary = "Submit an attempt for grading. Idempotency-Key honoured (CLAUDE.md §9).")
    public AttemptDetail submit(
            @PathVariable UUID attemptId,
            @RequestHeader(value = IdempotencyKeys.HEADER, required = false)
                    String idempotencyKey) {
        QuizAttempt attempt =
                attempts.submit(attemptId, currentUser.require().id(), idempotencyKey);
        return toDetail(
                attempts.require(attemptId, currentUser.require().id()), attempt.getLocale());
    }

    @GetMapping("/api/v1/attempts/{attemptId}")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT_SELF_READ')")
    @Operation(summary = "Get the caller's own attempt, with served questions and responses")
    public AttemptDetail get(
            @PathVariable UUID attemptId,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        AttemptView view = attempts.require(attemptId, currentUser.require().id());
        return toDetail(view, QuizController.resolveLocale(locale, acceptLanguage));
    }

    @GetMapping("/api/v1/quizzes/{quizId}/attempts/me")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT_SELF_READ')")
    @Operation(summary = "List the caller's own attempts at one quiz, most recent first")
    public PageResponse<AttemptSummary> listOwn(@PathVariable UUID quizId, Pageable pageable) {
        Page<QuizAttempt> page =
                attempts.listOwn(
                        quizId, currentUser.require().id(), pageableSupport.sanitize(pageable));
        return PageResponse.of(page, AttemptMapper::toSummary);
    }

    // ---------------------------------------------------------------- reveal-policy-aware mapping

    private AttemptDetail toDetail(AttemptView view, String locale) {
        QuizAttempt attempt = view.attempt();
        Quiz quiz = attempt.getQuiz();
        boolean revealed = isRevealed(quiz, attempt);
        String resolvedLocale = locale == null ? LocaleCode.DEFAULT.code() : locale;
        String defaultLocale = quiz.getCourse().getInstructionLanguage().code();
        List<ServedQuestionView> questionViews =
                view.servedQuestions().stream()
                        .map(
                                aq ->
                                        toServedQuestionView(
                                                aq, revealed, resolvedLocale, defaultLocale))
                        .toList();
        return new AttemptDetail(
                attempt.getId(),
                quiz.getId(),
                attempt.getAttemptNumber(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getMaxScore(),
                attempt.getPercent(),
                attempt.getPassed(),
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                attempt.getSubmittedAt(),
                attempt.getGradedAt(),
                questionViews);
    }

    private boolean isRevealed(Quiz quiz, QuizAttempt attempt) {
        if (attempt.getStatus() != AttemptStatus.GRADED) {
            return false;
        }
        Instant now = clock.instant();
        return switch (quiz.getRevealPolicy()) {
            case NEVER -> false;
            case IMMEDIATELY, AFTER_ATTEMPT -> true;
            case AFTER_LAST_ATTEMPT -> {
                int maxAttempt =
                        attempts.maxAttemptNumber(quiz.getId(), attempt.getEnrollment().getId());
                yield quiz.getMaxAttempts() != null && maxAttempt >= quiz.getMaxAttempts();
            }
            case AFTER_DUE_DATE -> {
                var activity = attempt.getActivity();
                yield activity != null
                        && activity.getDueAt() != null
                        && now.isAfter(activity.getDueAt());
            }
        };
    }

    private ServedQuestionView toServedQuestionView(
            QuizAttemptQuestion attemptQuestion,
            boolean revealed,
            String resolvedLocale,
            String defaultLocale) {
        Question question = attemptQuestion.getQuestion();
        List<AnswerOption> options = attempts.optionsFor(question.getId());
        Map<UUID, List<AnswerOptionTranslation>> optionTranslations =
                questions.listOptionTranslationsForOptions(
                        options.stream().map(AnswerOption::getId).toList());
        List<QuestionTranslation> questionTranslations =
                questions.listTranslations(question.getId());
        var resolved = resolveQuestion(questionTranslations, resolvedLocale, defaultLocale);

        List<ServedOptionView> optionViews =
                options.stream()
                        .map(
                                o ->
                                        new ServedOptionView(
                                                o.getId(),
                                                resolveOptionText(
                                                        optionTranslations.getOrDefault(
                                                                o.getId(), List.of()),
                                                        resolvedLocale,
                                                        defaultLocale)))
                        .toList();
        List<UUID> selectedIds =
                attempts.selectedOptionsFor(attemptQuestion.getId()).stream()
                        .map(s -> s.getOption().getId())
                        .toList();

        return new ServedQuestionView(
                question.getId(),
                attemptQuestion.getServedPosition(),
                question.getQuestionType(),
                attemptQuestion.getPointsPossible(),
                resolved[0],
                resolved[1],
                optionViews,
                attemptQuestion.getResponseText(),
                attemptQuestion.getResponseNumber(),
                selectedIds,
                revealed ? attemptQuestion.getPointsAwarded() : null,
                revealed ? attemptQuestion.getCorrect() : null,
                revealed ? resolved[2] : null);
    }

    private static String[] resolveQuestion(
            List<QuestionTranslation> translations, String resolvedLocale, String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(resolvedLocale))
                .findFirst()
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst())
                .or(() -> translations.stream().findFirst())
                .map(t -> new String[] {t.getPrompt(), t.getHint(), t.getExplanation()})
                .orElse(new String[] {null, null, null});
    }

    private static String resolveOptionText(
            List<AnswerOptionTranslation> translations,
            String resolvedLocale,
            String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(resolvedLocale))
                .findFirst()
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst())
                .or(() -> translations.stream().findFirst())
                .map(AnswerOptionTranslation::getText)
                .orElse(null);
    }
}
