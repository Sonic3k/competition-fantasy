package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Team;
import java.util.UUID;

public record TeamDto(UUID id, String name, String shortName, String type,
                      String primaryColor, String secondaryColor, String logoUrl,
                      NationDto nation) {
    public static TeamDto from(Team e) {
        if (e == null) return null;
        return new TeamDto(e.getId(), e.getName(), e.getShortName(),
            e.getType() != null ? e.getType().name() : null,
            e.getPrimaryColor(), e.getSecondaryColor(), e.getLogoUrl(),
            NationDto.from(e.getNation()));
    }
}
