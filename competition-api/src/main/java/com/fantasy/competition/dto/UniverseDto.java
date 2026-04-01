package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Universe;
import java.util.UUID;

public record UniverseDto(UUID id, String name, String description, String logoUrl) {
    public static UniverseDto from(Universe e) {
        return new UniverseDto(e.getId(), e.getName(), e.getDescription(), e.getLogoUrl());
    }
}
