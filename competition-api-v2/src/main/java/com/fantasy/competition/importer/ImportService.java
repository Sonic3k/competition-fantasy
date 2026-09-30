package com.fantasy.competition.importer;

import com.fantasy.competition.common.Json;
import com.fantasy.competition.common.Slugs;
import com.fantasy.competition.competition.Competition;
import com.fantasy.competition.competition.CompetitionRepository;
import com.fantasy.competition.competition.CompetitionService;
import com.fantasy.competition.competition.Enums;
import com.fantasy.competition.competition.Honour;
import com.fantasy.competition.competition.HonourRepository;
import com.fantasy.competition.competition.KoRound;
import com.fantasy.competition.competition.KoRoundRepository;
import com.fantasy.competition.competition.Match;
import com.fantasy.competition.competition.MatchRepository;
import com.fantasy.competition.competition.Round;
import com.fantasy.competition.competition.RoundRepository;
import com.fantasy.competition.competition.Season;
import com.fantasy.competition.competition.SeasonRepository;
import com.fantasy.competition.competition.SeasonTeam;
import com.fantasy.competition.competition.SeasonTeamRepository;
import com.fantasy.competition.competition.Stage;
import com.fantasy.competition.competition.StageGroup;
import com.fantasy.competition.competition.StageGroupRepository;
import com.fantasy.competition.competition.StageGroupTeam;
import com.fantasy.competition.competition.StageGroupTeamRepository;
import com.fantasy.competition.competition.StageRepository;
import com.fantasy.competition.competition.Tie;
import com.fantasy.competition.competition.TieRepository;
import com.fantasy.competition.format.FormatService;
import com.fantasy.competition.format.model.FormatDefinition;
import com.fantasy.competition.format.model.StageDef;
import com.fantasy.competition.format.model.StageType;
import com.fantasy.competition.importer.ImportDocument.CompetitionIn;
import com.fantasy.competition.importer.ImportDocument.GroupIn;
import com.fantasy.competition.importer.ImportDocument.HonourIn;
import com.fantasy.competition.importer.ImportDocument.KitIn;
import com.fantasy.competition.importer.ImportDocument.KoRoundIn;
import com.fantasy.competition.importer.ImportDocument.MatchIn;
import com.fantasy.competition.importer.ImportDocument.NationIn;
import com.fantasy.competition.importer.ImportDocument.ProfileIn;
import com.fantasy.competition.importer.ImportDocument.RecordedIn;
import com.fantasy.competition.importer.ImportDocument.RecordedRowIn;
import com.fantasy.competition.importer.ImportDocument.RoundIn;
import com.fantasy.competition.importer.ImportDocument.SeasonIn;
import com.fantasy.competition.importer.ImportDocument.SeasonTeamIn;
import com.fantasy.competition.importer.ImportDocument.StadiumIn;
import com.fantasy.competition.importer.ImportDocument.StageIn;
import com.fantasy.competition.importer.ImportDocument.TeamIn;
import com.fantasy.competition.importer.ImportDocument.TieIn;
import com.fantasy.competition.importer.ImportDocument.UniverseIn;
import com.fantasy.competition.standings.StandingsService;
import com.fantasy.competition.team.Kit;
import com.fantasy.competition.team.KitKind;
import com.fantasy.competition.team.KitRepository;
import com.fantasy.competition.team.Nation;
import com.fantasy.competition.team.NationRepository;
import com.fantasy.competition.team.Stadium;
import com.fantasy.competition.team.StadiumRepository;
import com.fantasy.competition.team.Team;
import com.fantasy.competition.team.TeamAlias;
import com.fantasy.competition.team.TeamAliasRepository;
import com.fantasy.competition.team.TeamProfile;
import com.fantasy.competition.team.TeamProfileRepository;
import com.fantasy.competition.team.TeamRepository;
import com.fantasy.competition.team.TeamType;
import com.fantasy.competition.universe.Universe;
import com.fantasy.competition.universe.UniverseRepository;
import com.fantasy.competition.universe.UniverseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Runs a JSON import document against the database in one transaction.
 * Dry runs execute everything and roll back at the end, so the report is exact.
 */
