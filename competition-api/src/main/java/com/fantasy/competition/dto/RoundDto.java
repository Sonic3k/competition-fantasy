package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Round;
import java.util.UUID;

public record RoundDto(UUID id, Integer roundNumber, String name,
                       UUID stageId, String stageName,
                       UUID stageGroupId, String stageGroupName) {
    public static RoundDto from(Round e) {
        return new RoundDto(e.getId(), e.getRoundNumber(), e.getName(),
            e.getStage() != null ? e.getStage().getId() : null,
            e.getStage() != null ? e.getStage().getName() : null,
            e.getStageGroup() != null ? e.getStageGroup().getId() : null,
            e.getStageGroup() != null ? e.getStageGroup().getName() : null);
    }
}
