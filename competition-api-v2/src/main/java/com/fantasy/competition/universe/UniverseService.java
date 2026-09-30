package com.fantasy.competition.universe;

import com.fantasy.competition.asset.AssetLookup;
import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.common.Slugs;
import com.fantasy.competition.competition.CompetitionRepository;
import com.fantasy.competition.team.NationRepository;
import com.fantasy.competition.team.TeamRepository;
import com.fantasy.competition.universe.UniverseDtos.UniverseDto;
import com.fantasy.competition.universe.UniverseDtos.UniverseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UniverseService {

    private final UniverseRepository universes;
    private final CompetitionRepository competitions;
    private final TeamRepository teams;
    private final NationRepository nations;
    private final AssetLookup assets;

    public UniverseService(UniverseRepository universes, CompetitionRepository competitions, TeamRepository teams,
                           NationRepository nations, AssetLookup assets) {
        this.universes = universes;
        this.competitions = competitions;
        this.teams = teams;
        this.nations = nations;
        this.assets = assets;
    }

    @Transactional(readOnly = true)
    public List<UniverseDto> list() {
        List<Universe> all = universes.findAllByOrderByNameAsc();
        List<Long> ids = all.stream().map(Universe::getId).toList();
        Map<Long, String> avatars = assets.currentUrls("UNIVERSE", ids, "AVATAR");
        Map<Long, String> banners = assets.currentUrls("UNIVERSE", ids, "BANNER");
        return all.stream().map(u -> toDto(u, avatars.get(u.getId()), banners.get(u.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public UniverseDto get(String key) {
        Universe u = byKey(key);
        return toDto(u, assets.currentUrls("UNIVERSE", List.of(u.getId()), "AVATAR").get(u.getId()),
                assets.currentUrls("UNIVERSE", List.of(u.getId()), "BANNER").get(u.getId()));
    }

    public Universe byKey(String key) {
        return universes.findByKey(key).orElseThrow(() -> new NotFoundException("Universe", key));
    }

    public Universe byId(Long id) {
        return universes.findById(id).orElseThrow(() -> new NotFoundException("Universe", id));
    }

    @Transactional
    public UniverseDto create(UniverseRequest req) {
        String key = Slugs.of(req.key());
        if (universes.findByKey(key).isPresent()) throw new BadRequestException("Universe key already exists: " + key);
        Universe u = new Universe();
        u.setKey(key);
        apply(u, req);
        return toDto(universes.save(u), null, null);
    }

    @Transactional
    public UniverseDto update(Long id, UniverseRequest req) {
        Universe u = byId(id);
        apply(u, req);
        return get(u.getKey());
    }

    @Transactional
    public void delete(Long id) {
        universes.delete(byId(id));
    }

    private void apply(Universe u, UniverseRequest req) {
        u.setName(req.name());
        u.setDescription(req.description());
        if (req.type() != null) u.setType(req.type());
        if (req.usesNations() != null) u.setUsesNations(req.usesNations());
    }

    private UniverseDto toDto(Universe u, String avatarUrl, String bannerUrl) {
        long comps = competitions.findByUniverseIdOrderByTierAscNameAsc(u.getId()).size();
        long teamCount = teams.countByUniverseId(u.getId());
        long nationCount = nations.findByUniverseIdOrderByNameAsc(u.getId()).size();
        return new UniverseDto(u.getId(), u.getKey(), u.getName(), u.getDescription(), u.getType().name(), u.isUsesNations(),
                comps, teamCount, nationCount, avatarUrl, bannerUrl);
    }
}
