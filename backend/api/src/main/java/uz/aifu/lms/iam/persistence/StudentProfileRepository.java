package uz.aifu.lms.iam.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.iam.domain.StudentProfile;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
    Optional<StudentProfile> findByStudentNumber(String studentNumber);

    @Query(
            "select p from StudentProfile p join fetch p.user u where p.groupCode = :code"
                    + " order by u.fullName")
    List<StudentProfile> findByGroupCodeWithUser(@Param("code") String code);

    @Query(
            "select p.groupCode as code, count(p) as size from StudentProfile p"
                    + " where p.groupCode in :codes group by p.groupCode")
    List<GroupSizeProjection> countByGroupCodes(@Param("codes") Collection<String> codes);

    interface GroupSizeProjection {
        String getCode();

        long getSize();
    }
}
