package uz.aifu.lms.scheduling.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.scheduling.domain.Room;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findAllByOrderByBuildingAscNumberAsc();

    boolean existsByBuildingAndNumber(String building, String number);
}
