package com.fantasy.competition.competition;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class TieResolverTest {

    private static Tie tie() {
        Tie t = new Tie();
        t.setHomeTeamId(1L);
        t.setAwayTeamId(2L);
        return t;
    }

    private static Match match(long home, long away, int leg, Integer hs, Integer as, Integer het, Integer aet, Integer hp, Integer ap) {
        Match m = new Match();
        m.setHomeTeamId(home);
        m.setAwayTeamId(away);
        m.setLeg(leg);
        m.setStatus(Enums.MatchStatus.PLAYED);
        m.setHomeScore(hs);
        m.setAwayScore(as);
        m.setHomeEt(het);
        m.setAwayEt(aet);
        m.setHomePens(hp);
        m.setAwayPens(ap);
        return m;
    }

    @Test
    void singleMatchDecidedInNormalTime() {
        TieResolver.Outcome o = TieResolver.resolve(tie(), List.of(match(1, 2, 1, 2, 1, null, null, null, null)), List.of("EXTRA_TIME", "PENALTIES"), 1);
        assertEquals(1L, o.winnerTeamId());
        assertNull(o.resolution());
    }

    @Test
    void singleMatchOnPenalties() {
        TieResolver.Outcome o = TieResolver.resolve(tie(), List.of(match(1, 2, 1, 1, 1, 1, 1, 4, 3)), List.of("EXTRA_TIME", "PENALTIES"), 1);
        assertEquals(1L, o.winnerTeamId());
        assertEquals(Enums.TieResolution.PENALTIES, o.resolution());
    }

    @Test
    void twoLegsOnAggregate() {
        List<Match> legs = List.of(match(1, 2, 1, 2, 0, null, null, null, null), match(2, 1, 2, 1, 0, null, null, null, null));
        TieResolver.Outcome o = TieResolver.resolve(tie(), legs, List.of("AGGREGATE", "AWAY_GOALS", "EXTRA_TIME", "PENALTIES"), 2);
        assertEquals(1L, o.winnerTeamId());
        assertEquals(Enums.TieResolution.AGGREGATE, o.resolution());
    }

    @Test
    void twoLegsOnAwayGoals() {
        // home team wins 1-0 at home, loses 2-1 away: 2-2 aggregate, home team scored away
        List<Match> legs = List.of(match(1, 2, 1, 1, 0, null, null, null, null), match(2, 1, 2, 2, 1, null, null, null, null));
        TieResolver.Outcome o = TieResolver.resolve(tie(), legs, List.of("AGGREGATE", "AWAY_GOALS", "EXTRA_TIME", "PENALTIES"), 2);
        assertEquals(1L, o.winnerTeamId());
        assertEquals(Enums.TieResolution.AWAY_GOALS, o.resolution());
    }

    @Test
    void twoLegsWithoutAwayGoalsRuleGoesToPenalties() {
        List<Match> legs = List.of(match(1, 2, 1, 1, 0, null, null, null, null), match(2, 1, 2, 2, 1, 2, 1, 5, 6));
        TieResolver.Outcome o = TieResolver.resolve(tie(), legs, List.of("AGGREGATE", "EXTRA_TIME", "PENALTIES"), 2);
        assertEquals(1L, o.winnerTeamId());
        assertEquals(Enums.TieResolution.PENALTIES, o.resolution());
    }

    @Test
    void undecidedWhenSecondLegMissing() {
        TieResolver.Outcome o = TieResolver.resolve(tie(), List.of(match(1, 2, 1, 2, 0, null, null, null, null)), List.of("AGGREGATE"), 2);
        assertFalse(o.decided());
    }
}
