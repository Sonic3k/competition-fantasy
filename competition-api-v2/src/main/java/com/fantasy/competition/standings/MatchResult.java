package com.fantasy.competition.standings;

/** A played match reduced to what a table needs. Goals are the ones that count for the table (90 minutes). */
public record MatchResult(long homeTeamId, long awayTeamId, int homeGoals, int awayGoals) {}
