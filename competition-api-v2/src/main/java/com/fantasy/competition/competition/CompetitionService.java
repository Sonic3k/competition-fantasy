package com.fantasy.competition.competition;

import com.fantasy.competition.asset.AssetLookup;
import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.Json;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.common.Slugs;
import com.fantasy.competition.competition.CompetitionDtos.CompetitionDetail;
import com.fantasy.competition.competition.CompetitionDtos.CompetitionDto;
import com.fantasy.competition.competition.CompetitionDtos.CompetitionRequest;
import com.fantasy.competition.competition.CompetitionDtos.GroupRequest;
import com.fantasy.competition.competition.CompetitionDtos.HonourRequest;
import com.fantasy.competition.competition.CompetitionDtos.KoRoundRequest;
import com.fantasy.competition.competition.CompetitionDtos.MatchRequest;
import com.fantasy.competition.competition.CompetitionDtos.ResultRequest;
import com.fantasy.competition.competition.CompetitionDtos.RoundRequest;
import com.fantasy.competition.competition.CompetitionDtos.SeasonRequest;
import com.fantasy.competition.competition.CompetitionDtos.SeasonSummary;
import com.fantasy.competition.competition.CompetitionDtos.SeasonTeamInput;
import com.fantasy.competition.competition.CompetitionDtos.StageRequest;
import com.fantasy.competition.competition.CompetitionDtos.TieRequest;
import com.fantasy.competition.format.FormatService;
import com.fantasy.competition.format.model.FormatDefinition;
import com.fantasy.competition.format.model.KoRoundDef;
import com.fantasy.competition.format.model.StageDef;
import com.fantasy.competition.format.model.StageType;
import com.fantasy.competition.standings.StandingsService;
import com.fantasy.competition.team.Team;
import com.fantasy.competition.team.TeamRepository;
import com.fantasy.competition.universe.Universe;
import com.fantasy.competition.universe.UniverseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Competitions, seasons and their structure (stages, groups, rounds, ties, matches). */
@Service
public class CompetitionService {

    private final UniverseRepository universes;
    private final CompetitionRepository competitions;
    private final SeasonRepository seasons;
    private final SeasonTeamRepository seasonTeams;
    private final StageRepository stages;
    private final StageGroupRepository groups;
    private final StageGroupTeamRepository groupTeams;
    private final RoundRepository rounds;
    private final KoRoundRepository koRounds;
    private final TieRepository ties;
    private final MatchRepository matches;
    private final HonourRepository honours;
    private final TeamRepository teams;
    private final FormatService formats;
    private final StandingsService standingsService;
    private final AssetLookup assets;
    private final Json json;

    public CompetitionService(UniverseRepository universes, CompetitionRepository competitions, SeasonRepository seasons,
                              SeasonTeamRepository seasonTeams, StageRepository stages, StageGroupRepository groups,
                              StageGroupTeamRepository groupTeams, RoundRepository rounds, KoRoundRepository koRounds,
                              TieRepository ties, MatchRepository matches, HonourRepository honours, TeamRepository teams,
                              FormatService formats, StandingsService standingsService, AssetLookup assets, Json json) {
        this.universes = universes;
        this.competitions = competitions;
        this.seasons = seasons;
        this.seasonTeams = seasonTeams;
        this.stages = stages;
        this.groups = groups;
        this.groupTeams = groupTeams;
        this.rounds = rounds;
        this.koRounds = koRounds;
        this.ties = ties;
        this.matches = matches;
        this.honours = honours;
        this.teams = teams;
        this.formats = formats;
        this.standingsService = standingsService;
        this.assets = assets;
        this.json = json;
    }

    // ---- competitions ----

