package uz.aifu.lms.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uz.aifu.lms.common.config.ApiProperties;

class PageableSupportTest {

    private final PageableSupport support =
            new PageableSupport(new ApiProperties("/api/v1", 100, 20));

    @Test
    void sanitize_unpaged_fallsBackToDefaultPageSize() {
        Pageable result = support.sanitize(Pageable.unpaged());

        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(20);
    }

    @Test
    void sanitize_requestedSizeAboveCap_isClampedToMax() {
        Pageable result = support.sanitize(PageRequest.of(0, 5_000));

        assertThat(result.getPageSize()).isEqualTo(100);
    }

    @Test
    void sanitize_negativePage_isClampedToZero() {
        Pageable result = support.sanitize(PageRequest.of(0, 20).previousOrFirst());

        assertThat(result.getPageNumber()).isZero();
    }

    @Test
    void sanitize_unsortedRequest_usesFallbackSort() {
        Pageable result =
                support.sanitize(PageRequest.of(0, 20), Sort.by("createdAt").descending());

        assertThat(result.getSort()).isEqualTo(Sort.by("createdAt").descending());
    }

    @Test
    void sanitize_sortedRequest_keepsRequestedSort() {
        Pageable result =
                support.sanitize(
                        PageRequest.of(0, 20, Sort.by("name").ascending()),
                        Sort.by("createdAt").descending());

        assertThat(result.getSort()).isEqualTo(Sort.by("name").ascending());
    }
}
