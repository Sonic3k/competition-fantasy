package com.fantasy.competition.competition;

import com.fantasy.competition.asset.AssetLookup;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.standings.Standing;
import com.fantasy.competition.standings.StandingRepository;
import com.fantasy.competition.standings.StandingRow;
import com.fantasy.competition.standings.StandingRowRepository;
import com.fantasy.competition.standings.StandingType;
import com.fantasy.competition.team.Nation;
import com.fantasy.competition.team.NationRepository;
import com.fantasy.competition.team.Team;
import com.fantasy.competition.team.TeamProfile;
import com.fantasy.competition.team.TeamProfileRepository;
import com.fantasy.competition.team.TeamRepository;
import com.fantasy.competition.universe.Universe;
import com.fantasy.competition.universe.UniverseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builds the SeasonView with a fixed number of bulk queries, no lazy loading. */
@Service
public class SeasonViewService {

    private final SeasonRepository seasons;
    private final CompetitionRepository competitions;
    private final UniverseRepository universes;
    private final SeasonTeamRepository seasonTeams;
    private final StageRepository stages;
    private final StageGroupRepository groups;
    private final StageGroupTeamRepository groupTeams;
    private final RoundRepository rounds;
    private final KoRoundRepository koRounds;
    private final TieRepository ties;
    private final MatchRepository matches;
    private final StandingRepository standings;
    private final StandingRowRepository standingRows;
    private final HonourRepository honours;
    private final TeamRepository teams;
    private final NationRepository nations;
    private final TeamProfileRepository profiles;
    private final AssetLookup assetLookup;

    public SeasonViewService(SeasonRepository seasons, CompetitionRepository competitions, UniverseRepository universes,
                             SeasonTeamRepository seasonTeams, StageRepository stages, StageGroupRepository groups,
                             StageGroupTeamRepository groupTeams, RoundRepository rounds, KoRoundRepository koRounds,
                             TieRepository ties, MatchRepository matches, StandingRepository standings,
                             StandingRowRepository standingRows, HonourRepository honours, TeamRepository teams,
                             NationRepository nations, TeamProfileRepository profiles, AssetLookup assetLookup) {
        this.seasons = seasons;
        this.competitions = competitions;
        this.universes = universes;
        this.seasonTeams = seasonTeams;
        this.stages = stages;
        this.groups = groups;
        this.groupTeams = groupTeams;
        this.rounds = rounds;
        this.koRounds = koRounds;
        this.ties = ties;
        this.matches = matches;
        this.standings = standings;
        this.standingRows = standingRows;
        this.honours = honours;
        this.teams = teams;
        this.nations = nations;
        this.profiles = profiles;
        this.assetLookup = assetLookup;
    }