    @Transactional(readOnly = true)
    public List<CompetitionDto> list(String universeKey) {
        Universe u = universes.findByKey(universeKey).orElseThrow(() -> new NotFoundException("Universe", universeKey));
        List<Competition> list = competitions.findByUniverseIdOrderByTierAscNameAsc(u.getId());
        Map<Long, String> logos = assets.currentUrls("COMPETITION", list.stream().map(Competition::getId).toList(), "LOGO");
        return list.stream().map(c -> dto(c, logos.get(c.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public CompetitionDetail detail(Long id) {
        Competition c = competition(id);
        List<Season> list = seasons.findByCompetitionIdOrderByYearDescNameDesc(id);
        return new CompetitionDetail(dto(c, assets.currentUrls("COMPETITION", List.of(id), "LOGO").get(id)), summaries(list));
    }

    @Transactional
    public CompetitionDto create(String universeKey, CompetitionRequest req) {
        Universe u = universes.findByKey(universeKey).orElseThrow(() -> new NotFoundException("Universe", universeKey));
        String key = Slugs.of(req.key());
        if (competitions.findByUniverseIdAndKey(u.getId(), key).isPresent()) throw new BadRequestException("Competition key already exists: " + key);
        Competition c = new Competition();
        c.setUniverseId(u.getId());
        c.setKey(key);
        apply(c, req);
        return dto(competitions.save(c), null);
    }

    @Transactional
    public CompetitionDto update(Long id, CompetitionRequest req) {
        Competition c = competition(id);
        apply(c, req);
        return dto(competitions.save(c), assets.currentUrls("COMPETITION", List.of(id), "LOGO").get(id));
    }

    @Transactional
    public void delete(Long id) {
        competitions.delete(competition(id));
    }

    private void apply(Competition c, CompetitionRequest req) {
        c.setName(req.name());
        if (req.sport() != null && !req.sport().isBlank()) c.setSport(req.sport().toUpperCase());
        if (req.teamLevel() != null) c.setTeamLevel(req.teamLevel());
        c.setTier(req.tier());
        c.setDescription(req.description());
    }

    private CompetitionDto dto(Competition c, String logoUrl) {
        int count = seasons.findByCompetitionIdOrderByYearDescNameDesc(c.getId()).size();
        Universe u = universes.findById(c.getUniverseId()).orElse(null);
        return new CompetitionDto(c.getId(), c.getUniverseId(), u == null ? null : u.getKey(), u == null ? null : u.getName(), c.getKey(),
                c.getName(), c.getSport(), c.getTeamLevel().name(), c.getTier(), c.getDescription(), count, logoUrl);
    }

    public Competition competition(Long id) {
        return competitions.findById(id).orElseThrow(() -> new NotFoundException("Competition", id));
    }

    public Season season(Long id) {
        return seasons.findById(id).orElseThrow(() -> new NotFoundException("Season", id));
    }

    // ---- seasons ----

    @Transactional(readOnly = true)
    public List<SeasonSummary> summaries(List<Season> list) {
        if (list.isEmpty()) return List.of();
        List<Long> ids = list.stream().map(Season::getId).toList();
        Map<Long, Long> championBySeason = new HashMap<>();
        for (Honour h : honours.findBySeasonIdIn(ids)) {
            if (h.getKind() == Enums.HonourKind.CHAMPION) championBySeason.putIfAbsent(h.getSeasonId(), h.getTeamId());
        }
        Map<Long, String> teamNames = new HashMap<>();
        if (!championBySeason.isEmpty()) {
            for (Team t : teams.findByIdIn(new LinkedHashSet<>(championBySeason.values()))) teamNames.put(t.getId(), t.getName());
        }
        List<SeasonSummary> out = new ArrayList<>();
        for (Season s : list) {
            Long champ = championBySeason.get(s.getId());
            int teamCount = seasonTeams.findBySeasonIdOrderBySeedAscIdAsc(s.getId()).size();
            out.add(new SeasonSummary(s.getId(), s.getCompetitionId(), s.getKey(), s.getName(), s.getYear(), s.getStartDate(), s.getEndDate(),
                    s.getStatus().name(), s.getPresetKey(), champ, champ == null ? null : teamNames.get(champ), teamCount));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public SeasonSummary summary(Long seasonId) {
        return summaries(List.of(season(seasonId))).get(0);
    }

    @Transactional
    public SeasonSummary createSeason(Long competitionId, SeasonRequest req) {
        Competition c = competition(competitionId);
        String key = Slugs.of(req.key());
        if (seasons.findByCompetitionIdAndKey(c.getId(), key).isPresent()) throw new BadRequestException("Season key already exists: " + key);
        Season s = new Season();
        s.setCompetitionId(c.getId());
        s.setKey(key);
        s.setFormat(formats.resolveFormatJson(req.presetKey(), req.format()));
        s.setPresetKey(req.presetKey());
        applySeason(s, req);
        s = seasons.save(s);
        if (req.scaffold() != null && req.scaffold()) scaffold(s);
        return summary(s.getId());
    }

    @Transactional
    public SeasonSummary updateSeason(Long seasonId, SeasonRequest req) {
        Season s = season(seasonId);
        if (req.format() != null && !req.format().isNull() && !req.format().isEmpty()) {
            s.setFormat(formats.resolveFormatJson(null, req.format()));
        } else if (req.presetKey() != null && !req.presetKey().equals(s.getPresetKey())) {
            s.setFormat(formats.resolveFormatJson(req.presetKey(), null));
        }
        if (req.presetKey() != null) s.setPresetKey(req.presetKey());
        applySeason(s, req);
        seasons.save(s);
        return summary(seasonId);
    }

    private static void applySeason(Season s, SeasonRequest req) {
        s.setName(req.name());
        s.setYear(req.year());
        s.setStartDate(req.startDate());
        s.setEndDate(req.endDate());
        if (req.status() != null) s.setStatus(req.status());
        s.setNotes(req.notes());
    }

    @Transactional
    public void deleteSeason(Long seasonId) {
        seasons.delete(season(seasonId));
    }

    /** Replaces the season's team list. */
    @Transactional
    public int setSeasonTeams(Long seasonId, List<SeasonTeamInput> input) {
        season(seasonId);
        seasonTeams.deleteBySeasonId(seasonId);
        seasonTeams.flush();
        Set<Long> seen = new LinkedHashSet<>();
        int n = 0;
        for (SeasonTeamInput in : input == null ? List.<SeasonTeamInput>of() : input) {
            if (in.teamId() == null || !seen.add(in.teamId())) continue;
            teams.findById(in.teamId()).orElseThrow(() -> new NotFoundException("Team", in.teamId()));
            SeasonTeam st = new SeasonTeam();
            st.setSeasonId(seasonId);
            st.setTeamId(in.teamId());
            st.setSeed(in.seed());
            st.setPot(in.pot());
            seasonTeams.save(st);
            n++;
        }
        return n;
    }

    /** Creates stages, empty groups and knockout rounds from the season's format (no teams, no fixtures). */
    @Transactional
    public void scaffold(Season s) {
        if (!stages.findBySeasonIdOrderByOrdinalAsc(s.getId()).isEmpty()) throw new BadRequestException("Season already has stages");
        FormatDefinition def = formats.parse(s.getFormat());
        int ordinal = 1;
        for (StageDef sd : def.stagesOrEmpty()) {
            Stage stage = newStage(s.getId(), ordinal++, sd.key(), sd.name(), sd.type(), json.write(sd));
            if (sd.type() == StageType.ROUND_ROBIN || sd.type() == StageType.LEAGUE_PHASE) {
                int n = sd.type() == StageType.LEAGUE_PHASE ? 1 : sd.groupsOrDefault();
                for (int i = 0; i < n; i++) {
                    String key = n == 1 ? "T" : String.valueOf((char) ('A' + i));
                    String name = n == 1 ? (sd.type() == StageType.LEAGUE_PHASE ? "League phase" : "Table") : "Group " + key;
                    newGroup(stage.getId(), i + 1, key, name);
                }
            } else {
                int o = 1;
                for (KoRoundDef r : sd.roundsOrEmpty()) {
                    newKoRound(stage.getId(), o++, r.key(), r.name() == null ? r.key() : r.name(), r.legs() == null ? sd.legsOrDefault() : r.legs(), r.isPlacement());
                }
            }
        }
    }

    // ---- structure ----

    @Transactional
    public Stage addStage(Long seasonId, StageRequest req) {
        Season s = season(seasonId);
        if (req.type() == null) throw new BadRequestException("stage type is required");
        int ordinal = req.ordinal() == null ? stages.findBySeasonIdOrderByOrdinalAsc(seasonId).size() + 1 : req.ordinal();
        String config = req.config() == null || req.config().isNull() ? formats.stageDef(s.getFormat(), req.key()).map(json::write).orElse(null) : req.config().toString();
        return newStage(seasonId, ordinal, req.key(), req.name(), req.type(), config);
    }

    private Stage newStage(Long seasonId, int ordinal, String key, String name, StageType type, String config) {
        Stage stage = new Stage();
        stage.setSeasonId(seasonId);
        stage.setOrdinal(ordinal);
        stage.setKey(key);
        stage.setName(name);
        stage.setType(type);
        stage.setConfig(config);
        return stages.save(stage);
    }

    @Transactional
    public void deleteStage(Long stageId) {
        stages.delete(stages.findById(stageId).orElseThrow(() -> new NotFoundException("Stage", stageId)));
    }

    @Transactional
    public StageGroup addGroup(Long stageId, GroupRequest req) {
        stages.findById(stageId).orElseThrow(() -> new NotFoundException("Stage", stageId));
        int ordinal = req.ordinal() == null ? groups.findByStageIdOrderByOrdinalAsc(stageId).size() + 1 : req.ordinal();
        StageGroup g = newGroup(stageId, ordinal, req.key(), req.name());
        if (req.teamIds() != null) setGroupTeams(g.getId(), req.teamIds());
        return g;
    }

    private StageGroup newGroup(Long stageId, int ordinal, String key, String name) {
        StageGroup g = new StageGroup();
        g.setStageId(stageId);
        g.setOrdinal(ordinal);
        g.setKey(key);
        g.setName(name);
        return groups.save(g);
    }

    @Transactional
    public void setGroupTeams(Long groupId, List<Long> teamIds) {
        groups.findById(groupId).orElseThrow(() -> new NotFoundException("Stage group", groupId));
        groupTeams.deleteAll(groupTeams.findByStageGroupIdOrderByPositionAsc(groupId));
        groupTeams.flush();
        int pos = 1;
        Set<Long> seen = new LinkedHashSet<>();
        for (Long teamId : teamIds) {
            if (teamId == null || !seen.add(teamId)) continue;
            StageGroupTeam gt = new StageGroupTeam();
            gt.setStageGroupId(groupId);
            gt.setTeamId(teamId);
            gt.setPosition(pos++);
            groupTeams.save(gt);
        }
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        groups.delete(groups.findById(groupId).orElseThrow(() -> new NotFoundException("Stage group", groupId)));
    }

    @Transactional
    public Round addRound(Long groupId, RoundRequest req) {
        groups.findById(groupId).orElseThrow(() -> new NotFoundException("Stage group", groupId));
        int number = req.number() == null ? rounds.findByStageGroupIdOrderByNumberAsc(groupId).size() + 1 : req.number();
        Round r = new Round();
        r.setStageGroupId(groupId);
        r.setNumber(number);
        r.setName(req.name() == null ? "Round " + number : req.name());
        r.setStartDate(req.startDate());
        r.setEndDate(req.endDate());
        return rounds.save(r);
    }

    @Transactional
    public void deleteRound(Long roundId) {
        rounds.delete(rounds.findById(roundId).orElseThrow(() -> new NotFoundException("Round", roundId)));
    }

    @Transactional
    public KoRound addKoRound(Long stageId, KoRoundRequest req) {
        stages.findById(stageId).orElseThrow(() -> new NotFoundException("Stage", stageId));
        int ordinal = req.ordinal() == null ? koRounds.findByStageIdOrderByOrdinalAsc(stageId).size() + 1 : req.ordinal();
        return newKoRound(stageId, ordinal, req.key(), req.name(), req.legs() == null ? 1 : req.legs(), req.placement() != null && req.placement());
    }

    private KoRound newKoRound(Long stageId, int ordinal, String key, String name, int legs, boolean placement) {
        KoRound ko = new KoRound();
        ko.setStageId(stageId);
        ko.setOrdinal(ordinal);
        ko.setKey(key);
        ko.setName(name);
        ko.setLegs(legs);
        ko.setPlacement(placement);
        return koRounds.save(ko);
    }

    @Transactional
    public void deleteKoRound(Long koRoundId) {
        koRounds.delete(koRounds.findById(koRoundId).orElseThrow(() -> new NotFoundException("Knockout round", koRoundId)));
    }

    @Transactional
    public Tie addTie(Long koRoundId, TieRequest req) {
        koRounds.findById(koRoundId).orElseThrow(() -> new NotFoundException("Knockout round", koRoundId));
        Tie t = new Tie();
        t.setKoRoundId(koRoundId);
        t.setPosition(req.position() == null ? ties.findByKoRoundIdOrderByPositionAsc(koRoundId).size() + 1 : req.position());
        t.setHomeTeamId(req.homeTeamId());
        t.setAwayTeamId(req.awayTeamId());
        t.setHomeSource(Json.text(req.homeSource()));
        t.setAwaySource(Json.text(req.awaySource()));
        return ties.save(t);
    }

    @Transactional
    public Tie updateTie(Long tieId, TieRequest req) {
        Tie t = ties.findById(tieId).orElseThrow(() -> new NotFoundException("Tie", tieId));
        if (req.position() != null) t.setPosition(req.position());
        t.setHomeTeamId(req.homeTeamId());
        t.setAwayTeamId(req.awayTeamId());
        if (req.homeSource() != null) t.setHomeSource(Json.text(req.homeSource()));
        if (req.awaySource() != null) t.setAwaySource(Json.text(req.awaySource()));
        t = ties.save(t);
        resolveTie(t);
        return t;
    }

    @Transactional
    public void deleteTie(Long tieId) {
        ties.delete(ties.findById(tieId).orElseThrow(() -> new NotFoundException("Tie", tieId)));
    }

    @Transactional
    public Match addMatch(Long seasonId, MatchRequest req) {
        Season s = season(seasonId);
        if (req.roundId() == null && req.tieId() == null) throw new BadRequestException("roundId or tieId is required");
        Long stageId;
        if (req.roundId() != null) {
            Round r = rounds.findById(req.roundId()).orElseThrow(() -> new NotFoundException("Round", req.roundId()));
            StageGroup g = groups.findById(r.getStageGroupId()).orElseThrow(() -> new NotFoundException("Stage group", r.getStageGroupId()));
            stageId = g.getStageId();
        } else {
            Tie t = ties.findById(req.tieId()).orElseThrow(() -> new NotFoundException("Tie", req.tieId()));
            KoRound ko = koRounds.findById(t.getKoRoundId()).orElseThrow(() -> new NotFoundException("Knockout round", t.getKoRoundId()));
            stageId = ko.getStageId();
        }
        Match m = new Match();
        m.setSeasonId(s.getId());
        m.setStageId(stageId);
        m.setRoundId(req.roundId());
        m.setTieId(req.tieId());
        m.setLeg(req.leg() == null ? 1 : req.leg());
        m.setHomeTeamId(req.homeTeamId());
        m.setAwayTeamId(req.awayTeamId());
        m.setMatchDate(req.date());
        m.setStadiumId(req.stadiumId());
        m.setStatus(req.status() == null ? (req.homeScore() != null && req.awayScore() != null ? Enums.MatchStatus.PLAYED : Enums.MatchStatus.SCHEDULED) : req.status());
        m.setHomeScore(req.homeScore());
        m.setAwayScore(req.awayScore());
        m.setHomeEt(req.homeEt());
        m.setAwayEt(req.awayEt());
        m.setHomePens(req.homePens());
        m.setAwayPens(req.awayPens());
        m.setWalkover(req.walkover());
        if (req.source() != null) m.setSource(req.source());
        m.setSourceRef(req.sourceRef());
        if (req.confidence() != null) m.setConfidence(req.confidence());
        m.setNotes(req.notes());
        m.setWinnerTeamId(winnerOf(m));
        m = matches.save(m);
        afterResultChange(m);
        return m;
    }

    @Transactional
    public Match updateMatch(Long matchId, MatchRequest req) {
        Match m = match(matchId);
        if (req.leg() != null) m.setLeg(req.leg());
        if (req.homeTeamId() != null) m.setHomeTeamId(req.homeTeamId());
        if (req.awayTeamId() != null) m.setAwayTeamId(req.awayTeamId());
        m.setMatchDate(req.date());
        m.setStadiumId(req.stadiumId());
        if (req.status() != null) m.setStatus(req.status());
        m.setHomeScore(req.homeScore());
        m.setAwayScore(req.awayScore());
        m.setHomeEt(req.homeEt());
        m.setAwayEt(req.awayEt());
        m.setHomePens(req.homePens());
        m.setAwayPens(req.awayPens());
        m.setWalkover(req.walkover());
        if (req.source() != null) m.setSource(req.source());
        m.setSourceRef(req.sourceRef());
        if (req.confidence() != null) m.setConfidence(req.confidence());
        m.setNotes(req.notes());
        m.setWinnerTeamId(winnerOf(m));
        m = matches.save(m);
        afterResultChange(m);
        return m;
    }

    /** Score entry: only result fields change; tables and ties are recomputed. */
    @Transactional
    public Match setResult(Long matchId, ResultRequest req) {
        Match m = match(matchId);
        m.setHomeScore(req.homeScore());
        m.setAwayScore(req.awayScore());
        m.setHomeEt(req.homeEt());
        m.setAwayEt(req.awayEt());
        m.setHomePens(req.homePens());
        m.setAwayPens(req.awayPens());
        m.setWalkover(req.walkover());
        if (req.date() != null) m.setMatchDate(req.date());
        if (req.status() != null) m.setStatus(req.status());
        else if (req.homeScore() != null && req.awayScore() != null) m.setStatus(Enums.MatchStatus.PLAYED);
        if (req.confidence() != null) m.setConfidence(req.confidence());
        if (req.source() != null) m.setSource(req.source());
        if (req.sourceRef() != null) m.setSourceRef(req.sourceRef());
        if (req.notes() != null) m.setNotes(req.notes());
        m.setWinnerTeamId(winnerOf(m));
        m = matches.save(m);
        afterResultChange(m);
        return m;
    }

    @Transactional
    public void deleteMatch(Long matchId) {
        Match m = match(matchId);
        matches.delete(m);
        afterResultChange(m);
    }

    public Match match(Long id) {
        return matches.findById(id).orElseThrow(() -> new NotFoundException("Match", id));
    }

    private static Long winnerOf(Match m) {
        if (m.getWalkover() != null) return m.getWalkover() == Enums.Walkover.HOME ? m.getHomeTeamId() : m.getAwayTeamId();
        if (!m.hasScore()) return null;
        int h = m.homeGoalsFinal();
        int a = m.awayGoalsFinal();
        if (h != a) return h > a ? m.getHomeTeamId() : m.getAwayTeamId();
        if (m.getHomePens() != null && m.getAwayPens() != null && !m.getHomePens().equals(m.getAwayPens())) {
            return m.getHomePens() > m.getAwayPens() ? m.getHomeTeamId() : m.getAwayTeamId();
        }
        return null;
    }

    private void afterResultChange(Match m) {
        if (m.getRoundId() != null) {
            rounds.findById(m.getRoundId()).ifPresent(r -> standingsService.recalculate(r.getStageGroupId()));
        }
        if (m.getTieId() != null) {
            ties.findById(m.getTieId()).ifPresent(this::resolveTie);
        }
    }

    /** Recomputes a tie's winner from its matches using the stage's decider list. */
    @Transactional
    public void resolveTie(Tie tie) {
        KoRound ko = koRounds.findById(tie.getKoRoundId()).orElse(null);
        if (ko == null) return;
        Stage stage = stages.findById(ko.getStageId()).orElse(null);
        List<String> decider = List.of("EXTRA_TIME", "PENALTIES");
        if (stage != null && stage.getConfig() != null) {
            StageDef def = json.read(stage.getConfig(), StageDef.class);
            if (def != null) decider = def.deciderOrDefault();
        }
        TieResolver.Outcome outcome = TieResolver.resolve(tie, matches.findByTieIdOrderByLegAsc(tie.getId()), decider, ko.getLegs());
        tie.setWinnerTeamId(outcome.winnerTeamId());
        tie.setResolution(outcome.resolution());
        ties.save(tie);
    }

    // ---- honours ----

    @Transactional
    public Honour addHonour(Long seasonId, HonourRequest req) {
        season(seasonId);
        if (req.teamId() == null || req.kind() == null) throw new BadRequestException("teamId and kind are required");
        Honour h = new Honour();
        h.setSeasonId(seasonId);
        h.setTeamId(req.teamId());
        h.setKind(req.kind());
        h.setManual(true);
        return honours.save(h);
    }

    @Transactional
    public void deleteHonour(Long honourId) {
        honours.deleteById(honourId);
    }
}
