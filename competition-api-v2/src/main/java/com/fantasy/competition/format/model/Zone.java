package com.fantasy.competition.format.model;

import java.util.List;

/** Positions in a table mapped to a meaning, e.g. {"positions":[1,2],"kind":"QUALIFY","label":"Round of 16"}. */
public record Zone(List<Integer> positions, String kind, String label) {
    public static final List<String> KINDS = List.of("CHAMPION", "QUALIFY", "PLAYOFF", "PROMOTION", "RELEGATION", "RELEGATION_PLAYOFF", "OTHER");
}
