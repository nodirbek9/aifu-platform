package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.CourseTeacher;
import uz.aifu.lms.academics.domain.CourseTeacherId;

/**
 * Backs {@code CourseTeacherLookupAdapter}, the real implementation of {@code
 * iam.app.access.CourseTeacherLookup} — this is the authorization-critical repository in this
 * module (skills/authorization/SKILL.md L3: SQL scoping, not a Java-side comparison).
 */
public interface CourseTeacherRepository extends JpaRepository<CourseTeacher, CourseTeacherId> {

    boolean existsByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherUserId);

    @Query("select ct.course.id from CourseTeacher ct where ct.teacher.id = :teacherUserId")
    Set<UUID> findCourseIdsByTeacherId(@Param("teacherUserId") UUID teacherUserId);

    @EntityGraph(attributePaths = "teacher")
    List<CourseTeacher> findByCourse_Id(UUID courseId);

    void deleteByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherUserId);
}
