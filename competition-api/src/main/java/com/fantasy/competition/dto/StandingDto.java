package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Standing;
import java.util.UUID;

public record StandingDto(UUID id, TeamDto team, String groupName,
                          int played, int won, int drawn, int lost,
                          int goalsFor, int goalsAgainst, int goalDifference, int points) {
    public static StandingDto from(Standing e) {
        return new StandingDto(e.getId(),
            e.getTeam() != null ? TeamDto.from(e.getTeam()) : null,
            e.getGroupName(), e.getPlayed(), e.getWon(), e.getDrawn(), e.getLost(),
            e.getGoalsFor(), e.getGoalsAgainst(), e.getGoalDifference(), e.getPoints());
    }
}
