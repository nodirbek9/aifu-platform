package uz.aifu.lms.progress.app;

import java.util.UUID;
import org.springframework.stereotype.Component;
import uz.aifu.lms.iam.app.access.LessonGate;

/**
 * Adapter exposing {@link LearningPathService}'s gate checks to other modules via {@link
 * LessonGate} (Phase 5, B5, ADR-015).
 */
@Component
public class LessonGateAdapter implements LessonGate {

    private final LearningPathService learningPathService;

    public LessonGateAdapter(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @Override
    public void requireLessonOpen(UUID lessonId, UUID enrollmentId) {
        learningPathService.requireLessonOpen(lessonId, enrollmentId);
    }

    @Override
    public void requireActivityOpen(UUID activityId, UUID enrollmentId) {
        learningPathService.requireActivityOpen(activityId, enrollmentId);
    }
}
