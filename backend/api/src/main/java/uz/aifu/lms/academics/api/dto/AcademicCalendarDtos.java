package uz.aifu.lms.academics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public final class AcademicCalendarDtos {

    private AcademicCalendarDtos() {}

    @Schema(description = "Create an academic year (\"o'quv yili\"). Admin only.")
    public record CreateAcademicYearRequest(
            @Schema(example = "2025/2026") @NotBlank String code,
            @NotNull LocalDate startsOn,
            @NotNull LocalDate endsOn,
            boolean placeholder) {}

    @Schema(description = "Create a term (\"semestr\") within an academic year. Admin only.")
    public record CreateTermRequest(
            @Min(1) @Max(4) int ordinal,
            @NotBlank String name,
            @NotNull LocalDate startsOn,
            @NotNull LocalDate endsOn,
            boolean placeholder) {}

    public record AcademicYearView(
            UUID id,
            String code,
            LocalDate startsOn,
            LocalDate endsOn,
            boolean current,
            boolean placeholder) {}

    public record TermView(
            UUID id,
            UUID academicYearId,
            String academicYearCode,
            int ordinal,
            String name,
            LocalDate startsOn,
            LocalDate endsOn,
            boolean placeholder) {}
}
