package uz.aifu.lms.curriculum.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.curriculum.domain.LessonPrerequisite;
import uz.aifu.lms.curriculum.domain.LessonPrerequisiteId;

public interface LessonPrerequisiteRepository
        extends JpaRepository<LessonPrerequisite, LessonPrerequisiteId> {

    @EntityGraph(attributePaths = "requiredLesson")
    List<LessonPrerequisite> findByLesson_Id(UUID lessonId);

    boolean existsByLesson_IdAndRequiredLesson_Id(UUID lessonId, UUID requiredLessonId);

    void deleteByLesson_IdAndRequiredLesson_Id(UUID lessonId, UUID requiredLessonId);
}
