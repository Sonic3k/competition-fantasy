package com.fantasy.competition.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Declarative description of a tournament's structure. Stored per-season
 * (as JSON in seasons.format_config) or referenced via a preset key.
 * An ordered list of phases maps 1:1 to Stage entities:
 *   GROUP phases  -> a GROUP stage with N StageGroups + round-robin fixtures
 *   KNOCKOUT phases -> a KNOCKOUT stage with TBD bracket matches
 *
 * Plain public fields so Jackson serializes/deserializes without annotations.
 */
public class TournamentFormat {

    public String presetKey;
    public String displayName;

    public int pointsWin = 3;
    public int pointsDraw = 1;

    public boolean thirdPlacePlayoff = false;

    /** Identifies the official bracket/best-third mapping table, e.g. FIFA_48, AFC_24, FIFA_32. */
    public String bracketMap;

    /** Knockout tie deciders, in order: EXTRA_TIME, PENALTIES, AWAY_GOALS. */
    public List<String> koDeciders = new ArrayList<>();

    public List<Phase> phases = new ArrayList<>();

    public static class Phase {
        /** GROUP or KNOCKOUT. */
        public String type;
        public String name;

        // --- GROUP fields ---
        public Integer numGroups;
        public Integer teamsPerGroup;
        public Integer legs;            // matches between each pair (1 or 2)
        public Integer advancePerGroup; // top-N qualify automatically
        public Integer bestThirds;      // number of best third-placed teams that qualify
        /** Tie-break order applied among teams level on points. */
        public List<String> tieBreak = new ArrayList<>();

        // --- KNOCKOUT fields ---
        public Integer size;            // teams entering this round (16=R16, 8=QF, ...)
    }

    public Phase firstGroupPhase() {
        for (Phase p : phases) if ("GROUP".equalsIgnoreCase(p.type)) return p;
        return null;
    }

    public boolean hasKnockout() {
        for (Phase p : phases) if ("KNOCKOUT".equalsIgnoreCase(p.type)) return true;
        return false;
    }
}
