package uz.aifu.lms.analytics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.analytics.api.dto.AdminProblemDtos.AdminProblemView;
import uz.aifu.lms.analytics.app.AdminProblemsService;
import uz.aifu.lms.analytics.domain.AdminProblemView.AdminProblemType;
import uz.aifu.lms.common.api.PageResponse;

@RestController
@RequestMapping("/api/v1/admin/problems")
@Tag(name = "Admin: problems", description = "Platform-wide administrative problems")
@SecurityRequirement(name = "bearerAuth")
public class AdminProblemsController {

    private final AdminProblemsService problems;

    public AdminProblemsController(AdminProblemsService problems) {
        this.problems = problems;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ANALYTICS_PLATFORM_READ')")
    @Operation(
            summary = "List administrative problems, optionally filtered by type",
            description =
                    "Read-only discovery across platform resources. Omit type to list all six types."
                            + " Page size is capped at 100. Supported sort fields: type, problemKey."
                            + " Default order: type ASC, problemKey ASC; missing tie-breakers are"
                            + " appended to explicit sorting.")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated administrative problems",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            value =
                                                    """
                            {"content":[{"problemKey":"STUDENT_WITHOUT_GROUP:30000000-0000-0000-0000-000000000001",
                            "type":"STUDENT_WITHOUT_GROUP","userId":"30000000-0000-0000-0000-000000000001",
                            "fullName":"Example student"}],"page":0,"size":20,"totalElements":1,
                            "totalPages":1,"hasNext":false,"hasPrevious":false}
                            """)))
    @ApiResponse(responseCode = "400", description = "Invalid type or unsupported sort field")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "ANALYTICS_PLATFORM_READ permission required")
    public PageResponse<AdminProblemView> list(
            @Parameter(
                            description = "Problem category; omit to include all categories",
                            example = "STUDENT_WITHOUT_GROUP")
                    @RequestParam(required = false)
                    AdminProblemType type,
            @ParameterObject Pageable pageable) {
        return problems.list(type, pageable);
    }
}
