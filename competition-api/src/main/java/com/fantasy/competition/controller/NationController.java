package com.fantasy.competition.controller;

import com.fantasy.competition.dto.NationDto;
import com.fantasy.competition.dto.TeamDto;
import com.fantasy.competition.entity.Nation;
import com.fantasy.competition.repository.NationRepository;
import com.fantasy.competition.repository.TeamRepository;
import com.fantasy.competition.repository.UniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class NationController {

    private final NationRepository repo;
    private final UniverseRepository universeRepo;
    private final TeamRepository teamRepo;

    @GetMapping
    public List<NationDto> listByUniverse(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId).stream().map(NationDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NationDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(NationDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/teams")
    public List<TeamDto> getTeams(@PathVariable UUID id) {
        return teamRepo.findByNationId(id).stream().map(TeamDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<NationDto> create(@RequestBody Nation nation, @RequestParam UUID universeId) {
        return universeRepo.findById(universeId).map(u -> {
            nation.setUniverse(u);
            return ResponseEntity.ok(NationDto.from(repo.save(nation)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<NationDto> update(@PathVariable UUID id, @RequestBody Nation body) {
        return repo.findById(id).map(existing -> {
            if (body.getName() != null) existing.setName(body.getName());
            if (body.getCode() != null) existing.setCode(body.getCode());
            if (body.getDescription() != null) existing.setDescription(body.getDescription());
            if (body.getFlagUrl() != null) existing.setFlagUrl(body.getFlagUrl());
            if (body.getPrimaryColor() != null) existing.setPrimaryColor(body.getPrimaryColor());
            if (body.getTextColor() != null) existing.setTextColor(body.getTextColor());
            if (body.getAwayColor() != null) existing.setAwayColor(body.getAwayColor());
            if (body.getAwayTextColor() != null) existing.setAwayTextColor(body.getAwayTextColor());
            return ResponseEntity.ok(NationDto.from(repo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
