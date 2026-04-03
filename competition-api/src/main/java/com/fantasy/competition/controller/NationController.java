package com.fantasy.competition.controller;

import com.fantasy.competition.dto.NationDto;
import com.fantasy.competition.entity.Nation;
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
    public List<NationDto> listByUniverse(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId).stream().map(NationDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NationDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(NationDto.from(e))).orElse(ResponseEntity.notFound().build());
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
            existing.setName(body.getName());
            existing.setFlagUrl(body.getFlagUrl());
            existing.setDescription(body.getDescription());
            existing.setCode(body.getCode());
            existing.setPrimaryColor(body.getPrimaryColor());
            existing.setTextColor(body.getTextColor());
            existing.setAwayColor(body.getAwayColor());
            existing.setAwayTextColor(body.getAwayTextColor());
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
