package uz.aifu.lms.curriculum.app;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.domain.BlockVisibility;
import uz.aifu.lms.curriculum.domain.ContentBlock;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslation;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.persistence.ContentBlockRepository;
import uz.aifu.lms.curriculum.persistence.ContentBlockTranslationRepository;
import uz.aifu.lms.curriculum.persistence.LessonRepository;

/**
 * Estimates reading time in seconds for a lesson (Phase 5 B3).
 *
 * <p>Rule: words of student-visible blocks' plain_text divided by 200 words-per-minute, minimum 120
 * s. If the lesson has an explicit estimated_minutes > 0, that takes precedence (estimated_minutes
 * * 60 s).
 */
@Service
public class ReadingTimeEstimator {

    public static final int WORDS_PER_MINUTE = 200;
    public static final int MINIMUM_READING_SECONDS = 120;

    private final LessonRepository lessons;
    private final ContentBlockRepository contentBlocks;
    private final ContentBlockTranslationRepository translations;

    public ReadingTimeEstimator(
            LessonRepository lessons,
            ContentBlockRepository contentBlocks,
            ContentBlockTranslationRepository translations) {
        this.lessons = lessons;
        this.contentBlocks = contentBlocks;
        this.translations = translations;
    }

    @Transactional(readOnly = true)
    public int estimatedSeconds(UUID lessonId, String locale) {
        Lesson lesson =
                lessons.findByIdAndDeletedAtIsNull(lessonId)
                        .orElseThrow(() -> Exceptions.notFound("Lesson", lessonId));
        return estimatedSeconds(lesson, locale);
    }

    @Transactional(readOnly = true)
    public int estimatedSeconds(Lesson lesson, String locale) {
        if (lesson.getEstimatedMinutes() != null && lesson.getEstimatedMinutes() > 0) {
            return lesson.getEstimatedMinutes() * 60;
        }

        List<ContentBlock> blocks =
                contentBlocks.findByLesson_IdAndDeletedAtIsNullOrderByPosition(lesson.getId());
        List<ContentBlock> visibleBlocks =
                blocks.stream()
                        .filter(b -> b.getVisibility() != BlockVisibility.TEACHER_ONLY)
                        .toList();

        if (visibleBlocks.isEmpty()) {
            return MINIMUM_READING_SECONDS;
        }

        List<UUID> blockIds = visibleBlocks.stream().map(ContentBlock::getId).toList();
        List<ContentBlockTranslation> allTranslations = translations.findAllByBlockIds(blockIds);

        Map<UUID, List<ContentBlockTranslation>> byBlock =
                allTranslations.stream().collect(Collectors.groupingBy(t -> t.getBlock().getId()));

        String targetLocale =
                (locale != null && !locale.isBlank())
                        ? LocaleCode.find(locale)
                                .map(LocaleCode::code)
                                .orElse(locale.trim().toLowerCase())
                        : LocaleCode.DEFAULT.code();

        int totalWords = 0;
        for (ContentBlock block : visibleBlocks) {
            List<ContentBlockTranslation> blockTranslations = byBlock.get(block.getId());
            if (blockTranslations == null || blockTranslations.isEmpty()) {
                continue;
            }
            ContentBlockTranslation chosen =
                    blockTranslations.stream()
                            .filter(t -> t.getLocale().equalsIgnoreCase(targetLocale))
                            .findFirst()
                            .or(
                                    () ->
                                            blockTranslations.stream()
                                                    .filter(
                                                            t ->
                                                                    t.getLocale()
                                                                            .equalsIgnoreCase(
                                                                                    LocaleCode
                                                                                            .DEFAULT
                                                                                            .code()))
                                                    .findFirst())
                            .orElse(blockTranslations.get(0));

            if (chosen.getPlainText() != null) {
                totalWords += countWords(chosen.getPlainText());
            }
        }

        return estimateSecondsFromWords(totalWords);
    }

    public static int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String trimmed = text.trim();
        String[] tokens = trimmed.split("\\s+");
        return tokens.length;
    }

    public static int estimateSecondsFromWords(int totalWords) {
        int calculated = (int) Math.ceil((double) totalWords * 60.0 / WORDS_PER_MINUTE);
        return Math.max(MINIMUM_READING_SECONDS, calculated);
    }

    public static int estimateSecondsFromText(String text) {
        return estimateSecondsFromWords(countWords(text));
    }
}
