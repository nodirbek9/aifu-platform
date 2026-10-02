package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.SubjectTranslation;
import uz.aifu.lms.academics.domain.SubjectTranslationId;

public interface SubjectTranslationRepository
        extends JpaRepository<SubjectTranslation, SubjectTranslationId> {

    @Query("select t from SubjectTranslation t where t.subject.id = :subjectId")
    List<SubjectTranslation> findAllBySubjectId(@Param("subjectId") UUID subjectId);

    @Query("select t from SubjectTranslation t where t.subject.id in :subjectIds")
    List<SubjectTranslation> findAllBySubjectIds(
            @Param("subjectIds") java.util.Collection<UUID> subjectIds);

    @Query(
            "select t from SubjectTranslation t where t.subject.id = :subjectId and t.id.locale = :locale")
    Optional<SubjectTranslation> findBySubjectIdAndLocale(
            @Param("subjectId") UUID subjectId, @Param("locale") String locale);

    /** Which locales exist per subject — batched for {@code PublicationValidator}, no N+1. */
    @Query(
            "select t.subject.id as subjectId, t.id.locale as locale from SubjectTranslation t"
                    + " where t.subject.id in :subjectIds")
    List<SubjectLocaleProjection> findLocalesForSubjects(
            @Param("subjectIds") java.util.Collection<UUID> subjectIds);

    interface SubjectLocaleProjection {
        UUID getSubjectId();

        String getLocale();
    }
}
