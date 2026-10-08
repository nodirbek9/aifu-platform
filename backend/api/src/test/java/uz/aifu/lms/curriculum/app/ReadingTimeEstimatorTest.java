package uz.aifu.lms.curriculum.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.curriculum.domain.BlockVisibility;
import uz.aifu.lms.curriculum.domain.ContentBlock;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslation;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.persistence.ContentBlockRepository;
import uz.aifu.lms.curriculum.persistence.ContentBlockTranslationRepository;
import uz.aifu.lms.curriculum.persistence.LessonRepository;

class ReadingTimeEstimatorTest {

    private LessonRepository lessons;
    private ContentBlockRepository contentBlocks;
    private ContentBlockTranslationRepository translations;
    private ReadingTimeEstimator estimator;

    @BeforeEach
    void setUp() {
        lessons = mock(LessonRepository.class);
        contentBlocks = mock(ContentBlockRepository.class);
        translations = mock(ContentBlockTranslationRepository.class);
        estimator = new ReadingTimeEstimator(lessons, contentBlocks, translations);
    }

    @Test
    @DisplayName("countWords handles null, whitespace, and multi-word sentences correctly")
    void countWords_worksAsExpected() {
        assertThat(ReadingTimeEstimator.countWords(null)).isZero();
        assertThat(ReadingTimeEstimator.countWords("")).isZero();
        assertThat(ReadingTimeEstimator.countWords("   \t\n  ")).isZero();
        assertThat(ReadingTimeEstimator.countWords("Salom dunyo")).isEqualTo(2);
        assertThat(ReadingTimeEstimator.countWords("   Bir    ikki   uch  to'rt  besh  "))
                .isEqualTo(5);
    }

    @Test
    @DisplayName("Short text gets minimum reading time of 120 seconds")
    void estimateSecondsFromText_clampsToMinimum120Seconds() {
        String shortText = "Bu qisqa matn bo'lib, uning so'zlar soni oz.";
        int seconds = ReadingTimeEstimator.estimateSecondsFromText(shortText);
        assertThat(seconds).isEqualTo(120);
    }

    @Test
    @DisplayName("Long text calculates 200 words-per-minute with ceiling rounding")
    void estimateSecondsFromText_calculatesAt200Wpm() {
        // 500 words at 200 wpm = 2.5 minutes = 150 seconds
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("soz ").append(i).append(" ");
        }
        // 1000 tokens
        int seconds = ReadingTimeEstimator.estimateSecondsFromText(sb.toString());
        // 1000 words * 60 / 200 = 300 seconds
        assertThat(seconds).isEqualTo(300);
    }

    @Test
    @DisplayName("Explicit lesson estimatedMinutes takes precedence")
    void lessonEstimatedMinutes_takesPrecedence() {
        Lesson lesson = mock(Lesson.class);
        when(lesson.getEstimatedMinutes()).thenReturn(15);

        int seconds = estimator.estimatedSeconds(lesson, "uz");

        // 15 minutes * 60 = 900 seconds
        assertThat(seconds).isEqualTo(900);
    }

    @Test
    @DisplayName(
            "Calculates words only from student-visible blocks and ignores teacher-only blocks")
    void ignoresTeacherOnlyBlocks() {
        Lesson lesson = mock(Lesson.class);
        UUID lessonId = UUID.randomUUID();
        when(lesson.getId()).thenReturn(lessonId);
        when(lesson.getEstimatedMinutes()).thenReturn(null);

        ContentBlock visibleBlock = mock(ContentBlock.class);
        UUID visibleBlockId = UUID.randomUUID();
        when(visibleBlock.getId()).thenReturn(visibleBlockId);
        when(visibleBlock.getVisibility()).thenReturn(BlockVisibility.ALL);

        ContentBlock teacherOnlyBlock = mock(ContentBlock.class);
        UUID teacherBlockId = UUID.randomUUID();
        when(teacherOnlyBlock.getId()).thenReturn(teacherBlockId);
        when(teacherOnlyBlock.getVisibility()).thenReturn(BlockVisibility.TEACHER_ONLY);

        when(contentBlocks.findByLesson_IdAndDeletedAtIsNullOrderByPosition(lessonId))
                .thenReturn(List.of(visibleBlock, teacherOnlyBlock));

        // Create 600 words for visible block (600 * 60 / 200 = 180s)
        StringBuilder visibleText = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            visibleText.append("word ");
        }

        ContentBlockTranslation translation = mock(ContentBlockTranslation.class);
        when(translation.getBlock()).thenReturn(visibleBlock);
        when(translation.getLocale()).thenReturn("uz");
        when(translation.getPlainText()).thenReturn(visibleText.toString());

        when(translations.findAllByBlockIds(any())).thenReturn(List.of(translation));

        int seconds = estimator.estimatedSeconds(lesson, "uz");

        assertThat(seconds).isEqualTo(180);
    }
}
