package com.fantasy.competition.standings;

/** One row of a computed table. tieNote is set when the criteria could not separate teams. */
public record RankedRow(
        long teamId,
        int position,
        int played,
        int won,
        int drawn,
        int lost,
        int goalsFor,
        int goalsAgainst,
        int goalDiff,
        int points,
        int adjustment,
        String tieNote
) {}
