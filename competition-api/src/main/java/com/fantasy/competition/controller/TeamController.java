package com.fantasy.competition.controller;

import com.fantasy.competition.entity.Team;
import com.fantasy.competition.repository.TeamRepository;
import com.fantasy.competition.repository.UniverseRepository;
import com.fantasy.competition.repository.NationRepository;
import com.fantasy.competition.repository.StadiumRepository;
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
    private final StadiumRepository stadiumRepo;

    @GetMapping
    public List<Team> list(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Team> create(@RequestBody Team team, @RequestParam UUID universeId,
                                        @RequestParam(required = false) UUID nationId,
                                        @RequestParam(required = false) UUID stadiumId) {
        return universeRepo.findById(universeId).map(u -> {
            team.setUniverse(u);
            if (nationId != null) nationRepo.findById(nationId).ifPresent(team::setNation);
            if (stadiumId != null) stadiumRepo.findById(stadiumId).ifPresent(team::setStadium);
            return ResponseEntity.ok(repo.save(team));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> update(@PathVariable UUID id, @RequestBody Team body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setShortName(body.getShortName());
            existing.setType(body.getType());
            existing.setPrimaryColor(body.getPrimaryColor());
            existing.setSecondaryColor(body.getSecondaryColor());
            existing.setLogoUrl(body.getLogoUrl());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
