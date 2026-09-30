package com.fantasy.competition.team;

import com.fantasy.competition.asset.AssetLookup;
import com.fantasy.competition.asset.AssetRepository;
import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.Json;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.common.Slugs;
import com.fantasy.competition.competition.Competition;
import com.fantasy.competition.competition.CompetitionRepository;
import com.fantasy.competition.competition.Honour;
import com.fantasy.competition.competition.HonourRepository;
import com.fantasy.competition.competition.Season;
import com.fantasy.competition.competition.SeasonRepository;
import com.fantasy.competition.team.TeamDtos.KitDto;
import com.fantasy.competition.team.TeamDtos.KitRequest;
import com.fantasy.competition.team.TeamDtos.NationDto;
import com.fantasy.competition.team.TeamDtos.NationRequest;
import com.fantasy.competition.team.TeamDtos.ProfileDto;
import com.fantasy.competition.team.TeamDtos.ProfileRequest;
import com.fantasy.competition.team.TeamDtos.SeasonPlayed;
import com.fantasy.competition.team.TeamDtos.StadiumDto;
import com.fantasy.competition.team.TeamDtos.StadiumRequest;
import com.fantasy.competition.team.TeamDtos.TeamDetail;
import com.fantasy.competition.team.TeamDtos.TeamDto;
import com.fantasy.competition.team.TeamDtos.TeamRequest;
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

@Service
public class TeamService {

    private final UniverseRepository universes;
    private final NationRepository nations;
    private final StadiumRepository stadiums;
    private final TeamRepository teams;
    private final TeamAliasRepository aliases;
    private final TeamProfileRepository profiles;
    private final KitRepository kits;
    private final SeasonRepository seasons;
    private final CompetitionRepository competitions;
    private final HonourRepository honours;
    private final AssetLookup assets;
    private final AssetRepository assetRepository;
    private final Json json;

    public TeamService(UniverseRepository universes, NationRepository nations, StadiumRepository stadiums, TeamRepository teams,
                       TeamAliasRepository aliases, TeamProfileRepository profiles, KitRepository kits, SeasonRepository seasons,
                       CompetitionRepository competitions, HonourRepository honours, AssetLookup assets,
                       AssetRepository assetRepository, Json json) {
        this.universes = universes;
        this.nations = nations;
        this.stadiums = stadiums;
        this.teams = teams;
        this.aliases = aliases;
        this.profiles = profiles;
        this.kits = kits;
        this.seasons = seasons;
        this.competitions = competitions;
        this.honours = honours;
        this.assets = assets;
        this.assetRepository = assetRepository;
        this.json = json;
    }

    private Universe universe(String key) {
        return universes.findByKey(key).orElseThrow(() -> new NotFoundException("Universe", key));
    }

    // ---- nations ----

