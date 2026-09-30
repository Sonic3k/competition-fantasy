package com.fantasy.competition.format.model;

import java.util.List;
import java.util.Optional;

/** The season's rulebook: stages in order, joined by their entry rules. */
public record FormatDefinition(
        String key,
        String name,
        String family,
        Integer teams,
        Points points,
        List<StageDef> stages
) {
    public Points pointsOrDefault() { return points == null ? Points.STANDARD : points; }
    public List<StageDef> stagesOrEmpty() { return stages == null ? List.of() : stages; }

    public Optional<StageDef> stage(String stageKey) {
        return stagesOrEmpty().stream().filter(s -> stageKey != null && stageKey.equals(s.key())).findFirst();
    }
}
