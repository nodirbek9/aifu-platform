package uz.aifu.lms.execution.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.coding.domain.Submission;

/**
 * The sandbox run's own record (V011) — written by {@code execution-worker} via plain JDBC through
 * the restricted role (V014), read-only from {@code api}'s perspective. {@code compile_output} is
 * already truncated to {@code aifu.execution.max-output-bytes} by the worker before it ever reaches
 * this row.
 */
@Entity
@Table(name = "execution_results")
public class ExecutionResult {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, updatable = false)
    private Submission submission;

    @Column(name = "engine", nullable = false)
    private String engine;

    @Column(name = "engine_version")
    private String engineVersion;

    @Column(name = "sandbox_profile", nullable = false)
    private String sandboxProfile;

    @Column(name = "compile_success")
    private Boolean compileSuccess;

    @Column(name = "compile_output")
    private String compileOutput;

    @Column(name = "exit_code")
    private Integer exitCode;

    @Column(name = "total_time_ms")
    private Integer totalTimeMs;

    @Column(name = "max_memory_kb")
    private Integer maxMemoryKb;

    @Column(name = "output_truncated", nullable = false)
    private boolean outputTruncated;

    @Column(name = "internal_error")
    private String internalError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ExecutionResult() {}

    public UUID getId() {
        return id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public String getEngine() {
        return engine;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public String getSandboxProfile() {
        return sandboxProfile;
    }

    public Boolean getCompileSuccess() {
        return compileSuccess;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public Integer getTotalTimeMs() {
        return totalTimeMs;
    }

    public Integer getMaxMemoryKb() {
        return maxMemoryKb;
    }

    public boolean isOutputTruncated() {
        return outputTruncated;
    }

    public String getInternalError() {
        return internalError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
