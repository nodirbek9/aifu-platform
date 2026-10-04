package uz.aifu.lms.curriculum.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.CreateContentBlockRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.UpdateContentBlockRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.UpsertContentBlockTranslationRequest;
import uz.aifu.lms.curriculum.app.content.ContentBlockPayloadValidator;
import uz.aifu.lms.curriculum.app.content.ContentBlockValidatorRegistry;
import uz.aifu.lms.curriculum.domain.BlockVisibility;
import uz.aifu.lms.curriculum.domain.ContentBlock;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslation;
import uz.aifu.lms.curriculum.domain.ContentBlockType;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.persistence.ContentBlockRepository;
import uz.aifu.lms.curriculum.persistence.ContentBlockTranslationRepository;

/**
 * Content block administration (docs/domain/domain-model.md §1, Phase 5). Every {@code settings}
 * and {@code payload} write is routed through the {@link ContentBlockValidatorRegistry} for the
 * block's {@link uz.aifu.lms.curriculum.domain.ContentBlockType} — this service never interprets a
 * payload shape itself (that would recreate the "central switch statement" the whole point of the
 * registry is to avoid).
 */
@Service
public class ContentBlockService {

    private final ContentBlockRepository blocks;
    private final ContentBlockTranslationRepository translations;
    private final LessonService lessons;
    private final ContentBlockValidatorRegistry validators;
    private final ObjectMapper objectMapper;
    private final AuditService audit;
    private final Clock clock;

    public ContentBlockService(
            ContentBlockRepository blocks,
            ContentBlockTranslationRepository translations,
            LessonService lessons,
            ContentBlockValidatorRegistry validators,
            ObjectMapper objectMapper,
            AuditService audit,
            Clock clock) {
        this.blocks = blocks;
        this.translations = translations;
        this.lessons = lessons;
        this.validators = validators;
        this.objectMapper = objectMapper;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Unpaginated — used to render every block inline in one lesson's detail view; see the
     * repository's javadoc for why that's not the CLAUDE.md §9 case. The teacher-facing block LIST
     * endpoint uses the paginated overload below.
     */
    @Transactional(readOnly = true)
    public List<ContentBlock> listByLesson(UUID lessonId) {
        return blocks.findByLesson_IdAndDeletedAtIsNullOrderByPosition(lessonId);
    }

    @Transactional(readOnly = true)
    public Page<ContentBlock> listByLesson(UUID lessonId, Pageable pageable) {
        return blocks.findByLesson_IdAndDeletedAtIsNull(lessonId, pageable);
    }

    @Transactional(readOnly = true)
    public ContentBlock require(UUID id) {
        return blocks.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> Exceptions.notFound("ContentBlock", id));
    }

    @Transactional
    public ContentBlock create(
            UUID lessonId, CreateContentBlockRequest request, UUID actorId, String ip) {
        Lesson lesson = lessons.require(lessonId);
        ContentBlockPayloadValidator validator = validators.forType(request.blockType());
        JsonNode validatedSettings = validator.validateSettings(orEmpty(request.settings()));
        if (validator.requiresFile() && request.fileId() == null) {
            throw Exceptions.validation(request.blockType() + " blocks require a fileId");
        }
        int position = blocks.nextPosition(lessonId);
        ContentBlock block = new ContentBlock(lesson, position, request.blockType());
        block.updateSettings(
                writeJson(validatedSettings),
                request.fileId(),
                request.isRequired(),
                visibilityFor(request.blockType(), request.visibility()));
        ContentBlock saved = blocks.save(block);
        audit.record(
                AuditAction.CONTENT_BLOCK_CHANGED,
                actorId,
                "content_block",
                saved.getId(),
                ip,
                null,
                Map.of("lessonId", lessonId.toString()));
        return saved;
    }

