package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Competition;
import java.util.UUID;

public record CompetitionDto(UUID id, String name, String description, String type, String teamLevel) {
    public static CompetitionDto from(Competition e) {
        return new CompetitionDto(e.getId(), e.getName(), e.getDescription(),
            e.getType() != null ? e.getType().name() : null,
            e.getTeamLevel() != null ? e.getTeamLevel().name() : null);
    }
}
