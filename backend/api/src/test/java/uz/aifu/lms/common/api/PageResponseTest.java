package uz.aifu.lms.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

    @Test
    void of_copiesEveryFieldFromSpringPage() {
        var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 7);

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(7);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isTrue();
    }

    @Test
    void of_withMapper_transformsContentButKeepsPagingMetadata() {
        var page = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(0, 3), 3);

        PageResponse<String> response = PageResponse.of(page, i -> "item-" + i);

        assertThat(response.content()).containsExactly("item-1", "item-2", "item-3");
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
    }
}
