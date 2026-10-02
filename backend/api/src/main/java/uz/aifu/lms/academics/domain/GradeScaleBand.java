package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "grade_scale_bands")
public class GradeScaleBand {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_scale_id", nullable = false, updatable = false)
    private GradeScale gradeScale;

    @Column(name = "min_score", nullable = false)
    private BigDecimal minScore;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "is_passing", nullable = false)
    private boolean passing;

    protected GradeScaleBand() {}

    public UUID getId() {
        return id;
    }

    public GradeScale getGradeScale() {
        return gradeScale;
    }

    public BigDecimal getMinScore() {
        return minScore;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPassing() {
        return passing;
    }
}
