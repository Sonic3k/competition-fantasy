package com.fantasy.competition.standings;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StandingsCalculatorTest {

    private static final Map<Long, String> NAMES = Map.of(1L, "A", 2L, "B", 3L, "C", 4L, "D");

    /** A, B, C each beat one another and D: FIFA order separates on overall goal difference. */
    private static final List<MatchResult> TRIANGLE = List.of(
            new MatchResult(1, 4, 5, 0), new MatchResult(2, 4, 1, 0), new MatchResult(3, 4, 2, 0),
            new MatchResult(1, 2, 1, 2), new MatchResult(2, 3, 0, 1), new MatchResult(3, 1, 0, 1));

    @Test
    void fifaOrderUsesOverallGoalDifferenceFirst() {
        List<RankedRow> rows = StandingsCalculator.rank(List.of(1L, 2L, 3L, 4L), NAMES, TRIANGLE,
                new TableRules(3, 1, 0, List.of("GD", "GF", "H2H")), Map.of());
        assertEquals(List.of(1L, 3L, 2L, 4L), rows.stream().map(RankedRow::teamId).toList());
        assertEquals(6, rows.get(0).points());
        assertEquals(5, rows.get(0).goalDiff());
        assertEquals(1, rows.get(0).position());
        assertEquals(4, rows.get(3).position());
    }

    @Test
    void uefaOrderUsesHeadToHeadAndReappliesItToTheLevelSubset() {
        List<RankedRow> rows = StandingsCalculator.rank(List.of(1L, 2L, 3L, 4L), NAMES, TRIANGLE,
                new TableRules(3, 1, 0, List.of("H2H", "GD", "GF")), Map.of());
        // mini-table: all 3 points, GD 0; goals scored A 2, B 2, C 1 -> C third; A vs B restarts: B beat A
        assertEquals(List.of(2L, 1L, 3L, 4L), rows.stream().map(RankedRow::teamId).toList());
    }

    @Test
    void levelTeamsAreFlaggedAndAdjustmentsApply() {
        List<MatchResult> allDraws = List.of(new MatchResult(1, 2, 0, 0), new MatchResult(2, 3, 0, 0), new MatchResult(3, 1, 0, 0));
        List<RankedRow> rows = StandingsCalculator.rank(List.of(1L, 2L, 3L), NAMES, allDraws, TableRules.standard(), Map.of(2L, -1));
        assertEquals(List.of(1L, 3L, 2L), rows.stream().map(RankedRow::teamId).toList());
        assertNotNull(rows.get(0).tieNote());
        assertNotNull(rows.get(1).tieNote());
        assertNull(rows.get(2).tieNote());
        assertEquals(1, rows.get(2).points());
        assertEquals(-1, rows.get(2).adjustment());
    }

    @Test
    void twoPointsForAWinChangesTheTable() {
        List<MatchResult> results = List.of(new MatchResult(1, 2, 1, 0), new MatchResult(1, 3, 0, 0), new MatchResult(2, 3, 0, 0));
        List<RankedRow> rows = StandingsCalculator.rank(List.of(1L, 2L, 3L), NAMES, results, new TableRules(2, 1, 0, List.of("GD", "GF")), Map.of());
        assertEquals(3, rows.get(0).points());
        assertEquals(1L, rows.get(0).teamId());
    }

    @Test
    void teamsWithoutMatchesStillGetRows() {
        List<RankedRow> rows = StandingsCalculator.rank(List.of(1L, 2L), NAMES, List.of(), TableRules.standard(), Map.of());
        assertEquals(2, rows.size());
        assertEquals(0, rows.get(0).played());
    }
}
