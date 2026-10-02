package uz.aifu.lms.scheduling.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.scheduling.domain.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    List<Holiday> findAllByOrderByOnDate();

    List<Holiday> findByOnDateBetweenOrderByOnDate(LocalDate from, LocalDate to);

    boolean existsByOnDate(LocalDate onDate);
}
