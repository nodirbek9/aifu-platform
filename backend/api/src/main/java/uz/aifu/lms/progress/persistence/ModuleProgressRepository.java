package uz.aifu.lms.progress.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.progress.domain.ModuleProgress;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, UUID> {

    Optional<ModuleProgress> findByEnrollment_IdAndModule_Id(UUID enrollmentId, UUID moduleId);

    /** Backs the per-module breakdown in {@code GET /courses/{id}/progress/me}. */
    @EntityGraph(attributePaths = "module")
    List<ModuleProgress> findByEnrollment_Id(UUID enrollmentId);
}
