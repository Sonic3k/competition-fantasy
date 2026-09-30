package com.fantasy.competition.standings;

import java.util.List;

/** Points and ordered tie-break criteria for one table. */
public record TableRules(int win, int draw, int loss, List<String> tiebreakers) {
    public static TableRules standard() {
        return new TableRules(3, 1, 0, List.of("GD", "GF", "H2H"));
    }
}
