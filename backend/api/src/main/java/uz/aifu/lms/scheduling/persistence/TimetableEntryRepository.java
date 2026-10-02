package uz.aifu.lms.scheduling.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.scheduling.domain.TimetableEntry;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, UUID> {

    /** A timetable is a few hundred rows; filtering by group / teacher / room happens in memory. */
    @EntityGraph(attributePaths = {"room", "pairSlot", "groups"})
    @Query("select distinct e from TimetableEntry e where e.timetable.id = :timetableId")
    List<TimetableEntry> findAllOf(@Param("timetableId") UUID timetableId);

    @EntityGraph(attributePaths = {"timetable", "room", "pairSlot", "groups"})
    @Query("select e from TimetableEntry e where e.id = :id")
    Optional<TimetableEntry> findDetailedById(@Param("id") UUID id);

    long countByTimetable_Id(UUID timetableId);

    boolean existsByRoom_Id(UUID roomId);

    boolean existsByPairSlot_Id(UUID pairSlotId);

    @Query("select count(e) > 0 from TimetableEntry e join e.groups g where g.id = :groupId")
    boolean existsForGroup(@Param("groupId") UUID groupId);
}
