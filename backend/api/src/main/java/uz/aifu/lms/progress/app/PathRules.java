package uz.aifu.lms.progress.app;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;

/**
 * Pure learning path gate evaluation logic (Phase 5, B4, ADR-015, ADR-017, ADR-018).
 *
 * <p>Free of Spring, JPA, or database dependencies: evaluates the state of a course's lessons given
 * plain in-memory inputs.
 */
public final class PathRules {

    private PathRules() {}

    public enum StepState {
        DONE,
        TODO,
        NONE
    }

    public enum LockReason {
        PREVIOUS_LESSON_INCOMPLETE,
        MIDTERM_REQUIRED
    }

    public record LessonSteps(StepState lecture, StepState test, StepState practice) {}

    public record LessonInput(
            UUID id,
            UUID moduleId,
            int modulePosition,
            int lessonPosition,
            SessionType sessionType,
            boolean required,
            boolean published,
            List<UUID> explicitPrerequisiteIds) {

        public LessonInput {
            if (explicitPrerequisiteIds == null) {
                explicitPrerequisiteIds = List.of();
            }
        }

        public LessonInput(
                UUID id,
                int modulePosition,
                int lessonPosition,
                SessionType sessionType,
                boolean required,
                boolean published) {
            this(
                    id,
                    null,
                    modulePosition,
                    lessonPosition,
                    sessionType,
                    required,
                    published,
                    List.of());
        }

        public LessonInput(UUID id, int position, SessionType sessionType) {
            this(id, null, 1, position, sessionType, true, true, List.of());
        }

        public LessonInput(UUID id, int position) {
            this(id, null, 1, position, SessionType.LECTURE, true, true, List.of());
        }
    }

    public record LessonProgressInput(
            boolean completed,
            boolean waived,
            boolean readingGreen,
            boolean testActivityPresent,
            boolean testActivityDone,
            boolean practiceActivityPresent,
            boolean practiceActivityDone) {

        public static LessonProgressInput empty() {
            return new LessonProgressInput(false, false, false, false, false, false, false);
        }

        public static LessonProgressInput ofCompleted() {
            return new LessonProgressInput(true, false, true, false, false, false, false);
        }

        public static LessonProgressInput ofWaived() {
            return new LessonProgressInput(false, true, false, false, false, false, false);
        }
    }

    public record MidtermConfig(
            UUID midtermActivityId,
            UUID anchorLessonId,
            boolean midtermPassed,
            Instant midtermOpensAt,
            Instant now) {

        public static MidtermConfig none() {
            return new MidtermConfig(null, null, false, null, null);
        }

        public static MidtermConfig anchor(UUID anchorLessonId, boolean passed) {
            return new MidtermConfig(null, anchorLessonId, passed, null, null);
        }
    }

    public record LessonPathState(
            UUID lessonId,
            LessonSteps steps,
            boolean done,
            boolean open,
            LockReason lockReason,
            UUID blockingLessonId) {}

