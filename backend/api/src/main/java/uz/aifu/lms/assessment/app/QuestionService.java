package uz.aifu.lms.assessment.app;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerKeyRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerOptionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpdateAnswerOptionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpdateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpsertAnswerOptionTranslationRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpsertQuestionTranslationRequest;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.AnswerOptionTranslation;
import uz.aifu.lms.assessment.domain.Question;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;
import uz.aifu.lms.assessment.domain.QuestionTranslation;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.persistence.AnswerOptionRepository;
import uz.aifu.lms.assessment.persistence.AnswerOptionTranslationRepository;
import uz.aifu.lms.assessment.persistence.QuestionAnswerKeyRepository;
import uz.aifu.lms.assessment.persistence.QuestionRepository;
import uz.aifu.lms.assessment.persistence.QuestionTranslationRepository;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;

/**
 * Question administration, including its answer options and answer keys
 * (docs/planning/phase-plan.md Phase 7) — bundled in one service the same way {@code
 * ActivityService} bundles translations and variants, rather than one service per child table.
 */
@Service
public class QuestionService {

    private final QuestionRepository questions;
    private final QuestionTranslationRepository questionTranslations;
    private final AnswerOptionRepository options;
    private final AnswerOptionTranslationRepository optionTranslations;
    private final QuestionAnswerKeyRepository answerKeys;
    private final QuizService quizzes;
    private final AuditService audit;

    public QuestionService(
            QuestionRepository questions,
            QuestionTranslationRepository questionTranslations,
            AnswerOptionRepository options,
            AnswerOptionTranslationRepository optionTranslations,
            QuestionAnswerKeyRepository answerKeys,
            QuizService quizzes,
            AuditService audit) {
        this.questions = questions;
        this.questionTranslations = questionTranslations;
        this.options = options;
        this.optionTranslations = optionTranslations;
        this.answerKeys = answerKeys;
        this.quizzes = quizzes;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Question require(UUID id) {
        return questions
                .findWithQuizById(id)
                .orElseThrow(() -> Exceptions.notFound("Question", id));
    }

    /** Unpaginated — internal use only (reorder validation, attempt serving). */
    @Transactional(readOnly = true)
    public List<Question> listByQuiz(UUID quizId) {
        return questions.findByQuiz_IdOrderByPosition(quizId);
    }

    @Transactional(readOnly = true)
    public Page<Question> listByQuiz(UUID quizId, Pageable pageable) {
        return questions.findByQuiz_Id(quizId, pageable);
    }

    @Transactional
    public Question create(UUID quizId, CreateQuestionRequest request, UUID actorId, String ip) {
        Quiz quiz = quizzes.require(quizId);
        int position = questions.nextPosition(quizId);
        Question question = new Question(quiz, position, request.questionType(), request.points());
        Question saved = questions.save(question);
        recomputeQuizMaxPoints(quiz);
        audit.record(
                AuditAction.QUESTION_CREATED,
                actorId,
                "question",
                saved.getId(),
                ip,
                null,
                Map.of("quizId", quizId.toString()));
        return saved;
    }

    @Transactional
    public Question updateDetails(UUID id, UpdateQuestionRequest request, UUID actorId, String ip) {
        Question question = require(id);
        String settingsJson = request.settings() == null ? "{}" : request.settings().toString();
        question.updateDetails(
                request.points(), request.difficulty(), request.isRequired(), settingsJson);
        recomputeQuizMaxPoints(question.getQuiz());
        audit.record(AuditAction.QUESTION_UPDATED, actorId, "question", id, ip, null);
        return question;
    }

    @Transactional
    public void reorder(UUID quizId, List<UUID> orderedQuestionIds, UUID actorId, String ip) {
        List<Question> existing = questions.findByQuiz_IdOrderByPosition(quizId);
        if (existing.size() != orderedQuestionIds.size()
                || !existing.stream()
                        .map(Question::getId)
                        .collect(Collectors.toSet())
                        .equals(Set.copyOf(orderedQuestionIds))) {
            throw Exceptions.validation(
                    "orderedQuestionIds must be exactly the quiz's current question ids");
        }
        Map<UUID, Question> byId =
                existing.stream().collect(Collectors.toMap(Question::getId, q -> q));
        for (int i = 0; i < orderedQuestionIds.size(); i++) {
            byId.get(orderedQuestionIds.get(i)).reorderTo(i);
        }
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "quiz",
                quizId,
                ip,
                null,
                Map.of("transition", "questions-reordered"));
    }

