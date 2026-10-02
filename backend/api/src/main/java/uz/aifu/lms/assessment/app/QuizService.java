package uz.aifu.lms.assessment.app;

import java.time.Clock;
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
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.assessment.api.dto.QuizDtos.CreateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpdateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpsertQuizTranslationRequest;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.domain.QuizTranslation;
import uz.aifu.lms.assessment.persistence.QuestionRepository;
import uz.aifu.lms.assessment.persistence.QuizRepository;
import uz.aifu.lms.assessment.persistence.QuizTranslationRepository;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;

/** Quiz administration (docs/planning/phase-plan.md Phase 7). */
@Service
public class QuizService {

    private final QuizRepository quizzes;
    private final QuizTranslationRepository translations;
    private final QuestionRepository questions;
    private final CourseService courses;
    private final TranslationCompletenessValidator translationValidator;
    private final AuditService audit;
    private final Clock clock;

    public QuizService(
            QuizRepository quizzes,
            QuizTranslationRepository translations,
            QuestionRepository questions,
            CourseService courses,
            TranslationCompletenessValidator translationValidator,
            AuditService audit,
            Clock clock) {
        this.quizzes = quizzes;
        this.translations = translations;
        this.questions = questions;
        this.courses = courses;
        this.translationValidator = translationValidator;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Quiz require(UUID id) {
        return quizzes.findWithCourseById(id).orElseThrow(() -> Exceptions.notFound("Quiz", id));
    }

    @Transactional(readOnly = true)
    public Quiz requirePublished(UUID id) {
        Quiz quiz = require(id);
        if (quiz.getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("Quiz", id);
        }
        return quiz;
    }

    @Transactional(readOnly = true)
    public Page<Quiz> listByCourse(UUID courseId, Pageable pageable) {
        return quizzes.findByCourse_Id(courseId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Quiz> listPublishedByCourse(UUID courseId, Pageable pageable) {
        return quizzes.findByCourse_IdAndStatus(courseId, PublicationStatus.PUBLISHED, pageable);
    }

    @Transactional
    public Quiz create(UUID courseId, CreateQuizRequest request, UUID actorId, String ip) {
        Course course = courses.require(courseId);
        Quiz quiz = new Quiz(course, request.code());
        Quiz saved = quizzes.save(quiz);
        audit.record(
                AuditAction.QUIZ_CREATED,
                actorId,
                "quiz",
                saved.getId(),
                ip,
                null,
                Map.of("courseId", courseId.toString()));
        return saved;
    }

    @Transactional
    public Quiz updateDetails(UUID id, UpdateQuizRequest request, UUID actorId, String ip) {
        Quiz quiz = require(id);
        quiz.updateDetails(
                request.code(),
                request.maxPoints(),
                request.passingPercent(),
                request.timeLimitSeconds(),
                request.maxAttempts(),
                request.questionPoolSize(),
                request.shuffleQuestions(),
                request.shuffleOptions(),
                request.revealPolicy());
        audit.record(AuditAction.QUIZ_UPDATED, actorId, "quiz", id, ip, null);
        return quiz;
    }

    @Transactional
    public Quiz publish(UUID id, UUID actorId, String ip) {
        Quiz quiz = require(id);
        if (questions.countByQuiz_Id(id) == 0) {
            throw Exceptions.rule(
                    ErrorCode.PUBLICATION_VALIDATION_FAILED,
                    "A quiz needs at least one question to publish");
        }
        translationValidator.requireLocales(
                Set.of(quiz.getCourse().getRequiredLocales()), availableLocales(id), "Quiz " + id);
        quiz.publish(clock.instant());
        audit.record(AuditAction.QUIZ_PUBLISHED, actorId, "quiz", id, ip, null);
        return quiz;
    }

    @Transactional
    public Quiz archive(UUID id, UUID actorId, String ip) {
        Quiz quiz = require(id);
        quiz.archive();
        audit.record(
                AuditAction.QUIZ_UPDATED,
                actorId,
                "quiz",
                id,
                ip,
                null,
                Map.of("transition", "archived"));
        return quiz;
    }

    @Transactional
    public Quiz returnToDraft(UUID id, UUID actorId, String ip) {
        Quiz quiz = require(id);
        quiz.returnToDraft();
        audit.record(
                AuditAction.QUIZ_UPDATED,
                actorId,
                "quiz",
                id,
                ip,
                null,
                Map.of("transition", "returned-to-draft"));
        return quiz;
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<QuizTranslation> listTranslations(UUID quizId) {
        return translations.findAllByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<QuizTranslation>> listTranslationsForQuizzes(Collection<UUID> quizIds) {
        if (quizIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByQuizIds(quizIds).stream()
                .collect(Collectors.groupingBy(t -> t.getQuiz().getId()));
    }

    @Transactional
    public QuizTranslation upsertTranslation(
            UUID quizId,
            String locale,
            UpsertQuizTranslationRequest request,
            UUID actorId,
            String ip) {
        Quiz quiz = require(quizId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        QuizTranslation translation =
                translations
                        .findByQuizIdAndLocale(quizId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new QuizTranslation(
                                                quiz,
                                                resolvedLocale.code(),
                                                request.title(),
                                                request.description(),
                                                request.instructions()));
        translation.update(request.title(), request.description(), request.instructions());
        QuizTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "quiz",
                quizId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    private Set<String> availableLocales(UUID quizId) {
        return translations.findAllByQuizId(quizId).stream()
                .map(QuizTranslation::getLocale)
                .collect(Collectors.toSet());
    }
}
