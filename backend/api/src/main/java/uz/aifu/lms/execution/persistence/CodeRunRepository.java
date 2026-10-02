package uz.aifu.lms.execution.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.execution.domain.CodeRun;
import uz.aifu.lms.execution.domain.CodeRunMode;
import uz.aifu.lms.execution.domain.CodeRunStatus;

public interface CodeRunRepository extends JpaRepository<CodeRun, UUID> {

    long countByUserIdAndStatusIn(UUID userId, Collection<CodeRunStatus> statuses);

    List<CodeRun> findByCodingTaskIdAndModeOrderByCreatedAtDesc(
            UUID codingTaskId, CodeRunMode mode);

    @Modifying
    @Query("delete from CodeRun r where r.mode <> :kept and r.createdAt < :before")
    int deleteScratchRunsBefore(@Param("kept") CodeRunMode kept, @Param("before") Instant before);

    @Modifying
    @Query("delete from CodeRun r where r.mode = :mode and r.createdAt < :before")
    int deleteByModeBefore(@Param("mode") CodeRunMode mode, @Param("before") Instant before);

    /**
     * A run nobody picked up (no worker running) would keep the browser polling for ever; fail it.
     */
    @Modifying
    @Query(
            "update CodeRun r set r.status = uz.aifu.lms.execution.domain.CodeRunStatus.FAILED,"
                    + " r.internalError = :reason, r.completedAt = :now"
                    + " where r.status = uz.aifu.lms.execution.domain.CodeRunStatus.QUEUED"
                    + " and r.createdAt < :before")
    int failUnclaimedBefore(
            @Param("before") Instant before,
            @Param("now") Instant now,
            @Param("reason") String reason);
}
