package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.CourseSessionAllocation;
import uz.aifu.lms.academics.domain.CourseSessionAllocationId;

public interface CourseSessionAllocationRepository
        extends JpaRepository<CourseSessionAllocation, CourseSessionAllocationId> {

    List<CourseSessionAllocation> findByCourse_Id(UUID courseId);
}
