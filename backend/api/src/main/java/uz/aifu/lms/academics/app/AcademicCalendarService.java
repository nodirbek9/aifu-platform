package uz.aifu.lms.academics.app;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.api.dto.AcademicCalendarDtos.CreateAcademicYearRequest;
import uz.aifu.lms.academics.api.dto.AcademicCalendarDtos.CreateTermRequest;
import uz.aifu.lms.academics.domain.AcademicYear;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.academics.persistence.AcademicYearRepository;
import uz.aifu.lms.academics.persistence.TermRepository;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * Minimal academic-calendar administration: {@link AcademicYear} and {@link Term} are prerequisites
 * for creating a {@code Course} (V005 {@code courses.term_id} is NOT NULL). Real calendar dates are
 * pending from AIFU (PENDING-INPUTS.md §4); this only manages the rows, not their accuracy.
 */
@Service
public class AcademicCalendarService {

    private final AcademicYearRepository academicYears;
    private final TermRepository terms;

    public AcademicCalendarService(AcademicYearRepository academicYears, TermRepository terms) {
        this.academicYears = academicYears;
        this.terms = terms;
    }

    @Transactional(readOnly = true)
    public List<AcademicYear> listAcademicYears() {
        return academicYears.findAllByOrderByStartsOnDesc();
    }

    @Transactional(readOnly = true)
    public AcademicYear requireAcademicYear(UUID id) {
        return academicYears
                .findById(id)
                .orElseThrow(() -> Exceptions.notFound("AcademicYear", id));
    }

    @Transactional
    public AcademicYear createAcademicYear(CreateAcademicYearRequest request) {
        if (academicYears.existsByCode(request.code())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This academic year code already exists");
        }
        return academicYears.save(
                new AcademicYear(
                        request.code(),
                        request.startsOn(),
                        request.endsOn(),
                        request.placeholder()));
    }

    @Transactional
    public AcademicYear markCurrent(UUID id) {
        academicYears.findByCurrentTrue().ifPresent(AcademicYear::unmarkCurrent);
        AcademicYear year = requireAcademicYear(id);
        year.markCurrent();
        return year;
    }

    @Transactional(readOnly = true)
    public List<Term> listTerms(UUID academicYearId) {
        return terms.findByAcademicYear_IdOrderByOrdinal(academicYearId);
    }

    @Transactional(readOnly = true)
    public List<Term> listAllTerms() {
        return terms.findAllByOrderByAcademicYear_StartsOnDescOrdinalAsc();
    }

    @Transactional(readOnly = true)
    public Term requireTerm(UUID id) {
        return terms.findById(id).orElseThrow(() -> Exceptions.notFound("Term", id));
    }

    @Transactional
    public Term createTerm(UUID academicYearId, CreateTermRequest request) {
        AcademicYear year = requireAcademicYear(academicYearId);
        if (terms.existsByAcademicYear_IdAndOrdinal(academicYearId, request.ordinal())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "A term with this ordinal already exists for the academic year");
        }
        Term term =
                new Term(
                        year,
                        request.ordinal(),
                        request.name(),
                        request.startsOn(),
                        request.endsOn(),
                        request.placeholder());
        return terms.save(term);
    }
}
