package com.fantasy.competition.standings;

import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.Json;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.competition.Match;
import com.fantasy.competition.competition.MatchRepository;
import com.fantasy.competition.competition.Round;
import com.fantasy.competition.competition.RoundRepository;
import com.fantasy.competition.competition.Season;
import com.fantasy.competition.competition.SeasonRepository;
import com.fantasy.competition.competition.Stage;
import com.fantasy.competition.competition.StageGroup;
import com.fantasy.competition.competition.StageGroupRepository;
import com.fantasy.competition.competition.StageGroupTeam;
import com.fantasy.competition.competition.StageGroupTeamRepository;
import com.fantasy.competition.competition.StageRepository;
import com.fantasy.competition.format.FormatService;
import com.fantasy.competition.format.model.Points;
import com.fantasy.competition.format.model.StageDef;
import com.fantasy.competition.format.model.Zone;
import com.fantasy.competition.team.Team;
import com.fantasy.competition.team.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Computes CALCULATED tables from matches and stores RECORDED tables from the notebook. */
@Service
public class StandingsService {

    public record RecordedRowInput(Long teamId, Integer position, Integer played, Integer won, Integer drawn, Integer lost,
                                   Integer goalsFor, Integer goalsAgainst, Integer points, String zone) {}

    private final StandingRepository standings;
    private final StandingRowRepository rows;
    private final MatchRepository matches;
    private final RoundRepository rounds;
    private final StageGroupRepository groups;
    private final StageGroupTeamRepository groupTeams;
    private final StageRepository stages;
    private final SeasonRepository seasons;
    private final TeamRepository teams;
    private final FormatService formats;
    private final Json json;

    public StandingsService(StandingRepository standings, StandingRowRepository rows, MatchRepository matches,
                            RoundRepository rounds, StageGroupRepository groups, StageGroupTeamRepository groupTeams,
                            StageRepository stages, SeasonRepository seasons, TeamRepository teams,
                            FormatService formats, Json json) {
        this.standings = standings;
        this.rows = rows;
        this.matches = matches;
        this.rounds = rounds;
        this.groups = groups;
        this.groupTeams = groupTeams;
        this.stages = stages;
        this.seasons = seasons;
        this.teams = teams;
        this.formats = formats;
        this.json = json;
    }

    /** Recomputes and stores the CALCULATED table (checkpoint 0) of one group. */
    @Transactional
    public Standing recalculate(long stageGroupId) {
        StageGroup group = groups.findById(stageGroupId).orElseThrow(() -> new NotFoundException("Stage group", stageGroupId));
        List<RankedRow> ranked = compute(group, null);
        StageDef def = stageDef(group);

        Standing standing = standings.findByStageGroupIdAndTypeAndCheckpointRound(stageGroupId, StandingType.CALCULATED, 0)
                .orElseGet(() -> {
                    Standing s = new Standing();
                    s.setStageGroupId(stageGroupId);
                    s.setType(StandingType.CALCULATED);
                    s.setCheckpointRound(0);
                    return s;
                });
        standing.setComputedAt(Instant.now());
        standing = standings.save(standing);
        rows.deleteByStandingId(standing.getId());
        rows.flush();

        Map<Integer, String> zones = zoneByPosition(def);
        List<StandingRow> out = new ArrayList<>();
        for (RankedRow r : ranked) {
            out.add(toRow(standing.getId(), r, zones.get(r.position())));
        }
        rows.saveAll(out);
        return standing;
    }

    /** Recalculates every group of a season (after an import or a bulk edit). */
    @Transactional
    public int recalculateSeason(long seasonId) {
        List<Long> stageIds = stages.findBySeasonIdOrderByOrdinalAsc(seasonId).stream().map(Stage::getId).toList();
        if (stageIds.isEmpty()) return 0;
        int n = 0;
        for (StageGroup g : groups.findByStageIdInOrderByOrdinalAsc(stageIds)) {
            recalculate(g.getId());
            n++;
        }
        return n;
    }

    /** Table of a group as of a round number (inclusive); null = all rounds. Not persisted. */
    @Transactional(readOnly = true)
    public List<RankedRow> computeAsOf(long stageGroupId, Integer upToRound) {
        StageGroup group = groups.findById(stageGroupId).orElseThrow(() -> new NotFoundException("Stage group", stageGroupId));
        return compute(group, upToRound);
    }

