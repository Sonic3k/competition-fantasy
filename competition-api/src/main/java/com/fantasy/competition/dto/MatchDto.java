package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Match;
import java.util.UUID;

public record MatchDto(UUID id, TeamDto homeTeam, TeamDto awayTeam,
                       Integer homeScore, Integer awayScore, String status) {
    public static MatchDto from(Match e) {
        return new MatchDto(e.getId(),
            e.getHomeTeam() != null ? TeamDto.from(e.getHomeTeam()) : null,
            e.getAwayTeam() != null ? TeamDto.from(e.getAwayTeam()) : null,
            e.getHomeScore(), e.getAwayScore(),
            e.getStatus() != null ? e.getStatus().name() : null);
    }
}