    /**
     * Evaluates the learning path state for each lesson in the input.
     *
     * @param lessons the lessons to evaluate
     * @param progressByLesson current student progress per lesson
     * @param midtermConfig course midterm configuration, or null if unconfigured
     * @return a map of lesson ID to its evaluated path state, preserving outline order
     */
    public static Map<UUID, LessonPathState> evaluate(
            List<LessonInput> lessons,
            Map<UUID, LessonProgressInput> progressByLesson,
            MidtermConfig midtermConfig) {
        if (lessons == null || lessons.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, LessonProgressInput> progress =
                progressByLesson == null ? Collections.emptyMap() : progressByLesson;

        // 1. Compute per-lesson steps and initial done status
        Map<UUID, LessonSteps> stepsMap = new HashMap<>();
        Map<UUID, Boolean> doneMap = new HashMap<>();

        for (LessonInput lesson : lessons) {
            LessonProgressInput p = progress.getOrDefault(lesson.id(), LessonProgressInput.empty());

            StepState lecture =
                    (p.readingGreen() || p.completed()) ? StepState.DONE : StepState.TODO;
            StepState test =
                    !p.testActivityPresent()
                            ? StepState.NONE
                            : (p.testActivityDone() ? StepState.DONE : StepState.TODO);
            StepState practice =
                    !p.practiceActivityPresent()
                            ? StepState.NONE
                            : (p.practiceActivityDone() ? StepState.DONE : StepState.TODO);

            LessonSteps steps = new LessonSteps(lecture, test, practice);
            stepsMap.put(lesson.id(), steps);

            boolean stepsSatisfied =
                    (lecture == StepState.DONE)
                            && (test != StepState.TODO)
                            && (practice != StepState.TODO);
            boolean done = p.waived() || p.completed() || stepsSatisfied;
            doneMap.put(lesson.id(), done);
        }

        // 2. Identify the chain lessons (published, required, not INDEPENDENT_STUDY)
        List<LessonInput> chain =
                lessons.stream()
                        .filter(
                                l ->
                                        l.published()
                                                && l.required()
                                                && l.sessionType() != SessionType.INDEPENDENT_STUDY)
                        .sorted(
                                Comparator.comparingInt(LessonInput::modulePosition)
                                        .thenComparingInt(LessonInput::lessonPosition))
                        .toList();

        // 3. Locate the midterm anchor in the chain
        int anchorIndex = -1;
        if (midtermConfig != null && midtermConfig.anchorLessonId() != null) {
            for (int idx = 0; idx < chain.size(); idx++) {
                if (chain.get(idx).id().equals(midtermConfig.anchorLessonId())) {
                    anchorIndex = idx;
                    break;
                }
            }
        }

        // 4. Evaluate open/lock state for chain lessons
        Map<UUID, LessonPathState> chainEvaluated = new HashMap<>();
        for (int i = 0; i < chain.size(); i++) {
            LessonInput current = chain.get(i);
            LessonSteps steps = stepsMap.get(current.id());
            boolean done = doneMap.get(current.id());
            LessonProgressInput p =
                    progress.getOrDefault(current.id(), LessonProgressInput.empty());

            // A waived lesson is always open and done (ADR-018)
            if (p.waived()) {
                chainEvaluated.put(
                        current.id(),
                        new LessonPathState(current.id(), steps, true, true, null, null));
                continue;
            }

            // The first lesson in the chain is always open
            if (i == 0) {
                chainEvaluated.put(
                        current.id(),
                        new LessonPathState(current.id(), steps, done, true, null, null));
                continue;
            }

            // Check if any earlier chain lesson is not done
            UUID blockingLessonId = null;
            for (int j = 0; j < i; j++) {
                if (!doneMap.get(chain.get(j).id())) {
                    blockingLessonId = chain.get(j).id();
                    break;
                }
            }

            if (blockingLessonId != null) {
                chainEvaluated.put(
                        current.id(),
                        new LessonPathState(
                                current.id(),
                                steps,
                                done,
                                false,
                                LockReason.PREVIOUS_LESSON_INCOMPLETE,
                                blockingLessonId));
                continue;
            }

            // Check explicit prerequisites
            UUID blockingPrereqId = null;
            for (UUID prereqId : current.explicitPrerequisiteIds()) {
                Boolean prereqDone = doneMap.get(prereqId);
                if (prereqDone == null || !prereqDone) {
                    blockingPrereqId = prereqId;
                    break;
                }
            }

            if (blockingPrereqId != null) {
                chainEvaluated.put(
                        current.id(),
                        new LessonPathState(
                                current.id(),
                                steps,
                                done,
                                false,
                                LockReason.PREVIOUS_LESSON_INCOMPLETE,
                                blockingPrereqId));
                continue;
            }

            // Check midterm anchor: lessons strictly after the anchor require midterm passed
            if (anchorIndex >= 0 && i > anchorIndex) {
                boolean passed = midtermConfig != null && midtermConfig.midtermPassed();
                if (!passed) {
                    chainEvaluated.put(
                            current.id(),
                            new LessonPathState(
                                    current.id(),
                                    steps,
                                    done,
                                    false,
                                    LockReason.MIDTERM_REQUIRED,
                                    null));
                    continue;
                }
            }

            // All conditions met: open
            chainEvaluated.put(
                    current.id(), new LessonPathState(current.id(), steps, done, true, null, null));
        }

        // 5. Build final map preserving the original lessons order
        Map<UUID, LessonPathState> resultMap = new LinkedHashMap<>();
        for (LessonInput lesson : lessons) {
            LessonPathState state = chainEvaluated.get(lesson.id());
            if (state != null) {
                resultMap.put(lesson.id(), state);
            } else {
                // Outside the chain (e.g. INDEPENDENT_STUDY, optional, or unpublished):
                // always open, never blocking
                LessonSteps steps = stepsMap.get(lesson.id());
                boolean done = doneMap.get(lesson.id());
                resultMap.put(
                        lesson.id(),
                        new LessonPathState(lesson.id(), steps, done, true, null, null));
            }
        }

        return resultMap;
    }
}
