package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Collection;
import java.util.List;
import java.util.UUID;

public record CollectionDto(UUID id, String name, String description, String type,
                            UUID parentId, String parentName, UUID universeId,
                            MediaFileDto coverMedia, int childCount, int mediaCount) {
    public static CollectionDto from(Collection e) {
        return new CollectionDto(e.getId(), e.getName(), e.getDescription(),
            e.getType() != null ? e.getType().name() : null,
            e.getParent() != null ? e.getParent().getId() : null,
            e.getParent() != null ? e.getParent().getName() : null,
            e.getUniverse() != null ? e.getUniverse().getId() : null,
            e.getCoverMedia() != null ? MediaFileDto.from(e.getCoverMedia()) : null,
            e.getChildren() != null ? e.getChildren().size() : 0,
            e.getMediaFiles() != null ? e.getMediaFiles().size() : 0);
    }
}
