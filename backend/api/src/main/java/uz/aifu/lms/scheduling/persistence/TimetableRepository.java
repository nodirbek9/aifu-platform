package uz.aifu.lms.scheduling.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.scheduling.domain.Timetable;
import uz.aifu.lms.scheduling.domain.TimetableStatus;

public interface TimetableRepository extends JpaRepository<Timetable, UUID> {

    List<Timetable> findAllByOrderByCreatedAtDesc();

    List<Timetable> findByTermIdOrderByCreatedAtDesc(UUID termId);

    Optional<Timetable> findByTermIdAndStatus(UUID termId, TimetableStatus status);

    /**
     * Serializes writers of one timetable: the conflict check and the insert that follows it must
     * not interleave with another admin's, or both could book the same teacher or room.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Timetable t where t.id = :id")
    Optional<Timetable> lockById(@Param("id") UUID id);
}
