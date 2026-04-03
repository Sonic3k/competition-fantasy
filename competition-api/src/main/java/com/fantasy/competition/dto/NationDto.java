package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Nation;
import java.util.UUID;

public record NationDto(UUID id, String name, String code, String flagUrl, String logoUrl, String bannerUrl,
                        String description, String primaryColor, String textColor,
                        String awayColor, String awayTextColor, UUID universeId) {
    public static NationDto from(Nation e) {
        if (e == null) return null;
        return new NationDto(e.getId(), e.getName(), e.getCode(), e.getFlagUrl(), e.getLogoUrl(), e.getBannerUrl(),
            e.getDescription(), e.getPrimaryColor(), e.getTextColor(),
            e.getAwayColor(), e.getAwayTextColor(),
            e.getUniverse() != null ? e.getUniverse().getId() : null);
    }
}
