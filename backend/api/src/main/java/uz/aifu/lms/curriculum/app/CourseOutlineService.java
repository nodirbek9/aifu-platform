package uz.aifu.lms.curriculum.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.CourseModuleTranslation;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.domain.LessonTranslation;
import uz.aifu.lms.curriculum.persistence.CourseModuleTranslationRepository;
import uz.aifu.lms.curriculum.persistence.LessonPrerequisiteRepository;
import uz.aifu.lms.curriculum.persistence.LessonRepository;
import uz.aifu.lms.curriculum.persistence.LessonTranslationRepository;

/**
 * A course's modules and lessons in teaching order, in a fixed number of queries. The course page
 * needs the whole tree at once; walking it module by module would cost a request per module.
 */
@Service
public class CourseOutlineService {

    public record OutlineLesson(
            UUID id,
            String code,
            String title,
            SessionType sessionType,
            BigDecimal allocatedHours,
            Integer estimatedMinutes,
            boolean required,
            List<UUID> prerequisiteLessonIds) {}

    public record OutlineModule(UUID id, String code, String title, List<OutlineLesson> lessons) {}

    private final LessonRepository lessons;
    private final LessonTranslationRepository lessonTranslations;
    private final CourseModuleTranslationRepository moduleTranslations;
    private final LessonPrerequisiteRepository prerequisites;

    public CourseOutlineService(
            LessonRepository lessons,
            LessonTranslationRepository lessonTranslations,
            CourseModuleTranslationRepository moduleTranslations,
            LessonPrerequisiteRepository prerequisites) {
        this.lessons = lessons;
        this.lessonTranslations = lessonTranslations;
        this.moduleTranslations = moduleTranslations;
        this.prerequisites = prerequisites;
    }

    /**
     * The published part of a course: published lessons of published modules. A module with no
     * published lesson is left out, since there is nothing in it to open.
     */
    @Transactional(readOnly = true)
    public List<OutlineModule> publishedOutline(UUID courseId, LocaleCode locale) {
        List<Lesson> found =
                lessons.findAllOfCourse(courseId).stream()
                        .filter(l -> l.getStatus().isVisibleToStudents())
                        .filter(l -> l.getModule().getStatus().isVisibleToStudents())
                        .toList();
        if (found.isEmpty()) {
            return List.of();
        }
        List<UUID> lessonIds = found.stream().map(Lesson::getId).toList();
        Map<UUID, List<LessonTranslation>> lessonTitles =
                lessonTranslations.findAllByLessonIds(lessonIds).stream()
                        .collect(Collectors.groupingBy(t -> t.getLesson().getId()));
        Map<UUID, CourseModule> modules = new LinkedHashMap<>();
        found.forEach(l -> modules.putIfAbsent(l.getModule().getId(), l.getModule()));
        Map<UUID, List<CourseModuleTranslation>> moduleTitles =
                moduleTranslations.findAllByModuleIds(modules.keySet()).stream()
                        .collect(Collectors.groupingBy(t -> t.getModule().getId()));
        Map<UUID, List<UUID>> requires = new LinkedHashMap<>();
        prerequisites
                .findPairs(lessonIds)
                .forEach(
                        p ->
                                requires.computeIfAbsent(p.getLessonId(), k -> new ArrayList<>())
                                        .add(p.getRequiredLessonId()));

        Map<UUID, List<OutlineLesson>> byModule = new LinkedHashMap<>();
        for (Lesson lesson : found) {
            byModule.computeIfAbsent(lesson.getModule().getId(), k -> new ArrayList<>())
                    .add(
                            new OutlineLesson(
                                    lesson.getId(),
                                    lesson.getCode(),
                                    pick(
                                            lessonTitles
                                                    .getOrDefault(lesson.getId(), List.of())
                                                    .stream()
                                                    .collect(
                                                            Collectors.toMap(
                                                                    LessonTranslation::getLocale,
                                                                    LessonTranslation::getTitle,
                                                                    (a, b) -> a)),
                                            locale,
                                            lesson.getCode()),
                                    lesson.getSessionType(),
                                    lesson.getAllocatedHours(),
                                    lesson.getEstimatedMinutes(),
                                    lesson.isRequired(),
                                    requires.getOrDefault(lesson.getId(), List.of())));
        }
        return modules.values().stream()
                .map(
                        m ->
                                new OutlineModule(
                                        m.getId(),
                                        m.getCode(),
                                        pick(
                                                moduleTitles
                                                        .getOrDefault(m.getId(), List.of())
                                                        .stream()
                                                        .collect(
                                                                Collectors.toMap(
                                                                        CourseModuleTranslation
                                                                                ::getLocale,
                                                                        CourseModuleTranslation
                                                                                ::getTitle,
                                                                        (a, b) -> a)),
                                                locale,
                                                m.getCode()),
                                        byModule.get(m.getId())))
                .toList();
    }

    /** Requested locale, then Uzbek, then anything; else the fallback (the item's code). */
    private static String pick(Map<String, String> titles, LocaleCode locale, String fallback) {
        String title = titles.get(locale.code());
        if (title == null) {
            title = titles.get(LocaleCode.DEFAULT.code());
        }
        if (title == null) {
            title = titles.values().stream().findFirst().orElse(fallback);
        }
        return title;
    }
}
