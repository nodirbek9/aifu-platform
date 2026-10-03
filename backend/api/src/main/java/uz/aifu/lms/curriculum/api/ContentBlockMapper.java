package uz.aifu.lms.curriculum.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.BlockTranslationView;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ContentBlockView;
import uz.aifu.lms.curriculum.domain.ContentBlock;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslation;

/** Shared entity-to-DTO mapping for content blocks, embedded inline in {@code LessonDetail}. */
final class ContentBlockMapper {

    private ContentBlockMapper() {}

    static ContentBlockView toView(
            ContentBlock block,
            List<ContentBlockTranslation> translations,
            String resolvedLocale,
            String defaultLocale,
            ObjectMapper objectMapper) {
        return toView(block, translations, resolvedLocale, defaultLocale, objectMapper, false);
    }

    /**
     * @param forEditors include every translation of the payload (the editor's language tabs); the
     *     student-facing rendering carries only the payload in the reader's language
     */
    static ContentBlockView toView(
            ContentBlock block,
            List<ContentBlockTranslation> translations,
            String resolvedLocale,
            String defaultLocale,
            ObjectMapper objectMapper,
            boolean forEditors) {
        ModuleMapper.TitleResolution payloadResolution =
                translations.stream()
                        .filter(t -> t.getLocale().equals(resolvedLocale))
                        .findFirst()
                        .map(t -> new ModuleMapper.TitleResolution(t.getPayload(), false))
                        .or(
                                () ->
                                        translations.stream()
                                                .filter(t -> t.getLocale().equals(defaultLocale))
                                                .findFirst()
                                                .map(
                                                        t ->
                                                                new ModuleMapper.TitleResolution(
                                                                        t.getPayload(), true)))
                        .or(
                                () ->
                                        translations.stream()
                                                .findFirst()
                                                .map(
                                                        t ->
                                                                new ModuleMapper.TitleResolution(
                                                                        t.getPayload(), true)))
                        .orElse(new ModuleMapper.TitleResolution(null, true));
        JsonNode payload = readJson(payloadResolution.text(), objectMapper);
        JsonNode settings = readJson(block.getSettings(), objectMapper);
        return new ContentBlockView(
                block.getId(),
                block.getPosition(),
                block.getBlockType(),
                block.isRequired(),
                block.getVisibility(),
                settings,
                block.getFileId(),
                payload,
                payloadResolution.fallback(),
                forEditors
                        ? translations.stream()
                                .map(
                                        t ->
                                                new BlockTranslationView(
                                                        t.getLocale(),
                                                        readJson(t.getPayload(), objectMapper)))
                                .toList()
                        : null);
    }

    private static JsonNode readJson(String json, ObjectMapper objectMapper) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw uz.aifu.lms.common.error.Exceptions.internal(
                    "Stored content block JSON is not valid JSON", e);
        }
    }
}