    @Transactional(readOnly = true)
    public SeasonView build(long seasonId) {
        Season season = seasons.findById(seasonId).orElseThrow(() -> new NotFoundException("Season", seasonId));
        Competition competition = competitions.findById(season.getCompetitionId()).orElseThrow(() -> new NotFoundException("Competition", season.getCompetitionId()));
        Universe universe = universes.findById(competition.getUniverseId()).orElseThrow(() -> new NotFoundException("Universe", competition.getUniverseId()));

        Set<Long> teamIds = new LinkedHashSet<>();
        for (SeasonTeam st : seasonTeams.findBySeasonIdOrderBySeedAscIdAsc(seasonId)) teamIds.add(st.getTeamId());

        List<Stage> stageList = stages.findBySeasonIdOrderByOrdinalAsc(seasonId);
        List<Long> stageIds = stageList.stream().map(Stage::getId).toList();

        List<StageGroup> groupList = stageIds.isEmpty() ? List.of() : groups.findByStageIdInOrderByOrdinalAsc(stageIds);
        List<Long> groupIds = groupList.stream().map(StageGroup::getId).toList();
        Map<Long, List<StageGroupTeam>> groupTeamsByGroup = groupBy(groupIds.isEmpty() ? List.of() : groupTeams.findByStageGroupIdInOrderByPositionAsc(groupIds), StageGroupTeam::getStageGroupId);
        Map<Long, List<Round>> roundsByGroup = groupBy(groupIds.isEmpty() ? List.of() : rounds.findByStageGroupIdInOrderByNumberAsc(groupIds), Round::getStageGroupId);
        List<Long> roundIds = roundsByGroup.values().stream().flatMap(List::stream).map(Round::getId).toList();
        Map<Long, List<Match>> matchesByRound = groupBy(roundIds.isEmpty() ? List.of() : matches.findByRoundIdIn(roundIds), Match::getRoundId);

        List<KoRound> koList = stageIds.isEmpty() ? List.of() : koRounds.findByStageIdInOrderByOrdinalAsc(stageIds);
        Map<Long, List<KoRound>> koByStage = groupBy(koList, KoRound::getStageId);
        List<Long> koIds = koList.stream().map(KoRound::getId).toList();
        List<Tie> tieList = koIds.isEmpty() ? List.of() : ties.findByKoRoundIdInOrderByPositionAsc(koIds);
        Map<Long, List<Tie>> tiesByKo = groupBy(tieList, Tie::getKoRoundId);
        List<Long> tieIds = tieList.stream().map(Tie::getId).toList();
        Map<Long, List<Match>> matchesByTie = groupBy(tieIds.isEmpty() ? List.of() : matches.findByTieIdIn(tieIds), Match::getTieId);

        List<Standing> standingList = groupIds.isEmpty() ? List.of() : standings.findByStageGroupIdIn(groupIds);
        Map<Long, List<Standing>> standingsByGroup = groupBy(standingList, Standing::getStageGroupId);
        List<Long> standingIds = standingList.stream().map(Standing::getId).toList();
        Map<Long, List<StandingRow>> rowsByStanding = groupBy(standingIds.isEmpty() ? List.of() : standingRows.findByStandingIdInOrderByPositionAsc(standingIds), StandingRow::getStandingId);

        // collect every team referenced anywhere
        groupTeamsByGroup.values().forEach(l -> l.forEach(gt -> { if (gt.getTeamId() != null) teamIds.add(gt.getTeamId()); }));
        matchesByRound.values().forEach(l -> l.forEach(m -> addTeams(teamIds, m)));
        matchesByTie.values().forEach(l -> l.forEach(m -> addTeams(teamIds, m)));
        tieList.forEach(t -> { if (t.getHomeTeamId() != null) teamIds.add(t.getHomeTeamId()); if (t.getAwayTeamId() != null) teamIds.add(t.getAwayTeamId()); });
        rowsByStanding.values().forEach(l -> l.forEach(r -> teamIds.add(r.getTeamId())));
        List<Honour> honourList = honours.findBySeasonId(seasonId);
        honourList.forEach(h -> teamIds.add(h.getTeamId()));

        Map<Long, SeasonView.TeamRef> teamRefs = teamRefs(teamIds, season.getYear());

        List<SeasonView.StageView> stageViews = new ArrayList<>();
        for (Stage stage : stageList) {
            List<SeasonView.GroupView> groupViews = new ArrayList<>();
            for (StageGroup g : groupList) {
                if (!g.getStageId().equals(stage.getId())) continue;
                List<SeasonView.GroupTeamView> gts = groupTeamsByGroup.getOrDefault(g.getId(), List.of()).stream()
                        .map(gt -> new SeasonView.GroupTeamView(gt.getTeamId(), gt.getPosition(), gt.getEntrySource())).toList();
                List<SeasonView.RoundView> roundViews = new ArrayList<>();
                for (Round r : roundsByGroup.getOrDefault(g.getId(), List.of())) {
                    List<SeasonView.MatchView> mv = matchesByRound.getOrDefault(r.getId(), List.of()).stream()
                            .sorted((a, b) -> Long.compare(a.getId(), b.getId())).map(SeasonView.MatchView::of).toList();
                    roundViews.add(new SeasonView.RoundView(r.getId(), r.getNumber(), r.getName(), r.getStartDate(), r.getEndDate(), mv));
                }
                SeasonView.StandingView calculated = null;
                List<SeasonView.StandingView> recorded = new ArrayList<>();
                for (Standing s : standingsByGroup.getOrDefault(g.getId(), List.of())) {
                    SeasonView.StandingView sv = standingView(s, rowsByStanding.getOrDefault(s.getId(), List.of()));
                    if (s.getType() == StandingType.CALCULATED && s.getCheckpointRound() == 0) calculated = sv;
                    else if (s.getType() == StandingType.RECORDED) recorded.add(sv);
                }
                recorded.sort((a, b) -> Integer.compare(a.checkpointRound() == 0 ? Integer.MAX_VALUE : a.checkpointRound(),
                        b.checkpointRound() == 0 ? Integer.MAX_VALUE : b.checkpointRound()));
                groupViews.add(new SeasonView.GroupView(g.getId(), g.getKey(), g.getName(), g.getOrdinal(), gts, roundViews, calculated, recorded));
            }
            List<SeasonView.KoRoundView> koViews = new ArrayList<>();
            for (KoRound ko : koByStage.getOrDefault(stage.getId(), List.of())) {
                List<SeasonView.TieView> tieViews = new ArrayList<>();
                for (Tie t : tiesByKo.getOrDefault(ko.getId(), List.of())) {
                    List<SeasonView.MatchView> mv = matchesByTie.getOrDefault(t.getId(), List.of()).stream()
                            .sorted((a, b) -> a.getLeg().equals(b.getLeg()) ? Long.compare(a.getId(), b.getId()) : Integer.compare(a.getLeg(), b.getLeg()))
                            .map(SeasonView.MatchView::of).toList();
                    tieViews.add(new SeasonView.TieView(t.getId(), t.getPosition(), t.getHomeSource(), t.getAwaySource(),
                            t.getHomeTeamId(), t.getAwayTeamId(), t.getWinnerTeamId(), SeasonView.name(t.getResolution()), mv));
                }
                koViews.add(new SeasonView.KoRoundView(ko.getId(), ko.getKey(), ko.getName(), ko.getOrdinal(), ko.getLegs(), ko.isPlacement(), tieViews));
            }
            stageViews.add(new SeasonView.StageView(stage.getId(), stage.getKey(), stage.getName(), stage.getType().name(),
                    stage.getOrdinal(), stage.getConfig(), groupViews, koViews));
        }

        List<SeasonView.HonourView> honourViews = honourList.stream()
                .map(h -> new SeasonView.HonourView(h.getId(), h.getTeamId(), h.getKind().name(), h.isManual())).toList();

        return new SeasonView(
                new SeasonView.SeasonInfo(season.getId(), season.getKey(), season.getName(), season.getYear(), season.getStartDate(),
                        season.getEndDate(), season.getStatus().name(), season.getPresetKey(), season.getNotes()),
                new SeasonView.CompetitionRef(competition.getId(), competition.getKey(), competition.getName(), competition.getSport(),
                        competition.getTeamLevel().name(), competition.getTier()),
                new SeasonView.UniverseRef(universe.getId(), universe.getKey(), universe.getName(), universe.getType().name(), universe.isUsesNations()),
                season.getFormat(), teamRefs, stageViews, honourViews);
    }

