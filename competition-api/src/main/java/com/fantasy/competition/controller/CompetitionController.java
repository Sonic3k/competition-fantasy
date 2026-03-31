package com.fantasy.competition.controller;

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
    public List<Competition> list(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competition> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Competition> create(@RequestBody Competition comp, @RequestParam UUID universeId) {
        return universeRepo.findById(universeId).map(u -> {
            comp.setUniverse(u);
            return ResponseEntity.ok(repo.save(comp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Competition> update(@PathVariable UUID id, @RequestBody Competition body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setDescription(body.getDescription());
            existing.setType(body.getType());
            existing.setTeamLevel(body.getTeamLevel());
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
