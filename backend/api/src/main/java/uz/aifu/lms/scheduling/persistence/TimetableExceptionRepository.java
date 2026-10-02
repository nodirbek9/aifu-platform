package uz.aifu.lms.scheduling.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.scheduling.domain.TimetableException;

public interface TimetableExceptionRepository extends JpaRepository<TimetableException, UUID> {

    /** Exceptions that take a class out of, or bring a class into, the given date range. */
    @EntityGraph(attributePaths = {"entry", "newPairSlot", "newRoom"})
    @Query(
            """
            select x from TimetableException x
            where x.entry.timetable.id = :timetableId
              and ((x.onDate between :from and :to) or (x.newDate between :from and :to))
            """)
    List<TimetableException> findTouching(
            @Param("timetableId") UUID timetableId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"entry", "newPairSlot", "newRoom"})
    List<TimetableException> findByEntry_IdOrderByOnDate(UUID entryId);

    boolean existsByEntry_IdAndOnDate(UUID entryId, LocalDate onDate);
}
