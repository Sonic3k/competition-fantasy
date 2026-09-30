package com.fantasy.competition.team;

import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;

import java.util.List;

public final class TeamDtos {
    private TeamDtos() {}

    public record NationDto(Long id, Long universeId, String key, String name, String code, @JsonRawValue String colors,
                            String description, String flagUrl, String emblemUrl) {}

    public record NationRequest(@NotBlank String key, @NotBlank String name, @NotBlank String code, JsonNode colors, String description) {}

    public record StadiumDto(Long id, Long universeId, String key, String name, String city, Integer capacity, String inspiredBy,
                             String description, String imageUrl) {}

    public record StadiumRequest(@NotBlank String key, @NotBlank String name, String city, Integer capacity, String inspiredBy, String description) {}

    public record TeamDto(Long id, Long universeId, Long nationId, String nationCode, String nationName, String type, String key,
                          String name, String shortName, String code, Long homeStadiumId, String description, Integer foundedYear,
                          Integer dissolvedYear, List<String> aliases, String logoUrl) {}

    public record TeamRequest(@NotBlank String key, @NotBlank String name, String shortName, String code, TeamType type,
                              Long nationId, Long homeStadiumId, String description, Integer foundedYear, Integer dissolvedYear,
                              List<String> aliases) {}

    public record ProfileDto(Long id, Long teamId, Integer year, String displayName, String sponsor, @JsonRawValue String colors, String notes) {}

    public record ProfileRequest(Integer year, String displayName, String sponsor, JsonNode colors, String notes) {}

    public record KitDto(Long id, Long teamId, Integer year, String kind, String shirtColor, String shortsColor, String socksColor,
                         String sponsor, Long imageAssetId, String imageUrl) {}

    public record KitRequest(Integer year, KitKind kind, String shirtColor, String shortsColor, String socksColor, String sponsor, Long imageAssetId) {}

    public record SeasonPlayed(Long seasonId, String seasonName, Integer year, Long competitionId, String competitionName,
                               String status, List<String> honours) {}

    public record TeamDetail(TeamDto team, List<ProfileDto> profiles, List<KitDto> kits, List<SeasonPlayed> seasons) {}
}
