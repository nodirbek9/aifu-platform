package uz.aifu.lms.curriculum.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.domain.GroupingSource;

public final class ModuleDtos {

    private ModuleDtos() {}

    @Schema(description = "Create a module within a course. Admin/teacher only.")
    public record CreateModuleRequest(String code, GroupingSource groupingSource) {}

    public record UpdateModuleRequest(
            String code,
            boolean isRequired,
            Integer estimatedMinutes,
            Instant availableFrom,
            GroupingSource groupingSource) {}

    public record UpsertModuleTranslationRequest(String title, String description) {}

    public record ChangeModuleStatusRequest(@NotNull PublicationStatus status) {}

    public record ReorderModulesRequest(@NotEmpty List<UUID> orderedModuleIds) {}

    @Schema(description = "Module summary for a course's module list")
    public record ModuleSummary(
            UUID id,
            UUID courseId,
            int position,
            String code,
            PublicationStatus status,
            boolean required,
            String title,
            boolean titleIsFallback) {}

    @Schema(description = "Full module detail")
    public record ModuleDetail(
            UUID id,
            UUID courseId,
            int position,
            String code,
            PublicationStatus status,
            boolean required,
            Integer estimatedMinutes,
            Instant availableFrom,
            GroupingSource groupingSource,
            Instant publishedAt,
            List<ModuleTranslationView> translations) {}

    public record ModuleTranslationView(String locale, String title, String description) {}
}
