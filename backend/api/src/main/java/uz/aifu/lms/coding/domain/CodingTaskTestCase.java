package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * Not a {@link uz.aifu.lms.common.domain.BaseEntity}: no audit columns. {@code HIDDEN} visibility:
 * neither {@link #expectedStdout} nor the actual run output is ever returned to a student (V010
 * table comment) — enforced by the student mapper, never here.
 */
@Entity
@Table(name = "coding_task_test_cases")
public class CodingTaskTestCase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coding_task_id", nullable = false, updatable = false)
    private CodingTask codingTask;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private TestCaseVisibility visibility = TestCaseVisibility.HIDDEN;

    @Column(name = "is_example", nullable = false)
    private boolean example;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "stdin", nullable = false)
    private String stdin = "";

    @Column(name = "expected_stdout", nullable = false)
    private String expectedStdout = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_mode", nullable = false)
    private ComparisonMode comparisonMode = ComparisonMode.TRIMMED;

    @Column(name = "numeric_tolerance")
    private BigDecimal numericTolerance;

    @Column(name = "cpu_time_limit_ms")
    private Integer cpuTimeLimitMs;

    protected CodingTaskTestCase() {}

    public CodingTaskTestCase(
            CodingTask codingTask, int position, String stdin, String expectedStdout) {
        this.codingTask = Guards.required(codingTask, "codingTask");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.stdin = stdin == null ? "" : stdin;
        this.expectedStdout = expectedStdout == null ? "" : expectedStdout;
    }

    public void update(
            String name,
            TestCaseVisibility visibility,
            boolean example,
            BigDecimal weight,
            String stdin,
            String expectedStdout,
            ComparisonMode comparisonMode,
            BigDecimal numericTolerance,
            Integer cpuTimeLimitMs) {
        this.name = name;
        this.visibility = visibility == null ? TestCaseVisibility.HIDDEN : visibility;
        this.example = example;
        Guards.require(weight != null && weight.signum() > 0, "weight must be > 0");
        this.weight = weight;
        this.stdin = stdin == null ? "" : stdin;
        this.expectedStdout = expectedStdout == null ? "" : expectedStdout;
        this.comparisonMode = comparisonMode == null ? ComparisonMode.TRIMMED : comparisonMode;
        if (this.comparisonMode == ComparisonMode.NUMERIC_TOLERANCE && numericTolerance == null) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "NUMERIC_TOLERANCE requires numericTolerance");
        }
        this.numericTolerance = numericTolerance;
        this.cpuTimeLimitMs = cpuTimeLimitMs;
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

    public String getName() {
        return name;
    }

    public TestCaseVisibility getVisibility() {
        return visibility;
    }

    public boolean isExample() {
        return example;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public String getStdin() {
        return stdin;
    }

    public String getExpectedStdout() {
        return expectedStdout;
    }

    public ComparisonMode getComparisonMode() {
        return comparisonMode;
    }

    public BigDecimal getNumericTolerance() {
        return numericTolerance;
    }

    public Integer getCpuTimeLimitMs() {
        return cpuTimeLimitMs;
    }
}
