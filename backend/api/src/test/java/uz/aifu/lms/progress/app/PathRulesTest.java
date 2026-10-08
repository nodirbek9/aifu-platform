package uz.aifu.lms.progress.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.progress.app.PathRules.LessonInput;
import uz.aifu.lms.progress.app.PathRules.LessonPathState;
import uz.aifu.lms.progress.app.PathRules.LessonProgressInput;
import uz.aifu.lms.progress.app.PathRules.LockReason;
import uz.aifu.lms.progress.app.PathRules.MidtermConfig;
import uz.aifu.lms.progress.app.PathRules.StepState;

class PathRulesTest {

    @Test
    @DisplayName(
            "First lesson in the chain is always open, while next lesson is locked when first is incomplete")
    void firstLessonAlwaysOpen() {
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();
        UUID l3 = UUID.randomUUID();

        List<LessonInput> lessons =
                List.of(
                        new LessonInput(l1, 1, SessionType.LECTURE),
                        new LessonInput(l2, 2, SessionType.LECTURE),
                        new LessonInput(l3, 3, SessionType.LECTURE));

        Map<UUID, LessonPathState> state =
                PathRules.evaluate(lessons, Map.of(), MidtermConfig.none());

        assertThat(state.get(l1).open()).isTrue();
        assertThat(state.get(l1).lockReason()).isNull();

        assertThat(state.get(l2).open()).isFalse();
        assertThat(state.get(l2).lockReason()).isEqualTo(LockReason.PREVIOUS_LESSON_INCOMPLETE);
        assertThat(state.get(l2).blockingLessonId()).isEqualTo(l1);

        assertThat(state.get(l3).open()).isFalse();
        assertThat(state.get(l3).lockReason()).isEqualTo(LockReason.PREVIOUS_LESSON_INCOMPLETE);
        assertThat(state.get(l3).blockingLessonId()).isEqualTo(l1);
    }

    @Test
    @DisplayName("A missing test/practice step evaluates to NONE and counts as satisfied")
    void missingStepIsSatisfied() {
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();

        List<LessonInput> lessons =
                List.of(
                        new LessonInput(l1, 1, SessionType.LECTURE),
                        new LessonInput(l2, 2, SessionType.LECTURE));

        // l1 has reading green, but no test and no practice activity present
        LessonProgressInput l1Progress =
                new LessonProgressInput(false, false, true, false, false, false, false);

        Map<UUID, LessonPathState> state =
                PathRules.evaluate(lessons, Map.of(l1, l1Progress), MidtermConfig.none());

        LessonPathState s1 = state.get(l1);
        assertThat(s1.steps().lecture()).isEqualTo(StepState.DONE);
        assertThat(s1.steps().test()).isEqualTo(StepState.NONE);
        assertThat(s1.steps().practice()).isEqualTo(StepState.NONE);
        assertThat(s1.done()).isTrue();

        // Since l1 steps are satisfied, l2 opens
        assertThat(state.get(l2).open()).isTrue();
        assertThat(state.get(l2).lockReason()).isNull();
    }

    @Test
    @DisplayName("A present step that is incomplete blocks the lesson and next lessons")
    void presentIncompleteStepBlocks() {
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();

        List<LessonInput> lessons =
                List.of(
                        new LessonInput(l1, 1, SessionType.LECTURE),
                        new LessonInput(l2, 2, SessionType.LECTURE));

        // l1 has reading green and test present, but test is NOT done
        LessonProgressInput l1Progress =
                new LessonProgressInput(false, false, true, true, false, false, false);

        Map<UUID, LessonPathState> state =
                PathRules.evaluate(lessons, Map.of(l1, l1Progress), MidtermConfig.none());

        LessonPathState s1 = state.get(l1);
        assertThat(s1.steps().lecture()).isEqualTo(StepState.DONE);
        assertThat(s1.steps().test()).isEqualTo(StepState.TODO);
        assertThat(s1.steps().practice()).isEqualTo(StepState.NONE);
        assertThat(s1.done()).isFalse();

        assertThat(state.get(l2).open()).isFalse();
        assertThat(state.get(l2).lockReason()).isEqualTo(LockReason.PREVIOUS_LESSON_INCOMPLETE);
        assertThat(state.get(l2).blockingLessonId()).isEqualTo(l1);
    }

    @Test
    @DisplayName("A waived lesson is open, counts as done, and opens the next lesson")
    void waiverOpens() {
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();

        List<LessonInput> lessons =
                List.of(
                        new LessonInput(l1, 1, SessionType.LECTURE),
                        new LessonInput(l2, 2, SessionType.LECTURE));

        // l1 is waived with no steps done
        LessonProgressInput l1Progress = LessonProgressInput.ofWaived();

        Map<UUID, LessonPathState> state =
                PathRules.evaluate(lessons, Map.of(l1, l1Progress), MidtermConfig.none());

        LessonPathState s1 = state.get(l1);
        assertThat(s1.open()).isTrue();
        assertThat(s1.done()).isTrue();
        // Circle states still show real step status
        assertThat(s1.steps().lecture()).isEqualTo(StepState.TODO);

        LessonPathState s2 = state.get(l2);
        assertThat(s2.open()).isTrue();
        assertThat(s2.lockReason()).isNull();
    }

