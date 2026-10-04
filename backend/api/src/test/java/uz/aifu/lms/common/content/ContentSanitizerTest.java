package uz.aifu.lms.common.content;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentSanitizerTest {

    private final ContentSanitizer sanitizer = new ContentSanitizer();

    @Test
    void selfCheckQuestions_keepTheirOpenOnDemandAnswer() {
        String html =
                "<details><summary>Indeks nechadan boshlanadi?</summary><p>0 dan.</p></details>";
        assertThat(sanitizer.sanitizeRichText(html)).isEqualTo(html);
    }

    @Test
    void attributesThatCouldRunScript_areStrippedFromDetails() {
        assertThat(
                        sanitizer.sanitizeRichText(
                                "<details ontoggle=\"x()\" open><summary>Q</summary></details>"))
                .doesNotContain("ontoggle");
    }
}
