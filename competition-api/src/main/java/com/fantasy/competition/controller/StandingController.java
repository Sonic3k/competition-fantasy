package com.fantasy.competition.controller;

import com.fantasy.competition.dto.StandingDto;
import com.fantasy.competition.entity.Match;
import com.fantasy.competition.entity.Standing;
import com.fantasy.competition.entity.Team;
import com.fantasy.competition.repository.MatchRepository;
import com.fantasy.competition.repository.SeasonRepository;
import com.fantasy.competition.repository.StandingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/standings")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StandingController {

    private final StandingRepository repo;
    private final MatchRepository matchRepo;
    private final SeasonRepository seasonRepo;

    @GetMapping
    public List<StandingDto> list(@RequestParam UUID seasonId,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) Integer afterRound) {
        List<Standing> standings;

        if (type != null && afterRound != null) {
            standings = repo.findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(
                seasonId, Standing.StandingType.valueOf(type), afterRound);
        } else if (type != null) {
            standings = repo.findBySeasonIdAndTypeAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(
                seasonId, Standing.StandingType.valueOf(type));
        } else if (afterRound != null) {
            // Return both RECORDED and CALCULATED for comparison
            List<Standing> recorded = repo.findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(
                seasonId, Standing.StandingType.RECORDED, afterRound);
            List<Standing> calculated = repo.findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(
                seasonId, Standing.StandingType.CALCULATED, afterRound);
            standings = new ArrayList<>(recorded);
            standings.addAll(calculated);
        } else {
            standings = repo.findBySeasonIdAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(seasonId);
        }

        return standings.stream().map(StandingDto::from).toList();
    }

    @PostMapping("/calculate")
    @Transactional
    public ResponseEntity<List<StandingDto>> calculate(@RequestParam UUID seasonId,
                                                        @RequestParam(required = false) Integer afterRound) {
        return seasonRepo.findById(seasonId).map(season -> {
            // Get all completed matches, filter by round if needed
            List<Match> matches = matchRepo.findByRoundSeasonId(seasonId).stream()
                .filter(m -> m.getStatus() == Match.MatchStatus.COMPLETED && m.getHomeScore() != null)
                .filter(m -> afterRound == null || m.getRound().getRoundNumber() <= afterRound)
                .toList();

            // Get all teams in season
            Set<Team> teams = new HashSet<>(season.getTeams());

            // Delete existing CALCULATED standings for this checkpoint
            if (afterRound != null) {
                repo.deleteBySeasonIdAndTypeAndAfterRound(seasonId, Standing.StandingType.CALCULATED, afterRound);
            } else {
                repo.deleteBySeasonIdAndTypeAndAfterRoundIsNull(seasonId, Standing.StandingType.CALCULATED);
            }

            // Build standings map
            Map<UUID, Standing> map = new HashMap<>();
            for (Team t : teams) {
                Standing s = new Standing();
                s.setSeason(season);
                s.setTeam(t);
                s.setType(Standing.StandingType.CALCULATED);
                s.setAfterRound(afterRound);
                map.put(t.getId(), s);
            }

            // Calculate from matches
            for (Match m : matches) {
                Standing home = map.get(m.getHomeTeam().getId());
                Standing away = map.get(m.getAwayTeam().getId());
                if (home == null || away == null) continue;

                home.setPlayed(home.getPlayed() + 1);
                away.setPlayed(away.getPlayed() + 1);
                home.setGoalsFor(home.getGoalsFor() + m.getHomeScore());
                home.setGoalsAgainst(home.getGoalsAgainst() + m.getAwayScore());
                away.setGoalsFor(away.getGoalsFor() + m.getAwayScore());
                away.setGoalsAgainst(away.getGoalsAgainst() + m.getHomeScore());

                if (m.getHomeScore() > m.getAwayScore()) {
                    home.setWon(home.getWon() + 1); home.setPoints(home.getPoints() + 3);
                    away.setLost(away.getLost() + 1);
                } else if (m.getHomeScore() < m.getAwayScore()) {
                    away.setWon(away.getWon() + 1); away.setPoints(away.getPoints() + 3);
                    home.setLost(home.getLost() + 1);
                } else {
                    home.setDrawn(home.getDrawn() + 1); home.setPoints(home.getPoints() + 1);
                    away.setDrawn(away.getDrawn() + 1); away.setPoints(away.getPoints() + 1);
                }
            }

            List<Standing> saved = repo.saveAll(map.values());
            saved.sort((a, b) -> b.getPoints() != a.getPoints() ? b.getPoints() - a.getPoints()
                : b.getGoalDifference() != a.getGoalDifference() ? b.getGoalDifference() - a.getGoalDifference()
                : b.getGoalsFor() - a.getGoalsFor());

            return ResponseEntity.ok(saved.stream().map(StandingDto::from).toList());
        }).orElse(ResponseEntity.notFound().build());
    }
}
