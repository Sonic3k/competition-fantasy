package com.fantasy.competition.universe;

import jakarta.validation.constraints.NotBlank;

public final class UniverseDtos {
    private UniverseDtos() {}

    public record UniverseDto(Long id, String key, String name, String description, String type, boolean usesNations,
                              long competitionCount, long teamCount, long nationCount, String avatarUrl, String bannerUrl) {}

    public record UniverseRequest(@NotBlank String key, @NotBlank String name, String description, UniverseType type, Boolean usesNations) {}
}
