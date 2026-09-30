package com.fantasy.competition.competition;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides a knockout tie from its matches: walkover, single match (with replays), or two legs with
 * aggregate, optional away-goals rule, extra time and penalties. Returns an empty outcome when undecided.
 */
public final class TieResolver {

    public record Outcome(Long winnerTeamId, Enums.TieResolution resolution) {
        public static final Outcome UNDECIDED = new Outcome(null, null);
        public boolean decided() { return winnerTeamId != null; }
    }

    private TieResolver() {}

    public static Outcome resolve(Tie tie, List<Match> tieMatches, List<String> decider, int legs) {
        if (tie.getHomeTeamId() == null || tie.getAwayTeamId() == null) return Outcome.UNDECIDED;
        List<Match> played = new ArrayList<>();
        for (Match m : tieMatches) {
            if (m.getWalkover() != null) {
                Long winner = m.getWalkover() == Enums.Walkover.HOME ? m.getHomeTeamId() : m.getAwayTeamId();
                return new Outcome(winner, Enums.TieResolution.WALKOVER);
            }
            if (m.hasScore()) played.add(m);
        }
        if (played.isEmpty()) return Outcome.UNDECIDED;
        played.sort((a, b) -> Integer.compare(a.getLeg(), b.getLeg()));

        if (legs <= 1) return singleMatch(played);
        if (played.size() < 2) return Outcome.UNDECIDED;
        return twoLegs(tie, played, decider);
    }

    private static Outcome singleMatch(List<Match> played) {
        Match last = played.get(played.size() - 1);
        boolean replay = played.size() > 1;
        int h = last.homeGoalsFinal();
        int a = last.awayGoalsFinal();
        if (h != a) {
            Enums.TieResolution res = last.getHomeEt() != null ? Enums.TieResolution.EXTRA_TIME : (replay ? Enums.TieResolution.REPLAY : null);
            return new Outcome(h > a ? last.getHomeTeamId() : last.getAwayTeamId(), res);
        }
        if (last.getHomePens() != null && last.getAwayPens() != null && !last.getHomePens().equals(last.getAwayPens())) {
            return new Outcome(last.getHomePens() > last.getAwayPens() ? last.getHomeTeamId() : last.getAwayTeamId(), Enums.TieResolution.PENALTIES);
        }
        return Outcome.UNDECIDED;
    }

    private static Outcome twoLegs(Tie tie, List<Match> played, List<String> decider) {
        long home = tie.getHomeTeamId();
        long away = tie.getAwayTeamId();
        Match first = played.get(0);
        Match second = played.get(played.size() - 1);
        int homeAgg = goalsFor(home, first, false) + goalsFor(home, second, true);
        int awayAgg = goalsFor(away, first, false) + goalsFor(away, second, true);
        boolean extraTime = second.getHomeEt() != null;
        if (homeAgg != awayAgg) {
            return new Outcome(homeAgg > awayAgg ? home : away, extraTime ? Enums.TieResolution.EXTRA_TIME : Enums.TieResolution.AGGREGATE);
        }
        if (decider != null && decider.contains("AWAY_GOALS")) {
            int homeAwayGoals = awayGoalsOf(home, first, second);
            int awayAwayGoals = awayGoalsOf(away, first, second);
            if (homeAwayGoals != awayAwayGoals) {
                return new Outcome(homeAwayGoals > awayAwayGoals ? home : away, Enums.TieResolution.AWAY_GOALS);
            }
        }
        if (second.getHomePens() != null && second.getAwayPens() != null && !second.getHomePens().equals(second.getAwayPens())) {
            Long winner = second.getHomePens() > second.getAwayPens() ? second.getHomeTeamId() : second.getAwayTeamId();
            return new Outcome(winner, Enums.TieResolution.PENALTIES);
        }
        return Outcome.UNDECIDED;
    }

    private static int goalsFor(long teamId, Match m, boolean useExtraTime) {
        if (m.getHomeTeamId() != null && m.getHomeTeamId() == teamId) return useExtraTime ? m.homeGoalsFinal() : nz(m.getHomeScore());
        if (m.getAwayTeamId() != null && m.getAwayTeamId() == teamId) return useExtraTime ? m.awayGoalsFinal() : nz(m.getAwayScore());
        return 0;
    }

    private static int awayGoalsOf(long teamId, Match first, Match second) {
        int goals = 0;
        if (first.getAwayTeamId() != null && first.getAwayTeamId() == teamId) goals += nz(first.getAwayScore());
        if (second.getAwayTeamId() != null && second.getAwayTeamId() == teamId) goals += second.awayGoalsFinal();
        return goals;
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }
}
