package com.fantasy.competition.format.model;

import java.util.List;

/**
 * How teams enter a stage.
 * from        stage key the teams come from; null/"SEASON_TEAMS" for the first stage.
 * positions   group positions that qualify (e.g. [1,2]).
 * bestPlaced  extra qualifiers ranked across groups (e.g. best 4 thirds).
 * bracket     key of the bracket mapping table used by the engine (e.g. "UEFA_24", "FIFA_2026").
 * count       number of teams entering when it cannot be derived (e.g. a knockout with byes).
 * constraints draw constraints, e.g. ["SAME_GROUP", "SAME_NATION"].
 */
public record Entry(String from, List<Integer> positions, BestPlaced bestPlaced, String bracket, Integer count, List<String> constraints) {
    public static final String SEASON_TEAMS = "SEASON_TEAMS";
}
