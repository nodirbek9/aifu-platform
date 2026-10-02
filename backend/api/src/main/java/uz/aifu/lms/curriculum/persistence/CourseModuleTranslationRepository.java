package uz.aifu.lms.curriculum.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.curriculum.domain.CourseModuleTranslation;
import uz.aifu.lms.curriculum.domain.CourseModuleTranslationId;

public interface CourseModuleTranslationRepository
        extends JpaRepository<CourseModuleTranslation, CourseModuleTranslationId> {

    @Query("select t from CourseModuleTranslation t where t.module.id = :moduleId")
    List<CourseModuleTranslation> findAllByModuleId(@Param("moduleId") UUID moduleId);

    @Query("select t from CourseModuleTranslation t where t.module.id in :moduleIds")
    List<CourseModuleTranslation> findAllByModuleIds(
            @Param("moduleIds") Collection<UUID> moduleIds);

    @Query(
            "select t from CourseModuleTranslation t where t.module.id = :moduleId and t.id.locale = :locale")
    Optional<CourseModuleTranslation> findByModuleIdAndLocale(
            @Param("moduleId") UUID moduleId, @Param("locale") String locale);
}
