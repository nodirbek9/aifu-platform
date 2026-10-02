package uz.aifu.lms.academics.persistence;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseStatus;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    @EntityGraph(attributePaths = {"subject", "term", "term.academicYear"})
    Optional<Course> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsBySubject_IdAndTerm_IdAndCohortCodeAndDeletedAtIsNull(
            UUID subjectId, UUID termId, String cohortCode);

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    /**
     * Backs {@code /teacher/courses} — the id set itself is the ownership scoping (already derived
     * from the caller's own id via {@code CourseTeacherRepository}), so no fetch-join pagination
     * risk here (only a to-one entity graph).
     */
    @EntityGraph(attributePaths = {"subject", "term"})
    Page<Course> findByIdInAndDeletedAtIsNull(Collection<UUID> ids, Pageable pageable);

    @Query("select c.status from Course c where c.id = :id and c.deletedAt is null")
    Optional<CourseStatus> findStatusByIdAndDeletedAtIsNull(@Param("id") UUID id);

    // Plain (non-fetch) join to course_translations for filtering only — see
    // SubjectRepository.search.
    @EntityGraph(attributePaths = {"subject", "term"})
    @Query(
            """
            select distinct c from Course c
            left join CourseTranslation t on t.course = c and t.id.locale = :locale
            where c.deletedAt is null
              and (:statuses is null or c.status in :statuses)
              and (:subjectId is null or c.subject.id = :subjectId)
              and (:termId is null or c.term.id = :termId)
              and (:pattern is null or lower(c.subject.code) like :pattern or lower(t.title) like :pattern)
            """)
    Page<Course> search(
            @Param("statuses") Collection<CourseStatus> statuses,
            @Param("subjectId") UUID subjectId,
            @Param("termId") UUID termId,
            @Param("locale") String locale,
            @Param("pattern") String pattern,
            Pageable pageable);
}
