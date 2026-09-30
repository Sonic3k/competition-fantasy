package com.fantasy.competition.importer;

import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * The JSON import document (schema v1). One document = one universe. Every entity has a stable `key`;
 * re-running a file upserts by key. Team references accept a key, the exact name, or any alias.
 * Seasons are replaced as a whole on re-import (stages, matches, tables, honours).
 */
public record ImportDocument(
        Integer schemaVersion,
        UniverseIn universe,
        List<NationIn> nations,
        List<StadiumIn> stadiums,
        List<TeamIn> teams,
        List<ProfileIn> teamProfiles,
        List<KitIn> kits,
        List<CompetitionIn> competitions,
        List<SeasonIn> seasons
) {
    public record UniverseIn(String key, String name, String description, String type, Boolean usesNations) {}

    public record NationIn(String key, String name, String code, JsonNode colors, String description) {}

    public record StadiumIn(String key, String name, String city, Integer capacity, String inspiredBy, String description) {}

    public record TeamIn(String key, String name, String shortName, String code, String type, String nation, List<String> aliases,
                         String homeStadium, String description, Integer foundedYear, Integer dissolvedYear) {}

    public record ProfileIn(String team, Integer year, String displayName, String sponsor, JsonNode colors, String notes) {}

    public record KitIn(String team, Integer year, String kind, String shirt, String shorts, String socks, String sponsor) {}

    public record CompetitionIn(String key, String name, String sport, String teamLevel, Integer tier, String description) {}

    public record SeasonIn(String competition, String key, String name, Integer year, String startDate, String endDate, String status,
                           String preset, JsonNode format, String notes, List<SeasonTeamIn> teams, List<StageIn> stages, List<HonourIn> honours) {}

    public record SeasonTeamIn(String team, Integer seed, Integer pot) {}

    public record StageIn(String key, String name, String type, JsonNode config, List<GroupIn> groups, List<KoRoundIn> rounds) {}

    public record GroupIn(String key, String name, List<String> teams, List<RoundIn> rounds, List<RecordedIn> recordedStandings) {}

    public record RoundIn(Integer number, String name, String startDate, String endDate, List<MatchIn> matches) {}

    public record KoRoundIn(String key, String name, Integer legs, Boolean placement, List<TieIn> ties) {}

    public record TieIn(Integer position, String home, String away, JsonNode homeSource, JsonNode awaySource, List<MatchIn> matches,
                        String winner, String resolution) {}

    public record MatchIn(Integer leg, String home, String away, Integer homeScore, Integer awayScore, Integer homeEt, Integer awayEt,
                          Integer homePens, Integer awayPens, String walkover, String date, String stadium, String status, String source,
                          String sourceRef, String confidence, String notes) {}

    public record RecordedIn(Integer afterRound, List<RecordedRowIn> rows) {}

    public record RecordedRowIn(String team, Integer position, Integer played, Integer won, Integer drawn, Integer lost,
                                Integer goalsFor, Integer goalsAgainst, Integer points, String zone) {}

    public record HonourIn(String team, String kind) {}
}
