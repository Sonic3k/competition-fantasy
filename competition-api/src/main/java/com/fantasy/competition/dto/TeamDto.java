package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Team;
import java.util.UUID;

public record TeamDto(UUID id, String name, String shortName, String type,
                      String primaryColor, String secondaryColor, String logoUrl) {
    public static TeamDto from(Team e) {
        return new TeamDto(e.getId(), e.getName(), e.getShortName(),
            e.getType() != null ? e.getType().name() : null,
            e.getPrimaryColor(), e.getSecondaryColor(), e.getLogoUrl());
    }
}
