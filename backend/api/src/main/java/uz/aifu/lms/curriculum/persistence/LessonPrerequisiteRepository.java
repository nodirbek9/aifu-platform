package uz.aifu.lms.curriculum.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.curriculum.domain.LessonPrerequisite;
import uz.aifu.lms.curriculum.domain.LessonPrerequisiteId;

public interface LessonPrerequisiteRepository
        extends JpaRepository<LessonPrerequisite, LessonPrerequisiteId> {

    @EntityGraph(attributePaths = "requiredLesson")
    List<LessonPrerequisite> findByLesson_Id(UUID lessonId);

    boolean existsByLesson_IdAndRequiredLesson_Id(UUID lessonId, UUID requiredLessonId);

    /** Which lesson requires which, for many lessons at once (ids only, nothing is loaded). */
    @Query(
            "select p.lesson.id as lessonId, p.requiredLesson.id as requiredLessonId"
                    + " from LessonPrerequisite p where p.lesson.id in :lessonIds")
    List<PrerequisitePair> findPairs(@Param("lessonIds") Collection<UUID> lessonIds);

    interface PrerequisitePair {
        UUID getLessonId();

        UUID getRequiredLessonId();
    }

    void deleteByLesson_IdAndRequiredLesson_Id(UUID lessonId, UUID requiredLessonId);
}
