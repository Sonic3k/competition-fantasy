package com.fantasy.competition.controller;

import com.fantasy.competition.dto.MatchDto;
import com.fantasy.competition.entity.Match;
import com.fantasy.competition.entity.Standing;
import com.fantasy.competition.repository.MatchRepository;
import com.fantasy.competition.repository.RoundRepository;
import com.fantasy.competition.repository.StandingRepository;
import com.fantasy.competition.repository.TeamRepository;
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
    private final TeamRepository teamRepo;
    private final StandingRepository standingRepo;

    @GetMapping
    public List<MatchDto> list(@RequestParam(required = false) UUID roundId,
                               @RequestParam(required = false) UUID seasonId) {
        if (roundId != null) return repo.findByRoundId(roundId).stream().map(MatchDto::from).toList();
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
            Match saved = repo.save(match);
            recalcStandings(match.getRound().getSeason().getId());
            return ResponseEntity.ok(MatchDto.from(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void recalcStandings(UUID seasonId) {
        List<Standing> standings = standingRepo.findBySeasonIdOrderByPointsDescGoalsForDesc(seasonId);
        standings.forEach(s -> { s.setPlayed(0); s.setWon(0); s.setDrawn(0); s.setLost(0); s.setGoalsFor(0); s.setGoalsAgainst(0); s.setPoints(0); });

        List<Match> matches = repo.findByRoundSeasonId(seasonId);
        for (Match m : matches) {
            if (m.getStatus() != Match.MatchStatus.COMPLETED || m.getHomeScore() == null) continue;
            Standing home = standings.stream().filter(s -> s.getTeam().getId().equals(m.getHomeTeam().getId())).findFirst().orElse(null);
            Standing away = standings.stream().filter(s -> s.getTeam().getId().equals(m.getAwayTeam().getId())).findFirst().orElse(null);
            if (home == null || away == null) continue;

            home.setPlayed(home.getPlayed() + 1); away.setPlayed(away.getPlayed() + 1);
            home.setGoalsFor(home.getGoalsFor() + m.getHomeScore()); home.setGoalsAgainst(home.getGoalsAgainst() + m.getAwayScore());
            away.setGoalsFor(away.getGoalsFor() + m.getAwayScore()); away.setGoalsAgainst(away.getGoalsAgainst() + m.getHomeScore());

            if (m.getHomeScore() > m.getAwayScore()) { home.setWon(home.getWon() + 1); home.setPoints(home.getPoints() + 3); away.setLost(away.getLost() + 1); }
            else if (m.getHomeScore() < m.getAwayScore()) { away.setWon(away.getWon() + 1); away.setPoints(away.getPoints() + 3); home.setLost(home.getLost() + 1); }
            else { home.setDrawn(home.getDrawn() + 1); home.setPoints(home.getPoints() + 1); away.setDrawn(away.getDrawn() + 1); away.setPoints(away.getPoints() + 1); }
        }
        standingRepo.saveAll(standings);
    }
}
