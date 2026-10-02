package uz.aifu.lms.scheduling.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.scheduling.domain.PairSlot;

public interface PairSlotRepository extends JpaRepository<PairSlot, UUID> {

    List<PairSlot> findAllByOrderByPairNo();

    boolean existsByPairNo(int pairNo);
}
