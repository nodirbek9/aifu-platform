package uz.aifu.lms.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void of_populatesEveryField_fromTheErrorCode() {
        ApiError error =
                ApiError.of(
                        ErrorCode.ENROLLMENT_DUPLICATE,
                        "Already enrolled",
                        Map.of("courseId", "abc"),
                        "req-1",
                        "/api/v1/enrollments");

        assertThat(error.code()).isEqualTo("ENROLLMENT_DUPLICATE");
        assertThat(error.message()).isEqualTo("Already enrolled");
        assertThat(error.details()).containsEntry("courseId", "abc");
        assertThat(error.requestId()).isEqualTo("req-1");
        assertThat(error.path()).isEqualTo("/api/v1/enrollments");
        assertThat(error.timestamp()).isNotNull();
    }

    @Test
    void of_withEmptyDetails_storesNullNotAnEmptyMap() {
        ApiError error = ApiError.of(ErrorCode.INTERNAL_ERROR, "boom", Map.of(), "req-2", "/x");

        assertThat(error.details()).isNull();
    }

    @Test
    void of_withNullDetails_storesNull() {
        ApiError error = ApiError.of(ErrorCode.INTERNAL_ERROR, "boom", null, "req-2", "/x");

        assertThat(error.details()).isNull();
    }
}
