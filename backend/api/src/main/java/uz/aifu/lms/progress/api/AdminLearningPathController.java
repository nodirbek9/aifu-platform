package uz.aifu.lms.progress.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.progress.api.dto.LearningPathDtos.LearningPathConfigRequest;
import uz.aifu.lms.progress.api.dto.LearningPathDtos.LearningPathConfigView;
import uz.aifu.lms.progress.app.LearningPathConfigService;

@RestController
@Tag(
        name = "Admin: learning path",
        description = "Course learning path configuration and gate management")
@SecurityRequirement(name = "bearerAuth")
public class AdminLearningPathController {

    private final LearningPathConfigService configService;
    private final CurrentUserProvider currentUser;

    public AdminLearningPathController(
            LearningPathConfigService configService, CurrentUserProvider currentUser) {
        this.configService = configService;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/admin/courses/{courseId}/learning-path")
    @PreAuthorize("hasAuthority(T(uz.aifu.lms.iam.app.access.Perm).LEARNING_PATH_MANAGE)")
    @Operation(summary = "Get course learning path configuration")
    public LearningPathConfigView get(@PathVariable UUID courseId) {
        return configService.get(courseId);
    }

    @PutMapping("/api/v1/admin/courses/{courseId}/learning-path")
    @PreAuthorize("hasAuthority(T(uz.aifu.lms.iam.app.access.Perm).LEARNING_PATH_MANAGE)")
    @Operation(summary = "Configure or enable/disable course learning path")
    public LearningPathConfigView put(
            @PathVariable UUID courseId, @Valid @RequestBody LearningPathConfigRequest request) {
        UUID adminUserId = currentUser.require().id();
        return configService.put(courseId, request, adminUserId);
    }
}