    /** Team references with that year's profile, nation and current logo/flag; reused by other read services. */
    @Transactional(readOnly = true)
    public Map<Long, SeasonView.TeamRef> teamRefs(Collection<Long> teamIds, Integer year) {
        Map<Long, SeasonView.TeamRef> out = new LinkedHashMap<>();
        if (teamIds == null || teamIds.isEmpty()) return out;
        List<Team> teamList = teams.findByIdIn(teamIds);
        Set<Long> nationIds = new LinkedHashSet<>();
        teamList.forEach(t -> { if (t.getNationId() != null) nationIds.add(t.getNationId()); });
        Map<Long, Nation> nationById = new HashMap<>();
        if (!nationIds.isEmpty()) nations.findAllById(nationIds).forEach(n -> nationById.put(n.getId(), n));
        Map<Long, TeamProfile> profileByTeam = new HashMap<>();
        if (year != null) profiles.findByTeamIdInAndYear(teamIds, year).forEach(p -> profileByTeam.put(p.getTeamId(), p));
        Map<Long, String> logos = assetLookup.currentUrls("TEAM", teamIds, "LOGO");
        Map<Long, String> flags = assetLookup.currentUrls("NATION", nationIds, "FLAG");

        for (Team t : teamList) {
            Nation n = t.getNationId() == null ? null : nationById.get(t.getNationId());
            TeamProfile p = profileByTeam.get(t.getId());
            out.put(t.getId(), new SeasonView.TeamRef(t.getId(), t.getKey(), t.getName(), t.getShortName(), t.getCode(), t.getType().name(),
                    t.getNationId(), n == null ? null : n.getCode(), n == null ? null : n.getName(),
                    p == null ? null : p.getColors(), n == null ? null : n.getColors(),
                    p == null ? null : p.getDisplayName(), p == null ? null : p.getSponsor(),
                    logos.get(t.getId()), n == null ? null : flags.get(n.getId())));
        }
        return out;
    }

    static SeasonView.StandingView standingView(Standing s, List<StandingRow> rows) {
        List<SeasonView.StandingRowView> rv = rows.stream().map(r -> new SeasonView.StandingRowView(r.getTeamId(), r.getPosition(),
                r.getPlayed(), r.getWon(), r.getDrawn(), r.getLost(), r.getGoalsFor(), r.getGoalsAgainst(), r.getGoalDiff(),
                r.getPoints(), r.getAdjustment(), r.getZone(), r.getTieNote())).toList();
        return new SeasonView.StandingView(s.getId(), s.getType().name(), s.getCheckpointRound(), rv);
    }

    private static void addTeams(Set<Long> ids, Match m) {
        if (m.getHomeTeamId() != null) ids.add(m.getHomeTeamId());
        if (m.getAwayTeamId() != null) ids.add(m.getAwayTeamId());
    }

    private static <T> Map<Long, List<T>> groupBy(List<T> items, java.util.function.Function<T, Long> key) {
        Map<Long, List<T>> out = new LinkedHashMap<>();
        for (T item : items) {
            Long k = key.apply(item);
            if (k == null) continue;
            out.computeIfAbsent(k, x -> new ArrayList<>()).add(item);
        }
        return out;
    }
}
