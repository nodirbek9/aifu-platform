package uz.aifu.lms.assessment.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.RecordAnswerRequest;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.SelectedOptionInput;
import uz.aifu.lms.assessment.app.grading.GradeResult;
import uz.aifu.lms.assessment.app.grading.GradingContext;
import uz.aifu.lms.assessment.app.grading.QuestionGraderRegistry;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.AttemptStatus;
import uz.aifu.lms.assessment.domain.Question;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.domain.QuizAttempt;
import uz.aifu.lms.assessment.domain.QuizAttemptQuestion;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;
import uz.aifu.lms.assessment.persistence.AnswerOptionRepository;
import uz.aifu.lms.assessment.persistence.QuestionAnswerKeyRepository;
import uz.aifu.lms.assessment.persistence.QuestionRepository;
import uz.aifu.lms.assessment.persistence.QuizAttemptQuestionRepository;
import uz.aifu.lms.assessment.persistence.QuizAttemptRepository;
import uz.aifu.lms.assessment.persistence.QuizAttemptSelectedOptionRepository;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.QuizAttemptGradedEvent;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * Quiz attempt lifecycle (docs/planning/phase-plan.md Phase 7, brief §35). Ownership for every
 * student-facing method is "the caller's own enrollment" (ADR-004 Layers 3/4) — no {@code
 * AccessGuard} needed here, same reasoning as {@code progress}'s student endpoints (ADR-012).
 *
 * <p>Expiry is lazy, not a background sweeper: every interaction with an {@code IN_PROGRESS}
 * attempt first checks {@link QuizAttempt#isExpired}; a stale attempt is transitioned to {@code
 * EXPIRED} in its own committed transaction ({@link #expireIfNeeded}) before the calling method
 * re-reads it and reacts — mutating-then-throwing in the same transaction would roll the expire
 * transition back along with the error, which is not the intended effect (CLAUDE.md §3: no extra
 * scheduler infrastructure without a real need this simple check already covers).
 */
@Service
public class AttemptService {

    private static final int MAX_OPTIMISTIC_RETRIES = 5;
    private static final Random RANDOM = new Random();

    private final QuizAttemptRepository attempts;
    private final QuizAttemptQuestionRepository attemptQuestions;
    private final QuizAttemptSelectedOptionRepository selectedOptions;
    private final QuestionRepository questions;
    private final AnswerOptionRepository answerOptions;
    private final QuestionAnswerKeyRepository answerKeys;
    private final QuizService quizzes;
    private final EnrollmentService enrollments;
    private final ActivityService activities;
    private final QuestionGraderRegistry graders;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final AttemptService self;

    public AttemptService(
            QuizAttemptRepository attempts,
            QuizAttemptQuestionRepository attemptQuestions,
            QuizAttemptSelectedOptionRepository selectedOptions,
            QuestionRepository questions,
            AnswerOptionRepository answerOptions,
            QuestionAnswerKeyRepository answerKeys,
            QuizService quizzes,
            EnrollmentService enrollments,
            ActivityService activities,
            QuestionGraderRegistry graders,
            ApplicationEventPublisher events,
            Clock clock,
            @Lazy AttemptService self) {
        this.attempts = attempts;
        this.attemptQuestions = attemptQuestions;
        this.selectedOptions = selectedOptions;
        this.questions = questions;
        this.answerOptions = answerOptions;
        this.answerKeys = answerKeys;
        this.quizzes = quizzes;
        this.enrollments = enrollments;
        this.activities = activities;
        this.graders = graders;
        this.events = events;
        this.clock = clock;
        this.self = self;
    }

    // ---------------------------------------------------------------- start

    public AttemptView start(UUID quizId, UUID studentUserId, String locale, UUID activityId) {
        return retrying(() -> self.doStart(quizId, studentUserId, locale, activityId));
    }

    @Transactional
    public AttemptView doStart(UUID quizId, UUID studentUserId, String locale, UUID activityId) {
        Quiz quiz = quizzes.requirePublished(quizId);
        Enrollment enrollment =
                enrollments.requireActiveEnrollment(quiz.getCourse().getId(), studentUserId);
        Instant now = clock.instant();

        attempts.findFirstByQuiz_IdAndEnrollment_IdAndStatus(
                        quizId, enrollment.getId(), AttemptStatus.IN_PROGRESS)
                .ifPresent(
                        open -> {
                            if (open.isExpired(now)) {
                                open.expire(now);
                            } else {
                                throw Exceptions.conflict(
                                        ErrorCode.ATTEMPT_ALREADY_OPEN,
                                        "An attempt is already in progress for this quiz");
                            }
                        });

        int maxAttemptNumber = attempts.findMaxAttemptNumber(quizId, enrollment.getId());
        if (quiz.getMaxAttempts() != null && maxAttemptNumber >= quiz.getMaxAttempts()) {
            throw Exceptions.rule(
                    ErrorCode.ATTEMPT_LIMIT_REACHED, "Maximum attempts reached for this quiz");
        }

        Activity activity = resolveActivity(activityId, quizId);
        Instant expiresAt =
                quiz.getTimeLimitSeconds() == null
                        ? null
                        : now.plusSeconds(quiz.getTimeLimitSeconds());
        QuizAttempt attempt =
                new QuizAttempt(
                        quiz, enrollment, activity, maxAttemptNumber + 1, locale, now, expiresAt);
        QuizAttempt saved;
        try {
            // saveAndFlush: a genuine concurrent double-start race (two requests both pass the
            // pre-check above) must surface here as a catchable violation of uq_quiz_attempt_open,
            // not later at an uncontrolled commit-time flush — same reasoning as
            // EnrollmentService.enroll.
            saved = attempts.saveAndFlush(attempt);
        } catch (DataIntegrityViolationException e) {
            throw Exceptions.conflict(
                    ErrorCode.ATTEMPT_ALREADY_OPEN,
                    "An attempt is already in progress for this quiz");
        }

        List<QuizAttemptQuestion> served = serveQuestions(quiz, saved);
        return new AttemptView(saved, served);
    }

    private Activity resolveActivity(UUID activityId, UUID quizId) {
        if (activityId == null) {
            return null;
        }
        Activity activity = activities.require(activityId);
        if (activity.getActivityType() != ActivityType.QUIZ
                || !quizId.equals(activity.getReferenceId())) {
            throw Exceptions.validation("activityId does not reference this quiz");
        }
        return activity;
    }

    private List<QuizAttemptQuestion> serveQuestions(Quiz quiz, QuizAttempt attempt) {
        List<Question> all = questions.findByQuiz_IdOrderByPosition(quiz.getId());
        List<Question> served = new ArrayList<>(all);
        Integer poolSize = quiz.getQuestionPoolSize();
        if (poolSize != null && poolSize < served.size()) {
            Collections.shuffle(served, RANDOM);
            served = new ArrayList<>(served.subList(0, poolSize));
        }
        if (quiz.isShuffleQuestions()) {
            Collections.shuffle(served, RANDOM);
        } else {
            served.sort(Comparator.comparingInt(Question::getPosition));
        }
        List<QuizAttemptQuestion> saved = new ArrayList<>();
        int position = 0;
        for (Question question : served) {
            saved.add(
                    attemptQuestions.save(
                            new QuizAttemptQuestion(
                                    attempt, question, position++, question.getPoints())));
        }
        return saved;
    }

    // ---------------------------------------------------------------- answer

    /**
     * Deliberately not {@code @Transactional} itself: {@link #expireIfNeeded} must commit in its
     * own transaction before {@link #doRecordAnswer} re-reads the (possibly now {@code EXPIRED})
     * attempt — see the class javadoc.
     */
    public QuizAttemptQuestion recordAnswer(
            UUID attemptId, UUID questionId, UUID studentUserId, RecordAnswerRequest request) {
        self.expireIfNeeded(attemptId, clock.instant());
        return self.doRecordAnswer(attemptId, questionId, studentUserId, request);
    }

    @Transactional
    public QuizAttemptQuestion doRecordAnswer(
            UUID attemptId, UUID questionId, UUID studentUserId, RecordAnswerRequest request) {
        QuizAttempt attempt = requireOwnedAttempt(attemptId, studentUserId);
        requireInProgress(attempt);
        Instant now = clock.instant();

        QuizAttemptQuestion attemptQuestion =
                attemptQuestions
                        .findByAttempt_IdAndQuestion_Id(attemptId, questionId)
                        .orElseThrow(
                                () ->
                                        Exceptions.validation(
                                                "This question was not served in this attempt"));
        attemptQuestion.recordResponse(request.responseText(), request.responseNumber(), now);

        selectedOptions.deleteByAttemptQuestion_Id(attemptQuestion.getId());
        selectedOptions.flush();
        if (request.selectedOptions() != null) {
            for (SelectedOptionInput input : request.selectedOptions()) {
                AnswerOption option =
                        answerOptions
                                .findById(input.optionId())
                                .orElseThrow(
                                        () ->
                                                Exceptions.notFound(
                                                        "AnswerOption", input.optionId()));
                if (!option.getQuestion().getId().equals(questionId)) {
                    throw Exceptions.validation("Option does not belong to this question");
                }
                selectedOptions.save(
                        new QuizAttemptSelectedOption(attemptQuestion, option, input.ordinal()));
            }
        }
        return attemptQuestion;
    }

    // ---------------------------------------------------------------- submit + grade

    public QuizAttempt submit(UUID attemptId, UUID studentUserId, String idempotencyKey) {
        return retrying(
                () -> {
                    self.expireIfNeeded(attemptId, clock.instant());
                    return self.doSubmit(attemptId, studentUserId, idempotencyKey);
                });
    }

    @Transactional
    public QuizAttempt doSubmit(UUID attemptId, UUID studentUserId, String idempotencyKey) {
        QuizAttempt attempt = requireOwnedAttempt(attemptId, studentUserId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            boolean replay =
                    idempotencyKey != null
                            && idempotencyKey.equals(attempt.getIdempotencyKey())
                            && (attempt.getStatus() == AttemptStatus.SUBMITTED
                                    || attempt.getStatus() == AttemptStatus.GRADED);
            if (replay) {
                return attempt;
            }
            throw Exceptions.conflict(
                    ErrorCode.ATTEMPT_ALREADY_SUBMITTED, "This attempt has already been submitted");
        }
        Instant now = clock.instant();
        attempt.submit(now, idempotencyKey);
        gradeInternal(attempt, now);
        return attempt;
    }

    private void gradeInternal(QuizAttempt attempt, Instant now) {
        List<QuizAttemptQuestion> served =
                attemptQuestions.findByAttempt_IdOrderByServedPosition(attempt.getId());
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalPossible = BigDecimal.ZERO;
        for (QuizAttemptQuestion attemptQuestion : served) {
            Question question = attemptQuestion.getQuestion();
            List<AnswerOption> options =
                    answerOptions.findByQuestion_IdOrderByPosition(question.getId());
            List<QuestionAnswerKey> keys = answerKeys.findByQuestion_Id(question.getId());
            List<QuizAttemptSelectedOption> selected =
                    selectedOptions.findByAttemptQuestion_IdOrderByOrdinal(attemptQuestion.getId());
            GradeResult result =
                    graders.forType(question.getQuestionType())
                            .grade(
                                    new GradingContext(
                                            question, options, keys, attemptQuestion, selected));
            attemptQuestion.grade(result.pointsAwarded(), result.correct(), null, now);
            totalScore = totalScore.add(result.pointsAwarded());
            totalPossible = totalPossible.add(attemptQuestion.getPointsPossible());
        }
        BigDecimal percent =
                totalPossible.signum() == 0
                        ? BigDecimal.ZERO
                        : totalScore
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalPossible, 2, RoundingMode.HALF_UP);
        boolean passed = percent.compareTo(attempt.getQuiz().getPassingPercent()) >= 0;
        attempt.grade(totalScore, totalPossible, percent, passed, now);

        events.publishEvent(
                new QuizAttemptGradedEvent(
                        attempt.getId(),
                        attempt.getQuiz().getId(),
                        attempt.getEnrollment().getId(),
                        attempt.getActivity() == null ? null : attempt.getActivity().getId(),
                        totalScore,
                        totalPossible,
                        percent,
                        passed,
                        now));
    }

    // ---------------------------------------------------------------- expiry

    @Transactional
    public void expireIfNeeded(UUID attemptId, Instant now) {
        attempts.findById(attemptId).filter(a -> a.isExpired(now)).ifPresent(a -> a.expire(now));
    }

    // ---------------------------------------------------------------- read

    @Transactional(readOnly = true)
    public AttemptView require(UUID attemptId, UUID studentUserId) {
        QuizAttempt attempt = requireOwnedAttempt(attemptId, studentUserId);
        return new AttemptView(
                attempt, attemptQuestions.findByAttempt_IdOrderByServedPosition(attemptId));
    }

    /**
     * Teacher-facing accessor — no self-ownership check (that's {@link #require(UUID, UUID)}'s
     * job); the caller is responsible for its own {@code AccessGuard} check against {@code quiz.
     * course}. Backs {@code grading.api.AdminFeedbackController}'s {@code QUIZ_ATTEMPT} target-type
     * ownership resolution, matching {@code coding.app.SubmissionService#require(UUID)}'s identical
     * no-ownership-check shape.
     */
    @Transactional(readOnly = true)
    public QuizAttempt requireForReview(UUID attemptId) {
        return attempts.findWithDetailsById(attemptId)
                .orElseThrow(() -> Exceptions.notFound("QuizAttempt", attemptId));
    }

    @Transactional(readOnly = true)
    public Page<QuizAttempt> listOwn(UUID quizId, UUID studentUserId, Pageable pageable) {
        Enrollment enrollment =
                enrollments.requireLatestEnrollment(resolveCourseId(quizId), studentUserId);
        return attempts.findByEnrollment_IdAndQuiz_IdOrderByAttemptNumberDesc(
                enrollment.getId(), quizId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuizAttempt> listForQuiz(UUID quizId, Pageable pageable) {
        return attempts.findByQuiz_Id(quizId, pageable);
    }

    /** The highest attempt number reached so far — used by the reveal-policy check. */
    @Transactional(readOnly = true)
    public int maxAttemptNumber(UUID quizId, UUID enrollmentId) {
        return attempts.findMaxAttemptNumber(quizId, enrollmentId);
    }

    @Transactional(readOnly = true)
    public List<AnswerOption> optionsFor(UUID questionId) {
        return answerOptions.findByQuestion_IdOrderByPosition(questionId);
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptSelectedOption> selectedOptionsFor(UUID attemptQuestionId) {
        return selectedOptions.findByAttemptQuestion_IdOrderByOrdinal(attemptQuestionId);
    }

    // ---------------------------------------------------------------- helpers

    private UUID resolveCourseId(UUID quizId) {
        return quizzes.require(quizId).getCourse().getId();
    }

    private QuizAttempt requireOwnedAttempt(UUID attemptId, UUID studentUserId) {
        QuizAttempt attempt =
                attempts.findWithDetailsById(attemptId)
                        .orElseThrow(() -> Exceptions.notFound("QuizAttempt", attemptId));
        if (!attempt.getEnrollment().getStudent().getId().equals(studentUserId)) {
            throw Exceptions.notFound("QuizAttempt", attemptId);
        }
        return attempt;
    }

    private void requireInProgress(QuizAttempt attempt) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw Exceptions.conflict(
                    ErrorCode.ATTEMPT_ALREADY_SUBMITTED, "This attempt has already been submitted");
        }
    }

    private <T> T retrying(Supplier<T> action) {
        int attemptsLeft = 0;
        while (true) {
            try {
                return action.get();
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
                attemptsLeft++;
                if (attemptsLeft >= MAX_OPTIMISTIC_RETRIES) {
                    throw Exceptions.conflict(
                            ErrorCode.CONCURRENT_MODIFICATION,
                            "Too many concurrent updates to this attempt; please retry");
                }
            }
        }
    }
}
