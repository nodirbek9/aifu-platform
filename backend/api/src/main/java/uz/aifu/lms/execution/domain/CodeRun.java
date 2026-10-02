package uz.aifu.lms.execution.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * An ungraded run of some code in the sandbox ("Run", "Run examples", "Verify solutions").
 *
 * <p>Not a submission: no attempt number, no grade, no progress, and deleted after a retention
 * period. The API only inserts the row; the execution worker claims it and writes the outcome, so
 * every outcome column is read-only here.
 */
@Entity
@Table(name = "code_runs")
public class CodeRun {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "coding_task_id", updatable = false)
    private UUID codingTaskId;

    @Column(name = "language_code", nullable = false, updatable = false)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, updatable = false)
    private CodeRunMode mode;

    @Column(name = "source_code", nullable = false, updatable = false)
    private String sourceCode;

    @Column(name = "stdin", nullable = false, updatable = false)
    private String stdin = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CodeRunStatus status = CodeRunStatus.QUEUED;

    @Column(name = "available_at", nullable = false, updatable = false)
    private Instant availableAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", insertable = false, updatable = false)
    private CodeRunVerdict verdict;

    @Column(name = "compile_output", insertable = false, updatable = false)
    private String compileOutput;

    @Column(name = "stdout", insertable = false, updatable = false)
    private String stdout;

    @Column(name = "stderr", insertable = false, updatable = false)
    private String stderr;

    @Column(name = "exit_code", insertable = false, updatable = false)
    private Integer exitCode;

    @Column(name = "time_ms", insertable = false, updatable = false)
    private Integer timeMs;

    @Column(name = "memory_kb", insertable = false, updatable = false)
    private Integer memoryKb;

    @Column(name = "tests_passed", insertable = false, updatable = false)
    private Integer testsPassed;

    @Column(name = "tests_total", insertable = false, updatable = false)
    private Integer testsTotal;

    @Column(name = "internal_error")
    private String internalError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected CodeRun() {}

    public CodeRun(
            UUID userId,
            UUID codingTaskId,
            String languageCode,
            CodeRunMode mode,
            String sourceCode,
            String stdin,
            Instant now) {
        this.userId = Guards.required(userId, "userId");
        this.codingTaskId = codingTaskId;
        this.languageCode = Guards.requiredText(languageCode, "languageCode");
        this.mode = Guards.required(mode, "mode");
        this.sourceCode = Guards.requiredText(sourceCode, "sourceCode");
        this.stdin = stdin == null ? "" : stdin;
        this.createdAt = now;
        this.availableAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCodingTaskId() {
        return codingTaskId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public CodeRunMode getMode() {
        return mode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getStdin() {
        return stdin;
    }

    public CodeRunStatus getStatus() {
        return status;
    }

    public CodeRunVerdict getVerdict() {
        return verdict;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public Integer getTimeMs() {
        return timeMs;
    }

    public Integer getMemoryKb() {
        return memoryKb;
    }

    public Integer getTestsPassed() {
        return testsPassed;
    }

    public Integer getTestsTotal() {
        return testsTotal;
    }

    public String getInternalError() {
        return internalError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
