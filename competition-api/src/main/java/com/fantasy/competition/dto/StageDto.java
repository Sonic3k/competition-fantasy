package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Stage;
import java.util.List;
import java.util.UUID;

public record StageDto(UUID id, String name, String type, Integer orderNumber, Integer legs, List<StageGroupDto> groups) {
    public static StageDto from(Stage e) {
        return new StageDto(e.getId(), e.getName(), e.getType().name(), e.getOrderNumber(), e.getLegs(),
            e.getGroups() != null ? e.getGroups().stream().map(StageGroupDto::from).toList() : List.of());
    }
}
