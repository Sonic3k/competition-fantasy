package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Round;
import java.util.UUID;

public record RoundDto(UUID id, Integer roundNumber, String name) {
    public static RoundDto from(Round e) {
        return new RoundDto(e.getId(), e.getRoundNumber(), e.getName());
    }
}
