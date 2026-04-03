package com.fantasy.competition.controller;

import com.fantasy.competition.dto.TeamDto;
import com.fantasy.competition.entity.Team;
import com.fantasy.competition.repository.NationRepository;
import com.fantasy.competition.repository.TeamRepository;
import com.fantasy.competition.repository.UniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin("*")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository repo;
    private final UniverseRepository universeRepo;
    private final NationRepository nationRepo;

    @GetMapping
    public List<TeamDto> listByUniverse(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId).stream().map(TeamDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(TeamDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TeamDto> create(@RequestBody Team team, @RequestParam UUID universeId,
                                           @RequestParam(required = false) UUID nationId) {
        return universeRepo.findById(universeId).map(u -> {
            team.setUniverse(u);
            if (nationId != null) nationRepo.findById(nationId).ifPresent(team::setNation);
            return ResponseEntity.ok(TeamDto.from(repo.save(team)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> update(@PathVariable UUID id, @RequestBody Team body) {
        return repo.findById(id).map(existing -> {
            if (body.getName() != null) existing.setName(body.getName());
            if (body.getShortName() != null) existing.setShortName(body.getShortName());
            if (body.getDescription() != null) existing.setDescription(body.getDescription());
            if (body.getHomeBg() != null) existing.setHomeBg(body.getHomeBg());
            if (body.getHomeText() != null) existing.setHomeText(body.getHomeText());
            if (body.getAwayBg() != null) existing.setAwayBg(body.getAwayBg());
            if (body.getAwayText() != null) existing.setAwayText(body.getAwayText());
            return ResponseEntity.ok(TeamDto.from(repo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
