package com.fantasy.competition.standings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranks teams from match results using a points system and an ordered tie-break chain.
 * Pure Java, deterministic. Supports FIFA-style (overall GD first) and UEFA-style (head-to-head first)
 * chains, with the head-to-head mini-table re-applied recursively to a subset that stays level.
 */
public final class StandingsCalculator {

    private static final int MAX_DEPTH = 12;

    private StandingsCalculator() {}

    /**
     * @param teamIds      every team in the table (teams without matches still get a row)
     * @param stableNames  used only as the last, deterministic ordering when all criteria are exhausted
     * @param results      played matches inside this table
     * @param rules        points and tie-break chain
     * @param adjustments  point deductions/bonuses per team (may be empty)
     */
    public static List<RankedRow> rank(Collection<Long> teamIds,
                                       Map<Long, String> stableNames,
                                       List<MatchResult> results,
                                       TableRules rules,
                                       Map<Long, Integer> adjustments) {
        Map<Long, Stats> stats = new LinkedHashMap<>();
        for (Long id : teamIds) stats.put(id, new Stats(id));
        for (MatchResult r : results) {
            Stats h = stats.computeIfAbsent(r.homeTeamId(), Stats::new);
            Stats a = stats.computeIfAbsent(r.awayTeamId(), Stats::new);
            h.record(r.homeGoals(), r.awayGoals(), rules, false);
            a.record(r.awayGoals(), r.homeGoals(), rules, true);
        }
        for (Stats s : stats.values()) {
            Integer adj = adjustments == null ? null : adjustments.get(s.teamId);
            s.adjustment = adj == null ? 0 : adj;
        }

        List<Stats> all = new ArrayList<>(stats.values());
        all.sort(Comparator.comparingInt((Stats s) -> s.totalPoints()).reversed());

        List<Stats> ordered = new ArrayList<>();
        for (List<Stats> cluster : splitBy(all, s -> new long[]{s.totalPoints()})) {
            ordered.addAll(breakTie(cluster, results, rules, 0, 0, stableNames));
        }

        List<RankedRow> rows = new ArrayList<>();
        int pos = 1;
        for (Stats s : ordered) {
            rows.add(new RankedRow(s.teamId, pos++, s.played, s.won, s.drawn, s.lost, s.gf, s.ga, s.gf - s.ga,
                    s.totalPoints(), s.adjustment, s.tieNote));
        }
        return rows;
    }

    private static List<Stats> breakTie(List<Stats> cluster, List<MatchResult> results, TableRules rules,
                                        int fromCriterion, int depth, Map<Long, String> stableNames) {
        if (cluster.size() <= 1) return cluster;
        List<String> criteria = rules.tiebreakers() == null ? List.of() : rules.tiebreakers();
        if (depth > MAX_DEPTH) return exhausted(cluster, stableNames, "tie-break recursion limit");

        for (int i = fromCriterion; i < criteria.size(); i++) {
            String crit = criteria.get(i);
            Map<Long, long[]> keys = keysFor(crit, cluster, results, rules);
            if (keys == null) continue;                       // criterion needs data we do not have
            if (allEqual(keys.values())) continue;

            List<Stats> sorted = new ArrayList<>(cluster);
            sorted.sort((a, b) -> compareDesc(keys.get(a.teamId), keys.get(b.teamId)));
            List<List<Stats>> subs = splitBy(sorted, s -> keys.get(s.teamId));

            boolean headToHead = crit.equals("H2H") || crit.equals("H2H_AWAY_GOALS");
            List<Stats> out = new ArrayList<>();
            for (List<Stats> sub : subs) {
                if (sub.size() == 1) {
                    out.add(sub.get(0));
                } else if (headToHead && sub.size() < cluster.size()) {
                    // UEFA rule: a subset that stays level after head-to-head restarts the chain among itself
                    out.addAll(breakTie(sub, results, rules, 0, depth + 1, stableNames));
                } else {
                    out.addAll(breakTie(sub, results, rules, i + 1, depth + 1, stableNames));
                }
            }
            return out;
        }
        return exhausted(cluster, stableNames, "level on all criteria");
    }

    private static List<Stats> exhausted(List<Stats> cluster, Map<Long, String> stableNames, String note) {
        List<Stats> out = new ArrayList<>(cluster);
        out.sort(Comparator.comparing((Stats s) -> nameOf(stableNames, s.teamId)).thenComparingLong(s -> s.teamId));
        String names = out.stream().map(s -> nameOf(stableNames, s.teamId)).collect(Collectors.joining(", "));
        for (Stats s : out) s.tieNote = note + ": " + names;
        return out;
    }

