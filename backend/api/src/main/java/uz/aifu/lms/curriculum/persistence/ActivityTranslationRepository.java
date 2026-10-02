package uz.aifu.lms.curriculum.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.curriculum.domain.ActivityTranslation;
import uz.aifu.lms.curriculum.domain.ActivityTranslationId;

public interface ActivityTranslationRepository
        extends JpaRepository<ActivityTranslation, ActivityTranslationId> {

    @Query("select t from ActivityTranslation t where t.activity.id = :activityId")
    List<ActivityTranslation> findAllByActivityId(@Param("activityId") UUID activityId);

    @Query("select t from ActivityTranslation t where t.activity.id in :activityIds")
    List<ActivityTranslation> findAllByActivityIds(
            @Param("activityIds") Collection<UUID> activityIds);

    @Query(
            "select t from ActivityTranslation t where t.activity.id = :activityId and t.id.locale = :locale")
    Optional<ActivityTranslation> findByActivityIdAndLocale(
            @Param("activityId") UUID activityId, @Param("locale") String locale);
}
