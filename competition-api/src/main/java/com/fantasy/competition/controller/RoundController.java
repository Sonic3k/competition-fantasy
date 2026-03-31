package com.fantasy.competition.controller;

import com.fantasy.competition.entity.Round;
import com.fantasy.competition.repository.RoundRepository;
import com.fantasy.competition.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rounds")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RoundController {

    private final RoundRepository repo;
    private final SeasonRepository seasonRepo;

    @GetMapping
    public List<Round> list(@RequestParam UUID seasonId) {
        return repo.findBySeasonIdOrderByRoundNumber(seasonId);
    }

    @PostMapping
    public ResponseEntity<Round> create(@RequestBody Round round, @RequestParam UUID seasonId) {
        return seasonRepo.findById(seasonId).map(s -> {
            round.setSeason(s);
            return ResponseEntity.ok(repo.save(round));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
