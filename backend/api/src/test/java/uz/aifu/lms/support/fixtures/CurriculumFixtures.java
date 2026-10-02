package uz.aifu.lms.support.fixtures;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.CreateLessonRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.UpsertLessonTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.CreateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpsertModuleTranslationRequest;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.app.ModuleService;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.Lesson;

/**
 * Builds a real, translated module/lesson through the actual {@code curriculum} app services — same
 * rationale as {@link EnrollmentFixtures}: tests need this repeatedly and it must genuinely satisfy
 * the services' own validation, not drift from it via raw inserts.
 */
@Component
public class CurriculumFixtures {

    private final ModuleService modules;
    private final LessonService lessons;

    public CurriculumFixtures(ModuleService modules, LessonService lessons) {
        this.modules = modules;
        this.lessons = lessons;
    }

    /**
     * A module with a {@code uz} translation, NOT published (caller decides whether to publish).
     */
    @Transactional
    public CourseModule module(Course course) {
        UUID actor = UUID.randomUUID();
        CourseModule module =
                modules.create(course.getId(), new CreateModuleRequest(null, null), actor, null);
        modules.upsertTranslation(
                module.getId(),
                "uz",
                new UpsertModuleTranslationRequest("Fixture module", null),
                actor,
                null);
        return module;
    }

    /** A lesson with a {@code uz} translation, NOT published. */
    @Transactional
    public Lesson lesson(CourseModule module) {
        UUID actor = UUID.randomUUID();
        Lesson lesson =
                lessons.create(
                        module.getId(),
                        new CreateLessonRequest(null, SessionType.LECTURE),
                        actor,
                        null);
        lessons.upsertTranslation(
                lesson.getId(),
                "uz",
                new UpsertLessonTranslationRequest("Fixture lesson", null),
                actor,
                null);
        return lesson;
    }

    /** A PUBLISHED module (translated + published). */
    @Transactional
    public CourseModule publishedModule(Course course) {
        CourseModule module = module(course);
        modules.publish(module.getId(), UUID.randomUUID(), null);
        return module;
    }

    /** A PUBLISHED lesson within a PUBLISHED module. */
    @Transactional
    public Lesson publishedLesson(Course course) {
        CourseModule module = publishedModule(course);
        Lesson lesson = lesson(module);
        lessons.publish(lesson.getId(), UUID.randomUUID(), null);
        return lesson;
    }
}
