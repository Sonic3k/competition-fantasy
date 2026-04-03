package com.fantasy.competition.dto;

import com.fantasy.competition.entity.MediaFile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MediaFileDto(UUID id, String filename, String cdnUrl, String contentType,
                           Long fileSize, Integer width, Integer height,
                           UUID collectionId, String sourceType, String promptUsed,
                           List<TagDto> tags, Instant createdAt) {
    public static MediaFileDto from(MediaFile e) {
        return new MediaFileDto(e.getId(), e.getFilename(), e.getCdnUrl(), e.getContentType(),
            e.getFileSize(), e.getWidth(), e.getHeight(),
            e.getCollection() != null ? e.getCollection().getId() : null,
            e.getSourceType() != null ? e.getSourceType().name() : null,
            e.getPromptUsed(),
            e.getTags() != null ? e.getTags().stream().map(TagDto::from).toList() : List.of(),
            e.getCreatedAt());
    }
}
