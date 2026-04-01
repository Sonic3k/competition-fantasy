package com.fantasy.competition.controller;

import com.fantasy.competition.dto.SeasonDto;
import com.fantasy.competition.entity.Season;
import com.fantasy.competition.repository.CompetitionRepository;
import com.fantasy.competition.repository.SeasonRepository;
import com.fantasy.competition.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seasons")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonRepository repo;
    private final CompetitionRepository competitionRepo;
    private final TeamRepository teamRepo;

    @GetMapping
    public List<SeasonDto> list(@RequestParam UUID competitionId) {
        return repo.findByCompetitionId(competitionId).stream().map(SeasonDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeasonDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(SeasonDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SeasonDto> create(@RequestBody Season season, @RequestParam UUID competitionId) {
        return competitionRepo.findById(competitionId).map(c -> {
            season.setCompetition(c);
            return ResponseEntity.ok(SeasonDto.from(repo.save(season)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/teams")
    public ResponseEntity<SeasonDto> addTeams(@PathVariable UUID id, @RequestBody List<UUID> teamIds) {
        return repo.findById(id).map(season -> {
            teamIds.forEach(tid -> teamRepo.findById(tid).ifPresent(t -> season.getTeams().add(t)));
            return ResponseEntity.ok(SeasonDto.from(repo.save(season)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeasonDto> update(@PathVariable UUID id, @RequestBody Season body) {
        return repo.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setYear(body.getYear());
            existing.setStatus(body.getStatus());
            return ResponseEntity.ok(SeasonDto.from(repo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
