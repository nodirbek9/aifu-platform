package uz.aifu.lms.analytics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.analytics.domain.AdminProblemView.AdminProblemType;

public final class AdminProblemDtos {

    private AdminProblemDtos() {}

    @Schema(description = "One administrative problem discovered across platform resources")
    public record AdminProblemView(
            @Schema(example = "STUDENT_WITHOUT_GROUP:123e4567-e89b-12d3-a456-426614174000")
                    String problemKey,
            @Schema(example = "STUDENT_WITHOUT_GROUP") AdminProblemType type,
            UUID userId,
            String fullName,
            UUID groupId,
            String groupCode,
            UUID courseId,
            String subjectCode,
            String cohortCode,
            String termName,
            UUID enrollmentId,
            UUID quizId,
            Integer attemptsUsed,
            Integer attemptsAllowed,
            Boolean grantAvailable,
            Instant lockedUntil) {

        public static AdminProblemView from(uz.aifu.lms.analytics.domain.AdminProblemView entity) {
            return new AdminProblemView(
                    entity.getProblemKey(),
                    entity.getType(),
                    entity.getUserId(),
                    entity.getFullName(),
                    entity.getGroupId(),
                    entity.getGroupCode(),
                    entity.getCourseId(),
                    entity.getSubjectCode(),
                    entity.getCohortCode(),
                    entity.getTermName(),
                    entity.getEnrollmentId(),
                    entity.getQuizId(),
                    entity.getAttemptsUsed(),
                    entity.getAttemptsAllowed(),
                    entity.getGrantAvailable(),
                    entity.getLockedUntil());
        }
    }
}
