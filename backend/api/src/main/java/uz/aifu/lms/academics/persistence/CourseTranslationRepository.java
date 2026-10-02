package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.CourseTranslation;
import uz.aifu.lms.academics.domain.CourseTranslationId;

public interface CourseTranslationRepository
        extends JpaRepository<CourseTranslation, CourseTranslationId> {

    @Query("select t from CourseTranslation t where t.course.id = :courseId")
    List<CourseTranslation> findAllByCourseId(@Param("courseId") UUID courseId);

    @Query("select t from CourseTranslation t where t.course.id in :courseIds")
    List<CourseTranslation> findAllByCourseIds(
            @Param("courseIds") java.util.Collection<UUID> courseIds);

    @Query(
            "select t from CourseTranslation t where t.course.id = :courseId and t.id.locale = :locale")
    Optional<CourseTranslation> findByCourseIdAndLocale(
            @Param("courseId") UUID courseId, @Param("locale") String locale);

    @Query(
            "select t.course.id as courseId, t.id.locale as locale from CourseTranslation t where t.course.id in :courseIds")
    List<CourseLocaleProjection> findLocalesForCourses(
            @Param("courseIds") java.util.Collection<UUID> courseIds);

    interface CourseLocaleProjection {
        UUID getCourseId();

        String getLocale();
    }
}
