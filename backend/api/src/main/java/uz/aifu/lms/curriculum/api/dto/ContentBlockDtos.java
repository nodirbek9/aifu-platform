package uz.aifu.lms.curriculum.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.curriculum.domain.BlockVisibility;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

public final class ContentBlockDtos {

    private ContentBlockDtos() {}

    @Schema(
            description =
                    "Create a content block within a lesson. Admin/teacher only. `settings` is "
                            + "non-localized, type-specific configuration validated per block_type — see docs/domain/domain-model.md.")
    public record CreateContentBlockRequest(
            @NotNull ContentBlockType blockType,
            JsonNode settings,
            UUID fileId,
            boolean isRequired,
            BlockVisibility visibility) {}

    public record UpdateContentBlockRequest(
            JsonNode settings, UUID fileId, boolean isRequired, BlockVisibility visibility) {}

    @Schema(
            description =
                    "Upsert one locale's block content. `payload`'s shape depends on the block's type.")
    public record UpsertContentBlockTranslationRequest(@NotNull JsonNode payload) {}

    public record ReorderContentBlocksRequest(@NotEmpty List<UUID> orderedBlockIds) {}

    @Schema(
            description =
                    "A content block as rendered for one locale (with fallback), inline in a lesson.")
    public record ContentBlockView(
            UUID id,
            int position,
            ContentBlockType blockType,
            boolean required,
            BlockVisibility visibility,
            JsonNode settings,
            UUID fileId,
            JsonNode payload,
            boolean payloadIsFallback) {}
}
