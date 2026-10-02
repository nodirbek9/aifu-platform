package uz.aifu.lms.curriculum.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.curriculum.domain.LessonTranslation;
import uz.aifu.lms.curriculum.domain.LessonTranslationId;

public interface LessonTranslationRepository
        extends JpaRepository<LessonTranslation, LessonTranslationId> {

    @Query("select t from LessonTranslation t where t.lesson.id = :lessonId")
    List<LessonTranslation> findAllByLessonId(@Param("lessonId") UUID lessonId);

    @Query("select t from LessonTranslation t where t.lesson.id in :lessonIds")
    List<LessonTranslation> findAllByLessonIds(@Param("lessonIds") Collection<UUID> lessonIds);

    @Query(
            "select t from LessonTranslation t where t.lesson.id = :lessonId and t.id.locale = :locale")
    Optional<LessonTranslation> findByLessonIdAndLocale(
            @Param("lessonId") UUID lessonId, @Param("locale") String locale);
}
