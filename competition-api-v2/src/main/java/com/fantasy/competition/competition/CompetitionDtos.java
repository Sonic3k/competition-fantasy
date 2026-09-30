package com.fantasy.competition.competition;

import com.fantasy.competition.format.model.StageType;
import com.fantasy.competition.team.TeamType;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

public final class CompetitionDtos {
    private CompetitionDtos() {}

    public record CompetitionDto(Long id, Long universeId, String universeKey, String universeName, String key, String name, String sport,
                                 String teamLevel, Integer tier, String description, int seasonCount, String logoUrl) {}

    public record CompetitionRequest(@NotBlank String key, @NotBlank String name, String sport, TeamType teamLevel, Integer tier, String description) {}

    public record SeasonSummary(Long id, Long competitionId, String key, String name, Integer year, LocalDate startDate, LocalDate endDate,
                                String status, String presetKey, Long championTeamId, String championName, int teamCount) {}

    public record CompetitionDetail(CompetitionDto competition, List<SeasonSummary> seasons) {}

    public record SeasonRequest(@NotBlank String key, @NotBlank String name, Integer year, LocalDate startDate, LocalDate endDate,
                                Enums.SeasonStatus status, String presetKey, JsonNode format, String notes, Boolean scaffold) {}

    public record SeasonTeamInput(Long teamId, Integer seed, Integer pot) {}

    public record StageRequest(@NotBlank String key, @NotBlank String name, StageType type, Integer ordinal, JsonNode config) {}

    public record GroupRequest(@NotBlank String key, @NotBlank String name, Integer ordinal, List<Long> teamIds) {}

    public record RoundRequest(Integer number, String name, LocalDate startDate, LocalDate endDate) {}

    public record KoRoundRequest(@NotBlank String key, @NotBlank String name, Integer ordinal, Integer legs, Boolean placement) {}

    public record TieRequest(Integer position, Long homeTeamId, Long awayTeamId, JsonNode homeSource, JsonNode awaySource) {}

    public record MatchRequest(Long roundId, Long tieId, Integer leg, Long homeTeamId, Long awayTeamId, LocalDate date, Long stadiumId,
                               Enums.MatchStatus status, Integer homeScore, Integer awayScore, Integer homeEt, Integer awayEt,
                               Integer homePens, Integer awayPens, Enums.Walkover walkover, Enums.MatchSource source, String sourceRef,
                               Enums.Confidence confidence, String notes) {}

    public record ResultRequest(Integer homeScore, Integer awayScore, Integer homeEt, Integer awayEt, Integer homePens, Integer awayPens,
                                Enums.Walkover walkover, Enums.MatchStatus status, Enums.Confidence confidence, Enums.MatchSource source,
                                String sourceRef, String notes, LocalDate date) {}

    public record HonourRequest(Long teamId, Enums.HonourKind kind) {}
}
