package uz.aifu.lms.curriculum.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslation;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslationId;

public interface ContentBlockTranslationRepository
        extends JpaRepository<ContentBlockTranslation, ContentBlockTranslationId> {

    @Query("select t from ContentBlockTranslation t where t.block.id = :blockId")
    List<ContentBlockTranslation> findAllByBlockId(@Param("blockId") UUID blockId);

    @Query("select t from ContentBlockTranslation t where t.block.id in :blockIds")
    List<ContentBlockTranslation> findAllByBlockIds(@Param("blockIds") Collection<UUID> blockIds);

    @Query(
            "select t from ContentBlockTranslation t where t.block.id = :blockId and t.id.locale = :locale")
    Optional<ContentBlockTranslation> findByBlockIdAndLocale(
            @Param("blockId") UUID blockId, @Param("locale") String locale);
}
