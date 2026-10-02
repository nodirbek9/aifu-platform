package uz.aifu.lms.curriculum.app;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.domain.LessonTranslation;
import uz.aifu.lms.curriculum.persistence.LessonRepository;
import uz.aifu.lms.curriculum.persistence.LessonTranslationRepository;

/**
 * Small read model of lessons for modules that only need to name a lesson or walk a course's
 * lessons in teaching order (class sessions, the calendar-thematic plan) without loading content.
 */
@Service
public class LessonDirectory {

    /** How a lesson is shown in a list: a title in the best available locale and its kind. */
    public record LessonLabel(
            UUID lessonId,
            UUID courseId,
            String title,
            SessionType sessionType,
            boolean published) {}

    private final LessonRepository lessons;
    private final LessonTranslationRepository translations;

    public LessonDirectory(LessonRepository lessons, LessonTranslationRepository translations) {
        this.lessons = lessons;
        this.translations = translations;
    }

    /** Labels for the given lessons; ids that do not exist are simply absent from the result. */
    @Transactional(readOnly = true)
    public Map<UUID, LessonLabel> labels(Collection<UUID> lessonIds, LocaleCode locale) {
        if (lessonIds.isEmpty()) {
            return Map.of();
        }
        return toLabels(lessons.findAllWithModuleByIdIn(lessonIds), locale).stream()
                .collect(
                        Collectors.toMap(
                                LessonLabel::lessonId, l -> l, (a, b) -> a, LinkedHashMap::new));
    }

    /** Every lesson of a course in teaching order. */
    @Transactional(readOnly = true)
    public List<LessonLabel> lessonsOfCourse(UUID courseId, LocaleCode locale) {
        return toLabels(lessons.findAllOfCourse(courseId), locale);
    }

    private List<LessonLabel> toLabels(List<Lesson> found, LocaleCode locale) {
        if (found.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<LessonTranslation>> byLesson =
                translations.findAllByLessonIds(found.stream().map(Lesson::getId).toList()).stream()
                        .collect(Collectors.groupingBy(t -> t.getLesson().getId()));
        return found.stream()
                .map(
                        l ->
                                new LessonLabel(
                                        l.getId(),
                                        l.getModule().getCourse().getId(),
                                        title(
                                                l,
                                                byLesson.getOrDefault(l.getId(), List.of()),
                                                locale),
                                        l.getSessionType(),
                                        l.getStatus().isVisibleToStudents()))
                .toList();
    }

    /** Requested locale, then Uzbek, then anything; else the lesson's code. */
    private static String title(Lesson lesson, List<LessonTranslation> all, LocaleCode locale) {
        Map<String, String> titles =
                all.stream()
                        .collect(
                                Collectors.toMap(
                                        LessonTranslation::getLocale,
                                        LessonTranslation::getTitle,
                                        (a, b) -> a));
        String title = titles.get(locale.code());
        if (title == null) {
            title = titles.get(LocaleCode.DEFAULT.code());
        }
        if (title == null) {
            title = titles.values().stream().findFirst().orElse(lesson.getCode());
        }
        return title;
    }
}
