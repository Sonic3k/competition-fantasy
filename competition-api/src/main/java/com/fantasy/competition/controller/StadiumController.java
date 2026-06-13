package com.fantasy.competition.controller;

import com.fantasy.competition.dto.StadiumDto;
import com.fantasy.competition.entity.Stadium;
import com.fantasy.competition.repository.StadiumRepository;
import com.fantasy.competition.repository.UniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stadiums")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumRepository repo;
    private final UniverseRepository universeRepo;

    @GetMapping
    public List<StadiumDto> list(@RequestParam UUID universeId) {
        return repo.findByUniverseId(universeId).stream().map(StadiumDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<StadiumDto> create(@RequestBody Stadium body, @RequestParam UUID universeId) {
        return universeRepo.findById(universeId).map(u -> {
            body.setUniverse(u);
            return ResponseEntity.ok(StadiumDto.from(repo.save(body)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
