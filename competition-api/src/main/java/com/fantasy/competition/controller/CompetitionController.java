package com.fantasy.competition.controller;

import com.fantasy.competition.dto.CompetitionDto;
import com.fantasy.competition.entity.Competition;
import com.fantasy.competition.repository.CompetitionRepository;
import com.fantasy.competition.repository.UniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionRepository repo;
    private final UniverseRepository universeRepo;

    @GetMapping
    public List<CompetitionDto> list(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId).stream().map(CompetitionDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(CompetitionDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompetitionDto> create(@RequestBody Competition comp, @RequestParam UUID universeId) {
        return universeRepo.findById(universeId).map(u -> {
            comp.setUniverse(u);
            return ResponseEntity.ok(CompetitionDto.from(repo.save(comp)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionDto> update(@PathVariable UUID id, @RequestBody Competition body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setDescription(body.getDescription());
            existing.setType(body.getType());
            existing.setTeamLevel(body.getTeamLevel());
            return ResponseEntity.ok(CompetitionDto.from(repo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
