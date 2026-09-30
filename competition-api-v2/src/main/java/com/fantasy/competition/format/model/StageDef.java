package com.fantasy.competition.format.model;

import java.util.List;

/**
 * One stage of a format. Which fields apply depends on type:
 *  ROUND_ROBIN   groups, teamsPerGroup, meetings, venue, tiebreakers, zones
 *  LEAGUE_PHASE  matchesPerTeam, pots, tiebreakers, zones (one table, partial schedule)
 *  KNOCKOUT      rounds, legs, decider, replays, thirdPlace
 *  SINGLE_MATCH  legs (1) and decider
 * entry says where the teams come from; carryOver brings points from an earlier stage.
 */
public record StageDef(
        String key,
        String name,
        StageType type,
        Integer groups,
        Integer teamsPerGroup,
        Integer meetings,
        String venue,
        Integer matchesPerTeam,
        Integer pots,
        List<String> tiebreakers,
        List<Zone> zones,
        List<KoRoundDef> rounds,
        Integer legs,
        List<String> decider,
        Integer replays,
        Boolean thirdPlace,
        Entry entry,
        CarryOver carryOver
) {
    public int meetingsOrDefault() { return meetings == null ? 1 : meetings; }
    public int legsOrDefault() { return legs == null ? 1 : legs; }
    public int groupsOrDefault() { return groups == null ? 1 : groups; }
    public List<String> tiebreakersOrDefault() {
        return tiebreakers == null || tiebreakers.isEmpty() ? List.of("GD", "GF", "H2H") : tiebreakers;
    }
    public List<Zone> zonesOrEmpty() { return zones == null ? List.of() : zones; }
    public List<KoRoundDef> roundsOrEmpty() { return rounds == null ? List.of() : rounds; }
    public List<String> deciderOrDefault() {
        return decider == null || decider.isEmpty() ? List.of("EXTRA_TIME", "PENALTIES") : decider;
    }
}
