package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * "O'quv yili", e.g. {@code "2025/2026"}. No {@code created_at}/{@code updated_at} in the table.
 */
@Entity
@Table(name = "academic_years")
public class AcademicYear {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on", nullable = false)
    private LocalDate endsOn;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    /** PENDING-INPUTS.md §4: real academic-calendar dates are not yet supplied by AIFU. */
    @Column(name = "is_placeholder", nullable = false)
    private boolean placeholder;

    protected AcademicYear() {}

    public AcademicYear(String code, LocalDate startsOn, LocalDate endsOn, boolean placeholder) {
        this.code = Guards.requiredText(code, "code");
        this.startsOn = Guards.required(startsOn, "startsOn");
        this.endsOn = Guards.required(endsOn, "endsOn");
        if (!endsOn.isAfter(startsOn)) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "endsOn must be after startsOn");
        }
        this.placeholder = placeholder;
    }

    public void markCurrent() {
        this.current = true;
    }

    public void unmarkCurrent() {
        this.current = false;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }
}
