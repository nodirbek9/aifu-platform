package uz.aifu.lms.worker.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OutputComparatorTest {

    @Test
    void exactRequiresByteForByteMatch() {
        assertThat(OutputComparator.matches("6", "6", "EXACT", null)).isTrue();
        assertThat(OutputComparator.matches("6\n", "6", "EXACT", null)).isFalse();
        assertThat(OutputComparator.matches("6 ", "6", "EXACT", null)).isFalse();
    }

    @Test
    void trimmedIgnoresLeadingTrailingWhitespaceOnly() {
        assertThat(OutputComparator.matches("6\n", "6", "TRIMMED", null)).isTrue();
        assertThat(OutputComparator.matches("  6  ", "6", "TRIMMED", null)).isTrue();
        assertThat(OutputComparator.matches("6 7", "67", "TRIMMED", null)).isFalse();
    }

    @Test
    void ignoreWhitespaceStripsAllWhitespace() {
        assertThat(OutputComparator.matches("6 7\n8", "678", "IGNORE_WHITESPACE", null)).isTrue();
    }

    @Test
    void caseInsensitiveIgnoresCaseAfterTrim() {
        assertThat(OutputComparator.matches(" Hello \n", "hello", "CASE_INSENSITIVE", null))
                .isTrue();
        assertThat(OutputComparator.matches("Hello", "World", "CASE_INSENSITIVE", null)).isFalse();
    }

    @Test
    void numericToleranceAcceptsWithinBound() {
        assertThat(
                        OutputComparator.matches(
                                "3.14159", "3.14160", "NUMERIC_TOLERANCE", new BigDecimal("0.001")))
                .isTrue();
        assertThat(
                        OutputComparator.matches(
                                "3.14159", "3.2", "NUMERIC_TOLERANCE", new BigDecimal("0.001")))
                .isFalse();
    }

    @Test
    void numericToleranceRejectsNonNumericOutput() {
        assertThat(
                        OutputComparator.matches(
                                "not-a-number", "3", "NUMERIC_TOLERANCE", BigDecimal.ONE))
                .isFalse();
    }

    @Test
    void regexMatchesFullTrimmedString() {
        assertThat(OutputComparator.matches(" ok123 \n", "ok\\d+", "REGEX", null)).isTrue();
        assertThat(OutputComparator.matches("xok123", "ok\\d+", "REGEX", null)).isFalse();
    }

    @Test
    void regexRejectsOnInvalidPattern() {
        assertThat(OutputComparator.matches("anything", "(unterminated", "REGEX", null)).isFalse();
    }
}
