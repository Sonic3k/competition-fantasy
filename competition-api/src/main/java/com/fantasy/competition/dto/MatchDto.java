package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Match;
import java.util.UUID;

public record MatchDto(UUID id, TeamDto homeTeam, TeamDto awayTeam,
                       Integer homeScore, Integer awayScore, Integer leg, String status,
                       String notes, UUID roundId, Integer roundNumber, String roundName,
                       UUID stageGroupId, String stageGroupName) {
    public static MatchDto from(Match e) {
        var round = e.getRound();
        return new MatchDto(e.getId(),
            TeamDto.from(e.getHomeTeam()), TeamDto.from(e.getAwayTeam()),
            e.getHomeScore(), e.getAwayScore(), e.getLeg(),
            e.getStatus() != null ? e.getStatus().name() : null,
            e.getNotes(),
            round != null ? round.getId() : null,
            round != null ? round.getRoundNumber() : null,
            round != null ? round.getName() : null,
            round != null && round.getStageGroup() != null ? round.getStageGroup().getId() : null,
            round != null && round.getStageGroup() != null ? round.getStageGroup().getName() : null);
    }
}
