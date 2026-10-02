package uz.aifu.lms.execution.domain;

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
import uz.aifu.lms.coding.domain.CodingTaskTestCase;

/**
 * One test case's outcome within one {@link ExecutionResult} (V011), read-only from {@code api}'s
 * perspective (written by {@code execution-worker}, restricted role V014). {@link
 * #stdoutExcerpt}/{@link #stderrExcerpt} are captured for teachers/debugging — the student-facing
 * mapper omits both, plus {@link CodingTaskTestCase#getExpectedStdout()}, for any test case whose
 * {@code visibility} is {@code HIDDEN} (V011 table comment, skills/coding-execution-security).
 */
@Entity
@Table(name = "execution_test_results")
public class ExecutionTestResult {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_result_id", nullable = false, updatable = false)
    private ExecutionResult executionResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_case_id", nullable = false, updatable = false)
    private CodingTaskTestCase testCase;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = false)
    private TestVerdict verdict;

    @Column(name = "time_ms")
    private Integer timeMs;

    @Column(name = "memory_kb")
    private Integer memoryKb;

    @Column(name = "exit_code")
    private Integer exitCode;

    @Column(name = "stdout_excerpt")
    private String stdoutExcerpt;

    @Column(name = "stderr_excerpt")
    private String stderrExcerpt;

    @Column(name = "points_awarded", nullable = false)
    private BigDecimal pointsAwarded = BigDecimal.ZERO;

    protected ExecutionTestResult() {}

    public UUID getId() {
        return id;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public CodingTaskTestCase getTestCase() {
        return testCase;
    }

    public boolean isPassed() {
        return passed;
    }

    public TestVerdict getVerdict() {
        return verdict;
    }

    public Integer getTimeMs() {
        return timeMs;
    }

    public Integer getMemoryKb() {
        return memoryKb;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getStdoutExcerpt() {
        return stdoutExcerpt;
    }

    public String getStderrExcerpt() {
        return stderrExcerpt;
    }

    public BigDecimal getPointsAwarded() {
        return pointsAwarded;
    }
}
