package uz.aifu.lms.coding.domain;

/**
 * {@code HIDDEN}: neither {@code expected_stdout} nor the actual stdout/stderr is ever returned to
 * a student — only pass/fail, time, and the verdict are (V010 table comment,
 * skills/coding-execution-security/SKILL.md). Teachers see everything regardless.
 */
public enum TestCaseVisibility {
    VISIBLE,
    HIDDEN
}
