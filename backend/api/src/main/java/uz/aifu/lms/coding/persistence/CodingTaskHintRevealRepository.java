package uz.aifu.lms.coding.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTaskHintReveal;
import uz.aifu.lms.coding.domain.CodingTaskHintRevealId;

public interface CodingTaskHintRevealRepository
        extends JpaRepository<CodingTaskHintReveal, CodingTaskHintRevealId> {

    @Query(
            "select r.id.hintId from CodingTaskHintReveal r"
                    + " where r.id.userId = :userId and r.id.hintId in :hintIds")
    List<UUID> findRevealedHintIds(
            @Param("userId") UUID userId, @Param("hintIds") Collection<UUID> hintIds);
}