    @Transactional(readOnly = true)
    public List<NationDto> nations(String universeKey) {
        Universe u = universe(universeKey);
        List<Nation> list = nations.findByUniverseIdOrderByNameAsc(u.getId());
        List<Long> ids = list.stream().map(Nation::getId).toList();
        Map<Long, String> flags = assets.currentUrls("NATION", ids, "FLAG");
        Map<Long, String> emblems = assets.currentUrls("NATION", ids, "EMBLEM");
        return list.stream().map(n -> nationDto(n, flags.get(n.getId()), emblems.get(n.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public NationDto nation(Long id) {
        Nation n = nations.findById(id).orElseThrow(() -> new NotFoundException("Nation", id));
        return nationDto(n, assets.currentUrls("NATION", List.of(id), "FLAG").get(id), assets.currentUrls("NATION", List.of(id), "EMBLEM").get(id));
    }

    @Transactional
    public NationDto createNation(String universeKey, NationRequest req) {
        Universe u = universe(universeKey);
        String key = Slugs.of(req.key());
        if (nations.findByUniverseIdAndKey(u.getId(), key).isPresent()) throw new BadRequestException("Nation key already exists: " + key);
        Nation n = new Nation();
        n.setUniverseId(u.getId());
        n.setKey(key);
        applyNation(n, req);
        return nationDto(nations.save(n), null, null);
    }

    @Transactional
    public NationDto updateNation(Long id, NationRequest req) {
        Nation n = nations.findById(id).orElseThrow(() -> new NotFoundException("Nation", id));
        applyNation(n, req);
        nations.save(n);
        return nation(id);
    }

    @Transactional
    public void deleteNation(Long id) {
        nations.delete(nations.findById(id).orElseThrow(() -> new NotFoundException("Nation", id)));
    }

    private void applyNation(Nation n, NationRequest req) {
        n.setName(req.name());
        n.setCode(req.code().toUpperCase());
        n.setColors(Json.text(req.colors()));
        n.setDescription(req.description());
    }

    private static NationDto nationDto(Nation n, String flag, String emblem) {
        return new NationDto(n.getId(), n.getUniverseId(), n.getKey(), n.getName(), n.getCode(), n.getColors(), n.getDescription(), flag, emblem);
    }

    // ---- stadiums ----

    @Transactional(readOnly = true)
    public List<StadiumDto> stadiums(String universeKey) {
        Universe u = universe(universeKey);
        List<Stadium> list = stadiums.findByUniverseIdOrderByNameAsc(u.getId());
        Map<Long, String> images = assets.currentUrls("STADIUM", list.stream().map(Stadium::getId).toList(), "IMAGE");
        return list.stream().map(s -> stadiumDto(s, images.get(s.getId()))).toList();
    }

    @Transactional
    public StadiumDto createStadium(String universeKey, StadiumRequest req) {
        Universe u = universe(universeKey);
        String key = Slugs.of(req.key());
        if (stadiums.findByUniverseIdAndKey(u.getId(), key).isPresent()) throw new BadRequestException("Stadium key already exists: " + key);
        Stadium s = new Stadium();
        s.setUniverseId(u.getId());
        s.setKey(key);
        applyStadium(s, req);
        return stadiumDto(stadiums.save(s), null);
    }

    @Transactional
    public StadiumDto updateStadium(Long id, StadiumRequest req) {
        Stadium s = stadiums.findById(id).orElseThrow(() -> new NotFoundException("Stadium", id));
        applyStadium(s, req);
        return stadiumDto(stadiums.save(s), assets.currentUrls("STADIUM", List.of(id), "IMAGE").get(id));
    }

    @Transactional
    public void deleteStadium(Long id) {
        stadiums.delete(stadiums.findById(id).orElseThrow(() -> new NotFoundException("Stadium", id)));
    }

    private static void applyStadium(Stadium s, StadiumRequest req) {
        s.setName(req.name());
        s.setCity(req.city());
        s.setCapacity(req.capacity());
        s.setInspiredBy(req.inspiredBy());
        s.setDescription(req.description());
    }

    private static StadiumDto stadiumDto(Stadium s, String imageUrl) {
        return new StadiumDto(s.getId(), s.getUniverseId(), s.getKey(), s.getName(), s.getCity(), s.getCapacity(), s.getInspiredBy(), s.getDescription(), imageUrl);
    }

    // ---- teams ----

    @Transactional(readOnly = true)
    public List<TeamDto> teams(String universeKey, TeamType type) {
        Universe u = universe(universeKey);
        List<Team> list = type == null ? teams.findByUniverseIdOrderByNameAsc(u.getId()) : teams.findByUniverseIdAndTypeOrderByNameAsc(u.getId(), type);
        return teamDtos(list);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> teamsOfNation(Long nationId) {
        return teamDtos(teams.findByNationIdOrderByNameAsc(nationId));
    }

    @Transactional(readOnly = true)
    public TeamDetail teamDetail(Long id) {
        Team t = teams.findById(id).orElseThrow(() -> new NotFoundException("Team", id));
        TeamDto dto = teamDtos(List.of(t)).get(0);
        List<ProfileDto> profileDtos = profiles.findByTeamIdOrderByYearDesc(id).stream().map(TeamService::profileDto).toList();
        List<Kit> kitList = kits.findByTeamIdOrderByYearDescKindAsc(id);
        Map<Long, String> kitImages = new HashMap<>();
        List<Long> assetIds = kitList.stream().map(Kit::getImageAssetId).filter(x -> x != null).toList();
        if (!assetIds.isEmpty()) assetRepository.findByIdIn(assetIds).forEach(a -> kitImages.put(a.getId(), a.getUrl()));
        List<KitDto> kitDtos = kitList.stream().map(k -> kitDto(k, k.getImageAssetId() == null ? null : kitImages.get(k.getImageAssetId()))).toList();

        List<Season> played = seasons.findByTeamId(id);
        Map<Long, Competition> compById = new HashMap<>();
        Set<Long> compIds = new LinkedHashSet<>();
        played.forEach(s -> compIds.add(s.getCompetitionId()));
        if (!compIds.isEmpty()) competitions.findAllById(compIds).forEach(c -> compById.put(c.getId(), c));
        Map<Long, List<String>> honoursBySeason = new HashMap<>();
        for (Honour h : honours.findByTeamId(id)) {
            honoursBySeason.computeIfAbsent(h.getSeasonId(), x -> new ArrayList<>()).add(h.getKind().name());
        }
        List<SeasonPlayed> seasonPlayed = played.stream().map(s -> {
            Competition c = compById.get(s.getCompetitionId());
            return new SeasonPlayed(s.getId(), s.getName(), s.getYear(), s.getCompetitionId(), c == null ? null : c.getName(),
                    s.getStatus().name(), honoursBySeason.getOrDefault(s.getId(), List.of()));
        }).toList();
        return new TeamDetail(dto, profileDtos, kitDtos, seasonPlayed);
    }

    @Transactional
    public TeamDto createTeam(String universeKey, TeamRequest req) {
        Universe u = universe(universeKey);
        String key = Slugs.of(req.key());
        if (teams.findByUniverseIdAndKey(u.getId(), key).isPresent()) throw new BadRequestException("Team key already exists: " + key);
        Team t = new Team();
        t.setUniverseId(u.getId());
        t.setKey(key);
        applyTeam(t, req);
        t = teams.save(t);
        replaceAliases(t.getId(), req.aliases());
        return teamDtos(List.of(t)).get(0);
    }

    @Transactional
    public TeamDto updateTeam(Long id, TeamRequest req) {
        Team t = teams.findById(id).orElseThrow(() -> new NotFoundException("Team", id));
        applyTeam(t, req);
        t = teams.save(t);
        if (req.aliases() != null) replaceAliases(t.getId(), req.aliases());
        return teamDtos(List.of(t)).get(0);
    }

    @Transactional
    public void deleteTeam(Long id) {
        teams.delete(teams.findById(id).orElseThrow(() -> new NotFoundException("Team", id)));
    }

    private void applyTeam(Team t, TeamRequest req) {
        t.setName(req.name());
        t.setShortName(req.shortName());
        t.setCode(req.code() == null ? null : req.code().toUpperCase());
        if (req.type() != null) t.setType(req.type());
        t.setNationId(req.nationId());
        t.setHomeStadiumId(req.homeStadiumId());
        t.setDescription(req.description());
        t.setFoundedYear(req.foundedYear());
        t.setDissolvedYear(req.dissolvedYear());
    }

    private void replaceAliases(Long teamId, List<String> names) {
        aliases.deleteByTeamId(teamId);
        aliases.flush();
        if (names == null) return;
        Set<String> seen = new LinkedHashSet<>();
        for (String n : names) {
            if (n == null || n.isBlank()) continue;
            if (seen.add(n.trim().toLowerCase())) aliases.save(new TeamAlias(teamId, n.trim()));
        }
    }

    private List<TeamDto> teamDtos(List<Team> list) {
        if (list.isEmpty()) return List.of();
        List<Long> ids = list.stream().map(Team::getId).toList();
        Map<Long, List<String>> aliasByTeam = new HashMap<>();
        aliases.findByTeamIdIn(ids).forEach(a -> aliasByTeam.computeIfAbsent(a.getTeamId(), x -> new ArrayList<>()).add(a.getAlias()));
        Map<Long, String> logos = assets.currentUrls("TEAM", ids, "LOGO");
        Set<Long> nationIds = new LinkedHashSet<>();
        list.forEach(t -> { if (t.getNationId() != null) nationIds.add(t.getNationId()); });
        Map<Long, Nation> nationById = new HashMap<>();
        if (!nationIds.isEmpty()) nations.findAllById(nationIds).forEach(n -> nationById.put(n.getId(), n));
        List<TeamDto> out = new ArrayList<>();
        for (Team t : list) {
            Nation n = t.getNationId() == null ? null : nationById.get(t.getNationId());
            out.add(new TeamDto(t.getId(), t.getUniverseId(), t.getNationId(), n == null ? null : n.getCode(), n == null ? null : n.getName(),
                    t.getType().name(), t.getKey(), t.getName(), t.getShortName(), t.getCode(), t.getHomeStadiumId(), t.getDescription(),
                    t.getFoundedYear(), t.getDissolvedYear(), aliasByTeam.getOrDefault(t.getId(), List.of()), logos.get(t.getId())));
        }
        return out;
    }

    // ---- profiles and kits ----

    @Transactional
    public ProfileDto saveProfile(Long teamId, ProfileRequest req) {
        teams.findById(teamId).orElseThrow(() -> new NotFoundException("Team", teamId));
        if (req.year() == null) throw new BadRequestException("year is required");
        TeamProfile p = profiles.findByTeamIdAndYear(teamId, req.year()).orElseGet(TeamProfile::new);
        p.setTeamId(teamId);
        p.setYear(req.year());
        p.setDisplayName(req.displayName());
        p.setSponsor(req.sponsor());
        p.setColors(Json.text(req.colors()));
        p.setNotes(req.notes());
        return profileDto(profiles.save(p));
    }

    @Transactional
    public void deleteProfile(Long profileId) {
        profiles.deleteById(profileId);
    }

    @Transactional
    public KitDto saveKit(Long teamId, KitRequest req) {
        teams.findById(teamId).orElseThrow(() -> new NotFoundException("Team", teamId));
        if (req.year() == null || req.kind() == null) throw new BadRequestException("year and kind are required");
        Kit k = kits.findByTeamIdAndYearAndKind(teamId, req.year(), req.kind()).orElseGet(Kit::new);
        k.setTeamId(teamId);
        k.setYear(req.year());
        k.setKind(req.kind());
        k.setShirtColor(req.shirtColor());
        k.setShortsColor(req.shortsColor());
        k.setSocksColor(req.socksColor());
        k.setSponsor(req.sponsor());
        k.setImageAssetId(req.imageAssetId());
        k = kits.save(k);
        String url = k.getImageAssetId() == null ? null : assetRepository.findById(k.getImageAssetId()).map(a -> a.getUrl()).orElse(null);
        return kitDto(k, url);
    }

    @Transactional
    public void deleteKit(Long kitId) {
        kits.deleteById(kitId);
    }

    private static ProfileDto profileDto(TeamProfile p) {
        return new ProfileDto(p.getId(), p.getTeamId(), p.getYear(), p.getDisplayName(), p.getSponsor(), p.getColors(), p.getNotes());
    }

    private static KitDto kitDto(Kit k, String imageUrl) {
        return new KitDto(k.getId(), k.getTeamId(), k.getYear(), k.getKind().name(), k.getShirtColor(), k.getShortsColor(), k.getSocksColor(),
                k.getSponsor(), k.getImageAssetId(), imageUrl);
    }

    /** Resolves a name to a team within a universe: key, exact name, or alias (case-insensitive). */
    @Transactional(readOnly = true)
    public Team resolve(Long universeId, String ref) {
        if (ref == null || ref.isBlank()) throw new BadRequestException("team reference is empty");
        Team byKey = teams.findByUniverseIdAndKey(universeId, ref).orElse(null);
        if (byKey != null) return byKey;
        List<Team> byName = teams.findByUniverseIdAndNameIgnoreCase(universeId, ref.trim());
        if (byName.size() == 1) return byName.get(0);
        for (TeamAlias a : aliases.findByUniverseId(universeId)) {
            if (a.getAlias().equalsIgnoreCase(ref.trim())) {
                return teams.findById(a.getTeamId()).orElseThrow();
            }
        }
        throw new NotFoundException("Team", ref);
    }

    @SuppressWarnings("unused")
    private Json json() { return json; }
}
