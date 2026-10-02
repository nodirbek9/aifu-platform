package uz.aifu.lms.academics.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.common.domain.PublicationStatus;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = "department")
    Optional<Subject> findByIdAndDeletedAtIsNull(UUID id);

    // No collection fetch here on purpose (department is a to-one, safe to fetch-join with
    // pagination) — see UserRepository.search for why a to-many fetch + pagination is forbidden.
    // The join to subject_translations is for FILTERING only (a plain join, not a fetch join), so
    // it does not hydrate a collection and does not trip the same Hibernate pagination guard.
    @EntityGraph(attributePaths = "department")
    @Query(
            """
            select distinct s from Subject s
            left join SubjectTranslation t on t.subject = s and t.id.locale = :locale
            where s.deletedAt is null
              and (:status is null or s.status = :status)
              and (:departmentId is null or s.department.id = :departmentId)
              and (:pattern is null or lower(s.code) like :pattern or lower(t.title) like :pattern)
            """)
    Page<Subject> search(
            @Param("status") PublicationStatus status,
            @Param("departmentId") UUID departmentId,
            @Param("locale") String locale,
            @Param("pattern") String pattern,
            Pageable pageable);
}
