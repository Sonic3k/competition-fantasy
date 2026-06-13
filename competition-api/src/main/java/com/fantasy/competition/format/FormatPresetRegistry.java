package com.fantasy.competition.format;

import com.fantasy.competition.format.TournamentFormat.Phase;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Built-in library of tournament formats. Real competitions plus the formats
 * already in the system (Othelose league, Wonder Empires cup). New presets are
 * added here; a season references one via seasons.format_preset_key.
 */
@Component
public class FormatPresetRegistry {

    // Tie-break orders (POINTS is always the implicit primary key).
    private static final List<String> TB_FIFA  = List.of("GOAL_DIFF", "GOALS_FOR", "HEAD_TO_HEAD", "FAIR_PLAY", "DRAW_LOTS");
    private static final List<String> TB_H2H    = List.of("HEAD_TO_HEAD", "GOAL_DIFF", "GOALS_FOR", "FAIR_PLAY", "DRAW_LOTS");
    private static final List<String> TB_LEAGUE = List.of("GOAL_DIFF", "GOALS_FOR");

    private final Map<String, TournamentFormat> presets = new LinkedHashMap<>();

    public FormatPresetRegistry() {
        // --- FIFA World Cup ---
        register(groupKnockout("WORLD_CUP_2026", "FIFA World Cup 2026 (48)", "FIFA_48", true, TB_FIFA,
            List.of(group("Group Stage", 12, 4, 1, 2, 8, TB_FIFA)),
            List.of(ko("Round of 32", 32, 1), ko("Round of 16", 16, 1),
                    ko("Quarter-final", 8, 1), ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        register(groupKnockout("WORLD_CUP_32", "FIFA World Cup (32)", "FIFA_32", true, TB_FIFA,
            List.of(group("Group Stage", 8, 4, 1, 2, 0, TB_FIFA)),
            List.of(ko("Round of 16", 16, 1), ko("Quarter-final", 8, 1),
                    ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        register(groupKnockout("WORLD_CUP_24", "FIFA World Cup (24)", "FIFA_24", true, TB_FIFA,
            List.of(group("Group Stage", 6, 4, 1, 2, 4, TB_FIFA)),
            List.of(ko("Round of 16", 16, 1), ko("Quarter-final", 8, 1),
                    ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        // --- UEFA Euro (head-to-head first, no third-place playoff) ---
        register(groupKnockout("EURO_24", "UEFA Euro (24)", "UEFA_24", false, TB_H2H,
            List.of(group("Group Stage", 6, 4, 1, 2, 4, TB_H2H)),
            List.of(ko("Round of 16", 16, 1), ko("Quarter-final", 8, 1),
                    ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        register(groupKnockout("EURO_16", "UEFA Euro (16)", "UEFA_16", false, TB_H2H,
            List.of(group("Group Stage", 4, 4, 1, 2, 0, TB_H2H)),
            List.of(ko("Quarter-final", 8, 1), ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        // --- AFC Asian Cup (head-to-head first, no third-place playoff) ---
        register(groupKnockout("ASIAN_CUP_2023", "AFC Asian Cup 2023 (24)", "AFC_24", false, TB_H2H,
            List.of(group("Group Stage", 6, 4, 1, 2, 4, TB_H2H)),
            List.of(ko("Round of 16", 16, 1), ko("Quarter-final", 8, 1),
                    ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        register(groupKnockout("ASIAN_CUP_16", "AFC Asian Cup (16)", "AFC_16", false, TB_H2H,
            List.of(group("Group Stage", 4, 4, 1, 2, 0, TB_H2H)),
            List.of(ko("Quarter-final", 8, 1), ko("Semi-final", 4, 1), ko("Final", 2, 1))));

        // --- Existing system formats ---
        // Othelose: single double round-robin league, no knockout.
        TournamentFormat league = base("LEAGUE_DOUBLE_RR", "Double Round-Robin League", null, false, TB_LEAGUE);
        league.phases.add(group("League", 1, null, 2, 0, 0, TB_LEAGUE));
        register(league);

        // Wonder Empires: two group phases then 2-leg knockout (Champions League / C1 style).
        TournamentFormat cup = base("CUP_C1_WONDER_EMPIRES", "Cup C1 (Wonder Empires)", "WONDER_EMPIRES", false, TB_LEAGUE);
        cup.koDeciders = new ArrayList<>(List.of("AWAY_GOALS", "EXTRA_TIME", "PENALTIES"));
        cup.phases.add(group("Group Stage", 8, 4, 1, 2, 0, TB_LEAGUE));
        cup.phases.add(group("Elimination Groups II", 4, 4, 1, 2, 0, TB_LEAGUE));
        cup.phases.add(ko("Quarter-final", 8, 2));
        cup.phases.add(ko("Semi-final", 4, 2));
        cup.phases.add(ko("Final", 2, 1));
        register(cup);
    }

    public TournamentFormat get(String key) {
        return key == null ? null : presets.get(key);
    }

    public Collection<TournamentFormat> all() {
        return presets.values();
    }

    public Set<String> keys() {
        return presets.keySet();
    }

    // ---- builders ----

    private void register(TournamentFormat f) {
        presets.put(f.presetKey, f);
    }

    private static TournamentFormat base(String key, String name, String bracketMap,
                                         boolean thirdPlace, List<String> ignoredDefaultTieBreak) {
        TournamentFormat f = new TournamentFormat();
        f.presetKey = key;
        f.displayName = name;
        f.bracketMap = bracketMap;
        f.thirdPlacePlayoff = thirdPlace;
        f.pointsWin = 3;
        f.pointsDraw = 1;
        return f;
    }

    private static TournamentFormat groupKnockout(String key, String name, String bracketMap,
                                                  boolean thirdPlace, List<String> defaultTieBreak,
                                                  List<Phase> groupPhases, List<Phase> koPhases) {
        TournamentFormat f = base(key, name, bracketMap, thirdPlace, defaultTieBreak);
        f.koDeciders = new ArrayList<>(List.of("EXTRA_TIME", "PENALTIES"));
        f.phases.addAll(groupPhases);
        f.phases.addAll(koPhases);
        return f;
    }

    private static Phase group(String name, int numGroups, Integer teamsPerGroup, int legs,
                               int advancePerGroup, int bestThirds, List<String> tieBreak) {
        Phase p = new Phase();
        p.type = "GROUP";
        p.name = name;
        p.numGroups = numGroups;
        p.teamsPerGroup = teamsPerGroup;
        p.legs = legs;
        p.advancePerGroup = advancePerGroup;
        p.bestThirds = bestThirds;
        p.tieBreak = new ArrayList<>(tieBreak);
        return p;
    }

    private static Phase ko(String name, int size, int legs) {
        Phase p = new Phase();
        p.type = "KNOCKOUT";
        p.name = name;
        p.size = size;
        p.legs = legs;
        return p;
    }
}