    /** Stores a RECORDED table copied from the notebook; checkpointRound 0 = final. Replaces an existing one at the same checkpoint. */
    @Transactional
    public Standing saveRecorded(long stageGroupId, int checkpointRound, List<RecordedRowInput> input) {
        groups.findById(stageGroupId).orElseThrow(() -> new NotFoundException("Stage group", stageGroupId));
        if (input == null || input.isEmpty()) throw new BadRequestException("rows are required");
        Standing standing = standings.findByStageGroupIdAndTypeAndCheckpointRound(stageGroupId, StandingType.RECORDED, checkpointRound)
                .orElseGet(() -> {
                    Standing s = new Standing();
                    s.setStageGroupId(stageGroupId);
                    s.setType(StandingType.RECORDED);
                    s.setCheckpointRound(checkpointRound);
                    return s;
                });
        standing.setComputedAt(Instant.now());
        standing = standings.save(standing);
        rows.deleteByStandingId(standing.getId());
        rows.flush();
        List<StandingRow> out = new ArrayList<>();
        int pos = 1;
        for (RecordedRowInput in : input) {
            if (in.teamId() == null) throw new BadRequestException("recorded row without teamId");
            StandingRow r = new StandingRow();
            r.setStandingId(standing.getId());
            r.setTeamId(in.teamId());
            r.setPosition(in.position() == null ? pos : in.position());
            r.setPlayed(nz(in.played()));
            r.setWon(nz(in.won()));
            r.setDrawn(nz(in.drawn()));
            r.setLost(nz(in.lost()));
            r.setGoalsFor(nz(in.goalsFor()));
            r.setGoalsAgainst(nz(in.goalsAgainst()));
            r.setGoalDiff(nz(in.goalsFor()) - nz(in.goalsAgainst()));
            r.setPoints(nz(in.points()));
            r.setZone(in.zone());
            out.add(r);
            pos++;
        }
        rows.saveAll(out);
        return standing;
    }

    @Transactional
    public void deleteRecorded(long stageGroupId, int checkpointRound) {
        standings.findByStageGroupIdAndTypeAndCheckpointRound(stageGroupId, StandingType.RECORDED, checkpointRound)
                .ifPresent(s -> {
                    rows.deleteByStandingId(s.getId());
                    standings.delete(s);
                });
    }

    // ---- internals ----

    private List<RankedRow> compute(StageGroup group, Integer upToRound) {
        StageDef def = stageDef(group);
        Points points = pointsOf(group);

        Set<Long> teamIds = new LinkedHashSet<>();
        for (StageGroupTeam gt : groupTeams.findByStageGroupIdOrderByPositionAsc(group.getId())) {
            if (gt.getTeamId() != null) teamIds.add(gt.getTeamId());
        }

        List<Round> groupRounds = rounds.findByStageGroupIdOrderByNumberAsc(group.getId());
        Map<Long, Integer> roundNumber = new HashMap<>();
        for (Round r : groupRounds) roundNumber.put(r.getId(), r.getNumber());
        List<MatchResult> results = new ArrayList<>();
        if (!groupRounds.isEmpty()) {
            for (Match m : matches.findByRoundIdIn(roundNumber.keySet())) {
                Integer n = roundNumber.get(m.getRoundId());
                if (upToRound != null && n != null && n > upToRound) continue;
                if (!m.hasScore() || m.getHomeTeamId() == null || m.getAwayTeamId() == null) continue;
                results.add(new MatchResult(m.getHomeTeamId(), m.getAwayTeamId(), m.getHomeScore(), m.getAwayScore()));
                teamIds.add(m.getHomeTeamId());
                teamIds.add(m.getAwayTeamId());
            }
        }

        Map<Long, String> names = new HashMap<>();
        if (!teamIds.isEmpty()) {
            for (Team t : teams.findByIdIn(teamIds)) names.put(t.getId(), t.getName());
        }
        TableRules rules = new TableRules(points.winOrDefault(), points.drawOrDefault(), points.lossOrDefault(), def.tiebreakersOrDefault());
        return StandingsCalculator.rank(teamIds, names, results, rules, Map.of());
    }

    private StageDef stageDef(StageGroup group) {
        Stage stage = stages.findById(group.getStageId()).orElseThrow(() -> new NotFoundException("Stage", group.getStageId()));
        StageDef def = json.read(stage.getConfig(), StageDef.class);
        if (def != null) return def;
        return new StageDef(stage.getKey(), stage.getName(), stage.getType(), 1, null, 1, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }

    private Points pointsOf(StageGroup group) {
        Stage stage = stages.findById(group.getStageId()).orElseThrow(() -> new NotFoundException("Stage", group.getStageId()));
        Season season = seasons.findById(stage.getSeasonId()).orElseThrow(() -> new NotFoundException("Season", stage.getSeasonId()));
        try {
            return formats.parse(season.getFormat()).pointsOrDefault();
        } catch (RuntimeException e) {
            return Points.STANDARD;
        }
    }

    static Map<Integer, String> zoneByPosition(StageDef def) {
        Map<Integer, String> zones = new HashMap<>();
        for (Zone z : def.zonesOrEmpty()) {
            if (z.positions() == null) continue;
            for (Integer p : z.positions()) zones.putIfAbsent(p, z.kind());
        }
        return zones;
    }

    private static StandingRow toRow(Long standingId, RankedRow r, String zone) {
        StandingRow row = new StandingRow();
        row.setStandingId(standingId);
        row.setTeamId(r.teamId());
        row.setPosition(r.position());
        row.setPlayed(r.played());
        row.setWon(r.won());
        row.setDrawn(r.drawn());
        row.setLost(r.lost());
        row.setGoalsFor(r.goalsFor());
        row.setGoalsAgainst(r.goalsAgainst());
        row.setGoalDiff(r.goalDiff());
        row.setPoints(r.points());
        row.setAdjustment(r.adjustment());
        row.setZone(zone);
        row.setTieNote(r.tieNote() == null ? null : (r.tieNote().length() > 160 ? r.tieNote().substring(0, 160) : r.tieNote()));
        return row;
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }

    @SuppressWarnings("unused")
    private static boolean same(Object a, Object b) { return Objects.equals(a, b); }
}
