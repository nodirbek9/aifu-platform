package uz.aifu.lms.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RequestIdFilterTest {

    @Test
    void keepsASafeId() {
        assertThat(RequestIdFilter.sanitize("abc-123_x.y")).isEqualTo("abc-123_x.y");
    }

    /** Stripping makes the id shorter than what was sent; the cap must follow the stripped text. */
    @Test
    void stripsUnsafeCharacters_withoutFailingOnTheShorterResult() {
        assertThat(RequestIdFilter.sanitize("abc$%\n<script>")).isEqualTo("abcscript");
        assertThat(RequestIdFilter.sanitize("$%^&*")).isEmpty();
    }

    @Test
    void capsTheLength() {
        assertThat(RequestIdFilter.sanitize("a".repeat(200))).hasSize(64);
    }

    @Test
    void nothingSent_isEmpty() {
        assertThat(RequestIdFilter.sanitize(null)).isEmpty();
        assertThat(RequestIdFilter.sanitize("  ")).isEmpty();
    }
}
