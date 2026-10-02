package uz.aifu.lms.worker.grading;

import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Applies one {@code coding_task_test_cases.comparison_mode} (V010) to an actual/expected stdout
 * pair. Mirrors the enum values of {@code coding.domain.ComparisonMode} in {@code api} — this
 * module cannot depend on {@code api}, so the mode is carried as the raw DB string.
 */
public final class OutputComparator {

    private OutputComparator() {}

    public static boolean matches(
            String actual, String expected, String mode, BigDecimal tolerance) {
        String a = actual == null ? "" : actual;
        String e = expected == null ? "" : expected;
        return switch (mode) {
            case "EXACT" -> a.equals(e);
            case "TRIMMED" -> a.strip().equals(e.strip());
            case "IGNORE_WHITESPACE" -> stripWhitespace(a).equals(stripWhitespace(e));
            case "CASE_INSENSITIVE" -> a.strip().equalsIgnoreCase(e.strip());
            case "NUMERIC_TOLERANCE" -> numericMatches(a, e, tolerance);
            case "REGEX" -> regexMatches(a, e);
            default -> throw new IllegalArgumentException("Unknown comparison mode: " + mode);
        };
    }

    private static String stripWhitespace(String value) {
        return value.replaceAll("\\s+", "");
    }

    private static boolean numericMatches(String actual, String expected, BigDecimal tolerance) {
        try {
            BigDecimal a = new BigDecimal(actual.strip());
            BigDecimal e = new BigDecimal(expected.strip());
            BigDecimal t = tolerance == null ? BigDecimal.ZERO : tolerance;
            return a.subtract(e).abs().compareTo(t) <= 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean regexMatches(String actual, String expected) {
        try {
            return Pattern.compile(expected).matcher(actual.strip()).matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
}