    private static String nameOf(Map<Long, String> names, long id) {
        String n = names == null ? null : names.get(id);
        return n == null ? String.valueOf(id) : n;
    }

    /** Key vector per team for a criterion, higher = better. null when the criterion cannot be computed. */
    private static Map<Long, long[]> keysFor(String crit, List<Stats> cluster, List<MatchResult> results, TableRules rules) {
        Map<Long, long[]> keys = new HashMap<>();
        switch (crit) {
            case "GD" -> { for (Stats s : cluster) keys.put(s.teamId, new long[]{s.gf - s.ga}); }
            case "GF" -> { for (Stats s : cluster) keys.put(s.teamId, new long[]{s.gf}); }
            case "GA" -> { for (Stats s : cluster) keys.put(s.teamId, new long[]{-s.ga}); }
            case "WINS" -> { for (Stats s : cluster) keys.put(s.teamId, new long[]{s.won}); }
            case "AWAY_GOALS" -> { for (Stats s : cluster) keys.put(s.teamId, new long[]{s.awayGoals}); }
            case "GOAL_AVERAGE" -> {
                for (Stats s : cluster) keys.put(s.teamId, new long[]{s.ga == 0 ? (s.gf == 0 ? 0 : Long.MAX_VALUE / 4) : (s.gf * 100_000L) / s.ga});
            }
            case "H2H", "H2H_AWAY_GOALS" -> {
                Set<Long> ids = cluster.stream().map(s -> s.teamId).collect(Collectors.toSet());
                Map<Long, Stats> mini = new HashMap<>();
                for (Stats s : cluster) mini.put(s.teamId, new Stats(s.teamId));
                for (MatchResult r : results) {
                    if (ids.contains(r.homeTeamId()) && ids.contains(r.awayTeamId())) {
                        mini.get(r.homeTeamId()).record(r.homeGoals(), r.awayGoals(), rules, false);
                        mini.get(r.awayTeamId()).record(r.awayGoals(), r.homeGoals(), rules, true);
                    }
                }
                for (Stats s : cluster) {
                    Stats m = mini.get(s.teamId);
                    if (crit.equals("H2H")) keys.put(s.teamId, new long[]{m.totalPoints(), m.gf - m.ga, m.gf});
                    else keys.put(s.teamId, new long[]{m.awayGoals});
                }
            }
            default -> { return null; }   // FAIR_PLAY, LOTS, PLAYOFF, RANKING: no data to separate on
        }
        return keys;
    }

    private static boolean allEqual(Collection<long[]> values) {
        long[] first = null;
        for (long[] v : values) {
            if (first == null) first = v;
            else if (!Arrays.equals(first, v)) return false;
        }
        return true;
    }

    private static int compareDesc(long[] a, long[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int c = Long.compare(b[i], a[i]);
            if (c != 0) return c;
        }
        return Integer.compare(b.length, a.length);
    }

    private static List<List<Stats>> splitBy(List<Stats> sorted, java.util.function.Function<Stats, long[]> key) {
        List<List<Stats>> out = new ArrayList<>();
        List<Stats> current = new ArrayList<>();
        long[] currentKey = null;
        for (Stats s : sorted) {
            long[] k = key.apply(s);
            if (currentKey != null && !Arrays.equals(currentKey, k)) {
                out.add(current);
                current = new ArrayList<>();
            }
            current.add(s);
            currentKey = k;
        }
        if (!current.isEmpty()) out.add(current);
        return out;
    }

    /** Mutable accumulator; only used inside the calculator. */
    private static final class Stats {
        final long teamId;
        int played, won, drawn, lost, gf, ga, points, awayGoals, adjustment;
        String tieNote;

        Stats(long teamId) { this.teamId = teamId; }

        void record(int scored, int conceded, TableRules rules, boolean away) {
            played++;
            gf += scored;
            ga += conceded;
            if (away) awayGoals += scored;
            if (scored > conceded) { won++; points += rules.win(); }
            else if (scored == conceded) { drawn++; points += rules.draw(); }
            else { lost++; points += rules.loss(); }
        }

        int totalPoints() { return points + adjustment; }
    }
}
