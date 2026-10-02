package uz.aifu.lms.attendance.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.attendance.domain.ClassSession;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

    Optional<ClassSession> findByCourseIdAndOnDateAndPairNoAndGroupsKeyAndSubgroup(
            UUID courseId, LocalDate onDate, int pairNo, String groupsKey, String subgroup);

    /** A course has at most a few hundred classes in a term; filtering happens in memory. */
    @EntityGraph(attributePaths = "groupIds")
    @Query(
            "select distinct s from ClassSession s where s.courseId = :courseId"
                    + " order by s.onDate, s.pairNo")
    List<ClassSession> findAllOfCourse(@Param("courseId") UUID courseId);

    List<ClassSession> findByIdIn(Collection<UUID> ids);

    /**
     * Serializes the first save of one class: two teachers opening the same class at once must not
     * both insert its session row. The lock is released when the transaction ends.
     */
    @Query(
            value =
                    "select count(*) from (select pg_advisory_xact_lock(hashtextextended(:key, 0)))"
                            + " as locked",
            nativeQuery = true)
    long lockKey(@Param("key") String key);
}