    @Test
    @DisplayName(
            "Independent-study (MT) lessons are outside the chain: always open and never blocking")
    void mtNeverLockedAndNeverBlocking() {
        UUID l1 = UUID.randomUUID();
        UUID mtLesson = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();

        List<LessonInput> lessons =
                List.of(
                        new LessonInput(l1, 1, SessionType.LECTURE),
                        new LessonInput(mtLesson, 2, SessionType.INDEPENDENT_STUDY),
                        new LessonInput(l2, 3, SessionType.LECTURE));

        // l1 is done, mtLesson has no progress
        LessonProgressInput l1Progress = LessonProgressInput.ofCompleted();

        Map<UUID, LessonPathState> state =
                PathRules.evaluate(lessons, Map.of(l1, l1Progress), MidtermConfig.none());

        // MT lesson is always open even without progress
        LessonPathState mtState = state.get(mtLesson);
        assertThat(mtState.open()).isTrue();
        assertThat(mtState.lockReason()).isNull();

        // l2 is open because l1 is done, and mtLesson does not block it
        LessonPathState s2 = state.get(l2);
        assertThat(s2.open()).isTrue();
        assertThat(s2.lockReason()).isNull();

        // Even when l1 is NOT done, MT lesson is still open
        Map<UUID, LessonPathState> unstartedState =
                PathRules.evaluate(lessons, Map.of(), MidtermConfig.none());
        assertThat(unstartedState.get(mtLesson).open()).isTrue();
        assertThat(unstartedState.get(mtLesson).lockReason()).isNull();
    }

    @Test
    @DisplayName("Midterm anchor blocks lessons strictly after it until midterm is passed")
    void midtermAnchorBlocksLessonsAfterItUntilPassed() {
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID(); // Anchor lesson
        UUID l3 = UUID.randomUUID(); // After anchor

        List<LessonInput> lessons =
                List.of(
                        new LessonInput(l1, 1, SessionType.LECTURE),
                        new LessonInput(l2, 2, SessionType.LECTURE),
                        new LessonInput(l3, 3, SessionType.LECTURE));

        // Both l1 and l2 are done
        Map<UUID, LessonProgressInput> progress =
                Map.of(
                        l1, LessonProgressInput.ofCompleted(),
                        l2, LessonProgressInput.ofCompleted());

        // Midterm not passed
        MidtermConfig notPassed = MidtermConfig.anchor(l2, false);
        Map<UUID, LessonPathState> stateNotPassed =
                PathRules.evaluate(lessons, progress, notPassed);

        assertThat(stateNotPassed.get(l1).open()).isTrue();
        assertThat(stateNotPassed.get(l2).open()).isTrue(); // Anchor itself is open
        assertThat(stateNotPassed.get(l3).open()).isFalse();
        assertThat(stateNotPassed.get(l3).lockReason()).isEqualTo(LockReason.MIDTERM_REQUIRED);
        assertThat(stateNotPassed.get(l3).blockingLessonId()).isNull();

        // Midterm passed
        MidtermConfig passed = MidtermConfig.anchor(l2, true);
        Map<UUID, LessonPathState> statePassed = PathRules.evaluate(lessons, progress, passed);

        assertThat(statePassed.get(l3).open()).isTrue();
        assertThat(statePassed.get(l3).lockReason()).isNull();
    }

    @Test
    @DisplayName("Explicit prerequisites block a lesson even if earlier chain lessons are done")
    void explicitPrerequisites() {
        UUID l1 = UUID.randomUUID();
        UUID l2 = UUID.randomUUID();
        UUID prereq = UUID.randomUUID();

        LessonInput lesson1 = new LessonInput(l1, 1, SessionType.LECTURE);
        LessonInput prerequisiteLesson = new LessonInput(prereq, 2, SessionType.LECTURE);
        LessonInput lesson2 =
                new LessonInput(
                        l2,
                        UUID.randomUUID(),
                        1,
                        3,
                        SessionType.LECTURE,
                        true,
                        true,
                        List.of(prereq));

        List<LessonInput> lessons = List.of(lesson1, prerequisiteLesson, lesson2);

        // l1 is done, but prereq is not done
        Map<UUID, LessonProgressInput> progressWithoutPrereq =
                Map.of(l1, LessonProgressInput.ofCompleted());

        Map<UUID, LessonPathState> state1 =
                PathRules.evaluate(lessons, progressWithoutPrereq, MidtermConfig.none());

        assertThat(state1.get(lesson2.id()).open()).isFalse();
        assertThat(state1.get(lesson2.id()).lockReason())
                .isEqualTo(LockReason.PREVIOUS_LESSON_INCOMPLETE);
        assertThat(state1.get(lesson2.id()).blockingLessonId()).isEqualTo(prereq);

        // Once prereq is completed, lesson2 opens
        Map<UUID, LessonProgressInput> progressWithPrereq =
                Map.of(
                        l1, LessonProgressInput.ofCompleted(),
                        prereq, LessonProgressInput.ofCompleted());

        Map<UUID, LessonPathState> state2 =
                PathRules.evaluate(lessons, progressWithPrereq, MidtermConfig.none());

        assertThat(state2.get(lesson2.id()).open()).isTrue();
        assertThat(state2.get(lesson2.id()).lockReason()).isNull();
    }
}
