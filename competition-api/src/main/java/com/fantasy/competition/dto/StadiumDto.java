package com.fantasy.competition.dto;

import com.fantasy.competition.entity.Stadium;
import java.util.UUID;

public record StadiumDto(UUID id, String name, String city, Integer capacity, String imageUrl, String description) {
    public static StadiumDto from(Stadium e) {
        return new StadiumDto(e.getId(), e.getName(), e.getCity(), e.getCapacity(), e.getImageUrl(), e.getDescription());
    }
}
