package com.fantasy.competition.controller;

import com.fantasy.competition.dto.MatchDto;
import com.fantasy.competition.entity.Match;
import com.fantasy.competition.repository.MatchRepository;
import com.fantasy.competition.repository.RoundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin("*")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRepository repo;
    private final RoundRepository roundRepo;

    @GetMapping
    public List<MatchDto> list(@RequestParam(required = false) UUID roundId,
                               @RequestParam(required = false) UUID seasonId,
                               @RequestParam(required = false) UUID stageGroupId) {
        if (roundId != null) return repo.findByRoundId(roundId).stream().map(MatchDto::from).toList();
        if (stageGroupId != null) return repo.findByStageGroupId(stageGroupId).stream().map(MatchDto::from).toList();
        if (seasonId != null) return repo.findByRoundSeasonId(seasonId).stream().map(MatchDto::from).toList();
        return List.of();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(MatchDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MatchDto> create(@RequestBody Match match, @RequestParam UUID roundId) {
        return roundRepo.findById(roundId).map(r -> {
            match.setRound(r);
            return ResponseEntity.ok(MatchDto.from(repo.save(match)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/score")
    @Transactional
    public ResponseEntity<MatchDto> updateScore(@PathVariable UUID id,
                                                @RequestParam int homeScore,
                                                @RequestParam int awayScore) {
        return repo.findById(id).map(match -> {
            match.setHomeScore(homeScore);
            match.setAwayScore(awayScore);
            match.setStatus(Match.MatchStatus.COMPLETED);
            return ResponseEntity.ok(MatchDto.from(repo.save(match)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
