package com.fantasy.competition.controller;

import com.fantasy.competition.entity.Nation;
import com.fantasy.competition.entity.Universe;
import com.fantasy.competition.repository.NationRepository;
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

    @GetMapping
    public List<Nation> listByUniverse(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nation> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Nation> create(@RequestBody Nation nation, @RequestParam UUID universeId) {
        return universeRepo.findById(universeId).map(u -> {
            nation.setUniverse(u);
            return ResponseEntity.ok(repo.save(nation));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Nation> update(@PathVariable UUID id, @RequestBody Nation body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setFlagUrl(body.getFlagUrl());
            existing.setDescription(body.getDescription());
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