@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    public record Report(Long runId, String status, boolean dryRun, Map<String, Integer> counts, List<String> warnings, List<String> errors, List<String> log) {}

    private final ImportRunStore runStore;
    private final ImportChangeRepository changes;
    private final UniverseRepository universes;
    private final NationRepository nations;
    private final StadiumRepository stadiums;
    private final TeamRepository teams;
    private final TeamAliasRepository aliases;
    private final TeamProfileRepository profiles;
    private final KitRepository kits;
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
    private final FormatService formats;
    private final StandingsService standingsService;
    private final CompetitionService competitionService;
    private final TransactionTemplate tx;
    private final Json json;

    public ImportService(ImportRunStore runStore, ImportChangeRepository changes, UniverseRepository universes, NationRepository nations,
                         StadiumRepository stadiums, TeamRepository teams, TeamAliasRepository aliases, TeamProfileRepository profiles,
                         KitRepository kits, CompetitionRepository competitions, SeasonRepository seasons, SeasonTeamRepository seasonTeams,
                         StageRepository stages, StageGroupRepository groups, StageGroupTeamRepository groupTeams, RoundRepository rounds,
                         KoRoundRepository koRounds, TieRepository ties, MatchRepository matches, HonourRepository honours,
                         FormatService formats, StandingsService standingsService, CompetitionService competitionService,
                         PlatformTransactionManager txManager, Json json) {
        this.runStore = runStore;
        this.changes = changes;
        this.universes = universes;
        this.nations = nations;
        this.stadiums = stadiums;
        this.teams = teams;
        this.aliases = aliases;
        this.profiles = profiles;
        this.kits = kits;
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
        this.formats = formats;
        this.standingsService = standingsService;
        this.competitionService = competitionService;
        this.tx = new TransactionTemplate(txManager);
        this.json = json;
    }

    public Report run(String fileName, String jsonText, ImportRun.Source source, boolean dryRun) {
        ImportRun run = runStore.start(fileName, source, sha256(jsonText), dryRun);
        Ctx ctx = new Ctx(run.getId(), dryRun);
        ImportRun.Status status;
        try {
            ImportDocument doc = json.read(jsonText, ImportDocument.class);
            if (doc == null) throw new ImportException("Empty document", List.of());
            tx.execute(s -> {
                process(doc, ctx);
                if (!ctx.errors.isEmpty()) throw new ImportException("Import has errors", ctx.errors);
                if (dryRun) s.setRollbackOnly();
                return null;
            });
            status = ImportRun.Status.SUCCESS;
        } catch (ImportException e) {
            if (!ctx.errors.contains(e.getMessage())) ctx.errors.add(0, e.getMessage());
            ctx.errors.addAll(e.getErrors().stream().filter(x -> !ctx.errors.contains(x)).toList());
            status = ImportRun.Status.FAILED;
        } catch (RuntimeException e) {
            log.error("Import {} failed", fileName, e);
            ctx.errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
            status = ImportRun.Status.FAILED;
        }
        Report report = new Report(run.getId(), status.name(), dryRun, ctx.counts, ctx.warnings, ctx.errors, ctx.log);
        runStore.finish(run.getId(), status, json.write(Map.of("counts", ctx.counts, "warnings", ctx.warnings.size(), "errors", ctx.errors)),
                String.join("\n", ctx.log) + (ctx.warnings.isEmpty() ? "" : "\n\nWARNINGS\n" + String.join("\n", ctx.warnings))
                        + (ctx.errors.isEmpty() ? "" : "\n\nERRORS\n" + String.join("\n", ctx.errors)));
        return report;
    }

    /** Deletes everything a run created, newest first. Updated or replaced records are not restored. */
    @Transactional
    public int revert(Long runId) {
        List<ImportChange> list = changes.findByRunIdOrderByOrdinalDesc(runId);
        int n = 0;
        for (ImportChange c : list) {
            if (!"CREATED".equals(c.getAction())) continue;
            switch (c.getEntityType()) {
                case "UNIVERSE" -> universes.deleteById(c.getEntityId());
                case "NATION" -> nations.deleteById(c.getEntityId());
                case "STADIUM" -> stadiums.deleteById(c.getEntityId());
                case "TEAM" -> teams.deleteById(c.getEntityId());
                case "TEAM_ALIAS" -> aliases.deleteById(c.getEntityId());
                case "TEAM_PROFILE" -> profiles.deleteById(c.getEntityId());
                case "KIT" -> kits.deleteById(c.getEntityId());
                case "COMPETITION" -> competitions.deleteById(c.getEntityId());
                case "SEASON" -> seasons.deleteById(c.getEntityId());
                default -> { continue; }
            }
            n++;
        }
        runStore.finish(runId, ImportRun.Status.REVERTED, null, "Reverted " + n + " created records");
        return n;
    }

    // ---- processing ----

    private void process(ImportDocument doc, Ctx ctx) {
        if (doc.universe() == null || isBlank(doc.universe().key())) throw new ImportException("universe.key is required", List.of());
        Universe universe = upsertUniverse(doc.universe(), ctx);
        ctx.universeId = universe.getId();
        loadUniverseCaches(ctx);

        for (NationIn n : nz(doc.nations())) upsertNation(n, ctx);
        for (StadiumIn s : nz(doc.stadiums())) upsertStadium(s, ctx);
        for (TeamIn t : nz(doc.teams())) upsertTeam(t, ctx);
        for (ProfileIn p : nz(doc.teamProfiles())) upsertProfile(p, ctx);
        for (KitIn k : nz(doc.kits())) upsertKit(k, ctx);
        for (CompetitionIn c : nz(doc.competitions())) upsertCompetition(c, ctx);
        for (SeasonIn s : nz(doc.seasons())) importSeason(s, ctx);
    }

    private Universe upsertUniverse(UniverseIn in, Ctx ctx) {
        String key = Slugs.of(in.key());
        Universe u = universes.findByKey(key).orElse(null);
        boolean created = u == null;
        if (created) { u = new Universe(); u.setKey(key); }
        if (in.name() != null) u.setName(in.name());
        if (u.getName() == null) u.setName(key);
        if (in.description() != null) u.setDescription(in.description());
        if (in.type() != null) u.setType(UniverseType.valueOf(in.type().toUpperCase(Locale.ROOT)));
        if (in.usesNations() != null) u.setUsesNations(in.usesNations());
        u = universes.save(u);
        ctx.record("UNIVERSE", u.getId(), created);
        ctx.log("Universe " + key + (created ? " created" : " updated"));
        return u;
    }

    private void loadUniverseCaches(Ctx ctx) {
        for (Nation n : nations.findByUniverseIdOrderByNameAsc(ctx.universeId)) ctx.nationsByKey.put(n.getKey(), n);
        for (Stadium s : stadiums.findByUniverseIdOrderByNameAsc(ctx.universeId)) ctx.stadiumsByKey.put(s.getKey(), s);
        for (Team t : teams.findByUniverseIdOrderByNameAsc(ctx.universeId)) ctx.addTeam(t);
        for (TeamAlias a : aliases.findByUniverseId(ctx.universeId)) ctx.teamsByAlias.put(a.getAlias().toLowerCase(Locale.ROOT), a.getTeamId());
        for (Competition c : competitions.findByUniverseIdOrderByTierAscNameAsc(ctx.universeId)) ctx.competitionsByKey.put(c.getKey(), c);
    }

    private void upsertNation(NationIn in, Ctx ctx) {
        if (isBlank(in.key())) { ctx.errors.add("nation without key"); return; }
        String key = Slugs.of(in.key());
        Nation n = ctx.nationsByKey.get(key);
        boolean created = n == null;
        if (created) { n = new Nation(); n.setUniverseId(ctx.universeId); n.setKey(key); }
        if (in.name() != null) n.setName(in.name());
        if (in.code() != null) n.setCode(in.code().toUpperCase(Locale.ROOT));
        if (n.getName() == null || n.getCode() == null) { ctx.errors.add("nation " + key + " needs name and code"); return; }
        if (in.colors() != null) n.setColors(Json.text(in.colors()));
        if (in.description() != null) n.setDescription(in.description());
        n = nations.save(n);
        ctx.nationsByKey.put(key, n);
        ctx.record("NATION", n.getId(), created);
    }

    private void upsertStadium(StadiumIn in, Ctx ctx) {
        if (isBlank(in.key())) { ctx.errors.add("stadium without key"); return; }
        String key = Slugs.of(in.key());
        Stadium s = ctx.stadiumsByKey.get(key);
        boolean created = s == null;
        if (created) { s = new Stadium(); s.setUniverseId(ctx.universeId); s.setKey(key); }
        if (in.name() != null) s.setName(in.name());
        if (s.getName() == null) s.setName(key);
        if (in.city() != null) s.setCity(in.city());
        if (in.capacity() != null) s.setCapacity(in.capacity());
        if (in.inspiredBy() != null) s.setInspiredBy(in.inspiredBy());
        if (in.description() != null) s.setDescription(in.description());
        s = stadiums.save(s);
        ctx.stadiumsByKey.put(key, s);
        ctx.record("STADIUM", s.getId(), created);
    }

    private void upsertTeam(TeamIn in, Ctx ctx) {
        if (isBlank(in.key())) { ctx.errors.add("team without key: " + in.name()); return; }
        String key = Slugs.of(in.key());
        Team t = ctx.teamsByKey.get(key);
        boolean created = t == null;
        if (created) { t = new Team(); t.setUniverseId(ctx.universeId); t.setKey(key); }
        if (in.name() != null) t.setName(in.name());
        if (t.getName() == null) { ctx.errors.add("team " + key + " needs a name"); return; }
        if (in.shortName() != null) t.setShortName(in.shortName());
        if (in.code() != null) t.setCode(in.code().toUpperCase(Locale.ROOT));
        if (in.type() != null) t.setType(TeamType.valueOf(in.type().toUpperCase(Locale.ROOT)));
        if (in.nation() != null) {
            Nation n = ctx.nationsByKey.get(Slugs.of(in.nation()));
            if (n == null) n = ctx.nationsByKey.values().stream().filter(x -> x.getCode().equalsIgnoreCase(in.nation()) || x.getName().equalsIgnoreCase(in.nation())).findFirst().orElse(null);
            if (n == null) { ctx.errors.add("team " + key + ": unknown nation '" + in.nation() + "'"); return; }
            t.setNationId(n.getId());
        }
        if (in.homeStadium() != null) {
            Stadium s = ctx.stadiumsByKey.get(Slugs.of(in.homeStadium()));
            if (s == null) ctx.warnings.add("team " + key + ": unknown stadium '" + in.homeStadium() + "' ignored");
            else t.setHomeStadiumId(s.getId());
        }
        if (in.description() != null) t.setDescription(in.description());
        if (in.foundedYear() != null) t.setFoundedYear(in.foundedYear());
        if (in.dissolvedYear() != null) t.setDissolvedYear(in.dissolvedYear());
        t = teams.save(t);
        ctx.addTeam(t);
        ctx.record("TEAM", t.getId(), created);
        for (String alias : nz(in.aliases())) {
            if (isBlank(alias)) continue;
            String lower = alias.trim().toLowerCase(Locale.ROOT);
            if (ctx.teamsByAlias.containsKey(lower)) continue;
            TeamAlias a = aliases.save(new TeamAlias(t.getId(), alias.trim()));
            ctx.teamsByAlias.put(lower, t.getId());
            ctx.record("TEAM_ALIAS", a.getId(), true);
        }
    }

    private void upsertProfile(ProfileIn in, Ctx ctx) {
        Team t = ctx.resolveTeam(in.team(), "teamProfiles");
        if (t == null || in.year() == null) { if (in.year() == null) ctx.errors.add("teamProfile for " + in.team() + " needs year"); return; }
        TeamProfile p = profiles.findByTeamIdAndYear(t.getId(), in.year()).orElse(null);
        boolean created = p == null;
        if (created) { p = new TeamProfile(); p.setTeamId(t.getId()); p.setYear(in.year()); }
        if (in.displayName() != null) p.setDisplayName(in.displayName());
        if (in.sponsor() != null) p.setSponsor(in.sponsor());
        if (in.colors() != null) p.setColors(Json.text(in.colors()));
        if (in.notes() != null) p.setNotes(in.notes());
        p = profiles.save(p);
        ctx.record("TEAM_PROFILE", p.getId(), created);
    }

    private void upsertKit(KitIn in, Ctx ctx) {
        Team t = ctx.resolveTeam(in.team(), "kits");
        if (t == null) return;
        if (in.year() == null || isBlank(in.kind())) { ctx.errors.add("kit for " + in.team() + " needs year and kind"); return; }
        KitKind kind = KitKind.valueOf(in.kind().toUpperCase(Locale.ROOT));
        Kit k = kits.findByTeamIdAndYearAndKind(t.getId(), in.year(), kind).orElse(null);
        boolean created = k == null;
        if (created) { k = new Kit(); k.setTeamId(t.getId()); k.setYear(in.year()); k.setKind(kind); }
        if (in.shirt() != null) k.setShirtColor(in.shirt());
        if (in.shorts() != null) k.setShortsColor(in.shorts());
        if (in.socks() != null) k.setSocksColor(in.socks());
        if (in.sponsor() != null) k.setSponsor(in.sponsor());
        k = kits.save(k);
        ctx.record("KIT", k.getId(), created);
    }

    private void upsertCompetition(CompetitionIn in, Ctx ctx) {
        if (isBlank(in.key())) { ctx.errors.add("competition without key"); return; }
        String key = Slugs.of(in.key());
        Competition c = ctx.competitionsByKey.get(key);
        boolean created = c == null;
        if (created) { c = new Competition(); c.setUniverseId(ctx.universeId); c.setKey(key); }
        if (in.name() != null) c.setName(in.name());
        if (c.getName() == null) c.setName(key);
        if (in.sport() != null) c.setSport(in.sport().toUpperCase(Locale.ROOT));
        if (in.teamLevel() != null) c.setTeamLevel(TeamType.valueOf(in.teamLevel().toUpperCase(Locale.ROOT)));
        if (in.tier() != null) c.setTier(in.tier());
        if (in.description() != null) c.setDescription(in.description());
        c = competitions.save(c);
        ctx.competitionsByKey.put(key, c);
        ctx.record("COMPETITION", c.getId(), created);
    }

    private void importSeason(SeasonIn in, Ctx ctx) {
        if (isBlank(in.competition()) || isBlank(in.key())) { ctx.errors.add("season needs competition and key"); return; }
        Competition c = ctx.competitionsByKey.get(Slugs.of(in.competition()));
        if (c == null) { ctx.errors.add("season " + in.key() + ": unknown competition '" + in.competition() + "'"); return; }
        String key = Slugs.of(in.key());
        String where = "season " + c.getKey() + "/" + key;

        Season s = seasons.findByCompetitionIdAndKey(c.getId(), key).orElse(null);
        boolean created = s == null;
        if (created) { s = new Season(); s.setCompetitionId(c.getId()); s.setKey(key); }
        else {
            stages.deleteBySeasonId(s.getId());
            seasonTeams.deleteBySeasonId(s.getId());
            honours.deleteBySeasonId(s.getId());
            stages.flush();
            ctx.log(where + ": existing structure replaced");
        }
        if (in.name() != null) s.setName(in.name());
        if (s.getName() == null) s.setName(key);
        if (in.year() != null) s.setYear(in.year());
        if (in.startDate() != null) s.setStartDate(date(in.startDate(), where, ctx));
        if (in.endDate() != null) s.setEndDate(date(in.endDate(), where, ctx));
        if (in.status() != null) s.setStatus(Enums.SeasonStatus.valueOf(in.status().toUpperCase(Locale.ROOT)));
        if (in.notes() != null) s.setNotes(in.notes());
        if (in.format() != null && !in.format().isNull()) {
            s.setFormat(formats.resolveFormatJson(null, in.format()));
            if (in.preset() != null) s.setPresetKey(in.preset());
        } else if (in.preset() != null) {
            s.setFormat(formats.resolveFormatJson(in.preset(), null));
            s.setPresetKey(in.preset());
        } else if (s.getFormat() == null) {
            ctx.errors.add(where + ": preset or format is required");
            return;
        }
        s = seasons.save(s);
        ctx.record("SEASON", s.getId(), created);
        ctx.counts.merge(created ? "SEASON created" : "SEASON replaced", 1, Integer::sum);
        FormatDefinition def = formats.parse(s.getFormat());

        for (SeasonTeamIn st : nz(in.teams())) {
            Team t = ctx.resolveTeam(st.team(), where + " teams");
            if (t == null) continue;
            if (seasonTeams.findBySeasonIdAndTeamId(s.getId(), t.getId()).isPresent()) continue;
            SeasonTeam e = new SeasonTeam();
            e.setSeasonId(s.getId());
            e.setTeamId(t.getId());
            e.setSeed(st.seed());
            e.setPot(st.pot());
            seasonTeams.save(e);
        }

        int ordinal = 1;
        for (StageIn si : nz(in.stages())) {
            importStage(s, def, si, ordinal++, where, ctx);
        }

        for (HonourIn h : nz(in.honours())) {
            Team t = ctx.resolveTeam(h.team(), where + " honours");
            if (t == null || isBlank(h.kind())) continue;
            Honour e = new Honour();
            e.setSeasonId(s.getId());
            e.setTeamId(t.getId());
            e.setKind(Enums.HonourKind.valueOf(h.kind().toUpperCase(Locale.ROOT)));
            e.setManual(true);
            honours.save(e);
            ctx.counts.merge("HONOUR", 1, Integer::sum);
        }

        if (ctx.errors.isEmpty()) {
            int tables = standingsService.recalculateSeason(s.getId());
            ctx.log(where + ": " + tables + " table(s) calculated");
        }
    }

    private void importStage(Season s, FormatDefinition def, StageIn si, int ordinal, String seasonWhere, Ctx ctx) {
        if (isBlank(si.key())) { ctx.errors.add(seasonWhere + ": stage without key"); return; }
        String where = seasonWhere + " stage " + si.key();
        StageDef fromFormat = def.stage(si.key()).orElse(null);
        StageType type = si.type() != null ? StageType.valueOf(si.type().toUpperCase(Locale.ROOT)) : (fromFormat != null ? fromFormat.type() : null);
        if (type == null) { ctx.errors.add(where + ": type missing and not found in format"); return; }
        Stage stage = new Stage();
        stage.setSeasonId(s.getId());
        stage.setOrdinal(ordinal);
        stage.setKey(si.key());
        stage.setName(si.name() != null ? si.name() : (fromFormat != null && fromFormat.name() != null ? fromFormat.name() : si.key()));
        stage.setType(type);
        stage.setConfig(si.config() != null && !si.config().isNull() ? si.config().toString() : (fromFormat != null ? json.write(fromFormat) : null));
        stage = stages.save(stage);
        ctx.counts.merge("STAGE", 1, Integer::sum);

        int gOrd = 1;
        for (GroupIn gi : nz(si.groups())) {
            StageGroup g = new StageGroup();
            g.setStageId(stage.getId());
            g.setOrdinal(gOrd++);
            g.setKey(gi.key() == null ? String.valueOf(gOrd - 1) : gi.key());
            g.setName(gi.name() == null ? "Group " + g.getKey() : gi.name());
            g = groups.save(g);
            ctx.counts.merge("GROUP", 1, Integer::sum);
            int pos = 1;
            List<Long> groupTeamIds = new ArrayList<>();
            for (String ref : nz(gi.teams())) {
                Team t = ctx.resolveTeam(ref, where + " group " + g.getKey());
                if (t == null || groupTeamIds.contains(t.getId())) continue;
                StageGroupTeam gt = new StageGroupTeam();
                gt.setStageGroupId(g.getId());
                gt.setTeamId(t.getId());
                gt.setPosition(pos++);
                groupTeams.save(gt);
                groupTeamIds.add(t.getId());
            }
            if (fromFormat != null && fromFormat.teamsPerGroup() != null && !groupTeamIds.isEmpty() && groupTeamIds.size() != fromFormat.teamsPerGroup()) {
                ctx.warnings.add(where + " group " + g.getKey() + ": " + groupTeamIds.size() + " teams, format expects " + fromFormat.teamsPerGroup());
            }
            int rNum = 1;
            for (RoundIn ri : nz(gi.rounds())) {
                Round r = new Round();
                r.setStageGroupId(g.getId());
                r.setNumber(ri.number() == null ? rNum : ri.number());
                rNum = r.getNumber() + 1;
                r.setName(ri.name() == null ? "Round " + r.getNumber() : ri.name());
                r.setStartDate(ri.startDate() == null ? null : date(ri.startDate(), where, ctx));
                r.setEndDate(ri.endDate() == null ? null : date(ri.endDate(), where, ctx));
                r = rounds.save(r);
                List<Long> seen = new ArrayList<>();
                for (MatchIn mi : nz(ri.matches())) {
                    Match m = toMatch(mi, s.getId(), stage.getId(), r.getId(), null, where + " round " + r.getNumber(), ctx);
                    if (m == null) continue;
                    for (Long id : List.of(nvl(m.getHomeTeamId()), nvl(m.getAwayTeamId()))) {
                        if (id != 0 && seen.contains(id)) ctx.warnings.add(where + " round " + r.getNumber() + ": team " + ctx.nameOf(id) + " plays twice");
                        if (id != 0) seen.add(id);
                    }
                    matches.save(m);
                    ctx.counts.merge("MATCH", 1, Integer::sum);
                }
            }
            for (RecordedIn rec : nz(gi.recordedStandings())) {
                List<StandingsService.RecordedRowInput> rows = new ArrayList<>();
                for (RecordedRowIn row : nz(rec.rows())) {
                    Team t = ctx.resolveTeam(row.team(), where + " recorded standings");
                    if (t == null) continue;
                    rows.add(new StandingsService.RecordedRowInput(t.getId(), row.position(), row.played(), row.won(), row.drawn(), row.lost(),
                            row.goalsFor(), row.goalsAgainst(), row.points(), row.zone()));
                }
                if (!rows.isEmpty()) {
                    standingsService.saveRecorded(g.getId(), rec.afterRound() == null ? 0 : rec.afterRound(), rows);
                    ctx.counts.merge("RECORDED_TABLE", 1, Integer::sum);
                }
            }
        }

        int kOrd = 1;
        for (KoRoundIn ki : nz(si.rounds())) {
            KoRound ko = new KoRound();
            ko.setStageId(stage.getId());
            ko.setOrdinal(kOrd++);
            ko.setKey(ki.key() == null ? "R" + (kOrd - 1) : ki.key());
            ko.setName(ki.name() == null ? ko.getKey() : ki.name());
            ko.setLegs(ki.legs() == null ? (fromFormat != null ? fromFormat.legsOrDefault() : 1) : ki.legs());
            ko.setPlacement(ki.placement() != null && ki.placement());
            ko = koRounds.save(ko);
            ctx.counts.merge("KO_ROUND", 1, Integer::sum);
            int tPos = 1;
            for (TieIn ti : nz(ki.ties())) {
                Tie t = new Tie();
                t.setKoRoundId(ko.getId());
                t.setPosition(ti.position() == null ? tPos : ti.position());
                tPos = t.getPosition() + 1;
                Team home = ti.home() == null ? null : ctx.resolveTeam(ti.home(), where + " " + ko.getKey());
                Team away = ti.away() == null ? null : ctx.resolveTeam(ti.away(), where + " " + ko.getKey());
                t.setHomeTeamId(home == null ? null : home.getId());
                t.setAwayTeamId(away == null ? null : away.getId());
                t.setHomeSource(Json.text(ti.homeSource()));
                t.setAwaySource(Json.text(ti.awaySource()));
                t = ties.save(t);
                ctx.counts.merge("TIE", 1, Integer::sum);
                int leg = 1;
                for (MatchIn mi : nz(ti.matches())) {
                    Match m = toMatch(mi, s.getId(), stage.getId(), null, t.getId(), where + " " + ko.getKey() + " tie " + t.getPosition(), ctx);
                    if (m == null) continue;
                    if (mi.leg() == null) m.setLeg(leg);
                    leg = m.getLeg() + 1;
                    if (m.getHomeTeamId() == null && t.getHomeTeamId() != null) { m.setHomeTeamId(t.getHomeTeamId()); m.setAwayTeamId(t.getAwayTeamId()); }
                    matches.save(m);
                    ctx.counts.merge("MATCH", 1, Integer::sum);
                }
                if (ti.winner() != null) {
                    Team w = ctx.resolveTeam(ti.winner(), where + " " + ko.getKey() + " winner");
                    if (w != null) t.setWinnerTeamId(w.getId());
                    if (ti.resolution() != null) t.setResolution(Enums.TieResolution.valueOf(ti.resolution().toUpperCase(Locale.ROOT)));
                    ties.save(t);
                } else if (ctx.errors.isEmpty()) {
                    competitionService.resolveTie(t);
                }
            }
        }
    }

    private Match toMatch(MatchIn mi, Long seasonId, Long stageId, Long roundId, Long tieId, String where, Ctx ctx) {
        Match m = new Match();
        m.setSeasonId(seasonId);
        m.setStageId(stageId);
        m.setRoundId(roundId);
        m.setTieId(tieId);
        m.setLeg(mi.leg() == null ? 1 : mi.leg());
        Team home = mi.home() == null ? null : ctx.resolveTeam(mi.home(), where);
        Team away = mi.away() == null ? null : ctx.resolveTeam(mi.away(), where);
        if ((mi.home() != null && home == null) || (mi.away() != null && away == null)) return null;
        m.setHomeTeamId(home == null ? null : home.getId());
        m.setAwayTeamId(away == null ? null : away.getId());
        m.setHomeScore(mi.homeScore());
        m.setAwayScore(mi.awayScore());
        m.setHomeEt(mi.homeEt());
        m.setAwayEt(mi.awayEt());
        m.setHomePens(mi.homePens());
        m.setAwayPens(mi.awayPens());
        if (mi.walkover() != null) m.setWalkover(Enums.Walkover.valueOf(mi.walkover().toUpperCase(Locale.ROOT)));
        if (mi.date() != null) m.setMatchDate(date(mi.date(), where, ctx));
        if (mi.stadium() != null) {
            Stadium st = ctx.stadiumsByKey.get(Slugs.of(mi.stadium()));
            if (st != null) m.setStadiumId(st.getId());
        }
        if (mi.status() != null) m.setStatus(Enums.MatchStatus.valueOf(mi.status().toUpperCase(Locale.ROOT)));
        else m.setStatus(mi.homeScore() != null && mi.awayScore() != null ? Enums.MatchStatus.PLAYED : (home == null || away == null ? Enums.MatchStatus.UNKNOWN : Enums.MatchStatus.SCHEDULED));
        m.setSource(mi.source() != null ? Enums.MatchSource.valueOf(mi.source().toUpperCase(Locale.ROOT)) : (mi.sourceRef() != null ? Enums.MatchSource.NOTEBOOK : Enums.MatchSource.IMPORT));
        m.setSourceRef(mi.sourceRef());
        if (mi.confidence() != null) m.setConfidence(Enums.Confidence.valueOf(mi.confidence().toUpperCase(Locale.ROOT)));
        m.setNotes(mi.notes());
        if (m.getWalkover() != null) m.setWinnerTeamId(m.getWalkover() == Enums.Walkover.HOME ? m.getHomeTeamId() : m.getAwayTeamId());
        else if (m.hasScore()) {
            int h = m.homeGoalsFinal(), a = m.awayGoalsFinal();
            if (h != a) m.setWinnerTeamId(h > a ? m.getHomeTeamId() : m.getAwayTeamId());
            else if (m.getHomePens() != null && m.getAwayPens() != null && !m.getHomePens().equals(m.getAwayPens()))
                m.setWinnerTeamId(m.getHomePens() > m.getAwayPens() ? m.getHomeTeamId() : m.getAwayTeamId());
        }
        return m;
    }

    private static LocalDate date(String text, String where, Ctx ctx) {
        try {
            return LocalDate.parse(text);
        } catch (RuntimeException e) {
            ctx.warnings.add(where + ": bad date '" + text + "' ignored");
            return null;
        }
    }

    private static long nvl(Long v) { return v == null ? 0 : v; }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static <T> List<T> nz(List<T> list) { return list == null ? List.of() : list; }

    private static String sha256(String text) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** Per-run working state. */
    private final class Ctx {
        final Long runId;
        final boolean dryRun;
        Long universeId;
        int ordinal = 0;
        final Map<String, Nation> nationsByKey = new HashMap<>();
        final Map<String, Stadium> stadiumsByKey = new HashMap<>();
        final Map<String, Team> teamsByKey = new HashMap<>();
        final Map<String, Long> teamsByName = new HashMap<>();
        final Map<String, Long> teamsByAlias = new HashMap<>();
        final Map<Long, Team> teamsById = new HashMap<>();
        final Map<String, Competition> competitionsByKey = new HashMap<>();
        final Map<String, Integer> counts = new LinkedHashMap<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final List<String> log = new ArrayList<>();

        Ctx(Long runId, boolean dryRun) { this.runId = runId; this.dryRun = dryRun; }

        void addTeam(Team t) {
            teamsByKey.put(t.getKey(), t);
            teamsByName.put(t.getName().toLowerCase(Locale.ROOT), t.getId());
            teamsById.put(t.getId(), t);
        }

        Team resolveTeam(String ref, String where) {
            if (isBlank(ref)) { errors.add(where + ": empty team reference"); return null; }
            String r = ref.trim();
            Team t = teamsByKey.get(r);
            if (t == null) t = teamsByKey.get(Slugs.of(r));
            if (t == null) {
                Long id = teamsByName.get(r.toLowerCase(Locale.ROOT));
                if (id == null) id = teamsByAlias.get(r.toLowerCase(Locale.ROOT));
                if (id != null) t = teamsById.get(id);
            }
            if (t == null) {
                String msg = "unknown team '" + r + "' (" + where + ")";
                if (!errors.contains(msg)) errors.add(msg);
            }
            return t;
        }

        String nameOf(Long id) {
            Team t = teamsById.get(id);
            return t == null ? String.valueOf(id) : t.getName();
        }

        void record(String type, Long id, boolean created) {
            counts.merge(type + (created ? " created" : " updated"), 1, Integer::sum);
            if (!dryRun) changes.save(new ImportChange(runId, ++ordinal, type, id, created ? "CREATED" : "UPDATED"));
        }

        void log(String line) { log.add(line); }
    }
}
