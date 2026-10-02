package uz.aifu.lms.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public final class SearchDtos {

    private SearchDtos() {}

    @Schema(description = "One ranked search hit")
    public record SearchResultView(
            String resultType, UUID entityId, String title, String snippet, double rank) {}
}
