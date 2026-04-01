package com.fantasy.competition.dto;

import com.fantasy.competition.entity.StageGroup;
import java.util.List;
import java.util.UUID;

public record StageGroupDto(UUID id, String name, List<TeamDto> teams) {
    public static StageGroupDto from(StageGroup e) {
        return new StageGroupDto(e.getId(), e.getName(),
            e.getTeams() != null ? e.getTeams().stream().map(TeamDto::from).toList() : List.of());
    }
}
