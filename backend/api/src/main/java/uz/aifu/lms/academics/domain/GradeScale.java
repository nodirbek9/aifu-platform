package uz.aifu.lms.academics.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Read-mostly reference data: the seeded {@code AIFU_100_5} scale is the only one either supplied
 * syllabus uses. No {@code created_at}/{@code updated_at} in the table.
 */
@Entity
@Table(name = "grade_scales")
public class GradeScale {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    @Column(name = "pass_score", nullable = false)
    private BigDecimal passScore;

    @OneToMany(
            mappedBy = "gradeScale",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("minScore DESC")
    private List<GradeScaleBand> bands = new ArrayList<>();

    protected GradeScale() {}

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public BigDecimal getPassScore() {
        return passScore;
    }

    public List<GradeScaleBand> getBands() {
        return bands;
    }
}
