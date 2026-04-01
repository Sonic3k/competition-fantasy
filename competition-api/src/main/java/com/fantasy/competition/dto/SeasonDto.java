package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Season;
import java.util.List;
import java.util.UUID;

public record SeasonDto(UUID id, String name, Integer year, String status,
                        CompetitionDto competition, UUID universeId, List<TeamDto> teams) {
    public static SeasonDto from(Season e) {
        UUID uniId = null;
        if (e.getCompetition() != null && e.getCompetition().getUniverse() != null) {
            uniId = e.getCompetition().getUniverse().getId();
        }
        return new SeasonDto(e.getId(), e.getName(), e.getYear(),
            e.getStatus() != null ? e.getStatus().name() : null,
            e.getCompetition() != null ? CompetitionDto.from(e.getCompetition()) : null,
            uniId,
            e.getTeams() != null ? e.getTeams().stream().map(TeamDto::from).toList() : List.of());
    }
}
