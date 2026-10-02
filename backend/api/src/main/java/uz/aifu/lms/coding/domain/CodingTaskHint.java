package uz.aifu.lms.coding.domain;

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
import uz.aifu.lms.common.util.Guards;

/** Not a {@link uz.aifu.lms.common.domain.BaseEntity}: no audit columns on this table either. */
@Entity
@Table(name = "coding_task_hints")
public class CodingTaskHint {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coding_task_id", nullable = false, updatable = false)
    private CodingTask codingTask;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "penalty_percent", nullable = false)
    private BigDecimal penaltyPercent = BigDecimal.ZERO;

    protected CodingTaskHint() {}

    public CodingTaskHint(CodingTask codingTask, int position, BigDecimal penaltyPercent) {
        this.codingTask = Guards.required(codingTask, "codingTask");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.penaltyPercent = penaltyPercent == null ? BigDecimal.ZERO : penaltyPercent;
    }

    public void update(BigDecimal penaltyPercent) {
        this.penaltyPercent = penaltyPercent == null ? BigDecimal.ZERO : penaltyPercent;
    }

    public void reorderTo(int newPosition) {
        Guards.require(newPosition >= 0, "position must be >= 0");
        this.position = newPosition;
    }

    public UUID getId() {
        return id;
    }

    public CodingTask getCodingTask() {
        return codingTask;
    }

    public int getPosition() {
        return position;
    }

    public BigDecimal getPenaltyPercent() {
        return penaltyPercent;
    }
}
