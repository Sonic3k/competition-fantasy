package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Nation;
import java.util.UUID;

public record NationDto(UUID id, String name, String flagUrl, String description) {
    public static NationDto from(Nation e) {
        if (e == null) return null;
        return new NationDto(e.getId(), e.getName(), e.getFlagUrl(), e.getDescription());
    }
}
