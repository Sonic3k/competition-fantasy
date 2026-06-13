package com.fantasy.competition.controller;

import com.fantasy.competition.dto.StandingDto;
import com.fantasy.competition.entity.Standing;
import com.fantasy.competition.repository.StandingRepository;
import com.fantasy.competition.service.StandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/standings")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StandingController {

    private final StandingRepository repo;
    private final StandingService standingService;

    @GetMapping
    public List<StandingDto> list(@RequestParam UUID seasonId,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) Integer afterRound,
                                  @RequestParam(required = false) UUID stageGroupId) {
        List<Standing> standings;

        if (stageGroupId != null) {
            if (type != null) {
                standings = repo.findByStageGroupIdAndTypeOrderByPointsDescGoalsForDesc(stageGroupId, Standing.StandingType.valueOf(type));
            } else {
                standings = repo.findByStageGroupIdOrderByPointsDescGoalsForDesc(stageGroupId);
            }
        } else if (type != null && afterRound != null) {
            standings = repo.findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(seasonId, Standing.StandingType.valueOf(type), afterRound);
        } else if (type != null) {
            standings = repo.findBySeasonIdAndTypeAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(seasonId, Standing.StandingType.valueOf(type));
        } else if (afterRound != null) {
            List<Standing> recorded = repo.findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(seasonId, Standing.StandingType.RECORDED, afterRound);
            List<Standing> calculated = repo.findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(seasonId, Standing.StandingType.CALCULATED, afterRound);
            standings = new ArrayList<>(recorded);
            standings.addAll(calculated);
        } else {
            standings = repo.findBySeasonIdAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(seasonId);
        }

        return standings.stream().map(StandingDto::from).toList();
    }

    /** Recompute CALCULATED standings per group, applying the season's tie-break config. */
    @PostMapping("/calculate")
    public ResponseEntity<List<StandingDto>> calculate(@RequestParam UUID seasonId,
                                                        @RequestParam(required = false) Integer afterRound) {
        List<Standing> saved = standingService.recalculate(seasonId, afterRound);
        return ResponseEntity.ok(saved.stream().map(StandingDto::from).toList());
    }
}
