package com.fantasy.competition.controller;

import com.fantasy.competition.dto.TeamDto;
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
    public List<TeamDto> list(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId).stream().map(TeamDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(TeamDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TeamDto> create(@RequestBody Team team, @RequestParam UUID universeId,
                                        @RequestParam(required = false) UUID nationId,
                                        @RequestParam(required = false) UUID stadiumId) {
        return universeRepo.findById(universeId).map(u -> {
            team.setUniverse(u);
            if (nationId != null) nationRepo.findById(nationId).ifPresent(team::setNation);
            if (stadiumId != null) stadiumRepo.findById(stadiumId).ifPresent(team::setStadium);
            return ResponseEntity.ok(TeamDto.from(repo.save(team)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> update(@PathVariable UUID id, @RequestBody Team body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setShortName(body.getShortName());
            existing.setType(body.getType());
            existing.setPrimaryColor(body.getPrimaryColor());
            existing.setSecondaryColor(body.getSecondaryColor());
            existing.setLogoUrl(body.getLogoUrl());
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
