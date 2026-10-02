package uz.aifu.lms.execution.domain;

/** What an ungraded run executes against (V022). */
public enum CodeRunMode {
    /** The caller's code with the stdin they typed. */
    CUSTOM,
    /** The caller's code against the task's example test cases — never the hidden ones. */
    EXAMPLES,
    /** A teacher's reference solution against every test case of the task. */
    VERIFY
}
