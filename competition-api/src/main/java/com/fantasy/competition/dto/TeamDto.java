package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Team;
import java.util.UUID;

public record TeamDto(UUID id, String name, String shortName, String type,
                      String homeBg, String homeText, String awayBg, String awayText,
                      String logoUrl, String bannerUrl, String description,
                      NationDto nation,
                      UUID logoMediaId, UUID bannerMediaId) {
    public static TeamDto from(Team e) {
        if (e == null) return null;
        return new TeamDto(e.getId(), e.getName(), e.getShortName(),
            e.getType() != null ? e.getType().name() : null,
            e.getHomeBg(), e.getHomeText(), e.getAwayBg(), e.getAwayText(),
            e.getLogoUrl(), e.getBannerUrl(), e.getDescription(),
            NationDto.from(e.getNation()),
            e.getLogoMedia() != null ? e.getLogoMedia().getId() : null,
            e.getBannerMedia() != null ? e.getBannerMedia().getId() : null);
    }
}
