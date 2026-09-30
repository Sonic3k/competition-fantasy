package com.fantasy.competition.competition;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Everything a season page needs, composed from the season's stages. */
public record SeasonView(
        SeasonInfo season,
        CompetitionRef competition,
        UniverseRef universe,
        @JsonRawValue String format,
        Map<Long, TeamRef> teams,
        List<StageView> stages,
        List<HonourView> honours
) {
    public record SeasonInfo(long id, String key, String name, Integer year, LocalDate startDate, LocalDate endDate,
                             String status, String presetKey, String notes) {}

    public record CompetitionRef(long id, String key, String name, String sport, String teamLevel, Integer tier) {}

    public record UniverseRef(long id, String key, String name, String type, boolean usesNations) {}

    public record TeamRef(long id, String key, String name, String shortName, String code, String type,
                          Long nationId, String nationCode, String nationName,
                          @JsonRawValue String colors, @JsonRawValue String nationColors,
                          String displayName, String sponsor, String logoUrl, String flagUrl) {}

    public record StageView(long id, String key, String name, String type, int ordinal, @JsonRawValue String config,
                            List<GroupView> groups, List<KoRoundView> koRounds) {}

    public record GroupView(long id, String key, String name, int ordinal, List<GroupTeamView> teams, List<RoundView> rounds,
                            StandingView calculated, List<StandingView> recorded) {}

    public record GroupTeamView(Long teamId, int position, @JsonRawValue String entrySource) {}

    public record RoundView(long id, int number, String name, LocalDate startDate, LocalDate endDate, List<MatchView> matches) {}

    public record MatchView(long id, Long roundId, Long tieId, int leg, Long homeTeamId, Long awayTeamId, LocalDate date,
                            Long stadiumId, String status, Integer homeScore, Integer awayScore, Integer homeEt, Integer awayEt,
                            Integer homePens, Integer awayPens, String walkover, Long winnerTeamId, String source,
                            String sourceRef, String confidence, String notes) {
        public static MatchView of(Match m) {
            return new MatchView(m.getId(), m.getRoundId(), m.getTieId(), m.getLeg(), m.getHomeTeamId(), m.getAwayTeamId(),
                    m.getMatchDate(), m.getStadiumId(), name(m.getStatus()), m.getHomeScore(), m.getAwayScore(), m.getHomeEt(),
                    m.getAwayEt(), m.getHomePens(), m.getAwayPens(), name(m.getWalkover()), m.getWinnerTeamId(),
                    name(m.getSource()), m.getSourceRef(), name(m.getConfidence()), m.getNotes());
        }
    }

    public record StandingView(long id, String type, int checkpointRound, List<StandingRowView> rows) {}

    public record StandingRowView(long teamId, int position, int played, int won, int drawn, int lost, int goalsFor,
                                  int goalsAgainst, int goalDiff, int points, int adjustment, String zone, String tieNote) {}

    public record KoRoundView(long id, String key, String name, int ordinal, int legs, boolean placement, List<TieView> ties) {}

    public record TieView(long id, int position, @JsonRawValue String homeSource, @JsonRawValue String awaySource,
                          Long homeTeamId, Long awayTeamId, Long winnerTeamId, String resolution, List<MatchView> matches) {}

    public record HonourView(long id, long teamId, String kind, boolean manual) {}

    static String name(Enum<?> e) { return e == null ? null : e.name(); }
}
