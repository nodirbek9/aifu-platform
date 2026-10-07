package uz.aifu.lms.scheduling.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.aifu.lms.scheduling.domain.PairSlot;
import uz.aifu.lms.scheduling.domain.Shift;

public interface PairSlotRepository extends JpaRepository<PairSlot, UUID> {

    List<PairSlot> findAllByOrderByPairNo();

    boolean existsByShiftAndShiftPairNo(Shift shift, int shiftPairNo);

    @Query("select coalesce(max(p.pairNo), 0) from PairSlot p")
    int maxPairNo();
}
