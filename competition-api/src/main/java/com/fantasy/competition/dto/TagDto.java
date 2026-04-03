package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Tag;
import java.util.UUID;

public record TagDto(UUID id, String name, String color) {
    public static TagDto from(Tag e) {
        return new TagDto(e.getId(), e.getName(), e.getColor());
    }
}