    @Transactional
    public void delete(UUID id, UUID actorId, String ip) {
        Question question = require(id);
        UUID quizId = question.getQuiz().getId();
        questions.deleteById(id);
        audit.record(AuditAction.QUESTION_DELETED, actorId, "question", id, ip, null);
        recomputeQuizMaxPoints(quizzes.require(quizId));
    }

    private void recomputeQuizMaxPoints(Quiz quiz) {
        BigDecimal total =
                questions.findByQuiz_IdOrderByPosition(quiz.getId()).stream()
                        .map(Question::getPoints)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        quiz.setMaxPoints(total);
    }

    // ---------------------------------------------------------------- question translations

    @Transactional(readOnly = true)
    public List<QuestionTranslation> listTranslations(UUID questionId) {
        return questionTranslations.findAllByQuestionId(questionId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<QuestionTranslation>> listTranslationsForQuestions(
            Collection<UUID> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionTranslations.findAllByQuestionIds(questionIds).stream()
                .collect(Collectors.groupingBy(t -> t.getQuestion().getId()));
    }

    @Transactional
    public QuestionTranslation upsertTranslation(
            UUID questionId,
            String locale,
            UpsertQuestionTranslationRequest request,
            UUID actorId,
            String ip) {
        Question question = require(questionId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        QuestionTranslation translation =
                questionTranslations
                        .findByQuestionIdAndLocale(questionId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new QuestionTranslation(
                                                question,
                                                resolvedLocale.code(),
                                                request.prompt(),
                                                request.explanation(),
                                                request.hint()));
        translation.update(request.prompt(), request.explanation(), request.hint());
        QuestionTranslation saved = questionTranslations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "question",
                questionId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- answer options

    @Transactional(readOnly = true)
    public List<AnswerOption> listOptions(UUID questionId) {
        return options.findByQuestion_IdOrderByPosition(questionId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<AnswerOptionTranslation>> listOptionTranslationsForOptions(
            Collection<UUID> optionIds) {
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        return optionTranslations.findAllByOptionIds(optionIds).stream()
                .collect(Collectors.groupingBy(t -> t.getOption().getId()));
    }

    @Transactional
    public AnswerOption addOption(
            UUID questionId, CreateAnswerOptionRequest request, UUID actorId, String ip) {
        Question question = require(questionId);
        int position = options.nextPosition(questionId);
        AnswerOption option =
                new AnswerOption(question, position, request.isCorrect(), request.matchKey());
        AnswerOption saved = options.save(option);
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "question",
                questionId,
                ip,
                null,
                Map.of("transition", "option-added"));
        return saved;
    }

    /**
     * The course an answer option belongs to, resolved inside a transaction so callers can run the
     * course-editor check BEFORE mutating (the option's question/quiz are lazy proxies and cannot
     * be navigated once the service transaction has closed).
     */
    @Transactional(readOnly = true)
    public UUID courseIdOfOption(UUID optionId) {
        return requireOption(optionId).getQuestion().getQuiz().getCourse().getId();
    }

    /** See {@link #courseIdOfOption}; same purpose for an answer key. */
    @Transactional(readOnly = true)
    public UUID courseIdOfAnswerKey(UUID keyId) {
        return answerKeys
                .findById(keyId)
                .orElseThrow(() -> Exceptions.notFound("QuestionAnswerKey", keyId))
                .getQuestion()
                .getQuiz()
                .getCourse()
                .getId();
    }

    @Transactional
    public AnswerOption updateOption(
            UUID optionId, UpdateAnswerOptionRequest request, UUID actorId, String ip) {
        AnswerOption option = requireOption(optionId);
        option.update(request.isCorrect(), request.matchKey());
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "question",
                option.getQuestion().getId(),
                ip,
                null,
                Map.of("transition", "option-updated"));
        return option;
    }

    @Transactional
    public void reorderOptions(
            UUID questionId, List<UUID> orderedOptionIds, UUID actorId, String ip) {
        List<AnswerOption> existing = options.findByQuestion_IdOrderByPosition(questionId);
        if (existing.size() != orderedOptionIds.size()
                || !existing.stream()
                        .map(AnswerOption::getId)
                        .collect(Collectors.toSet())
                        .equals(Set.copyOf(orderedOptionIds))) {
            throw Exceptions.validation(
                    "orderedOptionIds must be exactly the question's current option ids");
        }
        Map<UUID, AnswerOption> byId =
                existing.stream().collect(Collectors.toMap(AnswerOption::getId, o -> o));
        for (int i = 0; i < orderedOptionIds.size(); i++) {
            byId.get(orderedOptionIds.get(i)).reorderTo(i);
        }
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "question",
                questionId,
                ip,
                null,
                Map.of("transition", "options-reordered"));
    }

    @Transactional
    public void removeOption(UUID optionId, UUID actorId, String ip) {
        AnswerOption option = requireOption(optionId);
        UUID questionId = option.getQuestion().getId();
        options.delete(option);
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "question",
                questionId,
                ip,
                null,
                Map.of("transition", "option-removed"));
    }

    private AnswerOption requireOption(UUID optionId) {
        return options.findById(optionId)
                .orElseThrow(() -> Exceptions.notFound("AnswerOption", optionId));
    }

    @Transactional
    public AnswerOptionTranslation upsertOptionTranslation(
            UUID optionId,
            String locale,
            UpsertAnswerOptionTranslationRequest request,
            UUID actorId,
            String ip) {
        AnswerOption option = requireOption(optionId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        AnswerOptionTranslation translation =
                optionTranslations
                        .findByOptionIdAndLocale(optionId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new AnswerOptionTranslation(
                                                option,
                                                resolvedLocale.code(),
                                                request.text(),
                                                request.feedback()));
        translation.update(request.text(), request.feedback());
        AnswerOptionTranslation saved = optionTranslations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "answer_option",
                optionId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- answer keys

    @Transactional(readOnly = true)
    public List<QuestionAnswerKey> listAnswerKeys(UUID questionId) {
        return answerKeys.findByQuestion_Id(questionId);
    }

    @Transactional
    public QuestionAnswerKey addAnswerKey(
            UUID questionId, CreateAnswerKeyRequest request, UUID actorId, String ip) {
        Question question = require(questionId);
        QuestionAnswerKey key =
                new QuestionAnswerKey(
                        question,
                        request.matchMode(),
                        request.expectedValue(),
                        request.tolerance(),
                        request.locale());
        QuestionAnswerKey saved = answerKeys.save(key);
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "question",
                questionId,
                ip,
                null,
                Map.of("transition", "answer-key-added"));
        return saved;
    }

    @Transactional
    public void removeAnswerKey(UUID keyId, UUID actorId, String ip) {
        QuestionAnswerKey key =
                answerKeys
                        .findById(keyId)
                        .orElseThrow(() -> Exceptions.notFound("QuestionAnswerKey", keyId));
        UUID questionId = key.getQuestion().getId();
        answerKeys.delete(key);
        audit.record(
                AuditAction.QUESTION_UPDATED,
                actorId,
                "question",
                questionId,
                ip,
                null,
                Map.of("transition", "answer-key-removed"));
    }
}
