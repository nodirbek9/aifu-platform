package uz.aifu.lms.academics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.academics.domain.LearningOutcomeCategory;
import uz.aifu.lms.academics.domain.RequirementType;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.common.domain.PublicationStatus;

public final class SubjectDtos {

    private SubjectDtos() {}

    @Schema(description = "Create a subject (\"fan\"). Admin only.")
    public record CreateSubjectRequest(
            @Schema(example = "DAS1110") @NotBlank @Size(max = 32) String code,
            @NotNull UUID departmentId,
            SubjectKind subjectKind,
            @Schema(
                            example = "uz",
                            allowableValues = {"uz", "ru", "en"})
                    String defaultInstructionLanguage) {}

    public record UpdateSubjectRequest(
            UUID departmentId, SubjectKind subjectKind, String defaultInstructionLanguage) {}

    @Schema(description = "Upsert one locale's subject copy.")
    public record UpsertSubjectTranslationRequest(
            @NotBlank @Size(max = 500) String title,
            @Size(max = 500) String shortTitle,
            String summary,
            String description,
            @Schema(description = "\"Fan maqsadi\"") String aim,
            @Schema(description = "\"Fanning vazifasi\"") String objectives) {}

    public record AddPrerequisiteRequest(
            UUID prerequisiteSubjectId,
            RequirementType requirementType,
            @Size(max = 1000) String freeTextNote) {}

    public record AddLearningOutcomeRequest(
            @NotBlank @Size(max = 32) String code,
            @NotNull LearningOutcomeCategory category,
            int displayOrder) {}

    public record UpsertLearningOutcomeTranslationRequest(@NotBlank String statement) {}

    public record ChangeSubjectStatusRequest(@NotNull PublicationStatus status) {}

    @Schema(description = "Subject summary for catalogue lists")
    public record SubjectSummary(
            UUID id,
            String code,
            String departmentName,
            SubjectKind subjectKind,
            PublicationStatus status,
            String title,
            boolean titleIsFallback) {}

    @Schema(description = "Full subject detail")
    public record SubjectDetail(
            UUID id,
            String code,
            UUID departmentId,
            String departmentName,
            SubjectKind subjectKind,
            String defaultInstructionLanguage,
            PublicationStatus status,
            Instant publishedAt,
            List<SubjectTranslationView> translations,
            List<LearningOutcomeView> learningOutcomes,
            List<PrerequisiteView> prerequisites) {}

    public record SubjectTranslationView(
            String locale,
            String title,
            String shortTitle,
            String summary,
            String description,
            String aim,
            String objectives) {}

    public record LearningOutcomeView(
            UUID id,
            String code,
            LearningOutcomeCategory category,
            int displayOrder,
            List<OutcomeTranslationView> translations) {}

    public record OutcomeTranslationView(String locale, String statement) {}

    public record PrerequisiteView(
            UUID id,
            UUID prerequisiteSubjectId,
            String prerequisiteSubjectCode,
            RequirementType requirementType,
            String freeTextNote) {}
}
