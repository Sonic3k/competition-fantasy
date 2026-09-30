package com.fantasy.competition.format;

import com.fantasy.competition.format.model.Decider;
import com.fantasy.competition.format.model.Entry;
import com.fantasy.competition.format.model.FormatDefinition;
import com.fantasy.competition.format.model.KoRoundDef;
import com.fantasy.competition.format.model.StageDef;
import com.fantasy.competition.format.model.StageType;
import com.fantasy.competition.format.model.Tiebreaker;
import com.fantasy.competition.format.model.Zone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Structural validation of a format definition. Pure Java, no framework dependency. */
public final class FormatValidator {

    private FormatValidator() {}

    public static List<String> validate(FormatDefinition f) {
        List<String> problems = new ArrayList<>();
        if (f == null) {
            problems.add("format is null");
            return problems;
        }
        if (isBlank(f.key())) problems.add("format.key is required");
        List<StageDef> stages = f.stagesOrEmpty();
        if (stages.isEmpty()) problems.add("format needs at least one stage");

        Set<String> keys = new HashSet<>();
        for (int i = 0; i < stages.size(); i++) {
            StageDef s = stages.get(i);
            String at = "stages[" + i + "]";
            if (s == null) { problems.add(at + " is null"); continue; }
            if (isBlank(s.key())) problems.add(at + ".key is required");
            else if (!keys.add(s.key())) problems.add(at + ".key '" + s.key() + "' is duplicated");
            if (s.type() == null) { problems.add(at + ".type is required"); continue; }

            switch (s.type()) {
                case ROUND_ROBIN -> {
                    if (s.groupsOrDefault() < 1) problems.add(at + ".groups must be >= 1");
                    if (s.teamsPerGroup() != null && s.teamsPerGroup() < 2) problems.add(at + ".teamsPerGroup must be >= 2");
                    if (s.meetingsOrDefault() < 1 || s.meetingsOrDefault() > 4) problems.add(at + ".meetings must be 1..4");
                }
                case LEAGUE_PHASE -> {
                    if (s.matchesPerTeam() == null || s.matchesPerTeam() < 1) problems.add(at + ".matchesPerTeam is required for LEAGUE_PHASE");
                }
                case KNOCKOUT -> {
                    if (s.roundsOrEmpty().isEmpty()) problems.add(at + ".rounds is required for KNOCKOUT");
                    Set<String> roundKeys = new HashSet<>();
                    for (KoRoundDef r : s.roundsOrEmpty()) {
                        if (r == null || isBlank(r.key())) problems.add(at + ".rounds has an entry without key");
                        else if (!roundKeys.add(r.key())) problems.add(at + ".rounds key '" + r.key() + "' is duplicated");
                        if (r != null && r.legs() != null && (r.legs() < 1 || r.legs() > 2)) problems.add(at + ".rounds." + r.key() + ".legs must be 1 or 2");
                    }
                    if (s.legsOrDefault() < 1 || s.legsOrDefault() > 2) problems.add(at + ".legs must be 1 or 2");
                }
                case SINGLE_MATCH -> {
                    if (s.legsOrDefault() != 1) problems.add(at + ".legs must be 1 for SINGLE_MATCH");
                }
            }

            for (String t : s.tiebreakersOrDefault()) {
                if (!isEnum(Tiebreaker.class, t)) problems.add(at + ".tiebreakers contains unknown '" + t + "'");
            }
            for (String d : s.deciderOrDefault()) {
                if (!isEnum(Decider.class, d)) problems.add(at + ".decider contains unknown '" + d + "'");
            }
            for (Zone z : s.zonesOrEmpty()) {
                if (z.positions() == null || z.positions().isEmpty()) problems.add(at + ".zones has an entry without positions");
                if (isBlank(z.kind()) || !Zone.KINDS.contains(z.kind())) problems.add(at + ".zones has unknown kind '" + z.kind() + "'");
            }

            Entry e = s.entry();
            if (i == 0) {
                if (e != null && e.from() != null && !Entry.SEASON_TEAMS.equals(e.from())) {
                    problems.add(at + ".entry.from must be SEASON_TEAMS (or omitted) for the first stage");
                }
            } else {
                if (e == null || isBlank(e.from())) {
                    problems.add(at + ".entry.from is required for stages after the first");
                } else if (!Entry.SEASON_TEAMS.equals(e.from()) && !keys.contains(e.from())) {
                    problems.add(at + ".entry.from '" + e.from() + "' must reference an earlier stage key");
                }
            }
            if (s.carryOver() != null) {
                String cp = s.carryOver().points();
                if (cp == null || !(cp.equals("FULL") || cp.equals("HALF") || cp.equals("NONE"))) {
                    problems.add(at + ".carryOver.points must be FULL, HALF or NONE");
                }
                if (isBlank(s.carryOver().from()) || !keys.contains(s.carryOver().from())) {
                    problems.add(at + ".carryOver.from must reference an earlier stage key");
                }
            }
        }
        return problems;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static <E extends Enum<E>> boolean isEnum(Class<E> type, String value) {
        if (value == null) return false;
        for (E e : type.getEnumConstants()) if (e.name().equals(value)) return true;
        return false;
    }
}
