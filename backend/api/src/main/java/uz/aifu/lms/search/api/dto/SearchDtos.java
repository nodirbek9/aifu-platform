package uz.aifu.lms.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public final class SearchDtos {

    private SearchDtos() {}

    @Schema(description = "One ranked search hit")
    public record SearchResultView(
            @Schema(allowableValues = {"SUBJECT", "COURSE", "LESSON"}) String resultType,
            UUID entityId,
            @Schema(
                            description =
                                    "The course a hit is opened in: the course itself, the course"
                                            + " of a lesson; absent for a subject")
                    UUID courseId,
            String title,
            String snippet,
            double rank) {}
}
