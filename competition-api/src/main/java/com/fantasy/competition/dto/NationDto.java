package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Nation;
import java.util.UUID;

public record NationDto(UUID id, String name, String code, String flagUrl, String description,
                        String primaryColor, String textColor,
                        String awayColor, String awayTextColor) {
    public static NationDto from(Nation e) {
        if (e == null) return null;
        return new NationDto(e.getId(), e.getName(), e.getCode(), e.getFlagUrl(), e.getDescription(),
            e.getPrimaryColor(), e.getTextColor(),
            e.getAwayColor(), e.getAwayTextColor());
    }
}
