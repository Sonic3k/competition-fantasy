package com.fantasy.competition.controller;

import com.fantasy.competition.entity.Universe;
import com.fantasy.competition.repository.UniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/universes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UniverseController {

    private final UniverseRepository repo;

    @GetMapping
    public List<Universe> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Universe> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Universe create(@RequestBody Universe universe) {
        return repo.save(universe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Universe> update(@PathVariable UUID id, @RequestBody Universe body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setDescription(body.getDescription());
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
