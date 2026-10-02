package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/** "Semestr". No {@code created_at}/{@code updated_at} in the table. */
@Entity
@Table(name = "terms")
public class Term {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on", nullable = false)
    private LocalDate endsOn;

    @Column(name = "is_placeholder", nullable = false)
    private boolean placeholder;

    /** {@code ODD} or {@code EVEN}: the parity of the term's first week ("toq/juft hafta"). */
    @Column(name = "first_week_parity", nullable = false)
    private String firstWeekParity = "ODD";

    protected Term() {}

    public Term(
            AcademicYear academicYear,
            int ordinal,
            String name,
            LocalDate startsOn,
            LocalDate endsOn,
            boolean placeholder) {
        this.academicYear = Guards.required(academicYear, "academicYear");
        if (ordinal < 1 || ordinal > 4) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "ordinal must be between 1 and 4");
        }
        this.ordinal = ordinal;
        this.name = Guards.requiredText(name, "name");
        this.startsOn = Guards.required(startsOn, "startsOn");
        this.endsOn = Guards.required(endsOn, "endsOn");
        if (!endsOn.isAfter(startsOn)) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "endsOn must be after startsOn");
        }
        this.placeholder = placeholder;
    }

    public UUID getId() {
        return id;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public String getFirstWeekParity() {
        return firstWeekParity;
    }

    public void changeFirstWeekParity(String parity) {
        if (!"ODD".equals(parity) && !"EVEN".equals(parity)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "firstWeekParity must be ODD or EVEN");
        }
        this.firstWeekParity = parity;
    }
}
