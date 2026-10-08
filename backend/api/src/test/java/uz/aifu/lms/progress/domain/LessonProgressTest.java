package uz.aifu.lms.progress.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.domain.Enrollment;

class LessonProgressTest {

    private Enrollment enrollment;
    private Lesson lesson;
    private LessonProgress progress;

    @BeforeEach
    void setUp() {
        enrollment = mock(Enrollment.class);
        lesson = mock(Lesson.class);
        progress = new LessonProgress(enrollment, lesson);
    }

    @Test
    @DisplayName("First heartbeat initializes lastHeartbeatAt and credits 0 seconds")
    void firstBeat_creditsZero() {
        Instant t0 = Instant.parse("2026-10-08T10:00:00Z");

        assertThat(progress.getLastHeartbeatAt()).isNull();
        assertThat(progress.getActiveReadingSeconds()).isZero();
        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.NOT_STARTED);

        int credited = progress.creditReading(t0, 30, 10);

        assertThat(credited).isZero();
        assertThat(progress.getLastHeartbeatAt()).isEqualTo(t0);
        assertThat(progress.getActiveReadingSeconds()).isZero();
        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Heartbeat with gap under 10 seconds credits 0 and preserves lastHeartbeatAt")
    void gapUnder10Seconds_creditsZero() {
        Instant t0 = Instant.parse("2026-10-08T10:00:00Z");
        progress.creditReading(t0, 30, 10);

        Instant t1 = t0.plusSeconds(5);
        int credited = progress.creditReading(t1, 30, 10);

        assertThat(credited).isZero();
        assertThat(progress.getActiveReadingSeconds()).isZero();
        assertThat(progress.getLastHeartbeatAt()).isEqualTo(t0);
    }

    @Test
    @DisplayName("Heartbeat with gap >= 10 seconds credits elapsed seconds")
    void validGap_creditsElapsedSeconds() {
        Instant t0 = Instant.parse("2026-10-08T10:00:00Z");
        progress.creditReading(t0, 30, 10);

        Instant t1 = t0.plusSeconds(15);
        int credited = progress.creditReading(t1, 30, 10);

        assertThat(credited).isEqualTo(15);
        assertThat(progress.getActiveReadingSeconds()).isEqualTo(15);
        assertThat(progress.getTimeSpentSeconds()).isEqualTo(15);
        assertThat(progress.getLastHeartbeatAt()).isEqualTo(t1);
    }

    @Test
    @DisplayName("Heartbeat is capped at maxCredit (30 seconds)")
    void beatIsCappedAt30Seconds() {
        Instant t0 = Instant.parse("2026-10-08T10:00:00Z");
        progress.creditReading(t0, 30, 10);

        Instant t1 = t0.plusSeconds(60);
        int credited = progress.creditReading(t1, 30, 10);

        assertThat(credited).isEqualTo(30);
        assertThat(progress.getActiveReadingSeconds()).isEqualTo(30);
        assertThat(progress.getTimeSpentSeconds()).isEqualTo(30);
        assertThat(progress.getLastHeartbeatAt()).isEqualTo(t1);
    }

    @Test
    @DisplayName("Duration overload works identically")
    void durationOverload_worksIdentically() {
        Instant t0 = Instant.parse("2026-10-08T10:00:00Z");
        progress.creditReading(t0, Duration.ofSeconds(30), Duration.ofSeconds(10));

        Instant t1 = t0.plusSeconds(20);
        int credited = progress.creditReading(t1, Duration.ofSeconds(30), Duration.ofSeconds(10));

        assertThat(credited).isEqualTo(20);
        assertThat(progress.getActiveReadingSeconds()).isEqualTo(20);
        assertThat(progress.getLastHeartbeatAt()).isEqualTo(t1);
    }

    @Test
    @DisplayName("Helper lifecycle methods mutate expected state")
    void helperMethods_updateStateCorrectly() {
        Instant now = Instant.parse("2026-10-08T10:00:00Z");

        progress.snapshotRequiredReadingSeconds(300);
        progress.snapshotRequiredReadingSeconds(600); // idempotent
        assertThat(progress.getRequiredReadingSeconds()).isEqualTo(300);

        progress.updateMaxBlockPosition(2);
        progress.updateMaxBlockPosition(5);
        progress.updateMaxBlockPosition(3); // stays 5
        assertThat(progress.getMaxBlockPosition()).isEqualTo(5);

        progress.markReachedEnd(now);
        assertThat(progress.isReachedEnd()).isTrue();
        assertThat(progress.getReachedEndAt()).isEqualTo(now);

        progress.markReadCompleted(now);
        assertThat(progress.isReadCompleted()).isTrue();
        assertThat(progress.getReadCompletedAt()).isEqualTo(now);

        progress.waive(now);
        assertThat(progress.isWaived()).isTrue();
        assertThat(progress.getPathWaivedAt()).isEqualTo(now);
    }
}
