package uz.aifu.lms.execution.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/** One test case's outcome within an ungraded run. Written by the execution worker only. */
@Entity
@Immutable
@Table(name = "code_run_test_results")
public class CodeRunTestResult {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "test_case_id", nullable = false)
    private UUID testCaseId;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "verdict", nullable = false)
    private String verdict;

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

    protected CodeRunTestResult() {}

    public UUID getId() {
        return id;
    }

    public UUID getRunId() {
        return runId;
    }

    public UUID getTestCaseId() {
        return testCaseId;
    }

    public int getPosition() {
        return position;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getVerdict() {
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
}
