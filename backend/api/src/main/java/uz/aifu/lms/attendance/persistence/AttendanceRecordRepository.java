package uz.aifu.lms.attendance.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.attendance.domain.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findBySessionId(UUID sessionId);

    List<AttendanceRecord> findBySessionIdIn(Collection<UUID> sessionIds);

    List<AttendanceRecord> findByStudentUserId(UUID studentUserId);
}