    @Transactional
    public ContentBlock updateSettings(
            UUID id, UpdateContentBlockRequest request, UUID actorId, String ip) {
        ContentBlock block = require(id);
        ContentBlockPayloadValidator validator = validators.forType(block.getBlockType());
        JsonNode validatedSettings = validator.validateSettings(orEmpty(request.settings()));
        if (validator.requiresFile() && request.fileId() == null) {
            throw Exceptions.validation(block.getBlockType() + " blocks require a fileId");
        }
        block.updateSettings(
                writeJson(validatedSettings),
                request.fileId(),
                request.isRequired(),
                visibilityFor(block.getBlockType(), request.visibility()));
        audit.record(AuditAction.CONTENT_BLOCK_CHANGED, actorId, "content_block", id, ip, null);
        return block;
    }

    /** A teacher-only block type stays teacher-only whatever the request says. */
    private static BlockVisibility visibilityFor(ContentBlockType type, BlockVisibility requested) {
        return type.teacherOnly() ? BlockVisibility.TEACHER_ONLY : requested;
    }

    /** See {@link ModuleService#reorder} — identical deferred-constraint technique. */
    @Transactional
    public void reorder(UUID lessonId, List<UUID> orderedBlockIds, UUID actorId, String ip) {
        List<ContentBlock> existing =
                blocks.findByLesson_IdAndDeletedAtIsNullOrderByPosition(lessonId);
        if (existing.size() != orderedBlockIds.size()
                || !existing.stream()
                        .map(ContentBlock::getId)
                        .collect(Collectors.toSet())
                        .equals(Set.copyOf(orderedBlockIds))) {
            throw Exceptions.validation(
                    "orderedBlockIds must be exactly the lesson's current, non-deleted block ids");
        }
        Map<UUID, ContentBlock> byId =
                existing.stream().collect(Collectors.toMap(ContentBlock::getId, b -> b));
        for (int i = 0; i < orderedBlockIds.size(); i++) {
            byId.get(orderedBlockIds.get(i)).reorderTo(i);
        }
        audit.record(
                AuditAction.CONTENT_BLOCK_CHANGED,
                actorId,
                "lesson",
                lessonId,
                ip,
                null,
                Map.of("transition", "blocks-reordered"));
    }

    @Transactional
    public void softDelete(UUID id, UUID actorId, String ip) {
        ContentBlock block = require(id);
        block.softDelete(clock.instant());
        audit.record(
                AuditAction.CONTENT_BLOCK_CHANGED,
                actorId,
                "content_block",
                id,
                ip,
                null,
                Map.of("transition", "deleted"));
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<ContentBlockTranslation> listTranslations(UUID blockId) {
        return translations.findAllByBlockId(blockId);
    }

    @Transactional(readOnly = true)
    public java.util.Map<UUID, List<ContentBlockTranslation>> listTranslationsForBlocks(
            java.util.Collection<UUID> blockIds) {
        if (blockIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByBlockIds(blockIds).stream()
                .collect(Collectors.groupingBy(t -> t.getBlock().getId()));
    }

    @Transactional
    public ContentBlockTranslation upsertTranslation(
            UUID blockId,
            String locale,
            UpsertContentBlockTranslationRequest request,
            UUID actorId,
            String ip) {
        ContentBlock block = require(blockId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        ContentBlockPayloadValidator validator = validators.forType(block.getBlockType());
        JsonNode validatedPayload = validator.validatePayload(request.payload());
        String plainText = validator.plainText(validatedPayload);
        String payloadJson = writeJson(validatedPayload);
        ContentBlockTranslation translation =
                translations
                        .findByBlockIdAndLocale(blockId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new ContentBlockTranslation(
                                                block,
                                                resolvedLocale.code(),
                                                payloadJson,
                                                plainText));
        translation.update(payloadJson, plainText);
        ContentBlockTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "content_block",
                blockId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    private static JsonNode orEmpty(JsonNode node) {
        return node == null ? JsonNodeFactory.instance.objectNode() : node;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw Exceptions.internal("Failed to serialize content block JSON", e);
        }
    }
}
