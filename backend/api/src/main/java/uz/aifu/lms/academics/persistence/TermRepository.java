package uz.aifu.lms.academics.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.Term;

public interface TermRepository extends JpaRepository<Term, UUID> {

    @EntityGraph(attributePaths = "academicYear")
    List<Term> findByAcademicYear_IdOrderByOrdinal(UUID academicYearId);

    boolean existsByAcademicYear_IdAndOrdinal(UUID academicYearId, int ordinal);

    @EntityGraph(attributePaths = "academicYear")
    List<Term> findAllByOrderByAcademicYear_StartsOnDescOrdinalAsc();

    @EntityGraph(attributePaths = "academicYear")
    List<Term> findByStartsOnLessThanEqualAndEndsOnGreaterThanEqualOrderByStartsOnDesc(
            LocalDate startsOnOrBefore, LocalDate endsOnOrAfter);
}
